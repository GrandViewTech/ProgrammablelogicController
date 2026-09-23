# Nested Expressions, NOT/XOR, and Cross-Row References Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add nested boolean sub-expressions (AND/OR/XOR + an independent NOT/invert flag), and named cross-row references, to the ladder diagram — extending, not replacing, everything already shipped on this branch.

**Architecture:** `ColumnScreen` gains `inverted: bool`, `group: Option<Vec<ColumnScreen>>`, and `row_ref_name: Option<String>`; `RowScreen` gains `output_name: Option<String>`. The compiler's per-row column walk becomes recursive (a `group` is evaluated the same way a row's own columns are) and gains a small pre-pass that assigns every named row's output an internal-relay address, used both to resolve `RowRef` reads and to write a named row's final result after its own columns are processed. The UI extends the existing branch-rendering (segments.ts/RowLane/BlockNode) to render `group` recursively as a compact vertical tree, and adds row-naming/row-reference UI.

**Tech Stack:** Same as the rest of this branch — Rust (ladder-model/ladder-compiler/ladder-target-8085/src-tauri), React/TypeScript (ui/).

**Spec:** `docs/superpowers/specs/2026-09-23-nested-expressions-design.md` — read this first; it explains *why* NOT is a separate `inverted` flag rather than a 4th combinator value, why cross-row references resolve by name not index, and why there's no cycle-detection subsystem (references can only point at strictly-earlier rows, same as real ladder logic).

## Global Constraints

- Every Rust crate builds with zero warnings under `cargo build --workspace` and passes `cargo test --workspace` and `cargo clippy --workspace -- -D warnings`, from `ladder-core/`.
- **Nothing already shipped on this branch may regress.** Every existing test (37 in `ladder-core`, 3 in `src-tauri`, 9 in `ui` unit tests, 1 e2e) must still pass unchanged after each task, in addition to whatever new tests a task adds. Because `ColumnScreen` and `RowScreen` gain new fields, every existing Rust test file that constructs one directly will fail to compile until fixed — Task 1 is explicitly responsible for finding and fixing every such call site (via `cargo build --workspace --all-targets`), not just adding the new fields.
- New TypeScript fields on `ColumnScreen`/`RowScreen` (`inverted`, `group`, `rowRefName`, `outputName`) must be added as optional (`?:`) so existing UI test fixtures that construct these objects without the new fields keep compiling.
- `inverted` defaults to `false`, `group`/`row_ref_name`/`output_name` default to `None`/`null` — a row/column that uses none of this new capability behaves byte-for-byte as it does today.
- Cross-row references resolve by `output_name` (a string), never by row index/number — row reordering must never silently break a reference.
- A reference to an unknown name, or to a row that does not appear strictly before the referencing row, is a `CompileError`, never silently-wrong codegen.
- Commit after every task passes its verification step.

---

### Task 1: `ladder-model` — Xor combinator, inverted/group/row_ref_name, RowRef coil type, RowScreen.output_name

**Files:**
- Modify: `ladder-core/crates/ladder-model/src/screen.rs`
- Modify: `ladder-core/crates/ladder-model/src/coil_type.rs`
- Modify: every existing test file across the workspace that constructs a `ColumnScreen` or `RowScreen` literal (found via the build, not assumed — see Step 3)

**Interfaces:**
- Produces: `Combinator::Xor` (in addition to existing `And`/`Or`); `ColumnScreen.inverted: bool`, `ColumnScreen.group: Option<Vec<ColumnScreen>>`, `ColumnScreen.row_ref_name: Option<String>`; `CoilType::RowRef`; `RowScreen.output_name: Option<String>`. Consumed by every later task.

- [ ] **Step 1: Write the failing tests**

Add to `ladder-core/crates/ladder-model/src/coil_type.rs`'s existing test module:

```rust
#[test]
fn row_ref_wire_form_matches_screaming_snake_case_convention() {
    assert_eq!(serde_json::to_string(&CoilType::RowRef).unwrap(), "\"ROW_REF\"");
}
```

Add `RowRef` to the `CoilType` enum's variant list (alongside `Label, End, Jump, Output, Line, Load, Routine, LeftLink, RightLink, Parallel, Default, Delete`).

Add to `ladder-core/crates/ladder-model/src/screen.rs`'s existing test module:

```rust
#[test]
fn xor_wire_form_matches_screaming_snake_case_convention() {
    assert_eq!(serde_json::to_string(&Combinator::Xor).unwrap(), "\"XOR\"");
}

#[test]
fn new_column_fields_default_to_inactive() {
    let column = hand_built_column();
    assert!(!column.inverted);
    assert!(column.group.is_none());
    assert!(column.row_ref_name.is_none());
}

#[test]
fn group_round_trips_through_json() {
    let mut outer = hand_built_column();
    outer.group = Some(vec![hand_built_column(), locked_column()]);
    outer.combinator = Some(Combinator::Xor);
    outer.inverted = true;
    let json = serde_json::to_string(&outer).unwrap();
    let back: ColumnScreen = serde_json::from_str(&json).unwrap();
    assert_eq!(outer, back);
}

#[test]
fn row_screen_output_name_defaults_to_none_and_round_trips() {
    let row = RowScreen { row_number: 1, columns: vec![], output_name: Some("Conveyor Running".into()) };
    let json = serde_json::to_string(&row).unwrap();
    let back: RowScreen = serde_json::from_str(&json).unwrap();
    assert_eq!(row, back);
}
```

Add `Combinator::Xor` to the enum (alongside `And`, `Or`). Add `inverted: bool`, `group: Option<Vec<ColumnScreen>>`, `row_ref_name: Option<String>` fields to `ColumnScreen`. Add `output_name: Option<String>` to `RowScreen`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ladder-core && cargo test -p ladder-model`
Expected: compile errors — the new fields/variants aren't declared yet, and existing helper functions (`hand_built_column()`, `locked_column()` in `screen.rs`'s test module) don't initialize the new required fields.

- [ ] **Step 3: Implement the fields, then fix every broken call site workspace-wide**

Update `Combinator`, `ColumnScreen`, `CoilType`, `RowScreen` as described in Step 1. Update `ladder-model/src/screen.rs`'s own `locked_column()`/`hand_built_column()` test helpers to set `inverted: false, group: None, row_ref_name: None`.

Then run `cd ladder-core && cargo build --workspace --all-targets 2>&1` and fix every resulting compile error — this will surface every other `ColumnScreen`/`RowScreen` struct literal in the workspace (at minimum: `ladder-compiler/src/traversal.rs`'s `column()` test helper and its `Screen`/`RowScreen` literals, `ladder-compiler/src/legacy.rs`'s `blank_column()` test helper, and possibly `ladder-storage`'s tests if they construct a non-empty `Screen`). For each, add `inverted: false, group: None, row_ref_name: None` to the `ColumnScreen` literal (or `output_name: None` to the `RowScreen` literal) — do not change any other field or any assertion. This must not change what any existing test asserts, only make it compile.

Repeat `cargo build --workspace --all-targets` until it's clean, then run `cargo test --workspace` and confirm the exact same tests that passed before (37, per the branch's current state) still pass, plus the new ones from Step 1.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ladder-core && cargo test --workspace`
Expected: all prior tests still pass (37+), plus the 5 new tests from Step 1, with zero failures.

