# Building and running Ladder Studio v2

Ladder Studio v2 is a Tauri v2 desktop app: a React/TypeScript frontend (`ui/`) in a Rust
shell (`src-tauri/`) on top of a pure-Rust core (`ladder-core/`, a Cargo workspace holding
the model, compiler, 8085 target, routine library and SQLite storage crates). The 2016 Java
application under `src/` is the original and is not part of this build.

## Prerequisites

- **Rust**, stable toolchain, via [rustup](https://rustup.rs). The `src-tauri` crate sets
  `rust-version = "1.77.2"`, so anything at or above that works.
- **Node.js 20+** and npm.
- **Tauri CLI**, for the dev and bundle commands: `cargo install tauri-cli --version "^2"`.
  CI does not need this — `tauri-action` supplies its own.
- **Linux only**, the usual WebKitGTK build dependencies:
  ```bash
  sudo apt-get install -y libwebkit2gtk-4.1-dev libappindicator3-dev librsvg2-dev \
    patchelf build-essential curl wget file libssl-dev libgtk-3-dev
  ```

## Running in development

One terminal is enough. `tauri.conf.json`'s `beforeDevCommand` starts the Vite dev server
(`npm run dev` in `ui/`, port 5173) for you:

```bash
cd ui && npm ci      # once, to install frontend dependencies
cd ../src-tauri && cargo tauri dev
```

A bare `cargo run` from `src-tauri/` also works if the Vite dev server is already running in
another terminal. Tauri's `build.rs` stages `resources/routine/*.xml` into the target
directory on every `cargo build`/`cargo run`, not just `cargo tauri build`, so this path
still finds the routine library via Tauri's resource resolution — the `../resources/routine`
cwd-relative fallback in `load_routine_library` (`src-tauri/src/main.rs`) only matters if
that staged resource directory is ever missing.

## Tests

```bash
cd ladder-core && cargo test --workspace
cd ladder-core && cargo clippy --workspace -- -D warnings
cd src-tauri  && cargo test          # includes the Rust <-> TS wire-format tests
cd ui && npm test                    # vitest unit tests
cd ui && npm run build && npm run test:e2e   # Playwright, served from ui/dist
```

## Where data lives

- **Screens** are stored in SQLite at `<platform data dir>/ladder-studio/ladder-studio.sqlite`
  (`dirs::data_dir()` in `src-tauri/src/main.rs`):
  `~/Library/Application Support/ladder-studio/` on macOS, `%APPDATA%\ladder-studio\` on
  Windows, and `$XDG_DATA_HOME/ladder-studio/` (usually `~/.local/share/ladder-studio/`) on
  Linux. The directory is created on first launch.
- **Routine templates** are the XML files in `resources/routine/`. They are bundled into the
  installer via `bundle.resources` in `tauri.conf.json` and resolved at startup from the
  app's resource directory. A missing or unparseable routine directory is non-fatal: the app
  logs a warning and starts with an empty routine palette.

## Building an installer locally

```bash
cd src-tauri && cargo tauri build
```

This runs `beforeBuildCommand` (`npm run build` in `ui/`) to produce `ui/dist` first, then
bundles the platform installers under `src-tauri/target/release/bundle/`.

## Cutting a release

`.github/workflows/ci.yml` runs the full test matrix on pushes and PRs to `master` and the
feature branch. Pushing a tag matching `ladder-studio-v*` additionally runs the `release`
job, which builds installers on Windows, macOS and Linux via `tauri-apps/tauri-action` and
attaches them to a **draft** GitHub release named after the tag:

```bash
git tag ladder-studio-v0.1.0
git push origin ladder-studio-v0.1.0
```

Review the draft release on GitHub and publish it when the assets look right.
