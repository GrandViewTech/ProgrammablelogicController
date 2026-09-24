use std::collections::HashMap;

use ladder_model::{CoilType, Combinator, ColumnScreen, InputType, Screen};

use crate::target::Target;
use crate::types::{CompileError, LoadCombinator, LoadKind, OutputType};

/// Named-output relay addresses: row-output name -> (byte, bit).
type RelayAddresses = HashMap<String, (u32, u32)>;

/// Byte offset where this compiler's own automatic relay allocation (named
/// row outputs first, then non-leading-group / XOR / routine scratch slots)
/// begins, instead of byte 0.
///
/// **Why not 0.** Both allocators below feed `legacy::find_param`, whose
/// `(index / 8, index % 8)` byte is what `Target8085::emit_relay_write` /
/// `emit_relay_read` render as the literal address `RLY512_+{byte}`. The
/// shipped routine library (`resources/routine/**.xml`) writes to literal
/// relay symbols of its own — `RLY512_519` (used as a `MOV DPTR,#...` byte
/// base) and bit-level symbols `RLY514, RLY517, RLY567, RLY598, RLY600`
/// (used as `SETB`/`CLR` operands) — that plausibly share this same address
/// family. **The exact symbol-to-(byte,bit) resolution the original 8085
/// assembler used for those `SETB`/`CLR` operands isn't fully recoverable
/// from source alone**: reading the raw `N - 512` as this scheme's byte
/// offset gives a maximum of 88 (`RLY600`); reading it as a bit index
/// needing `/8` to reach a byte offset gives a maximum of 11. Both readings
/// are far below the 100-byte start chosen here, so the reservation is safe
/// under either interpretation — but neither reading is asserted as
/// confirmed. Starting at byte 0 would have handed a worker's first named
/// row output the same physical byte region a shipped routine may already
/// drive — a silent, data-dependent miscompile either way.
/// (The library's other literals — `RLY33, RLY34, RLY35` — are far below
/// either reading of `RLY512`'s own range and use unrelated low-number
/// addressing, so they aren't a collision risk here regardless.)
///
/// **HEURISTIC RESERVATION — NOT A PROVEN-SAFE HARDWARE RANGE.** Byte 100 is
/// simply a documented margin above the highest routine-library offset
/// observed at the time of this fix (88). It is not derived from a hardware
/// memory map, and nothing here proves bytes 100+ are free, or even that
/// they exist, on real PLC hardware. A real fix needs either a
/// hardware-provided safe address range, or the routine library's own
/// addressing unified with this scheme so both draw from one allocator.
/// Flagged in the same spirit as design spec §6's caveat about the relay
/// write mnemonic needing hardware validation — stated honestly rather than
/// asserted as safe. Adding a routine to the library that writes above
/// `RLY512_+99` re-opens the collision this constant closes.
const RELAY_ALLOC_START_BYTE: u32 = 100;

/// `RELAY_ALLOC_START_BYTE` expressed in the flat index space both
/// allocators count in — `find_param` splits it back to
/// `(RELAY_ALLOC_START_BYTE, 0)`.
const RELAY_ALLOC_START_INDEX: i32 = (RELAY_ALLOC_START_BYTE * 8) as i32;

fn create_label(row_number: u32, column_number: u32) -> String {
    format!("LABEL_{row_number}_{column_number}")
}

/// The column immediately to the left of `column_index` within `columns`, or
/// `None` if it's the first entry. Slice-based sibling of
/// `RowScreen::previous` (requirements: `resolve_combinator`/
/// `is_jump_required` need to walk either a row's top-level columns or a
/// nested `group`'s own columns, so they operate on whatever slice they were
/// called with rather than always `row.columns`).
fn previous_in(columns: &[ColumnScreen], column_index: usize) -> Option<&ColumnScreen> {
    column_index.checked_sub(1).and_then(|i| columns.get(i))
}

/// Reads the combinator directly off the block (requirements §7.3) instead
/// of inferring it. The first column in `columns` has no combinator — this
/// applies equally to a row's top-level columns and to a nested `group`'s
/// own columns, since group members combine among themselves the same way
/// top-level columns do (requirements: nested expressions).
///
/// A *later* column with no combinator is malformed input, not a defaultable
/// case: falling back to `LoadCombinator::None` would emit a plain
/// `MOV C, ACC.n`, silently overwriting the accumulated carry and producing
/// wrong logic on real hardware. Requirements §7.3 makes the combinator
/// mandatory for every block after the first, so this is rejected instead.
fn resolve_combinator(
    columns: &[ColumnScreen],
    row_number: u32,
    column_index: usize,
    column: &ColumnScreen,
) -> Result<LoadCombinator, CompileError> {
    if previous_in(columns, column_index).is_none() {
        return Ok(LoadCombinator::None);
    }
    match column.combinator {
        Some(Combinator::And) => Ok(LoadCombinator::Series),
        Some(Combinator::Or) => Ok(LoadCombinator::Parallel),
        // Task 4b: XOR is no longer resolved here. Every call site that
        // could reach a non-leading XOR-combined column (every LOAD
        // sub-kind's dispatch in `evaluate_columns`, RowRef's dispatch, and
        // the group-dispatch branch) checks `column.combinator ==
        // Some(Combinator::Xor)` itself, before ever calling
        // `resolve_combinator`, and routes that case through
        // `Target::emit_xor`'s own stash/evaluate-fresh/read-back sequence
        // instead (see `emit_combined_load` and the group-dispatch branch
        // below). A *first-position* XOR column also never reaches this
        // match arm: the early return above for "no preceding sibling"
        // fires before `column.combinator` is ever inspected, regardless of
        // its value. So this arm is provably unreachable given the current
        // call sites — not a placeholder (the old "treat XOR as OR" TODO
        // from Task 1's panic-prevention fix is gone; Task 3 already
        // landed). It's kept only to make this match exhaustive and to fail
        // loudly — rather than silently miscompiling to OR semantics — if a
        // future call site is ever added that forgets to pre-filter XOR.
        Some(Combinator::Xor) => {
            unreachable!("Combinator::Xor is routed around resolve_combinator by every call site")
        }
        None => Err(CompileError::MissingCombinator { row: row_number, column: column.column_number }),
    }
}

/// True when `column` is combined with a preceding sibling via XOR — the
/// only case `emit_combined_load` and the group-dispatch branch route
/// around the normal single-instruction AND/OR/None combine, since 8085 has
/// no single "XOR carry" instruction. A first-position column (no preceding
/// sibling) is never routed this way, even if its nominal combinator is
/// XOR: XOR's identity element is the same as OR's — "nothing precedes it,
/// so just take the value" — matching how `resolve_combinator` already
/// returns `LoadCombinator::None` for any first-position column regardless
/// of its nominal combinator.
fn is_xor_combined(columns: &[ColumnScreen], column_index: usize, column: &ColumnScreen) -> bool {
    previous_in(columns, column_index).is_some() && column.combinator == Some(Combinator::Xor)
}

/// Bundles a column together with the slice it lives in and its index in
/// that slice — the same 4 values `resolve_combinator` and `is_xor_combined`
/// each already take individually — purely to keep `emit_combined_load`'s
/// own argument count under clippy's `too_many_arguments` threshold.
struct ColumnContext<'a> {
    columns: &'a [ColumnScreen],
    row_number: u32,
    column_index: usize,
    column: &'a ColumnScreen,
}

