use ladder_compiler::compat::find_param;
use ladder_compiler::{CompileError, LoadCombinator, LoadKind, OutputType, Target};
use ladder_model::Screen;

/// Reproduces org.grandviewtech.service.execution.CompileService's original
/// 8085 mnemonics string-for-string (requirements §3).
pub struct Target8085;

fn series_parallel_suffix(combinator: LoadCombinator, bit: u32) -> String {
    match combinator {
        LoadCombinator::None => format!("MOV C, ACC.{bit}"),
        LoadCombinator::Parallel => format!("ORL C, ACC.{bit}"),
        LoadCombinator::Series => format!("ANL C, ACC.{bit}"),
    }
}

/// Two ACC bit positions to use as `emit_xor`'s own internal scratch —
/// guaranteed distinct from `bit` (which holds the live operand `emit_xor`
/// still needs to re-read) and from each other, so the synthesized sequence
/// never clobbers the operand while it's still needed. `bit` is always 0-7
/// (it comes from `find_param`'s `% 8` split, and in practice from
/// `traversal.rs`'s `next_scratch_index` counter, which cycles through every
/// bit position as it grows past 8 — so this can't assume `bit` stays out of
/// any particular range). Excluding one value from a 3-candidate pool always
/// leaves at least 2 usable bits.
fn xor_scratch_bits(bit: u32) -> (u32, u32) {
    let mut candidates = [7u32, 6, 5].into_iter().filter(|&candidate| candidate != bit);
    let b_bit = candidates.next().expect("bit excludes at most one of three candidates");
    let or_bit = candidates.next().expect("bit excludes at most one of three candidates");
    (b_bit, or_bit)
}

impl Target for Target8085 {
    fn emit_load(&self, buf: &mut Vec<String>, kind: LoadKind, input: i32, combinator: LoadCombinator) {
        let (byte, bit) = find_param(input);
        match kind {
            // The original's `CompileService.flag()` emits
            // `"MOV  DTPR , #RLY512_+" + params[0]` — the `+` is part of the
            // literal, exactly as in the INPUT and WORD helpers.
            LoadKind::Flag => self.emit_relay_read(buf, byte),
            LoadKind::Input => {
                buf.push(format!("MOV  DTPR , #INPUT0_7+{byte}"));
                buf.push("MOV X A,@DPTR".to_string());
            }
            LoadKind::Word => {
                buf.push(format!("MOV  DTPR , #WORD0_7+{byte}"));
                buf.push("MOV X A,@DPTR".to_string());
            }
        }
        if kind == LoadKind::Input {
            // Original's `input()` helper reads the carry bit directly from ACC
            // before applying series/parallel, distinct from flag/word.
            buf.push(format!("MOV C, ACC.{bit}"));
        }
        buf.push(series_parallel_suffix(combinator, bit));
    }

    fn emit_output(&self, buf: &mut Vec<String>, value: i32, output_type: OutputType, label: &str) {
        let (byte, bit) = find_param(value);
        match output_type {
            OutputType::Reset => {
                buf.push(format!("JNC {label}"));
                buf.push(format!("MOV  DTPR , #OUTPUT0_7+{byte}"));
                buf.push("MOV X A,@DPTR".to_string());
                buf.push(format!("MOV ACC.{bit} , C"));
                buf.push("MOVX @DPTR,A".to_string());
                buf.push(format!("{label} :"));
            }
            OutputType::Set => {
                buf.push(format!("JNC {label}"));
                buf.push(format!("MOV  DTPR , #OUTPUT0_7+{byte}"));
                buf.push("MOV X A,@DPTR".to_string());
                buf.push(format!("MOV ACC.{bit} , C"));
                buf.push("MOVX @DPTR,A".to_string());
                buf.push("SETB ".to_string());
                buf.push(format!("{label} :"));
            }
            OutputType::None => {
                buf.push(format!("MOV  DTPR , #OUTPUT0_7+{byte}"));
                buf.push("MOV X A,@DPTR".to_string());
                buf.push(format!("MOV ACC.{bit} , C"));
                buf.push("MOVX @DPTR,A".to_string());
            }
        }
    }

    fn emit_jnc(&self, buf: &mut Vec<String>, label: &str) {
        buf.push(format!("JNC {label}"));
    }

    fn emit_label(&self, buf: &mut Vec<String>, label: &str) {
        buf.push(format!("{label} :"));
    }

    fn emit_not(&self, buf: &mut Vec<String>) {
        buf.push("CMC".to_string());
    }

