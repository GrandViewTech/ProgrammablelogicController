import { useState } from 'react';

export function RowOutputName({ name, onRename }: { name: string | null; onRename: (name: string | null) => void }) {
  const [draft, setDraft] = useState(name ?? '');

  return (
    <input
      className="row-output-name"
      value={draft}
      placeholder="Name this row’s output (optional)"
      onChange={(e) => setDraft(e.target.value)}
      onBlur={() => {
        const trimmed = draft.trim();
        onRename(trimmed.length > 0 ? trimmed : null);
      }}
    />
  );
}
