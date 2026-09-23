use std::collections::BTreeMap;

use ladder_model::{RoutineOrigin, Screen};
use serde::Serialize;
use tauri::State;

use crate::state::AppState;

// Ruling 1: Tauri only auto-converts case for command *arguments*, not for
// return-value serialization. Without this attribute, `InjectedBlock` would
// serialize its `rendered_asm` field as-is (snake_case) instead of the
// camelCase the React UI (Tasks 11/13) expects (`renderedAsm`).
#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RoutineSummary {
    pub name: String,
    pub description: String,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct InjectedBlock {
    pub rendered_asm: String,
    pub origin: RoutineOrigin,
}

/// 17 of the 21 shipped `resources/routine/*.xml` files have an empty
/// `<description>` element. The UI renders the description as a block's and a
/// palette entry's primary label, so passing those through verbatim yields
/// blank text. Falling back to the routine's name here keeps every render site
/// (BlockNode, Palette, PreviewPanel) fed with non-empty text from one place,
/// rather than three copies of the same guard in TSX.
fn display_description(name: &str, description: &str) -> String {
    if description.trim().is_empty() {
        name.to_string()
    } else {
        description.to_string()
    }
}

/// Runs off the UI thread; drives both "Test output" and the always-visible
/// code preview panel from the same call (requirements §6, item 2).
#[tauri::command]
pub async fn generate(screen: Screen) -> Result<String, String> {
    ladder_target_8085::generate_8085(&screen).map_err(|e| e.to_string())
}

#[tauri::command]
pub async fn list_routines(state: State<'_, AppState>) -> Result<Vec<RoutineSummary>, String> {
    Ok(state
        .routines
        .names()
        .into_iter()
        .filter_map(|name| state.routines.get(name))
        .map(|t| RoutineSummary {
            name: t.name.clone(),
            description: display_description(&t.name, &t.description),
        })
        .collect())
}

#[tauri::command]
pub async fn inject_routine(
    state: State<'_, AppState>,
    routine_name: String,
    values: BTreeMap<u32, String>,
) -> Result<InjectedBlock, String> {
    let (rendered_asm, mut origin) = state
        .routines
        .inject_with_origin(&routine_name, &values)
        .map_err(|e| e.to_string())?;
    origin.description = display_description(&origin.routine_name, &origin.description);
    Ok(InjectedBlock { rendered_asm, origin })
}

#[tauri::command]
pub async fn save_screen(state: State<'_, AppState>, name: String, screen: Screen) -> Result<(), String> {
    let conn = state.db.lock().map_err(|_| "database lock poisoned".to_string())?;
    ladder_storage::save_screen(&conn, &name, &screen).map_err(|e| e.to_string())
}

#[tauri::command]
pub async fn load_screen(state: State<'_, AppState>, name: String) -> Result<Screen, String> {
    let conn = state.db.lock().map_err(|_| "database lock poisoned".to_string())?;
    ladder_storage::load_screen(&conn, &name).map_err(|e| e.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn blank_description_falls_back_to_the_routine_name() {
        assert_eq!(display_description("BIT RESET", ""), "BIT RESET");
        assert_eq!(display_description("BIT RESET", "   \n "), "BIT RESET");
        assert_eq!(display_description("BIT RESET", "Resets the bit"), "Resets the bit");
    }

    /// Wire-format guard for the Rust -> TypeScript boundary: `InjectedBlock`
    /// must serialize with the exact camelCase keys `ui/src/api/ladderStudio.ts`
    /// declares, including the nested `RoutineOrigin`.
    #[test]
    fn injected_block_serializes_with_the_camel_case_keys_the_ui_declares() {
        let block = InjectedBlock {
            rendered_asm: "MOV DPTR,#RLY512_519+".into(),
            origin: RoutineOrigin {
                routine_name: "BIT RESET".into(),
                description: "Resets the bit".into(),
                values: BTreeMap::from([(1, "519".to_string())]),
            },
        };
        let json: serde_json::Value = serde_json::to_value(&block).unwrap();
        assert_eq!(json["renderedAsm"], "MOV DPTR,#RLY512_519+");
        assert_eq!(json["origin"]["routineName"], "BIT RESET");
        assert!(json.get("rendered_asm").is_none(), "snake_case key leaked to the UI");
        assert!(json["origin"].get("routine_name").is_none(), "snake_case key leaked to the UI");
    }

    /// The other direction: a literal payload shaped exactly like what the
    /// frontend sends for `save_screen`/`generate` must deserialize into a
    /// `Screen` without any field renaming drift.
    #[test]
    fn frontend_shaped_screen_json_deserializes_and_round_trips() {
        let from_ui = r#"{
            "rows": [{
                "rowNumber": 1,
                "columns": [{
                    "rowNumber": 1, "columnNumber": 2, "coilType": "ROUTINE",
                    "inputType": null, "value": "", "tag": "", "comment": "",
                    "routineOrigin": {
                        "routineName": "BIT RESET", "description": "Resets the bit",
                        "values": {"1": "519"}
                    },
                    "renderedAsm": "MOV DPTR,#RLY512_519+",
                    "combinator": "AND", "isBlank": false
                }]
            }],
            "endRowNumber": null, "endColumnNumber": null
        }"#;
        let screen: Screen = serde_json::from_str(from_ui).unwrap();
        let column = &screen.rows[0].columns[0];
        assert_eq!(column.rendered_asm.as_deref(), Some("MOV DPTR,#RLY512_519+"));
        assert_eq!(column.routine_origin.as_ref().unwrap().routine_name, "BIT RESET");
        assert_eq!(column.combinator, Some(ladder_model::Combinator::And));

        let back: Screen = serde_json::from_value(serde_json::to_value(&screen).unwrap()).unwrap();
        assert_eq!(screen, back);
    }
}
