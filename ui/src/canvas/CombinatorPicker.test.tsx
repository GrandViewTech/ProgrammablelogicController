import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CombinatorPicker } from './CombinatorPicker';

describe('CombinatorPicker', () => {
  it('calls onPick with AND and inverted=false by default', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('AND'));
    expect(onPick).toHaveBeenCalledWith('AND', false);
  });

  it('calls onPick with OR when the OR button is clicked', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('OR'));
    expect(onPick).toHaveBeenCalledWith('OR', false);
  });

  it('calls onPick with XOR when the XOR button is clicked', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('XOR'));
    expect(onPick).toHaveBeenCalledWith('XOR', false);
  });

  it('passes inverted=true when the Invert checkbox is checked before picking', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByLabelText('Invert (NOT)'));
    fireEvent.click(screen.getByText('AND'));
    expect(onPick).toHaveBeenCalledWith('AND', true);
  });
});
