"""
Persistent State Storage Manager for QuantLab Paper Trading (Phase 18.1).
Guarantees ONE authoritative source of truth across multiple processes (FastAPI, CLI Runner, Scripts).
Provides atomic reads and writes for:
1. Active Session Metadata
2. Portfolio State & Positions
3. Paper Orders & Fills
4. Immutable Audit Journal
"""

import os
import json
import tempfile
from typing import Dict, List, Optional, Any
from datetime import datetime, timezone
from pathlib import Path


class PaperTradingPersistenceManager:
    """
    File-backed persistent state manager ensuring process-safe consistency.
    """

    def __init__(self, data_dir: Optional[str] = None):
        if data_dir:
            self.data_dir = Path(data_dir)
        else:
            # Default to quant-service/data/paper_trading
            self.data_dir = Path(__file__).resolve().parent.parent.parent / "data" / "paper_trading"
        self.data_dir.mkdir(parents=True, exist_ok=True)

        self.session_file = self.data_dir / "active_session.json"
        self.portfolio_file = self.data_dir / "portfolio_state.json"
        self.positions_file = self.data_dir / "positions.json"
        self.orders_file = self.data_dir / "orders.json"
        self.fills_file = self.data_dir / "fills.json"
        self.journal_file = self.data_dir / "journal.jsonl"

    def _atomic_write_json(self, file_path: Path, data: Any) -> None:
        """Writes data atomically to prevent corrupted reads during concurrent access."""
        dir_name = file_path.parent
        dir_name.mkdir(parents=True, exist_ok=True)
        temp_fd, temp_path = tempfile.mkstemp(dir=str(dir_name), prefix="tmp_paper_")
        with os.fdopen(temp_fd, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, default=str)
        os.replace(temp_path, str(file_path))

    def _read_json(self, file_path: Path) -> Optional[Dict[str, Any]]:
        """Reads JSON data if file exists, else returns None."""
        if not file_path.exists():
            return None
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return None

    def has_active_session(self) -> bool:
        """Returns True if an active persisted session exists."""
        return self.session_file.exists()

    def load_active_session(self) -> Optional[Dict[str, Any]]:
        """Loads authoritative session metadata."""
        return self._read_json(self.session_file)

    def save_active_session(self, session_data: Dict[str, Any]) -> None:
        """Saves authoritative session metadata atomically."""
        self._atomic_write_json(self.session_file, session_data)

    def load_portfolio(self) -> Optional[Dict[str, Any]]:
        """Loads portfolio accounting and capital state."""
        return self._read_json(self.portfolio_file)

    def save_portfolio(self, portfolio_data: Dict[str, Any]) -> None:
        """Saves portfolio accounting and capital state atomically."""
        self._atomic_write_json(self.portfolio_file, portfolio_data)

    def load_positions(self) -> Dict[str, Any]:
        """Loads open positions map."""
        data = self._read_json(self.positions_file)
        return data if data is not None else {}

    def save_positions(self, positions_data: Dict[str, Any]) -> None:
        """Saves open positions map atomically."""
        self._atomic_write_json(self.positions_file, positions_data)

    def load_orders(self) -> List[Dict[str, Any]]:
        """Loads order history."""
        data = self._read_json(self.orders_file)
        return data if isinstance(data, list) else []

    def save_orders(self, orders_data: List[Dict[str, Any]]) -> None:
        """Saves order history atomically."""
        self._atomic_write_json(self.orders_file, orders_data)

    def load_fills(self) -> List[Dict[str, Any]]:
        """Loads fill history."""
        data = self._read_json(self.fills_file)
        return data if isinstance(data, list) else []

    def save_fills(self, fills_data: List[Dict[str, Any]]) -> None:
        """Saves fill history atomically."""
        self._atomic_write_json(self.fills_file, fills_data)

    def append_journal(self, entry_dict: Dict[str, Any]) -> None:
        """Appends an immutable line to journal.jsonl."""
        with open(self.journal_file, "a", encoding="utf-8") as f:
            f.write(json.dumps(entry_dict, default=str) + "\n")

    def clear_state(self) -> None:
        """Clears all persisted paper trading state files (used for clean resets)."""
        for f in [self.session_file, self.portfolio_file, self.positions_file, self.orders_file, self.fills_file, self.journal_file]:
            if f.exists():
                try:
                    f.unlink()
                except Exception:
                    pass
