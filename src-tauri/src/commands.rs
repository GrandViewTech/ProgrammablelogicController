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
        .map(|t| RoutineSummary { name: t.name.clone(), description: t.description.clone() })
        .collect())
}

#[tauri::command]
pub async fn inject_routine(
    state: State<'_, AppState>,
    routine_name: String,
    values: BTreeMap<u32, String>,
) -> Result<InjectedBlock, String> {
    let (rendered_asm, origin) = state
        .routines
        .inject_with_origin(&routine_name, &values)
        .map_err(|e| e.to_string())?;
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
