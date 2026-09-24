import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { RawBlockForm } from './RawBlockForm';

function fillAddress(value: string) {
  fireEvent.change(screen.getByLabelText('Address'), { target: { value } });
}

describe('RawBlockForm', () => {
  it('defaults to a LOAD contact reading from INPUT', () => {
    render(<RawBlockForm onAdd={() => {}} />);
    expect(screen.getByLabelText('Contact (LOAD)')).toBeChecked();
    expect(screen.getByLabelText('INPUT')).toBeChecked();
  });

  it('submits a LOAD draft with the chosen input type, address and tag', () => {
    const onAdd = vi.fn();
    render(<RawBlockForm onAdd={onAdd} />);
    fireEvent.click(screen.getByLabelText('FLAG'));
    fillAddress('19');
    fireEvent.change(screen.getByLabelText('Tag (optional)'), { target: { value: '  Start button  ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Add block' }));
    expect(onAdd).toHaveBeenCalledWith({
      coilType: 'LOAD',
      inputType: 'FLAG',
      value: '19',
      tag: 'Start button',
    });
  });

  it('submits an OUTPUT draft with no input type', () => {
    const onAdd = vi.fn();
    render(<RawBlockForm onAdd={onAdd} />);
    fireEvent.click(screen.getByLabelText('Coil (OUTPUT)'));
    fillAddress('4');
    fireEvent.click(screen.getByRole('button', { name: 'Add block' }));
    expect(onAdd).toHaveBeenCalledWith({ coilType: 'OUTPUT', inputType: null, value: '4', tag: '' });
  });

  it('hides the input-type choice for an OUTPUT coil', () => {
    render(<RawBlockForm onAdd={() => {}} />);
    expect(screen.getByLabelText('WORD')).toBeInTheDocument();
    fireEvent.click(screen.getByLabelText('Coil (OUTPUT)'));
    expect(screen.queryByLabelText('WORD')).not.toBeInTheDocument();
  });

  it('keeps the address as a string, matching ColumnScreen.value', () => {
    const onAdd = vi.fn();
    render(<RawBlockForm onAdd={onAdd} />);
    fillAddress('007');
    fireEvent.click(screen.getByRole('button', { name: 'Add block' }));
    expect(onAdd.mock.calls[0][0].value).toBe('007');
  });

  it('will not submit an empty address', () => {
    const onAdd = vi.fn();
    render(<RawBlockForm onAdd={onAdd} />);
    expect(screen.getByRole('button', { name: 'Add block' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Add block' }));
    expect(onAdd).not.toHaveBeenCalled();
  });

  it('rejects a non-numeric address, which the compiler would reject anyway', () => {
    const onAdd = vi.fn();
    render(<RawBlockForm onAdd={onAdd} />);
    fillAddress('not-a-number');
    expect(screen.getByRole('alert')).toHaveTextContent('Address must be a whole number.');
    expect(screen.getByRole('button', { name: 'Add block' })).toBeDisabled();
    expect(onAdd).not.toHaveBeenCalled();
  });

  it('clears the address and tag after a successful add, ready for the next block', () => {
    render(<RawBlockForm onAdd={() => {}} />);
    fillAddress('19');
    fireEvent.change(screen.getByLabelText('Tag (optional)'), { target: { value: 'Start' } });
    fireEvent.click(screen.getByRole('button', { name: 'Add block' }));
    expect(screen.getByLabelText('Address')).toHaveValue('');
    expect(screen.getByLabelText('Tag (optional)')).toHaveValue('');
  });
});
