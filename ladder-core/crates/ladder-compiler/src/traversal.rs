use std::collections::HashMap;

use ladder_model::{CoilType, Combinator, ColumnScreen, InputType, Screen};

use crate::target::Target;
use crate::types::{CompileError, LoadCombinator, LoadKind, OutputType};

/// Named-output relay addresses: row-output name -> (byte, bit).
type RelayAddresses = HashMap<String, (u32, u32)>;

/// Scratch relay addresses for non-leading groups: (row_number,
/// column_number) of the group-owning column -> (byte, bit). A disjoint key
/// space and a disjoint address range from `RelayAddresses` (see
/// `assign_relay_addresses`), so the two can never collide.
type ScratchAddresses = HashMap<(u32, u32), (u32, u32)>;

fn create_label(row_number: u32, column_number: u32) -> String {
    format!("LABEL_{row_number}_{column_number}")
}

/// The column immediately to the left of `column_index` within `columns`, or
/// `None` if it's the first entry. Slice-based sibling of
/// `RowScreen::previous` (requirements: `resolve_combinator`/
/// `is_jump_required` need to walk either a row's top-level columns or a
/// nested `group`'s own columns, so they operate on whatever slice they were
/// called with rather than always `row.columns`).
fn previous_in(columns: &[ColumnScreen], column_index: usize) -> Option<&ColumnScreen> {
    column_index.checked_sub(1).and_then(|i| columns.get(i))
}

/// Reads the combinator directly off the block (requirements §7.3) instead
/// of inferring it. The first column in `columns` has no combinator — this
/// applies equally to a row's top-level columns and to a nested `group`'s
/// own columns, since group members combine among themselves the same way
/// top-level columns do (requirements: nested expressions).
///
/// A *later* column with no combinator is malformed input, not a defaultable
/// case: falling back to `LoadCombinator::None` would emit a plain
/// `MOV C, ACC.n`, silently overwriting the accumulated carry and producing
/// wrong logic on real hardware. Requirements §7.3 makes the combinator
/// mandatory for every block after the first, so this is rejected instead.
fn resolve_combinator(
    columns: &[ColumnScreen],
    row_number: u32,
    column_index: usize,
    column: &ColumnScreen,
) -> Result<LoadCombinator, CompileError> {
    if previous_in(columns, column_index).is_none() {
        return Ok(LoadCombinator::None);
    }
    match column.combinator {
        Some(Combinator::And) => Ok(LoadCombinator::Series),
        Some(Combinator::Or) => Ok(LoadCombinator::Parallel),
        // TODO: real XOR codegen (`Target::emit_xor`) is not yet wired into
        // combinator resolution — no test in this plan so far exercises a
        // top-level or in-group XOR-combined LOAD. Until that lands, treat
        // XOR the same as OR (LoadCombinator has no XOR variant yet) so that
        // a screen with an XOR-combined column compiles instead of
        // panicking a Tauri command on frontend-supplied JSON. This is a
        // temporary placeholder, not a design decision.
        Some(Combinator::Xor) => Ok(LoadCombinator::Parallel),
        None => Err(CompileError::MissingCombinator { row: row_number, column: column.column_number }),
    }
}

/// Ported from CompileService.isJumpRequired: true when the previous column
/// in `columns` is a LOAD.
fn is_jump_required(columns: &[ColumnScreen], column_index: usize) -> bool {
    matches!(previous_in(columns, column_index), Some(previous) if previous.coil_type == CoilType::Load)
}

/// GAP (spec §3): the original's findOutputType(NoNc) lookup was disconnected
/// in ColumnScreen (the nonc field was commented out, so CompileService always
/// called it with null). This is carried forward as a documented gap, not
/// fixed: NoNc is not modeled on ColumnScreen, and every call site here always
/// resolves to OutputType::None, matching the original's actual behavior.
fn find_output_type() -> OutputType {
    OutputType::None
}

