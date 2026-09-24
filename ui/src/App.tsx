import { useEffect, useRef, useState } from 'react';
import { type EditMode, EditModeToggle } from './modes/EditModeToggle';
import { ViewStyleToggle } from './modes/ViewStyleToggle';
import { Palette } from './palette/Palette';
import { Canvas } from './canvas/Canvas';
import type { ViewStyle } from './canvas/BlockNode';
import { CombinatorPicker } from './canvas/CombinatorPicker';
import { RawBlockForm, type RawBlockDraft } from './canvas/RawBlockForm';
import { RowOutputName } from './canvas/RowOutputName';
import { RowReferencePicker } from './canvas/RowReferencePicker';
import { PreviewPanel } from './preview/PreviewPanel';
import { ladderStudioApi } from './api/ladderStudio';
import type { Combinator, ColumnScreen, InjectedBlock, RoutineSummary, Screen } from './api/ladderStudio';

function message(error: unknown): string {
  if (typeof error === 'string') return error;
  if (error instanceof Error) return error.message;
  return String(error);
}

/**
 * What a worker asked to place, before it's known where it lands or how it
 * combines. The three kinds are the three things that can become a column:
 * a library routine, a hand-built raw contact/coil, and a reference to an
 * earlier row's named output.
 *
 * They share one append pipeline deliberately — every one of them needs the
 * same "is this the first block on the rung? if not, pick a combinator"
 * decision, and the previous code had that logic copy-pasted per flow (with
 * the row-reference copy silently defaulting to AND instead of asking).
 */
type PendingBlock =
  | { kind: 'ROUTINE'; routine: RoutineSummary }
  | { kind: 'RAW'; draft: RawBlockDraft }
  | { kind: 'ROW_REF'; name: string };

/** A `PendingBlock` with any async work (routine injection) already done. */
type ResolvedBlock =
  | { kind: 'ROUTINE'; routine: RoutineSummary; injected: InjectedBlock }
  | { kind: 'RAW'; draft: RawBlockDraft }
  | { kind: 'ROW_REF'; name: string };

/** How a block reads in an error message, before it exists as a column. */
function describePendingBlock(block: PendingBlock): string {
  switch (block.kind) {
    case 'ROUTINE':
      return block.routine.name;
    case 'RAW':
      return `${block.draft.coilType} ${block.draft.value}`;
    case 'ROW_REF':
      return `reference to “${block.name}”`;
  }
}

/**
 * The one place a `ColumnScreen` is built. Every field a column can carry is
 * listed once here, so a new block kind can't quietly omit one (the previous
 * per-flow object literals had already drifted: the row-reference one never
 * set `inverted`).
 */
