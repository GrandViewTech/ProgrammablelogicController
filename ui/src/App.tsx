import { useEffect, useRef, useState } from 'react';
import { type EditMode, EditModeToggle } from './modes/EditModeToggle';
import { ViewStyleToggle } from './modes/ViewStyleToggle';
import { Palette } from './palette/Palette';
import { Canvas } from './canvas/Canvas';
import type { ViewStyle } from './canvas/BlockNode';
import { CombinatorPicker } from './canvas/CombinatorPicker';
import { PreviewPanel } from './preview/PreviewPanel';
import { ladderStudioApi } from './api/ladderStudio';
import type { Combinator, ColumnScreen, RoutineSummary, Screen } from './api/ladderStudio';

function message(error: unknown): string {
  if (typeof error === 'string') return error;
  if (error instanceof Error) return error.message;
  return String(error);
}

export default function App() {
  const [mode, setMode] = useState<EditMode>('WORKER');
  const [viewStyle, setViewStyle] = useState<ViewStyle>('CARDS');
  const [activeScreen, setActiveScreen] = useState<Screen>({ rows: [], endRowNumber: null, endColumnNumber: null });
  const [pendingDrop, setPendingDrop] = useState<{ routine: RoutineSummary; rowNumber: number } | null>(null);
  const [error, setError] = useState<string | null>(null);

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
      await finishInjection(routine, rowNumber, null);
    } else {
      setPendingDrop({ routine, rowNumber });
    }
  }

  async function finishInjection(routine: RoutineSummary, rowNumber: number, combinator: Combinator | null) {
    setPendingDrop(null);
    try {
      const injected = await ladderStudioApi.injectRoutine(routine.name, {});
      // Everything derived from the screen is computed inside the updater, from
      // `prev`. Deriving the target row (and therefore the new block's
      // `columnNumber`, which is also its React key) from the render-time
      // `activeScreen` closure would drop a block and collide keys whenever two
      // injections resolve close together.
      setActiveScreen((prev) => {
        const row = prev.rows.find((r) => r.rowNumber === rowNumber) ?? { rowNumber, columns: [] };
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
        return {
          ...prev,
          rows: [...prev.rows.filter((r) => r.rowNumber !== rowNumber), updatedRow].sort(
            (a, b) => a.rowNumber - b.rowNumber,
          ),
        };
      });
      setError(null);
    } catch (e) {
      // Injection legitimately fails for routines with unfilled `${INPUTn}`
      // placeholders (3 of the 21 shipped routines). Surfacing it beats the
      // previous behavior, where the click simply did nothing.
      setError(`Could not add "${routine.name}": ${message(e)}`);
    }
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
        <Canvas screen={activeScreen} mode={mode} viewStyle={viewStyle} />
        {pendingDrop && (
          <CombinatorPicker onPick={(c) => finishInjection(pendingDrop.routine, pendingDrop.rowNumber, c)} />
        )}
        <PreviewPanel screen={activeScreen} />
      </main>
    </div>
  );
}
