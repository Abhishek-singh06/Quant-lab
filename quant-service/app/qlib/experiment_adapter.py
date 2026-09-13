"""Experiment and Artifact Management Bridge for Qlib Research.

Tracks Qlib quantitative research runs, alpha factor metrics, model parameters,
and registers artifacts into QuantLab's Model Registry and filesystem cache.
"""

import os
import json
from typing import Dict, Any, Optional, List
from datetime import datetime
import joblib

from app.qlib.config import get_qlib_config
from app.ml.walk_forward.registry import LocalModelRegistry


class QlibExperimentManager:
    """Manages tracking, persistence, and audit logs of Qlib alpha research experiments."""

    def __init__(self, base_dir: Optional[str] = None):
        config = get_qlib_config()
        self.base_dir = base_dir or config.experiments_dir
        os.makedirs(self.base_dir, exist_ok=True)
        self.registry = LocalModelRegistry(base_dir=os.path.join(self.base_dir, "models"))

    def record_experiment(
        self,
        experiment_name: str,
        model_id: str,
        model_type: str,
        dataset_info: Dict[str, Any],
        hyperparameters: Dict[str, Any],
        metrics: Dict[str, Any],
        backtest_comparison: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, Any]:
        """Saves experiment run record to disk and returns record summary."""
        exp_id = f"{experiment_name}_{model_id}_{int(datetime.now().timestamp())}"
        exp_path = os.path.join(self.base_dir, f"{exp_id}.json")

        record = {
            "experiment_id": exp_id,
            "experiment_name": experiment_name,
            "model_id": model_id,
            "model_type": model_type,
            "timestamp": datetime.now().isoformat(),
            "dataset_info": dataset_info,
            "hyperparameters": hyperparameters,
            "evaluation_metrics": metrics,
            "backtest_comparison": backtest_comparison,
        }

        with open(exp_path, "w", encoding="utf-8") as f:
            json.dump(record, f, indent=2)

        return record

    def list_experiments(self) -> List[Dict[str, Any]]:
        """Lists all recorded Qlib experiments."""
        results = []
        if not os.path.exists(self.base_dir):
            return results

        for fname in sorted(os.listdir(self.base_dir)):
            if fname.endswith(".json"):
                fpath = os.path.join(self.base_dir, fname)
                try:
                    with open(fpath, "r", encoding="utf-8") as f:
                        data = json.load(f)
                        results.append(data)
                except Exception:
                    continue
        return results

    def get_experiment(self, experiment_id: str) -> Optional[Dict[str, Any]]:
        """Retrieves details of a specific experiment."""
        fpath = os.path.join(self.base_dir, f"{experiment_id}.json")
        if os.path.exists(fpath):
            with open(fpath, "r", encoding="utf-8") as f:
                return json.load(f)
        return None
