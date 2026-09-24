#!/usr/bin/env bash
# Ladder Studio v2 — one-shot dev environment setup.
#
# Installs everything needed to build and run the app locally and verifies
# it actually works, so you don't have to run each setup command by hand.
#
# Usage: ./scripts/setup-dev-env.sh
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

echo "=== Ladder Studio v2 — dev environment setup ==="
echo "Repo root: $REPO_ROOT"
echo

# --- 1. Rust toolchain -------------------------------------------------
if [ -x "$HOME/.cargo/bin/cargo" ]; then
  echo "--> Rust already installed ($("$HOME/.cargo/bin/rustc" --version))."
else
  echo "--> Installing Rust via rustup..."
  curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --default-toolchain stable
fi

# Make cargo available for the rest of THIS script, regardless of whether
# any shell rc file has been fixed yet.
export PATH="$HOME/.cargo/bin:$PATH"

"$HOME/.cargo/bin/rustup" component add clippy rustfmt >/dev/null 2>&1 || true

# --- 2. Fix shell PATH for future terminals -----------------------------
# rustup's installer appends its sourcing line to ~/.profile, which zsh
# (macOS's default login shell) does NOT read automatically. Without this,
# `cargo` "disappears" in every new terminal even though it's installed.
CARGO_ENV_LINE='. "$HOME/.cargo/env"'
FIXED_RC=""
for RC in "$HOME/.zshrc" "$HOME/.zprofile"; do
  touch "$RC"
  if ! grep -qF "$CARGO_ENV_LINE" "$RC" 2>/dev/null; then
    { echo ""; echo "# Added by Ladder Studio setup-dev-env.sh — make cargo/rustup visible in zsh"; echo "$CARGO_ENV_LINE"; } >> "$RC"
    FIXED_RC="$FIXED_RC $RC"
  fi
done
if [ -n "$FIXED_RC" ]; then
  echo "--> Fixed cargo PATH sourcing in:$FIXED_RC"
else
  echo "--> Shell rc files already source cargo env."
fi

# --- 3. Tauri CLI --------------------------------------------------------
if cargo tauri --version >/dev/null 2>&1; then
  echo "--> tauri-cli already installed ($(cargo tauri --version))."
else
  echo "--> Installing tauri-cli (this compiles from source, a few minutes)..."
  cargo install tauri-cli --version "^2" --locked
fi

# --- 4. Node / npm ---------------------------------------------------------
if ! command -v npm >/dev/null 2>&1; then
  echo "!!! npm not found. Install Node.js 20+ (e.g. via https://nodejs.org or 'brew install node') and re-run this script." >&2
  exit 1
fi
echo "--> Node $(node --version), npm $(npm --version)"

echo "--> Installing UI dependencies..."
(cd "$REPO_ROOT/ui" && npm ci)

# --- 5. Verify everything actually builds ---------------------------------
echo "--> Building the Rust workspace (ladder-core)..."
(cd "$REPO_ROOT/ladder-core" && cargo build --workspace)

echo "--> Building the Tauri shell (src-tauri)..."
(cd "$REPO_ROOT/src-tauri" && cargo build)

echo "--> Building the UI..."
(cd "$REPO_ROOT/ui" && npm run build)

echo
echo "=== Setup complete ==="
echo "Everything is installed and builds cleanly."
echo
echo "Open a NEW terminal so 'cargo' is picked up there too (or run: source ~/.cargo/env in this one)."
echo "Then, to run the app:"
echo "    cd $REPO_ROOT/src-tauri && cargo tauri dev"
echo
echo "To run the full test suite:"
echo "    cd $REPO_ROOT/ladder-core && cargo test --workspace"
echo "    cd $REPO_ROOT/src-tauri  && cargo test"
echo "    cd $REPO_ROOT/ui         && npm test"
