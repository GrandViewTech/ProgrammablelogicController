use std::collections::BTreeMap;

use serde::{Deserialize, Serialize};

use crate::{CoilType, InputType};

/// Explicit AND/OR combinator, chosen by the worker at drop time (requirements §7.3).
/// Never inferred — the compiler reads this directly off the block.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Combinator {
    And,
    Or,
    Xor,
}

/// Worker vs Engineer editing mode (requirements §7.1). Enforcement lives on
/// ColumnScreen::is_locked, not just in UI state (requirements §4).
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum EditMode {
    Worker,
    Engineer,
}

/// Stamped onto any column(s) produced by injecting a library routine
/// (requirements §4). `None` on hand-built columns.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoutineOrigin {
    pub routine_name: String,
    pub description: String,
    /// INPUT index -> substituted value, as supplied at injection time.
    pub values: BTreeMap<u32, String>,
}

/// A single cell on the ladder grid. Ported from
/// org.grandviewtech.userinterface.screen.ColumnScreen, data fields only
/// (no Swing/paint/drag-and-drop state — that's a UI concern in `ui/`).
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ColumnScreen {
    pub row_number: u32,
    pub column_number: u32,
    pub coil_type: CoilType,
    pub input_type: Option<InputType>,
    pub value: String,
    pub tag: String,
    pub comment: String,
    /// Present only for columns produced by routine injection (requirements §4).
    pub routine_origin: Option<RoutineOrigin>,
    /// Pre-rendered ASM for a ROUTINE column, produced by
    /// `ladder-routines::inject_with_origin` at injection time (requirements §5).
    /// The compiler splices this in verbatim; it never runs template substitution itself.
    pub rendered_asm: Option<String>,
    /// Required (Some) for every block after the first on a row; ignored for
    /// the first block. Read directly by the compiler (requirements §7.3).
    pub combinator: Option<Combinator>,
    pub is_blank: bool,
    /// Independent inversion flag (design spec: NOT is orthogonal to
    /// AND/OR/XOR, not a 4th `Combinator` value) — "is this value flipped"
    /// is a separate question from "how does this combine with what came
    /// before".
    #[serde(default)]
    pub inverted: bool,
    /// A nested boolean sub-expression: when present, this column represents
    /// the AND/OR/XOR-combined group of its child columns rather than a
    /// single leaf condition.
    #[serde(default)]
    pub group: Option<Vec<ColumnScreen>>,
    /// Name of another row this column references (cross-row reference),
    /// used together with `CoilType::RowRef`.
    #[serde(default)]
    pub row_ref_name: Option<String>,
}

impl ColumnScreen {
    /// Data-layer enforcement of Worker/Engineer editing rules (requirements §4):
    /// a worker-mode client cannot be tricked into editing a routine-sourced block.
    pub fn is_locked(&self, mode: EditMode) -> bool {
        match mode {
            EditMode::Engineer => false,
            EditMode::Worker => self.routine_origin.is_some(),
        }
    }
}

/// One rung. Variable-width (requirements §4) — a `Vec`, not the original's
/// fixed-size array, so rungs grow to fit whatever blocks are placed.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RowScreen {
    pub row_number: u32,
    pub columns: Vec<ColumnScreen>,
    /// Name this row can be referenced by from other rows via
    /// `CoilType::RowRef` + `ColumnScreen.row_ref_name`. `None` if the row
    /// has not been given a name.
    #[serde(default)]
    pub output_name: Option<String>,
}

impl RowScreen {
    /// The column immediately to the left in this row, or `None` if `column_index`
    /// is the first column (ported semantics from
    /// ColumnScreenGenerator.left(), which links columnNumber-1 within the same row).
    pub fn previous(&self, column_index: usize) -> Option<&ColumnScreen> {
        column_index.checked_sub(1).and_then(|i| self.columns.get(i))
    }
}

/// The whole diagram. Ported from org.grandviewtech.entity.bo.Screen.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct Screen {
    pub rows: Vec<RowScreen>,
    pub end_row_number: Option<u32>,
    pub end_column_number: Option<u32>,
}

#[cfg(test)]
mod tests {
    use super::*;

    fn locked_column() -> ColumnScreen {
        ColumnScreen {
            row_number: 1,
            column_number: 1,
            coil_type: CoilType::Routine,
            input_type: None,
            value: String::new(),
            tag: String::new(),
            comment: String::new(),
            routine_origin: Some(RoutineOrigin {
                routine_name: "BIT RESET".into(),
                description: "Resets the bit".into(),
                values: BTreeMap::new(),
            }),
            rendered_asm: Some("MOV DPTR,#RLY512_519+".into()),
            combinator: None,
            is_blank: false,
            inverted: false,
            group: None,
            row_ref_name: None,
        }
    }

    fn hand_built_column() -> ColumnScreen {
        ColumnScreen {
            routine_origin: None,
            ..locked_column()
        }
    }

    #[test]
    fn worker_mode_locks_routine_sourced_blocks() {
        assert!(locked_column().is_locked(EditMode::Worker));
    }

    #[test]
    fn worker_mode_does_not_lock_hand_built_blocks() {
        assert!(!hand_built_column().is_locked(EditMode::Worker));
    }

    #[test]
    fn engineer_mode_never_locks_anything() {
        assert!(!locked_column().is_locked(EditMode::Engineer));
        assert!(!hand_built_column().is_locked(EditMode::Engineer));
    }

    #[test]
    fn row_previous_returns_left_neighbor_within_same_row() {
        let row = RowScreen {
            row_number: 1,
            columns: vec![hand_built_column(), locked_column()],
            output_name: None,
        };
        assert_eq!(row.previous(0), None);
        assert_eq!(row.previous(1).unwrap().coil_type, CoilType::Routine);
    }

    #[test]
    fn screen_round_trips_through_json() {
        let screen = Screen {
            rows: vec![RowScreen {
                row_number: 1,
                columns: vec![hand_built_column()],
                output_name: None,
            }],
            end_row_number: Some(1),
            end_column_number: Some(1),
        };
        let json = serde_json::to_string(&screen).unwrap();
        let back: Screen = serde_json::from_str(&json).unwrap();
        assert_eq!(screen, back);
    }

    #[test]
    fn xor_wire_form_matches_screaming_snake_case_convention() {
        assert_eq!(serde_json::to_string(&Combinator::Xor).unwrap(), "\"XOR\"");
    }

    #[test]
    fn new_column_fields_default_to_inactive() {
        let column = hand_built_column();
        assert!(!column.inverted);
        assert!(column.group.is_none());
        assert!(column.row_ref_name.is_none());
    }

    #[test]
    fn group_round_trips_through_json() {
        let mut outer = hand_built_column();
        outer.group = Some(vec![hand_built_column(), locked_column()]);
        outer.combinator = Some(Combinator::Xor);
        outer.inverted = true;
        let json = serde_json::to_string(&outer).unwrap();
        let back: ColumnScreen = serde_json::from_str(&json).unwrap();
        assert_eq!(outer, back);
    }

    #[test]
    fn row_screen_output_name_defaults_to_none_and_round_trips() {
        let row = RowScreen { row_number: 1, columns: vec![], output_name: Some("Conveyor Running".into()) };
        let json = serde_json::to_string(&row).unwrap();
        let back: RowScreen = serde_json::from_str(&json).unwrap();
        assert_eq!(row, back);
    }
}
