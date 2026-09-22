import { useState } from 'react';
import { type EditMode, EditModeToggle } from './modes/EditModeToggle';
import { Palette } from './palette/Palette';

export default function App() {
  const [mode, setMode] = useState<EditMode>('WORKER');

  return (
    <div className="app-shell">
      <header>
        <h1>Ladder Studio</h1>
        <EditModeToggle mode={mode} onChange={setMode} />
      </header>
      <main>
        <Palette onSelect={() => {}} />
        {/* Canvas and PreviewPanel wired in Tasks 12-14 */}
      </main>
    </div>
  );
}