    fn emit_relay_write(&self, buf: &mut Vec<String>, byte: u32, bit: u32) {
        buf.push(format!("MOV  DTPR , #RLY512_+{byte}"));
        buf.push("MOV X A,@DPTR".to_string());
        buf.push(format!("MOV ACC.{bit} , C"));
        buf.push("MOVX @DPTR,A".to_string());
    }

    fn emit_relay_read(&self, buf: &mut Vec<String>, byte: u32) {
        buf.push(format!("MOV  DTPR , #RLY512_+{byte}"));
        buf.push("MOV X A,@DPTR".to_string());
    }

    // Contract (Task 4b — see task-4b-report.md): at entry, carry holds one
    // operand (`B`) and `ACC.{bit}` holds the other (`A`), already stashed
    // there by the caller's own `emit_relay_write` + read-back sequence
    // (see `traversal.rs`'s XOR dispatch) rather than by an ACC-bit-only
    // stash — Task 4's original contract assumed the latter, but that bit
    // gets clobbered by any intervening `MOVX` (e.g. evaluating a fresh
    // operand), the same lesson Task 3 learned for non-leading groups. This
    // computes `carry := A XOR B` via `(A OR B) AND NOT(A AND B)`,
    // branch-free, using only the bit-carry primitives already established
    // in this file (`MOV`/`ANL`/`ORL`/`CMC`) rather than an unprecedented
    // accumulator-wide `XRL`. `ACC.{bit}` (operand A) is only ever read
    // here, never overwritten. The two other ACC bits used as scratch for
    // "B" and "A OR B" are chosen dynamically (`xor_scratch_bits`) to avoid
    // `bit` itself — `bit` is caller-supplied and can be any of 0-7, so a
    // fixed pair of scratch bits (as Task 4's narrower, single-call-site
    // test used) would self-collide whenever `bit` happened to match one of
    // them. See the corresponding tests' doc comments for the hand-traced
    // truth table across multiple `bit` values, including the two that
    // would collide with the original fixed choice.
    fn emit_xor(&self, buf: &mut Vec<String>, bit: u32) {
        let (b_bit, or_bit) = xor_scratch_bits(bit);
        buf.push(format!("MOV ACC.{b_bit} , C")); // {b_bit} = B
        buf.push(format!("MOV C, ACC.{bit}")); // C = A
        buf.push(format!("ORL C, ACC.{b_bit}")); // C = A OR B
        buf.push(format!("MOV ACC.{or_bit} , C")); // {or_bit} = A OR B
        buf.push(format!("MOV C, ACC.{bit}")); // C = A
        buf.push(format!("ANL C, ACC.{b_bit}")); // C = A AND B
        buf.push("CMC".to_string()); // C = NOT(A AND B)
        buf.push(format!("ANL C, ACC.{or_bit}")); // C = NOT(A AND B) AND (A OR B) = A XOR B
    }
}

pub fn generate_8085(screen: &Screen) -> Result<String, CompileError> {
    ladder_compiler::generate(screen, &Target8085)
}

#[cfg(test)]
mod tests {
    use super::*;
    use ladder_compiler::{LoadCombinator, LoadKind, OutputType, Target};

    #[test]
    fn emit_load_input_matches_original_input_helper_for_first_block() {
        let mut buf = Vec::new();
        Target8085.emit_load(&mut buf, LoadKind::Input, 19, LoadCombinator::None);
        assert_eq!(
            buf,
            vec![
                "MOV  DTPR , #INPUT0_7+2",
                "MOV X A,@DPTR",
                "MOV C, ACC.3",
                "MOV C, ACC.3",
            ]
        );
    }

    #[test]
    fn emit_load_flag_keeps_the_literal_plus_from_the_original_flag_helper() {
        // CompileService.flag() emits `"MOV  DTPR , #RLY512_+" + params[0]`;
        // dropping the `+` produced `#RLY512_2` instead of `#RLY512_+2`.
        let mut buf = Vec::new();
        Target8085.emit_load(&mut buf, LoadKind::Flag, 19, LoadCombinator::None);
        assert_eq!(
            buf,
            vec!["MOV  DTPR , #RLY512_+2", "MOV X A,@DPTR", "MOV C, ACC.3"]
        );
    }

    #[test]
    fn emit_load_flag_with_parallel_combinator_uses_orl() {
        let mut buf = Vec::new();
        Target8085.emit_load(&mut buf, LoadKind::Flag, 8, LoadCombinator::Parallel);
        assert_eq!(
            buf,
            vec!["MOV  DTPR , #RLY512_+1", "MOV X A,@DPTR", "ORL C, ACC.0"]
        );
    }