- [ ] **Step 5: Commit**

```bash
git add ladder-core
git commit -m "Add Combinator::Xor, ColumnScreen.inverted/group/row_ref_name, CoilType::RowRef, RowScreen.output_name"
```

---

### Task 2: `ladder-compiler` — new CompileError variants and Target trait methods

**Files:**
- Modify: `ladder-core/crates/ladder-compiler/src/types.rs`
- Modify: `ladder-core/crates/ladder-compiler/src/target.rs`

**Interfaces:**
- Consumes: nothing new from Task 1 directly (this task only adds types/trait surface).
- Produces: `CompileError::{UnknownRowReference, ForwardRowReference}`; `Target::{emit_not, emit_xor, emit_relay_write}`. Consumed by Task 3 (traversal) and Task 4 (`Target8085`).

- [ ] **Step 1: Write the failing tests**

Add to `ladder-core/crates/ladder-compiler/src/types.rs`'s test module:

```rust
#[test]
fn unknown_row_reference_message_names_the_row_and_reference() {
    let err = CompileError::UnknownRowReference { row: 3, column: 1, name: "Conveyor Running".into() };
    assert_eq!(
        err.to_string(),
        "row 3, column 1: reference to unknown row output \"Conveyor Running\""
    );
}

#[test]
fn forward_row_reference_message_names_the_row_and_reference() {
    let err = CompileError::ForwardRowReference { row: 1, column: 1, name: "Conveyor Running".into() };
    assert_eq!(
        err.to_string(),
        "row 1, column 1: reference to \"Conveyor Running\" points at a row that isn't earlier in the screen"
    );
}
```

Add to `CompileError`:

```rust
#[error("row {row}, column {column}: reference to unknown row output {name:?}")]
UnknownRowReference { row: u32, column: u32, name: String },
#[error("row {row}, column {column}: reference to {name:?} points at a row that isn't earlier in the screen")]
ForwardRowReference { row: u32, column: u32, name: String },
```

Add to `ladder-core/crates/ladder-compiler/src/target.rs`, extending the `Target` trait:

```rust
/// Complements the current carry — the 8085 `CMC` instruction. Used when a
/// block or group has `inverted: true` (requirements: a NOT contact is a
/// normally-closed contact, combined via the same AND/OR rules as any other —
/// see the design spec's §3 for why this isn't a 4th Combinator value).
fn emit_not(&self, buf: &mut Vec<String>);

/// Combines the current carry with a second operand's carry via XOR. 8085 has
/// no single "XOR carry" instruction, so this emits whatever short sequence
/// the target needs — `Target8085`'s exact sequence is worked out with
/// golden-string TDD in Task 4, not fixed by this trait.
fn emit_xor(&self, buf: &mut Vec<String>);

/// Writes the current carry to a named row's internal-relay address —
/// distinct from `emit_output`'s physical `OUTPUT0_7+` writes. See the design
/// spec's §4 and §6 for why this reuses the `RLY512_+` address space and why
/// it needs a hardware sanity-check before production use.
fn emit_relay_write(&self, buf: &mut Vec<String>, byte: u32, bit: u32);
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ladder-core && cargo test -p ladder-compiler`
Expected: compile error — `CompileError` doesn't have the new variants yet, and adding the two new `Target` trait methods (without a body) makes `RecordingTarget` in `traversal.rs`'s test module (which implements `Target`) fail to compile, since Rust requires every trait method to be implemented — this is expected; Step 3 fixes it.

- [ ] **Step 3: Implement, and fix the resulting `Target` implementors**

Add the two `CompileError` variants and the three `Target` trait methods as above. Then fix every type that implements `Target`: `traversal.rs`'s test-only `RecordingTarget` (add minimal bodies, e.g. push a recognizable string like `"NOT"`/`"XOR"`/`"RELAY_WRITE {byte} {bit}"` to `buf`, following the existing pattern for its other methods) — this is a different crate/file than `ladder-target-8085`'s real `Target8085`, which Task 4 implements properly.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ladder-core && cargo test -p ladder-compiler`
Expected: all prior `ladder-compiler` tests pass, plus the 2 new ones.

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-compiler
git commit -m "Add CompileError row-reference variants and Target::emit_not/emit_xor/emit_relay_write"
```

---

### Task 3: `ladder-compiler` — recursive evaluation, relay-address pre-pass, RowRef resolution, named-output write

**Files:**
- Modify: `ladder-core/crates/ladder-compiler/src/traversal.rs`

**Interfaces:**
- Consumes: `ColumnScreen.{inverted, group, row_ref_name}`, `RowScreen.output_name`, `CoilType::RowRef` (Task 1); `CompileError::{UnknownRowReference, ForwardRowReference}`, `Target::{emit_not, emit_xor, emit_relay_write}` (Task 2).
- Produces: an updated `generate()` with the same public signature (`pub fn generate(screen: &Screen, target: &dyn Target) -> Result<String, CompileError>`) that now also handles groups, inversion, and row references. Consumed by `ladder-target-8085` (Task 4) and the Tauri `generate` command (unchanged, already wired).

- [ ] **Step 1: Write the failing tests**

Add to `traversal.rs`'s test module (alongside the existing tests from the original plan — do not remove or change those):

