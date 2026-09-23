import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { RowReferencePicker } from './RowReferencePicker';

describe('RowReferencePicker', () => {
  it('lists every available named row output', () => {
    render(<RowReferencePicker availableNames={['Conveyor Running', 'Door Open']} onPick={() => {}} onCancel={() => {}} />);
    expect(screen.getByText('Conveyor Running')).toBeInTheDocument();
    expect(screen.getByText('Door Open')).toBeInTheDocument();
  });

  it('calls onPick with the clicked name', () => {
    const onPick = vi.fn();
    render(<RowReferencePicker availableNames={['Conveyor Running']} onPick={onPick} onCancel={() => {}} />);
    fireEvent.click(screen.getByText('Conveyor Running'));
    expect(onPick).toHaveBeenCalledWith('Conveyor Running');
  });

  it('shows a message and no list when there are no named outputs yet', () => {
    render(<RowReferencePicker availableNames={[]} onPick={() => {}} onCancel={() => {}} />);
    expect(screen.getByText(/no named row outputs yet/i)).toBeInTheDocument();
  });

  it('calls onCancel when the cancel action is used', () => {
    const onCancel = vi.fn();
    render(<RowReferencePicker availableNames={['Conveyor Running']} onPick={() => {}} onCancel={onCancel} />);
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(onCancel).toHaveBeenCalled();
  });
});
