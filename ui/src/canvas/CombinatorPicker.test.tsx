import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CombinatorPicker } from './CombinatorPicker';

describe('CombinatorPicker', () => {
  it('calls onPick with AND when the AND button is clicked', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('AND'));
    expect(onPick).toHaveBeenCalledWith('AND');
  });

  it('calls onPick with OR when the OR button is clicked', () => {
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} />);
    fireEvent.click(screen.getByText('OR'));
    expect(onPick).toHaveBeenCalledWith('OR');
  });
});
