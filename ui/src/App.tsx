import { useState } from 'react';
import { type EditMode, EditModeToggle } from './modes/EditModeToggle';
import { Palette } from './palette/Palette';
import { Canvas } from './canvas/Canvas';
import type { Screen } from './api/ladderStudio';

export default function App() {
  const [mode, setMode] = useState<EditMode>('WORKER');
  // setActiveScreen is unused until Task 13 wires drop handling; destructuring
  // it here (unused) would fail this project's noUnusedLocals check.
  const [activeScreen] = useState<Screen>({ rows: [], endRowNumber: null, endColumnNumber: null });

  return (
    <div className="app-shell">
      <header>
        <h1>Ladder Studio</h1>
        <EditModeToggle mode={mode} onChange={setMode} />
      </header>
      <main>
        <Palette onSelect={() => {}} />
        <Canvas screen={activeScreen} mode={mode} />
        {/* PreviewPanel wired in Task 14 */}
      </main>
    </div>
  );
}
