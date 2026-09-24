use serde::{Deserialize, Serialize};

/// Ported from org.grandviewtech.entity.enums.NoNc.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum NoNc {
    No,
    Nc,
    Default,
    Set,
    Reset,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn wire_form_matches_original_java_enum_strings() {
        assert_eq!(serde_json::to_string(&NoNc::Set).unwrap(), "\"SET\"");
        assert_eq!(serde_json::to_string(&NoNc::Nc).unwrap(), "\"NC\"");
    }
}
