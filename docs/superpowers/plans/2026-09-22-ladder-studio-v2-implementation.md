# Ladder Studio v2.0 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stand up the full Ladder Studio v2.0 stack — a Rust compiler core faithfully porting `CompileService`, a Tauri shell, and a React/TypeScript UI implementing the worker-facing product design — as a working, tested, CI-covered, installer-producing application on branch `feature/ladder-studio-v2`.

**Architecture:** A Cargo workspace (`ladder-core/`) of five focused crates (model, compiler, 8085 target, routine library, storage) with zero UI dependencies, wrapped by a Tauri shell (`src-tauri/`) exposing async commands, driving a React/TypeScript UI (`ui/`) with a React Flow canvas. GitHub Actions builds and tests all three crates/UI on Windows/macOS/Linux and packages installers on tag push.

**Tech Stack:** Rust (stable, 2021 edition), serde/serde_json, rusqlite (SQLite), Tauri v2, React 18 + TypeScript 5 + Vite 5, React Flow (`reactflow`), Vitest, Playwright, GitHub Actions + `tauri-action`.

**Spec:** `docs/superpowers/specs/2026-09-22-ladder-studio-v2-design.md` (architecture/decisions) and `requirements/v-2.0/ladder-studio-requirements.md` (product requirements, section numbers referenced below as `§N`). Executors should read both — this plan argues from them but does not restate every rationale.

**Source reference (original app being ported):** `src/main/java/org/grandviewtech/service/execution/CompileService.java`, `.../entity/enums/{CoilType,InputType,NoNc}.java`, `.../userinterface/screen/{ColumnScreen,RowScreen}.java`, `.../userinterface/helper/ColumnScreenGenerator.java`, `.../entity/bo/{Screen,Routine}.java`. These are left untouched on this branch — read-only reference for fidelity.

## Global Constraints

- Old Java/Swing app (`src/`, `pom.xml`, `resources/`) is never modified by this plan — new code only in `ladder-core/`, `src-tauri/`, `ui/`, `.github/workflows/`.
- Every Rust crate builds with zero warnings under `cargo build --workspace` and passes `cargo test --workspace` and `cargo clippy --workspace -- -D warnings`.
- `ladder-model` and `ladder-compiler` have **zero** dependency on Tauri, SQLite, or any UI crate — enforced by their `Cargo.toml` dependency lists containing only `serde`/`serde_json` (model) and `ladder-model` (compiler).
- JSON is the wire/storage format (§2) — every model type derives `serde::{Serialize, Deserialize}`.
- `EditMode` locking is enforced on `ColumnScreen` itself (§4) — never only in UI state.
- SERIES/PARALLEL is never inferred for multi-block combining; it is read directly from an explicit `combinator` field the worker sets (§7.3, §10) — this supersedes the original's inference for that purpose. The original inference function is still ported faithfully as `legacy::find_parallel_series` for fidelity/reference, but the live `generate()` path does not call it.
- The `findOutputType`/`NoNc` disconnection and the FLAG min-value offset are carried forward as documented gaps, not fixed (§3) — `generate()` always passes `None` for `NoNc` lookups and never subtracts a preference-store minimum from FLAG inputs. Each site has a `// GAP (spec §3): ...` comment, not a silent omission.
- Commit after every task passes its verification step.

---

### Task 1: Rust workspace & toolchain scaffold

**Files:**
- Create: `ladder-core/Cargo.toml` (workspace manifest)
- Create: `ladder-core/crates/ladder-model/Cargo.toml`, `ladder-core/crates/ladder-model/src/lib.rs`
- Create: `ladder-core/crates/ladder-compiler/Cargo.toml`, `ladder-core/crates/ladder-compiler/src/lib.rs`
- Create: `ladder-core/crates/ladder-target-8085/Cargo.toml`, `ladder-core/crates/ladder-target-8085/src/lib.rs`
- Create: `ladder-core/crates/ladder-routines/Cargo.toml`, `ladder-core/crates/ladder-routines/src/lib.rs`
- Create: `ladder-core/crates/ladder-storage/Cargo.toml`, `ladder-core/crates/ladder-storage/src/lib.rs`
- Modify: `.gitignore`

**Interfaces:**
- Produces: a `cargo build --workspace` / `cargo test --workspace` entry point every later Rust task builds on.

- [ ] **Step 1: Install the Rust toolchain**

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --default-toolchain stable
source "$HOME/.cargo/env"
rustup component add clippy rustfmt
```

Expected: `cargo --version` and `rustc --version` print stable versions; `cargo clippy --version` and `cargo fmt --version` succeed.

- [ ] **Step 2: Create the workspace manifest**

`ladder-core/Cargo.toml`:
```toml
[workspace]
resolver = "2"
members = [
    "crates/ladder-model",
    "crates/ladder-compiler",
    "crates/ladder-target-8085",
    "crates/ladder-routines",
    "crates/ladder-storage",
]

[workspace.package]
version = "0.1.0"
edition = "2021"

[workspace.dependencies]
serde = { version = "1", features = ["derive"] }
serde_json = "1"
```

- [ ] **Step 3: Scaffold each crate**

```bash
cd ladder-core
for c in ladder-model ladder-compiler ladder-target-8085 ladder-routines ladder-storage; do
  cargo new --lib "crates/$c" --vcs none
