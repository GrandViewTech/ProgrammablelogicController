use std::path::Path;

use ladder_model::Screen;
use rusqlite::{params, Connection};

#[derive(Debug, thiserror::Error)]
pub enum StorageError {
    #[error("sqlite error: {0}")]
    Sqlite(#[from] rusqlite::Error),
    #[error("json error: {0}")]
    Json(#[from] serde_json::Error),
    #[error("no screen named {0}")]
    NotFound(String),
}

const SCHEMA: &str = "
CREATE TABLE IF NOT EXISTS screens (
    name TEXT PRIMARY KEY,
    data TEXT NOT NULL
);
";

/// Opens (creating if needed) the SQLite store and applies the schema.
/// Replaces the original's loose XStream XML files on disk (requirements §2).
pub fn open(path: &Path) -> Result<Connection, StorageError> {
    let conn = Connection::open(path)?;
    conn.execute_batch(SCHEMA)?;
    Ok(conn)
}

pub fn save_screen(conn: &Connection, name: &str, screen: &Screen) -> Result<(), StorageError> {
    let data = serde_json::to_string(screen)?;
    conn.execute(
        "INSERT INTO screens (name, data) VALUES (?1, ?2)
         ON CONFLICT(name) DO UPDATE SET data = excluded.data",
        params![name, data],
    )?;
    Ok(())
}

pub fn load_screen(conn: &Connection, name: &str) -> Result<Screen, StorageError> {
    let data: String = conn
        .query_row("SELECT data FROM screens WHERE name = ?1", params![name], |row| row.get(0))
        .map_err(|e| match e {
            rusqlite::Error::QueryReturnedNoRows => StorageError::NotFound(name.to_string()),
            other => StorageError::Sqlite(other),
        })?;
    Ok(serde_json::from_str(&data)?)
}

pub fn list_screens(conn: &Connection) -> Result<Vec<String>, StorageError> {
    let mut stmt = conn.prepare("SELECT name FROM screens ORDER BY name")?;
    let rows = stmt.query_map([], |row| row.get(0))?;
    let mut names = Vec::new();
    for row in rows {
        names.push(row?);
    }
    Ok(names)
}

/// Portable export, independent of the SQLite store (requirements §2, §4).
pub fn export_json(screen: &Screen) -> String {
    serde_json::to_string_pretty(screen).expect("Screen always serializes")
}

pub fn import_json(json: &str) -> Result<Screen, serde_json::Error> {
    serde_json::from_str(json)
}

#[cfg(test)]
mod tests {
    use super::*;

    fn empty_screen() -> Screen {
        Screen { rows: Vec::new(), end_row_number: None, end_column_number: None }
    }

    #[test]
    fn save_then_load_round_trips() {
        let conn = open(Path::new(":memory:")).unwrap();
        save_screen(&conn, "line-1", &empty_screen()).unwrap();
        let loaded = load_screen(&conn, "line-1").unwrap();
        assert_eq!(loaded, empty_screen());
    }

    #[test]
    fn save_twice_updates_rather_than_erroring() {
        let conn = open(Path::new(":memory:")).unwrap();
        save_screen(&conn, "line-1", &empty_screen()).unwrap();
        save_screen(&conn, "line-1", &empty_screen()).unwrap();
        assert_eq!(list_screens(&conn).unwrap(), vec!["line-1".to_string()]);
    }

    #[test]
    fn load_missing_screen_is_not_found_error() {
        let conn = open(Path::new(":memory:")).unwrap();
        let err = load_screen(&conn, "nope").unwrap_err();
        assert!(matches!(err, StorageError::NotFound(name) if name == "nope"));
    }

    #[test]
    fn export_then_import_round_trips() {
        let json = export_json(&empty_screen());
        let back = import_json(&json).unwrap();
        assert_eq!(back, empty_screen());
    }
}
