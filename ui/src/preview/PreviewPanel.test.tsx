import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { PreviewPanel } from './PreviewPanel';
import { ladderStudioApi } from '../api/ladderStudio';
import type { ColumnScreen, Screen } from '../api/ladderStudio';

vi.mock('../api/ladderStudio', async () => {
  const actual = await vi.importActual<typeof import('../api/ladderStudio')>('../api/ladderStudio');
  return { ...actual, ladderStudioApi: { generate: vi.fn() } };
});

const generate = ladderStudioApi.generate as unknown as ReturnType<typeof vi.fn>;

const emptyScreen: Screen = { rows: [], endRowNumber: null, endColumnNumber: null };

function column(overrides: Partial<ColumnScreen>): ColumnScreen {
  return {
    rowNumber: 1,
    columnNumber: 1,
    coilType: 'LOAD',
    inputType: 'INPUT',
    value: '1',
    tag: '',
    comment: '',
    routineOrigin: null,
    renderedAsm: null,
    combinator: null,
    isBlank: false,
    ...overrides,
  };
}

function oneRow(columns: ColumnScreen[]): Screen {
  return { rows: [{ rowNumber: 1, columns }], endRowNumber: null, endColumnNumber: null };
}

beforeEach(() => {
  vi.clearAllMocks();
  generate.mockResolvedValue('');
});

describe('PreviewPanel', () => {
  it('defaults to Plain English view', async () => {
    render(<PreviewPanel screen={emptyScreen} />);
    expect(screen.getByRole('button', { name: 'Plain English', pressed: true })).toBeInTheDocument();
  });

  it('calls generate and shows Code view ASM when toggled', async () => {
    generate.mockResolvedValue('MOV DPTR,#RLY512_519+');
    render(<PreviewPanel screen={emptyScreen} />);
    fireEvent.click(screen.getByRole('button', { name: 'Code' }));
    await waitFor(() => expect(screen.getByText('MOV DPTR,#RLY512_519+')).toBeInTheDocument());
    expect(generate).toHaveBeenCalledWith(emptyScreen);
  });

  it('runs the generate validation check in the default Plain English view too', async () => {
    // `generate` is the only thing that surfaces compile errors. Gating it on
    // the non-default Code tab let a worker save a screen that could never
    // compile without ever being told why.
    render(<PreviewPanel screen={emptyScreen} />);
    await waitFor(() => expect(generate).toHaveBeenCalledWith(emptyScreen));
    expect(screen.getByRole('button', { name: 'Plain English', pressed: true })).toBeInTheDocument();
  });

  it('reports a compile error upward without the Code tab ever being opened', async () => {
    generate.mockRejectedValue(new Error('unknown row reference "X" on row 2'));
    const onValidationError = vi.fn();
    render(<PreviewPanel screen={emptyScreen} onValidationError={onValidationError} />);
    await waitFor(() =>
      expect(onValidationError).toHaveBeenCalledWith(expect.stringContaining('unknown row reference "X" on row 2')),
    );
  });

  it('clears a previously reported error once the screen compiles again', async () => {
    const onValidationError = vi.fn();
    render(<PreviewPanel screen={emptyScreen} onValidationError={onValidationError} />);
    await waitFor(() => expect(onValidationError).toHaveBeenCalledWith(null));
  });

  it('names the referenced row output instead of rendering a bare ROW_REF', async () => {
    render(<PreviewPanel screen={oneRow([column({ coilType: 'ROW_REF', rowRefName: 'Conveyor Running' })])} />);
    expect(screen.getByText(/row output = Conveyor Running/)).toBeInTheDocument();
    expect(screen.queryByText(/^ROW_REF$/)).not.toBeInTheDocument();
  });

  it('prefixes an inverted block with NOT', async () => {
    render(<PreviewPanel screen={oneRow([column({ value: '5', inverted: true })])} />);
    expect(screen.getByText('NOT LOAD 5')).toBeInTheDocument();
  });

  it('names the combinator joining each block to the one before it', async () => {
    render(
      <PreviewPanel
        screen={oneRow([
          column({ columnNumber: 1, value: '1' }),
          column({ columnNumber: 2, value: '2', combinator: 'XOR' }),
        ])}
      />,
    );
    expect(screen.getByText('LOAD 1 XOR LOAD 2')).toBeInTheDocument();
  });

  it('brackets a nested group and describes its members', async () => {
    render(
      <PreviewPanel
        screen={oneRow([
          column({ columnNumber: 1, value: '1' }),
          column({
            columnNumber: 2,
            combinator: 'OR',
            value: '',
            group: [
              column({ columnNumber: 1, value: '2' }),
              column({ columnNumber: 2, value: '3', combinator: 'AND', inverted: true }),
            ],
          }),
        ])}
      />,
    );
    expect(screen.getByText('LOAD 1 OR (LOAD 2 AND NOT LOAD 3)')).toBeInTheDocument();
  });
});
