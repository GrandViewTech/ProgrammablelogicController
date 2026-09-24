import { useState } from 'react';
import type { ColumnScreen } from '../api/ladderStudio';

export type ViewStyle = 'CARDS' | 'SYMBOLS';

/**
 * Traditional ladder-logic symbol for a raw (hand-built) block, used in
 * Symbols view. Routine blocks never render this way — they're an opaque
 * injected subroutine (§5), not a single contact/coil, so they stay a
 * labeled box in both views.
 */
function RawSymbol({ coilType, value }: { coilType: string; value: string }) {
  if (coilType === 'OUTPUT') {
    return (
      <span className="block-symbol block-symbol--coil" aria-label="Coil">
        <span className="block-symbol__glyph">( )</span>
        <span className="block-symbol__value">{value}</span>
      </span>
    );
  }
  if (coilType === 'LOAD') {
    return (
      <span className="block-symbol block-symbol--contact" aria-label="Contact">
        <span className="block-symbol__glyph">| |</span>
        <span className="block-symbol__value">{value}</span>
      </span>
    );
  }
  return (
    <span className="block-node__raw">
      {coilType} {value}
    </span>
  );
}

export function BlockNode({
  column,
  mode,
  viewStyle,
}: {
  column: ColumnScreen;
  mode: 'WORKER' | 'ENGINEER';
  viewStyle: ViewStyle;
}) {
  const [expanded, setExpanded] = useState(false);
  const hasGroup = column.group !== undefined && column.group.length > 0;
  const isRoutine = column.routineOrigin !== null;
  const isLocked = mode === 'WORKER' && isRoutine;

  return (
    <div className={`block-node${isRoutine ? ' block-node--routine' : ''}`}>
      {isLocked && <span aria-label="Locked routine block" className="lock-badge">🔒</span>}
      {column.inverted && (
        <span aria-label="Inverted (NOT)" className="invert-badge">NOT</span>
      )}
      {hasGroup ? (
        <div className="block-node__group">
          {column.group!.map((inner) => (
            <BlockNode key={`${inner.rowNumber}-${inner.columnNumber}`} column={inner} mode={mode} viewStyle={viewStyle} />
          ))}
        </div>
      ) : isRoutine ? (
        <>
          <span className="block-node__description">{column.routineOrigin!.description}</span>
          <button
            type="button"
            className="block-node__expand"
            aria-label="Expand block"
            aria-expanded={expanded}
            onClick={() => setExpanded((value) => !value)}
          >
            {expanded ? '▾' : '▸'}
          </button>
          {expanded && <pre className="mono block-node__asm">{column.renderedAsm}</pre>}
        </>
      ) : viewStyle === 'SYMBOLS' ? (
        <RawSymbol coilType={column.coilType} value={column.value} />
      ) : (
        <span className="block-node__raw">
          {column.coilType} {column.value}
        </span>
      )}
    </div>
  );
}