    #[test]
    fn emit_load_word_with_series_combinator_uses_anl() {
        let mut buf = Vec::new();
        Target8085.emit_load(&mut buf, LoadKind::Word, 8, LoadCombinator::Series);
        assert_eq!(
            buf,
            vec!["MOV  DTPR , #WORD0_7+1", "MOV X A,@DPTR", "ANL C, ACC.0"]
        );
    }

    #[test]
    fn emit_output_reset_matches_original_jnc_wrap() {
        let mut buf = Vec::new();
        Target8085.emit_output(&mut buf, 3, OutputType::Reset, "LABEL_1_1");
        assert_eq!(
            buf,
            vec![
                "JNC LABEL_1_1",
                "MOV  DTPR , #OUTPUT0_7+0",
                "MOV X A,@DPTR",
                "MOV ACC.3 , C",
                "MOVX @DPTR,A",
                "LABEL_1_1 :",
            ]
        );
    }

    #[test]
    fn emit_output_set_adds_the_original_setb_line_inside_the_jnc_wrap() {
        let mut buf = Vec::new();
        Target8085.emit_output(&mut buf, 3, OutputType::Set, "LABEL_1_1");
        assert_eq!(
            buf,
            vec![
                "JNC LABEL_1_1",
                "MOV  DTPR , #OUTPUT0_7+0",
                "MOV X A,@DPTR",
                "MOV ACC.3 , C",
                "MOVX @DPTR,A",
                "SETB ",
                "LABEL_1_1 :",
            ]
        );
    }

    #[test]
    fn emit_output_none_emits_the_bare_store_without_jnc_or_label() {
        // The traversal's `find_output_type()` always resolves to `None`
        // (see the documented NoNc gap), so this is the arm real screens hit.
        let mut buf = Vec::new();
        Target8085.emit_output(&mut buf, 19, OutputType::None, "LABEL_2_4");
        assert_eq!(
            buf,
            vec![
                "MOV  DTPR , #OUTPUT0_7+2",
                "MOV X A,@DPTR",
                "MOV ACC.3 , C",
                "MOVX @DPTR,A",
            ]
        );
    }

    #[test]
    fn emit_jnc_and_emit_label_match_original_literal_strings() {
        let mut buf = Vec::new();
        Target8085.emit_jnc(&mut buf, "LABEL_2_1");
        Target8085.emit_label(&mut buf, "LABEL_2_1");
        assert_eq!(buf, vec!["JNC LABEL_2_1", "LABEL_2_1 :"]);
    }

    #[test]
    fn emit_not_appends_complement_carry() {
        let mut buf = Vec::new();
        Target8085.emit_not(&mut buf);
        assert_eq!(buf, vec!["CMC"]);
    }

    #[test]
    fn emit_relay_write_mirrors_the_output_none_case_pattern_against_the_relay_base() {
        let mut buf = Vec::new();
        Target8085.emit_relay_write(&mut buf, 2, 3);
        assert_eq!(
            buf,
            vec![
                "MOV  DTPR , #RLY512_+2",
                "MOV X A,@DPTR",
                "MOV ACC.3 , C",
                "MOVX @DPTR,A",
            ]
        );
    }

    #[test]
    fn emit_relay_read_loads_a_relay_address_into_acc_without_touching_carry() {
        // The "read back" half of the stash/evaluate-fresh/read-back/
        // emit_xor pattern (traversal.rs's XOR dispatch): only the DPTR set
        // + MOVX lines, deliberately stopping short of emit_load's trailing
        // series/parallel combine instruction, which would clobber whatever
        // a freshly-evaluated operand just placed in carry.
        let mut buf = Vec::new();
        Target8085.emit_relay_read(&mut buf, 2);
        assert_eq!(buf, vec!["MOV  DTPR , #RLY512_+2", "MOV X A,@DPTR"]);
    }

