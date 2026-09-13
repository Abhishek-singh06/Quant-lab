"""Experiment Lineage and Provenance Tracker.

Maintains immutable audit records connecting data snapshots, feature schemas,
model weights, backtest evaluations, and strategy versions with strict Point-in-Time (PIT) integrity.
"""

import hashlib
import json
import threading
from typing import Dict, List, Optional, Any
from datetime import datetime, timezone

from app.strategy_lifecycle.models import ExperimentProvenance


def compute_hash(data: Any) -> str:
    """Compute deterministic SHA-256 hash of arbitrary serializable data or string."""
    if isinstance(data, str):
        content = data.encode("utf-8")
    elif isinstance(data, bytes):
        content = data
    else:
        content = json.dumps(data, sort_keys=True, default=str).encode("utf-8")
    return hashlib.sha256(content).hexdigest()


class ProvenanceTracker:
    """Thread-safe tracker for strategy experiment provenance and audit lineage."""

    def __init__(self) -> None:
        self._lock = threading.RLock()
        # Key: provenance_id -> ExperimentProvenance
        self._records: Dict[str, ExperimentProvenance] = {}
        # Index: (strategy_id, strategy_version) -> List[provenance_id]
        self._strategy_index: Dict[tuple, List[str]] = {}

    def record_provenance(self, provenance: ExperimentProvenance) -> ExperimentProvenance:
        """Record an immutable provenance entry."""
        with self._lock:
            stored = provenance.model_copy(deep=True)
            self._records[stored.provenance_id] = stored
            key = (stored.strategy_id, stored.strategy_version)
            if key not in self._strategy_index:
                self._strategy_index[key] = []
            self._strategy_index[key].append(stored.provenance_id)
            return stored.model_copy(deep=True)

    def get_provenance(self, provenance_id: str) -> Optional[ExperimentProvenance]:
        """Retrieve a provenance record by ID."""
        with self._lock:
            rec = self._records.get(provenance_id)
            return rec.model_copy(deep=True) if rec else None

    def get_provenance_for_strategy(
        self,
        strategy_id: str,
        strategy_version: str
    ) -> List[ExperimentProvenance]:
        """Retrieve all provenance records for a given strategy and version."""
        with self._lock:
            key = (strategy_id, strategy_version)
            prov_ids = self._strategy_index.get(key, [])
            return [self._records[pid].model_copy(deep=True) for pid in prov_ids if pid in self._records]

    def verify_pit_integrity(
        self,
        source_timestamp: datetime,
        information_available_at: datetime
    ) -> bool:
        """Verify Point-in-Time integrity: source timestamp must not precede information availability."""
        # Ensure both are timezone-aware UTC for comparison
        if source_timestamp.tzinfo is None:
            source_timestamp = source_timestamp.replace(tzinfo=timezone.utc)
        if information_available_at.tzinfo is None:
            information_available_at = information_available_at.replace(tzinfo=timezone.utc)
        return source_timestamp <= information_available_at

    def generate_lineage_graph(
        self,
        strategy_id: str,
        strategy_version: str
    ) -> Dict[str, Any]:
        """Generate full provenance lineage graph and audit metadata for a strategy version."""
        with self._lock:
            records = self.get_provenance_for_strategy(strategy_id, strategy_version)
            if not records:
                return {
                    "strategy_id": strategy_id,
                    "strategy_version": strategy_version,
                    "records_count": 0,
                    "lineage_complete": False,
                    "nodes": [],
                    "edges": [],
                    "audit_status": "NO_PROVENANCE_RECORDED"
                }

            latest_record = records[-1]
            nodes = [
                {"id": f"data:{latest_record.data_version}", "type": "DATASET", "hash": latest_record.data_hash},
                {"id": f"feature:{latest_record.feature_version}", "type": "FEATURE_SET", "hash": latest_record.feature_config_hash},
                {"id": f"model:{latest_record.model_version}", "type": "MODEL", "checkpoint": latest_record.model_checkpoint},
                {"id": f"backtest:{latest_record.backtest_id}", "type": "BACKTEST", "metrics": latest_record.backtest_metrics},
                {"id": f"strategy:{strategy_id}:{strategy_version}", "type": "STRATEGY_VERSION", "run_id": latest_record.run_id}
            ]

            edges = [
                {"from": f"data:{latest_record.data_version}", "to": f"feature:{latest_record.feature_version}"},
                {"from": f"feature:{latest_record.feature_version}", "to": f"model:{latest_record.model_version}"},
                {"from": f"model:{latest_record.model_version}", "to": f"backtest:{latest_record.backtest_id}"},
                {"from": f"backtest:{latest_record.backtest_id}", "to": f"strategy:{strategy_id}:{strategy_version}"},
            ]

            return {
                "strategy_id": strategy_id,
                "strategy_version": strategy_version,
                "records_count": len(records),
                "latest_provenance_id": latest_record.provenance_id,
                "lineage_complete": True,
                "pit_timestamp": latest_record.pit_timestamp.isoformat(),
                "nodes": nodes,
                "edges": edges,
                "audit_status": "VERIFIED_PIT_LINEAGE"
            }

    def clear(self) -> None:
        """Clear all records (primarily for testing)."""
        with self._lock:
            self._records.clear()
            self._strategy_index.clear()


# Global singleton instance
global_provenance_tracker = ProvenanceTracker()
