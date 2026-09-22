# Ladder Studio v2.0 — Design Spec

**Status:** Approved for implementation planning
**Source requirements:** `requirements/v-2.0/ladder-studio-requirements.md`
**Base branch:** `origin/ui_changes`
**Feature branch:** `feature/ladder-studio-v2`
**Date:** 2026-09-22

## 1. Scope and intent

This is a ground-up rewrite of the ladder-logic diagram editor (2016 Java 8/Swing/Spring Boot 1.5/XStream, compiling to 8085 hex) onto the stack and product design specified in `requirements/v-2.0/ladder-studio-requirements.md`. The user has chosen to scaffold the **entire** new stack on a single feature branch in one pass — compiler core, storage, routine library, Tauri shell, React UI, tests, CI, and installer packaging — rather than split it into sequential sub-project branches. This spec records the architectural decisions needed to plan that work; it does not restate the product requirements already captured in the source requirements doc, which remains the authority for product behavior.

The old Java/Swing application is left untouched in place (`src/`, `pom.xml`, `resources/`) for reference and rollback; the new stack lives in new top-level directories alongside it.

## 2. Repo / crate layout

```
ladder-core/                  # Rust workspace root (Cargo.toml with [workspace])
  crates/
    ladder-model/              # Screen/RowScreen/ColumnScreen, RoutineOrigin, EditMode; serde JSON
    ladder-compiler/           # ported CompileService traversal + Target trait; depends only on ladder-model
    ladder-target-8085/        # Target8085 impl — string-for-string port of original 8085 mnemonics
    ladder-routines/           # RoutineTemplate loader, ${KEY} substitution, in-memory HashMap cache
    ladder-storage/            # SQLite (rusqlite) persistence + JSON import/export
src-tauri/                     # Tauri app shell: async commands wrapping ladder-* crates, tauri.conf.json
ui/                             # React + TypeScript + Vite
  src/
    canvas/                     # React Flow-based ladder canvas, horizontal-scroll box (§7.5 of requirements)
    palette/                    # searchable routine library palette (§7.2)
    preview/                     # Plain English / Code toggle panel (§7.4)
    modes/                       # Worker/Engineer segmented control + client-side mode mirror
    theme/                       # v2 design tokens (§8): Manrope/JetBrains Mono, coral/teal palette
.github/workflows/               # CI: test matrix + release packaging
docs/superpowers/specs/          # this file
requirements/v-2.0/               # existing — source of truth for product requirements
```

**Rationale:** splitting `ladder-model` / `ladder-compiler` / `ladder-target-8085` mirrors the requirements' pluggable-`Target` design (§3) and lets the ported compiler logic be unit-tested with zero UI/Tauri dependency, which matters because that logic must be preserved *faithfully*, not reinvented.

## 3. Compiler core & data flow

- `ladder-compiler` ports `org.grandviewtech.service.execution.CompileService`'s row/column grid traversal 1:1, plus `CoilType`/`InputType`/`NoNc` enums, SERIES/PARALLEL inference for single-column LOADs, label generation, `JNC`-before-`ROUTINE` jump logic, `END` early-exit, and `findParam` byte/bit splitting — per requirements §3.
- Mnemonic emission is behind a `Target` trait (`emit_load`, `emit_output`, `emit_jnc`, `emit_label`); `ladder-target-8085::Target8085` reproduces original mnemonics string-for-string.
- `ladder-model` types (`Screen`, `RowScreen`, `ColumnScreen`, `RoutineOrigin`, `EditMode`) derive `serde::Serialize`/`Deserialize`. `ColumnScreen::is_locked(mode: EditMode)` is a data-layer method, not UI logic, per requirements §4.
- `generate()` is exposed as an async Tauri command returning `Result<String, CompileError>`, run off the UI thread, driving both the "Test output" action and the always-visible code-preview panel (requirements §6, item 2).

### Resolved open items (previously §9 of the requirements doc — "not yet decided")

These are resolved here as design decisions, not left open, since implementation needs concrete answers:

