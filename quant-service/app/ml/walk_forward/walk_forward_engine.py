"""Production Walk-Forward Training and Historical Evaluation Engine.
"""

from typing import Dict, Any, Optional, List, Tuple
from dataclasses import dataclass
import numpy as np
import pandas as pd
from datetime import datetime

from app.ml.models.regression import RegressionModel
from app.ml.models.classification import ClassificationModel
from app.ml.models.volatility import VolatilityModel
from app.ml.validation.preprocessor import LeakageSafePreprocessor
from app.ml.validation.purging import PurgeAndEmbargo
from app.ml.validation.metrics import QuantMetrics
from app.ml.walk_forward.registry import LocalModelRegistry


@dataclass
class FoldConfig:
    fold_number: int
    train_start: str
    train_end: str
    val_start: str
    val_end: str
    test_start: str
    test_end: str


class WalkForwardEngine:
    """Orchestrates Chronological Expanding / Rolling Walk-Forward Model Training."""

    def __init__(
        self,
        mode: str = "EXPANDING",  # EXPANDING, ROLLING
        model_type: str = "REGRESSION",  # REGRESSION, CLASSIFICATION, VOLATILITY
        target_name: str = "future_return_1d",
        purge_window_days: int = 5,
        embargo_window_days: int = 2,
        registry: Optional[LocalModelRegistry] = None
    ):
        self.mode = mode.upper()
        self.model_type = model_type.upper()
        self.target_name = target_name
        self.purge_window_days = purge_window_days
        self.embargo_window_days = embargo_window_days
        self.registry = registry or LocalModelRegistry()

    def run_walk_forward(
        self,
        X: pd.DataFrame,
        y: pd.Series,
        dates: List[str],
        folds_config: List[FoldConfig]
    ) -> Dict[str, Any]:
        """Executes all walk-forward folds chronologically."""
        if X.empty or y.empty or len(dates) != len(X):
            return {"status": "ERROR", "message": "Invalid dataset or date alignment"}

        df_full = X.copy()
        df_full['date'] = pd.to_datetime(dates)
        df_full['target'] = y.values

        fold_results = []
        all_test_predictions = []

        for fold in folds_config:
            # 1. Slice Partitions
            tr_mask = (df_full['date'] >= pd.to_datetime(fold.train_start)) & (df_full['date'] <= pd.to_datetime(fold.train_end))
            val_mask = (df_full['date'] >= pd.to_datetime(fold.val_start)) & (df_full['date'] <= pd.to_datetime(fold.val_end))
            te_mask = (df_full['date'] >= pd.to_datetime(fold.test_start)) & (df_full['date'] <= pd.to_datetime(fold.test_end))

            train_df = df_full[tr_mask].copy()
            val_df = df_full[val_mask].copy()
            test_df = df_full[te_mask].copy()

            if len(train_df) < 30 or len(test_df) < 10:
                continue

            # 2. Apply Purging & Embargo
            train_df = PurgeAndEmbargo.purge_train_overlap(
                train_df, fold.test_start, target_horizon_days=self.purge_window_days
            )
            test_df = PurgeAndEmbargo.apply_embargo(
                test_df, fold.test_start, embargo_days=self.embargo_window_days
            )

            feat_cols = [c for c in X.columns]
            X_tr, y_tr = train_df[feat_cols], train_df['target']
            X_val, y_val = val_df[feat_cols], val_df['target']
            X_te, y_te = test_df[feat_cols], test_df['target']

            # 3. Fit Preprocessing strictly on Training partition
            preproc = LeakageSafePreprocessor(scaler_type="ROBUST")
            X_tr_sc = preproc.fit_transform(X_tr)
            X_val_sc = preproc.transform(X_val)
            X_te_sc = preproc.transform(X_te)

            # 4. Train Candidate Models and Select Winning Champion on Validation
            candidates = self._create_candidate_models(fold.fold_number)
            best_candidate = None
            best_val_score = -999.0

            for cand in candidates:
                cand.fit(X_tr_sc, y_tr)
                val_eval = cand.evaluate(X_val_sc, y_val)
                score = val_eval.get("ic", val_eval.get("roc_auc", -val_eval.get("mae", 0.0)))
                if score > best_val_score:
                    best_val_score = score
                    best_candidate = cand

            # 5. Freeze Champion Model & Evaluate on Out-of-Sample Test Set
            test_eval = best_candidate.evaluate(X_te_sc, y_te)
            test_preds = best_candidate.predict(X_te_sc)
            decile_eval = QuantMetrics.compute_decile_spread(y_te.values, test_preds)

            # Baseline comparisons on same test set
            base_mean = RegressionModel("BASE-MEAN", algorithm="HISTORICAL_MEAN").fit(X_tr_sc, y_tr)
            base_eval = base_mean.evaluate(X_te_sc, y_te)

            # Save artifact
            self.registry.save_model(best_candidate, fold_id=f"fold_{fold.fold_number}")

            fold_summary = {
                "fold_number": fold.fold_number,
                "train_period": f"{fold.train_start} to {fold.train_end}",
                "test_period": f"{fold.test_start} to {fold.test_end}",
                "champion_algorithm": best_candidate.algorithm,
                "champion_version": best_candidate.model_version,
                "test_observations": len(y_te),
                "metrics": test_eval,
                "decile_spread": decile_eval,
                "baseline_ic": base_eval.get("ic", 0.0)
            }
            fold_results.append(fold_summary)

        # 6. Aggregate Walk-Forward Metrics
        all_ics = [f["metrics"].get("ic", 0.0) for f in fold_results if "ic" in f["metrics"]]
        mean_ic = float(np.mean(all_ics)) if all_ics else 0.0
        std_ic = float(np.std(all_ics)) if all_ics else 0.0

        return {
            "status": "SUCCESS",
            "mode": self.mode,
            "model_type": self.model_type,
            "target_name": self.target_name,
            "total_folds": len(fold_results),
            "mean_ic": round(mean_ic, 4),
            "std_ic": round(std_ic, 4),
            "ic_ir": round(mean_ic / std_ic, 4) if std_ic > 1e-6 else 0.0,
            "fold_results": fold_results
        }

    def _create_candidate_models(self, fold_number: int) -> List[Any]:
        ver = f"v{fold_number}.0"
        if self.model_type == "REGRESSION":
            return [
                RegressionModel(f"MOD-RIDGE-{fold_number}", algorithm="RIDGE", model_version=f"RIDGE_1D_{ver}"),
                RegressionModel(f"MOD-RF-{fold_number}", algorithm="RANDOM_FOREST", model_version=f"RF_1D_{ver}"),
                RegressionModel(f"MOD-GB-{fold_number}", algorithm="GRADIENT_BOOSTING", model_version=f"GB_1D_{ver}")
            ]
        elif self.model_type == "CLASSIFICATION":
            return [
                ClassificationModel(f"MOD-LOG-{fold_number}", algorithm="LOGISTIC_REGRESSION", model_version=f"LOG_1D_{ver}"),
                ClassificationModel(f"MOD-RFC-{fold_number}", algorithm="RANDOM_FOREST_CLASSIFIER", model_version=f"RFC_1D_{ver}"),
                ClassificationModel(f"MOD-GBC-{fold_number}", algorithm="GRADIENT_BOOSTING_CLASSIFIER", model_version=f"GBC_1D_{ver}")
            ]
        else:
            return [
                VolatilityModel(f"MOD-VOL-RF-{fold_number}", algorithm="RANDOM_FOREST", model_version=f"VOL_RF_{ver}"),
                VolatilityModel(f"MOD-VOL-GB-{fold_number}", algorithm="GRADIENT_BOOSTING", model_version=f"VOL_GB_{ver}")
            ]