/// Emits either the normal single-instruction combinator load or, when this
/// leaf is XOR-combined with a preceding sibling (`is_xor_combined`), the
/// XOR-specific sequence: stash the running total to a scratch relay
/// address (`emit_relay_write`) — durable across the fresh evaluation's own
/// `MOVX`, unlike an accumulator bit (see `Target::emit_xor`'s doc comment
/// and task-3-report.md for why) — evaluate this leaf's own value fresh
/// into carry ignoring the outer combinator, apply `column.inverted` to
/// that fresh value (before combining, matching Task 3's inversion-ordering
/// fix for groups), read the stash back into the accumulator
/// (`emit_relay_read`, which doesn't touch carry), then `emit_xor` combines
/// the two. Shared by every LOAD sub-kind (Flag/Input/Word) and RowRef,
/// which all resolve their combinator against the same outer `columns`
/// slice the same way.
///
/// Returns `true` if it already applied `column.inverted` itself (XOR path
/// only), so the caller's shared trailing inversion check can skip
/// re-applying it.
fn emit_combined_load(
    ctx: ColumnContext<'_>,
    kind: LoadKind,
    input: i32,
    next_scratch_index: &mut i32,
    target: &dyn Target,
    buf: &mut Vec<String>,
) -> Result<bool, CompileError> {
    let ColumnContext { columns, row_number, column_index, column } = ctx;
    if is_xor_combined(columns, column_index, column) {
        let (scratch_byte, scratch_bit) = crate::legacy::find_param(*next_scratch_index);
        *next_scratch_index += 1;
        target.emit_relay_write(buf, scratch_byte, scratch_bit); // stash running total

        target.emit_load(buf, kind, input, LoadCombinator::None); // fresh value into carry
        if column.inverted {
            target.emit_not(buf); // invert fresh value, before combining
        }

        target.emit_relay_read(buf, scratch_byte); // old total -> ACC, carry untouched
        target.emit_xor(buf, scratch_bit); // carry := fresh XOR old total
        Ok(true)
    } else {
        let combinator = resolve_combinator(columns, row_number, column_index, column)?;
        target.emit_load(buf, kind, input, combinator);
        Ok(false)
    }
}

/// Ported from CompileService.isJumpRequired: true when the previous column
/// in `columns` is a LOAD.
fn is_jump_required(columns: &[ColumnScreen], column_index: usize) -> bool {
    matches!(previous_in(columns, column_index), Some(previous) if previous.coil_type == CoilType::Load)
}

/// GAP (spec §3): the original's findOutputType(NoNc) lookup was disconnected
/// in ColumnScreen (the nonc field was commented out, so CompileService always
/// called it with null). This is carried forward as a documented gap, not
/// fixed: NoNc is not modeled on ColumnScreen, and every call site here always
/// resolves to OutputType::None, matching the original's actual behavior.
fn find_output_type() -> OutputType {
    OutputType::None
}

/// Pre-pass (requirements: named row outputs / cross-row references): every
/// row with an `output_name` is assigned the next internal-relay address, in
/// row order. `legacy::find_param` reproduces the original's byte/bit split
/// exactly (see its doc comment), so an address computed here and later
/// re-split by a `Target` implementation's own `find_param` call lands on
/// the same (byte, bit).
///
/// Non-leading groups do NOT get a pre-pass address here (fix round 2 — see
/// task-3-report.md). `column_number` is only unique WITHIN whatever slice a
/// column lives in — a row's own top-level `columns`, or a group's own
/// separate `Vec<ColumnScreen>` — numbering restarts at 1 inside every
/// group. A key of `(row_number, column_number)` alone can therefore collide
/// between a non-leading group and a same-numbered non-leading group nested
/// inside it. Instead, scratch addresses are allocated lazily, inline,
/// during `evaluate_columns` itself, via a monotonically-increasing counter
/// (`next_scratch_index`) that `generate()` seeds starting from
/// `RELAY_ALLOC_START_INDEX + relay_addresses.len()` — guaranteeing scratch
/// indices never collide with named-output indices (which occupy
/// `RELAY_ALLOC_START_INDEX..RELAY_ALLOC_START_INDEX + relay_addresses.len()`)
/// — and threads through every recursive call by mutable reference.
/// Allocation then happens in the exact same depth-first order as
/// evaluation, and each non-leading group is allocated its slot exactly
/// once, at the moment it's about to be used, so no two groups can ever
/// share an index.
///
/// Both sequences start at `RELAY_ALLOC_START_INDEX`, not 0 — see that
/// constant's doc comment for the routine-library collision that motivates
/// it, and for why the chosen offset is an honestly-flagged heuristic rather
/// than a proven-safe hardware range.
fn assign_relay_addresses(screen: &Screen) -> RelayAddresses {
    let mut addresses = HashMap::new();
    let mut next_relay_index: i32 = RELAY_ALLOC_START_INDEX;
    for row in &screen.rows {
        if let Some(name) = &row.output_name {
            addresses.insert(name.clone(), crate::legacy::find_param(next_relay_index));
            next_relay_index += 1;
        }
    }
    addresses
}

/// Companion to `assign_relay_addresses`: which row number owns each named
/// output, so a `RowRef` can be checked for forward-reference (requirements:
/// a row may only reference a strictly earlier row's output).
fn assign_row_owners(screen: &Screen) -> HashMap<String, u32> {
    let mut owners = HashMap::new();
    for row in &screen.rows {
        if let Some(name) = &row.output_name {
            owners.insert(name.clone(), row.row_number);
        }
    }
    owners
}

/// Faithful ASM emission for one screen. Runs synchronously; the Tauri
/// command wrapping this (Task 9) is what makes it async off the UI thread
/// (requirements §6, item 2) — `generate()` itself has no I/O.
pub fn generate(screen: &Screen, target: &dyn Target) -> Result<String, CompileError> {
    let mut buf: Vec<String> = Vec::new();
    let relay_addresses = assign_relay_addresses(screen);
    let row_owners = assign_row_owners(screen);
    // Scratch indices for non-leading groups (and XOR / routine combines)
    // start right after the named-output indices already handed out above,
    // so the two address spaces never collide (see
    // `assign_relay_addresses`'s doc comment). Both are offset by
    // `RELAY_ALLOC_START_INDEX` to stay clear of the addresses the shipped
    // routine library writes to directly.
    let mut next_scratch_index: i32 = RELAY_ALLOC_START_INDEX + relay_addresses.len() as i32;

    for row in &screen.rows {
        let end_hit = evaluate_columns(
            &row.columns,
            row.row_number,
            &relay_addresses,
            &row_owners,
            &mut next_scratch_index,
            target,
            &mut buf,
        )?;
        if end_hit {
            // matches the original: nothing after End runs, in this row or
            // any later one — including this row's own output_name write,
            // since its expression didn't finish evaluating.
            break;
        }
        if let Some(name) = &row.output_name {
            let (byte, bit) =
                relay_addresses.get(name).expect("assigned in the pre-pass for every row.output_name");
            target.emit_relay_write(&mut buf, *byte, *bit);
        }
    }

    Ok(buf.join("\n"))
}

