import { useState } from 'react';
import type { Combinator } from '../api/ladderStudio';

const DEFAULT_LABEL = 'Choose AND, OR, or XOR, and optionally invert';

/**
 * Shown at drop time for every block after the first on a rung (requirements
 * §7.3) — never inferred from neighboring block types.
 *
 * `prompt` names which question this instance is asking, because the group
 * authoring flow asks two of them in sequence (how the new block joins the
 * previous one *inside* a new group, then how the whole group joins what came
 * before it) and a bare AND/OR/XOR row twice over would be ambiguous.
 *
 * `onGroup`, when supplied, adds the "Group with previous block" escape
 * hatch: instead of appending flat, bracket this block together with the
 * rung's current last block. The invert checkbox deliberately does not apply
 * to that path — grouping captures its own inversion choices in the two
 * follow-up steps, one for the new block and one for the group as a whole.
 */
export function CombinatorPicker({
  prompt,
  onPick,
  onGroup,
}: {
  prompt?: string;
  onPick: (combinator: Combinator, inverted: boolean) => void;
  onGroup?: () => void;
}) {
  const [inverted, setInverted] = useState(false);

  return (
    <div className="combinator-picker" role="group" aria-label={prompt ?? DEFAULT_LABEL}>
      {prompt && <p className="combinator-picker__prompt">{prompt}</p>}
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
      {onGroup && (
        <button type="button" className="combinator-picker__group" onClick={onGroup}>
          Group with previous block
        </button>
      )}
    </div>
  );
}