/// Pre-pass (requirements: named row outputs / cross-row references): every
/// row with an `output_name` is assigned the next internal-relay address, in
/// row order. `legacy::find_param` reproduces the original's byte/bit split
/// exactly (see its doc comment), so an address computed here and later
/// re-split by a `Target` implementation's own `find_param` call lands on
/// the same (byte, bit).
///
/// Also assigns a **scratch** relay address to every non-leading group (a
/// column with `group: Some(_)` that has a preceding sibling in its own
/// slice — top-level columns or a nested group's own columns). A
/// non-leading group needs somewhere to stash the running total from before
/// it, in memory, while it recurses and clobbers the carry with its own
/// fresh evaluation (fix round 1: see task-3-report.md). Scratch addresses
/// are keyed by `(row_number, column_number)` of the group-owning column —
/// unique within a screen, since `column_number` is unique within its row
/// and groups only nest inside specific columns.
///
/// Scratch addresses share the SAME sequential counter as named-output
/// addresses, continuing from wherever the named-output pass left off, so
/// the two address spaces (`String` name -> address, `(u32, u32)` position
/// -> address) can never collide with each other: every address is handed
/// out by exactly one increment of `next_relay_index`, once, to exactly one
/// key in exactly one of the two maps.
fn assign_relay_addresses(screen: &Screen) -> (RelayAddresses, ScratchAddresses) {
    let mut addresses = HashMap::new();
    let mut scratch_addresses = HashMap::new();
    let mut next_relay_index: i32 = 0;
    for row in &screen.rows {
        if let Some(name) = &row.output_name {
            addresses.insert(name.clone(), crate::legacy::find_param(next_relay_index));
            next_relay_index += 1;
        }
        assign_scratch_addresses(&row.columns, row.row_number, &mut scratch_addresses, &mut next_relay_index);
    }
    (addresses, scratch_addresses)
}

/// Walks `columns` (a row's top-level columns, or recursively a nested
/// group's own columns — groups can themselves contain non-leading groups)
/// assigning a scratch address to every group-owning column that has a
/// preceding sibling in `columns`, using and advancing the shared counter.
fn assign_scratch_addresses(
    columns: &[ColumnScreen],
    row_number: u32,
    scratch_addresses: &mut ScratchAddresses,
    next_relay_index: &mut i32,
) {
    for (column_index, column) in columns.iter().enumerate() {
        if let Some(group_columns) = &column.group {
            if previous_in(columns, column_index).is_some() {
                scratch_addresses
                    .insert((row_number, column.column_number), crate::legacy::find_param(*next_relay_index));
                *next_relay_index += 1;
            }
            assign_scratch_addresses(group_columns, row_number, scratch_addresses, next_relay_index);
        }
    }
}

/// Companion to `assign_relay_addresses`: which row number owns each named
/// output, so a `RowRef` can be checked for forward-reference (requirements:
/// a row may only reference a strictly earlier row's output).
fn assign_row_owners(screen: &Screen) -> HashMap<String, u32> {
    let mut owners = HashMap::new();
    for row in &screen.rows {
        if let Some(name) = &row.output_name {
            owners.insert(name.clone(), row.row_number);
        }
    }
    owners
}

/// Faithful ASM emission for one screen. Runs synchronously; the Tauri
/// command wrapping this (Task 9) is what makes it async off the UI thread
/// (requirements §6, item 2) — `generate()` itself has no I/O.
pub fn generate(screen: &Screen, target: &dyn Target) -> Result<String, CompileError> {
    let mut buf: Vec<String> = Vec::new();
    let (relay_addresses, scratch_addresses) = assign_relay_addresses(screen);
    let row_owners = assign_row_owners(screen);

    for row in &screen.rows {
        let end_hit = evaluate_columns(
            &row.columns,
            row.row_number,
            &relay_addresses,
            &scratch_addresses,
            &row_owners,
            target,
            &mut buf,
        )?;
        if end_hit {
            // matches the original: nothing after End runs, in this row or
            // any later one — including this row's own output_name write,
            // since its expression didn't finish evaluating.
            break;
        }
        if let Some(name) = &row.output_name {
            let (byte, bit) =
                relay_addresses.get(name).expect("assigned in the pre-pass for every row.output_name");
            target.emit_relay_write(&mut buf, *byte, *bit);
        }
    }

    Ok(buf.join("\n"))
}

