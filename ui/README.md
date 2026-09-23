# Ladder Studio v2 — frontend

This is the React + TypeScript + Vite frontend for Ladder Studio v2, the ladder-logic
editor that replaces the original Swing application. It runs inside the Tauri shell in
`../src-tauri` and talks to the Rust core (`../ladder-core`) over Tauri IPC — see
`src/api/ladderStudio.ts` for the command surface. It is not meant to be deployed as a
standalone web app.

See [`../docs/BUILDING.md`](../docs/BUILDING.md) for prerequisites and the full dev/release
flow. Within this directory:

```bash
npm ci            # install dependencies
npm run dev       # Vite dev server on :5173 (normally started for you by `tauri dev`)
npm test          # vitest unit tests
npm run test:e2e  # Playwright e2e tests (builds are served from dist/)
npm run build     # type-check and produce dist/, which Tauri bundles
npm run lint      # oxlint
```

## Expanding the Oxlint configuration

For type-aware lint rules, install `oxlint-tsgolint` and set `options.typeAware` in
`.oxlintrc.json`. See the
[Oxlint rules documentation](https://oxc.rs/docs/guide/usage/linter/rules).
