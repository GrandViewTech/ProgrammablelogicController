use crate::types::{LoadCombinator, LoadKind, OutputType};

/// Pluggable mnemonic emission (requirements §3). `Target8085` (in the
/// ladder-target-8085 crate) implements this to reproduce the original 8085
/// mnemonics string-for-string. Future targets (AVR, PIC, a simulator)
/// implement the same trait without touching `generate()`'s traversal logic.
pub trait Target {
    fn emit_load(&self, buf: &mut Vec<String>, kind: LoadKind, input: i32, combinator: LoadCombinator);
    fn emit_output(&self, buf: &mut Vec<String>, value: i32, output_type: OutputType, label: &str);
    fn emit_jnc(&self, buf: &mut Vec<String>, label: &str);
    fn emit_label(&self, buf: &mut Vec<String>, label: &str);
}
