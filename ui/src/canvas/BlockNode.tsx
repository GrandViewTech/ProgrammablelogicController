import type { ColumnScreen } from '../api/ladderStudio';

export function BlockNode({ column, mode }: { column: ColumnScreen; mode: 'WORKER' | 'ENGINEER' }) {
  const isRoutine = column.routineOrigin !== null;
  const isLocked = mode === 'WORKER' && isRoutine;

  return (
    <div className={`block-node${isRoutine ? ' block-node--routine' : ''}`}>
      {isLocked && <span aria-label="Locked routine block" className="lock-badge">🔒</span>}
      {isRoutine ? (
        <span className="block-node__description">{column.routineOrigin!.description}</span>
      ) : (
        <span className="block-node__raw">
          {column.coilType} {column.value}
        </span>
      )}
    </div>
  );
}
