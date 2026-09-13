"""REST API Router for Qlib Quantitative Research Engine.

Provides endpoints for alpha factor computation, model training, signal evaluation,
and backtest comparison against QuantLab's canonical execution engine.
"""

from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from fastapi import APIRouter, HTTPException, Query

from app.qlib import (
    QuantLabQlibDataProvider,
    QuantLabDataLoader,
    QuantLabDataHandler,
    QlibGBDTModel,
    QlibLinearModel,
    QlibRandomForestModel,
    QlibEnsembleModel,
    QlibSignalEvaluator,
    QlibBacktestComparator,
    QlibExperimentManager,
)

router = APIRouter(prefix="/qlib", tags=["Qlib Quantitative Research"])


# ---------------------------------------------------------------------------
# Request / Response Schemas
# ---------------------------------------------------------------------------

class AlphaComputeRequest(BaseModel):
    instruments: List[str] = Field(default=["RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK"])
    start_time: str = Field(default="2023-01-01")
    end_time: str = Field(default="2023-12-31")
    feature_type: str = Field(default="Alpha158")  # "Alpha158" or "Alpha360"


class AlphaComputeResponse(BaseModel):
    feature_type: str
    n_instruments: int
    n_records: int
    n_features: int
    feature_names: List[str]


class ModelTrainRequest(BaseModel):
    instruments: List[str] = Field(default=["RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK"])
    train_start: str = Field(default="2023-01-01")
    train_end: str = Field(default="2023-08-31")
    test_start: str = Field(default="2023-09-01")
    test_end: str = Field(default="2023-12-31")
    model_type: str = Field(default="GBDT")  # "GBDT", "Ridge", "RandomForest", "Ensemble"
    feature_type: str = Field(default="Alpha158")
    hyperparameters: Optional[Dict[str, Any]] = None


class ModelTrainResponse(BaseModel):
    model_id: str
    model_type: str
    experiment_id: str
    evaluation_metrics: Dict[str, Any]
    backtest_comparison: Dict[str, Any]


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------

@router.post("/alpha/compute", response_model=AlphaComputeResponse)
def compute_alpha_factors(request: AlphaComputeRequest):
    """Computes Alpha158 or Alpha360 features on canonical warehouse data."""
    try:
        loader = QuantLabDataLoader(feature_type=request.feature_type)
        features_df, _, _ = loader.load_data(
            instruments=request.instruments,
            start_time=request.start_time,
            end_time=request.end_time,
        )
        return AlphaComputeResponse(
            feature_type=request.feature_type,
            n_instruments=len(request.instruments),
            n_records=len(features_df),
            n_features=features_df.shape[1],
            feature_names=list(features_df.columns),
        )
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/model/train", response_model=ModelTrainResponse)
def train_and_evaluate_model(request: ModelTrainRequest):
    """Trains a Qlib alpha model, evaluates signals, and performs backtest comparison."""
    try:
        loader = QuantLabDataLoader(feature_type=request.feature_type)
        handler = QuantLabDataHandler(
            instruments=request.instruments,
            start_time=request.train_start,
            end_time=request.test_end,
            loader=loader,
        )

        dataset = handler.setup_dataset(
            train_range=(request.train_start, request.train_end),
            test_range=(request.test_start, request.test_end),
        )

        m_type = request.model_type.upper()
        if m_type == "GBDT":
            model = QlibGBDTModel(hyperparameters=request.hyperparameters)
        elif m_type == "RIDGE" or m_type == "LINEAR":
            model = QlibLinearModel(hyperparameters=request.hyperparameters)
        elif m_type == "RANDOMFOREST" or m_type == "RF":
            model = QlibRandomForestModel(hyperparameters=request.hyperparameters)
        elif m_type == "ENSEMBLE":
            m1 = QlibGBDTModel()
            m2 = QlibLinearModel()
            model = QlibEnsembleModel(models=[(m1, 0.7), (m2, 0.3)])
        else:
            raise ValueError(f"Unsupported model_type: '{request.model_type}'")

        # Fit model on training segment
        model.fit(dataset)

        # Generate test predictions
        test_preds = model.predict(dataset, segment="test")
        test_y = dataset.prepare("test", col_set="label")

        # Evaluate alpha predictive metrics (IC, Rank IC, Long-Short Sharpe)
        evaluator = QlibSignalEvaluator()
        metrics = evaluator.evaluate(test_preds, test_y)

        # Run Backtest Comparison (Theoretical Vectorized vs Canonical Realistic Next-Bar T+1)
        provider = QuantLabQlibDataProvider()
        price_panel = provider.get_market_data(
            instruments=request.instruments,
            start_time=request.test_start,
            end_time=request.test_end,
        )
        comparator = QlibBacktestComparator()
        comparison = comparator.run_comparison(test_preds, price_panel)

        # Record experiment
        exp_mgr = QlibExperimentManager()
        record = exp_mgr.record_experiment(
            experiment_name=f"qlib_{request.feature_type.lower()}_{request.model_type.lower()}",
            model_id=model.model_id,
            model_type=request.model_type,
            dataset_info={
                "instruments": request.instruments,
                "train_range": [request.train_start, request.train_end],
                "test_range": [request.test_start, request.test_end],
                "feature_type": request.feature_type,
            },
            hyperparameters=request.hyperparameters or {},
            metrics=metrics,
            backtest_comparison=comparison,
        )

        return ModelTrainResponse(
            model_id=model.model_id,
            model_type=request.model_type,
            experiment_id=record["experiment_id"],
            evaluation_metrics=metrics,
            backtest_comparison=comparison,
        )
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.get("/experiments")
def list_experiments():
    """Lists all recorded Qlib alpha research experiments."""
    exp_mgr = QlibExperimentManager()
    return exp_mgr.list_experiments()
