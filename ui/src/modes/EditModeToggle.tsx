export type EditMode = 'WORKER' | 'ENGINEER';

export function EditModeToggle({
  mode,
  onChange,
}: {
  mode: EditMode;
  onChange: (mode: EditMode) => void;
}) {
  return (
    <div className="edit-mode-toggle" role="radiogroup" aria-label="Edit mode">
      {(['WORKER', 'ENGINEER'] as const).map((candidate) => (
        <button
          key={candidate}
          role="radio"
          aria-checked={mode === candidate}
          className={mode === candidate ? 'active' : ''}
          onClick={() => onChange(candidate)}
        >
          {candidate === 'WORKER' ? 'Worker' : 'Engineer'}
        </button>
      ))}
    </div>
  );
}
