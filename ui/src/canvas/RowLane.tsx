import type { ColumnScreen } from '../api/ladderStudio';
import { BlockNode } from './BlockNode';
import type { ViewStyle } from './BlockNode';
import { groupIntoSegments } from './segments';

/**
 * The combinator badge drawn between this segment and the one before it.
 *
 * It used to be the hardcoded string "AND", which was accurate only while
 * AND was the sole badge-rendered combinator. XOR is now a real choice, so
 * the badge has to read the column's own value or it silently mislabels an
 * XOR rung as an AND rung.
 *
 * OR never reaches here: an OR-combined column is drawn as a real parallel
 * branch (`.row-branch`), not a badge. A missing combinator on a non-first
 * block is a compile error the preview surfaces — the badge falls back to
 * AND rather than rendering an empty pill.
 */
function CombinatorBadge({ column }: { column: ColumnScreen }) {
  const combinator = column.combinator ?? 'AND';
  return (
    <span className={`combinator-badge combinator-badge--${combinator.toLowerCase()}`}>{combinator}</span>
  );
}

export function RowLane({
  columns,
  mode,
  viewStyle,
}: {
  columns: ColumnScreen[];
  mode: 'WORKER' | 'ENGINEER';
  viewStyle: ViewStyle;
}) {
  const segments = groupIntoSegments(columns);

  return (
    <div className="row-lane">
      <span className="power-rail power-rail--left" />
      {segments.map((segment, segmentIndex) =>
        segment.kind === 'series' ? (
          <div key={`${segment.column.rowNumber}-${segment.column.columnNumber}`} className="row-lane__cell">
            {segmentIndex > 0 && <CombinatorBadge column={segment.column} />}
            <BlockNode column={segment.column} mode={mode} viewStyle={viewStyle} />
          </div>
        ) : (
          <div
            key={`branch-${segment.columns[0].rowNumber}-${segment.columns[0].columnNumber}`}
            className="row-branch"
          >
            {/* A branch's own combinator is its anchor column's — the
                OR-connected columns after it are what make it a branch in the
                first place, so their OR is drawn as the fork, not a badge. */}
            {segmentIndex > 0 && <CombinatorBadge column={segment.columns[0]} />}
            <div className="row-branch__paths">
              {segment.columns.map((column) => (
                <div key={`${column.rowNumber}-${column.columnNumber}`} className="row-branch__path">
                  <BlockNode column={column} mode={mode} viewStyle={viewStyle} />
                </div>
              ))}
            </div>
          </div>
        ),
      )}
      <span className="power-rail power-rail--right" />
    </div>
  );
}
