import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { PreviewPanel } from './PreviewPanel';
import { ladderStudioApi } from '../api/ladderStudio';
import type { Screen } from '../api/ladderStudio';

vi.mock('../api/ladderStudio', async () => {
  const actual = await vi.importActual<typeof import('../api/ladderStudio')>('../api/ladderStudio');
  return { ...actual, ladderStudioApi: { generate: vi.fn() } };
});

const emptyScreen: Screen = { rows: [], endRowNumber: null, endColumnNumber: null };

describe('PreviewPanel', () => {
  it('defaults to Plain English view', async () => {
    render(<PreviewPanel screen={emptyScreen} />);
    expect(screen.getByRole('button', { name: 'Plain English', pressed: true })).toBeInTheDocument();
  });

  it('calls generate and shows Code view ASM when toggled', async () => {
    (ladderStudioApi.generate as any).mockResolvedValue('MOV DPTR,#RLY512_519+');
    render(<PreviewPanel screen={emptyScreen} />);
    fireEvent.click(screen.getByRole('button', { name: 'Code' }));
    await waitFor(() => expect(screen.getByText('MOV DPTR,#RLY512_519+')).toBeInTheDocument());
    expect(ladderStudioApi.generate).toHaveBeenCalledWith(emptyScreen);
  });
});
