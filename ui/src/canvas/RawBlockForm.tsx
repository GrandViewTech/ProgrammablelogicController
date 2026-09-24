import { useState } from 'react';

export type RawCoilType = 'LOAD' | 'OUTPUT';
export type RawInputType = 'INPUT' | 'FLAG' | 'WORD';

export type RawBlockDraft = {
  coilType: RawCoilType;
  /** Only meaningful for a LOAD — an OUTPUT coil has no input kind. */
  inputType: RawInputType | null;
  /** The numeric address, kept as a string to match `ColumnScreen.value`. */
  value: string;
  tag: string;
};

/** The compiler parses `value` with `i32::from_str`; anything else is
 * `CompileError::InvalidValue`. Catching it here means a worker gets told at
 * the point of entry rather than via a compile error after the fact. */
const NUMERIC_ADDRESS = /^-?\d+$/;

/**
 * Places a raw contact (LOAD) or coil (OUTPUT) on the current rung.
 *
 * Engineer-mode only, and gated as such by its single call site in `App`:
 * worker mode is routine-only by product design (requirements §7.1 — "Worker
 * can only drag routines... cannot edit raw contacts/coils"). Until this
 * existed, routine injection from the palette was the *only* way to get a
 * block onto the canvas at all, in either mode.
 *
 * The form only describes a block; `App` owns where it lands and how it
 * combines, so a raw block appended to a non-empty rung goes through exactly
 * the same AND/OR/XOR + Invert picker a routine block does.
 */
export function RawBlockForm({ onAdd }: { onAdd: (draft: RawBlockDraft) => void }) {
  const [coilType, setCoilType] = useState<RawCoilType>('LOAD');
  const [inputType, setInputType] = useState<RawInputType>('INPUT');
  const [value, setValue] = useState('');
  const [tag, setTag] = useState('');

  const trimmedValue = value.trim();
  const valueIsValid = NUMERIC_ADDRESS.test(trimmedValue);

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    if (!valueIsValid) return;
    onAdd({
      coilType,
      inputType: coilType === 'LOAD' ? inputType : null,
      value: trimmedValue,
      tag: tag.trim(),
    });
    setValue('');
    setTag('');
  }

  return (
    <form className="raw-block-form" aria-label="Add a raw block" onSubmit={handleSubmit}>
      <fieldset className="raw-block-form__group">
        <legend>Block</legend>
        {(['LOAD', 'OUTPUT'] as const).map((candidate) => (
          <label key={candidate}>
            <input
              type="radio"
              name="raw-block-coil-type"
              value={candidate}
              checked={coilType === candidate}
              onChange={() => setCoilType(candidate)}
            />
            {candidate === 'LOAD' ? 'Contact (LOAD)' : 'Coil (OUTPUT)'}
          </label>
        ))}
      </fieldset>

      {/* An OUTPUT coil writes to the output bank; only a LOAD contact reads
          from one of the three input banks, so the choice is hidden rather
          than disabled when it cannot apply. */}
      {coilType === 'LOAD' && (
        <fieldset className="raw-block-form__group">
          <legend>Reads from</legend>
          {(['INPUT', 'FLAG', 'WORD'] as const).map((candidate) => (
            <label key={candidate}>
              <input
                type="radio"
                name="raw-block-input-type"
                value={candidate}
                checked={inputType === candidate}
                onChange={() => setInputType(candidate)}
              />
              {candidate}
            </label>
          ))}
        </fieldset>
      )}

      <label className="raw-block-form__field">
        Address
        <input
          type="text"
          inputMode="numeric"
          placeholder="e.g. 19"
          value={value}
          onChange={(e) => setValue(e.target.value)}
        />
      </label>

      <label className="raw-block-form__field">
        Tag (optional)
        <input type="text" placeholder="e.g. Start button" value={tag} onChange={(e) => setTag(e.target.value)} />
      </label>

      {trimmedValue.length > 0 && !valueIsValid && (
        <p className="raw-block-form__hint" role="alert">
          Address must be a whole number.
        </p>
      )}

      <button type="submit" disabled={!valueIsValid}>
        Add block
      </button>
    </form>
  );
}