1. **Routine footprint validation:** no upper bound — rows grow unbounded to fit placed blocks. Consistent with requirements §7.5's rejection of forced rung-splitting.
2. **Locked-block visual strength:** routine-sourced blocks get both a distinct tinted background *and* the existing lock badge, so lock status reads at a glance under time pressure (addresses the original doc's "legible enough... under time pressure" concern).
3. **`ROUTINE` FreeMarker templating:** **no second templating mechanism is built.** The §5 single-pass `${KEY}` substitution (already specified) fully covers routine-library injection; injected ASM is spliced into the row and walked by the existing §3 traversal/JNC/label logic. This closes the item rather than deferring it.
4. **Multi-target validation** remains genuinely deferred (no second `Target` impl in this pass) — the trait's generality is asserted by design, not proven by a second backend, and that's an accepted limitation of this pass.

## 4. Routine library

`ladder-routines` ports `RoutineFileReader` loading behavior and the (originally commented-out) FreeMarker substitution intent from `CompileService.routine()`, replaced by a single-pass `${KEY}` scan (not a general templating engine). Routines load once at app startup into `HashMap<String, RoutineTemplate>`. A missing required input is a `Result::Err` caught before injection. `inject_with_origin()` returns rendered ASM plus a `RoutineOrigin` for the caller to stamp onto resulting blocks.

## 5. Storage

`ladder-storage` uses SQLite via `rusqlite`, replacing loose XStream XML files, and supports JSON export/import of `Screen` data for portability. Schema: screens, rows, columns/blocks (with `RoutineOrigin` as nullable JSON column), routines (cached from the in-memory library, not the source of truth for it).

## 6. UI

- React + TypeScript + Vite; canvas via React Flow.
- Fonts/theme: Manrope (UI) + JetBrains Mono (code/labels), v2 design tokens (light card-based layout, `#F4F3F8` page background, coral `#FF6B4A` accent, teal `#0F9B8E` lock indicator) as CSS custom properties, per requirements §8.
- Worker/Engineer toggle (segmented control in header) drives a client-side mode flag that *mirrors* — never overrides — the `EditMode` enforcement already done at the data layer (§4/§7.1 of requirements).
- Combinator picker (AND/OR) shown at drop time for every block after the first on a rung, per requirements §7.3 — never inferred.
- Preview panel: Plain English (default) / Code toggle, always visible side-by-side with the canvas, per requirements §7.4.
- Horizontal scroll: explicit-width scrollable box, power rails span the box's full width, edge fade + "Scroll for more" chip, per requirements §7.5.

## 7. Testing

- `cargo test` per crate. `ladder-compiler` gets golden-file tests comparing generated ASM against known-good 8085 mnemonics, derived from existing `resources/routine/*.xml` fixtures where usable as reference data.
- `ladder-routines` gets substitution and missing-input-error tests.
- UI: Vitest for component/unit tests; Playwright for the worker-mode drag/drop flow and the AND/OR combinator picker interaction.

## 8. CI/CD and deployment

- GitHub Actions matrix: `windows-latest`, `macos-latest`, `ubuntu-latest`.
- Pipeline: `cargo test` (workspace) + `npm test` (ui/) on every push/PR; on tag push, `tauri-action` builds platform installers (.msi / .dmg / .AppImage) and attaches them as release artifacts.
- Installers are **unsigned** in this pass — code-signing certificates are an organizational/procurement decision out of scope for this spec, tracked as a known follow-up, not a blocker for "deployable."

## 9. Toolchain prerequisite

No Rust toolchain (`cargo`/`rustc`) is installed on the development machine as of this spec. Installing `rustup` (stable toolchain) is a prerequisite step in the implementation plan, before any `ladder-core` crate work can be built or tested.

## 10. Non-goals for this pass (explicit)

- Code signing / notarization of installers.
- A second `Target` implementation beyond `Target8085`.
- Any change to the old Java/Swing app under `src/` — it is left in place, untouched, for reference/rollback.
- Auto-update mechanism for the Tauri app.
