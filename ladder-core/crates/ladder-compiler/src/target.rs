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

    /// XORs the current carry with a specific accumulator bit, leaving the
    /// result in carry — the XOR counterpart to the AND/OR combining every
    /// leaf load already does via `ANL C, ACC.{bit}` / `ORL C, ACC.{bit}`.
    /// Unlike `emit_load`'s combinator parameter, 8085 has no single
    /// instruction for this, so it's a short synthesized sequence (see
    /// `Target8085`'s implementation). Callers combine two operands by
    /// stashing one to a relay address (`emit_relay_write`), evaluating the
    /// other fresh into carry, reading the first one back into the
    /// accumulator (`emit_relay_read`), and then calling this with the bit
    /// that read landed on (task-4b-report.md documents why an ACC-bit-only
    /// stash, as Task 4's original narrower contract assumed, doesn't
    /// survive a caller whose fresh evaluation does its own `MOVX` — the
    /// same lesson Task 3 learned for non-leading groups).
    fn emit_xor(&self, buf: &mut Vec<String>, bit: u32);

    /// Writes the current carry to a named row's internal-relay address —
    /// distinct from `emit_output`'s physical `OUTPUT0_7+` writes. See the design
    /// spec's §4 and §6 for why this reuses the `RLY512_+` address space and why
    /// it needs a hardware sanity-check before production use.
    fn emit_relay_write(&self, buf: &mut Vec<String>, byte: u32, bit: u32);

    /// Reads a relay address into the accumulator without touching carry and
    /// without combining anything into it — the "read back" half of the
    /// stash/evaluate-fresh/read-back/`emit_xor` pattern a non-leading
    /// XOR-combined column uses (see `traversal.rs`'s XOR dispatch).
    /// Equivalent to the `MOV DPTR,#RLY512_+{byte}` / `MOVX A,@DPTR` lines
    /// already inside `emit_load`'s `LoadKind::Flag` arm and
    /// `emit_relay_write`, factored out so a caller can land a stashed value
    /// in the accumulator without also triggering `emit_load`'s trailing
    /// series/parallel combine instruction, which would clobber the carry a
    /// freshly-evaluated operand was just placed into.
    fn emit_relay_read(&self, buf: &mut Vec<String>, byte: u32);
}
