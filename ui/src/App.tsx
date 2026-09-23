import { useState } from 'react';
import { type EditMode, EditModeToggle } from './modes/EditModeToggle';
import { Palette } from './palette/Palette';
import { Canvas } from './canvas/Canvas';
import { CombinatorPicker } from './canvas/CombinatorPicker';
import { ladderStudioApi } from './api/ladderStudio';
import type { Combinator, ColumnScreen, RoutineSummary, Screen } from './api/ladderStudio';

export default function App() {
  const [mode, setMode] = useState<EditMode>('WORKER');
  const [activeScreen, setActiveScreen] = useState<Screen>({ rows: [], endRowNumber: null, endColumnNumber: null });
  const [pendingDrop, setPendingDrop] = useState<{ routine: RoutineSummary; rowNumber: number } | null>(null);

  async function handleRoutineDrop(routine: RoutineSummary, rowNumber: number, isFirstOnRow: boolean) {
    if (isFirstOnRow) {
      await finishInjection(routine, rowNumber, null);
    } else {
      setPendingDrop({ routine, rowNumber });
    }
  }

  async function finishInjection(routine: RoutineSummary, rowNumber: number, combinator: Combinator | null) {
    const injected = await ladderStudioApi.injectRoutine(routine.name, {});
    const row = activeScreen.rows.find((r) => r.rowNumber === rowNumber) ?? { rowNumber, columns: [] };
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
    setActiveScreen((prev) => ({
      ...prev,
      rows: [...prev.rows.filter((r) => r.rowNumber !== rowNumber), updatedRow].sort((a, b) => a.rowNumber - b.rowNumber),
    }));
    setPendingDrop(null);
  }

  // Palette selection stands in for drag-and-drop until a real drop target
  // exists on the canvas (no rung/row-picking UI is in scope for this task).
  // Routine blocks always target the first existing row (or a new row 1),
  // so a second selection onto a non-empty row exercises the AND/OR picker.
  function handleRoutineSelect(routine: RoutineSummary) {
    const targetRowNumber = activeScreen.rows[0]?.rowNumber ?? 1;
    const targetRow = activeScreen.rows.find((r) => r.rowNumber === targetRowNumber);
    const isFirstOnRow = !targetRow || targetRow.columns.length === 0;
    void handleRoutineDrop(routine, targetRowNumber, isFirstOnRow);
  }

  return (
    <div className="app-shell">
      <header>
        <h1>Ladder Studio</h1>
        <EditModeToggle mode={mode} onChange={setMode} />
      </header>
      <main>
        <Palette onSelect={handleRoutineSelect} />
        <Canvas screen={activeScreen} mode={mode} />
        {pendingDrop && (
          <CombinatorPicker onPick={(c) => finishInjection(pendingDrop.routine, pendingDrop.rowNumber, c)} />
        )}
        {/* PreviewPanel wired in Task 14 */}
      </main>
    </div>
  );
}
