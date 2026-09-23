import type { ColumnScreen } from '../api/ladderStudio';
import { BlockNode } from './BlockNode';

export function RowLane({ columns, mode }: { columns: ColumnScreen[]; mode: 'WORKER' | 'ENGINEER' }) {
  return (
    <div className="row-lane">
      <span className="power-rail power-rail--left" />
      {columns.map((column, index) => (
        <div key={`${column.rowNumber}-${column.columnNumber}`} className="row-lane__cell">
          {index > 0 && column.combinator && (
            <span className={`combinator-badge combinator-badge--${column.combinator.toLowerCase()}`}>
              {column.combinator}
            </span>
          )}
          <BlockNode column={column} mode={mode} />
        </div>
      ))}
      <span className="power-rail power-rail--right" />
    </div>
  );
}
