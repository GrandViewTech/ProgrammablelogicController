import { useEffect, useMemo, useState } from 'react';
import { ladderStudioApi, type RoutineSummary } from '../api/ladderStudio';

export function Palette({ onSelect }: { onSelect: (routine: RoutineSummary) => void }) {
  const [routines, setRoutines] = useState<RoutineSummary[]>([]);
  const [query, setQuery] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    ladderStudioApi
      .listRoutines()
      .then((loaded) => {
        setRoutines(loaded);
        setError(null);
      })
      // Without this the palette just stays empty and the worker has no way to
      // tell "no routines installed" apart from "the backend call failed".
      .catch((e: unknown) => setError(`Could not load routines: ${e instanceof Error ? e.message : String(e)}`));
  }, []);

  const filtered = useMemo(
    () => routines.filter((r) => r.name.toLowerCase().includes(query.toLowerCase())),
    [routines, query],
  );

  return (
    <aside className="palette">
      <input
        placeholder="Search routines"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />
      {error && (
        <p className="panel-error" role="alert">
          {error}
        </p>
      )}
      <ul>
        {filtered.map((routine) => (
          <li key={routine.name} onClick={() => onSelect(routine)}>
            <strong>{routine.name}</strong>
            <p>{routine.description}</p>
          </li>
        ))}
      </ul>
    </aside>
  );
}
