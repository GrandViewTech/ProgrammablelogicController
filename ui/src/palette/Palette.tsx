import { useEffect, useMemo, useState } from 'react';
import { ladderStudioApi, type RoutineSummary } from '../api/ladderStudio';

export function Palette({ onSelect }: { onSelect: (routine: RoutineSummary) => void }) {
  const [routines, setRoutines] = useState<RoutineSummary[]>([]);
  const [query, setQuery] = useState('');

  useEffect(() => {
    ladderStudioApi.listRoutines().then(setRoutines);
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
