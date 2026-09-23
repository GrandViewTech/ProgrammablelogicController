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
}
