"""FastAPI Router for Deep Learning & Temporal Sequence Model Engine."""

from typing import Dict, Any, List, Optional
from fastapi import APIRouter, HTTPException, Query, Body
from pydantic import BaseModel, Field
import numpy as np
import pandas as pd

from app.deep_learning.adapter import DeepLearningModelAdapter
from app.deep_learning.ensemble import WalkForwardEnsembleComparator
from app.deep_learning.sequence_generator import TemporalSequenceGenerator

router = APIRouter(prefix="/api/v1/deep-learning", tags=["deep-learning"])


class TrainDLModelRequest(BaseModel):
    model_type: str = Field("GRU", description="Model architecture: GRU, LSTM, 1DCNN, ATTENTION")
    lookback: int = Field(60, description="Sequence lookback bars (default: 60)")
    horizon: int = Field(1, description="Forward target horizon bars (default: 1)")
    hidden_dim: int = Field(32, description="Hidden state dimension / filters")
    epochs: int = Field(15, description="Training epochs")
    learning_rate: float = Field(0.005, description="Learning rate")
    scaling_method: str = Field("standard", description="Scaling method: standard, robust, minmax")
    random_seed: int = Field(42, description="Random seed")
    # Sample synthetic or panel data records: list of dicts with timestamp, symbol, features, and target
    records: Optional[List[Dict[str, Any]]] = None


class CompareModelsRequest(BaseModel):
    lookback: int = Field(60, description="Sequence lookback bars")
    horizon: int = Field(1, description="Forward target horizon bars")
    n_splits: int = Field(3, description="Number of purged walk-forward splits")
    cost_bps: float = Field(15.0, description="Indian equity transaction costs in bps")
    records: Optional[List[Dict[str, Any]]] = None


@router.get("/architectures")
def list_architectures() -> Dict[str, Any]:
    """Lists supported native deep learning architectures and default hyperparameters."""
    return {
        "status": "success",
        "architectures": [
            {
                "type": "GRU",
                "name": "Temporal Gated Recurrent Unit",
                "description": "Recurrent memory model with update and reset gates, optimized for short-to-medium dependencies.",
                "default_hidden_dim": 32,
                "supports_attention": True,
            },
            {
                "type": "LSTM",
                "name": "Temporal Long Short-Term Memory",
                "description": "Recurrent model with input, forget, and output gating for persistent long-range dependencies.",
                "default_hidden_dim": 32,
                "supports_attention": True,
            },
            {
                "type": "1DCNN",
                "name": "Temporal 1D Dilated Convolutional Network",
                "description": "Causal 1D temporal convolution with multi-scale receptive fields and global pooling.",
                "default_hidden_dim": 32,
                "supports_attention": True,
            },
            {
                "type": "ATTENTION",
                "name": "Temporal Self-Attention Network",
                "description": "Multi-step temporal self-attention computing dynamic historical importance weights.",
                "default_hidden_dim": 32,
                "supports_attention": True,
            },
        ],
        "safeguards": {
            "point_in_time_scaling": True,
            "purge_and_embargo": True,
            "cross_symbol_isolation": True,
            "realistic_transaction_costs_bps": 15.0,
            "classical_ml_comparison_mandatory": True,
        }
    }


def _create_sample_panel_df(n_bars: int = 300, n_symbols: int = 3) -> pd.DataFrame:
    """Generates synthetic panel data for live demonstration."""
    rng = np.random.RandomState(42)
    dfs = []
    symbols = [f"NSE_STOCK_{i+1}" for i in range(n_symbols)]

    for sym in symbols:
        dates = pd.date_range(start="2024-01-01", periods=n_bars, freq="B")
        # Generate non-stationary price series
        returns = rng.randn(n_bars) * 0.015 + 0.0005
        prices = 100.0 * np.cumprod(1.0 + returns)
        
        # Features
        f1 = rng.randn(n_bars) * 0.02
        f2 = np.sin(np.linspace(0, 10, n_bars)) + rng.randn(n_bars) * 0.1
        f3 = np.cos(np.linspace(0, 5, n_bars)) + rng.randn(n_bars) * 0.1
        
        # Target: 1-day forward return
        target = np.zeros(n_bars)
        target[:-1] = (prices[1:] - prices[:-1]) / prices[:-1]

        df_sym = pd.DataFrame({
            "timestamp": dates,
            "symbol": sym,
            "close": prices,
            "feature_rsi": f1,
            "feature_volatility": f2,
            "feature_momentum": f3,
            "target": target,
        })
        dfs.append(df_sym)

    return pd.concat(dfs, ignore_index=True)


@router.post("/train")
def train_dl_model(req: TrainDLModelRequest) -> Dict[str, Any]:
    """Trains a native deep learning sequence model."""
    try:
        if req.records and len(req.records) > 0:
            df = pd.DataFrame(req.records)
        else:
            df = _create_sample_panel_df(n_bars=250, n_symbols=2)

        adapter = DeepLearningModelAdapter(
            model_type_name=req.model_type,
            lookback=req.lookback,
            horizon=req.horizon,
            hidden_dim=req.hidden_dim,
            learning_rate=req.learning_rate,
            epochs=req.epochs,
            scaling_method=req.scaling_method,
            random_seed=req.random_seed,
        )

        adapter.fit(df)
        eval_metrics = adapter.evaluate(df)
        explanations = adapter.explain(df)

        return {
            "status": "success",
            "model_id": adapter.model_id,
            "algorithm": adapter.algorithm,
            "provenance_hash": adapter.provenance_hash,
            "lookback": adapter.lookback,
            "horizon": adapter.horizon,
            "metrics": eval_metrics,
            "sample_attention": explanations[0] if explanations else {},
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/compare")
def compare_models(req: CompareModelsRequest) -> Dict[str, Any]:
    """Runs purged walk-forward cross-validation comparing Classical ML vs Deep Learning."""
    try:
        if req.records and len(req.records) > 0:
            df = pd.DataFrame(req.records)
        else:
            df = _create_sample_panel_df(n_bars=300, n_symbols=2)

        comparator = WalkForwardEnsembleComparator(
            lookback=req.lookback,
            horizon=req.horizon,
            n_splits=req.n_splits,
            cost_bps=req.cost_bps,
        )

        comparison_results = comparator.run_comparison(df)
        return {
            "status": "success",
            "results": comparison_results,
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