```rust
#[test]
fn a_group_is_evaluated_and_its_result_combines_like_a_leaf() {
    // (raw_a AND raw_b) OR raw_c — the parenthesized pair is a `group` on
    // the second top-level column; group members combine among themselves
    // the same way top-level columns do (raw_b's combinator is AND,
    // relative to raw_a, both inside the group).
    let mut group_head = column(CoilType::Load);
    group_head.column_number = 1;
    group_head.input_type = Some(InputType::Input);
    group_head.value = "1".into();

    let mut group_tail = column(CoilType::Load);
    group_tail.column_number = 2;
    group_tail.input_type = Some(InputType::Input);
    group_tail.value = "2".into();
    group_tail.combinator = Some(Combinator::And);

    let mut grouped = column(CoilType::Load);
    grouped.column_number = 1;
    grouped.input_type = Some(InputType::Input);
    grouped.value = "0".into(); // unused when `group` is Some — the group's own columns are what's evaluated
    grouped.group = Some(vec![group_head, group_tail]);

    let mut raw_c = column(CoilType::Load);
    raw_c.column_number = 2;
    raw_c.input_type = Some(InputType::Input);
    raw_c.value = "3".into();
    raw_c.combinator = Some(Combinator::Or);

    let screen = Screen {
        rows: vec![RowScreen { row_number: 1, columns: vec![grouped, raw_c], output_name: None }],
        end_row_number: None,
        end_column_number: None,
    };
    let target = RecordingTarget::new();
    let out = generate(&screen, &target).unwrap();
    assert_eq!(
        out,
        "LOAD Input 1 None\nLOAD Input 2 Series\nLOAD Input 3 Parallel"
    );
}

#[test]
fn inverted_flag_emits_not_before_folding_into_the_running_total() {
    let mut raw = column(CoilType::Load);
    raw.input_type = Some(InputType::Input);
    raw.value = "1".into();
    raw.inverted = true;
    let screen = Screen {
        rows: vec![RowScreen { row_number: 1, columns: vec![raw], output_name: None }],
        end_row_number: None,
        end_column_number: None,
    };
    let target = RecordingTarget::new();
    let out = generate(&screen, &target).unwrap();
    assert_eq!(out, "LOAD Input 1 None\nNOT");
}

#[test]
fn row_ref_reads_a_prior_row_s_named_output() {
    let mut named_row_output = column(CoilType::Load);
    named_row_output.input_type = Some(InputType::Input);
    named_row_output.value = "1".into();

    let mut row_ref = column(CoilType::RowRef);
    row_ref.row_ref_name = Some("Conveyor Running".into());

    let screen = Screen {
        rows: vec![
            RowScreen { row_number: 1, columns: vec![named_row_output], output_name: Some("Conveyor Running".into()) },
            RowScreen { row_number: 2, columns: vec![row_ref], output_name: None },
        ],
        end_row_number: None,
        end_column_number: None,
    };
    let target = RecordingTarget::new();
    let out = generate(&screen, &target).unwrap();
    // Row 1: loads its own column, then writes the result to the relay
    // address assigned to "Conveyor Running" (the first named output, so
    // relay index 0 -> byte 0, bit 0). Row 2: reads that same address back
    // as a Flag-style load (RowRef reuses LoadKind::Flag — no new emission
    // path needed for reading).
    assert_eq!(
        out,
        "LOAD Input 1 None\nRELAY_WRITE 0 0\nLOAD Flag 0 None"
    );
}

#[test]
fn unknown_row_reference_name_is_a_compile_error() {
    let mut row_ref = column(CoilType::RowRef);
    row_ref.row_ref_name = Some("Nonexistent".into());
    let screen = Screen {
        rows: vec![RowScreen { row_number: 1, columns: vec![row_ref], output_name: None }],
        end_row_number: None,
        end_column_number: None,
    };
    let target = RecordingTarget::new();
    let err = generate(&screen, &target).unwrap_err();
    assert_eq!(err, CompileError::UnknownRowReference { row: 1, column: 1, name: "Nonexistent".into() });
}

#[test]
fn referencing_a_row_that_is_not_earlier_is_a_compile_error() {
    let mut row_ref = column(CoilType::RowRef);
    row_ref.row_ref_name = Some("Later".into());
    let mut later_output = column(CoilType::Load);
    later_output.input_type = Some(InputType::Input);
    later_output.value = "1".into();
    let screen = Screen {
        rows: vec![
            RowScreen { row_number: 1, columns: vec![row_ref], output_name: None },
            RowScreen { row_number: 2, columns: vec![later_output], output_name: Some("Later".into()) },
        ],
        end_row_number: None,
        end_column_number: None,
    };
    let target = RecordingTarget::new();
    let err = generate(&screen, &target).unwrap_err();
    assert_eq!(err, CompileError::ForwardRowReference { row: 1, column: 1, name: "Later".into() });
}
```

