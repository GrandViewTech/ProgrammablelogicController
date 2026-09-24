use ladder_model::{CoilType, RowScreen};

/// 1:1 port of CompileService.findParam(Integer). Byte/bit address split.
/// The original's Integer2Hex flag was always false in practice (dead code
/// path), so this returns decimal digit strings, matching observed behavior.
pub fn find_param(input: i32) -> (u32, u32) {
    let input = input.unsigned_abs();
    (input / 8, input % 8)
}

/// Legacy PARALLEL_SERIES inference, ported 1:1 from
/// CompileService.findParallelSeries for fidelity/reference. The original
/// never actually produced PARALLEL (the AND/OR combination logic for LOAD
/// columns was left unfinished — requirements §3, §7.3) so this always
/// returns Series or None; it is not called by `generate()`, which instead
/// reads the explicit `Combinator` off the block (requirements §7.3, §10).
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum LegacyParallelSeries {
    Parallel,
    Series,
    None,
}

pub fn find_parallel_series(row: &RowScreen, column_index: usize) -> LegacyParallelSeries {
    match row.previous(column_index) {
        Some(previous) if previous.coil_type == CoilType::Load => LegacyParallelSeries::Series,
        _ => LegacyParallelSeries::None,
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use ladder_model::{CoilType, ColumnScreen};

    fn blank_column(row: u32, col: u32, coil_type: CoilType) -> ColumnScreen {
        ColumnScreen {
            row_number: row,
            column_number: col,
            coil_type,
            input_type: None,
            value: String::new(),
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
    fn find_param_splits_byte_and_bit_matching_original_arithmetic() {
        assert_eq!(find_param(19), (2, 3)); // 19 / 8 = 2, 19 % 8 = 3
        assert_eq!(find_param(-19), (2, 3)); // original negates before dividing
        assert_eq!(find_param(0), (0, 0));
    }

    #[test]
    fn find_parallel_series_returns_series_when_previous_is_load() {
        let row = RowScreen {
            row_number: 1,
            columns: vec![
                blank_column(1, 1, CoilType::Load),
                blank_column(1, 2, CoilType::Output),
            ],
            output_name: None,
        };
        assert_eq!(find_parallel_series(&row, 1), LegacyParallelSeries::Series);
    }

    #[test]
    fn find_parallel_series_returns_none_for_first_column_or_non_load_previous() {
        let row = RowScreen {
            row_number: 1,
            columns: vec![
                blank_column(1, 1, CoilType::Output),
                blank_column(1, 2, CoilType::Output),
            ],
            output_name: None,
        };
        assert_eq!(find_parallel_series(&row, 0), LegacyParallelSeries::None);
        assert_eq!(find_parallel_series(&row, 1), LegacyParallelSeries::None);
    }
}
