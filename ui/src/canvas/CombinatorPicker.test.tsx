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

  it('offers no grouping action unless onGroup is supplied', () => {
    render(<CombinatorPicker onPick={() => {}} />);
    expect(screen.queryByRole('button', { name: 'Group with previous block' })).not.toBeInTheDocument();
  });

  it('calls onGroup when the grouping action is used', () => {
    const onGroup = vi.fn();
    const onPick = vi.fn();
    render(<CombinatorPicker onPick={onPick} onGroup={onGroup} />);
    fireEvent.click(screen.getByRole('button', { name: 'Group with previous block' }));
    expect(onGroup).toHaveBeenCalled();
    expect(onPick).not.toHaveBeenCalled();
  });

  it('shows the prompt and uses it as the accessible name when given', () => {
    render(<CombinatorPicker prompt="How does the group combine?" onPick={() => {}} />);
    expect(screen.getByText('How does the group combine?')).toBeInTheDocument();
    expect(screen.getByRole('group', { name: 'How does the group combine?' })).toBeInTheDocument();
  });

  it('hides the XOR button when excludeXor is set, e.g. combining a routine block', () => {
    // A routine block combined via XOR can silently collapse to a constant
    // result (most shipped routines never touch the carry flag), so the
    // picker hides the option rather than let it produce a rung that always
    // reads the same regardless of the routine's actual state.
    render(<CombinatorPicker onPick={() => {}} excludeXor />);
    expect(screen.getByText('AND')).toBeInTheDocument();
    expect(screen.getByText('OR')).toBeInTheDocument();
    expect(screen.queryByText('XOR')).not.toBeInTheDocument();
  });

  it('still offers XOR when excludeXor is not set', () => {
    render(<CombinatorPicker onPick={() => {}} />);
    expect(screen.getByText('XOR')).toBeInTheDocument();
  });
});
