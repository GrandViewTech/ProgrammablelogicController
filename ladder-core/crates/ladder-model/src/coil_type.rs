use serde::{Deserialize, Serialize};

/// Ported from org.grandviewtech.entity.enums.CoilType.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum CoilType {
    Label,
    End,
    Jump,
    Output,
    Line,
    Load,
    Routine,
    LeftLink,
    RightLink,
    Parallel,
    Default,
    Delete,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn wire_form_matches_original_java_enum_strings() {
        assert_eq!(serde_json::to_string(&CoilType::Load).unwrap(), "\"LOAD\"");
        assert_eq!(serde_json::to_string(&CoilType::LeftLink).unwrap(), "\"LEFT_LINK\"");
        assert_eq!(serde_json::to_string(&CoilType::RightLink).unwrap(), "\"RIGHT_LINK\"");
        assert_eq!(serde_json::to_string(&CoilType::Routine).unwrap(), "\"ROUTINE\"");
    }

    #[test]
    fn round_trips_through_json() {
        let value: CoilType = serde_json::from_str("\"END\"").unwrap();
        assert_eq!(value, CoilType::End);
    }
}
