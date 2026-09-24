import { beforeEach, describe, expect, it, vi } from 'vitest';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import App from './App';
import { ladderStudioApi } from './api/ladderStudio';
import type { Screen } from './api/ladderStudio';

vi.mock('./api/ladderStudio', async () => {
  const actual = await vi.importActual<typeof import('./api/ladderStudio')>('./api/ladderStudio');
  return {
    ...actual,
    ladderStudioApi: { generate: vi.fn(), listRoutines: vi.fn(), injectRoutine: vi.fn() },
  };
});

const generate = ladderStudioApi.generate as unknown as ReturnType<typeof vi.fn>;
const listRoutines = ladderStudioApi.listRoutines as unknown as ReturnType<typeof vi.fn>;
const injectRoutine = ladderStudioApi.injectRoutine as unknown as ReturnType<typeof vi.fn>;

/**
 * The `Screen` App most recently handed to the compiler. `PreviewPanel` calls
 * `generate(screen)` on every meaningful screen change, so this is the real
 * payload the Rust side would compile — the closest a UI test can get to
 * "what does the backend actually receive" without running the compiler.
 */
function lastGeneratedScreen(): Screen {
  expect(generate).toHaveBeenCalled();
  return generate.mock.calls[generate.mock.calls.length - 1][0] as Screen;
}

async function waitForScreen(predicate: (screen: Screen) => boolean): Promise<Screen> {
  await waitFor(() => expect(predicate(lastGeneratedScreen())).toBe(true));
  return lastGeneratedScreen();
}

beforeEach(() => {
  vi.clearAllMocks();
  generate.mockResolvedValue('');
  listRoutines.mockResolvedValue([{ name: 'BIT RESET', description: 'Resets the target bit' }]);
  injectRoutine.mockResolvedValue({
    renderedAsm: 'MOV DPTR,#RLY512_519+',
    origin: { routineName: 'BIT RESET', description: 'Resets the target bit', values: {} },
  });
});

/**
 * Every interaction below kicks off async work (routine injection, and the
 * preview panel's `generate` round trip), so each one is flushed inside
 * `act` rather than left to settle after the assertion.
 */
async function click(element: HTMLElement) {
  await act(async () => {
    fireEvent.click(element);
  });
}

async function renderApp() {
  await act(async () => {
    render(<App />);
  });
}

/** Puts one routine block on whichever rung is currently targeted. */
async function addRoutineBlock() {
  await click(await screen.findByText('BIT RESET'));
}

async function switchToEngineerMode() {
  await click(screen.getByRole('radio', { name: 'Engineer' }));
}

/** Fills and submits the Engineer-only raw block form. */
async function addRawBlock({ coil = 'Contact (LOAD)', value }: { coil?: string; value: string }) {
  await act(async () => {
    fireEvent.click(screen.getByLabelText(coil));
    fireEvent.change(screen.getByLabelText('Address'), { target: { value } });
  });
  await click(screen.getByRole('button', { name: 'Add block' }));
}

async function nameTargetRowOutput(rowIndex: number, name: string) {
  const fields = screen.getAllByPlaceholderText('Name this row\u2019s output (optional)');
  await act(async () => {
    fireEvent.change(fields[rowIndex], { target: { value: name } });
    fireEvent.blur(fields[rowIndex]);
  });
}

