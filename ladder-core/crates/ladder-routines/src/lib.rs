use std::collections::{BTreeMap, HashMap};
use std::fs;
use std::path::Path;

use ladder_model::RoutineOrigin;
use regex::Regex;
use serde::Deserialize;

#[derive(Debug, Clone, PartialEq)]
pub struct RoutineTemplate {
    pub name: String,
    pub description: String,
    /// Raw ASM, optionally containing `${INPUTn}`-style placeholders
    /// (requirements §5), as seen in resources/routine/*.xml.
    pub functional_block: String,
}

#[derive(Debug, Deserialize)]
struct RawRoutineXml {
    name: String,
    #[serde(default)]
    description: String,
    #[serde(rename = "functionalBlock")]
    functional_block: String,
}

#[derive(Debug, thiserror::Error)]
pub enum RoutineLoadError {
    #[error("failed to read routine directory {0}: {1}")]
    Io(String, std::io::Error),
    #[error("failed to parse routine file {0}: {1}")]
    Parse(String, String),
}

#[derive(Debug, thiserror::Error, PartialEq)]
pub enum InjectError {
    #[error("unknown routine: {0}")]
    UnknownRoutine(String),
    #[error("routine {routine} is missing required input(s): {missing:?}")]
    MissingInputs { routine: String, missing: Vec<u32> },
}

/// Loads once (e.g. app startup) into an in-memory map keyed by name — O(1)
/// dropdown and injection lookups (requirements §5). Replaces the original's
/// repeated XStream re-parsing, a direct cause of the old slow-screen-load
/// complaint (requirements §6, item 1).
pub struct RoutineLibrary {
    templates: HashMap<String, RoutineTemplate>,
}

fn placeholder_regex() -> Regex {
    Regex::new(r"\$\{INPUT(\d+)\}").expect("static regex is valid")
}

impl RoutineLibrary {
    pub fn load_from_dir(dir: &Path) -> Result<Self, RoutineLoadError> {
        let mut templates = HashMap::new();
        let entries = fs::read_dir(dir).map_err(|e| RoutineLoadError::Io(dir.display().to_string(), e))?;
        for entry in entries {
            let entry = entry.map_err(|e| RoutineLoadError::Io(dir.display().to_string(), e))?;
            let path = entry.path();
            if path.extension().and_then(|e| e.to_str()) != Some("xml") {
                continue;
            }
            let contents = fs::read_to_string(&path).map_err(|e| RoutineLoadError::Io(path.display().to_string(), e))?;
            let raw: RawRoutineXml = quick_xml::de::from_str(&contents)
                .map_err(|e| RoutineLoadError::Parse(path.display().to_string(), e.to_string()))?;
            templates.insert(
                raw.name.clone(),
                RoutineTemplate {
                    name: raw.name,
                    description: raw.description,
                    functional_block: raw.functional_block,
                },
            );
        }
        Ok(Self { templates })
    }

    pub fn get(&self, name: &str) -> Option<&RoutineTemplate> {
        self.templates.get(name)
    }

    pub fn names(&self) -> Vec<&str> {
        self.templates.keys().map(String::as_str).collect()
    }

    /// Single-pass `${KEY}` scan (requirements §5) — not a general templating
    /// engine. A missing required input is caught as an error before
    /// injection, so the UI can flag exactly which field is empty rather than
    /// failing at render/compile time.
    pub fn inject_with_origin(
        &self,
        name: &str,
        values: &BTreeMap<u32, String>,
    ) -> Result<(String, RoutineOrigin), InjectError> {
        let template = self.get(name).ok_or_else(|| InjectError::UnknownRoutine(name.to_string()))?;
        let re = placeholder_regex();

        let mut missing = Vec::new();
        for capture in re.captures_iter(&template.functional_block) {
            let key: u32 = capture[1].parse().expect("regex guarantees digits");
            if !values.contains_key(&key) {
                missing.push(key);
            }
        }
        if !missing.is_empty() {
            missing.sort_unstable();
            missing.dedup();
            return Err(InjectError::MissingInputs { routine: name.to_string(), missing });
        }

        let rendered = re
            .replace_all(&template.functional_block, |caps: &regex::Captures| {
                let key: u32 = caps[1].parse().expect("regex guarantees digits");
                values.get(&key).cloned().unwrap_or_default()
            })
            .into_owned();

        let origin = RoutineOrigin {
            routine_name: template.name.clone(),
            description: template.description.clone(),
            values: values.clone(),
        };

        Ok((rendered, origin))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::path::PathBuf;

    fn fixtures_dir() -> PathBuf {
        PathBuf::from(env!("CARGO_MANIFEST_DIR")).join("tests/fixtures")
    }

    #[test]
    fn loads_all_xml_files_in_directory_keyed_by_name() {
        let library = RoutineLibrary::load_from_dir(&fixtures_dir()).unwrap();
        assert_eq!(library.names(), vec!["BIT RESET"]);
    }

    #[test]
    fn injects_and_substitutes_all_placeholders() {
        let library = RoutineLibrary::load_from_dir(&fixtures_dir()).unwrap();
        let mut values = BTreeMap::new();
        values.insert(1, "519".to_string());
        values.insert(2, "567".to_string());
        let (rendered, origin) = library.inject_with_origin("BIT RESET", &values).unwrap();
        assert!(rendered.contains("MOV DPTR,#RLY512_519+"));
        assert!(rendered.contains("CLR RLY567"));
        assert_eq!(origin.routine_name, "BIT RESET");
        assert_eq!(origin.values, values);
    }

    #[test]
    fn missing_required_input_is_an_error_not_a_bad_render() {
        let library = RoutineLibrary::load_from_dir(&fixtures_dir()).unwrap();
        let mut values = BTreeMap::new();
        values.insert(1, "519".to_string());
        // INPUT2 deliberately omitted.
        let err = library.inject_with_origin("BIT RESET", &values).unwrap_err();
        assert_eq!(err, InjectError::MissingInputs { routine: "BIT RESET".into(), missing: vec![2] });
    }

    #[test]
    fn unknown_routine_name_is_an_error() {
        let library = RoutineLibrary::load_from_dir(&fixtures_dir()).unwrap();
        let err = library.inject_with_origin("NOT A ROUTINE", &BTreeMap::new()).unwrap_err();
        assert_eq!(err, InjectError::UnknownRoutine("NOT A ROUTINE".into()));
    }
}
