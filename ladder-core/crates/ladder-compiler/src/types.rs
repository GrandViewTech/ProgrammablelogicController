/// Which LOAD sub-case is being emitted — mirrors the FLAG/INPUT/WORD arms of
/// CompileService's switch on ColumnScreen.getInputType() under CoilType.LOAD.
/// (The OUTPUT arm under LOAD reuses `Target::emit_output`, not this enum —
/// see CompileService.java's `case OUTPUT` under the LOAD switch.)
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum LoadKind {
    Flag,
    Input,
    Word,
}

/// Resolved combinator for a LOAD emission. `None` for the first block on a
/// row. For later blocks this is derived from the block's explicit
/// `Combinator` (requirements §7.3), not inferred.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum LoadCombinator {
    None,
    Series,
    Parallel,
}

/// Ported from CompileService.OUTPUT_TYPE.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum OutputType {
    Set,
    Reset,
    None,
}

#[derive(Debug, Clone, PartialEq, Eq, thiserror::Error)]
pub enum CompileError {
    #[error("row {row}, column {column}: value {value:?} is not a valid integer")]
    InvalidValue { row: u32, column: u32, value: String },
    #[error("row {row}, column {column}: ROUTINE block has no rendered_asm (not injected via ladder-routines)")]
    MissingRenderedAsm { row: u32, column: u32 },
    #[error("row {row}, column {column}: every block after the first on a rung must carry an explicit AND/OR combinator")]
    MissingCombinator { row: u32, column: u32 },
    #[error("row {row}, column {column}: reference to unknown row output {name:?}")]
    UnknownRowReference { row: u32, column: u32, name: String },
    #[error("row {row}, column {column}: reference to {name:?} points at a row that isn't earlier in the screen")]
    ForwardRowReference { row: u32, column: u32, name: String },
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn compile_error_messages_are_actionable() {
        let err = CompileError::InvalidValue { row: 2, column: 3, value: "abc".into() };
        assert_eq!(
            err.to_string(),
            "row 2, column 3: value \"abc\" is not a valid integer"
        );
    }

    #[test]
    fn unknown_row_reference_message_names_the_row_and_reference() {
        let err = CompileError::UnknownRowReference { row: 3, column: 1, name: "Conveyor Running".into() };
        assert_eq!(
            err.to_string(),
            "row 3, column 1: reference to unknown row output \"Conveyor Running\""
        );
    }

    #[test]
    fn forward_row_reference_message_names_the_row_and_reference() {
        let err = CompileError::ForwardRowReference { row: 1, column: 1, name: "Conveyor Running".into() };
        assert_eq!(
            err.to_string(),
            "row 1, column 1: reference to \"Conveyor Running\" points at a row that isn't earlier in the screen"
        );
    }
}
