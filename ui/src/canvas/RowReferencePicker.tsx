/**
 * One named row output per list entry, filtered to the ones a row at
 * `targetRowNumber` may legally reference.
 *
 * Rows compile strictly top-to-bottom, exactly as a real PLC scan runs
 * (design spec §2), so a row can only usefully read an output written by a
 * *strictly earlier* row — referencing its own row, or a later one, is
 * `CompileError::ForwardRowReference`, not silently-wrong codegen. This
 * filter is what keeps the picker from offering a name that is guaranteed to
 * fail: it used to list every named row in the screen, including the target
 * row itself, which (with everything landing on row 1) meant every reachable
 * reference compiled to a forward-reference error.
 */
export function RowReferencePicker({
  namedRows,
  targetRowNumber,
  onPick,
  onCancel,
}: {
  namedRows: { name: string; rowNumber: number }[];
  targetRowNumber: number;
  onPick: (name: string) => void;
  onCancel: () => void;
}) {
  const availableNames = namedRows
    .filter((row) => row.rowNumber < targetRowNumber)
    .map((row) => row.name);

  return (
    <div className="row-reference-picker" role="dialog" aria-label="Reference an existing row's output">
      {availableNames.length === 0 ? (
        <p>No named row outputs yet — name an earlier row’s output first to reference it here.</p>
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
