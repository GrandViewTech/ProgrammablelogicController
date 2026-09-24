use serde::{Deserialize, Serialize};

/// Ported from org.grandviewtech.entity.enums.InputType.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum InputType {
    Input,
    Flag,
    Word,
    Output,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn wire_form_matches_original_java_enum_strings() {
        assert_eq!(serde_json::to_string(&InputType::Flag).unwrap(), "\"FLAG\"");
        assert_eq!(serde_json::to_string(&InputType::Word).unwrap(), "\"WORD\"");
    }
}