    #[test]
    fn emit_xor_combines_current_carry_with_a_given_acc_bit_bit_3() {
        // Task 4b (see task-4b-report.md): Task 4's `emit_xor()` assumed a
        // fixed ACC.7-based two-operand stash set up entirely by the
        // caller before the call. That contract doesn't survive real use:
        // the durable stash has to be memory (a relay address), not an
        // accumulator bit, because evaluating a fresh operand does its own
        // MOVX, which clobbers the whole accumulator. So `emit_xor` now
        // takes `bit: u32` — the caller reads the stashed operand back into
        // ACC.{bit} (via `emit_relay_read`) and passes that bit here; the
        // *other* operand is still carry, exactly as before.
        //
        // Contract at entry: carry = B (the freshly-evaluated operand),
        // ACC.{bit} = A (the old running total, just read back from
        // memory). Computes `carry := A XOR B` via
        // `(A OR B) AND NOT(A AND B)`, branch-free, reusing this file's
        // existing bit-carry primitives (`MOV`/`ANL`/`ORL`/`CMC`) instead of
        // an unprecedented accumulator-wide `XRL` — same derivation Task 4
        // verified, just retargeted from the fixed ACC.7 to an arbitrary
        // `bit`. For `bit = 3`, `xor_scratch_bits(3)` picks (7, 6) (neither
        // collides with 3), giving:
        //
        //   MOV ACC.7 , C   // ACC.7 = B
        //   MOV C, ACC.3    // C = A
        //   ORL C, ACC.7    // C = A OR B
        //   MOV ACC.6 , C   // ACC.6 = A OR B
        //   MOV C, ACC.3    // C = A
        //   ANL C, ACC.7    // C = A AND B
        //   CMC             // C = NOT(A AND B)
        //   ANL C, ACC.6    // C = NOT(A AND B) AND (A OR B) = A XOR B
        //
        // Hand-traced against all 4 input combinations (A = ACC.3, B =
        // carry at entry):
        //   A=0,B=0: C=A OR B=0 -> ACC.6=0. C=A AND B=0 -> CMC -> C=1.
        //            ANL C,ACC.6 -> C = 1 AND 0 = 0.  0 XOR 0 = 0. OK.
        //   A=0,B=1: C=A OR B=1 -> ACC.6=1. C=A AND B=0 -> CMC -> C=1.
        //            ANL C,ACC.6 -> C = 1 AND 1 = 1.  0 XOR 1 = 1. OK.
        //   A=1,B=0: C=A OR B=1 -> ACC.6=1. C=A AND B=0 -> CMC -> C=1.
        //            ANL C,ACC.6 -> C = 1 AND 1 = 1.  1 XOR 0 = 1. OK.
        //   A=1,B=1: C=A OR B=1 -> ACC.6=1. C=A AND B=1 -> CMC -> C=0.
        //            ANL C,ACC.6 -> C = 0 AND 1 = 0.  1 XOR 1 = 0. OK.
        // All 4 combinations match A XOR B.
        let mut buf = Vec::new();
        Target8085.emit_xor(&mut buf, 3);
        assert_eq!(
            buf,
            vec![
                "MOV ACC.7 , C",
                "MOV C, ACC.3",
                "ORL C, ACC.7",
                "MOV ACC.6 , C",
                "MOV C, ACC.3",
                "ANL C, ACC.7",
                "CMC",
                "ANL C, ACC.6",
            ]
        );
    }

    #[test]
    fn emit_xor_reproduces_task_4s_original_sequence_when_bit_is_7() {
        // `bit = 7` is the one Task 4's original (narrower) contract always
        // used, so `xor_scratch_bits(7)` must avoid re-choosing 7 itself —
        // it picks (6, 5), reproducing Task 4's exact original sequence.
        // This is also a regression check: bit 7 must not appear as one of
        // emit_xor's own scratch writes when `bit` itself is 7.
        let mut buf = Vec::new();
        Target8085.emit_xor(&mut buf, 7);
        assert_eq!(
            buf,
            vec![
                "MOV ACC.6 , C",
                "MOV C, ACC.7",
                "ORL C, ACC.6",
                "MOV ACC.5 , C",
                "MOV C, ACC.7",
                "ANL C, ACC.6",
                "CMC",
                "ANL C, ACC.5",
            ]
        );
    }

    #[test]
    fn emit_xor_avoids_self_collision_when_bit_is_one_of_its_own_default_scratch_bits() {
        // Bit-collision regression check: `scratch_bit` (the address a
        // non-leading column's running total is stashed at) is allocated
        // from `next_scratch_index % 8` in traversal.rs, so it cycles
        // through every value 0-7 as a screen grows, including 5 and 6 —
        // the exact bits a naive fixed-scratch implementation (Task 4's
        // original ACC.6/ACC.5 choice) would use internally. If `bit == 6`
        // and emit_xor still wrote to ACC.6 as its own "B" scratch, the
        // very first instruction would overwrite operand A (sitting in
        // ACC.6) with B, before it's ever read — corrupting the result.
        // `xor_scratch_bits(6)` must therefore pick scratch bits other than
        // 6: it picks (7, 5).
        let mut buf = Vec::new();
        Target8085.emit_xor(&mut buf, 6);
        assert_eq!(
            buf,
            vec![
                "MOV ACC.7 , C",
                "MOV C, ACC.6",
                "ORL C, ACC.7",
                "MOV ACC.5 , C",
                "MOV C, ACC.6",
                "ANL C, ACC.7",
                "CMC",
                "ANL C, ACC.5",
            ]
        );
    }
}
