import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { RowReferencePicker } from './RowReferencePicker';

const namedRows = [
  { name: 'Conveyor Running', rowNumber: 1 },
  { name: 'Door Open', rowNumber: 2 },
];

describe('RowReferencePicker', () => {
  it('lists every named row output from a strictly earlier row', () => {
    render(<RowReferencePicker namedRows={namedRows} targetRowNumber={3} onPick={() => {}} onCancel={() => {}} />);
    expect(screen.getByText('Conveyor Running')).toBeInTheDocument();
    expect(screen.getByText('Door Open')).toBeInTheDocument();
  });

  it('calls onPick with the clicked name', () => {
    const onPick = vi.fn();
    render(<RowReferencePicker namedRows={namedRows} targetRowNumber={3} onPick={onPick} onCancel={() => {}} />);
    fireEvent.click(screen.getByText('Conveyor Running'));
    expect(onPick).toHaveBeenCalledWith('Conveyor Running');
  });

  it('never offers the target row’s own output name', () => {
    // Referencing your own row means reading a bit this same scan has not
    // written yet — the compiler rejects it as ForwardRowReference.
    render(<RowReferencePicker namedRows={namedRows} targetRowNumber={2} onPick={() => {}} onCancel={() => {}} />);
    expect(screen.getByText('Conveyor Running')).toBeInTheDocument();
    expect(screen.queryByText('Door Open')).not.toBeInTheDocument();
  });

  it('never offers a later row’s output name', () => {
    render(<RowReferencePicker namedRows={namedRows} targetRowNumber={1} onPick={() => {}} onCancel={() => {}} />);
    expect(screen.queryByText('Conveyor Running')).not.toBeInTheDocument();
    expect(screen.queryByText('Door Open')).not.toBeInTheDocument();
  });

  it('shows a message and no list when there are no named outputs yet', () => {
    render(<RowReferencePicker namedRows={[]} targetRowNumber={1} onPick={() => {}} onCancel={() => {}} />);
    expect(screen.getByText(/no named row outputs yet/i)).toBeInTheDocument();
  });

  it('shows the same message when every named output is on the target row or later', () => {
    render(<RowReferencePicker namedRows={namedRows} targetRowNumber={1} onPick={() => {}} onCancel={() => {}} />);
    expect(screen.getByText(/no named row outputs yet/i)).toBeInTheDocument();
    expect(screen.queryByRole('list')).not.toBeInTheDocument();
  });

  it('calls onCancel when the cancel action is used', () => {
    const onCancel = vi.fn();
    render(<RowReferencePicker namedRows={namedRows} targetRowNumber={3} onPick={() => {}} onCancel={onCancel} />);
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(onCancel).toHaveBeenCalled();
  });
});
