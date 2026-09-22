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
pub struct RowScreen {
    pub row_number: u32,
    pub columns: Vec<ColumnScreen>,
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
        };
        assert_eq!(row.previous(0), None);
        assert_eq!(row.previous(1).unwrap().coil_type, CoilType::Routine);
    }

    #[test]
    fn screen_round_trips_through_json() {
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![hand_built_column()] }],
            end_row_number: Some(1),
            end_column_number: Some(1),
        };
        let json = serde_json::to_string(&screen).unwrap();
        let back: Screen = serde_json::from_str(&json).unwrap();
        assert_eq!(screen, back);
    }
}
