"""
Walk-Forward Out-of-Sample Backtesting Engine.
Combines walk-forward training folds (Part 12) with point-in-time backtesting simulation.
"""

from typing import Dict, List, Any
import pandas as pd
from app.backtesting.schemas import BacktestConfig, BacktestResult
from app.backtesting.engine import ProductionBacktestEngine


class WalkForwardBacktestEngine:
    """
    Executes sequential out-of-sample backtest windows and stitches out-of-sample equity curves.
    """

    def __init__(self, base_config: BacktestConfig):
        self.base_config = base_config

    def run_walk_forward_backtest(
        self,
        folds: List[Dict[str, Any]],  # list of {'fold_index', 'train_start', 'train_end', 'test_start', 'test_end', 'model_fn'}
        market_data: Dict[str, pd.DataFrame],
        corporate_actions: List[Dict] = None
    ) -> List[BacktestResult]:
        fold_results = []

        for fold in folds:
            fold_cfg = self.base_config.copy(deep=True)
            fold_cfg.name = f"{self.base_config.name}_fold_{fold['fold_index']}"
            fold_cfg.start_date = fold['test_start']
            fold_cfg.end_date = fold['test_end']

            engine = ProductionBacktestEngine(fold_cfg)
            result = engine.run(
                market_data=market_data,
                signal_generator_fn=fold.get('signal_generator_fn'),
                corporate_actions=corporate_actions
            )
            fold_results.append(result)

        return fold_results
