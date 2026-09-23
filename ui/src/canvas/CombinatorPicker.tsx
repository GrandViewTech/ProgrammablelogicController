import { useState } from 'react';
import type { Combinator } from '../api/ladderStudio';

/**
 * Shown at drop time for every block after the first on a rung (requirements
 * §7.3) — never inferred from neighboring block types.
 */
export function CombinatorPicker({ onPick }: { onPick: (combinator: Combinator, inverted: boolean) => void }) {
  const [inverted, setInverted] = useState(false);

  return (
    <div className="combinator-picker" role="group" aria-label="Choose AND, OR, or XOR, and optionally invert">
      <label className="combinator-picker__invert">
        <input
          type="checkbox"
          aria-label="Invert (NOT)"
          checked={inverted}
          onChange={(e) => setInverted(e.target.checked)}
        />
        Invert (NOT)
      </label>
      <div className="combinator-picker__buttons">
        {(['AND', 'OR', 'XOR'] as const).map((combinator) => (
          <button key={combinator} onClick={() => onPick(combinator, inverted)}>
            {combinator}
          </button>
        ))}
      </div>
    </div>
  );
}
