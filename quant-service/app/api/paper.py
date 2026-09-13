"""
Paper Trading API Endpoints for QuantLab (Phase 17).
Provides REST endpoints for Paper Trading sessions, health gates, telemetry, and emergency controls.
"""

from fastapi import APIRouter, HTTPException, Query
from typing import Dict, List, Any, Optional
from datetime import datetime, timezone

from app.paper.session_runner import PaperTradingSessionOrchestrator, PaperTradingStartGateReport
from app.paper.schemas import ClockType

router = APIRouter(prefix="/api/v1/paper", tags=["Paper Trading"])

# Global singleton session orchestrator for paper trading runtime
_global_orchestrator: Optional[PaperTradingSessionOrchestrator] = None


def get_or_create_orchestrator() -> PaperTradingSessionOrchestrator:
    global _global_orchestrator
    if _global_orchestrator is None:
        _global_orchestrator = PaperTradingSessionOrchestrator(
            session_name="QUANTLAB_PROD_PAPER_V1",
            initial_cash=1_000_000.0,
            clock_type=ClockType.LIVE_CLOCK,
            provider_name="YAHOO_FINANCE",
            auto_load_persisted=True
        )
    return _global_orchestrator


@router.get("/health")
async def get_paper_health() -> Dict[str, Any]:
    """Evaluates paper trading health report and start gate readiness."""
    orch = get_or_create_orchestrator()
    gate = orch.evaluate_start_gate()
    latency = orch.ingestion_service.get_latency_metrics()

    return {
        "status": "HEALTHY" if gate.is_ready_to_start else "BLOCKED",
        "start_gate": gate.model_dump(),
        "latency_metrics": latency,
        "emergency_stop_active": orch.emergency_stop_active,
        "live_trading_enabled": False,
        "paper_trading_mode": True,
        "real_money_at_risk": 0.0
    }


@router.get("/session")
async def get_current_session() -> Dict[str, Any]:
    """Returns current paper trading session summary and performance."""
    orch = get_or_create_orchestrator()
    return orch.get_session_summary()


@router.post("/session/start")
async def start_paper_session() -> Dict[str, Any]:
    """Starts a paper trading session through the start gate."""
    orch = get_or_create_orchestrator()
    success, message = orch.start_session()
    if not success:
        raise HTTPException(status_code=400, detail=message)
    return {"success": True, "message": message, "session": orch.get_session_summary()}


@router.post("/session/stop")
async def stop_paper_session(reason: str = "OPERATOR_STOP") -> Dict[str, Any]:
    """Stops the active paper trading session and persists state."""
    orch = get_or_create_orchestrator()
    success, message = orch.stop_session(reason=reason)
    return {"success": success, "message": message, "session": orch.get_session_summary()}


@router.post("/session/emergency-stop")
async def trigger_emergency_stop(reason: str = "OPERATOR_TRIGGER") -> Dict[str, Any]:
    """Activates emergency stop halting all new paper orders."""
    orch = get_or_create_orchestrator()
    orch.trigger_emergency_stop(reason=reason)
    return {"success": True, "emergency_stop_active": True, "reason": reason}


@router.post("/session/reset-stop")
async def reset_emergency_stop() -> Dict[str, Any]:
    """Resets emergency stop to resume paper trading."""
    orch = get_or_create_orchestrator()
    orch.reset_emergency_stop()
    return {"success": True, "emergency_stop_active": False}


@router.get("/journal")
async def get_paper_journal(limit: int = 50) -> List[Dict[str, Any]]:
    """Returns recent structured audit journal entries."""
    orch = get_or_create_orchestrator()
    entries = orch.journal[-limit:]
    return [e.model_dump() for e in reversed(entries)]


@router.get("/positions")
async def get_paper_positions() -> List[Dict[str, Any]]:
    """Returns all paper positions and mark-to-market status."""
    orch = get_or_create_orchestrator()
    orch.reload_persisted_state()
    return [p.model_dump() for p in orch.portfolio_mgr.positions.values()]


@router.get("/orders")
async def get_paper_orders(limit: int = 50) -> List[Dict[str, Any]]:
    """Returns all paper trading orders."""
    orch = get_or_create_orchestrator()
    orch.reload_persisted_state()
    orders = orch.orders[-limit:]
    return [o.model_dump() for o in reversed(orders)]


@router.get("/fills")
async def get_paper_fills(limit: int = 50) -> List[Dict[str, Any]]:
    """Returns recent paper fills with simulated transaction costs and slippage."""
    orch = get_or_create_orchestrator()
    orch.reload_persisted_state()
    fills = orch.fills[-limit:]
    return [f.model_dump() for f in reversed(fills)]


@router.get("/decisions")
async def get_paper_decisions(limit: int = 50) -> List[Dict[str, Any]]:
    """Returns recent model paper decisions."""
    orch = get_or_create_orchestrator()
    orch.reload_persisted_state()
    decisions = orch.decisions[-limit:]
    return [d.model_dump() for d in reversed(decisions)]


@router.get("/portfolios")
async def get_paper_portfolios() -> List[Dict[str, Any]]:
    """Returns paper portfolio state."""
    orch = get_or_create_orchestrator()
    orch.reload_persisted_state()
    return [orch.portfolio_mgr.portfolio.model_dump()]


@router.get("/provenance")
async def get_paper_provenance() -> Dict[str, Any]:
    """Returns complete observation-to-fill provenance audit for all paper orders."""
    orch = get_or_create_orchestrator()
    return orch.get_provenance_audit()


@router.get("/telemetry")
async def get_paper_telemetry() -> Dict[str, Any]:
    """Returns safe provider telemetry without secrets."""
    orch = get_or_create_orchestrator()
    orch.reload_persisted_state()
    return orch.ingestion_service.get_telemetry()
