import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { RowOutputName } from './RowOutputName';

describe('RowOutputName', () => {
  it('shows the existing name when the row already has one', () => {
    render(<RowOutputName name="Conveyor Running" onRename={() => {}} />);
    expect(screen.getByDisplayValue('Conveyor Running')).toBeInTheDocument();
  });

  it('shows a placeholder prompt when the row has no name yet', () => {
    render(<RowOutputName name={null} onRename={() => {}} />);
    expect(screen.getByPlaceholderText('Name this row’s output (optional)')).toBeInTheDocument();
  });

  it('calls onRename with the trimmed value when the field loses focus', () => {
    const onRename = vi.fn();
    render(<RowOutputName name={null} onRename={onRename} />);
    const input = screen.getByPlaceholderText('Name this row’s output (optional)');
    fireEvent.change(input, { target: { value: '  Conveyor Running  ' } });
    fireEvent.blur(input);
    expect(onRename).toHaveBeenCalledWith('Conveyor Running');
  });

  it('calls onRename with null when the field is cleared', () => {
    const onRename = vi.fn();
    render(<RowOutputName name="Conveyor Running" onRename={onRename} />);
    const input = screen.getByDisplayValue('Conveyor Running');
    fireEvent.change(input, { target: { value: '' } });
    fireEvent.blur(input);
    expect(onRename).toHaveBeenCalledWith(null);
  });
});
