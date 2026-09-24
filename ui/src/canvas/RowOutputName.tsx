import { useState } from 'react';

/**
 * `onRename` may reject the new name (see `App.handleRowOutputRename`: row
 * output names have to stay unique, because the compiler keys each row's
 * automatically-assigned relay address by name). A rejected rename returns
 * `false`, and the field snaps back to the name the screen actually holds —
 * otherwise the input would keep displaying a name that was never applied.
 */
export function RowOutputName({
  name,
  onRename,
}: {
  name: string | null;
  onRename: (name: string | null) => boolean | void;
}) {
  const [draft, setDraft] = useState(name ?? '');

  return (
    <input
      className="row-output-name"
      value={draft}
      placeholder="Name this row’s output (optional)"
      onChange={(e) => setDraft(e.target.value)}
      onBlur={() => {
        const trimmed = draft.trim();
        const accepted = onRename(trimmed.length > 0 ? trimmed : null);
        if (accepted === false) setDraft(name ?? '');
      }}
    />
  );
}