function buildColumn(
  block: ResolvedBlock,
  rowNumber: number,
  columnNumber: number,
  combinator: Combinator | null,
  inverted: boolean,
): ColumnScreen {
  const base = {
    rowNumber,
    columnNumber,
    inputType: null,
    value: '',
    tag: '',
    comment: '',
    routineOrigin: null,
    renderedAsm: null,
    combinator,
    isBlank: false,
    inverted,
  } satisfies Partial<ColumnScreen> & Omit<ColumnScreen, 'coilType'>;

  switch (block.kind) {
    case 'ROUTINE':
      return {
        ...base,
        coilType: 'ROUTINE',
        routineOrigin: block.injected.origin,
        renderedAsm: block.injected.renderedAsm,
      };
    case 'RAW':
      return {
        ...base,
        coilType: block.draft.coilType,
        inputType: block.draft.inputType,
        value: block.draft.value,
        tag: block.draft.tag,
      };
    case 'ROW_REF':
      return { ...base, coilType: 'ROW_REF', rowRefName: block.name };
  }
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

/**
 * Replaces the row's current last top-level column with a single new column
 * whose `group` is `[thatOldColumn, theNewColumn]` — the one grouping
 * gesture this app offers ("group with the immediately preceding block"),
 * deliberately not general multi-select.
 *
 * Two real combinator choices are captured, never defaulted:
 *
 * - `makeColumn` builds the new block already carrying the combinator and
 *   inversion describing how it joins the old block *inside* the group.
 * - `outer` describes how the whole group joins whatever precedes it in the
 *   row, and becomes the grouping column's own combinator/inversion —
 *   exactly what the compiler reads for a `group` slot (design spec §3).
 *
 * Two details that matter to the compiler:
 *
 * - The old column's own combinator is cleared as it moves inside the group.
 *   It described how that block joined what preceded it *in the row*; that
 *   role now belongs to the grouping column. Leaving it would describe a
 *   relationship to a sibling it no longer has.
 * - Column numbering restarts at 1 inside the group, matching `traversal.rs`,
 *   which evaluates a group's own `Vec<ColumnScreen>` as its own slice with
 *   its own first-position rules.
 */
function groupWithLastColumn(
  prev: Screen,
  rowNumber: number,
  makeColumn: (columnNumber: number) => ColumnScreen,
  outer: { combinator: Combinator | null; inverted: boolean },
): Screen {
  const row = prev.rows.find((r) => r.rowNumber === rowNumber);
  // Nothing to group with — shouldn't be reachable (the grouping action is
  // only offered once a rung has a block on it), but falling back to a plain
  // append beats producing a one-member group.
  if (!row || row.columns.length === 0) return appendColumn(prev, rowNumber, makeColumn);

  const precedingColumns = row.columns.slice(0, -1);
  const oldLastColumn = row.columns[row.columns.length - 1];

  const groupingColumn: ColumnScreen = {
    rowNumber,
    columnNumber: oldLastColumn.columnNumber,
    // The compiler never reads `coilType` for a column with a `group`: it
    // dispatches on `group` being present, before the coil-type match. LOAD
    // matches the convention `traversal.rs`'s own group tests use.
    coilType: 'LOAD',
    inputType: null,
    value: '',
    tag: '',
    comment: '',
    routineOrigin: null,
    renderedAsm: null,
    // Only meaningful when something actually precedes the group; when the
    // group becomes the row's first column there is nothing to combine with,
    // and a first-position combinator is ignored anyway.
    combinator: precedingColumns.length > 0 ? outer.combinator : null,
    isBlank: false,
    inverted: outer.inverted,
    group: [{ ...oldLastColumn, columnNumber: 1, combinator: null }, makeColumn(2)],
  };

  return {
    ...prev,
    rows: prev.rows.map((r) => (r.rowNumber === rowNumber ? { ...r, columns: [...precedingColumns, groupingColumn] } : r)),
  };
}

/**
 * Which question the combinator picker is currently asking for a pending
 * block. A plain append asks one; grouping asks two more, in sequence.
 */
type AppendStage =
  | { step: 'COMBINE' }
  | { step: 'GROUP_INNER' }
  | { step: 'GROUP_OUTER'; innerCombinator: Combinator; innerInverted: boolean };

export default function App() {
  const [mode, setMode] = useState<EditMode>('WORKER');
  const [viewStyle, setViewStyle] = useState<ViewStyle>('CARDS');
  const [activeScreen, setActiveScreen] = useState<Screen>({ rows: [], endRowNumber: null, endColumnNumber: null });
  // The block a worker has asked to place on a non-empty rung, parked until
  // they pick how it combines with what's already there — and, if they chose
  // to group it with the previous block, through the two follow-up questions
  // that grouping needs answered (`AppendStage`).
  const [pendingAppend, setPendingAppend] = useState<{
    block: PendingBlock;
    rowNumber: number;
    stage: AppendStage;
  } | null>(null);
  const [error, setError] = useState<string | null>(null);
  // Compile errors reported by `PreviewPanel`'s always-on `generate` check.
  // Kept at app level, not inside the panel, so a worker sees an unbuildable
  // screen no matter which preview tab happens to be selected.
  const [validationError, setValidationError] = useState<string | null>(null);
  const [showReferencePicker, setShowReferencePicker] = useState(false);
  // Which rung new blocks land on. Everything used to hard-target
  // `rows[0]?.rowNumber ?? 1`, so a worker could never build a screen with
  // more than one row — and the only reachable row-reference flow therefore
  // always pointed at the target row's own not-yet-written output, which the
  // compiler correctly rejects as `ForwardRowReference` every single time.
  // A real second row is what makes cross-row references usable at all.
  const [targetRowNumber, setTargetRowNumber] = useState(1);

  // Mirrors `activeScreen` for the synchronous pre-check in `startAppend`,
  // which has to decide whether to show the AND/OR/XOR picker *before* any
  // async injection runs and so cannot use a state updater. Click handlers
  // always run after effects have flushed, so this is current for the normal
  // one-block-at-a-time flow; the only residual staleness is two clicks
  // landing while an injection is still in flight, which at worst shows or
  // skips a picker. The actual state mutation in `commitAppend` reads `prev`,
  // so it can never corrupt the screen.
  const activeScreenRef = useRef(activeScreen);
  useEffect(() => {
    activeScreenRef.current = activeScreen;
  }, [activeScreen]);

  /**
   * Entry point for every kind of block placement. A block landing on an
   * empty rung has nothing to combine with and goes straight in; anything
   * else parks in `pendingAppend` until the worker picks a combinator —
   * requirements §7.3 makes that choice mandatory, never inferred.
   */
  function startAppend(block: PendingBlock) {
    const targetRow = activeScreenRef.current.rows.find((r) => r.rowNumber === targetRowNumber);
    if (!targetRow || targetRow.columns.length === 0) {
      void commitAppend(block, targetRowNumber, null, false);
    } else {
      setPendingAppend({ block, rowNumber: targetRowNumber, stage: { step: 'COMBINE' } });
    }
  }

  /** Resolves any async work a block needs before it can become a column. */
  async function resolveBlock(block: PendingBlock): Promise<ResolvedBlock> {
    if (block.kind !== 'ROUTINE') return block;
    const injected = await ladderStudioApi.injectRoutine(block.routine.name, {});
    return { kind: 'ROUTINE', routine: block.routine, injected };
  }

  async function commitAppend(
    block: PendingBlock,
    rowNumber: number,
    combinator: Combinator | null,
    inverted: boolean,
  ) {
    setPendingAppend(null);
    try {
      const resolved = await resolveBlock(block);
      // Everything derived from the screen is computed inside the updater, from
      // `prev`. Deriving the target row (and therefore the new block's
      // `columnNumber`, which is also its React key) from the render-time
      // `activeScreen` closure would drop a block and collide keys whenever two
      // injections resolve close together.
      setActiveScreen((prev) =>
        appendColumn(prev, rowNumber, (columnNumber) =>
          buildColumn(resolved, rowNumber, columnNumber, combinator, inverted),
        ),
      );
      setError(null);
    } catch (e) {
      // Routine injection legitimately fails for routines with unfilled
      // `${INPUTn}` placeholders (3 of the 21 shipped routines). Surfacing it
      // beats the previous behavior, where the click simply did nothing.
      setError(`Could not add "${describePendingBlock(block)}": ${message(e)}`);
    }
  }

  /**
   * Same pipeline as `commitAppend`, but the new column is bracketed together
   * with the rung's current last column instead of appended beside it.
   */
  async function commitGroup(
    block: PendingBlock,
    rowNumber: number,
    inner: { combinator: Combinator; inverted: boolean },
    outer: { combinator: Combinator | null; inverted: boolean },
  ) {
    setPendingAppend(null);
    try {
      const resolved = await resolveBlock(block);
      setActiveScreen((prev) =>
        groupWithLastColumn(
          prev,
          rowNumber,
          (columnNumber) => buildColumn(resolved, rowNumber, columnNumber, inner.combinator, inner.inverted),
          outer,
        ),
      );
      setError(null);
    } catch (e) {
      setError(`Could not add "${describePendingBlock(block)}": ${message(e)}`);
    }
  }

  /** "Group with previous block" — moves on to the first of two questions. */
  function handleStartGrouping() {
    setPendingAppend((prev) => (prev ? { ...prev, stage: { step: 'GROUP_INNER' } } : prev));
  }

  /**
   * Answer to "how does the new block join the previous one *inside* the
   * group". If something precedes the block being swallowed into the group,
   * a second question follows (how the group itself joins that); if the group
   * is becoming the rung's first column there is nothing before it to combine
   * with, so it commits straight away.
   */
  function handleInnerCombinatorPick(combinator: Combinator, inverted: boolean) {
    if (!pendingAppend) return;
    const { block, rowNumber } = pendingAppend;
    const row = activeScreenRef.current.rows.find((r) => r.rowNumber === rowNumber);
    const groupWillHavePrecedingSiblings = (row?.columns.length ?? 0) > 1;
    if (groupWillHavePrecedingSiblings) {
      setPendingAppend({
        block,
        rowNumber,
        stage: { step: 'GROUP_OUTER', innerCombinator: combinator, innerInverted: inverted },
      });
      return;
    }
    void commitGroup(block, rowNumber, { combinator, inverted }, { combinator: null, inverted: false });
  }

  /** Answer to "how does the whole group join what came before it". */
  function handleOuterCombinatorPick(combinator: Combinator, inverted: boolean) {
    if (!pendingAppend || pendingAppend.stage.step !== 'GROUP_OUTER') return;
    const { block, rowNumber, stage } = pendingAppend;
    void commitGroup(
      block,
      rowNumber,
      { combinator: stage.innerCombinator, inverted: stage.innerInverted },
      { combinator, inverted },
    );
  }

  // Renaming a row's output is a pure local edit (no backend round-trip),
  // but it uses the same `prev`-based updater discipline as everything else
  // that touches `activeScreen` — never the render-time closure.
  //
  // Names must be unique across the screen. The compiler keys a row output's
  // automatically-assigned relay address by name
  // (`assign_relay_addresses`/`assign_row_owners` are both `HashMap<String,
  // _>`), so two rows sharing a name collapse to one address: the second row
  // silently overwrites the first row's bit, and every `RowRef` to that name
  // resolves to whichever row happened to be inserted last. Rejecting the
  // rename keeps the "one name, one address" invariant the whole
  // cross-row-reference feature rests on.
  // Returns `false` when the rename was rejected, so the field can snap back
  // to the name the screen actually has instead of displaying one it doesn't.
  function handleRowOutputRename(rowNumber: number, name: string | null): boolean {
    if (name !== null) {
      // Synchronous pre-check, so it uses `activeScreenRef` rather than the
      // render-time closure — the same discipline `handleRoutineSelect` uses
      // for its own pre-check. The mutation below still goes through `prev`.
      const clash = activeScreenRef.current.rows.find(
        (row) => row.rowNumber !== rowNumber && row.outputName === name,
      );
      if (clash) {
        setError(`Row ${clash.rowNumber} already uses the output name “${name}” — pick a different one.`);
        return false;
      }
    }
    setError(null);
    setActiveScreen((prev) => ({
      ...prev,
      rows: prev.rows.map((row) => (row.rowNumber === rowNumber ? { ...row, outputName: name ?? undefined } : row)),
    }));
    return true;
  }

  // Every named row, paired with the row that owns it, so the reference
  // picker can drop the ones that aren't strictly earlier than the row the
  // reference is being added to (design spec §2: rows compile strictly
  // top-to-bottom, so only a *previous* row's output is readable).
  const namedRows = activeScreen.rows
    .filter((row) => Boolean(row.outputName))
    .map((row) => ({ name: row.outputName as string, rowNumber: row.rowNumber }));

  // Starts a fresh rung below every existing one and points subsequent
  // appends at it. The row itself doesn't exist in `activeScreen` until the
  // first block lands on it — `appendColumn` already creates a missing row
  // on demand, so no placeholder row is needed (and an abandoned "New row"
  // click leaves no empty rung behind).
  function handleNewRow() {
    const highestRowNumber = activeScreen.rows.reduce((max, row) => Math.max(max, row.rowNumber), 0);
    setTargetRowNumber(highestRowNumber + 1);
  }

  function handleOpenReferencePicker() {
    setShowReferencePicker(true);
  }

  // Now goes through the shared append pipeline, so a reference landing on a
  // non-empty rung gets the same combinator/invert picker every other block
  // kind gets, instead of silently defaulting to AND.
  function handleRowReferencePick(name: string) {
    setShowReferencePicker(false);
    startAppend({ kind: 'ROW_REF', name });
  }

  // Palette selection stands in for drag-and-drop until a real drop target
  // exists on the canvas. Blocks land on `targetRowNumber` (the row the
  // "New row" / row-selector controls point at), so a second selection onto
  // a non-empty row exercises the AND/OR/XOR picker.
  function handleRoutineSelect(routine: RoutineSummary) {
    startAppend({ kind: 'ROUTINE', routine });
  }

  // Engineer-mode only (see the render below and `RawBlockForm`'s own doc
  // comment): worker mode is routine-only by product design, requirements
  // §7.1.
  function handleRawBlockAdd(draft: RawBlockDraft) {
    startAppend({ kind: 'RAW', draft });
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
      {validationError && (
        <p className="app-error" role="alert">
          {validationError}
        </p>
      )}
      <main>
        <Palette onSelect={handleRoutineSelect} />
        <div className="canvas-column">
          <div className="canvas-toolbar">
            <span className="canvas-toolbar__target">
              Adding to <strong>Row {targetRowNumber}</strong>
            </span>
            <button type="button" className="canvas-toolbar__new-row" onClick={handleNewRow}>
              New row
            </button>
          </div>
          {/* Requirements §7.1: a worker may only place library routines and
              cannot edit raw contacts/coils, so raw block placement is gated
              on Engineer mode exactly the way `EditModeToggle`'s `mode`
              already gates the rest of the app's engineer-only behavior. */}
          {mode === 'ENGINEER' && <RawBlockForm onAdd={handleRawBlockAdd} />}
          {activeScreen.rows.length > 0 && (
            <div className="row-outputs-bar">
              {activeScreen.rows.map((row) => (
                <div key={row.rowNumber} className="row-outputs-bar__row">
                  <button
                    type="button"
                    className="row-outputs-bar__label"
                    aria-pressed={row.rowNumber === targetRowNumber}
                    aria-label={`Add to row ${row.rowNumber}`}
                    onClick={() => setTargetRowNumber(row.rowNumber)}
                  >
                    Row {row.rowNumber}
                  </button>
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
        {pendingAppend?.stage.step === 'COMBINE' && (
          <CombinatorPicker
            onPick={(c, inverted) => commitAppend(pendingAppend.block, pendingAppend.rowNumber, c, inverted)}
            onGroup={handleStartGrouping}
          />
        )}
        {pendingAppend?.stage.step === 'GROUP_INNER' && (
          <CombinatorPicker
            prompt="Inside the group: how does the new block join the previous one?"
            onPick={handleInnerCombinatorPick}
          />
        )}
        {pendingAppend?.stage.step === 'GROUP_OUTER' && (
          <CombinatorPicker
            prompt="How does the group combine with what comes before it?"
            onPick={handleOuterCombinatorPick}
          />
        )}
        {showReferencePicker && (
          <RowReferencePicker
            namedRows={namedRows}
            targetRowNumber={targetRowNumber}
            onPick={handleRowReferencePick}
            onCancel={() => setShowReferencePicker(false)}
          />
        )}
        <PreviewPanel screen={activeScreen} onValidationError={setValidationError} />
      </main>
    </div>
  );
}
