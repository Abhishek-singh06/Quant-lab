"""REST API Router for Strategy Validation and Lookahead Verification.

Provides endpoints for pre-deployment strategy validation, programmatic lookahead bias detection,
and recursive indicator warmup sufficiency analysis.
"""

from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from fastapi import APIRouter, HTTPException, Query, status
import pandas as pd

from app.strategy_lifecycle.models import StrategyDefinition
from app.strategy_lifecycle.registry import global_strategy_registry
from app.strategy_validation import (
    StrategyValidator,
    StrategyValidationReport,
    LookaheadAnalyzer,
    LookaheadReport,
    RecursiveAnalyzer,
    RecursiveStabilityReport,
)

router = APIRouter(prefix="/validation", tags=["Strategy Validation & Verification"])
_validator = StrategyValidator()
_lookahead_analyzer = LookaheadAnalyzer()
_recursive_analyzer = RecursiveAnalyzer()


class ValidateRegisteredStrategyRequest(BaseModel):
    strategy_id: str
    version: Optional[str] = None


class WarmupCheckRequest(BaseModel):
    indicator_name: str
    data_series: List[float]
    warmup_steps: Optional[List[int]] = None


@router.post("/validate-strategy", response_model=StrategyValidationReport)
def validate_strategy_definition(strategy: StrategyDefinition):
    """Run full pre-deployment validation on a StrategyDefinition payload."""
    return _validator.validate_strategy(strategy)


@router.post("/validate-registered", response_model=StrategyValidationReport)
def validate_registered_strategy(req: ValidateRegisteredStrategyRequest):
    """Run full pre-deployment validation on a previously registered strategy."""
    strat = global_strategy_registry.get_strategy(req.strategy_id, req.version)
    if not strat:
        raise HTTPException(
            status_code=404,
            detail=f"Strategy '{req.strategy_id}' (version: {req.version or 'latest'}) not found in registry."
        )
    return _validator.validate_strategy(strat)


@router.post("/warmup-check", response_model=RecursiveStabilityReport)
def check_indicator_warmup(req: WarmupCheckRequest):
    """Evaluate convergence and warmup requirements for an indicator series."""
    df = pd.DataFrame({"close": req.data_series})
    # Default EMA calculation for warmup convergence testing
    indicator_fn = lambda d: d["close"].ewm(span=20, adjust=False).mean()
    
    return _recursive_analyzer.analyze_warmup_convergence(
        indicator_fn=indicator_fn,
        df=df,
        indicator_name=req.indicator_name,
        test_warmup_bars=req.warmup_steps
    )
