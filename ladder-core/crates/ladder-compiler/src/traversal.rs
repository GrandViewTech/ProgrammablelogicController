use ladder_model::{CoilType, Combinator, ColumnScreen, InputType, RowScreen, Screen};

use crate::target::Target;
use crate::types::{CompileError, LoadCombinator, LoadKind, OutputType};

fn create_label(row_number: u32, column_number: u32) -> String {
    format!("LABEL_{row_number}_{column_number}")
}

/// Reads the combinator directly off the block (requirements §7.3) instead
/// of inferring it. The first column on a row has no combinator.
///
/// A *later* column with no combinator is malformed input, not a defaultable
/// case: falling back to `LoadCombinator::None` would emit a plain
/// `MOV C, ACC.n`, silently overwriting the accumulated carry and producing
/// wrong logic on real hardware. Requirements §7.3 makes the combinator
/// mandatory for every block after the first, so this is rejected instead.
fn resolve_combinator(
    row: &RowScreen,
    column_index: usize,
    column: &ColumnScreen,
) -> Result<LoadCombinator, CompileError> {
    if row.previous(column_index).is_none() {
        return Ok(LoadCombinator::None);
    }
    match column.combinator {
        Some(Combinator::And) => Ok(LoadCombinator::Series),
        Some(Combinator::Or) => Ok(LoadCombinator::Parallel),
        // TODO(Task 3): XOR combining and nested-group evaluation are added by
        // the recursive-evaluation rewrite of this function (implementation
        // plan Task 3); this whole function is superseded there. Until then,
        // treat XOR the same as OR (LoadCombinator has no XOR variant yet) so
        // that a screen with an XOR-combined column compiles instead of
        // panicking a Tauri command on frontend-supplied JSON. This is a
        // temporary placeholder, not a design decision — Task 3 must replace
        // this with real XOR codegen.
        Some(Combinator::Xor) => Ok(LoadCombinator::Parallel),
        None => Err(CompileError::MissingCombinator {
            row: row.row_number,
            column: column.column_number,
        }),
    }
}

/// Ported from CompileService.isJumpRequired: true when the previous column
/// in the row is a LOAD.
fn is_jump_required(row: &RowScreen, column_index: usize) -> bool {
    matches!(row.previous(column_index), Some(previous) if previous.coil_type == CoilType::Load)
}

/// GAP (spec §3): the original's findOutputType(NoNc) lookup was disconnected
/// in ColumnScreen (the nonc field was commented out, so CompileService always
/// called it with null). This is carried forward as a documented gap, not
/// fixed: NoNc is not modeled on ColumnScreen, and every call site here always
/// resolves to OutputType::None, matching the original's actual behavior.
fn find_output_type() -> OutputType {
    OutputType::None
}

/// Faithful ASM emission for one screen. Runs synchronously; the Tauri
/// command wrapping this (Task 9) is what makes it async off the UI thread
/// (requirements §6, item 2) — `generate()` itself has no I/O.
pub fn generate(screen: &Screen, target: &dyn Target) -> Result<String, CompileError> {
    let mut buf: Vec<String> = Vec::new();

    'rows: for row in &screen.rows {
        for (column_index, column) in row.columns.iter().enumerate() {
            if column.is_blank {
                continue;
            }

            if column.coil_type == CoilType::End {
                break 'rows;
            }

            let label = create_label(row.row_number, column.column_number);

            match column.coil_type {
                CoilType::Load => {
                    let input = parse_value(row.row_number, column)?;
                    match column.input_type {
                        Some(InputType::Flag) => {
                            // GAP (spec §3): PreferenceScreen-backed min-value bounds for
                            // FLAG inputs aren't ported (no settings store exists yet) —
                            // the original's `min - input` offset is not applied.
                            let combinator = resolve_combinator(row, column_index, column)?;
                            target.emit_load(&mut buf, LoadKind::Flag, input, combinator);
                        }
                        Some(InputType::Input) => {
                            let combinator = resolve_combinator(row, column_index, column)?;
                            target.emit_load(&mut buf, LoadKind::Input, input, combinator);
                        }
                        Some(InputType::Word) => {
                            let combinator = resolve_combinator(row, column_index, column)?;
                            target.emit_load(&mut buf, LoadKind::Word, input, combinator);
                        }
                        Some(InputType::Output) => {
                            target.emit_output(&mut buf, input, find_output_type(), &label);
                        }
                        None => {}
                    }
                }
                CoilType::Output => {
                    let value = parse_value(row.row_number, column)?;
                    target.emit_output(&mut buf, value, find_output_type(), &label);
                }
                CoilType::Routine => {
                    let jump = is_jump_required(row, column_index);
                    if jump {
                        target.emit_jnc(&mut buf, &label);
                    }
                    let asm = column.rendered_asm.as_deref().ok_or(CompileError::MissingRenderedAsm {
                        row: row.row_number,
                        column: column.column_number,
                    })?;
                    buf.push(asm.to_string());
                    if jump {
                        target.emit_label(&mut buf, &label);
                    }
                }
                _ => {
                    // LABEL, JUMP, LINE, LEFT_LINK, RIGHT_LINK, PARALLEL, DEFAULT,
                    // DELETE are UI-only markers in the original — CompileService's
                    // traversal never emits ASM for them either.
                }
            }
        }
    }

    Ok(buf.join("\n"))
}

fn parse_value(row_number: u32, column: &ColumnScreen) -> Result<i32, CompileError> {
    column.value.parse::<i32>().map_err(|_| CompileError::InvalidValue {
        row: row_number,
        column: column.column_number,
        value: column.value.clone(),
    })
}

