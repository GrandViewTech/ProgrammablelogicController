#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod commands;
mod state;

use std::sync::Mutex;

use ladder_routines::RoutineLibrary;
use state::AppState;

fn main() {
    let routines_dir = std::env::current_dir().unwrap().join("resources/routine");
    let routines = RoutineLibrary::load_from_dir(&routines_dir).unwrap_or_else(|e| {
        eprintln!("warning: failed to load routine library from {routines_dir:?}: {e}");
        RoutineLibrary::load_from_dir(std::path::Path::new(".")).expect("cwd always readable")
    });

    // Ruling 2: Tauri v2 removed the pre-app-instance `tauri::api::path::app_data_dir`
    // free function used by Tauri v1. The v2 replacement (`app_handle.path().app_data_dir()`,
    // via the `Manager` trait) requires an `App`/`AppHandle`, which only exists once the
    // `Builder` has been constructed (e.g. inside `.setup()`) — but this `main()` needs the
    // database open *before* `.manage(AppState { .. })` is called on the builder, matching
    // the brief's structure. Rather than restructure around a `.setup()` closure, we use the
    // `dirs` crate fallback the brief explicitly sanctions: `dirs::data_dir()` joined with an
    // app-specific subdirectory. This resolves to `~/Library/Application Support` on macOS,
    // `%APPDATA%` on Windows, and `$XDG_DATA_HOME`/`~/.local/share` on Linux — a persistent,
    // writable, app-local directory, satisfying the same intent as `app_data_dir`.
    let db_path = dirs::data_dir()
        .unwrap_or_else(std::env::temp_dir)
        .join("ladder-studio")
        .join("ladder-studio.sqlite");
    std::fs::create_dir_all(db_path.parent().unwrap()).ok();
    let db = ladder_storage::open(&db_path).expect("failed to open ladder-studio.sqlite");

    tauri::Builder::default()
        .manage(AppState { routines, db: Mutex::new(db) })
        .invoke_handler(tauri::generate_handler![
            commands::generate,
            commands::list_routines,
            commands::inject_routine,
            commands::save_screen,
            commands::load_screen,
        ])
        .run(tauri::generate_context!())
        .expect("error while running Ladder Studio");
}