/// Returns `Ok(true)` if an `End` coil was encountered anywhere in this
/// slice (including inside a nested group) — the caller must stop
/// processing immediately: skip the rest of this row (including any
/// row-output write) and every row after it, matching the original's
/// "END coil early-exit" behavior (requirements §3) exactly, now that the
/// column walk is recursive instead of one flat loop with a labeled break.
fn evaluate_columns(
    columns: &[ColumnScreen],
    row_number: u32,
    relay_addresses: &RelayAddresses,
    row_owners: &HashMap<String, u32>,
    next_scratch_index: &mut i32,
    target: &dyn Target,
    buf: &mut Vec<String>,
) -> Result<bool, CompileError> {
    for (column_index, column) in columns.iter().enumerate() {
        if column.is_blank {
            continue;
        }
        if column.coil_type == CoilType::End {
            return Ok(true);
        }

        let label = create_label(row_number, column.column_number);

        if let Some(group_columns) = &column.group {
            // A group is evaluated recursively to get "the group's result"
            // onto the carry, then treated exactly like a leaf load for the
            // purposes of inversion below. If an End coil turns up inside
            // the group, propagate the stop signal immediately — don't
            // finish this group, don't apply this slot's inversion, don't
            // continue the row.
            //
            // Fix round 1: when this group has a preceding sibling, there's
            // already a running total in the carry that the group's own
            // fresh evaluation would otherwise silently clobber (its first
            // internal load resolves its OWN combinator as None and
            // overwrites the carry — it has no visibility into what came
            // before it in the OUTER slice). Stash the running total in a
            // memory-backed scratch relay bit before recursing, then after
            // the group returns, read that stash back and combine it with
            // the group's fresh result via THIS column's own combinator
            // (resolved against the outer `columns` slice) — exactly the
            // same mechanism a normal leaf load-with-combinator already
            // uses, just pointed at scratch memory instead of a real input.
            //
            // Fix round 2 (see task-3-report.md): the scratch address is now
            // allocated lazily, right here, from the shared
            // `next_scratch_index` counter — not looked up from a pre-pass
            // map keyed by `(row_number, column.column_number)`, which could
            // collide between a non-leading group and a same-numbered
            // non-leading group nested inside it (column numbering restarts
            // at 1 in every group's own `Vec<ColumnScreen>`). Allocating at
            // the moment of use, in strict depth-first evaluation order,
            // makes a collision structurally impossible.
            //
            // Fix round 2 also moves `inverted` handling here, scoped to the
            // group's OWN result: `emit_not` (if `column.inverted`) now runs
            // on the group's fresh result, before it's folded into the
            // running total via the outer combinator — matching design spec
            // §4 ("that result is complemented... *before* being folded into
            // the running total via the slot's combinator"). Previously a
            // single trailing `if column.inverted` ran after the combine
            // step for both branches, which for a non-leading group inverted
            // `running_total OP group_result` instead of
            // `running_total OP NOT(group_result)`.
            let has_preceding_sibling = previous_in(columns, column_index).is_some();
            if has_preceding_sibling {
                let (scratch_byte, scratch_bit) = crate::legacy::find_param(*next_scratch_index);
                *next_scratch_index += 1;
                target.emit_relay_write(buf, scratch_byte, scratch_bit);

                if evaluate_columns(group_columns, row_number, relay_addresses, row_owners, next_scratch_index, target, buf)? {
                    return Ok(true);
                }

                if column.inverted {
                    target.emit_not(buf); // invert the group's OWN result, before combining
                }

                // Task 4b: 8085 has no single "XOR carry" instruction, so a
                // non-leading group combined via XOR can't use the same
                // single-instruction `emit_load(..., combinator)` combine
                // AND/OR groups use above. Everything up to this point
                // (stash, recurse, invert) is identical for XOR — only the
                // final combine differs: read the stash back into the
                // accumulator (carry, holding the group's fresh result,
                // stays untouched) and let `emit_xor` do the combining.
                if column.combinator == Some(Combinator::Xor) {
                    target.emit_relay_read(buf, scratch_byte);
                    target.emit_xor(buf, scratch_bit);
                } else {
                    let resolved_combinator = resolve_combinator(columns, row_number, column_index, column)?;
                    let scratch_input = (scratch_byte * 8 + scratch_bit) as i32;
                    target.emit_load(buf, LoadKind::Flag, scratch_input, resolved_combinator);
                }
            } else {
                if evaluate_columns(group_columns, row_number, relay_addresses, row_owners, next_scratch_index, target, buf)? {
                    return Ok(true);
                }

                if column.inverted {
                    // Group is first in its slice: its result IS the running
                    // total so far, so invert it directly — nothing to
                    // combine it with yet.
                    target.emit_not(buf);
                }
            }
        } else {
            let mut inversion_applied = false;
            match column.coil_type {
                CoilType::Load => {
                    let input = parse_value(row_number, column)?;
                    match column.input_type {
                        Some(InputType::Flag) => {
                            // GAP (spec §3): PreferenceScreen-backed min-value bounds for
                            // FLAG inputs aren't ported (no settings store exists yet) —
                            // the original's `min - input` offset is not applied.
                            let ctx = ColumnContext { columns, row_number, column_index, column };
                            inversion_applied =
                                emit_combined_load(ctx, LoadKind::Flag, input, next_scratch_index, target, buf)?;
                        }
                        Some(InputType::Input) => {
                            let ctx = ColumnContext { columns, row_number, column_index, column };
                            inversion_applied =
                                emit_combined_load(ctx, LoadKind::Input, input, next_scratch_index, target, buf)?;
                        }
                        Some(InputType::Word) => {
                            let ctx = ColumnContext { columns, row_number, column_index, column };
                            inversion_applied =
                                emit_combined_load(ctx, LoadKind::Word, input, next_scratch_index, target, buf)?;
                        }
                        Some(InputType::Output) => {
                            target.emit_output(buf, input, find_output_type(), &label);
                        }
                        None => {}
                    }
                }
                CoilType::Output => {
                    let value = parse_value(row_number, column)?;
                    target.emit_output(buf, value, find_output_type(), &label);
                }
                CoilType::Routine => {
                    // A Routine column used to consult neither `combinator`
                    // nor the right inversion point: AND/OR/XOR all produced
                    // byte-identical output (its ASM was spliced in and
                    // whatever carry that left behind simply became the new
                    // running total, discarding the preceding one), while the
                    // shared trailing `if column.inverted` complemented that
                    // post-splice carry — a different operation from "invert
                    // this block's own result before combining it".
                    //
                    // The fix reuses the exact stash/evaluate-fresh/combine
                    // pattern the non-leading-group path above (and the
                    // XOR-leaf path in `emit_combined_load`) already
                    // established, with the routine's own injected ASM
                    // playing the "evaluate this operand fresh" role:
                    //
                    //   1. stash the running total (`emit_relay_write`)
                    //   2. emit the routine's ASM, JNC-wrap unchanged
                    //   3. `emit_not` here, on the routine's own fresh result
                    //   4. read the stash back and combine via this column's
                    //      own resolved combinator
                    //
                    // Step 1 is safe to place before the JNC wrap:
                    // `emit_relay_write` is a pure ACC/DPTR read-modify-write
                    // that never touches carry, so the JNC still tests the
                    // same running total it tested before this change.
                    //
                    // Backward compatibility: a *non-first* Routine column
                    // with no combinator at all is left on the legacy path
                    // (splice only, no stash/combine) rather than becoming a
                    // `MissingCombinator` error. Every routine block the UI
                    // appends to a non-empty row carries an explicit
                    // combinator, so this only affects screens saved before
                    // the picker existed — turning those into hard compile
                    // errors would be a regression, not a fix. The emitted
                    // sequence for that legacy case is byte-for-byte what it
                    // was before (the inversion below lands in exactly the
                    // same position the shared trailing check used to put
                    // it, since no combine step follows).
                    let combines_with_previous =
                        previous_in(columns, column_index).is_some() && column.combinator.is_some();
                    let stash = if combines_with_previous {
                        let (scratch_byte, scratch_bit) = crate::legacy::find_param(*next_scratch_index);
                        *next_scratch_index += 1;
                        target.emit_relay_write(buf, scratch_byte, scratch_bit);
                        Some((scratch_byte, scratch_bit))
                    } else {
                        None
                    };

                    let jump = is_jump_required(columns, column_index);
                    if jump {
                        target.emit_jnc(buf, &label);
                    }
                    let asm = column.rendered_asm.as_deref().ok_or(CompileError::MissingRenderedAsm {
                        row: row_number,
                        column: column.column_number,
                    })?;
                    buf.push(asm.to_string());
                    if jump {
                        target.emit_label(buf, &label);
                    }

                    // Applies to the routine's OWN result, before combining —
                    // matching design spec §4's ordering and the group path
                    // above. For a first-position routine (nothing to combine
                    // with) this is still the right and only place for it.
                    if column.inverted {
                        target.emit_not(buf);
                    }
                    inversion_applied = true;

                    if let Some((scratch_byte, scratch_bit)) = stash {
                        if column.combinator == Some(Combinator::Xor) {
                            target.emit_relay_read(buf, scratch_byte);
                            target.emit_xor(buf, scratch_bit);
                        } else {
                            let resolved_combinator =
                                resolve_combinator(columns, row_number, column_index, column)?;
                            let scratch_input = (scratch_byte * 8 + scratch_bit) as i32;
                            target.emit_load(buf, LoadKind::Flag, scratch_input, resolved_combinator);
                        }
                    }
                }
                CoilType::RowRef => {
                    let name = column.row_ref_name.as_deref().unwrap_or_default();
                    if let Some(&owner_row) = row_owners.get(name) {
                        if owner_row >= row_number {
                            return Err(CompileError::ForwardRowReference {
                                row: row_number,
                                column: column.column_number,
                                name: name.to_string(),
                            });
                        }
                    }
                    let (byte, bit) = relay_addresses.get(name).copied().ok_or_else(|| {
                        CompileError::UnknownRowReference {
                            row: row_number,
                            column: column.column_number,
                            name: name.to_string(),
                        }
                    })?;
                    // Reconstruct a single `input` consistent with how
                    // `find_param` would split it back apart, so the round
                    // trip through a `Target`'s own `find_param` call (e.g.
                    // `Target8085`) lands on the same (byte, bit) address
                    // `assign_relay_addresses` computed.
                    let input = (byte * 8 + bit) as i32;
                    let ctx = ColumnContext { columns, row_number, column_index, column };
                    inversion_applied =
                        emit_combined_load(ctx, LoadKind::Flag, input, next_scratch_index, target, buf)?;
                }
                _ => {
                    // LABEL, JUMP, LINE, LEFT_LINK, RIGHT_LINK, PARALLEL, DEFAULT,
                    // DELETE are UI-only markers in the original — CompileService's
                    // traversal never emits ASM for them either.
                }
            }

            // Inversion applies after obtaining this leaf's value, before
            // folding into the running total (design spec: NOT is orthogonal
            // to AND/OR/XOR, not a 4th `Combinator` value).
            //
            // Fix round 2 note (task-3-report.md): this is unchanged from
            // before that fix round — same position, same behavior, same
            // pre-existing issue for the *leaf* case (it applies `emit_not`
            // to whatever's in carry after `emit_load`'s combined
            // load-and-combine, not to the leaf's own raw value before
            // combining). Fixing that is out of scope here; it's a larger
            // piece of work involving `emit_load`'s single-instruction
            // load+combine semantics. Only the *group* case's inversion
            // ordering (handled above) was in scope for this round.
            //
            // Task 4b: `emit_combined_load`'s XOR path already applies
            // `column.inverted` itself, to the leaf's own fresh value before
            // combining (matching the group fix above, not this pre-existing
            // AND/OR gap) — `inversion_applied` guards against inverting
            // twice for that path. The Routine arm above sets the same flag
            // for the same reason.
            if column.inverted && !inversion_applied {
                target.emit_not(buf);
            }
        }
    }
    Ok(false)
}

