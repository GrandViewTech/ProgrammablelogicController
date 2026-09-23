
# ProgrammablelogicController
programmable logic controller
The Above Project Enables you to create a PLC Diagram and Convert it to hex
For Ultra Instruments

 [![Codacy Badge](https://api.codacy.com/project/badge/Grade/15e735be14534c7a85728085dd701cce)](https://www.codacy.com/app/sharmapuneet1510/ProgrammablelogicController?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=GrandViewTech/ProgrammablelogicController&amp;utm_campaign=Badge_Grade)

## Ladder Studio v2

Ladder Studio v2 is the current rewrite of this tool: a Tauri v2 desktop app with a
React/TypeScript frontend and a pure-Rust core, replacing the original Java/Swing
application (which remains under `src/`). It targets shop-floor workers assembling ladder
diagrams from a library of pre-built routines, and still compiles them to 8085 assembly.

| Path | What it is |
| --- | --- |
| `ladder-core/` | Cargo workspace: model, compiler, 8085 target, routine library, SQLite storage |
| `src-tauri/` | Tauri v2 shell and IPC commands |
| `ui/` | React + TypeScript + Vite frontend |
| `resources/routine/` | Routine template XML, bundled into the installer |
| `requirements/v-2.0/` | Requirements for the rewrite |

**Quick start** (needs a stable Rust toolchain, Node 20+ and `cargo install tauri-cli --version "^2"`):

```bash
cd ui && npm ci
cd ../src-tauri && cargo tauri dev
```

Screens are saved to SQLite under your platform data directory
(`~/Library/Application Support/ladder-studio/` on macOS, `%APPDATA%\ladder-studio\` on
Windows, `~/.local/share/ladder-studio/` on Linux).

Installers are built by pushing a `ladder-studio-v*` tag, which triggers the `release` job
in `.github/workflows/ci.yml`.

See **[docs/BUILDING.md](docs/BUILDING.md)** for prerequisites, the test commands, and the
full dev and release flow.
