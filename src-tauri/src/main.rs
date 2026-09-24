#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod commands;
mod state;

use std::path::PathBuf;
use std::sync::Mutex;

use ladder_routines::RoutineLibrary;
use state::AppState;
use tauri::path::BaseDirectory;
use tauri::{AppHandle, Manager};

/// Candidate routine directories, most-authoritative first.
///
/// 1. The bundled resource directory. `tauri.conf.json` maps
///    `../resources/routine/*` to `resources/routine` inside the bundle, so a
///    packaged app (and `tauri dev`, which stages resources the same way)
///    finds its routines here regardless of the process working directory.
/// 2. `<cwd>/resources/routine` — a bare `cargo run` from the repo root.
/// 3. `<cwd>/../resources/routine` — a bare `cargo run` from `src-tauri/`,
///    where the working directory is the crate directory, not the repo root.
///
/// The cwd entries are dev-only conveniences; a packaged app always hits (1).
fn candidate_routine_dirs(app: &AppHandle) -> Vec<PathBuf> {
    let mut candidates = Vec::new();
    if let Ok(resource_dir) = app.path().resolve("resources/routine", BaseDirectory::Resource) {
        candidates.push(resource_dir);
    }
    if let Ok(cwd) = std::env::current_dir() {
        candidates.push(cwd.join("resources/routine"));
        candidates.push(cwd.join("../resources/routine"));
    }
    candidates
}

/// Never fatal: a missing or unparseable routine directory degrades the app to
/// "zero routines in the palette", it does not stop it launching. A packaged
/// build with a broken resource bundle, or a parse error in a single shipped
/// XML file, must still open a window the user can save and load screens in.
fn load_routine_library(app: &AppHandle) -> RoutineLibrary {
    for dir in candidate_routine_dirs(app) {
        if !dir.is_dir() {
            continue;
        }
        match RoutineLibrary::load_from_dir(&dir) {
            Ok(library) => return library,
            Err(e) => eprintln!("warning: failed to load routine library from {dir:?}: {e}"),
        }
    }
    eprintln!("warning: no readable routine directory found; starting with an empty routine library");
    RoutineLibrary::empty()
}

fn main() {
    // Ruling 2: Tauri v2 removed the pre-app-instance `tauri::api::path::app_data_dir`
    // free function used by Tauri v1. The v2 replacement (`app_handle.path().app_data_dir()`,
    // via the `Manager` trait) requires an `App`/`AppHandle`. The database is opened here,
    // before the builder exists, using the `dirs` crate fallback the brief explicitly
    // sanctions: `dirs::data_dir()` joined with an app-specific subdirectory. This resolves
    // to `~/Library/Application Support` on macOS, `%APPDATA%` on Windows, and
    // `$XDG_DATA_HOME`/`~/.local/share` on Linux — a persistent, writable, app-local
    // directory, satisfying the same intent as `app_data_dir`.
    let db_path = dirs::data_dir()
        .unwrap_or_else(std::env::temp_dir)
        .join("ladder-studio")
        .join("ladder-studio.sqlite");
    std::fs::create_dir_all(db_path.parent().unwrap()).ok();
    let db = ladder_storage::open(&db_path).expect("failed to open ladder-studio.sqlite");

    tauri::Builder::default()
        // Routines, unlike the database, *do* need an `AppHandle` to locate: only
        // `app.path()` knows where the platform put the bundled resource directory.
        // `.setup()` is the first point that handle exists, so state is registered
        // there rather than on the builder.
        .setup(move |app| {
            let routines = load_routine_library(app.handle());
            app.manage(AppState { routines, db: Mutex::new(db) });
            Ok(())
        })
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