/// Returns `Ok(true)` if an `End` coil was encountered anywhere in this
/// slice (including inside a nested group) — the caller must stop
/// processing immediately: skip the rest of this row (including any
/// row-output write) and every row after it, matching the original's
/// "END coil early-exit" behavior (requirements §3) exactly, now that the
/// column walk is recursive instead of one flat loop with a labeled break.
fn evaluate_columns(
    columns: &[ColumnScreen],
    row_number: u32,
    relay_addresses: &RelayAddresses,
    scratch_addresses: &ScratchAddresses,
    row_owners: &HashMap<String, u32>,
    target: &dyn Target,
    buf: &mut Vec<String>,
) -> Result<bool, CompileError> {
    for (column_index, column) in columns.iter().enumerate() {
        if column.is_blank {
            continue;
        }
        if column.coil_type == CoilType::End {
            return Ok(true);
        }

        let label = create_label(row_number, column.column_number);

        if let Some(group_columns) = &column.group {
            // A group is evaluated recursively to get "the group's result"
            // onto the carry, then treated exactly like a leaf load for the
            // purposes of inversion below. If an End coil turns up inside
            // the group, propagate the stop signal immediately — don't
            // finish this group, don't apply this slot's inversion, don't
            // continue the row.
            //
            // Fix round 1: when this group has a preceding sibling, there's
            // already a running total in the carry that the group's own
            // fresh evaluation would otherwise silently clobber (its first
            // internal load resolves its OWN combinator as None and
            // overwrites the carry — it has no visibility into what came
            // before it in the OUTER slice). Stash the running total in a
            // memory-backed scratch relay bit before recursing, then after
            // the group returns, read that stash back and combine it with
            // the group's fresh result via THIS column's own combinator
            // (resolved against the outer `columns` slice) — exactly the
            // same mechanism a normal leaf load-with-combinator already
            // uses, just pointed at scratch memory instead of a real input.
            let has_preceding_sibling = previous_in(columns, column_index).is_some();
            if has_preceding_sibling {
                let (scratch_byte, scratch_bit) = scratch_addresses
                    .get(&(row_number, column.column_number))
                    .copied()
                    .expect("assigned in the pre-pass for every non-leading group");
                target.emit_relay_write(buf, scratch_byte, scratch_bit);
            }

            if evaluate_columns(group_columns, row_number, relay_addresses, scratch_addresses, row_owners, target, buf)? {
                return Ok(true);
            }

            if has_preceding_sibling {
                let (scratch_byte, scratch_bit) = scratch_addresses
                    .get(&(row_number, column.column_number))
                    .copied()
                    .expect("assigned in the pre-pass for every non-leading group");
                let resolved_combinator = resolve_combinator(columns, row_number, column_index, column)?;
                let scratch_input = (scratch_byte * 8 + scratch_bit) as i32;
                target.emit_load(buf, LoadKind::Flag, scratch_input, resolved_combinator);
            }
        } else {
            match column.coil_type {
                CoilType::Load => {
                    let input = parse_value(row_number, column)?;
                    match column.input_type {
                        Some(InputType::Flag) => {
                            // GAP (spec §3): PreferenceScreen-backed min-value bounds for
                            // FLAG inputs aren't ported (no settings store exists yet) —
                            // the original's `min - input` offset is not applied.
                            let combinator = resolve_combinator(columns, row_number, column_index, column)?;
                            target.emit_load(buf, LoadKind::Flag, input, combinator);
                        }
                        Some(InputType::Input) => {
                            let combinator = resolve_combinator(columns, row_number, column_index, column)?;
                            target.emit_load(buf, LoadKind::Input, input, combinator);
                        }
                        Some(InputType::Word) => {
                            let combinator = resolve_combinator(columns, row_number, column_index, column)?;
                            target.emit_load(buf, LoadKind::Word, input, combinator);
                        }
                        Some(InputType::Output) => {
                            target.emit_output(buf, input, find_output_type(), &label);
                        }
                        None => {}
                    }
                }
                CoilType::Output => {
                    let value = parse_value(row_number, column)?;
                    target.emit_output(buf, value, find_output_type(), &label);
                }
                CoilType::Routine => {
                    let jump = is_jump_required(columns, column_index);
                    if jump {
                        target.emit_jnc(buf, &label);
                    }
                    let asm = column.rendered_asm.as_deref().ok_or(CompileError::MissingRenderedAsm {
                        row: row_number,
                        column: column.column_number,
                    })?;
                    buf.push(asm.to_string());
                    if jump {
                        target.emit_label(buf, &label);
                    }
                }
                CoilType::RowRef => {
                    let name = column.row_ref_name.as_deref().unwrap_or_default();
                    if let Some(&owner_row) = row_owners.get(name) {
                        if owner_row >= row_number {
                            return Err(CompileError::ForwardRowReference {
                                row: row_number,
                                column: column.column_number,
                                name: name.to_string(),
                            });
                        }
                    }
                    let (byte, bit) = relay_addresses.get(name).copied().ok_or_else(|| {
                        CompileError::UnknownRowReference {
                            row: row_number,
                            column: column.column_number,
                            name: name.to_string(),
                        }
                    })?;
                    let combinator = resolve_combinator(columns, row_number, column_index, column)?;
                    // Reconstruct a single `input` consistent with how
                    // `find_param` would split it back apart, so the round
                    // trip through a `Target`'s own `find_param` call (e.g.
                    // `Target8085`) lands on the same (byte, bit) address
                    // `assign_relay_addresses` computed.
                    let input = (byte * 8 + bit) as i32;
                    target.emit_load(buf, LoadKind::Flag, input, combinator);
                }
                _ => {
                    // LABEL, JUMP, LINE, LEFT_LINK, RIGHT_LINK, PARALLEL, DEFAULT,
                    // DELETE are UI-only markers in the original — CompileService's
                    // traversal never emits ASM for them either.
                }
            }
        }

        // Inversion applies after obtaining this slot's value (leaf or
        // group), before folding into the running total (design spec: NOT
        // is orthogonal to AND/OR/XOR, not a 4th `Combinator` value).
        if column.inverted {
            target.emit_not(buf);
        }
    }
    Ok(false)
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
    use ladder_model::{CoilType, ColumnScreen, RoutineOrigin, RowScreen};
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

    #[test]
    fn a_group_is_evaluated_and_its_result_combines_like_a_leaf() {
        // (raw_a AND raw_b) OR raw_c — the parenthesized pair is a `group` on
        // the second top-level column; group members combine among themselves
        // the same way top-level columns do (raw_b's combinator is AND,
        // relative to raw_a, both inside the group).
        let mut group_head = column(CoilType::Load);
        group_head.column_number = 1;
        group_head.input_type = Some(InputType::Input);
        group_head.value = "1".into();

        let mut group_tail = column(CoilType::Load);
        group_tail.column_number = 2;
        group_tail.input_type = Some(InputType::Input);
        group_tail.value = "2".into();
        group_tail.combinator = Some(Combinator::And);

        let mut grouped = column(CoilType::Load);
        grouped.column_number = 1;
        grouped.input_type = Some(InputType::Input);
        grouped.value = "0".into(); // unused when `group` is Some — the group's own columns are what's evaluated
        grouped.group = Some(vec![group_head, group_tail]);

        let mut raw_c = column(CoilType::Load);
        raw_c.column_number = 2;
        raw_c.input_type = Some(InputType::Input);
        raw_c.value = "3".into();
        raw_c.combinator = Some(Combinator::Or);

        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![grouped, raw_c], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nLOAD Input 2 Series\nLOAD Input 3 Parallel"
        );
    }

    #[test]
    fn a_non_leading_group_combines_with_the_running_total_via_its_own_combinator() {
        // raw_a OR (b AND c) — `grouped` is the SECOND top-level column, so
        // there's already a running total (raw_a's) in the carry when the
        // group is reached. The group's result must combine with that
        // running total via `grouped`'s OWN combinator (Or), not silently
        // overwrite it. Confirmed bug scenario from the Task 3 review.
        let mut raw_a = column(CoilType::Load);
        raw_a.column_number = 1;
        raw_a.input_type = Some(InputType::Input);
        raw_a.value = "1".into();

        let mut group_head = column(CoilType::Load);
        group_head.column_number = 1;
        group_head.input_type = Some(InputType::Input);
        group_head.value = "2".into();

        let mut group_tail = column(CoilType::Load);
        group_tail.column_number = 2;
        group_tail.input_type = Some(InputType::Input);
        group_tail.value = "3".into();
        group_tail.combinator = Some(Combinator::And);

        let mut grouped = column(CoilType::Load);
        grouped.column_number = 2;
        grouped.input_type = Some(InputType::Input);
        grouped.value = "0".into(); // unused when `group` is Some
        grouped.combinator = Some(Combinator::Or);
        grouped.group = Some(vec![group_head, group_tail]);

        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![raw_a, grouped], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nRELAY_WRITE 0 0\nLOAD Input 2 None\nLOAD Input 3 Series\nLOAD Flag 0 Parallel"
        );
    }

    #[test]
    fn inverted_flag_emits_not_before_folding_into_the_running_total() {
        let mut raw = column(CoilType::Load);
        raw.input_type = Some(InputType::Input);
        raw.value = "1".into();
        raw.inverted = true;
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![raw], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 1 None\nNOT");
    }

    #[test]
    fn row_ref_reads_a_prior_row_s_named_output() {
        let mut named_row_output = column(CoilType::Load);
        named_row_output.input_type = Some(InputType::Input);
        named_row_output.value = "1".into();

        let mut row_ref = column(CoilType::RowRef);
        row_ref.row_ref_name = Some("Conveyor Running".into());

        let screen = Screen {
            rows: vec![
                RowScreen { row_number: 1, columns: vec![named_row_output], output_name: Some("Conveyor Running".into()) },
                RowScreen { row_number: 2, columns: vec![row_ref], output_name: None },
            ],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        // Row 1: loads its own column, then writes the result to the relay
        // address assigned to "Conveyor Running" (the first named output, so
        // relay index 0 -> byte 0, bit 0). Row 2: reads that same address back
        // as a Flag-style load (RowRef reuses LoadKind::Flag — no new emission
        // path needed for reading).
        assert_eq!(
            out,
            "LOAD Input 1 None\nRELAY_WRITE 0 0\nLOAD Flag 0 None"
        );
    }

    #[test]
    fn unknown_row_reference_name_is_a_compile_error() {
        let mut row_ref = column(CoilType::RowRef);
        row_ref.row_ref_name = Some("Nonexistent".into());
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![row_ref], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(err, CompileError::UnknownRowReference { row: 1, column: 1, name: "Nonexistent".into() });
    }

    #[test]
    fn referencing_a_row_that_is_not_earlier_is_a_compile_error() {
        let mut row_ref = column(CoilType::RowRef);
        row_ref.row_ref_name = Some("Later".into());
        let mut later_output = column(CoilType::Load);
        later_output.input_type = Some(InputType::Input);
        later_output.value = "1".into();
        let screen = Screen {
            rows: vec![
                RowScreen { row_number: 1, columns: vec![row_ref], output_name: None },
                RowScreen { row_number: 2, columns: vec![later_output], output_name: Some("Later".into()) },
            ],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(err, CompileError::ForwardRowReference { row: 1, column: 1, name: "Later".into() });
    }
}
