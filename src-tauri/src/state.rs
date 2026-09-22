use std::sync::Mutex;

use ladder_routines::RoutineLibrary;
use rusqlite::Connection;

pub struct AppState {
    pub routines: RoutineLibrary,
    pub db: Mutex<Connection>,
}
