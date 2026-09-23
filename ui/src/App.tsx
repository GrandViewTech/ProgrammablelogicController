import { useEffect, useRef, useState } from 'react';
import { type EditMode, EditModeToggle } from './modes/EditModeToggle';
import { ViewStyleToggle } from './modes/ViewStyleToggle';
import { Palette } from './palette/Palette';
import { Canvas } from './canvas/Canvas';
import type { ViewStyle } from './canvas/BlockNode';
import { CombinatorPicker } from './canvas/CombinatorPicker';
import { RowOutputName } from './canvas/RowOutputName';
import { RowReferencePicker } from './canvas/RowReferencePicker';
import { PreviewPanel } from './preview/PreviewPanel';
import { ladderStudioApi } from './api/ladderStudio';
import type { Combinator, ColumnScreen, RoutineSummary, Screen } from './api/ladderStudio';

function message(error: unknown): string {
  if (typeof error === 'string') return error;
  if (error instanceof Error) return error.message;
  return String(error);
}

// The single append-into-`prev` pattern used by every operation that adds a
// column to a row (routine injection, row-reference insertion, and any
// future one): find the row (or default to an empty one), hand the caller
// the next column number so it can build the new `ColumnScreen`, then
// splice the updated row back in and re-sort. Keeping this as one function
// is what "reuse the pattern, don't invent a second one" means in practice —
// two call sites building the same row/columns/sort logic by hand would
// silently drift apart.
function appendColumn(prev: Screen, rowNumber: number, makeColumn: (columnNumber: number) => ColumnScreen): Screen {
  const row = prev.rows.find((r) => r.rowNumber === rowNumber) ?? { rowNumber, columns: [] };
  const newColumn = makeColumn(row.columns.length + 1);
  const updatedRow = { ...row, columns: [...row.columns, newColumn] };
  return {
    ...prev,
    rows: [...prev.rows.filter((r) => r.rowNumber !== rowNumber), updatedRow].sort((a, b) => a.rowNumber - b.rowNumber),
  };
}

