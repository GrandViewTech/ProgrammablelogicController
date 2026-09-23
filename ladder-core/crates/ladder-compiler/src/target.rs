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

    /// Complements the current carry — the 8085 `CMC` instruction. Used when a
    /// block or group has `inverted: true` (requirements: a NOT contact is a
    /// normally-closed contact, combined via the same AND/OR rules as any other —
    /// see the design spec's §3 for why this isn't a 4th Combinator value).
    fn emit_not(&self, buf: &mut Vec<String>);

    /// Combines the current carry with a second operand's carry via XOR. 8085 has
    /// no single "XOR carry" instruction, so this emits whatever short sequence
    /// the target needs — `Target8085`'s exact sequence is worked out with
    /// golden-string TDD in Task 4, not fixed by this trait.
    fn emit_xor(&self, buf: &mut Vec<String>);

    /// Writes the current carry to a named row's internal-relay address —
    /// distinct from `emit_output`'s physical `OUTPUT0_7+` writes. See the design
    /// spec's §4 and §6 for why this reuses the `RLY512_+` address space and why
    /// it needs a hardware sanity-check before production use.
    fn emit_relay_write(&self, buf: &mut Vec<String>, byte: u32, bit: u32);
}