#[cfg(test)]
mod tests {
    use super::*;
    use ladder_model::{CoilType, ColumnScreen, RoutineOrigin};
    use std::cell::RefCell;

    struct RecordingTarget {
        calls: RefCell<Vec<String>>,
    }

    impl RecordingTarget {
        fn new() -> Self {
            Self { calls: RefCell::new(Vec::new()) }
        }
    }

    impl Target for RecordingTarget {
        fn emit_load(&self, buf: &mut Vec<String>, kind: LoadKind, input: i32, combinator: LoadCombinator) {
            let line = format!("LOAD {kind:?} {input} {combinator:?}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_output(&self, buf: &mut Vec<String>, value: i32, output_type: OutputType, label: &str) {
            let line = format!("OUTPUT {value} {output_type:?} {label}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_jnc(&self, buf: &mut Vec<String>, label: &str) {
            let line = format!("JNC {label}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_label(&self, buf: &mut Vec<String>, label: &str) {
            let line = format!("{label}:");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_not(&self, buf: &mut Vec<String>) {
            let line = "NOT".to_string();
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_xor(&self, buf: &mut Vec<String>) {
            let line = "XOR".to_string();
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_relay_write(&self, buf: &mut Vec<String>, byte: u32, bit: u32) {
            let line = format!("RELAY_WRITE {byte} {bit}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
    }

    fn column(coil_type: CoilType) -> ColumnScreen {
        ColumnScreen {
            row_number: 1,
            column_number: 1,
            coil_type,
            input_type: None,
            value: "0".into(),
            tag: String::new(),
            comment: String::new(),
            routine_origin: None,
            rendered_asm: None,
            combinator: None,
            is_blank: false,
            inverted: false,
            group: None,
            row_ref_name: None,
        }
    }

    #[test]
    fn end_coil_stops_traversal_before_later_rows() {
        let screen = Screen {
            rows: vec![
                RowScreen { row_number: 1, columns: vec![column(CoilType::End)], output_name: None },
                RowScreen {
                    row_number: 2,
                    columns: vec![ColumnScreen { input_type: Some(InputType::Input), ..column(CoilType::Load) }],
                    output_name: None,
                },
            ],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "");
        assert!(target.calls.borrow().is_empty());
    }

    #[test]
    fn load_input_emits_with_none_combinator_when_first_on_row() {
        let mut c = column(CoilType::Load);
        c.input_type = Some(InputType::Input);
        c.value = "19".into();
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![c], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 19 None");
    }

    #[test]
    fn second_block_uses_explicit_combinator_not_inference() {
        let mut first = column(CoilType::Load);
        first.input_type = Some(InputType::Input);
        first.value = "1".into();
        let mut second = column(CoilType::Load);
        second.column_number = 2;
        second.input_type = Some(InputType::Input);
        second.value = "2".into();
        second.combinator = Some(Combinator::Or);
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![first, second], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 1 None\nLOAD Input 2 Parallel");
    }

    #[test]
    fn xor_combinator_compiles_without_panicking_as_temporary_or_fallback() {
        // TODO(Task 3): once real XOR codegen exists, this should assert on
        // XOR-specific output, not "Parallel". For now this only proves the
        // `todo!()` panic reachable via frontend-supplied JSON is gone.
        let mut first = column(CoilType::Load);
        first.input_type = Some(InputType::Input);
        first.value = "1".into();
        let mut second = column(CoilType::Load);
        second.column_number = 2;
        second.input_type = Some(InputType::Input);
        second.value = "2".into();
        second.combinator = Some(Combinator::Xor);
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![first, second], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 1 None\nLOAD Input 2 Parallel");
    }

    #[test]
    fn second_block_without_combinator_is_a_compile_error() {
        // Requirements §7.3: every block after the first carries an explicit
        // combinator. Defaulting to `None` here would emit `MOV C, ACC.n`,
        // clobbering the accumulated carry instead of AND/OR-ing into it.
        let mut first = column(CoilType::Load);
        first.input_type = Some(InputType::Input);
        first.value = "1".into();
        let mut second = column(CoilType::Load);
        second.column_number = 2;
        second.input_type = Some(InputType::Input);
        second.value = "2".into();
        second.combinator = None;
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![first, second], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(err, CompileError::MissingCombinator { row: 1, column: 2 });
    }

    #[test]
    fn routine_after_load_wraps_with_jnc_and_label() {
        let mut load = column(CoilType::Load);
        load.input_type = Some(InputType::Input);
        load.value = "1".into();
        let mut routine = column(CoilType::Routine);
        routine.column_number = 2;
        routine.routine_origin = Some(RoutineOrigin {
            routine_name: "BIT RESET".into(),
            description: "Resets the bit".into(),
            values: Default::default(),
        });
        routine.rendered_asm = Some("MOV DPTR,#RLY512_519+".into());
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![load, routine], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nJNC LABEL_1_2\nMOV DPTR,#RLY512_519+\nLABEL_1_2:"
        );
    }

    #[test]
    fn routine_without_rendered_asm_is_a_compile_error() {
        let mut routine = column(CoilType::Routine);
        routine.rendered_asm = None;
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![routine], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(err, CompileError::MissingRenderedAsm { row: 1, column: 1 });
    }

    #[test]
    fn invalid_value_is_a_compile_error_not_a_panic() {
        let mut c = column(CoilType::Output);
        c.value = "not-a-number".into();
        let screen = Screen {
            rows: vec![RowScreen { row_number: 3, columns: vec![c], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(
            err,
            CompileError::InvalidValue { row: 3, column: 1, value: "not-a-number".into() }
        );
    }
}