export default function App() {
  const [mode, setMode] = useState<EditMode>('WORKER');
  const [viewStyle, setViewStyle] = useState<ViewStyle>('CARDS');
  const [activeScreen, setActiveScreen] = useState<Screen>({ rows: [], endRowNumber: null, endColumnNumber: null });
  const [pendingDrop, setPendingDrop] = useState<{ routine: RoutineSummary; rowNumber: number } | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [showReferencePicker, setShowReferencePicker] = useState(false);

  // Mirrors `activeScreen` for the synchronous pre-check in
  // `handleRoutineSelect`, which has to decide whether to show the AND/OR
  // picker *before* the async injection runs and so cannot use a state
  // updater. Click handlers always run after effects have flushed, so this is
  // current for the normal one-drop-at-a-time flow; the only residual
  // staleness is two clicks landing while an injection is still in flight,
  // which at worst shows or skips a picker. The actual state mutation in
  // `finishInjection` reads `prev`, so it can never corrupt the screen.
  const activeScreenRef = useRef(activeScreen);
  useEffect(() => {
    activeScreenRef.current = activeScreen;
  }, [activeScreen]);

  async function handleRoutineDrop(routine: RoutineSummary, rowNumber: number, isFirstOnRow: boolean) {
    if (isFirstOnRow) {
      await finishInjection(routine, rowNumber, null, false);
    } else {
      setPendingDrop({ routine, rowNumber });
    }
  }

  async function finishInjection(
    routine: RoutineSummary,
    rowNumber: number,
    combinator: Combinator | null,
    inverted: boolean,
  ) {
    setPendingDrop(null);
    try {
      const injected = await ladderStudioApi.injectRoutine(routine.name, {});
      // Everything derived from the screen is computed inside the updater, from
      // `prev`. Deriving the target row (and therefore the new block's
      // `columnNumber`, which is also its React key) from the render-time
      // `activeScreen` closure would drop a block and collide keys whenever two
      // injections resolve close together.
      setActiveScreen((prev) =>
        appendColumn(prev, rowNumber, (columnNumber) => ({
          rowNumber,
          columnNumber,
          coilType: 'ROUTINE',
          inputType: null,
          value: '',
          tag: '',
          comment: '',
          routineOrigin: injected.origin,
          renderedAsm: injected.renderedAsm,
          combinator,
          isBlank: false,
          inverted,
        })),
      );
      setError(null);
    } catch (e) {
      // Injection legitimately fails for routines with unfilled `${INPUTn}`
      // placeholders (3 of the 21 shipped routines). Surfacing it beats the
      // previous behavior, where the click simply did nothing.
      setError(`Could not add "${routine.name}": ${message(e)}`);
    }
  }

  // Renaming a row's output is a pure local edit (no backend round-trip),
  // but it uses the same `prev`-based updater discipline as everything else
  // that touches `activeScreen` — never the render-time closure.
  function handleRowOutputRename(rowNumber: number, name: string | null) {
    setActiveScreen((prev) => ({
      ...prev,
      rows: prev.rows.map((row) => (row.rowNumber === rowNumber ? { ...row, outputName: name ?? undefined } : row)),
    }));
  }

  const availableRowOutputNames = activeScreen.rows
    .map((row) => row.outputName)
    .filter((name): name is string => Boolean(name));

  // Row-reference blocks always target the first existing row (or a new row
  // 1), mirroring `handleRoutineSelect` below — there's no rung/row-picking
  // UI yet for either flow.
  function handleOpenReferencePicker() {
    setShowReferencePicker(true);
  }

  function handleRowReferencePick(name: string) {
    setShowReferencePicker(false);
    setActiveScreen((prev) => {
      const targetRowNumber = prev.rows[0]?.rowNumber ?? 1;
      const targetRow = prev.rows.find((r) => r.rowNumber === targetRowNumber);
      const isFirstOnRow = !targetRow || targetRow.columns.length === 0;
      return appendColumn(prev, targetRowNumber, (columnNumber) => ({
        rowNumber: targetRowNumber,
        columnNumber,
        coilType: 'ROW_REF',
        inputType: null,
        value: '',
        tag: '',
        comment: '',
        routineOrigin: null,
        renderedAsm: null,
        // Same requirement as routine blocks (§7.3): every non-first block on
        // a row needs an explicit combinator. There's no picker step in this
        // flow yet, so a plain append defaults to AND — the common case.
        combinator: isFirstOnRow ? null : 'AND',
        isBlank: false,
        rowRefName: name,
      }));
    });
  }

  // Palette selection stands in for drag-and-drop until a real drop target
  // exists on the canvas (no rung/row-picking UI is in scope for this task).
  // Routine blocks always target the first existing row (or a new row 1),
  // so a second selection onto a non-empty row exercises the AND/OR picker.
  function handleRoutineSelect(routine: RoutineSummary) {
    const screen = activeScreenRef.current;
    const targetRowNumber = screen.rows[0]?.rowNumber ?? 1;
    const targetRow = screen.rows.find((r) => r.rowNumber === targetRowNumber);
    const isFirstOnRow = !targetRow || targetRow.columns.length === 0;
    void handleRoutineDrop(routine, targetRowNumber, isFirstOnRow);
  }

  return (
    <div className="app-shell">
      <header>
        <h1>Ladder Studio</h1>
        <div className="header-toggles">
          <ViewStyleToggle style={viewStyle} onChange={setViewStyle} />
          <EditModeToggle mode={mode} onChange={setMode} />
        </div>
      </header>
      {error && (
        <p className="app-error" role="alert">
          {error}
        </p>
      )}
      <main>
        <Palette onSelect={handleRoutineSelect} />
        <div className="canvas-column">
          {activeScreen.rows.length > 0 && (
            <div className="row-outputs-bar">
              {activeScreen.rows.map((row) => (
                <div key={row.rowNumber} className="row-outputs-bar__row">
                  <span className="row-outputs-bar__label">Row {row.rowNumber}</span>
                  <RowOutputName
                    name={row.outputName ?? null}
                    onRename={(name) => handleRowOutputRename(row.rowNumber, name)}
                  />
                </div>
              ))}
              <button type="button" className="row-outputs-bar__reference" onClick={handleOpenReferencePicker}>
                Reference row output…
              </button>
            </div>
          )}
          <Canvas screen={activeScreen} mode={mode} viewStyle={viewStyle} />
        </div>
        {pendingDrop && (
          <CombinatorPicker
            onPick={(c, inverted) => finishInjection(pendingDrop.routine, pendingDrop.rowNumber, c, inverted)}
          />
        )}
        {showReferencePicker && (
          <RowReferencePicker
            availableNames={availableRowOutputNames}
            onPick={handleRowReferencePick}
            onCancel={() => setShowReferencePicker(false)}
          />
        )}
        <PreviewPanel screen={activeScreen} />
      </main>
    </div>
  );
}
