export function RowReferencePicker({
  availableNames,
  onPick,
  onCancel,
}: {
  availableNames: string[];
  onPick: (name: string) => void;
  onCancel: () => void;
}) {
  return (
    <div className="row-reference-picker" role="dialog" aria-label="Reference an existing row's output">
      {availableNames.length === 0 ? (
        <p>No named row outputs yet — name a row's output first to reference it elsewhere.</p>
      ) : (
        <ul>
          {availableNames.map((name) => (
            <li key={name} onClick={() => onPick(name)}>
              {name}
            </li>
          ))}
        </ul>
      )}
      <button type="button" onClick={onCancel}>
        Cancel
      </button>
    </div>
  );
}
