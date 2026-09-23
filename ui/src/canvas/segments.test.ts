import { describe, expect, it } from 'vitest';
import { groupIntoSegments } from './segments';
import type { ColumnScreen } from '../api/ladderStudio';

function column(columnNumber: number, combinator: 'AND' | 'OR' | null): ColumnScreen {
  return {
    rowNumber: 1,
    columnNumber,
    coilType: 'LOAD',
    inputType: 'INPUT',
    value: String(columnNumber),
    tag: '',
    comment: '',
    routineOrigin: null,
    renderedAsm: null,
    combinator,
    isBlank: false,
  };
}

describe('groupIntoSegments', () => {
  it('returns an empty list for an empty row', () => {
    expect(groupIntoSegments([])).toEqual([]);
  });

  it('puts a single column in its own series segment', () => {
    const c1 = column(1, null);
    expect(groupIntoSegments([c1])).toEqual([{ kind: 'series', column: c1 }]);
  });

  it('keeps AND-connected columns as separate series segments', () => {
    const c1 = column(1, null);
    const c2 = column(2, 'AND');
    expect(groupIntoSegments([c1, c2])).toEqual([
      { kind: 'series', column: c1 },
      { kind: 'series', column: c2 },
    ]);
  });

  it('folds an OR-connected column and its predecessor into one branch segment', () => {
    const c1 = column(1, null);
    const c2 = column(2, 'OR');
    expect(groupIntoSegments([c1, c2])).toEqual([{ kind: 'branch', columns: [c1, c2] }]);
  });

  it('adds a third consecutive OR column as a third branch in the same group', () => {
    const c1 = column(1, null);
    const c2 = column(2, 'OR');
    const c3 = column(3, 'OR');
    expect(groupIntoSegments([c1, c2, c3])).toEqual([
      { kind: 'branch', columns: [c1, c2, c3] },
    ]);
  });

  it('starts a fresh series segment after a branch group when the next column is AND', () => {
    const c1 = column(1, null);
    const c2 = column(2, 'OR');
    const c3 = column(3, 'AND');
    expect(groupIntoSegments([c1, c2, c3])).toEqual([
      { kind: 'branch', columns: [c1, c2] },
      { kind: 'series', column: c3 },
    ]);
  });

  it('starts a second independent branch group after a series column', () => {
    const c1 = column(1, null);
    const c2 = column(2, 'OR');
    const c3 = column(3, 'AND');
    const c4 = column(4, 'OR');
    expect(groupIntoSegments([c1, c2, c3, c4])).toEqual([
      { kind: 'branch', columns: [c1, c2] },
      { kind: 'branch', columns: [c3, c4] },
    ]);
  });
});