describe('App — row targeting', () => {
  it('starts out targeting row 1', async () => {
    await renderApp();
    expect(screen.getByText(/Adding to/)).toHaveTextContent('Adding to Row 1');
  });

  it('“New row” targets one past the highest existing row, and blocks land there', async () => {
    await renderApp();
    await addRoutineBlock();
    await waitForScreen((s) => s.rows.length === 1);

    await click(screen.getByRole('button', { name: 'New row' }));
    expect(screen.getByText(/Adding to/)).toHaveTextContent('Adding to Row 2');

    await addRoutineBlock();
    const built = await waitForScreen((s) => s.rows.length === 2);
    expect(built.rows.map((r) => r.rowNumber)).toEqual([1, 2]);
    expect(built.rows[1].columns).toHaveLength(1);
    expect(built.rows[1].columns[0].rowNumber).toBe(2);
    // Sole block on its own rung — no combinator required, and no picker.
    expect(built.rows[1].columns[0].combinator).toBeNull();
  });

  it('lets a worker switch back to an earlier row from the row bar', async () => {
    await renderApp();
    await addRoutineBlock();
    await waitForScreen((s) => s.rows.length === 1);
    await click(screen.getByRole('button', { name: 'New row' }));
    await addRoutineBlock();
    await waitForScreen((s) => s.rows.length === 2);

    await click(screen.getByRole('button', { name: 'Add to row 1' }));
    expect(screen.getByText(/Adding to/)).toHaveTextContent('Adding to Row 1');
    expect(screen.getByRole('button', { name: 'Add to row 1' })).toHaveAttribute('aria-pressed', 'true');
  });
});

describe('App — cross-row references', () => {
  it('builds a RowRef on a strictly later row than the row it references', async () => {
    await renderApp();

    // Row 1: one block, named "X".
    await addRoutineBlock();
    await waitForScreen((s) => s.rows.length === 1);
    await nameTargetRowOutput(0, 'X');
    await waitForScreen((s) => s.rows[0].outputName === 'X');

    // Row 2: a reference to "X".
    await click(screen.getByRole('button', { name: 'New row' }));
    await click(screen.getByRole('button', { name: 'Reference row output…' }));
    await click(screen.getByText('X'));

    const built = await waitForScreen((s) => s.rows.length === 2);
    const referencingRow = built.rows.find((r) => r.rowNumber === 2)!;
    const reference = referencingRow.columns[0];
    expect(reference.coilType).toBe('ROW_REF');
    expect(reference.rowRefName).toBe('X');

    // The property the compiler actually enforces: a reference may only
    // point at a STRICTLY EARLIER row, otherwise it is
    // `CompileError::ForwardRowReference`. Before this fix every append
    // targeted row 1, so the only reachable reference pointed at its own
    // row and failed 100% of the time.
    const referencedRow = built.rows.find((r) => r.outputName === 'X')!;
    expect(referencingRow.rowNumber).toBeGreaterThan(referencedRow.rowNumber);
  });

  it('offers no names when the only named row is the row being added to', async () => {
    await renderApp();
    await addRoutineBlock();
    await waitForScreen((s) => s.rows.length === 1);
    await nameTargetRowOutput(0, 'X');
    await waitForScreen((s) => s.rows[0].outputName === 'X');

    // Still targeting row 1 — "X" is row 1's own output.
    await click(screen.getByRole('button', { name: 'Reference row output…' }));
    expect(screen.getByText(/no named row outputs yet/i)).toBeInTheDocument();
  });
});

