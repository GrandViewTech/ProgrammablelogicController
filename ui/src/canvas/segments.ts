import type { ColumnScreen } from '../api/ladderStudio';

/**
 * A rung's columns, grouped for rendering as a real circuit branch rather
 * than a flat list. The underlying data model stays exactly as it is (a
 * flat sequence of columns, each carrying an explicit AND/OR combinator
 * relative to the running result, per requirements §7.3) — this only
 * changes how that sequence is drawn.
 *
 * A 'series' segment is a single block on the main line. A 'branch'
 * segment is a run of one anchor column followed by one or more
 * consecutive OR-connected columns: each column in the group gets its own
 * parallel line, forking from a common point and merging back into one
 * wire before the rung continues.
 */
export type Segment =
  | { kind: 'series'; column: ColumnScreen }
  | { kind: 'branch'; columns: ColumnScreen[] };

export function groupIntoSegments(columns: ColumnScreen[]): Segment[] {
  const segments: Segment[] = [];

  for (let index = 0; index < columns.length; index++) {
    const column = columns[index];

    if (index === 0 || column.combinator !== 'OR') {
      segments.push({ kind: 'series', column });
      continue;
    }

    const previous = segments[segments.length - 1];
    if (previous.kind === 'series') {
      segments[segments.length - 1] = { kind: 'branch', columns: [previous.column, column] };
    } else {
      previous.columns.push(column);
    }
  }

  return segments;
}
