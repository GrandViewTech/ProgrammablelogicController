import type { ColumnScreen } from '../api/ladderStudio';
import { BlockNode } from './BlockNode';
import type { ViewStyle } from './BlockNode';
import { groupIntoSegments } from './segments';

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
            {segmentIndex > 0 && (
              <span className="combinator-badge combinator-badge--and">AND</span>
            )}
            <BlockNode column={segment.column} mode={mode} viewStyle={viewStyle} />
          </div>
        ) : (
          <div
            key={`branch-${segment.columns[0].rowNumber}-${segment.columns[0].columnNumber}`}
            className="row-branch"
          >
            {segmentIndex > 0 && (
              <span className="combinator-badge combinator-badge--and">AND</span>
            )}
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