describe('App — raw block placement', () => {
  it('hides the raw block form in Worker mode', async () => {
    // Requirements §7.1: worker mode is routine-only.
    await renderApp();
    expect(screen.queryByLabelText('Add a raw block')).not.toBeInTheDocument();
  });

  it('shows the raw block form in Engineer mode', async () => {
    await renderApp();
    await switchToEngineerMode();
    expect(screen.getByLabelText('Add a raw block')).toBeInTheDocument();
  });

  it('places a raw LOAD contact on the targeted row', async () => {
    await renderApp();
    await switchToEngineerMode();
    await addRawBlock({ value: '19' });

    const built = await waitForScreen((s) => s.rows.length === 1);
    expect(built.rows[0].columns).toEqual([
      expect.objectContaining({
        rowNumber: 1,
        columnNumber: 1,
        coilType: 'LOAD',
        inputType: 'INPUT',
        value: '19',
        combinator: null,
        inverted: false,
      }),
    ]);
  });

  it('places a raw OUTPUT coil with no input type', async () => {
    await renderApp();
    await switchToEngineerMode();
    await addRawBlock({ coil: 'Coil (OUTPUT)', value: '4' });

    const built = await waitForScreen((s) => s.rows.length === 1);
    expect(built.rows[0].columns[0]).toMatchObject({ coilType: 'OUTPUT', inputType: null, value: '4' });
  });

  it('routes a raw block appended to a non-empty row through the combinator picker', async () => {
    await renderApp();
    await switchToEngineerMode();
    await addRawBlock({ value: '1' });
    await waitForScreen((s) => s.rows[0]?.columns.length === 1);

    await addRawBlock({ value: '2' });
    // Not committed yet — the picker is open, exactly as it is for a routine.
    expect(lastGeneratedScreen().rows[0].columns).toHaveLength(1);
    expect(screen.getByRole('group', { name: /Choose AND, OR, or XOR/ })).toBeInTheDocument();

    await click(screen.getByLabelText('Invert (NOT)'));
    await click(screen.getByRole('button', { name: 'XOR' }));

    const built = await waitForScreen((s) => s.rows[0].columns.length === 2);
    expect(built.rows[0].columns[1]).toMatchObject({
      coilType: 'LOAD',
      value: '2',
      columnNumber: 2,
      combinator: 'XOR',
      inverted: true,
    });
  });

  it('routes a row reference appended to a non-empty row through the picker too', async () => {
    // Previously this flow silently defaulted to AND with no picker step.
    await renderApp();
    await switchToEngineerMode();
    await addRawBlock({ value: '1' });
    await waitForScreen((s) => s.rows.length === 1);
    await nameTargetRowOutput(0, 'X');
    await waitForScreen((s) => s.rows[0].outputName === 'X');

    await click(screen.getByRole('button', { name: 'New row' }));
    await addRawBlock({ value: '2' });
    await waitForScreen((s) => s.rows.length === 2);

    await click(screen.getByRole('button', { name: 'Reference row output…' }));
    await click(screen.getByText('X'));
    expect(screen.getByRole('group', { name: /Choose AND, OR, or XOR/ })).toBeInTheDocument();
    await click(screen.getByRole('button', { name: 'OR' }));

    const built = await waitForScreen((s) => s.rows[1].columns.length === 2);
    expect(built.rows[1].columns[1]).toMatchObject({
      coilType: 'ROW_REF',
      rowRefName: 'X',
      combinator: 'OR',
      inverted: false,
    });
  });
});

describe('App — row output names', () => {
  /** Two rows, each holding one routine block. */
  async function twoRows() {
    await renderApp();
    await addRoutineBlock();
    await waitForScreen((s) => s.rows.length === 1);
    await click(screen.getByRole('button', { name: 'New row' }));
    await addRoutineBlock();
    await waitForScreen((s) => s.rows.length === 2);
  }

  it('rejects renaming a row output to a name another row already uses', async () => {
    await twoRows();
    await nameTargetRowOutput(0, 'X');
    await waitForScreen((s) => s.rows[0].outputName === 'X');

    await nameTargetRowOutput(1, 'X');

    expect(screen.getByRole('alert')).toHaveTextContent(/Row 1 already uses the output name/);
    // The duplicate must never reach the screen: the compiler keys a row
    // output's relay address by name, so two rows sharing one name collapse
    // onto one address and silently overwrite each other.
    const built = lastGeneratedScreen();
    expect(built.rows.filter((r) => r.outputName === 'X')).toHaveLength(1);
    expect(built.rows[1].outputName).toBeUndefined();
  });

  it('allows two different names on two different rows', async () => {
    await twoRows();
    await nameTargetRowOutput(0, 'X');
    await waitForScreen((s) => s.rows[0].outputName === 'X');
    await nameTargetRowOutput(1, 'Y');

    const built = await waitForScreen((s) => s.rows[1].outputName === 'Y');
    expect(built.rows.map((r) => r.outputName)).toEqual(['X', 'Y']);
  });

  it('allows a row to keep its own name on a re-blur', async () => {
    await twoRows();
    await nameTargetRowOutput(0, 'X');
    await waitForScreen((s) => s.rows[0].outputName === 'X');

    // Same row, same name — a self-clash must not be treated as a duplicate.
    await nameTargetRowOutput(0, 'X');
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(lastGeneratedScreen().rows[0].outputName).toBe('X');
  });
});
