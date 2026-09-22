# Ladder Studio — Requirements Document

**Origin:** Modernization of [GrandViewTech/ProgrammablelogicController](https://github.com/GrandViewTech/ProgrammablelogicController) — a 2016 Java/Swing ladder-logic diagram editor that compiled to 8085 hex for "Ultra Instruments" PLC hardware. This document captures the target architecture and product decisions for the rebuild.

---

## 1. Goals

- Replace the old technology stack (Java 8, Swing, Spring Boot 1.5, XStream) end to end.
- **Preserve the compiled-output logic** of the original where it worked, ported faithfully rather than reinvented.
- **Fix, rather than port, the parts of the original that were unfinished** (see §7).
- Make the tool usable by a shop-floor **worker** with no programming background, not just an engineer.
- Solve two concrete performance problems from the original: slow screen load, and the UI freezing during export.

---

## 2. Technology stack

| Layer | Choice | Why |
|---|---|---|
| Compiler core | **Rust** | Pluggable codegen via a `Target` trait; native performance for compile/export; no JVM dependency |
| Application shell | **Tauri** | Small binary vs. Electron; Rust core runs natively, UI is a webview |
| UI | **React / TypeScript**, canvas via React Flow or Konva.js | Drag-and-drop ladder canvas; replaces Swing screens |
| Data format | **JSON** | Replaces XStream/XML; portable, diffable, no reflection-based deserialization cost |
| Storage | **SQLite** | Replaces loose XML files on disk; supports export/import |
| Search (routine/library lookup) | In-memory `HashMap`, loaded once | Old app used Lucene; not needed at this scale — see §5 |

---

## 3. Compiler core (ported logic)

Ported 1:1 from `org.grandviewtech.service.execution.CompileService` and related entities. This is the part of the original that worked and must **not** change behavior:

- Row/column grid traversal (`Screen` → `RowScreen` → `ColumnScreen`)
- `CoilType`, `InputType`, `NoNc` enums, carried over unchanged
- SERIES/PARALLEL inference logic *for single-column LOADs* (superseded for multi-block rungs — see §7)
- Label generation and `JNC`-before-`ROUTINE` jump logic
- `END` coil early-exit behavior
- Byte/bit address splitting (`findParam`)

**Pluggable targets:** mnemonic emission was pulled out behind a `Target` trait (`emit_load`, `emit_output`, `emit_jnc`, `emit_label`). `Target8085` reproduces the original 8085 mnemonics string-for-string. Future targets (AVR, PIC, a simulator) implement the same trait without touching the traversal logic.

**Known original gaps, carried forward as documented gaps (not fixed):**
- AND/OR combination logic for a LOAD column was never finished in the original (explicit `// TODO` in source) — this is addressed by the new explicit-combinator design in §7, not a preservation.
- `findOutputType`'s `NoNc` lookup was disconnected (always passed `null`).
- `PreferenceScreen`-backed min-value bounds for FLAG-type inputs aren't ported (no settings store exists yet).

---

## 4. Data model

- `Screen` → `RowScreen` → `ColumnScreen`, JSON-serializable (replaces XStream XML).
- **Block-based placement**, not single-column-only: a block is either a raw contact/coil (engineer-only) or a routine's *footprint* (an anchor position + width, holding one `RoutineOrigin`).
- **Variable-width rows**: rungs grow to fit whatever blocks are placed on them. The compiler traversal is unaffected — it already walks whatever columns exist in a row, in order.
- `RoutineOrigin { routine_name, description, values }` is stamped onto any column(s) produced by injecting a library routine, versus `None` for hand-built columns.
- `EditMode { Worker, Engineer }` — enforced at the **data layer** via `ColumnScreen::is_locked(mode)`, not just hidden in the UI, so a worker-mode client can't be tricked into editing a routine-sourced block.

---

## 5. Routine library

Ports `RoutineFileReader` (file loading) and the commented-out FreeMarker call in `CompileService.routine()` (template substitution).

- Routines load **once** (e.g. app startup) into an in-memory `HashMap<String, RoutineTemplate>` keyed by name — O(1) dropdown and injection lookups. The original re-parsed XStream XML repeatedly; this was a direct cause of the old performance complaints.
- Each routine's body is a raw ASM template, optionally containing `${INPUT1}`-style placeholders (seen in the original `resources/routine/*.xml` files, e.g. `Timer.xml`).
- Substitution is a single-pass `${KEY}` scan, not a general templating engine — sufficient for what the library actually needs, and avoids FreeMarker's parsing overhead.
- Missing a required input is caught as a `Result::Err` before injection, so the UI can flag exactly which field is empty rather than failing at render/compile time.
- `inject_with_origin()` returns both the rendered ASM and a `RoutineOrigin`, which the caller stamps onto the resulting block(s) for locking and preview purposes.

---

## 6. Performance fixes

Two distinct problems in the original, with distinct fixes:

1. **Screen load was slow** — caused by XStream reflection-deserializing ~20 routine XML files per session, plus Swing repainting the entire grid canvas on every interaction (custom `paintComponent` cost). Fixed by: routines loaded once into memory (§5); React/canvas-based rendering instead of full-grid Swing repaint.
2. **Export froze the UI** — `CompileService` printed ASM via blocking `System.out` on the UI thread. Fixed by: `generate()` runs as an async Tauri command off the UI thread; the same call drives both the "Test output" action and the always-visible code preview panel, so there's no separate blocking export step.

---

## 7. Worker-facing product design

### 7.1 Two edit modes
- **Worker**: can only drag routines from the library onto rungs. Cannot edit raw contacts/coils, and cannot edit any block carrying a `RoutineOrigin`.
- **Engineer**: can hand-build raw rungs in addition to using the routine library.
- Toggled via a segmented control in the app header; enforced against the data model itself (§4), not just the UI.

### 7.2 Routine injection
- Left-hand palette lists library routines (searchable), each with a name, icon, and one-line description.
- Dragging a routine onto a rung creates a block, stamped with its `RoutineOrigin`.
- Routine blocks render **collapsed** by default — icon + plain-English description (`RoutineOrigin.description`) — with a chevron to expand into raw mnemonics. This is the "idiot-proof" requirement: a worker sees "Starts conveyor after 5s," not `MOV DPTR,#...`.
- A small lock badge marks routine-sourced blocks as non-editable in Worker mode.

### 7.3 Combining blocks on a rung (explicit combinator — a deliberate fix, not a port)
The original's SERIES/PARALLEL inference (based on "was the previous single column a LOAD") doesn't generalize once blocks can be multi-column routines, and was flagged as unfinished in the original source itself.

**Decision:** every block on a rung *after the first* carries an explicit combinator (`AND` / `OR`), chosen by the worker at drop time via a picker — never inferred from neighboring block types. The compiler reads the combinator directly off the block.

### 7.4 Preview panel
- Always visible, side-by-side with the diagram (not a separate export step).
- Toggle: **Plain English** (default) ↔ **Code**. Plain English lists what each rung does in worker-readable language; Code shows the generated ASM in a monospace panel.

### 7.5 Canvas scaling
- **Vertical**: rungs stack downward; canvas scrolls vertically. No special handling needed — this already falls out of ordinary layout.
- **Horizontal**: since rungs are variable-width (§4), a rung can legitimately exceed the visible canvas width (e.g., several combined routine blocks on one rung).
  - **Decision: whole-canvas horizontal scroll.** The diagram content sits in an explicit-width scrollable box; both power rails span that box's full width (not the viewport), so they stay correctly anchored as the worker scrolls, rather than appearing to float free of the diagram.
  - UX affordances: an edge fade and a "Scroll for more" chip hint that a rung continues off-screen.
  - Rejected alternatives: pan/zoom canvas (adds an interaction worker mode is trying to avoid) and a soft width cap with forced rung-splitting (limits legitimate routine combinations).

---

## 8. Visual design system (UI mockups)

Two rounds of exploration were built as Design-type artboards (not covered in application logic, included here for reference):

- **v1 — industrial panel**: dark graphite chrome, warm off-white ladder canvas, amber accent, IBM Plex Sans/Mono.
- **v2 — current direction**: light, card-based layout. White elevated cards (soft shadow, no hard borders) on a soft neutral (`#F4F3F8`) page background, 18px+ corner radius, pill-shaped buttons/toggles, coral accent (`#FF6B4A`), teal lock indicator (`#0F9B8E`), **Manrope** (UI) + **JetBrains Mono** (code/labels).

Three artboards exist under this system: the default Worker view, the Code-view + AND/OR combinator picker interaction, and the horizontal-scroll behavior on a wide rung.

---

## 9. Open items (not yet decided)

- **Routine footprint validation**: what happens if a worker tries to drop a routine that doesn't fit the remaining space in a rung's row, beyond "the rung grows" — is there any upper bound, or truly unbounded growth?
- **Locked-block visual strength**: whether the current lock-badge treatment on routine blocks is legible enough at a glance for a worker under time pressure, or needs a stronger visual differentiator (e.g. a distinct block background rather than just an icon).
- **`ROUTINE` FreeMarker templating**: the original left this commented out entirely for full routine-block code generation (as opposed to the simple `${KEY}` substitution in §5, which only covers the routine library's own inputs). Not yet designed.
- **Multi-target validation**: no second `Target` implementation exists yet beyond `Target8085`, so the trait's generality is unverified against a second real backend.

---

## 10. Explicit non-goals / deliberate deviations from the original

- SERIES/PARALLEL is no longer inferred — it's an explicit worker choice (§7.3). This is a behavior change, not a preservation, and should be called out as such to anyone comparing output against the old tool.
- Lucene-based search is dropped in favor of an in-memory `HashMap`, since the routine library's scale doesn't warrant a full search index.
- XStream/XML storage is dropped in favor of JSON.
