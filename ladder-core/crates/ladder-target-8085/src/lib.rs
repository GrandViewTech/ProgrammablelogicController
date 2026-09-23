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

impl Target for Target8085 {
    fn emit_load(&self, buf: &mut Vec<String>, kind: LoadKind, input: i32, combinator: LoadCombinator) {
        let (byte, bit) = find_param(input);
        let base = match kind {
            // The original's `CompileService.flag()` emits
            // `"MOV  DTPR , #RLY512_+" + params[0]` — the `+` is part of the
            // literal, exactly as in the INPUT and WORD helpers.
            LoadKind::Flag => "RLY512_+",
            LoadKind::Input => "INPUT0_7+",
            LoadKind::Word => "WORD0_7+",
        };
        buf.push(format!("MOV  DTPR , #{base}{byte}"));
        buf.push("MOV X A,@DPTR".to_string());
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

    // Contract: the caller has already stashed the first operand's carry in
    // ACC.7 (`MOV ACC.7 , C`) and then evaluated the second operand into
    // carry, so at entry ACC.7 holds operand A and the carry flag holds
    // operand B. This computes `carry := A XOR B` via
    // `(A OR B) AND NOT(A AND B)`, branch-free, using only the bit-carry
    // primitives `emit_load`/`emit_not` already establish in this file
    // (`MOV`/`ANL`/`ORL`/`CMC`) rather than an unprecedented
    // accumulator-wide `XRL`. See the corresponding test's doc comment for
    // why this replaces the brief's original 3-line draft (which reduced to
    // a plain carry-complement, ignoring the stashed operand entirely) and
    // task-4-report.md for the hand-traced truth table.
    fn emit_xor(&self, buf: &mut Vec<String>) {
        buf.push("MOV ACC.6 , C".to_string()); // ACC.6 = B
        buf.push("MOV C, ACC.7".to_string()); // C = A
        buf.push("ORL C, ACC.6".to_string()); // C = A OR B
        buf.push("MOV ACC.5 , C".to_string()); // ACC.5 = A OR B
        buf.push("MOV C, ACC.7".to_string()); // C = A
        buf.push("ANL C, ACC.6".to_string()); // C = A AND B
        buf.push("CMC".to_string()); // C = NOT(A AND B)
        buf.push("ANL C, ACC.5".to_string()); // C = NOT(A AND B) AND (A OR B) = A XOR B
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
    fn emit_xor_combines_two_operands_carry_results() {
        // Contract: by the time this is called, the caller has already
        // stashed the first operand's carry in ACC.7 (`MOV ACC.7 , C`) and
        // then evaluated the second operand into carry — so at entry,
        // ACC.7 holds operand A and the carry flag holds operand B.
        //
        // The brief's original 3-line draft (`MOV ACC.7 , C` / `XRL A,
        // #0x80` / `MOV C, ACC.7`) does NOT implement this: its first line
        // overwrites ACC.7 with the *current* carry (operand B per this
        // contract), clobbering the stashed operand A before it's ever
        // read. Hand-tracing it shows the whole sequence reduces to
        // `carry := NOT(carry)` — i.e. it's functionally identical to
        // `emit_not`/CMC and completely ignores the stashed operand. That's
        // a genuine correctness bug, not a style nit, so this sequence
        // replaces it (task-4-report.md documents the change).
        //
        // Replacement: `A XOR B = (A OR B) AND NOT(A AND B)`, branch-free,
        // built only from the bit-carry primitives this file already uses
        // elsewhere (`MOV`/`ANL`/`ORL`/`CMC`) rather than introducing an
        // unprecedented accumulator-wide `XRL`. ACC.7 (operand A) is only
        // ever read, never overwritten, so it's left intact for any caller
        // that still needs it afterward. ACC.6 and ACC.5 are scratch bits
        // used only within this sequence. Hand-verified against all 4
        // input combinations (see task-4-report.md).
        let mut buf = Vec::new();
        Target8085.emit_xor(&mut buf);
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
}
