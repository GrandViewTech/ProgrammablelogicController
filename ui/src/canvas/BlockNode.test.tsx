import { describe, expect, it } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BlockNode } from './BlockNode';
import type { ColumnScreen } from '../api/ladderStudio';

function routineColumn(): ColumnScreen {
  return {
    rowNumber: 1,
    columnNumber: 1,
    coilType: 'ROUTINE',
    inputType: null,
    value: '',
    tag: '',
    comment: '',
    routineOrigin: { routineName: 'BIT RESET', description: 'Resets the target bit', values: {} },
    renderedAsm: 'MOV DPTR,#RLY512_519+\nMOVX A,@DPTR',
    combinator: null,
    isBlank: false,
  };
}

function rawColumn(coilType: string, value: string): ColumnScreen {
  return {
    rowNumber: 1,
    columnNumber: 1,
    coilType,
    inputType: 'INPUT',
    value,
    tag: '',
    comment: '',
    routineOrigin: null,
    renderedAsm: null,
    combinator: null,
    isBlank: false,
  };
}

describe('BlockNode expand/collapse', () => {
  it('shows only the description for a routine block by default', () => {
    render(<BlockNode column={routineColumn()} mode="WORKER" viewStyle="CARDS" />);
    expect(screen.getByText('Resets the target bit')).toBeInTheDocument();
    expect(screen.queryByText(/MOV DPTR/)).not.toBeInTheDocument();
  });

  it('reveals the raw ASM when the expand chevron is clicked', () => {
    render(<BlockNode column={routineColumn()} mode="WORKER" viewStyle="CARDS" />);
    fireEvent.click(screen.getByRole('button', { name: 'Expand block' }));
    expect(screen.getByText(/MOV DPTR/)).toBeInTheDocument();
  });

  it('hides the raw ASM again when the chevron is clicked a second time', () => {
    render(<BlockNode column={routineColumn()} mode="WORKER" viewStyle="CARDS" />);
    const button = screen.getByRole('button', { name: 'Expand block' });
    fireEvent.click(button);
    fireEvent.click(button);
    expect(screen.queryByText(/MOV DPTR/)).not.toBeInTheDocument();
  });

  it('does not show an expand chevron for a hand-built (non-routine) block', () => {
    render(<BlockNode column={rawColumn('LOAD', '3')} mode="ENGINEER" viewStyle="CARDS" />);
    expect(screen.queryByRole('button', { name: 'Expand block' })).not.toBeInTheDocument();
  });
});

describe('BlockNode view style', () => {
  it('renders a contact symbol for a LOAD block in Symbols view', () => {
    render(<BlockNode column={rawColumn('LOAD', '3')} mode="ENGINEER" viewStyle="SYMBOLS" />);
    expect(screen.getByLabelText('Contact')).toBeInTheDocument();
  });

  it('renders a coil symbol for an OUTPUT block in Symbols view', () => {
    render(<BlockNode column={rawColumn('OUTPUT', '3')} mode="ENGINEER" viewStyle="SYMBOLS" />);
    expect(screen.getByLabelText('Coil')).toBeInTheDocument();
  });

  it('renders the same friendly text for a raw block in Cards view', () => {
    render(<BlockNode column={rawColumn('LOAD', '3')} mode="ENGINEER" viewStyle="CARDS" />);
    expect(screen.getByText('LOAD 3')).toBeInTheDocument();
    expect(screen.queryByLabelText('Contact')).not.toBeInTheDocument();
  });
});

function groupedColumn(): ColumnScreen {
  return {
    ...rawColumn('LOAD', '1'),
    group: [rawColumn('LOAD', '2'), { ...rawColumn('LOAD', '3'), combinator: 'AND' }],
  };
}

describe('BlockNode nested groups', () => {
  it('renders every column inside a group', () => {
    render(<BlockNode column={groupedColumn()} mode="ENGINEER" viewStyle="CARDS" />);
    expect(screen.getByText('LOAD 2')).toBeInTheDocument();
    expect(screen.getByText('LOAD 3')).toBeInTheDocument();
  });

  it('shows an inversion badge for an inverted block', () => {
    render(<BlockNode column={{ ...rawColumn('LOAD', '1'), inverted: true }} mode="ENGINEER" viewStyle="CARDS" />);
    expect(screen.getByLabelText('Inverted (NOT)')).toBeInTheDocument();
  });

  it('does not show an inversion badge when not inverted', () => {
    render(<BlockNode column={rawColumn('LOAD', '1')} mode="ENGINEER" viewStyle="CARDS" />);
    expect(screen.queryByLabelText('Inverted (NOT)')).not.toBeInTheDocument();
  });
});