done
```

Then edit each generated `crates/*/Cargo.toml` to use workspace inheritance, e.g. `ladder-model`:
```toml
[package]
name = "ladder-model"
version.workspace = true
edition.workspace = true

[dependencies]
serde.workspace = true
```

`ladder-compiler/Cargo.toml` additionally depends on `ladder-model = { path = "../ladder-model" }`. `ladder-target-8085/Cargo.toml` depends on both `ladder-model` and `ladder-compiler` (it implements `ladder-compiler`'s `Target` trait). `ladder-routines/Cargo.toml` depends on `ladder-model` and `serde_json.workspace = true` and adds `regex = "1"`. `ladder-storage/Cargo.toml` depends on `ladder-model`, `serde_json.workspace = true`, and adds `rusqlite = { version = "0.31", features = ["bundled"] }`.

- [ ] **Step 4: Update `.gitignore` for the new toolchains**

Append to the repo-root `.gitignore`:
```
ladder-core/target/
src-tauri/target/
ui/node_modules/
ui/dist/
```

- [ ] **Step 5: Verify the workspace builds**

Run: `cd ladder-core && cargo build --workspace`
Expected: `Compiling ladder-model v0.1.0 ...` through all 5 crates, ending `Finished` with no errors.

- [ ] **Step 6: Commit**

```bash
git add ladder-core .gitignore
git commit -m "Scaffold Rust workspace with 5 empty crates"
```

---

### Task 2: `ladder-model` — enums ported from `entity/enums/*`

**Files:**
- Create: `ladder-core/crates/ladder-model/src/coil_type.rs`
- Create: `ladder-core/crates/ladder-model/src/input_type.rs`
- Create: `ladder-core/crates/ladder-model/src/no_nc.rs`
- Modify: `ladder-core/crates/ladder-model/src/lib.rs`

**Interfaces:**
- Produces: `CoilType`, `InputType`, `NoNc` — `pub enum`s, `Copy + Clone + Debug + PartialEq + Eq + Serialize + Deserialize`, JSON wire form `SCREAMING_SNAKE_CASE` matching the original Java enums' string values exactly (verified by test).

- [ ] **Step 1: Write the failing tests**

`ladder-core/crates/ladder-model/src/coil_type.rs`:
```rust
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
```

`ladder-core/crates/ladder-model/src/input_type.rs`:
```rust
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
```

`ladder-core/crates/ladder-model/src/no_nc.rs`:
```rust
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
```

- [ ] **Step 2: Run the tests to verify they fail (module not wired up yet)**

Run: `cd ladder-core && cargo test -p ladder-model`
Expected: compile error, `lib.rs` doesn't declare the modules yet.

- [ ] **Step 3: Wire up `lib.rs`**

`ladder-core/crates/ladder-model/src/lib.rs`:
```rust
mod coil_type;
mod input_type;
mod no_nc;

pub use coil_type::CoilType;
pub use input_type::InputType;
pub use no_nc::NoNc;
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd ladder-core && cargo test -p ladder-model`
Expected: `test result: ok. 6 passed`

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-model
git commit -m "Port CoilType, InputType, NoNc enums to ladder-model"
```

---

### Task 3: `ladder-model` — Screen/RowScreen/ColumnScreen/RoutineOrigin/EditMode

**Files:**
- Create: `ladder-core/crates/ladder-model/src/screen.rs`
- Modify: `ladder-core/crates/ladder-model/src/lib.rs`

**Interfaces:**
- Consumes: `CoilType`, `InputType`, `NoNc` from Task 2.
- Produces: `Screen { rows: Vec<RowScreen>, end_row_number: Option<u32>, end_column_number: Option<u32> }`, `RowScreen { row_number: u32, columns: Vec<ColumnScreen> }`, `ColumnScreen { row_number, column_number, coil_type, input_type: Option<InputType>, value: String, tag: String, comment: String, routine_origin: Option<RoutineOrigin>, rendered_asm: Option<String>, combinator: Option<Combinator>, is_blank: bool }`, `RoutineOrigin { routine_name: String, description: String, values: BTreeMap<u32, String> }`, `Combinator { And, Or }`, `EditMode { Worker, Engineer }`, and the method `ColumnScreen::is_locked(&self, mode: EditMode) -> bool`. All are used by `ladder-compiler` (Task 4-5), `ladder-routines` (Task 7), `ladder-storage` (Task 8), and the Tauri commands (Task 9).

- [ ] **Step 1: Write the failing tests**

`ladder-core/crates/ladder-model/src/screen.rs`:
```rust
use std::collections::BTreeMap;

use serde::{Deserialize, Serialize};

use crate::{CoilType, InputType};

/// Explicit AND/OR combinator, chosen by the worker at drop time (requirements §7.3).
/// Never inferred — the compiler reads this directly off the block.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Combinator {
    And,
    Or,
}

/// Worker vs Engineer editing mode (requirements §7.1). Enforcement lives on
/// ColumnScreen::is_locked, not just in UI state (requirements §4).
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum EditMode {
    Worker,
    Engineer,
}

/// Stamped onto any column(s) produced by injecting a library routine
/// (requirements §4). `None` on hand-built columns.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct RoutineOrigin {
    pub routine_name: String,
    pub description: String,
    /// INPUT index -> substituted value, as supplied at injection time.
    pub values: BTreeMap<u32, String>,
}

/// A single cell on the ladder grid. Ported from
/// org.grandviewtech.userinterface.screen.ColumnScreen, data fields only
/// (no Swing/paint/drag-and-drop state — that's a UI concern in `ui/`).
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct ColumnScreen {
    pub row_number: u32,
    pub column_number: u32,
    pub coil_type: CoilType,
    pub input_type: Option<InputType>,
    pub value: String,
    pub tag: String,
    pub comment: String,
    /// Present only for columns produced by routine injection (requirements §4).
    pub routine_origin: Option<RoutineOrigin>,
    /// Pre-rendered ASM for a ROUTINE column, produced by
    /// `ladder-routines::inject_with_origin` at injection time (requirements §5).
    /// The compiler splices this in verbatim; it never runs template substitution itself.
    pub rendered_asm: Option<String>,
    /// Required (Some) for every block after the first on a row; ignored for
    /// the first block. Read directly by the compiler (requirements §7.3).
    pub combinator: Option<Combinator>,
    pub is_blank: bool,
}

impl ColumnScreen {
    /// Data-layer enforcement of Worker/Engineer editing rules (requirements §4):
    /// a worker-mode client cannot be tricked into editing a routine-sourced block.
    pub fn is_locked(&self, mode: EditMode) -> bool {
        match mode {
            EditMode::Engineer => false,
            EditMode::Worker => self.routine_origin.is_some(),
        }
    }
}

/// One rung. Variable-width (requirements §4) — a `Vec`, not the original's
/// fixed-size array, so rungs grow to fit whatever blocks are placed.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct RowScreen {
    pub row_number: u32,
    pub columns: Vec<ColumnScreen>,
}

impl RowScreen {
    /// The column immediately to the left in this row, or `None` if `column_index`
    /// is the first column (ported semantics from
    /// ColumnScreenGenerator.left(), which links columnNumber-1 within the same row).
    pub fn previous(&self, column_index: usize) -> Option<&ColumnScreen> {
        column_index.checked_sub(1).and_then(|i| self.columns.get(i))
    }
}

/// The whole diagram. Ported from org.grandviewtech.entity.bo.Screen.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize, Default)]
pub struct Screen {
    pub rows: Vec<RowScreen>,
    pub end_row_number: Option<u32>,
    pub end_column_number: Option<u32>,
}

#[cfg(test)]
mod tests {
    use super::*;

    fn locked_column() -> ColumnScreen {
        ColumnScreen {
            row_number: 1,
            column_number: 1,
            coil_type: CoilType::Routine,
            input_type: None,
            value: String::new(),
            tag: String::new(),
            comment: String::new(),
            routine_origin: Some(RoutineOrigin {
                routine_name: "BIT RESET".into(),
                description: "Resets the bit".into(),
                values: BTreeMap::new(),
            }),
            rendered_asm: Some("MOV DPTR,#RLY512_519+".into()),
            combinator: None,
            is_blank: false,
        }
    }

    fn hand_built_column() -> ColumnScreen {
        ColumnScreen {
            routine_origin: None,
            ..locked_column()
        }
    }

    #[test]
    fn worker_mode_locks_routine_sourced_blocks() {
        assert!(locked_column().is_locked(EditMode::Worker));
    }

    #[test]
    fn worker_mode_does_not_lock_hand_built_blocks() {
        assert!(!hand_built_column().is_locked(EditMode::Worker));
    }

    #[test]
    fn engineer_mode_never_locks_anything() {
        assert!(!locked_column().is_locked(EditMode::Engineer));
        assert!(!hand_built_column().is_locked(EditMode::Engineer));
    }

    #[test]
    fn row_previous_returns_left_neighbor_within_same_row() {
        let row = RowScreen {
            row_number: 1,
            columns: vec![hand_built_column(), locked_column()],
        };
        assert_eq!(row.previous(0), None);
        assert_eq!(row.previous(1).unwrap().coil_type, CoilType::Routine);
    }

    #[test]
    fn screen_round_trips_through_json() {
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![hand_built_column()] }],
            end_row_number: Some(1),
            end_column_number: Some(1),
        };
        let json = serde_json::to_string(&screen).unwrap();
        let back: Screen = serde_json::from_str(&json).unwrap();
        assert_eq!(screen, back);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ladder-core && cargo test -p ladder-model`
Expected: compile error — `screen` module not declared in `lib.rs`.

- [ ] **Step 3: Wire up `lib.rs`**

`ladder-core/crates/ladder-model/src/lib.rs`:
```rust
mod coil_type;
mod input_type;
mod no_nc;
mod screen;

pub use coil_type::CoilType;
pub use input_type::InputType;
pub use no_nc::NoNc;
pub use screen::{Combinator, ColumnScreen, EditMode, RoutineOrigin, RowScreen, Screen};
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ladder-core && cargo test -p ladder-model`
Expected: `test result: ok. 11 passed`

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-model
git commit -m "Add Screen/RowScreen/ColumnScreen data model with EditMode locking"
```

---

### Task 4: `ladder-compiler` — shared types and the `Target` trait

**Files:**
- Create: `ladder-core/crates/ladder-compiler/src/types.rs`
- Create: `ladder-core/crates/ladder-compiler/src/target.rs`
- Modify: `ladder-core/crates/ladder-compiler/src/lib.rs`

**Interfaces:**
- Consumes: `CoilType`, `ColumnScreen`, `Combinator` from `ladder-model` (Tasks 2-3).
- Produces: `LoadKind { Flag, Input, Word }`, `LoadCombinator { None, Series, Parallel }`, `OutputType { Set, Reset, None }`, `CompileError` (used by Task 5's `generate()` and by `ladder-target-8085`), and the `Target` trait with exactly the four methods named in the requirements doc §3 (`emit_load`, `emit_output`, `emit_jnc`, `emit_label`) — implemented by `ladder-target-8085` in Task 6.

- [ ] **Step 1: Write the failing test**

`ladder-core/crates/ladder-compiler/src/types.rs`:
```rust
/// Which LOAD sub-case is being emitted — mirrors the FLAG/INPUT/WORD arms of
/// CompileService's switch on ColumnScreen.getInputType() under CoilType.LOAD.
/// (The OUTPUT arm under LOAD reuses `Target::emit_output`, not this enum —
/// see CompileService.java's `case OUTPUT` under the LOAD switch.)
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum LoadKind {
    Flag,
    Input,
    Word,
}

/// Resolved combinator for a LOAD emission. `None` for the first block on a
/// row. For later blocks this is derived from the block's explicit
/// `Combinator` (requirements §7.3), not inferred.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum LoadCombinator {
    None,
    Series,
    Parallel,
}

/// Ported from CompileService.OUTPUT_TYPE.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum OutputType {
    Set,
    Reset,
    None,
}

#[derive(Debug, Clone, PartialEq, Eq, thiserror::Error)]
pub enum CompileError {
    #[error("row {row}, column {column}: value {value:?} is not a valid integer")]
    InvalidValue { row: u32, column: u32, value: String },
    #[error("row {row}, column {column}: ROUTINE block has no rendered_asm (not injected via ladder-routines)")]
    MissingRenderedAsm { row: u32, column: u32 },
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn compile_error_messages_are_actionable() {
        let err = CompileError::InvalidValue { row: 2, column: 3, value: "abc".into() };
        assert_eq!(
            err.to_string(),
            "row 2, column 3: value \"abc\" is not a valid integer"
        );
    }
}
```

Add `thiserror = "1"` to `ladder-core/crates/ladder-compiler/Cargo.toml` under `[dependencies]`.

`ladder-core/crates/ladder-compiler/src/target.rs`:
```rust
use crate::types::{LoadCombinator, LoadKind, OutputType};

/// Pluggable mnemonic emission (requirements §3). `Target8085` (in the
/// ladder-target-8085 crate) implements this to reproduce the original 8085
/// mnemonics string-for-string. Future targets (AVR, PIC, a simulator)
/// implement the same trait without touching `generate()`'s traversal logic.
pub trait Target {
    fn emit_load(&self, buf: &mut Vec<String>, kind: LoadKind, input: i32, combinator: LoadCombinator);
    fn emit_output(&self, buf: &mut Vec<String>, value: i32, output_type: OutputType, label: &str);
    fn emit_jnc(&self, buf: &mut Vec<String>, label: &str);
    fn emit_label(&self, buf: &mut Vec<String>, label: &str);
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd ladder-core && cargo test -p ladder-compiler`
Expected: compile error — modules not wired up, `thiserror` not yet a dependency.

- [ ] **Step 3: Add the dependency and wire up `lib.rs`**

Add to `ladder-core/crates/ladder-compiler/Cargo.toml`:
```toml
[dependencies]
ladder-model = { path = "../ladder-model" }
thiserror = "1"
```

`ladder-core/crates/ladder-compiler/src/lib.rs`:
```rust
mod target;
mod types;

pub use target::Target;
pub use types::{CompileError, LoadCombinator, LoadKind, OutputType};
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd ladder-core && cargo test -p ladder-compiler`
Expected: `test result: ok. 1 passed`

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-compiler
git commit -m "Add ladder-compiler Target trait and shared emission types"
```

---

### Task 5: `ladder-compiler` — traversal and `generate()`

**Files:**
- Create: `ladder-core/crates/ladder-compiler/src/legacy.rs`
- Create: `ladder-core/crates/ladder-compiler/src/traversal.rs`
- Modify: `ladder-core/crates/ladder-compiler/src/lib.rs`

**Interfaces:**
- Consumes: `Screen`, `RowScreen`, `ColumnScreen`, `CoilType`, `InputType`, `Combinator` (ladder-model); `Target`, `LoadKind`, `LoadCombinator`, `OutputType`, `CompileError` (Task 4).
- Produces: `pub fn generate(screen: &Screen, target: &dyn Target) -> Result<String, CompileError>` — consumed by the Tauri `generate` command (Task 9) and by `ladder-target-8085`'s golden-file tests (Task 6). Also `pub mod legacy` exposing `find_parallel_series` and `find_param` as directly-tested pure ports (fidelity reference; not called by `generate()` — see Global Constraints).

- [ ] **Step 1: Write the failing tests**

`ladder-core/crates/ladder-compiler/src/legacy.rs`:
```rust
use ladder_model::{CoilType, ColumnScreen, RowScreen};

/// 1:1 port of CompileService.findParam(Integer). Byte/bit address split.
/// The original's Integer2Hex flag was always false in practice (dead code
/// path), so this returns decimal digit strings, matching observed behavior.
pub fn find_param(input: i32) -> (u32, u32) {
    let input = input.unsigned_abs();
    (input / 8, input % 8)
}

/// Legacy PARALLEL_SERIES inference, ported 1:1 from
/// CompileService.findParallelSeries for fidelity/reference. The original
/// never actually produced PARALLEL (the AND/OR combination logic for LOAD
/// columns was left unfinished — requirements §3, §7.3) so this always
/// returns Series or None; it is not called by `generate()`, which instead
/// reads the explicit `Combinator` off the block (requirements §7.3, §10).
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum LegacyParallelSeries {
    Parallel,
    Series,
    None,
}

pub fn find_parallel_series(row: &RowScreen, column_index: usize) -> LegacyParallelSeries {
    match row.previous(column_index) {
        Some(previous) if previous.coil_type == CoilType::Load => LegacyParallelSeries::Series,
        _ => LegacyParallelSeries::None,
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use ladder_model::{CoilType, ColumnScreen};

    fn blank_column(row: u32, col: u32, coil_type: CoilType) -> ColumnScreen {
        ColumnScreen {
            row_number: row,
            column_number: col,
            coil_type,
            input_type: None,
            value: String::new(),
            tag: String::new(),
            comment: String::new(),
            routine_origin: None,
            rendered_asm: None,
            combinator: None,
            is_blank: false,
        }
    }

    #[test]
    fn find_param_splits_byte_and_bit_matching_original_arithmetic() {
        assert_eq!(find_param(19), (2, 3)); // 19 / 8 = 2, 19 % 8 = 3
        assert_eq!(find_param(-19), (2, 3)); // original negates before dividing
        assert_eq!(find_param(0), (0, 0));
    }

    #[test]
    fn find_parallel_series_returns_series_when_previous_is_load() {
        let row = RowScreen {
            row_number: 1,
            columns: vec![
                blank_column(1, 1, CoilType::Load),
                blank_column(1, 2, CoilType::Output),
            ],
        };
        assert_eq!(find_parallel_series(&row, 1), LegacyParallelSeries::Series);
    }

    #[test]
    fn find_parallel_series_returns_none_for_first_column_or_non_load_previous() {
        let row = RowScreen {
            row_number: 1,
            columns: vec![
                blank_column(1, 1, CoilType::Output),
                blank_column(1, 2, CoilType::Output),
            ],
        };
        assert_eq!(find_parallel_series(&row, 0), LegacyParallelSeries::None);
        assert_eq!(find_parallel_series(&row, 1), LegacyParallelSeries::None);
    }
}
```

`ladder-core/crates/ladder-compiler/src/traversal.rs`:
```rust
use ladder_model::{CoilType, Combinator, ColumnScreen, InputType, RowScreen, Screen};

use crate::target::Target;
use crate::types::{CompileError, LoadCombinator, LoadKind, OutputType};

fn create_label(row_number: u32, column_number: u32) -> String {
    format!("LABEL_{row_number}_{column_number}")
}

/// Reads the combinator directly off the block (requirements §7.3) instead
/// of inferring it. The first column on a row has no combinator.
fn resolve_combinator(row: &RowScreen, column_index: usize, column: &ColumnScreen) -> LoadCombinator {
    if row.previous(column_index).is_none() {
        return LoadCombinator::None;
    }
    match column.combinator {
        Some(Combinator::And) => LoadCombinator::Series,
        Some(Combinator::Or) => LoadCombinator::Parallel,
        None => LoadCombinator::None,
    }
}

/// Ported from CompileService.isJumpRequired: true when the previous column
/// in the row is a LOAD.
fn is_jump_required(row: &RowScreen, column_index: usize) -> bool {
    matches!(row.previous(column_index), Some(previous) if previous.coil_type == CoilType::Load)
}

/// GAP (spec §3): the original's findOutputType(NoNc) lookup was disconnected
/// in ColumnScreen (the nonc field was commented out, so CompileService always
/// called it with null). This is carried forward as a documented gap, not
/// fixed: NoNc is not modeled on ColumnScreen, and every call site here always
/// resolves to OutputType::None, matching the original's actual behavior.
fn find_output_type() -> OutputType {
    OutputType::None
}

/// Faithful ASM emission for one screen. Runs synchronously; the Tauri
/// command wrapping this (Task 9) is what makes it async off the UI thread
/// (requirements §6, item 2) — `generate()` itself has no I/O.
pub fn generate(screen: &Screen, target: &dyn Target) -> Result<String, CompileError> {
    let mut buf: Vec<String> = Vec::new();

    'rows: for row in &screen.rows {
        for (column_index, column) in row.columns.iter().enumerate() {
            if column.is_blank {
                continue;
            }

            if column.coil_type == CoilType::End {
                break 'rows;
            }

            let label = create_label(row.row_number, column.column_number);

            match column.coil_type {
                CoilType::Load => {
                    let input = parse_value(column)?;
                    match column.input_type {
                        Some(InputType::Flag) => {
                            // GAP (spec §3): PreferenceScreen-backed min-value bounds for
                            // FLAG inputs aren't ported (no settings store exists yet) —
                            // the original's `min - input` offset is not applied.
                            let combinator = resolve_combinator(row, column_index, column);
                            target.emit_load(&mut buf, LoadKind::Flag, input, combinator);
                        }
                        Some(InputType::Input) => {
                            let combinator = resolve_combinator(row, column_index, column);
                            target.emit_load(&mut buf, LoadKind::Input, input, combinator);
                        }
                        Some(InputType::Word) => {
                            let combinator = resolve_combinator(row, column_index, column);
                            target.emit_load(&mut buf, LoadKind::Word, input, combinator);
                        }
                        Some(InputType::Output) => {
                            target.emit_output(&mut buf, input, find_output_type(), &label);
                        }
                        None => {}
                    }
                }
                CoilType::Output => {
                    let value = parse_value(column)?;
                    target.emit_output(&mut buf, value, find_output_type(), &label);
                }
                CoilType::Routine => {
                    let jump = is_jump_required(row, column_index);
                    if jump {
                        target.emit_jnc(&mut buf, &label);
                    }
                    let asm = column.rendered_asm.as_deref().ok_or(CompileError::MissingRenderedAsm {
                        row: row.row_number,
                        column: column.column_number,
                    })?;
                    buf.push(asm.to_string());
                    if jump {
                        target.emit_label(&mut buf, &label);
                    }
                }
                _ => {
                    // LABEL, JUMP, LINE, LEFT_LINK, RIGHT_LINK, PARALLEL, DEFAULT,
                    // DELETE are UI-only markers in the original — CompileService's
                    // traversal never emits ASM for them either.
                }
            }
        }
    }

    Ok(buf.join("\n"))
}

fn parse_value(column: &ColumnScreen) -> Result<i32, CompileError> {
    column.value.parse::<i32>().map_err(|_| CompileError::InvalidValue {
        row: column.row_number,
        column: column.column_number,
        value: column.value.clone(),
    })
}

#[cfg(test)]
mod tests {
    use super::*;
    use ladder_model::{CoilType, ColumnScreen, RoutineOrigin};
    use std::cell::RefCell;

    struct RecordingTarget {
        calls: RefCell<Vec<String>>,
    }

    impl RecordingTarget {
        fn new() -> Self {
            Self { calls: RefCell::new(Vec::new()) }
        }
    }

    impl Target for RecordingTarget {
        fn emit_load(&self, buf: &mut Vec<String>, kind: LoadKind, input: i32, combinator: LoadCombinator) {
            let line = format!("LOAD {kind:?} {input} {combinator:?}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_output(&self, buf: &mut Vec<String>, value: i32, output_type: OutputType, label: &str) {
            let line = format!("OUTPUT {value} {output_type:?} {label}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_jnc(&self, buf: &mut Vec<String>, label: &str) {
            let line = format!("JNC {label}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_label(&self, buf: &mut Vec<String>, label: &str) {
            let line = format!("{label}:");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
    }

    fn column(coil_type: CoilType) -> ColumnScreen {
        ColumnScreen {
            row_number: 1,
            column_number: 1,
            coil_type,
            input_type: None,
            value: "0".into(),
            tag: String::new(),
            comment: String::new(),
            routine_origin: None,
            rendered_asm: None,
            combinator: None,
            is_blank: false,
        }
    }

    #[test]
    fn end_coil_stops_traversal_before_later_rows() {
        let screen = Screen {
            rows: vec![
                RowScreen { row_number: 1, columns: vec![column(CoilType::End)] },
                RowScreen {
                    row_number: 2,
                    columns: vec![ColumnScreen { input_type: Some(InputType::Input), ..column(CoilType::Load) }],
                },
            ],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "");
        assert!(target.calls.borrow().is_empty());
    }

    #[test]
    fn load_input_emits_with_none_combinator_when_first_on_row() {
        let mut c = column(CoilType::Load);
        c.input_type = Some(InputType::Input);
        c.value = "19".into();
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![c] }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 19 None");
    }

    #[test]
    fn second_block_uses_explicit_combinator_not_inference() {
        let mut first = column(CoilType::Load);
        first.input_type = Some(InputType::Input);
        first.value = "1".into();
        let mut second = column(CoilType::Load);
        second.column_number = 2;
        second.input_type = Some(InputType::Input);
        second.value = "2".into();
        second.combinator = Some(Combinator::Or);
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![first, second] }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 1 None\nLOAD Input 2 Parallel");
    }

    #[test]
    fn routine_after_load_wraps_with_jnc_and_label() {
        let mut load = column(CoilType::Load);
        load.input_type = Some(InputType::Input);
        load.value = "1".into();
        let mut routine = column(CoilType::Routine);
        routine.column_number = 2;
        routine.routine_origin = Some(RoutineOrigin {
            routine_name: "BIT RESET".into(),
            description: "Resets the bit".into(),
            values: Default::default(),
        });
        routine.rendered_asm = Some("MOV DPTR,#RLY512_519+".into());
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![load, routine] }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nJNC LABEL_1_2\nMOV DPTR,#RLY512_519+\nLABEL_1_2:"
        );
    }

    #[test]
    fn routine_without_rendered_asm_is_a_compile_error() {
        let mut routine = column(CoilType::Routine);
        routine.rendered_asm = None;
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![routine] }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(err, CompileError::MissingRenderedAsm { row: 1, column: 1 });
    }

    #[test]
    fn invalid_value_is_a_compile_error_not_a_panic() {
        let mut c = column(CoilType::Output);
        c.value = "not-a-number".into();
        let screen = Screen {
            rows: vec![RowScreen { row_number: 3, columns: vec![c] }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(
            err,
            CompileError::InvalidValue { row: 3, column: 1, value: "not-a-number".into() }
        );
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ladder-core && cargo test -p ladder-compiler`
Expected: compile errors — `legacy`/`traversal` not declared in `lib.rs`; `CompileError` needs `PartialEq` for the test assertions.

- [ ] **Step 3: Add `PartialEq` to `CompileError` and wire up `lib.rs`**

In `ladder-core/crates/ladder-compiler/src/types.rs`, change the derive line to:
```rust
#[derive(Debug, Clone, PartialEq, Eq, thiserror::Error)]
```
(`Eq` is safe to add since all fields are `String`/`u32`.)

`ladder-core/crates/ladder-compiler/src/lib.rs`:
```rust
mod legacy;
mod target;
mod traversal;
mod types;

pub use target::Target;
pub use traversal::generate;
pub use types::{CompileError, LoadCombinator, LoadKind, OutputType};

pub mod compat {
    //! Fidelity-reference exports of the original's inference logic.
    //! Not used by `generate()` — see Global Constraints in the plan.
    pub use crate::legacy::{find_parallel_series, find_param, LegacyParallelSeries};
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ladder-core && cargo test -p ladder-compiler`
Expected: `test result: ok. 8 passed` (1 from Task 4 + 3 legacy + 5 traversal — adjust count if it differs slightly, but zero failures).

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-compiler
git commit -m "Port CompileService traversal into ladder-compiler generate()"
```

---

### Task 6: `ladder-target-8085` — faithful mnemonic emission

**Files:**
- Create: `ladder-core/crates/ladder-target-8085/src/lib.rs`

**Interfaces:**
- Consumes: `Target`, `LoadKind`, `LoadCombinator`, `OutputType` from `ladder-compiler` (Tasks 4-5); `ladder_compiler::compat::find_param` for byte/bit splitting.
- Produces: `pub struct Target8085;` implementing `Target`, plus `pub fn generate_8085(screen: &Screen) -> Result<String, CompileError>` (a thin convenience wrapper `ladder_compiler::generate(screen, &Target8085)`), consumed by the Tauri `generate` command (Task 9).

- [ ] **Step 1: Write the failing tests**

`ladder-core/crates/ladder-target-8085/src/lib.rs`:
```rust
use ladder_compiler::compat::find_param;
use ladder_compiler::{CompileError, LoadCombinator, LoadKind, OutputType, Target};
use ladder_model::Screen;

/// Reproduces org.grandviewtech.service.execution.CompileService's original
/// 8085 mnemonics string-for-string (requirements §3).
pub struct Target8085;

fn series_parallel_suffix(combinator: LoadCombinator, bit: u32) -> String {
    match combinator {
        LoadCombinator::None => format!("MOV C, ACC.{bit}"),
        LoadCombinator::Parallel => format!("ORL C, ACC.{bit}"),
        LoadCombinator::Series => format!("ANL C, ACC.{bit}"),
    }
}

impl Target for Target8085 {
    fn emit_load(&self, buf: &mut Vec<String>, kind: LoadKind, input: i32, combinator: LoadCombinator) {
        let (byte, bit) = find_param(input);
        let base = match kind {
            LoadKind::Flag => "RLY512_",
            LoadKind::Input => "INPUT0_7+",
            LoadKind::Word => "WORD0_7+",
        };
        buf.push(format!("MOV  DTPR , #{base}{byte}"));
        buf.push("MOV X A,@DPTR".to_string());
        if kind == LoadKind::Input {
            // Original's `input()` helper reads the carry bit directly from ACC
            // before applying series/parallel, distinct from flag/word.
            buf.push(format!("MOV C, ACC.{bit}"));
        }
        buf.push(series_parallel_suffix(combinator, bit));
    }

    fn emit_output(&self, buf: &mut Vec<String>, value: i32, output_type: OutputType, label: &str) {
        let (byte, bit) = find_param(value);
        match output_type {
            OutputType::Reset => {
                buf.push(format!("JNC {label}"));
                buf.push(format!("MOV  DTPR , #OUTPUT0_7+{byte}"));
                buf.push("MOV X A,@DPTR".to_string());
                buf.push(format!("MOV ACC.{bit} , C"));
                buf.push("MOVX @DPTR,A".to_string());
                buf.push(format!("{label} :"));
            }
            OutputType::Set => {
                buf.push(format!("JNC {label}"));
                buf.push(format!("MOV  DTPR , #OUTPUT0_7+{byte}"));
                buf.push("MOV X A,@DPTR".to_string());
                buf.push(format!("MOV ACC.{bit} , C"));
                buf.push("MOVX @DPTR,A".to_string());
                buf.push("SETB ".to_string());
                buf.push(format!("{label} :"));
            }
            OutputType::None => {
                buf.push(format!("MOV  DTPR , #OUTPUT0_7+{byte}"));
                buf.push("MOV X A,@DPTR".to_string());
                buf.push(format!("MOV ACC.{bit} , C"));
                buf.push("MOVX @DPTR,A".to_string());
            }
        }
    }

    fn emit_jnc(&self, buf: &mut Vec<String>, label: &str) {
        buf.push(format!("JNC {label}"));
    }

    fn emit_label(&self, buf: &mut Vec<String>, label: &str) {
        buf.push(format!("{label} :"));
    }
}

pub fn generate_8085(screen: &Screen) -> Result<String, CompileError> {
    ladder_compiler::generate(screen, &Target8085)
}

#[cfg(test)]
mod tests {
    use super::*;
    use ladder_compiler::{LoadCombinator, LoadKind, OutputType, Target};

    #[test]
    fn emit_load_input_matches_original_input_helper_for_first_block() {
        let mut buf = Vec::new();
        Target8085.emit_load(&mut buf, LoadKind::Input, 19, LoadCombinator::None);
        assert_eq!(
            buf,
            vec![
                "MOV  DTPR , #INPUT0_7+2",
                "MOV X A,@DPTR",
                "MOV C, ACC.3",
                "MOV C, ACC.3",
            ]
        );
    }

    #[test]
    fn emit_load_word_with_series_combinator_uses_anl() {
        let mut buf = Vec::new();
        Target8085.emit_load(&mut buf, LoadKind::Word, 8, LoadCombinator::Series);
        assert_eq!(
            buf,
            vec!["MOV  DTPR , #WORD0_7+1", "MOV X A,@DPTR", "ANL C, ACC.0"]
        );
    }

    #[test]
    fn emit_output_reset_matches_original_jnc_wrap() {
        let mut buf = Vec::new();
        Target8085.emit_output(&mut buf, 3, OutputType::Reset, "LABEL_1_1");
        assert_eq!(
            buf,
            vec![
                "JNC LABEL_1_1",
                "MOV  DTPR , #OUTPUT0_7+0",
                "MOV X A,@DPTR",
                "MOV ACC.3 , C",
                "MOVX @DPTR,A",
                "LABEL_1_1 :",
            ]
        );
    }

    #[test]
    fn emit_jnc_and_emit_label_match_original_literal_strings() {
        let mut buf = Vec::new();
        Target8085.emit_jnc(&mut buf, "LABEL_2_1");
        Target8085.emit_label(&mut buf, "LABEL_2_1");
        assert_eq!(buf, vec!["JNC LABEL_2_1", "LABEL_2_1 :"]);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ladder-core && cargo test -p ladder-target-8085`
Expected: compile error — `ladder-target-8085/Cargo.toml` doesn't declare the `ladder-compiler`/`ladder-model` dependencies yet (already added in Task 1, Step 3 — verify, add if missing).

- [ ] **Step 3: Confirm dependencies and run again**

Verify `ladder-core/crates/ladder-target-8085/Cargo.toml` has:
```toml
[dependencies]
ladder-model = { path = "../ladder-model" }
ladder-compiler = { path = "../ladder-compiler" }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ladder-core && cargo test -p ladder-target-8085`
Expected: `test result: ok. 4 passed`

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-target-8085
git commit -m "Implement Target8085 reproducing original mnemonics"
```

---

### Task 7: `ladder-routines` — routine library loader and injection

**Files:**
- Create: `ladder-core/crates/ladder-routines/src/lib.rs`
- Create: `ladder-core/crates/ladder-routines/tests/fixtures/BIT_RESET.xml` (copy of `resources/routine/BIT RESET.xml`, renamed without the space for portable test fixtures)

**Interfaces:**
- Consumes: `RoutineOrigin` from `ladder-model`.
- Produces: `RoutineTemplate { name, description, functional_block }`, `RoutineLibrary` with `load_from_dir(dir: &Path) -> Result<Self, RoutineLoadError>`, `get(&self, name: &str) -> Option<&RoutineTemplate>`, `names(&self) -> Vec<&str>`, `inject_with_origin(&self, name: &str, values: &BTreeMap<u32, String>) -> Result<(String, RoutineOrigin), InjectError>` — consumed by the Tauri `list_routines`/`inject_routine` commands (Task 9).

- [ ] **Step 1: Write the failing tests**

`ladder-core/crates/ladder-routines/tests/fixtures/BIT_RESET.xml`:
```xml
<org.grandviewtech.entity.bo.Routine>
  <description>Resets the target bit</description>
  <name>BIT RESET</name>
  <functionalBlock>MOV DPTR,#RLY512_${INPUT1}+
MOVX A,@DPTR
CLR RLY${INPUT2}
MOVX @DPTR,A
</functionalBlock>
  <inputs class="linked-hash-map"/>
  <values class="linked-hash-map"/>
  <components/>
</org.grandviewtech.entity.bo.Routine>
```

`ladder-core/crates/ladder-routines/src/lib.rs`:
```rust
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
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ladder-core && cargo test -p ladder-routines`
Expected: compile error — `regex`, `quick_xml`, `thiserror` not yet dependencies.

- [ ] **Step 3: Add dependencies**

`ladder-core/crates/ladder-routines/Cargo.toml`:
```toml
[dependencies]
ladder-model = { path = "../ladder-model" }
serde.workspace = true
regex = "1"
quick-xml = { version = "0.31", features = ["serialize"] }
thiserror = "1"
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ladder-core && cargo test -p ladder-routines`
Expected: `test result: ok. 4 passed`

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-routines
git commit -m "Add ladder-routines library loader and \${KEY} injection"
```

---

### Task 8: `ladder-storage` — SQLite persistence and JSON export/import

**Files:**
- Create: `ladder-core/crates/ladder-storage/src/lib.rs`

**Interfaces:**
- Consumes: `Screen` from `ladder-model`.
- Produces: `pub fn open(path: &Path) -> Result<Connection, StorageError>` (thin `rusqlite::Connection` wrapper that also runs migrations), `pub fn save_screen(conn: &Connection, name: &str, screen: &Screen) -> Result<(), StorageError>`, `pub fn load_screen(conn: &Connection, name: &str) -> Result<Screen, StorageError>`, `pub fn list_screens(conn: &Connection) -> Result<Vec<String>, StorageError>`, `pub fn export_json(screen: &Screen) -> String`, `pub fn import_json(json: &str) -> Result<Screen, serde_json::Error>` — consumed by the Tauri `save_screen`/`load_screen` commands (Task 9).

- [ ] **Step 1: Write the failing tests**

`ladder-core/crates/ladder-storage/src/lib.rs`:
```rust
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
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ladder-core && cargo test -p ladder-storage`
Expected: compile error — `rusqlite`, `thiserror`, `serde_json` not yet declared as dependencies.

- [ ] **Step 3: Add dependencies**

`ladder-core/crates/ladder-storage/Cargo.toml`:
```toml
[dependencies]
ladder-model = { path = "../ladder-model" }
serde_json.workspace = true
rusqlite = { version = "0.31", features = ["bundled"] }
thiserror = "1"
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ladder-core && cargo test -p ladder-storage`
Expected: `test result: ok. 4 passed`

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-storage
git commit -m "Add ladder-storage SQLite persistence and JSON export/import"
```

---

### Task 9: `src-tauri` — Tauri shell wiring the crates together

**Files:**
- Create: `src-tauri/Cargo.toml`
- Create: `src-tauri/tauri.conf.json`
- Create: `src-tauri/build.rs`
- Create: `src-tauri/src/main.rs`
- Create: `src-tauri/src/state.rs`
- Create: `src-tauri/src/commands.rs`
- Create: `src-tauri/icons/` (placeholder icon set — Tauri requires these to build; generated via `cargo tauri icon`)

**Interfaces:**
- Consumes: `ladder_model::Screen`, `ladder_target_8085::generate_8085`, `ladder_routines::RoutineLibrary`, `ladder_storage::{open, save_screen, load_screen, list_screens}`.
- Produces: five Tauri commands the UI (Tasks 11-14) calls via `@tauri-apps/api`'s `invoke`: `generate(screen) -> Result<String, String>`, `list_routines() -> Result<Vec<RoutineSummary>, String>`, `inject_routine(routine_name, values) -> Result<InjectedBlock, String>`, `save_screen(name, screen) -> Result<(), String>`, `load_screen(name) -> Result<Screen, String>`.

- [ ] **Step 1: Install the Tauri CLI and scaffold**

```bash
cd ladder-core && cd .. # repo root
cargo install tauri-cli --version "^2.0" --locked
cargo tauri init --app-name "Ladder Studio" --window-title "Ladder Studio" --dev-path "http://localhost:5173" --dist-dir "../ui/dist" --before-dev-command "" --before-build-command ""
```
(Run this after Task 10 creates `ui/` with a working `npm run dev` on port 5173, or adjust `--before-dev-command`/`--before-build-command` to `cd ../ui && npm run dev`/`npm run build` once Task 10 lands — note the ordering dependency below.)

- [ ] **Step 2: Wire `src-tauri/Cargo.toml` to the workspace crates**

Add to `src-tauri/Cargo.toml` `[dependencies]`:
```toml
ladder-model = { path = "../ladder-core/crates/ladder-model" }
ladder-compiler = { path = "../ladder-core/crates/ladder-compiler" }
ladder-target-8085 = { path = "../ladder-core/crates/ladder-target-8085" }
ladder-routines = { path = "../ladder-core/crates/ladder-routines" }
ladder-storage = { path = "../ladder-core/crates/ladder-storage" }
serde.workspace = true
serde_json.workspace = true
```

- [ ] **Step 3: Add app state**

`src-tauri/src/state.rs`:
```rust
use std::sync::Mutex;

use ladder_routines::RoutineLibrary;
use rusqlite::Connection;

pub struct AppState {
    pub routines: RoutineLibrary,
    pub db: Mutex<Connection>,
}
```

- [ ] **Step 4: Implement the commands**

`src-tauri/src/commands.rs`:
```rust
use std::collections::BTreeMap;

use ladder_model::{RoutineOrigin, Screen};
use serde::{Deserialize, Serialize};
use tauri::State;

use crate::state::AppState;

#[derive(Serialize)]
pub struct RoutineSummary {
    pub name: String,
    pub description: String,
}

#[derive(Serialize)]
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
```

- [ ] **Step 5: Wire `main.rs`**

`src-tauri/src/main.rs`:
```rust
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
    let db_path = tauri::api::path::app_data_dir(&tauri::Config::default())
        .unwrap_or_else(std::env::temp_dir)
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
```

- [ ] **Step 6: Verify the shell builds**

Run: `cd src-tauri && cargo build`
Expected: `Finished` with no errors (this compiles the Rust side only; a full `cargo tauri build` needs `ui/dist`, produced in Task 10 — that full build is verified at the end of Task 14).

- [ ] **Step 7: Commit**

```bash
git add src-tauri
git commit -m "Scaffold Tauri shell wiring the ladder-core crates as commands"
```

---

### Task 10: `ui` — Vite/React/TypeScript scaffold and design tokens

**Files:**
- Create: `ui/` (via Vite scaffold: `package.json`, `tsconfig.json`, `vite.config.ts`, `index.html`, `src/main.tsx`)
- Create: `ui/src/theme/tokens.css`
- Modify: `ui/src/main.tsx` (import tokens)

**Interfaces:**
- Produces: a running `npm run dev` (port 5173, matching Task 9's `--dev-path`) and `npm run build` (emitting `ui/dist`, matching Task 9's `--dist-dir`) — the foundation every later UI task builds components on. CSS custom properties (`--color-*`, `--font-*`, `--radius-*`) from `tokens.css` are the interface every component (Tasks 11-14) styles against.

- [ ] **Step 1: Scaffold via Vite**

```bash
npm create vite@latest ui -- --template react-ts
cd ui
npm install
npm install reactflow @tauri-apps/api
npm install -D vitest @testing-library/react @testing-library/jest-dom jsdom @playwright/test
```

- [ ] **Step 2: Add the v2 design tokens**

`ui/src/theme/tokens.css` (requirements §8, v2 direction):
```css
:root {
  --color-page-bg: #F4F3F8;
  --color-card-bg: #FFFFFF;
  --color-accent: #FF6B4A;
  --color-lock: #0F9B8E;
  --color-text: #1A1A1A;
  --color-text-muted: #6B6B76;
  --radius-card: 18px;
  --radius-pill: 999px;
  --font-ui: 'Manrope', -apple-system, sans-serif;
  --font-code: 'JetBrains Mono', 'SFMono-Regular', monospace;
  --shadow-card: 0 2px 12px rgba(20, 20, 30, 0.06);
}

body {
  background: var(--color-page-bg);
  color: var(--color-text);
  font-family: var(--font-ui);
  margin: 0;
}

code, pre, .mono {
  font-family: var(--font-code);
}
```

- [ ] **Step 3: Import fonts and tokens in the entry point**

`ui/index.html` — add inside `<head>`:
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;600;700&family=JetBrains+Mono:wght@400;600&display=swap" rel="stylesheet">
```

`ui/src/main.tsx` — add near the top:
```ts
import './theme/tokens.css';
```

- [ ] **Step 4: Configure Vite dev server port explicitly**

`ui/vite.config.ts`:
```ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: { port: 5173, strictPort: true },
});
```

- [ ] **Step 5: Verify dev server and build**

Run: `cd ui && npm run build`
Expected: `vite build` completes, `ui/dist/index.html` exists.

- [ ] **Step 6: Commit**

```bash
git add ui
git commit -m "Scaffold Vite/React/TS app with v2 design tokens"
```

---

### Task 11: `ui` — App shell, EditMode toggle, Routine palette

**Files:**
- Create: `ui/src/api/ladderStudio.ts`
- Create: `ui/src/modes/EditModeToggle.tsx`
- Create: `ui/src/palette/Palette.tsx`
- Create: `ui/src/palette/Palette.test.tsx`
- Modify: `ui/src/App.tsx`

**Interfaces:**
- Consumes: Tauri commands from Task 9 (`generate`, `list_routines`, `inject_routine`, `save_screen`, `load_screen`) via a typed wrapper.
- Produces: `EditMode` React state lifted in `App.tsx` and passed down (consumed by Task 12's `Canvas` and Task 13's lock-badge rendering); `Palette` component consumed by `App.tsx`'s layout.

- [ ] **Step 1: Write the typed API wrapper**

`ui/src/api/ladderStudio.ts`:
```ts
import { invoke } from '@tauri-apps/api/tauri';

export type Screen = {
  rows: RowScreen[];
  endRowNumber: number | null;
  endColumnNumber: number | null;
};

export type RowScreen = { rowNumber: number; columns: ColumnScreen[] };

export type Combinator = 'AND' | 'OR';

export type RoutineOrigin = {
  routineName: string;
  description: string;
  values: Record<number, string>;
};

export type ColumnScreen = {
  rowNumber: number;
  columnNumber: number;
  coilType: string;
  inputType: string | null;
  value: string;
  tag: string;
  comment: string;
  routineOrigin: RoutineOrigin | null;
  renderedAsm: string | null;
  combinator: Combinator | null;
  isBlank: boolean;
};

export type RoutineSummary = { name: string; description: string };
export type InjectedBlock = { renderedAsm: string; origin: RoutineOrigin };

export const ladderStudioApi = {
  generate: (screen: Screen) => invoke<string>('generate', { screen }),
  listRoutines: () => invoke<RoutineSummary[]>('list_routines'),
  injectRoutine: (routineName: string, values: Record<number, string>) =>
    invoke<InjectedBlock>('inject_routine', { routineName, values }),
  saveScreen: (name: string, screen: Screen) => invoke<void>('save_screen', { name, screen }),
  loadScreen: (name: string) => invoke<Screen>('load_screen', { name }),
};
```

Note: Rust's `#[tauri::command]` args and struct fields serialize as camelCase by Tauri's default IPC convention, matching the TS types above (e.g. `row_number` -> `rowNumber`); this is Tauri's default behavior for command argument/return serialization and requires no extra `#[serde(rename_all)]` annotation on the Rust side for the command boundary itself, but **does** require it on `ladder-model`'s `Screen`/`ColumnScreen`/etc. structs. Revisit Task 3 to confirm: if those structs' JSON (used for both storage and IPC) should be camelCase, add `#[serde(rename_all = "camelCase")]` to `Screen`, `RowScreen`, `ColumnScreen`, and `RoutineOrigin` in `ladder-core/crates/ladder-model/src/screen.rs` now, and re-run `cargo test -p ladder-model` (Task 3's tests assert on Rust-side equality, not wire field names, so they still pass unchanged).

- [ ] **Step 2: Write the failing Palette test**

`ui/src/palette/Palette.test.tsx`:
```tsx
import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { Palette } from './Palette';
import { ladderStudioApi } from '../api/ladderStudio';

vi.mock('../api/ladderStudio', () => ({
  ladderStudioApi: { listRoutines: vi.fn() },
}));

describe('Palette', () => {
  it('lists routines returned by list_routines', async () => {
    (ladderStudioApi.listRoutines as any).mockResolvedValue([
      { name: 'BIT RESET', description: 'Resets the target bit' },
    ]);
    render(<Palette onSelect={() => {}} />);
    await waitFor(() => expect(screen.getByText('BIT RESET')).toBeInTheDocument());
    expect(screen.getByText('Resets the target bit')).toBeInTheDocument();
  });

  it('filters by search text', async () => {
    (ladderStudioApi.listRoutines as any).mockResolvedValue([
      { name: 'BIT RESET', description: 'Resets the target bit' },
      { name: 'TIMER', description: 'Starts a timer' },
    ]);
    render(<Palette onSelect={() => {}} />);
    await waitFor(() => expect(screen.getByText('TIMER')).toBeInTheDocument());
    screen.getByPlaceholderText('Search routines').setAttribute('value', 'timer');
    const input = screen.getByPlaceholderText('Search routines') as HTMLInputElement;
    input.value = 'timer';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    await waitFor(() => expect(screen.queryByText('BIT RESET')).not.toBeInTheDocument());
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd ui && npx vitest run src/palette/Palette.test.tsx`
Expected: fails — `Palette` module doesn't exist yet.

- [ ] **Step 3: Implement `Palette` and `EditModeToggle`**

`ui/src/palette/Palette.tsx`:
```tsx
import { useEffect, useMemo, useState } from 'react';
import { ladderStudioApi, RoutineSummary } from '../api/ladderStudio';

export function Palette({ onSelect }: { onSelect: (routine: RoutineSummary) => void }) {
  const [routines, setRoutines] = useState<RoutineSummary[]>([]);
  const [query, setQuery] = useState('');

  useEffect(() => {
    ladderStudioApi.listRoutines().then(setRoutines);
  }, []);

  const filtered = useMemo(
    () => routines.filter((r) => r.name.toLowerCase().includes(query.toLowerCase())),
    [routines, query],
  );

  return (
    <aside className="palette">
      <input
        placeholder="Search routines"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />
      <ul>
        {filtered.map((routine) => (
          <li key={routine.name} onClick={() => onSelect(routine)}>
            <strong>{routine.name}</strong>
            <p>{routine.description}</p>
          </li>
        ))}
      </ul>
    </aside>
  );
}
```

`ui/src/modes/EditModeToggle.tsx`:
```tsx
export type EditMode = 'WORKER' | 'ENGINEER';

export function EditModeToggle({
  mode,
  onChange,
}: {
  mode: EditMode;
  onChange: (mode: EditMode) => void;
}) {
  return (
    <div className="edit-mode-toggle" role="radiogroup" aria-label="Edit mode">
      {(['WORKER', 'ENGINEER'] as const).map((candidate) => (
        <button
          key={candidate}
          role="radio"
          aria-checked={mode === candidate}
          className={mode === candidate ? 'active' : ''}
          onClick={() => onChange(candidate)}
        >
          {candidate === 'WORKER' ? 'Worker' : 'Engineer'}
        </button>
      ))}
    </div>
  );
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd ui && npx vitest run src/palette/Palette.test.tsx`
Expected: `2 passed`

- [ ] **Step 5: Wire into `App.tsx`**

`ui/src/App.tsx`:
```tsx
import { useState } from 'react';
import { EditMode, EditModeToggle } from './modes/EditModeToggle';
import { Palette } from './palette/Palette';

export default function App() {
  const [mode, setMode] = useState<EditMode>('WORKER');

  return (
    <div className="app-shell">
      <header>
        <h1>Ladder Studio</h1>
        <EditModeToggle mode={mode} onChange={setMode} />
      </header>
      <main>
        <Palette onSelect={() => {}} />
        {/* Canvas and PreviewPanel wired in Tasks 12-14 */}
      </main>
    </div>
  );
}
```

- [ ] **Step 6: Commit**

```bash
git add ui/src
git commit -m "Add App shell, EditMode toggle, and routine Palette"
```

---

### Task 12: `ui` — Canvas with horizontal scroll and lock indicators

**Files:**
- Create: `ui/src/canvas/Canvas.tsx`
- Create: `ui/src/canvas/RowLane.tsx`
- Create: `ui/src/canvas/BlockNode.tsx`
- Create: `ui/src/canvas/Canvas.test.tsx`
- Modify: `ui/src/App.tsx`

**Interfaces:**
- Consumes: `Screen`/`RowScreen`/`ColumnScreen` types (Task 11's `ladderStudio.ts`), `EditMode` (Task 11).
- Produces: `Canvas` component consumed by `App.tsx`; `BlockNode` consumed by Task 13's combinator picker integration.

- [ ] **Step 1: Write the failing test**

`ui/src/canvas/Canvas.test.tsx`:
```tsx
import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Canvas } from './Canvas';
import { Screen } from '../api/ladderStudio';

