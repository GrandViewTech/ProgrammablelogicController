import type { Screen } from '../api/ladderStudio';
import { RowLane } from './RowLane';

/**
 * Rungs are variable-width (requirements §4); a rung can legitimately exceed
 * the visible canvas width. Whole-canvas horizontal scroll (requirements
 * §7.5): the content sits in an explicit-width scrollable box so both power
 * rails span the box's full width and stay anchored while scrolling.
 */
export function Canvas({ screen, mode }: { screen: Screen; mode: 'WORKER' | 'ENGINEER' }) {
  return (
    <div className="canvas">
      <div className="canvas-scroll-box">
        {screen.rows.map((row) => (
          <RowLane key={row.rowNumber} columns={row.columns} mode={mode} />
        ))}
        <div className="canvas-edge-fade" aria-hidden="true" />
        <div className="canvas-scroll-chip">Scroll for more →</div>
      </div>
    </div>
  );
}
