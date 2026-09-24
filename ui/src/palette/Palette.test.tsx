import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { Palette } from './Palette';
import { ladderStudioApi } from '../api/ladderStudio';

vi.mock('../api/ladderStudio', () => ({
  ladderStudioApi: { listRoutines: vi.fn() },
}));

describe('Palette', () => {
  it('lists routines returned by list_routines', async () => {
    (ladderStudioApi.listRoutines as any).mockResolvedValue([
      { name: 'BIT RESET', description: 'Resets the target bit' },
    ]);
    render(<Palette onSelect={() => {}} />);
    await waitFor(() => expect(screen.getByText('BIT RESET')).toBeInTheDocument());
    expect(screen.getByText('Resets the target bit')).toBeInTheDocument();
  });

  it('filters by search text', async () => {
    (ladderStudioApi.listRoutines as any).mockResolvedValue([
      { name: 'BIT RESET', description: 'Resets the target bit' },
      { name: 'TIMER', description: 'Starts a timer' },
    ]);
    render(<Palette onSelect={() => {}} />);
    await waitFor(() => expect(screen.getByText('TIMER')).toBeInTheDocument());
    const input = screen.getByPlaceholderText('Search routines') as HTMLInputElement;
    fireEvent.change(input, { target: { value: 'timer' } });
    await waitFor(() => expect(screen.queryByText('BIT RESET')).not.toBeInTheDocument());
  });
});
