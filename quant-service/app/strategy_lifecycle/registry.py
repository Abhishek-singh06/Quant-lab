"""Immutable Strategy Registry.

Manages versioned quantitative strategy definitions with strict immutability,
version comparison, and lifecycle state management.
"""

import threading
from typing import Dict, List, Optional, Any, Tuple
from datetime import datetime, timezone
import re

from app.strategy_lifecycle.models import StrategyDefinition, StrategyStatus


def _parse_semver(version_str: str) -> Tuple[int, int, int]:
    """Parse semantic version 'X.Y.Z' into a comparable tuple."""
    match = re.match(r"^v?(\d+)\.(\d+)\.(\d+)", version_str.strip())
    if match:
        return (int(match.group(1)), int(match.group(2)), int(match.group(3)))
    # Fallback for non-standard versions
    return (0, 0, 0)


class StrategyRegistry:
    """Thread-safe registry for immutable strategy versions."""

    def __init__(self) -> None:
        self._lock = threading.RLock()
        # Key: (strategy_id, version) -> StrategyDefinition
        self._strategies: Dict[Tuple[str, str], StrategyDefinition] = {}

    def register_strategy(self, definition: StrategyDefinition) -> StrategyDefinition:
        """Register a new strategy version. Enforces strict immutability."""
        with self._lock:
            key = (definition.strategy_id, definition.version)
            if key in self._strategies:
                raise ValueError(
                    f"Strategy '{definition.strategy_id}' version '{definition.version}' "
                    f"already exists. Strategy versions are immutable. Please increment the version."
                )
            
            stored = definition.model_copy(deep=True)
            stored.created_at = datetime.now(timezone.utc)
            stored.updated_at = stored.created_at
            self._strategies[key] = stored
            return stored.model_copy(deep=True)

    def get_strategy(self, strategy_id: str, version: Optional[str] = None) -> Optional[StrategyDefinition]:
        """Get a strategy by ID and optional version. Returns latest version if version is None."""
        with self._lock:
            if version is not None:
                strat = self._strategies.get((strategy_id, version))
                return strat.model_copy(deep=True) if strat else None

            # Find all versions for strategy_id and return the highest semver
            versions = [
                (v, strat) for (s_id, v), strat in self._strategies.items()
                if s_id == strategy_id
            ]
            if not versions:
                return None

            versions.sort(key=lambda item: _parse_semver(item[0]), reverse=True)
            return versions[0][1].model_copy(deep=True)

    def get_versions(self, strategy_id: str) -> List[str]:
        """List all registered semantic versions for a strategy, sorted descending."""
        with self._lock:
            versions = [
                v for (s_id, v) in self._strategies.keys()
                if s_id == strategy_id
            ]
            versions.sort(key=_parse_semver, reverse=True)
            return versions

    def list_strategies(
        self,
        status: Optional[StrategyStatus] = None,
        horizon: Optional[str] = None,
        author: Optional[str] = None,
    ) -> List[StrategyDefinition]:
        """List strategies matching optional filters."""
        with self._lock:
            results = []
            for strat in self._strategies.values():
                if status and strat.status != status:
                    continue
                if horizon and strat.horizon.upper() != horizon.upper():
                    continue
                if author and author.lower() not in strat.author.lower():
                    continue
                results.append(strat.model_copy(deep=True))
            return results

    def update_status(
        self,
        strategy_id: str,
        version: str,
        new_status: StrategyStatus
    ) -> StrategyDefinition:
        """Update the lifecycle status of a registered strategy."""
        with self._lock:
            key = (strategy_id, version)
            if key not in self._strategies:
                raise KeyError(f"Strategy '{strategy_id}' version '{version}' not found.")
            
            strat = self._strategies[key]
            strat.status = new_status
            strat.updated_at = datetime.now(timezone.utc)
            return strat.model_copy(deep=True)

    def clone_strategy(
        self,
        strategy_id: str,
        from_version: str,
        new_version: str,
        overrides: Optional[Dict[str, Any]] = None
    ) -> StrategyDefinition:
        """Clone an existing strategy version to a new version with optional modifications."""
        with self._lock:
            parent = self.get_strategy(strategy_id, from_version)
            if not parent:
                raise KeyError(f"Parent strategy '{strategy_id}' version '{from_version}' not found.")

            data = parent.model_dump()
            data["version"] = new_version
            data["status"] = StrategyStatus.DRAFT
            data["created_at"] = datetime.now(timezone.utc)
            data["updated_at"] = datetime.now(timezone.utc)
            
            if overrides:
                for k, v in overrides.items():
                    if k in data and k not in ("strategy_id", "version", "created_at"):
                        data[k] = v

            new_strat = StrategyDefinition(**data)
            return self.register_strategy(new_strat)

    def clear(self) -> None:
        """Clear all registered strategies (primarily for testing)."""
        with self._lock:
            self._strategies.clear()


# Global singleton instance
global_strategy_registry = StrategyRegistry()
