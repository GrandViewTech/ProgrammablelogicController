import type { ViewStyle } from '../canvas/BlockNode';

export function ViewStyleToggle({
  style,
  onChange,
}: {
  style: ViewStyle;
  onChange: (style: ViewStyle) => void;
}) {
  return (
    <div className="view-style-toggle" role="radiogroup" aria-label="View style">
      {(['CARDS', 'SYMBOLS'] as const).map((candidate) => (
        <button
          key={candidate}
          role="radio"
          aria-checked={style === candidate}
          className={style === candidate ? 'active' : ''}
          onClick={() => onChange(candidate)}
        >
          {candidate === 'CARDS' ? 'Cards' : 'Symbols'}
        </button>
      ))}
    </div>
  );
}