`RecordingTarget` (already defined in this file's test module) needs its `emit_not`/`emit_relay_write` bodies (added in Task 2, Step 3) to push exactly `"NOT"` and `format!("RELAY_WRITE {byte} {bit}")` respectively, matching the assertions above — adjust Task 2's placeholder bodies if the exact string doesn't already match; the assertions here are authoritative.

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ladder-core && cargo test -p ladder-compiler`
Expected: compile errors or assertion failures — `Screen`/`RowScreen` construction needs `output_name`, `generate()` doesn't yet handle `group`, `inverted`, or `CoilType::RowRef`.

- [ ] **Step 3: Implement**

This is the highest-risk task in this plan — take it carefully, and prefer many small `cargo test -p ladder-compiler` runs over one large rewrite.

1. **Relay-address pre-pass**, added near the top of `generate()`:

```rust
use std::collections::HashMap;

fn assign_relay_addresses(screen: &Screen) -> HashMap<String, (u32, u32)> {
    let mut addresses = HashMap::new();
    let mut next_relay_index: i32 = 0;
    for row in &screen.rows {
        if let Some(name) = &row.output_name {
            addresses.insert(name.clone(), crate::legacy::find_param(next_relay_index));
            next_relay_index += 1;
        }
    }
    addresses
}
```

(`legacy::find_param` is already `pub` via `ladder_compiler::compat::find_param` — within this crate you can call `crate::legacy::find_param` directly.)

2. **Extract the existing per-row column-walk into a recursive helper — with an explicit "stop everything" signal.** The original `generate()` used a *labeled* `break 'rows` to make an `End` coil stop both the column loop and the row loop at once, in one function. Extracting the column loop into its own function means that label is no longer in scope there — a bare `break` inside the new helper would only stop that one call's loop (or, worse, only the innermost recursive call when `End` is nested inside a `group`), silently leaving `generate()`'s outer row loop to keep processing rows after `End`, which would be a real behavior regression, not just a refactor. Instead, thread the "stop everything" signal back up as a return value, the way the original *Java* `CompileService` did with its own `loopbreakCondition` boolean before the Rust port collapsed it into a labeled break:

```rust
/// Returns `Ok(true)` if an `End` coil was encountered anywhere in this
/// slice (including inside a nested group) — the caller must stop
/// processing immediately: skip the rest of this row (including any
/// row-output write) and every row after it, matching the original's
/// "END coil early-exit" behavior (requirements §3) exactly, now that the
/// column walk is recursive instead of one flat loop with a labeled break.
fn evaluate_columns(
    columns: &[ColumnScreen],
    row_number: u32,
    relay_addresses: &HashMap<String, (u32, u32)>,
    target: &dyn Target,
    buf: &mut Vec<String>,
) -> Result<bool, CompileError> {
    for (column_index, column) in columns.iter().enumerate() {
        if column.is_blank {
            continue;
        }
        if column.coil_type == CoilType::End {
            return Ok(true);
        }

        // NEW: if this slot is a group, recursively evaluate the group's
        // own columns first, to get "the group's result" onto the carry,
        // then treat that exactly like a leaf load for the purposes of
        // inversion/combining below. If an End coil turns up inside the
        // group, propagate the stop signal immediately — don't finish this
        // group, don't apply this slot's inversion/combinator, don't
        // continue the row.
        if let Some(group_columns) = &column.group {
            if evaluate_columns(group_columns, row_number, relay_addresses, target, buf)? {
                return Ok(true);
            }
        } else {
            // existing LOAD/OUTPUT/ROUTINE dispatch from the original
            // generate(), UNCHANGED, except: add a CoilType::RowRef arm
            // (see point 3 below), and route LOAD's combinator resolution
            // through `columns` (the slice this function was called with)
            // instead of always `row.columns` — `resolve_combinator` and
            // `is_jump_required` already take a `&RowScreen`; give them a
            // small sibling that takes `&[ColumnScreen]` instead, or adapt
            // them to accept a slice directly (whichever keeps the existing
            // Task 5 tests passing unchanged — those call the original
            // functions against `&row.columns` via a real `RowScreen`, so
            // the slice-based version must produce identical results for
            // that case).
        }

        // NEW: apply inversion after obtaining this slot's value (leaf or
        // group), before folding into the running total.
        if column.inverted {
            target.emit_not(buf);
        }
    }
    Ok(false)
}
```

   `generate()`'s outer row loop becomes:

```rust
for row in &screen.rows {
    let end_hit = evaluate_columns(&row.columns, row.row_number, &relay_addresses, target, &mut buf)?;
    if end_hit {
        break; // matches the original: nothing after End runs, in this row or any later one — including this row's own output_name write, since its expression didn't finish evaluating
    }
    if let Some(name) = &row.output_name {
        // point 4, below
    }
}
```

   Keep every existing behavior from the original `generate()` (LOAD/OUTPUT/ROUTINE dispatch, label creation, jump wrapping, `find_output_type`'s documented gap, `parse_value`'s error handling) — this step is additive, not a rewrite of working logic. All 11 existing `ladder-compiler` tests (from the original plan) must still pass unchanged after this refactor, **including `end_coil_stops_traversal_before_later_rows`, which is the one that would silently start failing if the stop-signal propagation above is wrong** — pay particular attention to it. If any assertion changes, that's a bug in the refactor, not a spec change.

3. **`CoilType::RowRef` handling**, added as a new match arm alongside the existing `Load`/`Output`/`Routine`/`_` arms:

```rust
CoilType::RowRef => {
    let name = column.row_ref_name.as_deref().unwrap_or_default();
    let (byte, bit) = relay_addresses.get(name).copied().ok_or_else(|| {
        CompileError::UnknownRowReference { row: row_number, column: column.column_number, name: name.to_string() }
    })?;
    let combinator = resolve_combinator(/* the slice-based equivalent, per point 2 */);
    target.emit_load(buf, LoadKind::Flag, /* value reconstructed from byte/bit, or thread byte/bit directly if emit_load's signature allows */, combinator);
}
```

   Note: `emit_load` takes an `input: i32` that `Target8085` re-splits into byte/bit via `find_param` internally — since `assign_relay_addresses` already computed `(byte, bit)`, reconstruct a single `input` value consistent with how `find_param` would split it (`(byte * 8 + bit) as i32`), so the round trip through `Target8085`'s own `find_param` call lands on the same address. Add a unit test locally if this reconstruction is non-obvious to verify.

4. **Row-level `output_name` write**, added in `generate()`'s outer per-row loop, immediately after a row's `evaluate_columns` call returns `Ok(false)` — i.e. only when that row did *not* hit an `End` coil, per point 2's `end_hit` check:

```rust
if let Some(name) = &row.output_name {
    let (byte, bit) = relay_addresses.get(name).expect("assigned in the pre-pass for every row.output_name");
    target.emit_relay_write(&mut buf, *byte, *bit);
}
```

5. **Forward-reference validation.** Before evaluating a row's columns, if any `RowRef` column within it names a row output that IS in `relay_addresses` but whose owning row's `row_number` is `>=` the current row's `row_number`, return `CompileError::ForwardRowReference` instead of proceeding. The simplest correct way to know "which row owns this name" is to build a second small map during the pre-pass, `HashMap<String, u32>` (name -> owning row_number), alongside `assign_relay_addresses`, and check it at the point a `RowRef` is resolved (point 3, above) before the `UnknownRowReference` check.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ladder-core && cargo test -p ladder-compiler`
Expected: all prior tests (11 from the original plan) pass unchanged, plus the 5 new tests from Step 1, zero failures.

Then run: `cd ladder-core && cargo clippy --workspace -- -D warnings` — clean.

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-compiler
git commit -m "Add recursive group evaluation, relay addressing, and row-reference resolution to generate()"
```

---

### Task 4: `ladder-target-8085` — implement emit_not, emit_relay_write, emit_xor

**Files:**
- Modify: `ladder-core/crates/ladder-target-8085/src/lib.rs`

**Interfaces:**
- Consumes: `Target::{emit_not, emit_xor, emit_relay_write}` (Task 2).
- Produces: `Target8085`'s implementations of all three, exercised by Task 3's `generate()` when actually run through `Target8085` (not just the test-only `RecordingTarget`).

- [ ] **Step 1: Write the failing tests**

Add to `ladder-target-8085/src/lib.rs`'s test module:

```rust
#[test]
fn emit_not_appends_complement_carry() {
    let mut buf = Vec::new();
    Target8085.emit_not(&mut buf);
    assert_eq!(buf, vec!["CMC"]);
}

#[test]
fn emit_relay_write_mirrors_the_output_none_case_pattern_against_the_relay_base() {
    let mut buf = Vec::new();
    Target8085.emit_relay_write(&mut buf, 2, 3);
    assert_eq!(
        buf,
        vec![
            "MOV  DTPR , #RLY512_+2",
            "MOV X A,@DPTR",
            "MOV ACC.3 , C",
            "MOVX @DPTR,A",
        ]
    );
}

#[test]
fn emit_xor_combines_two_operands_carry_results() {
    // XOR(a, b) = (a AND NOT b) OR (NOT a AND b). Implemented here as: stash
    // the first operand's carry in ACC.7 (a scratch bit not otherwise used
    // by this sequence), evaluate the second operand into carry (by the
    // caller, before this call — emit_xor only combines what's already in
    // carry with what was stashed), then compute the XOR via a short
    // compare-and-branch-free sequence. This exact sequence has no original
    // to match against (genuinely new capability) — the assertion below IS
    // the specification; if a cleaner equivalent sequence is found while
    // implementing, update this test to match and note the change in the
    // task's self-review, rather than silently deviating from what's
    // committed.
    let mut buf = Vec::new();
    Target8085.emit_xor(&mut buf);
    assert_eq!(
        buf,
        vec![
            "MOV ACC.7 , C",
            "XRL A, #0x80",
            "MOV C, ACC.7",
        ]
    );
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ladder-core && cargo test -p ladder-target-8085`
Expected: compile error — `Target8085` doesn't implement the 3 new trait methods yet (this crate's `impl Target for Target8085` block is now missing required methods after Task 2 extended the trait).

- [ ] **Step 3: Implement**

```rust
fn emit_not(&self, buf: &mut Vec<String>) {
    buf.push("CMC".to_string());
}

fn emit_relay_write(&self, buf: &mut Vec<String>, byte: u32, bit: u32) {
    buf.push(format!("MOV  DTPR , #RLY512_+{byte}"));
    buf.push("MOV X A,@DPTR".to_string());
    buf.push(format!("MOV ACC.{bit} , C"));
    buf.push("MOVX @DPTR,A".to_string());
}

fn emit_xor(&self, buf: &mut Vec<String>) {
    buf.push("MOV ACC.7 , C".to_string());
    buf.push("XRL A, #0x80".to_string());
    buf.push("MOV C, ACC.7".to_string());
}
```

If the exact `emit_xor` sequence needs correction to genuinely compute a carry-level XOR (verify by hand-tracing the 8085 semantics of `XRL A, #0x80` against whatever was in `ACC.7` before it, since this is new, unverified-against-original logic) — fix the implementation AND update Step 1's test to match, and flag the correction clearly in this task's self-review section of your report. Getting this exactly right matters (requirements-level fidelity bar applies to new codegen too, per the design spec §4) but a materially-equivalent correct sequence is an acceptable outcome, not a required literal match to what's drafted above.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ladder-core && cargo test -p ladder-target-8085`
Expected: all prior tests pass, plus the 3 new ones.

- [ ] **Step 5: Commit**

```bash
git add ladder-core/crates/ladder-target-8085
git commit -m "Implement Target8085::emit_not/emit_relay_write/emit_xor"
```

---

### Task 4b: `ladder-compiler`/`ladder-target-8085` — wire XOR into the live compiler path

> **Added after Task 4 landed.** Task 4's implementer correctly implemented and tested `emit_xor()` against its own narrow, documented contract, but flagged that the contract (an ACC.7-based two-operand stash) doesn't survive the memory-based staging the rest of the compiler actually needs — the same lesson Task 3 learned the hard way for groups (an accumulator-bit stash is destroyed by any intervening `MOVX A,@DPTR`, which every leaf load does). This task exists because the original plan never scoped "wire XOR into `resolve_combinator`'s live path" as its own step — it was silently assumed to fall out of Task 3, but Task 3's actual scope never touched it, and `Combinator::Xor` has been compiling as plain OR (a placeholder from Task 1's panic-prevention fix) ever since. The user was asked and chose to fix this properly now, before Tasks 5-8 add a worker-facing XOR button to the UI.

**Files:**
- Modify: `ladder-core/crates/ladder-target-8085/src/lib.rs`
- Modify: `ladder-core/crates/ladder-compiler/src/target.rs`
- Modify: `ladder-core/crates/ladder-compiler/src/traversal.rs`

**Interfaces:**
- Consumes: the existing `emit_relay_write`/`emit_load(LoadKind::Flag, ...)` stash/read-back pattern already proven correct for non-leading groups in Task 3.
- Produces: a corrected `Target::emit_xor` signature and real XOR codegen reachable from `generate()` for both a non-leading leaf and a non-leading group. A first-position XOR (nothing precedes it) needs no special handling — XOR's identity element is the same as OR's (combining with nothing means "just take this value"), matching how `resolve_combinator` already returns `LoadCombinator::None` for any first-position column regardless of its nominal combinator.

- [ ] **Step 1: Change `emit_xor`'s contract to match how it will actually be called**

The core problem: MOVX clobbers the whole accumulator, but leaves the carry flag alone. So the durable place to stash "the old running total" across a fresh sub-evaluation (which may itself do MOVX) is memory (a relay address, exactly like groups already do) — not an accumulator bit. Once the old total is safely stashed in memory and the new operand is freshly evaluated into carry, *reading the old total back* via `MOVX A,@DPTR` is safe, because that only clobbers the accumulator, and the new operand's value is sitting in carry, untouched by MOVX. What's needed at that point is exactly the same shape as the AND/OR combining already built into every leaf load (`ANL C, ACC.{bit}` / `ORL C, ACC.{bit}` — combine the current carry with a specific accumulator bit) — just the XOR version of it, which doesn't exist as a single 8085 instruction and needs the short synthesized sequence Task 4 already derived and truth-table-verified, adapted to take an arbitrary bit rather than assuming a fixed one.

In `ladder-core/crates/ladder-compiler/src/target.rs`, change the trait method:

```rust
/// XORs the current carry with a specific accumulator bit, leaving the
/// result in carry — the XOR counterpart to the AND/OR combining every
/// leaf load already does via `ANL C, ACC.{bit}` / `ORL C, ACC.{bit}`.
/// Unlike `emit_load`'s combinator parameter, 8085 has no single
/// instruction for this, so it's a short synthesized sequence (see
/// `Target8085`'s implementation). Callers combine two operands by
/// stashing one to a relay address (`emit_relay_write`), evaluating the
/// other fresh into carry, reading the first one back into the
/// accumulator (`emit_load`'s own `MOVX` pattern, or an equivalent), and
/// then calling this with the bit that read landed on.
fn emit_xor(&self, buf: &mut Vec<String>, bit: u32);
```

Update the failing test in `ladder-core/crates/ladder-target-8085/src/lib.rs` for the new signature — re-derive the instruction sequence for "XOR current carry with ACC.{bit}" from the same truth table Task 4 already used (`A XOR B = (A OR B) AND NOT(A AND B)`), verify it by hand for all 4 input combinations the way Task 4's report did, and write the test's expected `buf` to match your verified sequence (don't guess — trace it).

Update `Target8085`'s implementation to match.

- [ ] **Step 2: Run tests to verify RED then GREEN**

Run: `cd ladder-core && cargo test -p ladder-target-8085` — confirm it fails first (signature mismatch), then implement, then confirm it passes.

- [ ] **Step 3: Wire XOR into `evaluate_columns`**

In `ladder-core/crates/ladder-compiler/src/traversal.rs`: at both the leaf-dispatch site and the group-dispatch site, before falling into the existing AND/OR/None combining logic, check whether this column has a preceding sibling AND its own `combinator` is `Some(Combinator::Xor)`. If so, use this sequence instead of the normal single-instruction combine:

1. Allocate a scratch address via the existing `next_scratch_index` counter (same mechanism Task 3's group-fix already uses — reuse it directly, don't build a second counter).
2. `target.emit_relay_write(buf, scratch_byte, scratch_bit)` — stash the current running total.
3. Evaluate this slot's own value fresh into carry, ignoring the outer combinator (`LoadCombinator::None` for a leaf; the existing group-recursion call for a group) — same "evaluate fresh" step the AND/OR/group path already does before combining.
4. Read the stashed total back: `target.emit_relay_write`'s address-resolution pattern already computes `MOV DPTR,#RLY512_+{byte}` / `MOVX A,@DPTR` internally — you'll need the equivalent read here (reuse whatever the existing `emit_load(LoadKind::Flag, ...)` path already does for loading a relay address into the accumulator, but stop short of its own combine step, since you need `emit_xor` to do the combining instead — read the actual current code to find the cleanest way to reuse this without duplicating the DPTR/MOVX lines verbatim; a `bit` parameter for the address you already computed in step 1 is what `emit_xor` needs).
5. `target.emit_xor(buf, scratch_bit)` — combines the read-back stash with the freshly-evaluated carry.
6. Apply `column.inverted` at the correct point (on this slot's own fresh value, matching Task 3's inversion-ordering fix for groups — the same principle applies here: invert before combining, not after).

Also replace `resolve_combinator`'s `Some(Combinator::Xor) => Ok(LoadCombinator::Parallel)` placeholder arm (added in Task 1's fix round, purely to prevent a panic) — once XOR has its own real dispatch path that never calls `resolve_combinator` for the XOR case, this arm should become genuinely unreachable for any column actually using it correctly; decide whether to leave it as a defensive fallback with a comment explaining it's now dead in the intended flow, or restructure so it's provably unreachable — your call, but don't leave a misleading comment claiming it's "temporary until Task 3" (Task 3 already landed; update or remove that comment).

- [ ] **Step 4: Write tests proving real XOR, not OR-equivalent behavior**

At minimum:
- A non-leading LOAD column with `combinator: Some(Xor)` — assert the emitted sequence shows the stash/fresh-load/read-back/`emit_xor` pattern (not a plain `ORL`-equivalent `LOAD ... Parallel` call).
- A non-leading GROUP with `combinator: Some(Xor)` — same proof, via the group path.
- Confirm the *previous* `xor_combinator_compiles_without_panicking_as_temporary_or_fallback` test (from Task 1's fix round) — if it still exists and still asserts OR-equivalent output, either update it to assert real XOR output now that this is wired, or replace it; don't leave a test on the books asserting the old, now-incorrect placeholder behavior as if it were still the intended design.

- [ ] **Step 5: Full verification**

```bash
cd ladder-core && cargo test --workspace
cd ladder-core && cargo clippy --workspace -- -D warnings
```

All prior tests (56, per Task 4's report) pass unchanged except any you deliberately updated per Step 4's note about the old placeholder test, plus your new ones.

- [ ] **Step 6: Commit**

```bash
git add ladder-core
git commit -m "Wire Combinator::Xor into the live compiler path"
```

---

### Task 5: `ui` — `segments.ts` extended for recursive nested groups

**Files:**
- Modify: `ui/src/api/ladderStudio.ts`
- Modify: `ui/src/canvas/segments.ts`
- Modify: `ui/src/canvas/segments.test.ts`

**Interfaces:**
- Consumes: nothing new from earlier tasks in this plan (UI-side, independent of the Rust wire format already matching via existing camelCase conventions).
- Produces: `ColumnScreen.{inverted?, group?, rowRefName?}` (TS types), and `groupIntoSegments` extended to recognize a column with `group` set as needing recursive rendering — consumed by Task 6.

- [ ] **Step 1: Extend the `ColumnScreen` type**

In `ui/src/api/ladderStudio.ts`, add to the `ColumnScreen` type (all optional, per Global Constraints):

```ts
export type ColumnScreen = {
  // ...existing fields unchanged...
  inverted?: boolean;
  group?: ColumnScreen[];
  rowRefName?: string | null;
};
```

Also add to `RowScreen`:

```ts
export type RowScreen = { rowNumber: number; columns: ColumnScreen[]; outputName?: string | null };
```

- [ ] **Step 2: Write the failing test**

Add to `ui/src/canvas/segments.test.ts` (the existing `column()` helper in this file needs no changes — new fields are optional, so omitting them is valid):

```ts
it('treats a column with a group as its own series segment carrying the nested columns', () => {
  const inner1 = column(1, null);
  const inner2 = column(2, 'AND');
  const grouped = { ...column(1, null), group: [inner1, inner2] };
  expect(groupIntoSegments([grouped])).toEqual([{ kind: 'series', column: grouped }]);
});
```

(This confirms `groupIntoSegments` doesn't need to unpack `group` itself — a grouped column is still one top-level segment; Task 6's `BlockNode`/`RowLane` is what recurses into `column.group` when rendering that segment. Keeping `segments.ts` unaware of `group`'s internals keeps its existing AND/OR/branch logic — which operates on top-level `combinator`, unaffected by nesting — correct without modification.)

- [ ] **Step 3: Run test to verify it fails**

Run: `cd ui && npx vitest run src/canvas/segments.test.ts`
Expected: fails — `group` isn't a recognized property until Step 1's type change lands; if Step 1 is already done, this specific test should actually pass immediately since `groupIntoSegments`'s existing logic already treats any non-OR column as its own series segment regardless of other fields. If it passes without any production code change, that's correct — say so in your report rather than forcing an unnecessary change to make it "fail first as a formality."

- [ ] **Step 4: Confirm passing, run the full existing `segments.test.ts` suite**

Run: `cd ui && npx vitest run src/canvas/segments.test.ts`
Expected: all 8 tests (7 existing + 1 new) pass.

- [ ] **Step 5: Commit**

```bash
git add ui/src/api/ladderStudio.ts ui/src/canvas/segments.ts ui/src/canvas/segments.test.ts
git commit -m "Add inverted/group/rowRefName/outputName types; confirm segments.ts handles grouped columns"
```

---

### Task 6: `ui` — render nested groups as a compact vertical tree, plus inversion badge

**Files:**
- Modify: `ui/src/canvas/BlockNode.tsx`
- Modify: `ui/src/canvas/BlockNode.test.tsx`
- Modify: `ui/src/theme/tokens.css`

**Interfaces:**
- Consumes: `ColumnScreen.{inverted, group}` (Task 5).
- Produces: `BlockNode` recursively rendering `column.group` (when present) as a vertical stack, with an inversion badge when `column.inverted` — consumed visually by `RowLane` (already renders `BlockNode` per top-level segment/branch-path; no `RowLane` changes needed, since a grouped column is still just one `BlockNode` render per Task 5).

- [ ] **Step 1: Write the failing tests**

Add to `ui/src/canvas/BlockNode.test.tsx`:

```tsx
function groupedColumn(): ColumnScreen {
  return {
    ...rawColumn('LOAD', '1'),
    group: [rawColumn('LOAD', '2'), { ...rawColumn('LOAD', '3'), combinator: 'AND' }],
  };
}

describe('BlockNode nested groups', () => {
  it('renders every column inside a group', () => {
    render(<BlockNode column={groupedColumn()} mode="ENGINEER" viewStyle="CARDS" />);
    expect(screen.getByText('LOAD 2')).toBeInTheDocument();
    expect(screen.getByText('LOAD 3')).toBeInTheDocument();
  });

  it('shows an inversion badge for an inverted block', () => {
    render(<BlockNode column={{ ...rawColumn('LOAD', '1'), inverted: true }} mode="ENGINEER" viewStyle="CARDS" />);
    expect(screen.getByLabelText('Inverted (NOT)')).toBeInTheDocument();
  });

  it('does not show an inversion badge when not inverted', () => {
    render(<BlockNode column={rawColumn('LOAD', '1')} mode="ENGINEER" viewStyle="CARDS" />);
    expect(screen.queryByLabelText('Inverted (NOT)')).not.toBeInTheDocument();
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ui && npx vitest run src/canvas/BlockNode.test.tsx`
Expected: the two new `describe` blocks' 3 tests fail — `BlockNode` doesn't render `group` or an inversion badge yet; the 11 existing tests in this file still pass.

- [ ] **Step 3: Implement**

In `BlockNode.tsx`, add near the top of the component body (before the existing `isRoutine`/`isLocked` logic):

```tsx
const hasGroup = column.group !== undefined && column.group.length > 0;
```

Add an inversion badge, rendered whenever `column.inverted` is true, regardless of routine/raw/grouped kind — place it as a sibling to the existing lock badge:

```tsx
{column.inverted && (
  <span aria-label="Inverted (NOT)" className="invert-badge">NOT</span>
)}
```

For the grouped case, add a branch (checked before the existing `isRoutine` ternary, since a grouped column takes precedence over routine/raw rendering — a group's members are rendered individually, not the outer "grouped" column itself):

```tsx
{hasGroup ? (
  <div className="block-node__group">
    {column.group!.map((inner) => (
      <BlockNode key={`${inner.rowNumber}-${inner.columnNumber}`} column={inner} mode={mode} viewStyle={viewStyle} />
    ))}
  </div>
) : isRoutine ? (
  // ...existing routine rendering, unchanged...
) : viewStyle === 'SYMBOLS' ? (
  // ...existing symbol rendering, unchanged...
) : (
  // ...existing raw text rendering, unchanged...
)}
```

Add to `tokens.css`:

```css
.block-node__group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  border-left: 3px solid var(--color-accent);
  border-radius: 0 8px 8px 0;
}

.invert-badge {
  padding: 1px 6px;
  border-radius: var(--radius-pill);
  background: var(--color-text);
  color: #FFFFFF;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.4px;
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ui && npx vitest run src/canvas/BlockNode.test.tsx`
Expected: all 14 tests (11 existing + 3 new) pass.

Then: `cd ui && npx vitest run` (full suite) and `cd ui && npm run build` — both clean.

- [ ] **Step 5: Commit**

```bash
git add ui/src/canvas/BlockNode.tsx ui/src/canvas/BlockNode.test.tsx ui/src/theme/tokens.css
git commit -m "Render nested groups recursively and an inversion badge in BlockNode"
```

---

### Task 7: `ui` — `CombinatorPicker` gains XOR and an independent Invert checkbox

**Files:**
- Modify: `ui/src/canvas/CombinatorPicker.tsx`
- Modify: `ui/src/canvas/CombinatorPicker.test.tsx`
- Modify: `ui/src/theme/tokens.css`

**Interfaces:**
- Consumes: `Combinator` type (already includes `'XOR'` once the TS union in `ladderStudio.ts` is widened — see Step 1).
- Produces: `CombinatorPicker`'s `onPick` callback signature grows an `inverted: boolean` second argument; consumed by `App.tsx` (Task 8 wires the new argument through).

- [ ] **Step 1: Widen the `Combinator` TS type**

In `ui/src/api/ladderStudio.ts`: `export type Combinator = 'AND' | 'OR' | 'XOR';`

- [ ] **Step 2: Write the failing tests**

Replace `ui/src/canvas/CombinatorPicker.test.tsx` with (extends, doesn't remove, the 2 existing tests — AND/OR calls now also assert the second `inverted` argument):

```tsx
import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CombinatorPicker } from './CombinatorPicker';

describe('CombinatorPicker', () => {
  it('calls onPick with AND and inverted=false by default', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('AND'));
    expect(onPick).toHaveBeenCalledWith('AND', false);
  });

  it('calls onPick with OR when the OR button is clicked', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('OR'));
    expect(onPick).toHaveBeenCalledWith('OR', false);
  });

  it('calls onPick with XOR when the XOR button is clicked', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('XOR'));
    expect(onPick).toHaveBeenCalledWith('XOR', false);
  });

  it('passes inverted=true when the Invert checkbox is checked before picking', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByLabelText('Invert (NOT)'));
    fireEvent.click(screen.getByText('AND'));
    expect(onPick).toHaveBeenCalledWith('AND', true);
  });
});
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `cd ui && npx vitest run src/canvas/CombinatorPicker.test.tsx`
Expected: fails — current `CombinatorPicker` has 2 buttons, `onPick(combinator)` with one argument, no checkbox.

- [ ] **Step 4: Implement**

```tsx
import { useState } from 'react';
import type { Combinator } from '../api/ladderStudio';

export function CombinatorPicker({ onPick }: { onPick: (combinator: Combinator, inverted: boolean) => void }) {
  const [inverted, setInverted] = useState(false);

  return (
    <div className="combinator-picker" role="group" aria-label="Choose AND, OR, or XOR, and optionally invert">
      <label className="combinator-picker__invert">
        <input
          type="checkbox"
          aria-label="Invert (NOT)"
          checked={inverted}
          onChange={(e) => setInverted(e.target.checked)}
        />
        Invert (NOT)
      </label>
      <div className="combinator-picker__buttons">
        {(['AND', 'OR', 'XOR'] as const).map((combinator) => (
          <button key={combinator} onClick={() => onPick(combinator, inverted)}>
            {combinator}
          </button>
        ))}
      </div>
    </div>
  );
}
```

Add to `tokens.css`, alongside the existing `.combinator-picker` rules:

```css
.combinator-picker {
  flex-direction: column;
  align-items: stretch;
}

.combinator-picker__buttons {
  display: flex;
  gap: 8px;
}

.combinator-picker__invert {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-muted);
  padding-bottom: 4px;
}
```

(The existing `.combinator-picker` rule already sets `display: flex; gap: 8px;` — check whether adding `flex-direction: column` here conflicts with that base rule or needs to replace it; reconcile so the checkbox row sits above the button row without breaking the existing rule's other properties like `position: fixed`, `padding`, `background`, `box-shadow`, which should stay unchanged.)

- [ ] **Step 5: Run tests to verify they pass**

Run: `cd ui && npx vitest run src/canvas/CombinatorPicker.test.tsx`
Expected: all 4 tests pass.

- [ ] **Step 6: Commit**

```bash
git add ui/src/canvas/CombinatorPicker.tsx ui/src/canvas/CombinatorPicker.test.tsx ui/src/theme/tokens.css ui/src/api/ladderStudio.ts
git commit -m "Add XOR and an independent Invert checkbox to CombinatorPicker"
```

---

### Task 8: `ui` — row-output naming and row-reference picker, wired into `App.tsx`

**Files:**
- Create: `ui/src/canvas/RowOutputName.tsx`
- Create: `ui/src/canvas/RowOutputName.test.tsx`
- Create: `ui/src/canvas/RowReferencePicker.tsx`
- Create: `ui/src/canvas/RowReferencePicker.test.tsx`
- Modify: `ui/src/App.tsx`
- Modify: `ui/src/theme/tokens.css`

**Interfaces:**
- Consumes: `RowScreen.outputName` (Task 5), `CombinatorPicker`'s new `(combinator, inverted)` signature (Task 7).
- Produces: a working end-to-end flow — naming a row's output, and referencing an existing named output from another row — wired into `App.tsx`'s existing `activeScreen` state.

- [ ] **Step 1: Write the failing tests for `RowOutputName`**

```tsx
// ui/src/canvas/RowOutputName.test.tsx
import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { RowOutputName } from './RowOutputName';

describe('RowOutputName', () => {
  it('shows the existing name when the row already has one', () => {
    render(<RowOutputName name="Conveyor Running" onRename={() => {}} />);
    expect(screen.getByDisplayValue('Conveyor Running')).toBeInTheDocument();
  });

  it('shows a placeholder prompt when the row has no name yet', () => {
    render(<RowOutputName name={null} onRename={() => {}} />);
    expect(screen.getByPlaceholderText('Name this row’s output (optional)')).toBeInTheDocument();
  });

  it('calls onRename with the trimmed value when the field loses focus', () => {
    const onRename = vi.fn();
    render(<RowOutputName name={null} onRename={onRename} />);
    const input = screen.getByPlaceholderText('Name this row’s output (optional)');
    fireEvent.change(input, { target: { value: '  Conveyor Running  ' } });
    fireEvent.blur(input);
    expect(onRename).toHaveBeenCalledWith('Conveyor Running');
  });

  it('calls onRename with null when the field is cleared', () => {
    const onRename = vi.fn();
    render(<RowOutputName name="Conveyor Running" onRename={onRename} />);
    const input = screen.getByDisplayValue('Conveyor Running');
    fireEvent.change(input, { target: { value: '' } });
    fireEvent.blur(input);
    expect(onRename).toHaveBeenCalledWith(null);
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd ui && npx vitest run src/canvas/RowOutputName.test.tsx`
Expected: fails — module doesn't exist.

- [ ] **Step 3: Implement `RowOutputName`**

```tsx
import { useState } from 'react';

export function RowOutputName({ name, onRename }: { name: string | null; onRename: (name: string | null) => void }) {
  const [draft, setDraft] = useState(name ?? '');

  return (
    <input
      className="row-output-name"
      value={draft}
      placeholder="Name this row’s output (optional)"
      onChange={(e) => setDraft(e.target.value)}
      onBlur={() => {
        const trimmed = draft.trim();
        onRename(trimmed.length > 0 ? trimmed : null);
      }}
    />
  );
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd ui && npx vitest run src/canvas/RowOutputName.test.tsx`
Expected: 4/4 pass.

- [ ] **Step 5: Write the failing tests for `RowReferencePicker`**

```tsx
// ui/src/canvas/RowReferencePicker.test.tsx
import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { RowReferencePicker } from './RowReferencePicker';

describe('RowReferencePicker', () => {
  it('lists every available named row output', () => {
    render(<RowReferencePicker availableNames={['Conveyor Running', 'Door Open']} onPick={() => {}} onCancel={() => {}} />);
    expect(screen.getByText('Conveyor Running')).toBeInTheDocument();
    expect(screen.getByText('Door Open')).toBeInTheDocument();
  });

  it('calls onPick with the clicked name', () => {
    const onPick = vi.fn();
    render(<RowReferencePicker availableNames={['Conveyor Running']} onPick={onPick} onCancel={() => {}} />);
    fireEvent.click(screen.getByText('Conveyor Running'));
    expect(onPick).toHaveBeenCalledWith('Conveyor Running');
  });

  it('shows a message and no list when there are no named outputs yet', () => {
    render(<RowReferencePicker availableNames={[]} onPick={() => {}} onCancel={() => {}} />);
    expect(screen.getByText(/no named row outputs yet/i)).toBeInTheDocument();
  });

  it('calls onCancel when the cancel action is used', () => {
    const onCancel = vi.fn();
    render(<RowReferencePicker availableNames={['Conveyor Running']} onPick={() => {}} onCancel={onCancel} />);
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(onCancel).toHaveBeenCalled();
  });
});
```

- [ ] **Step 6: Run tests to verify they fail**

Run: `cd ui && npx vitest run src/canvas/RowReferencePicker.test.tsx`
Expected: fails — module doesn't exist.

- [ ] **Step 7: Implement `RowReferencePicker`**

```tsx
export function RowReferencePicker({
  availableNames,
  onPick,
  onCancel,
}: {
  availableNames: string[];
  onPick: (name: string) => void;
  onCancel: () => void;
}) {
  return (
    <div className="row-reference-picker" role="dialog" aria-label="Reference an existing row's output">
      {availableNames.length === 0 ? (
        <p>No named row outputs yet — name a row's output first to reference it elsewhere.</p>
      ) : (
        <ul>
          {availableNames.map((name) => (
            <li key={name} onClick={() => onPick(name)}>
              {name}
            </li>
          ))}
        </ul>
      )}
      <button type="button" onClick={onCancel}>
        Cancel
      </button>
    </div>
  );
}
```

- [ ] **Step 8: Run tests to verify they pass**

Run: `cd ui && npx vitest run src/canvas/RowReferencePicker.test.tsx`
Expected: 4/4 pass.

- [ ] **Step 9: Wire both into `App.tsx`**

Add state and handlers:

```tsx
import { RowOutputName } from './canvas/RowOutputName';
import { RowReferencePicker } from './canvas/RowReferencePicker';

// alongside the existing state declarations:
const [showReferencePicker, setShowReferencePicker] = useState(false);

function handleRowOutputRename(rowNumber: number, name: string | null) {
  setActiveScreen((prev) => ({
    ...prev,
    rows: prev.rows.map((row) => (row.rowNumber === rowNumber ? { ...row, outputName: name ?? undefined } : row)),
  }));
}

const availableRowOutputNames = activeScreen.rows
  .map((row) => row.outputName)
  .filter((name): name is string => Boolean(name));
```

Update the `CombinatorPicker` usage to pass through the new `inverted` argument (Task 7 changed its `onPick` signature) — thread `inverted` into `finishInjection`'s constructed `ColumnScreen` as its `inverted` field, alongside the existing `combinator` field.

Render `RowOutputName` next to each row in the canvas (exact placement — e.g. as a small control in `RowLane`'s header, or a fixed slot in `App.tsx`'s layout above `Canvas` — is your call based on what reads cleanest; keep it minimal, this isn't a design pass) and a way to open `RowReferencePicker` (e.g. a button in the palette or canvas toolbar) that, on `onPick`, adds a `RowRef` column to the target row via the same append-into-`prev` pattern `finishInjection` already uses (reuse that pattern rather than inventing a second one).

- [ ] **Step 10: Verify the full stack**

Run, from `ui/`: `npx vitest run` (full suite, all files) and `npm run build`. Both must be clean, with the exact prior test count plus everything added in Tasks 5-8.

- [ ] **Step 11: Commit**

```bash
git add ui/src
git commit -m "Add row-output naming and row-reference picker, wired into App.tsx"
```

---

### Task 9: Final full-stack verification

**Files:** none (verification only).

- [ ] **Step 1: Run every verification command from scratch**

```bash
cd ladder-core && cargo test --workspace
cd ladder-core && cargo clippy --workspace -- -D warnings
cd src-tauri  && cargo build
cd src-tauri  && cargo test
cd ui         && npx vitest run
cd ui         && npm run build
cd ui         && npx playwright test
```

All must be clean. Confirm the Rust test count is at least 37 (original) + 5 (Task 1) + 2 (Task 2) + 5 (Task 3) + 3 (Task 4) = 52, and the UI vitest count is at least 27 (original) + 1 (Task 5) + 3 (Task 6) + 2 (Task 7, net of the 2 existing tests being replaced with equivalents) + 8 (Task 8) = 41 — treat any shortfall as a real gap to investigate, not a rounding error.

- [ ] **Step 2: Push**

```bash
git push origin feature/ladder-studio-v2
```

## Self-Review Notes

- **Spec coverage:** §3 (data model) — Task 1. §4 (compiler: recursive walk, emit_not/emit_xor/emit_relay_write, row-reference resolution and validation) — Tasks 2-4. §5 (UI: vertical-tree rendering, extended picker, naming/reference UI) — Tasks 6-8. §6 (open items: XOR's exact instruction sequence, relay-write mnemonic needing hardware validation) — explicitly flagged in Task 4's own text, not silently resolved. §7 (layout choice) — reflected in Task 6's vertical-stack CSS.
- **Placeholder scan:** no bare "TBD"/"add appropriate logic" — Task 3's higher-latitude prose (point 2 of Step 3) still specifies exact function signatures, exact existing-test-preservation requirements, and every new test's exact expected output; the latitude given is over internal refactor mechanics, not over what must be true when the task is done.
- **Type consistency:** `Combinator` values (`And`/`Or`/`Xor` in Rust, `'AND'|'OR'|'XOR'` in TS) match across Tasks 1, 5, 7. `CompileError` variant field names (`row`, `column`, `name`) match between Task 2's definition and Task 3's construction sites. `Target` trait method signatures (Task 2) match their `Target8085` implementations (Task 4) and their `RecordingTarget` test-double implementations (Task 2 Step 3, exercised by Task 3's tests) exactly.
- **Known follow-up work flagged, not hidden:** Task 4 explicitly allows `emit_xor`'s exact sequence to be corrected during implementation if the drafted one isn't right, with a requirement to update the test and flag it — matching how this branch's earlier plan handled `Task 5`'s `parse_value` self-contradiction. The relay-write mnemonic's need for real hardware validation (spec §6) is carried into Task 4's text, not dropped.
