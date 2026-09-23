import { useEffect, useState } from 'react';
import { ladderStudioApi } from '../api/ladderStudio';
import type { Screen } from '../api/ladderStudio';

type ViewMode = 'PLAIN_ENGLISH' | 'CODE';

function describeRow(row: Screen['rows'][number]): string {
  const parts = row.columns
    .filter((c) => !c.isBlank)
    .map((c) => (c.routineOrigin ? c.routineOrigin.description : `${c.coilType} ${c.value}`.trim()));
  return parts.length > 0 ? parts.join(', then ') : '(empty rung)';
}

/**
 * Always visible, side-by-side with the diagram — not a separate export step
 * (requirements §7.4). Both views are driven by the same `generate` call
 * (requirements §6, item 2): Code shows its raw ASM output; Plain English is
 * derived client-side from the same Screen data without a second round trip.
 */
export function PreviewPanel({ screen }: { screen: Screen }) {
  const [mode, setMode] = useState<ViewMode>('PLAIN_ENGLISH');
  const [code, setCode] = useState<string>('');

  useEffect(() => {
    if (mode === 'CODE') {
      ladderStudioApi.generate(screen).then(setCode);
    }
  }, [mode, screen]);

  return (
    <section className="preview-panel">
      <div className="preview-panel__toggle" role="group" aria-label="Preview view">
        <button
          aria-pressed={mode === 'PLAIN_ENGLISH'}
          className={mode === 'PLAIN_ENGLISH' ? 'active' : ''}
          onClick={() => setMode('PLAIN_ENGLISH')}
        >
          Plain English
        </button>
        <button
          aria-pressed={mode === 'CODE'}
          className={mode === 'CODE' ? 'active' : ''}
          onClick={() => setMode('CODE')}
        >
          Code
        </button>
      </div>
      {mode === 'PLAIN_ENGLISH' ? (
        <ul className="preview-panel__plain-english">
          {screen.rows.map((row) => (
            <li key={row.rowNumber}>{describeRow(row)}</li>
          ))}
        </ul>
      ) : (
        <pre className="mono preview-panel__code">{code}</pre>
      )}
    </section>
  );
}
