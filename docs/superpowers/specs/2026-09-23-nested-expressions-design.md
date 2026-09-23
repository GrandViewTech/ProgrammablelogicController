# Nested Expressions, NOT/XOR, and Cross-Row References — Design Spec

**Status:** Approved for implementation planning
**Base:** `feature/ladder-studio-v2` (Ladder Studio v2.0, already shipped)
**Date:** 2026-09-23

## 1. Goals

- Let a worker/engineer combine multiple routines (and raw contacts) into a single
  compact boolean expression per row, using **AND, OR, NOT, and XOR** — not just
  AND/OR as today.
- Let one row's result feed into another row's expression as an input, the way real
  ladder logic already works (a rung's coil writes an internal relay bit; a later
  rung reads that same bit as a contact) — so a worker can compose a "big routine"
  out of smaller, named pieces instead of one flat rung.
- Keep the visual footprint small: a compact, **vertically-growing tree**, extending
  — not replacing — the row/branch rendering already shipped (expand-on-click,
  OR-as-a-branch, Cards/Symbols toggle all continue to work unchanged).
- Everything already built keeps working: nesting, NOT/XOR, and references are
  purely additive. A row that uses none of this behaves exactly as it does today.

## 2. Non-goals

- **No arbitrary dependency graph or topological solver.** Rows still compile
  strictly top-to-bottom, exactly as today and as a real PLC scan does — a row can
  only usefully reference an *earlier* row's output in the same pass. Referencing a
  later row is a compile error, not silently wrong codegen (see §4).
- **No full pan/zoom node-graph canvas** (React Flow or similar). The vertical-tree
  layout is fixed-position CSS/flexbox, matching the cost of everything else built
  so far — confirmed via mockup comparison (see §7).
- **No cycle-detection subsystem.** Since a reference can only point backward
  (strictly-earlier rows), cycles are structurally impossible — the top-to-bottom
  ordering constraint already rules them out.

## 3. Data model (`ladder-model`)

- `Combinator` gains two variants: `Not`, `Xor`, alongside the existing `And`,
  `Or`. Wire form via the existing `SCREAMING_SNAKE_CASE` rename: `"NOT"`, `"XOR"`.
- `ColumnScreen` gains one new optional field: `group: Option<Vec<ColumnScreen>>`.
  - `group: None` — behaves exactly as every existing column does today (a leaf).
  - `group: Some(columns)` — a bracketed sub-expression: `columns` is evaluated the
    same way a row's columns are (recursively), and the group's own `combinator`
    (the same field, on the *outer* column) says how the group's result combines
    with whatever precedes it. `Not` is unary — it complements the group's own
    result and ignores any "previous" running result.
- `ColumnScreen` gains a new coil kind for cross-row references: `CoilType::RowRef`,
  with a new field `row_ref_name: Option<String>` naming the target row's output.
  Looked up **by name**, not by row index, so reordering rows can't silently break
  a reference the way index-based numbering would.
- `RowScreen` gains an optional `output_name: Option<String>` — set when a worker
  names this row's output for other rows to reference. `None` means this row's
  output isn't referenceable (matches every row today).
- All new/changed types keep the existing `#[serde(rename_all = "camelCase")]`
  convention.

## 4. Compiler (`ladder-compiler`, `ladder-target-8085`)

- The row-walk becomes recursive: evaluating a "slot" (a `ColumnScreen`) means
  evaluating its own value/reference if `group` is `None`, or recursively
  evaluating and combining every column in `group` if `Some`, then applying this
  slot's own `combinator` against the running result exactly as today (`Not` is
  unary, as above).
- New `Target` trait methods:
  - `emit_not(&self, buf: &mut Vec<String>)` — appends the 8085 `CMC` (Complement
    Carry) instruction. A real, standard, direct-fit instruction — high confidence.
  - `emit_xor(&self, buf: &mut Vec<String>, ...)` — a short carry-XOR instruction
    sequence, since 8085 has no single "XOR carry" instruction. **The exact
    instruction sequence is not fixed by this spec** — it's worked out and locked
    down with golden-string TDD in the implementation plan, the same way
    `Target8085`'s existing mnemonics were verified (requirements §3's
    string-for-string fidelity bar applies here too, even though this is new
    codegen with no original to match against).
- **Named row outputs write to an internal-relay address** — reusing the same
  `RLY512_+` address space the existing FLAG *read* path already uses (confirmed
  writable on this hardware), via the same read-modify-write idiom `emit_output`
  already uses for physical `OUTPUT0_7+` writes, just against the relay base
  instead. **This mnemonic pattern is inferred by analogy to the existing, working
  patterns and needs a hardware sanity-check before it reaches real PLC hardware**
  — flagged explicitly, the same way this project flags every other assumption it
  isn't 100% certain of, rather than asserting false confidence.
- Address allocation for named row outputs is automatic — the app assigns the next
  free relay address when a worker names a row's output. Workers never see or type
  an address, matching the rest of the app's design (§7.2 of the original
  requirements doc).
- A `RowRef` leaf resolves by looking up the target row's `output_name` at compile
  time; referencing an unknown name, or a row that appears at or after the
  referencing row, is a new `CompileError` variant, not silently wrong codegen.

## 5. UI

- `segments.ts` / `RowLane` / `BlockNode` extend to render nested groups
  recursively as a compact **vertical tree** (the chosen layout — see §7): a
  group's result flows downward into its combinator; siblings that need to stay
  paired (e.g. "Routine n AND Routine 2") stack side by side within their own
  branch, matching the mockup.
- The AND/OR combinator picker (`CombinatorPicker`) gains NOT and XOR as two more
  choices.
- New: naming a row's output (a short text field, shown the first time a row gets
  an output) and a "reference an existing row" picker (a list of named outputs,
  the same interaction pattern as today's routine palette) for building `RowRef`
  leaves.
- Everything shipped in the previous change (expand-on-click, OR-as-a-branch,
  Cards/Symbols toggle) continues to work unchanged for any row that doesn't use
  nesting, NOT/XOR, or references.

## 6. Open items (explicit, not hidden)

- Exact 8085 instruction sequence for XOR — resolved with TDD during
  implementation, not fixed here (§4).
- Exact addressing/mnemonic pattern for writing to an internal relay — inferred by
  analogy to existing OUTPUT codegen; needs a real hardware sanity-check before
  production use (§4).
- Whether the UI should warn (not block) a worker referencing a row that appears
  below the current one — a UX nicety, decided during implementation, not a hard
  requirement of this spec.

## 7. Layout exploration (visual companion)

Three layouts were compared for how a nested expression renders — a wide
node-graph (React Flow style), a compact vertical tree, and inline nested-bracket
chips. **Compact vertical tree was chosen**: it reads like a flowchart, stays
narrow (matching the "should be smaller" goal), and — like the rejected inline
option — needs no pan/zoom graph-canvas library, just CSS flexbox and simple
border-line connectors. The wide node-graph option was rejected as unnecessarily
costly for this app's scale.
