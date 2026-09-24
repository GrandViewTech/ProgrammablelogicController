import { useEffect, useRef, useState } from 'react';
import { ladderStudioApi } from '../api/ladderStudio';
import type { ColumnScreen, Screen } from '../api/ladderStudio';

type ViewMode = 'PLAIN_ENGLISH' | 'CODE';

/**
 * One block, in words. Covers everything this plan added — nesting, NOT,
 * XOR and cross-row references — so the default view actually describes what
 * a worker built instead of hiding it:
 *
 * - a `group` renders its own children in brackets, recursively;
 * - a `ROW_REF` names the row output it reads (it used to render the bare
 *   string "ROW_REF", which told a worker nothing at all);
 * - an inverted block is prefixed "NOT".
 */
function describeColumn(column: ColumnScreen): string {
  let body: string;
  if (column.group && column.group.length > 0) {
    body = `(${column.group.map(describeInSequence).join(' ')})`;
  } else if (column.coilType === 'ROW_REF') {
    body = `row output = ${column.rowRefName ?? '(unnamed)'}`;
  } else if (column.routineOrigin) {
    body = column.routineOrigin.description;
  } else {
    body = `${column.coilType} ${column.value}`.trim();
  }
  return column.inverted ? `NOT ${body}` : body;
}

/**
 * A block together with the combinator joining it to whatever precedes it.
 * The first block in any sequence (a row's own columns, or a group's) has
 * nothing to combine with, so it carries no combinator — matching the
 * compiler's own `resolve_combinator`.
 */
function describeInSequence(column: ColumnScreen, index: number): string {
  const described = describeColumn(column);
  if (index === 0) return described;
  return `${column.combinator ?? 'AND'} ${described}`;
}

function describeRow(row: Screen['rows'][number]): string {
  const parts = row.columns.filter((c) => !c.isBlank).map(describeInSequence);
  return parts.length > 0 ? parts.join(' ') : '(empty rung)';
}

/**
 * Always visible, side-by-side with the diagram — not a separate export step
 * (requirements §7.4). Both views are driven by the same `generate` call
 * (requirements §6, item 2): Code shows its raw ASM output; Plain English is
 * derived client-side from the same Screen data without a second round trip.
 *
 * `generate` runs on every screen change regardless of the selected view.
 * It used to be gated on `mode === 'CODE'`, but `generate` is also the only
 * thing that surfaces real compile errors (`UnknownRowReference`,
 * `ForwardRowReference`, a missing combinator, a non-numeric value) — and
 * the default view is Plain English, so a worker could build and save a
 * screen that could never compile and never be told why. The view toggle now
 * gates only what is *displayed*, never whether validation *runs*; errors go
 * to `onValidationError` so they can be shown in a persistent app-level
 * banner that no preview tab can hide.
 */
export function PreviewPanel({
  screen,
  onValidationError,
}: {
  screen: Screen;
  onValidationError?: (message: string | null) => void;
}) {
  const [mode, setMode] = useState<ViewMode>('PLAIN_ENGLISH');
  const [code, setCode] = useState<string>('');

  // Held in a ref so the callback's identity never becomes an effect
  // dependency: App passes it inline, so a new function arrives on every
  // parent render and would re-issue `generate` on renders that didn't
  // change the screen at all.
  const notifyRef = useRef(onValidationError);
  useEffect(() => {
    notifyRef.current = onValidationError;
  }, [onValidationError]);

  // Latest-wins guard: screen edits can land faster than the round trip, and
  // a slow earlier call resolving last would otherwise overwrite the newer
  // result — showing a stale error for a screen that already compiles (or
  // clearing a real one).
  const runIdRef = useRef(0);

  useEffect(() => {
    const runId = ++runIdRef.current;
    ladderStudioApi
      .generate(screen)
      .then((asm) => {
        if (runId !== runIdRef.current) return;
        setCode(asm);
        notifyRef.current?.(null);
      })
      // `generate` reports real compile errors (a missing combinator, an
      // un-injected ROUTINE block, a non-numeric value, a forward or unknown
      // row reference). Swallowing them left the Code view showing stale or
      // empty ASM with no explanation — and left the default Plain English
      // view showing no explanation at all.
      .catch((e: unknown) => {
        if (runId !== runIdRef.current) return;
        setCode('');
        notifyRef.current?.(`Could not generate code: ${e instanceof Error ? e.message : String(e)}`);
      });
  }, [screen]);

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
