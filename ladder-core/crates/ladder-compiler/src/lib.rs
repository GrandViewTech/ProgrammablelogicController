mod legacy;
mod target;
mod traversal;
mod types;

pub use target::Target;
pub use traversal::generate;
pub use types::{CompileError, LoadCombinator, LoadKind, OutputType};

pub mod compat {
    //! Fidelity-reference exports of the original's inference logic.
    //! Not used by `generate()` — see Global Constraints in the plan.
    pub use crate::legacy::{find_parallel_series, find_param, LegacyParallelSeries};
}