fn parse_value(row_number: u32, column: &ColumnScreen) -> Result<i32, CompileError> {
    column.value.parse::<i32>().map_err(|_| CompileError::InvalidValue {
        row: row_number,
        column: column.column_number,
        value: column.value.clone(),
    })
}

#[cfg(test)]
mod tests {
    use super::*;
    use ladder_model::{CoilType, ColumnScreen, RoutineOrigin, RowScreen};
    use std::cell::RefCell;

    struct RecordingTarget {
        calls: RefCell<Vec<String>>,
    }

    impl RecordingTarget {
        fn new() -> Self {
            Self { calls: RefCell::new(Vec::new()) }
        }
    }

    impl Target for RecordingTarget {
        fn emit_load(&self, buf: &mut Vec<String>, kind: LoadKind, input: i32, combinator: LoadCombinator) {
            let line = format!("LOAD {kind:?} {input} {combinator:?}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_output(&self, buf: &mut Vec<String>, value: i32, output_type: OutputType, label: &str) {
            let line = format!("OUTPUT {value} {output_type:?} {label}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_jnc(&self, buf: &mut Vec<String>, label: &str) {
            let line = format!("JNC {label}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_label(&self, buf: &mut Vec<String>, label: &str) {
            let line = format!("{label}:");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_not(&self, buf: &mut Vec<String>) {
            let line = "NOT".to_string();
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_xor(&self, buf: &mut Vec<String>, bit: u32) {
            let line = format!("XOR {bit}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_relay_write(&self, buf: &mut Vec<String>, byte: u32, bit: u32) {
            let line = format!("RELAY_WRITE {byte} {bit}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
        fn emit_relay_read(&self, buf: &mut Vec<String>, byte: u32) {
            let line = format!("RELAY_READ {byte}");
            self.calls.borrow_mut().push(line.clone());
            buf.push(line);
        }
    }

    fn column(coil_type: CoilType) -> ColumnScreen {
        ColumnScreen {
            row_number: 1,
            column_number: 1,
            coil_type,
            input_type: None,
            value: "0".into(),
            tag: String::new(),
            comment: String::new(),
            routine_origin: None,
            rendered_asm: None,
            combinator: None,
            is_blank: false,
            inverted: false,
            group: None,
            row_ref_name: None,
        }
    }

    #[test]
    fn end_coil_stops_traversal_before_later_rows() {
        let screen = Screen {
            rows: vec![
                RowScreen { row_number: 1, columns: vec![column(CoilType::End)], output_name: None },
                RowScreen {
                    row_number: 2,
                    columns: vec![ColumnScreen { input_type: Some(InputType::Input), ..column(CoilType::Load) }],
                    output_name: None,
                },
            ],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "");
        assert!(target.calls.borrow().is_empty());
    }

    #[test]
    fn load_input_emits_with_none_combinator_when_first_on_row() {
        let mut c = column(CoilType::Load);
        c.input_type = Some(InputType::Input);
        c.value = "19".into();
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![c], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 19 None");
    }

    #[test]
    fn second_block_uses_explicit_combinator_not_inference() {
        let mut first = column(CoilType::Load);
        first.input_type = Some(InputType::Input);
        first.value = "1".into();
        let mut second = column(CoilType::Load);
        second.column_number = 2;
        second.input_type = Some(InputType::Input);
        second.value = "2".into();
        second.combinator = Some(Combinator::Or);
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![first, second], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 1 None\nLOAD Input 2 Parallel");
    }

    #[test]
    fn non_leading_leaf_xor_combinator_uses_the_stash_evaluate_read_back_emit_xor_pattern() {
        // Task 4b: `Combinator::Xor` used to compile as plain OR (a
        // placeholder from Task 1's panic-prevention fix, kept until this
        // task wired real XOR codegen into the live path). This proves
        // real XOR output now — the stash/evaluate-fresh/read-back/
        // `emit_xor` sequence, not a plain `LOAD ... Parallel` call.
        let mut first = column(CoilType::Load);
        first.input_type = Some(InputType::Input);
        first.value = "1".into();
        let mut second = column(CoilType::Load);
        second.column_number = 2;
        second.input_type = Some(InputType::Input);
        second.value = "2".into();
        second.combinator = Some(Combinator::Xor);
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![first, second], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        // No named outputs, so the scratch counter starts at
        // RELAY_ALLOC_START_INDEX -> (byte 100, bit 0). The running total (first's result) is stashed there
        // before second's own value is loaded fresh (ignoring the outer
        // combinator, `LoadCombinator::None`), then the stash is read back
        // and combined via `emit_xor`.
        assert_eq!(
            out,
            "LOAD Input 1 None\nRELAY_WRITE 100 0\nLOAD Input 2 None\nRELAY_READ 100\nXOR 0"
        );
    }

    #[test]
    fn first_position_xor_combinator_needs_no_special_handling() {
        // XOR's identity element is the same as OR's: "nothing precedes it,
        // so just take the value." A first-position column with a nominal
        // XOR combinator must NOT trigger the stash/read-back sequence —
        // it should compile exactly like a first-position AND/OR/no-op
        // column, matching how `resolve_combinator` already returns
        // `LoadCombinator::None` regardless of the nominal combinator.
        let mut only = column(CoilType::Load);
        only.input_type = Some(InputType::Input);
        only.value = "1".into();
        only.combinator = Some(Combinator::Xor);
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![only], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 1 None");
    }

    #[test]
    fn second_block_without_combinator_is_a_compile_error() {
        // Requirements §7.3: every block after the first carries an explicit
        // combinator. Defaulting to `None` here would emit `MOV C, ACC.n`,
        // clobbering the accumulated carry instead of AND/OR-ing into it.
        let mut first = column(CoilType::Load);
        first.input_type = Some(InputType::Input);
        first.value = "1".into();
        let mut second = column(CoilType::Load);
        second.column_number = 2;
        second.input_type = Some(InputType::Input);
        second.value = "2".into();
        second.combinator = None;
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![first, second], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(err, CompileError::MissingCombinator { row: 1, column: 2 });
    }

    #[test]
    fn routine_after_load_wraps_with_jnc_and_label() {
        let mut load = column(CoilType::Load);
        load.input_type = Some(InputType::Input);
        load.value = "1".into();
        let mut routine = column(CoilType::Routine);
        routine.column_number = 2;
        routine.routine_origin = Some(RoutineOrigin {
            routine_name: "BIT RESET".into(),
            description: "Resets the bit".into(),
            values: Default::default(),
        });
        routine.rendered_asm = Some("MOV DPTR,#RLY512_519+".into());
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![load, routine], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nJNC LABEL_1_2\nMOV DPTR,#RLY512_519+\nLABEL_1_2:"
        );
    }

    #[test]
    fn routine_without_rendered_asm_is_a_compile_error() {
        let mut routine = column(CoilType::Routine);
        routine.rendered_asm = None;
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![routine], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(err, CompileError::MissingRenderedAsm { row: 1, column: 1 });
    }

    #[test]
    fn invalid_value_is_a_compile_error_not_a_panic() {
        let mut c = column(CoilType::Output);
        c.value = "not-a-number".into();
        let screen = Screen {
            rows: vec![RowScreen { row_number: 3, columns: vec![c], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(
            err,
            CompileError::InvalidValue { row: 3, column: 1, value: "not-a-number".into() }
        );
    }

    #[test]
    fn a_group_is_evaluated_and_its_result_combines_like_a_leaf() {
        // (raw_a AND raw_b) OR raw_c — the parenthesized pair is a `group` on
        // the second top-level column; group members combine among themselves
        // the same way top-level columns do (raw_b's combinator is AND,
        // relative to raw_a, both inside the group).
        let mut group_head = column(CoilType::Load);
        group_head.column_number = 1;
        group_head.input_type = Some(InputType::Input);
        group_head.value = "1".into();

        let mut group_tail = column(CoilType::Load);
        group_tail.column_number = 2;
        group_tail.input_type = Some(InputType::Input);
        group_tail.value = "2".into();
        group_tail.combinator = Some(Combinator::And);

        let mut grouped = column(CoilType::Load);
        grouped.column_number = 1;
        grouped.input_type = Some(InputType::Input);
        grouped.value = "0".into(); // unused when `group` is Some — the group's own columns are what's evaluated
        grouped.group = Some(vec![group_head, group_tail]);

        let mut raw_c = column(CoilType::Load);
        raw_c.column_number = 2;
        raw_c.input_type = Some(InputType::Input);
        raw_c.value = "3".into();
        raw_c.combinator = Some(Combinator::Or);

        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![grouped, raw_c], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nLOAD Input 2 Series\nLOAD Input 3 Parallel"
        );
    }

    #[test]
    fn a_non_leading_group_combines_with_the_running_total_via_its_own_combinator() {
        // raw_a OR (b AND c) — `grouped` is the SECOND top-level column, so
        // there's already a running total (raw_a's) in the carry when the
        // group is reached. The group's result must combine with that
        // running total via `grouped`'s OWN combinator (Or), not silently
        // overwrite it. Confirmed bug scenario from the Task 3 review.
        let mut raw_a = column(CoilType::Load);
        raw_a.column_number = 1;
        raw_a.input_type = Some(InputType::Input);
        raw_a.value = "1".into();

        let mut group_head = column(CoilType::Load);
        group_head.column_number = 1;
        group_head.input_type = Some(InputType::Input);
        group_head.value = "2".into();

        let mut group_tail = column(CoilType::Load);
        group_tail.column_number = 2;
        group_tail.input_type = Some(InputType::Input);
        group_tail.value = "3".into();
        group_tail.combinator = Some(Combinator::And);

        let mut grouped = column(CoilType::Load);
        grouped.column_number = 2;
        grouped.input_type = Some(InputType::Input);
        grouped.value = "0".into(); // unused when `group` is Some
        grouped.combinator = Some(Combinator::Or);
        grouped.group = Some(vec![group_head, group_tail]);

        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![raw_a, grouped], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nRELAY_WRITE 100 0\nLOAD Input 2 None\nLOAD Input 3 Series\nLOAD Flag 800 Parallel"
        );
    }

    #[test]
    fn a_non_leading_group_combined_via_xor_uses_the_stash_read_back_emit_xor_pattern() {
        // Task 4b: same shape as
        // `a_non_leading_group_combines_with_the_running_total_via_its_own_combinator`
        // above, but `grouped`'s own combinator is Xor. The stash/recurse
        // steps are identical to the AND/OR case (same mechanism Task 3
        // proved out) — only the final combine differs: a read-back
        // (`RELAY_READ`) plus `emit_xor`, not a single `LOAD Flag ...
        // Parallel`/`Series` call, proving this is real XOR codegen, not
        // the old OR-equivalent placeholder.
        let mut raw_a = column(CoilType::Load);
        raw_a.column_number = 1;
        raw_a.input_type = Some(InputType::Input);
        raw_a.value = "1".into();

        let mut group_head = column(CoilType::Load);
        group_head.column_number = 1;
        group_head.input_type = Some(InputType::Input);
        group_head.value = "2".into();

        let mut group_tail = column(CoilType::Load);
        group_tail.column_number = 2;
        group_tail.input_type = Some(InputType::Input);
        group_tail.value = "3".into();
        group_tail.combinator = Some(Combinator::And);

        let mut grouped = column(CoilType::Load);
        grouped.column_number = 2;
        grouped.input_type = Some(InputType::Input);
        grouped.value = "0".into(); // unused when `group` is Some
        grouped.combinator = Some(Combinator::Xor);
        grouped.group = Some(vec![group_head, group_tail]);

        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![raw_a, grouped], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nRELAY_WRITE 100 0\nLOAD Input 2 None\nLOAD Input 3 Series\nRELAY_READ 100\nXOR 0"
        );
    }

    #[test]
    fn inverted_non_leading_leaf_xor_inverts_its_own_fresh_value_before_combining_once() {
        // Mirrors `inverted_non_leading_group_inverts_its_own_result_before_combining`
        // for the leaf-XOR path: `column.inverted` must apply to this leaf's
        // own fresh value (between the fresh LOAD and the read-back/XOR
        // combine), not after the XOR combine, and must not be applied a
        // second time by the shared trailing inversion check
        // (`xor_inversion_applied` guards that).
        let mut raw_a = column(CoilType::Load);
        raw_a.column_number = 1;
        raw_a.input_type = Some(InputType::Input);
        raw_a.value = "1".into();

        let mut second = column(CoilType::Load);
        second.column_number = 2;
        second.input_type = Some(InputType::Input);
        second.value = "2".into();
        second.combinator = Some(Combinator::Xor);
        second.inverted = true;

        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![raw_a, second], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nRELAY_WRITE 100 0\nLOAD Input 2 None\nNOT\nRELAY_READ 100\nXOR 0"
        );
    }

    #[test]
    fn inverted_flag_emits_not_before_folding_into_the_running_total() {
        let mut raw = column(CoilType::Load);
        raw.input_type = Some(InputType::Input);
        raw.value = "1".into();
        raw.inverted = true;
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![raw], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "LOAD Input 1 None\nNOT");
    }

    #[test]
    fn nested_non_leading_groups_get_distinct_scratch_addresses() {
        // Fix round 2, Finding 1: a non-leading group (`outer_grouped`, at
        // top-level column_number = 2) that itself contains its OWN
        // non-leading group (`inner_grouped`, at column_number = 2 within
        // `outer_grouped`'s own group vector — numbering restarts inside
        // every group). Under the pre-pass/lookup approach these two used
        // to share the key (row_number=1, column_number=2) and silently
        // collide; the lazy counter-based allocator must hand them distinct
        // addresses instead.
        let mut raw_a = column(CoilType::Load);
        raw_a.column_number = 1;
        raw_a.input_type = Some(InputType::Input);
        raw_a.value = "1".into();

        let mut inner_raw = column(CoilType::Load);
        inner_raw.column_number = 1;
        inner_raw.input_type = Some(InputType::Input);
        inner_raw.value = "2".into();

        let mut deep_a = column(CoilType::Load);
        deep_a.column_number = 1;
        deep_a.input_type = Some(InputType::Input);
        deep_a.value = "3".into();

        let mut deep_b = column(CoilType::Load);
        deep_b.column_number = 2;
        deep_b.input_type = Some(InputType::Input);
        deep_b.value = "4".into();
        deep_b.combinator = Some(Combinator::And);

        let mut inner_grouped = column(CoilType::Load);
        inner_grouped.column_number = 2; // same column_number as outer_grouped below
        inner_grouped.combinator = Some(Combinator::And);
        inner_grouped.group = Some(vec![deep_a, deep_b]);

        let mut outer_grouped = column(CoilType::Load);
        outer_grouped.column_number = 2;
        outer_grouped.combinator = Some(Combinator::Or);
        outer_grouped.group = Some(vec![inner_raw, inner_grouped]);

        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![raw_a, outer_grouped], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        let calls = target.calls.borrow();
        let relay_writes: Vec<&String> = calls.iter().filter(|line| line.starts_with("RELAY_WRITE")).collect();
        assert_eq!(relay_writes.len(), 2, "expected two distinct scratch stashes, got {relay_writes:?}");
        assert_ne!(relay_writes[0], relay_writes[1], "the two non-leading groups must not share a scratch address");
        assert_eq!(
            out,
            "LOAD Input 1 None\n\
             RELAY_WRITE 100 0\n\
             LOAD Input 2 None\n\
             RELAY_WRITE 100 1\n\
             LOAD Input 3 None\n\
             LOAD Input 4 Series\n\
             LOAD Flag 801 Series\n\
             LOAD Flag 800 Parallel"
        );
    }

    #[test]
    fn inverted_non_leading_group_inverts_its_own_result_before_combining() {
        // Fix round 2, Finding 2: `grouped` is the second top-level column
        // (a non-leading group), combinator = Or, inverted = true, wrapping
        // a single leaf `b`. The spec (§4) requires the group's OWN result
        // be complemented BEFORE folding into the running total — i.e. the
        // emitted order must be: stash write, b's load, NOT, then the final
        // combine-read (LOAD Flag ... Parallel) — not NOT AFTER the combine.
        let mut raw_a = column(CoilType::Load);
        raw_a.column_number = 1;
        raw_a.input_type = Some(InputType::Input);
        raw_a.value = "1".into();

        let mut b = column(CoilType::Load);
        b.column_number = 1;
        b.input_type = Some(InputType::Input);
        b.value = "5".into();

        let mut grouped = column(CoilType::Load);
        grouped.column_number = 2;
        grouped.combinator = Some(Combinator::Or);
        grouped.inverted = true;
        grouped.group = Some(vec![b]);

        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![raw_a, grouped], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\nRELAY_WRITE 100 0\nLOAD Input 5 None\nNOT\nLOAD Flag 800 Parallel"
        );
    }

    #[test]
    fn row_ref_reads_a_prior_row_s_named_output() {
        let mut named_row_output = column(CoilType::Load);
        named_row_output.input_type = Some(InputType::Input);
        named_row_output.value = "1".into();

        let mut row_ref = column(CoilType::RowRef);
        row_ref.row_ref_name = Some("Conveyor Running".into());

        let screen = Screen {
            rows: vec![
                RowScreen { row_number: 1, columns: vec![named_row_output], output_name: Some("Conveyor Running".into()) },
                RowScreen { row_number: 2, columns: vec![row_ref], output_name: None },
            ],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        // Row 1: loads its own column, then writes the result to the relay
        // address assigned to "Conveyor Running" (the first named output, so
        // relay index RELAY_ALLOC_START_INDEX -> byte 100, bit 0). Row 2: reads that same address back
        // as a Flag-style load (RowRef reuses LoadKind::Flag — no new emission
        // path needed for reading).
        assert_eq!(
            out,
            "LOAD Input 1 None\nRELAY_WRITE 100 0\nLOAD Flag 800 None"
        );
    }

    #[test]
    fn unknown_row_reference_name_is_a_compile_error() {
        let mut row_ref = column(CoilType::RowRef);
        row_ref.row_ref_name = Some("Nonexistent".into());
        let screen = Screen {
            rows: vec![RowScreen { row_number: 1, columns: vec![row_ref], output_name: None }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(err, CompileError::UnknownRowReference { row: 1, column: 1, name: "Nonexistent".into() });
    }

    #[test]
    fn referencing_a_row_that_is_not_earlier_is_a_compile_error() {
        let mut row_ref = column(CoilType::RowRef);
        row_ref.row_ref_name = Some("Later".into());
        let mut later_output = column(CoilType::Load);
        later_output.input_type = Some(InputType::Input);
        later_output.value = "1".into();
        let screen = Screen {
            rows: vec![
                RowScreen { row_number: 1, columns: vec![row_ref], output_name: None },
                RowScreen { row_number: 2, columns: vec![later_output], output_name: Some("Later".into()) },
            ],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let err = generate(&screen, &target).unwrap_err();
        assert_eq!(err, CompileError::ForwardRowReference { row: 1, column: 1, name: "Later".into() });
    }

    // ------------------------------------------------------------------
    // Fix 4: relay-address allocation must start clear of the addresses the
    // shipped routine library already writes to directly.
    // ------------------------------------------------------------------

    /// Every `RELAY_WRITE {byte} {bit}` / `RELAY_READ {byte}` line the target
    /// recorded, reduced to its byte, so a test can assert the whole
    /// allocation run stayed inside the reserved range.
    fn recorded_relay_bytes(target: &RecordingTarget) -> Vec<u32> {
        target
            .calls
            .borrow()
            .iter()
            .filter_map(|line| {
                let rest = line
                    .strip_prefix("RELAY_WRITE ")
                    .or_else(|| line.strip_prefix("RELAY_READ "))?;
                rest.split_whitespace().next()?.parse::<u32>().ok()
            })
            .collect()
    }

    #[test]
    fn named_output_and_scratch_addresses_start_clear_of_the_routine_library_range() {
        // The shipped routine library writes literal `RLY512_+n` addresses at
        // byte offsets up to 88 (RLY600). Allocation used to start at byte 0,
        // handing the very first named row output the same physical bit
        // `RLY512` — a silent collision. Both allocators now start at
        // `RELAY_ALLOC_START_BYTE` (100).
        //
        // This screen exercises both allocators at once: row 1 has a named
        // output (pre-pass allocator) and row 2 contains a non-leading group
        // (lazy scratch allocator).
        let mut named = column(CoilType::Load);
        named.input_type = Some(InputType::Input);
        named.value = "1".into();

        let mut raw_a = column(CoilType::Load);
        raw_a.row_number = 2;
        raw_a.column_number = 1;
        raw_a.input_type = Some(InputType::Input);
        raw_a.value = "2".into();

        let mut inner = column(CoilType::Load);
        inner.input_type = Some(InputType::Input);
        inner.value = "3".into();

        let mut grouped = column(CoilType::Load);
        grouped.row_number = 2;
        grouped.column_number = 2;
        grouped.combinator = Some(Combinator::And);
        grouped.group = Some(vec![inner]);

        let screen = Screen {
            rows: vec![
                RowScreen { row_number: 1, columns: vec![named], output_name: Some("Stage 1".into()) },
                RowScreen { row_number: 2, columns: vec![raw_a, grouped], output_name: None },
            ],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        generate(&screen, &target).unwrap();

        let bytes = recorded_relay_bytes(&target);
        assert!(!bytes.is_empty(), "expected the screen to allocate at least one relay address");
        for byte in &bytes {
            assert!(
                *byte >= RELAY_ALLOC_START_BYTE,
                "relay byte {byte} is below the reserved start {RELAY_ALLOC_START_BYTE}; \
                 it can collide with the shipped routine library's own RLY512_+n writes"
            );
        }
        // Concretely: the named output takes index 800 -> (byte 100, bit 0),
        // and the non-leading group's scratch takes the next index, 801 ->
        // (byte 100, bit 1).
        assert_eq!(bytes[0], 100);
    }

    #[test]
    fn relay_alloc_start_byte_clears_the_highest_observed_routine_library_offset() {
        // `RLY600` is the highest literal relay symbol in the routine library
        // that plausibly falls in the `RLY512_+n` family. Its own
        // symbol-to-(byte,bit) resolution isn't fully recoverable from source
        // alone (see the doc comment on `RELAY_ALLOC_START_BYTE`), so this
        // checks both plausible readings rather than asserting one as fact:
        // the raw `600 - 512 = 88` reading, and the `/8`-for-a-true-byte-offset
        // reading (`88 / 8 = 11`). `RELAY_ALLOC_START_BYTE` must clear the
        // larger of the two to be safe under either interpretation.
        const HIGHEST_OBSERVED_ROUTINE_LIBRARY_RAW_OFFSET: u32 = 88;
        const HIGHEST_OBSERVED_ROUTINE_LIBRARY_BYTE_OFFSET: u32 =
            HIGHEST_OBSERVED_ROUTINE_LIBRARY_RAW_OFFSET / 8;
        // `assert!` on two compile-time constants trips clippy's
        // `assertions-on-constants` lint under `--all-targets`; wrapping it in
        // a `const {}` block makes the check happen at compile time instead,
        // which is what the lint is steering toward anyway.
        const {
            assert!(RELAY_ALLOC_START_BYTE > HIGHEST_OBSERVED_ROUTINE_LIBRARY_RAW_OFFSET);
            assert!(RELAY_ALLOC_START_BYTE > HIGHEST_OBSERVED_ROUTINE_LIBRARY_BYTE_OFFSET);
        }
        assert_eq!(crate::legacy::find_param(RELAY_ALLOC_START_INDEX), (RELAY_ALLOC_START_BYTE, 0));
    }

    // ------------------------------------------------------------------
    // Fix 5: a Routine column honors its own combinator, and applies
    // inversion to its own fresh result before combining.
    // ------------------------------------------------------------------

    /// A routine column whose ASM is already rendered — the shape every
    /// Fix 5/6 test below starts from.
    fn routine_column(column_number: u32) -> ColumnScreen {
        let mut routine = column(CoilType::Routine);
        routine.column_number = column_number;
        routine.routine_origin = Some(RoutineOrigin {
            routine_name: "BIT RESET".into(),
            description: "Resets the bit".into(),
            values: Default::default(),
        });
        routine.rendered_asm = Some("ROUTINE_ASM".into());
        routine
    }

    fn leading_load() -> ColumnScreen {
        let mut load = column(CoilType::Load);
        load.column_number = 1;
        load.input_type = Some(InputType::Input);
        load.value = "1".into();
        load
    }

    fn one_row(columns: Vec<ColumnScreen>) -> Screen {
        Screen {
            rows: vec![RowScreen { row_number: 1, columns, output_name: None }],
            end_row_number: None,
            end_column_number: None,
        }
    }

    #[test]
    fn non_leading_routine_combined_via_or_stashes_then_reads_back_with_its_own_combinator() {
        // Before this fix a Routine column never consulted `combinator` at
        // all: AND, OR and XOR produced byte-identical output, and the
        // preceding running total was silently discarded by whatever carry
        // the routine's own ASM left behind. The emitted order must now be
        // stash -> [JNC] routine ASM [LABEL] -> combine-read, reusing the
        // same pattern the non-leading-group path established.
        let mut routine = routine_column(2);
        routine.combinator = Some(Combinator::Or);
        let target = RecordingTarget::new();
        let out = generate(&one_row(vec![leading_load(), routine]), &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\n\
             RELAY_WRITE 100 0\n\
             JNC LABEL_1_2\n\
             ROUTINE_ASM\n\
             LABEL_1_2:\n\
             LOAD Flag 800 Parallel"
        );
    }

    #[test]
    fn non_leading_routine_combined_via_and_uses_a_series_read_back() {
        let mut routine = routine_column(2);
        routine.combinator = Some(Combinator::And);
        let target = RecordingTarget::new();
        let out = generate(&one_row(vec![leading_load(), routine]), &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\n\
             RELAY_WRITE 100 0\n\
             JNC LABEL_1_2\n\
             ROUTINE_ASM\n\
             LABEL_1_2:\n\
             LOAD Flag 800 Series"
        );
    }

    #[test]
    fn non_leading_routine_combined_via_xor_uses_the_read_back_plus_emit_xor_pattern() {
        // Same dispatch the group and leaf paths already use for XOR: 8085
        // has no single "XOR carry" instruction, so the combine step is a
        // read-back into ACC plus `emit_xor`, not a single `emit_load`.
        let mut routine = routine_column(2);
        routine.combinator = Some(Combinator::Xor);
        let target = RecordingTarget::new();
        let out = generate(&one_row(vec![leading_load(), routine]), &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\n\
             RELAY_WRITE 100 0\n\
             JNC LABEL_1_2\n\
             ROUTINE_ASM\n\
             LABEL_1_2:\n\
             RELAY_READ 100\n\
             XOR 0"
        );
    }

    #[test]
    fn inverted_non_leading_routine_inverts_its_own_result_before_combining_once() {
        // `emit_not` must land between the routine's own ASM and the combine
        // read-back — inverting the routine's own result, not
        // `running_total OP routine_result` — and must fire exactly once
        // (the shared trailing inversion check is suppressed).
        let mut routine = routine_column(2);
        routine.combinator = Some(Combinator::Or);
        routine.inverted = true;
        let target = RecordingTarget::new();
        let out = generate(&one_row(vec![leading_load(), routine]), &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\n\
             RELAY_WRITE 100 0\n\
             JNC LABEL_1_2\n\
             ROUTINE_ASM\n\
             LABEL_1_2:\n\
             NOT\n\
             LOAD Flag 800 Parallel"
        );
        assert_eq!(target.calls.borrow().iter().filter(|line| *line == "NOT").count(), 1);
    }

    #[test]
    fn first_position_routine_needs_no_stash_but_still_inverts_its_own_result() {
        // Nothing precedes it, so there is no running total to stash or
        // combine with — but `inverted` still applies, directly to the
        // routine's own result.
        let mut routine = routine_column(1);
        routine.inverted = true;
        let target = RecordingTarget::new();
        let out = generate(&one_row(vec![routine]), &target).unwrap();
        assert_eq!(out, "ROUTINE_ASM\nNOT");
    }

    #[test]
    fn non_leading_routine_without_a_combinator_keeps_its_legacy_splice_only_emission() {
        // Backward compatibility: screens saved before the combinator picker
        // existed carry routine columns with `combinator: None`. Those stay
        // on the legacy path (no stash, no combine, no new compile error) —
        // byte-for-byte what `routine_after_load_wraps_with_jnc_and_label`
        // already asserts.
        let routine = routine_column(2);
        let target = RecordingTarget::new();
        let out = generate(&one_row(vec![leading_load(), routine]), &target).unwrap();
        assert_eq!(out, "LOAD Input 1 None\nJNC LABEL_1_2\nROUTINE_ASM\nLABEL_1_2:");
        assert!(!target.calls.borrow().iter().any(|line| line.starts_with("RELAY_WRITE")));
    }

    // ------------------------------------------------------------------
    // Fix 6: naming a routine-only row's output now stores a real value.
    // ------------------------------------------------------------------

    #[test]
    fn a_routine_only_row_with_a_named_output_writes_the_routine_s_own_result() {
        // Consequence of Fix 5: the routine's own result reliably ends up in
        // carry the way a Load's does, so the (already-correct in principle)
        // row-output write stores something meaningful rather than residual
        // garbage. The only thing that can be in carry at the RELAY_WRITE is
        // whatever the routine's own ASM left there.
        let screen = Screen {
            rows: vec![RowScreen {
                row_number: 1,
                columns: vec![routine_column(1)],
                output_name: Some("Reset Done".into()),
            }],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(out, "ROUTINE_ASM\nRELAY_WRITE 100 0");
    }

    #[test]
    fn a_named_routine_row_output_captures_the_inverted_and_combined_result() {
        // Row 1: `load OR NOT(routine)`, named. The RELAY_WRITE must come
        // after the full combine — i.e. it captures the value the whole row
        // expression produced, not the raw carry the routine's ASM left
        // behind mid-expression. Row 2 then reads that same address back.
        let mut routine = routine_column(2);
        routine.combinator = Some(Combinator::Or);
        routine.inverted = true;

        let mut row_ref = column(CoilType::RowRef);
        row_ref.row_number = 2;
        row_ref.row_ref_name = Some("Reset Done".into());

        let screen = Screen {
            rows: vec![
                RowScreen {
                    row_number: 1,
                    columns: vec![leading_load(), routine],
                    output_name: Some("Reset Done".into()),
                },
                RowScreen { row_number: 2, columns: vec![row_ref], output_name: None },
            ],
            end_row_number: None,
            end_column_number: None,
        };
        let target = RecordingTarget::new();
        let out = generate(&screen, &target).unwrap();
        assert_eq!(
            out,
            "LOAD Input 1 None\n\
             RELAY_WRITE 100 1\n\
             JNC LABEL_1_2\n\
             ROUTINE_ASM\n\
             LABEL_1_2:\n\
             NOT\n\
             LOAD Flag 801 Parallel\n\
             RELAY_WRITE 100 0\n\
             LOAD Flag 800 None"
        );
    }
}