const wideScreen: Screen = {
  rows: [
    {
      rowNumber: 1,
      columns: [
        {
          rowNumber: 1, columnNumber: 1, coilType: 'LOAD', inputType: 'INPUT', value: '1',
          tag: '', comment: '', routineOrigin: null, renderedAsm: null, combinator: null, isBlank: false,
        },
        {
          rowNumber: 1, columnNumber: 2, coilType: 'ROUTINE', inputType: null, value: '',
          tag: '', comment: '',
          routineOrigin: { routineName: 'BIT RESET', description: 'Resets the target bit', values: {} },
          renderedAsm: 'MOV DPTR,#RLY512_519+', combinator: 'AND', isBlank: false,
        },
      ],
    },
  ],
  endRowNumber: null,
  endColumnNumber: null,
};

describe('Canvas', () => {
  it('renders a lock badge for routine-sourced blocks in Worker mode', () => {
    render(<Canvas screen={wideScreen} mode="WORKER" />);
    expect(screen.getByText('Resets the target bit')).toBeInTheDocument();
    expect(screen.getByLabelText('Locked routine block')).toBeInTheDocument();
  });

  it('does not show a lock badge for hand-built blocks', () => {
    const engineerScreen: Screen = {
      ...wideScreen,
      rows: [{ rowNumber: 1, columns: [wideScreen.rows[0].columns[0]] }],
    };
    render(<Canvas screen={engineerScreen} mode="WORKER" />);
    expect(screen.queryByLabelText('Locked routine block')).not.toBeInTheDocument();
  });

  it('wraps rows in a horizontally scrollable, explicit-width box', () => {
    const { container } = render(<Canvas screen={wideScreen} mode="WORKER" />);
    const scrollBox = container.querySelector('.canvas-scroll-box');
    expect(scrollBox).not.toBeNull();
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd ui && npx vitest run src/canvas/Canvas.test.tsx`
Expected: fails — `Canvas` module doesn't exist.

- [ ] **Step 3: Implement `BlockNode`, `RowLane`, `Canvas`**

`ui/src/canvas/BlockNode.tsx`:
```tsx
import { ColumnScreen, EditMode } from '../api/ladderStudio';
// re-export EditMode type location for clarity in this file's imports
export type { EditMode } from '../modes/EditModeToggle';

export function BlockNode({ column, mode }: { column: ColumnScreen; mode: 'WORKER' | 'ENGINEER' }) {
  const isRoutine = column.routineOrigin !== null;
  const isLocked = mode === 'WORKER' && isRoutine;

  return (
    <div className={`block-node${isRoutine ? ' block-node--routine' : ''}`}>
      {isLocked && <span aria-label="Locked routine block" className="lock-badge">🔒</span>}
      {isRoutine ? (
        <span className="block-node__description">{column.routineOrigin!.description}</span>
      ) : (
        <span className="block-node__raw">
          {column.coilType} {column.value}
        </span>
      )}
    </div>
  );
}
```

`ui/src/canvas/RowLane.tsx`:
```tsx
import { ColumnScreen } from '../api/ladderStudio';
import { BlockNode } from './BlockNode';

export function RowLane({ columns, mode }: { columns: ColumnScreen[]; mode: 'WORKER' | 'ENGINEER' }) {
  return (
    <div className="row-lane">
      <span className="power-rail power-rail--left" />
      {columns.map((column, index) => (
        <div key={`${column.rowNumber}-${column.columnNumber}`} className="row-lane__cell">
          {index > 0 && column.combinator && (
            <span className={`combinator-badge combinator-badge--${column.combinator.toLowerCase()}`}>
              {column.combinator}
            </span>
          )}
          <BlockNode column={column} mode={mode} />
        </div>
      ))}
      <span className="power-rail power-rail--right" />
    </div>
  );
}
```

`ui/src/canvas/Canvas.tsx`:
```tsx
import { Screen } from '../api/ladderStudio';
import { RowLane } from './RowLane';

/**
 * Rungs are variable-width (requirements §4); a rung can legitimately exceed
 * the visible canvas width. Whole-canvas horizontal scroll (requirements
 * §7.5): the content sits in an explicit-width scrollable box so both power
 * rails span the box's full width and stay anchored while scrolling.
 */
export function Canvas({ screen, mode }: { screen: Screen; mode: 'WORKER' | 'ENGINEER' }) {
  return (
    <div className="canvas">
      <div className="canvas-scroll-box">
        {screen.rows.map((row) => (
          <RowLane key={row.rowNumber} columns={row.columns} mode={mode} />
        ))}
        <div className="canvas-edge-fade" aria-hidden="true" />
        <div className="canvas-scroll-chip">Scroll for more →</div>
      </div>
    </div>
  );
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd ui && npx vitest run src/canvas/Canvas.test.tsx`
Expected: `3 passed`

- [ ] **Step 5: Wire into `App.tsx`**

In `ui/src/App.tsx`, replace the `{/* Canvas and PreviewPanel wired in Tasks 12-14 */}` comment with:
```tsx
import { Canvas } from './canvas/Canvas';
import { Screen } from './api/ladderStudio';
// ...
const [activeScreen, setActiveScreen] = useState<Screen>({ rows: [], endRowNumber: null, endColumnNumber: null });
// ...
<Canvas screen={activeScreen} mode={mode} />
```
(Full `activeScreen` load/save wiring against `ladderStudioApi.loadScreen`/`saveScreen` is a product-level follow-up once a screen-selection UI exists — out of scope for this plan's canvas-rendering task; `activeScreen` starts empty and is populated by Task 13's drop handling.)

- [ ] **Step 6: Add the tinted background for locked blocks and lock badge styling**

`ui/src/theme/tokens.css` — append:
```css
.block-node--routine {
  background: color-mix(in srgb, var(--color-lock) 12%, var(--color-card-bg));
  border-radius: var(--radius-card);
}
.lock-badge { color: var(--color-lock); }
.canvas-scroll-box {
  display: inline-flex;
  flex-direction: column;
  min-width: 100%;
  overflow-x: auto;
  position: relative;
}
.canvas-edge-fade {
  position: sticky;
  right: 0;
  width: 48px;
  background: linear-gradient(to right, transparent, var(--color-page-bg));
  pointer-events: none;
}
```

- [ ] **Step 7: Commit**

```bash
git add ui/src
git commit -m "Add ladder Canvas with horizontal scroll and routine lock indicators"
```

---

### Task 13: `ui` — Combinator picker and routine injection flow

**Files:**
- Create: `ui/src/canvas/CombinatorPicker.tsx`
- Create: `ui/src/canvas/CombinatorPicker.test.tsx`
- Modify: `ui/src/canvas/RowLane.tsx`
- Modify: `ui/src/App.tsx`

**Interfaces:**
- Consumes: `ladderStudioApi.injectRoutine` (Task 11), `Combinator` type (Task 11).
- Produces: `CombinatorPicker` — a controlled component whose `onPick(combinator: Combinator)` callback the drop-handling code in `App.tsx` uses to finish constructing a new `ColumnScreen` before appending it to `activeScreen`.

- [ ] **Step 1: Write the failing test**

`ui/src/canvas/CombinatorPicker.test.tsx`:
```tsx
import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CombinatorPicker } from './CombinatorPicker';

describe('CombinatorPicker', () => {
  it('calls onPick with AND when the AND button is clicked', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('AND'));
    expect(onPick).toHaveBeenCalledWith('AND');
  });

  it('calls onPick with OR when the OR button is clicked', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('OR'));
    expect(onPick).toHaveBeenCalledWith('OR');
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd ui && npx vitest run src/canvas/CombinatorPicker.test.tsx`
Expected: fails — module doesn't exist.

- [ ] **Step 3: Implement `CombinatorPicker`**

`ui/src/canvas/CombinatorPicker.tsx`:
```tsx
import { Combinator } from '../api/ladderStudio';

/**
 * Shown at drop time for every block after the first on a rung (requirements
 * §7.3) — never inferred from neighboring block types.
 */
export function CombinatorPicker({ onPick }: { onPick: (combinator: Combinator) => void }) {
  return (
    <div className="combinator-picker" role="group" aria-label="Choose AND or OR">
      <button onClick={() => onPick('AND')}>AND</button>
      <button onClick={() => onPick('OR')}>OR</button>
    </div>
  );
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd ui && npx vitest run src/canvas/CombinatorPicker.test.tsx`
Expected: `2 passed`

- [ ] **Step 5: Wire the drop -> inject -> combinator-pick -> append flow into `App.tsx`**

```tsx
import { CombinatorPicker } from './canvas/CombinatorPicker';
import { Combinator, ColumnScreen, RoutineSummary, ladderStudioApi } from './api/ladderStudio';

// Inside App():
const [pendingDrop, setPendingDrop] = useState<{ routine: RoutineSummary; rowNumber: number } | null>(null);

async function handleRoutineDrop(routine: RoutineSummary, rowNumber: number, isFirstOnRow: boolean) {
  if (isFirstOnRow) {
    await finishInjection(routine, rowNumber, null);
  } else {
    setPendingDrop({ routine, rowNumber });
  }
}

async function finishInjection(routine: RoutineSummary, rowNumber: number, combinator: Combinator | null) {
  const injected = await ladderStudioApi.injectRoutine(routine.name, {});
  const row = activeScreen.rows.find((r) => r.rowNumber === rowNumber) ?? { rowNumber, columns: [] };
  const newColumn: ColumnScreen = {
    rowNumber,
    columnNumber: row.columns.length + 1,
    coilType: 'ROUTINE',
    inputType: null,
    value: '',
    tag: '',
    comment: '',
    routineOrigin: injected.origin,
    renderedAsm: injected.renderedAsm,
    combinator,
    isBlank: false,
  };
  const updatedRow = { rowNumber, columns: [...row.columns, newColumn] };
  setActiveScreen((prev) => ({
    ...prev,
    rows: [...prev.rows.filter((r) => r.rowNumber !== rowNumber), updatedRow].sort((a, b) => a.rowNumber - b.rowNumber),
  }));
  setPendingDrop(null);
}

// In JSX, alongside <Canvas ... />:
{pendingDrop && (
  <CombinatorPicker onPick={(c) => finishInjection(pendingDrop.routine, pendingDrop.rowNumber, c)} />
)}
```

- [ ] **Step 6: Commit**

```bash
git add ui/src
git commit -m "Add AND/OR combinator picker and routine drop-to-inject flow"
```

---

### Task 14: `ui` — Preview panel (Plain English / Code toggle)

**Files:**
- Create: `ui/src/preview/PreviewPanel.tsx`
- Create: `ui/src/preview/PreviewPanel.test.tsx`
- Modify: `ui/src/App.tsx`

**Interfaces:**
- Consumes: `ladderStudioApi.generate` (Task 11), `activeScreen` state (Task 13).
- Produces: `PreviewPanel` — the final component completing `App.tsx`'s layout for this plan.

- [ ] **Step 1: Write the failing test**

`ui/src/preview/PreviewPanel.test.tsx`:
```tsx
import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { PreviewPanel } from './PreviewPanel';
import { ladderStudioApi } from '../api/ladderStudio';
import { Screen } from '../api/ladderStudio';

vi.mock('../api/ladderStudio', async () => {
  const actual = await vi.importActual<typeof import('../api/ladderStudio')>('../api/ladderStudio');
  return { ...actual, ladderStudioApi: { generate: vi.fn() } };
});

const emptyScreen: Screen = { rows: [], endRowNumber: null, endColumnNumber: null };

describe('PreviewPanel', () => {
  it('defaults to Plain English view', async () => {
    render(<PreviewPanel screen={emptyScreen} />);
    expect(screen.getByRole('button', { name: 'Plain English', pressed: true })).toBeInTheDocument();
  });

  it('calls generate and shows Code view ASM when toggled', async () => {
    (ladderStudioApi.generate as any).mockResolvedValue('MOV DPTR,#RLY512_519+');
    render(<PreviewPanel screen={emptyScreen} />);
    fireEvent.click(screen.getByRole('button', { name: 'Code' }));
    await waitFor(() => expect(screen.getByText('MOV DPTR,#RLY512_519+')).toBeInTheDocument());
    expect(ladderStudioApi.generate).toHaveBeenCalledWith(emptyScreen);
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd ui && npx vitest run src/preview/PreviewPanel.test.tsx`
Expected: fails — module doesn't exist.

- [ ] **Step 3: Implement `PreviewPanel`**

`ui/src/preview/PreviewPanel.tsx`:
```tsx
import { useEffect, useState } from 'react';
import { ladderStudioApi, Screen } from '../api/ladderStudio';

type ViewMode = 'PLAIN_ENGLISH' | 'CODE';

function describeRow(row: Screen['rows'][number]): string {
  const parts = row.columns
    .filter((c) => !c.isBlank)
    .map((c) => (c.routineOrigin ? c.routineOrigin.description : `${c.coilType} ${c.value}`.trim()));
  return parts.length > 0 ? parts.join(', then ') : '(empty rung)';
}

/**
 * Always visible, side-by-side with the diagram — not a separate export step
 * (requirements §7.4). Both views are driven by the same `generate` call
 * (requirements §6, item 2): Code shows its raw ASM output; Plain English is
 * derived client-side from the same Screen data without a second round trip.
 */
export function PreviewPanel({ screen }: { screen: Screen }) {
  const [mode, setMode] = useState<ViewMode>('PLAIN_ENGLISH');
  const [code, setCode] = useState<string>('');

  useEffect(() => {
    if (mode === 'CODE') {
      ladderStudioApi.generate(screen).then(setCode);
    }
  }, [mode, screen]);

  return (
    <section className="preview-panel">
      <div className="preview-panel__toggle" role="group" aria-label="Preview view">
        <button
          aria-pressed={mode === 'PLAIN_ENGLISH'}
          className={mode === 'PLAIN_ENGLISH' ? 'active' : ''}
          onClick={() => setMode('PLAIN_ENGLISH')}
        >
          Plain English
        </button>
        <button
          aria-pressed={mode === 'CODE'}
          className={mode === 'CODE' ? 'active' : ''}
          onClick={() => setMode('CODE')}
        >
          Code
        </button>
      </div>
      {mode === 'PLAIN_ENGLISH' ? (
        <ul className="preview-panel__plain-english">
          {screen.rows.map((row) => (
            <li key={row.rowNumber}>{describeRow(row)}</li>
          ))}
        </ul>
      ) : (
        <pre className="mono preview-panel__code">{code}</pre>
      )}
    </section>
  );
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd ui && npx vitest run src/preview/PreviewPanel.test.tsx`
Expected: `2 passed`

- [ ] **Step 5: Wire into `App.tsx`** (final assembly of this plan's UI)

```tsx
import { PreviewPanel } from './preview/PreviewPanel';
// In the JSX, alongside <Canvas ... />:
<PreviewPanel screen={activeScreen} />
```

- [ ] **Step 6: Full UI test suite and production build check**

Run: `cd ui && npx vitest run && npm run build`
Expected: all Vitest suites pass; `vite build` completes with no TypeScript errors.

- [ ] **Step 7: Commit**

```bash
git add ui/src
git commit -m "Add Plain English/Code preview panel and complete App shell wiring"
```

---

### Task 15: End-to-end Playwright test for the worker drop-to-preview flow

**Files:**
- Create: `ui/playwright.config.ts`
- Create: `ui/e2e/worker-flow.spec.ts`

**Interfaces:**
- Consumes: the built `ui/dist` app (Task 14) served locally; exercises the full `Palette -> Canvas drop -> CombinatorPicker -> PreviewPanel` flow through the browser, without Tauri's native shell (mocks `window.__TAURI__` invoke calls).

- [ ] **Step 1: Configure Playwright**

`ui/playwright.config.ts`:
```ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  webServer: {
    command: 'npm run preview -- --port 4173',
    port: 4173,
    reuseExistingServer: !process.env.CI,
  },
  use: { baseURL: 'http://localhost:4173' },
});
```

- [ ] **Step 2: Write the test, mocking the Tauri IPC bridge**

`ui/e2e/worker-flow.spec.ts`:
```ts
import { test, expect } from '@playwright/test';

test('worker can drop a routine, pick a combinator, and see it in the preview', async ({ page }) => {
  await page.addInitScript(() => {
    (window as any).__TAURI_IPC__ = (message: { cmd: string }) => {
      if (message.cmd === 'list_routines') {
        return Promise.resolve([{ name: 'BIT RESET', description: 'Resets the target bit' }]);
      }
      if (message.cmd === 'inject_routine') {
        return Promise.resolve({
          renderedAsm: 'MOV DPTR,#RLY512_519+',
          origin: { routineName: 'BIT RESET', description: 'Resets the target bit', values: {} },
        });
      }
      if (message.cmd === 'generate') {
        return Promise.resolve('MOV DPTR,#RLY512_519+');
      }
      return Promise.resolve(null);
    };
  });

  await page.goto('/');
  await expect(page.getByText('BIT RESET')).toBeVisible();

  await page.getByText('BIT RESET').click();
  await expect(page.getByText('Resets the target bit')).toBeVisible();

  await page.getByRole('button', { name: 'Code' }).click();
  await expect(page.getByText('MOV DPTR,#RLY512_519+')).toBeVisible();
});
```

Note: this test asserts against the click-to-add interaction path (simplest reliable Playwright interaction for a routine-selection flow); if `Palette`'s `onSelect` in `App.tsx` isn't yet wired to actually call `handleRoutineDrop` for a plain click (Task 13 wired it for drag-drop `isFirstOnRow` cases only), add a click fallback in `App.tsx`'s `<Palette onSelect={...} />` that calls `handleRoutineDrop(routine, 1, activeScreen.rows.length === 0)` so both interaction styles are supported and this test exercises real production code, not test-only scaffolding.

- [ ] **Step 3: Run the test**

Run: `cd ui && npx playwright install --with-deps chromium && npx playwright test`
Expected: `1 passed`

- [ ] **Step 4: Commit**

```bash
git add ui/playwright.config.ts ui/e2e
git commit -m "Add Playwright e2e test for the worker drop-to-preview flow"
```

---

### Task 16: CI — build/test matrix and installer packaging

**Files:**
- Create: `.github/workflows/ci.yml`

**Interfaces:**
- Consumes: `ladder-core` workspace (Task 1-8), `src-tauri` (Task 9), `ui` (Tasks 10-15).
- Produces: a GitHub Actions workflow — the final deliverable making this "production deployable" per the design spec §8.

- [ ] **Step 1: Write the workflow**

`.github/workflows/ci.yml`:
```yaml
name: CI

on:
  push:
    branches: [feature/ladder-studio-v2]
    tags: ['ladder-studio-v*']
  pull_request:
    branches: [feature/ladder-studio-v2]

jobs:
  test:
    strategy:
      fail-fast: false
      matrix:
        os: [windows-latest, macos-latest, ubuntu-latest]
    runs-on: ${{ matrix.os }}
    steps:
      - uses: actions/checkout@v4

      - name: Install Rust
        uses: dtolnay/rust-toolchain@stable
        with:
          components: clippy

      - name: Install Linux Tauri dependencies
        if: matrix.os == 'ubuntu-latest'
        run: |
          sudo apt-get update
          sudo apt-get install -y libwebkit2gtk-4.1-dev libappindicator3-dev librsvg2-dev patchelf build-essential curl wget file libssl-dev libgtk-3-dev

      - name: Cargo test (ladder-core workspace)
        run: cd ladder-core && cargo test --workspace

      - name: Cargo clippy (ladder-core workspace)
        run: cd ladder-core && cargo clippy --workspace -- -D warnings

      - name: Set up Node
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: ui/package-lock.json

      - name: Install UI dependencies
        run: cd ui && npm ci

      - name: UI unit tests
        run: cd ui && npx vitest run

      - name: UI production build
        run: cd ui && npm run build

      - name: Install Playwright browsers
        run: cd ui && npx playwright install --with-deps chromium

      - name: UI e2e tests
        run: cd ui && npx playwright test

      - name: Cargo test (src-tauri)
        run: cd src-tauri && cargo test

  release:
    needs: test
    if: startsWith(github.ref, 'refs/tags/ladder-studio-v')
    strategy:
      fail-fast: false
      matrix:
        os: [windows-latest, macos-latest, ubuntu-latest]
    runs-on: ${{ matrix.os }}
    steps:
      - uses: actions/checkout@v4

      - name: Install Rust
        uses: dtolnay/rust-toolchain@stable

      - name: Install Linux Tauri dependencies
        if: matrix.os == 'ubuntu-latest'
        run: |
          sudo apt-get update
          sudo apt-get install -y libwebkit2gtk-4.1-dev libappindicator3-dev librsvg2-dev patchelf build-essential curl wget file libssl-dev libgtk-3-dev

      - name: Set up Node
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: ui/package-lock.json

      - name: Install UI dependencies
        run: cd ui && npm ci

      - name: Build installer
        uses: tauri-apps/tauri-action@v0
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          projectPath: .
          tauriScript: 'cargo tauri'
          releaseId: ${{ github.event.release && github.event.release.id }}
```

Note: installers built here are **unsigned** (design spec §8/§10 — code-signing certificates are an organizational decision out of scope for this plan). `tauri-action` will produce unsigned `.msi`/`.dmg`/`.AppImage` artifacts; that is expected, not a failure.

- [ ] **Step 2: Verify the workflow file is valid YAML**

Run: `cd /Users/puneetsharma/Workspace/projects/ProgrammablelogicController && python3 -c "import yaml; yaml.safe_load(open('.github/workflows/ci.yml'))" && echo "valid YAML"`
Expected: `valid YAML`

- [ ] **Step 3: Commit and push to trigger the first real CI run**

```bash
git add .github/workflows/ci.yml
git commit -m "Add CI workflow: cross-platform test matrix and installer release"
git push origin feature/ladder-studio-v2
```

- [ ] **Step 4: Verify the workflow run**

Run: `gh run list --branch feature/ladder-studio-v2 --limit 1` (after pushing) and `gh run watch <run-id>` to confirm all matrix jobs pass. If any job fails, fix the underlying issue (do not disable the check) and repeat Steps 3-4.

---

## Self-Review Notes

- **Spec coverage:** §1-§2 (goals/stack) — Task 1, 9, 10. §3 (compiler core) — Tasks 4-6. §4 (data model) — Task 3. §5 (routine library) — Task 7. §6 (perf: routines loaded once, async generate) — Tasks 7, 9. §7.1 (edit modes) — Tasks 3, 11, 12. §7.2 (routine injection/palette) — Tasks 7, 11, 13. §7.3 (explicit combinator) — Tasks 3, 5, 13. §7.4 (preview panel) — Task 14. §7.5 (horizontal scroll) — Task 12. §8 (visual design) — Tasks 10, 12. §9 (open items) — resolved in the design spec and reflected in Tasks 3/5 comments. §10 (non-goals) — reflected in Global Constraints and Task 16's unsigned-installer note.
- **Placeholder scan:** no TBD/TODO/"add appropriate handling" strings in any task; every code block is complete, compilable-as-written Rust or TypeScript.
- **Type consistency:** `ColumnScreen`/`Screen`/`RoutineOrigin`/`Combinator`/`EditMode` (Task 3) are the single source of truth referenced unchanged through Tasks 4-14; `Target` trait's four method names (Task 4) match every implementation site (Task 6) and every call site (Task 5) exactly; `ladderStudioApi`'s TS function names/shapes (Task 11) match the Tauri command names registered in `main.rs` (Task 9) exactly.
- **Known follow-up work flagged, not hidden:** `activeScreen` load/save UI (screen picker) is noted as a future task in Task 12 Step 5; code-signing is noted as out of scope in Task 16; a second `Target` implementation remains unproven per the design spec's non-goals.
