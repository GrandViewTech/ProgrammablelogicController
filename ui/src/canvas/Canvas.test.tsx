import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Canvas } from './Canvas';
import type { Screen } from '../api/ladderStudio';

const wideScreen: Screen = {
  rows: [
    {
      rowNumber: 1,
      columns: [
        {
          rowNumber: 1, columnNumber: 1, coilType: 'LOAD', inputType: 'INPUT', value: '1',
          tag: '', comment: '', routineOrigin: null, renderedAsm: null, combinator: null, isBlank: false,
        },
        {
          rowNumber: 1, columnNumber: 2, coilType: 'ROUTINE', inputType: null, value: '',
          tag: '', comment: '',
          routineOrigin: { routineName: 'BIT RESET', description: 'Resets the target bit', values: {} },
          renderedAsm: 'MOV DPTR,#RLY512_519+', combinator: 'AND', isBlank: false,
        },
      ],
    },
  ],
  endRowNumber: null,
  endColumnNumber: null,
};

describe('Canvas', () => {
  it('renders a lock badge for routine-sourced blocks in Worker mode', () => {
    render(<Canvas screen={wideScreen} mode="WORKER" />);
    expect(screen.getByText('Resets the target bit')).toBeInTheDocument();
    expect(screen.getByLabelText('Locked routine block')).toBeInTheDocument();
  });

  it('does not show a lock badge for hand-built blocks', () => {
    const engineerScreen: Screen = {
      ...wideScreen,
      rows: [{ rowNumber: 1, columns: [wideScreen.rows[0].columns[0]] }],
    };
    render(<Canvas screen={engineerScreen} mode="WORKER" />);
    expect(screen.queryByLabelText('Locked routine block')).not.toBeInTheDocument();
  });

  it('wraps rows in a horizontally scrollable, explicit-width box', () => {
    const { container } = render(<Canvas screen={wideScreen} mode="WORKER" />);
    const scrollBox = container.querySelector('.canvas-scroll-box');
    expect(scrollBox).not.toBeNull();
  });
});
