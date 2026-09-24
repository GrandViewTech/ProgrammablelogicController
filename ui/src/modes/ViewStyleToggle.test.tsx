import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ViewStyleToggle } from './ViewStyleToggle';

describe('ViewStyleToggle', () => {
  it('marks the current style as checked', () => {
    render(<ViewStyleToggle style="CARDS" onChange={() => {}} />);
    expect(screen.getByRole('radio', { name: 'Cards' })).toHaveAttribute('aria-checked', 'true');
    expect(screen.getByRole('radio', { name: 'Symbols' })).toHaveAttribute('aria-checked', 'false');
  });

  it('calls onChange with SYMBOLS when the Symbols button is clicked', () => {
    const onChange = vi.fn();
    render(<ViewStyleToggle style="CARDS" onChange={onChange} />);
    fireEvent.click(screen.getByRole('radio', { name: 'Symbols' }));
    expect(onChange).toHaveBeenCalledWith('SYMBOLS');
  });
});
