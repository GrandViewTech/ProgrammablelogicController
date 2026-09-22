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
}
