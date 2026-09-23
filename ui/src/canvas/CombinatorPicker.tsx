import type { Combinator } from '../api/ladderStudio';

/**
 * Shown at drop time for every block after the first on a rung (requirements
 * §7.3) — never inferred from neighboring block types.
 */
export function CombinatorPicker({ onPick }: { onPick: (combinator: Combinator) => void }) {
  return (
    <div className="combinator-picker" role="group" aria-label="Choose AND or OR">
      <button onClick={() => onPick('AND')}>AND</button>
      <button onClick={() => onPick('OR')}>OR</button>
    </div>
  );
}
