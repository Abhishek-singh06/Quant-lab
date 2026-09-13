"""Indian Exchange Trading Calendar & Session Management.

Handles NSE/BSE trading sessions, weekends, official exchange holidays,
Muhurat special trading sessions, and missing session detection.
"""

from typing import List, Set, Dict, Any, Optional
from datetime import date, timedelta
import pandas as pd


class IndianTradingCalendar:
    """Trading calendar for Indian Stock Exchanges (NSE / BSE)."""

    def __init__(self):
        self._holidays: Set[date] = set()
        self._special_sessions: Dict[date, str] = {}
        self._seed_exchange_holidays()

    def is_trading_day(self, dt: date) -> bool:
        """Determines if a given date is an official trading day."""
        # Check special session override (e.g. Muhurat trading on Sunday/holiday)
        if dt in self._special_sessions:
            return True

        # Check weekend
        if dt.weekday() >= 5:  # 5=Saturday, 6=Sunday
            return False

        # Check official holiday
        if dt in self._holidays:
            return False

        return True

    def get_trading_days_between(self, start_date: date, end_date: date) -> List[date]:
        """Returns list of all official trading dates in interval [start_date, end_date]."""
        days: List[date] = []
        curr = start_date
        while curr <= end_date:
            if self.is_trading_day(curr):
                days.append(curr)
            curr += timedelta(days=1)
        return days

    def count_expected_sessions(self, start_date: date, end_date: date) -> int:
        """Returns total expected market sessions between start_date and end_date."""
        return len(self.get_trading_days_between(start_date, end_date))

    def detect_missing_sessions(self, actual_dates: List[date], start_date: date, end_date: date) -> List[date]:
        """Identifies any trading sessions that were expected but missing from data."""
        expected = set(self.get_trading_days_between(start_date, end_date))
        actual = set(actual_dates)
        missing = expected - actual
        return sorted(list(missing))

    def _seed_exchange_holidays(self):
        """Seeds major Indian market holidays (2020 - 2026)."""
        # Standard recurring Indian Exchange Holidays
        holidays_list = [
            # 2024
            date(2024, 1, 22),  # Special Holiday
            date(2024, 1, 26),  # Republic Day
            date(2024, 3, 8),   # Mahashivratri
            date(2024, 3, 25),  # Holi
            date(2024, 3, 29),  # Good Friday
            date(2024, 4, 11),  # Id-Ul-Fitr
            date(2024, 4, 17),  # Shri Ram Navami
            date(2024, 5, 1),   # Maharashtra Day
            date(2024, 5, 20),  # General Parliamentary Elections
            date(2024, 6, 17),  # Bakri Id
            date(2024, 7, 17),  # Moharram
            date(2024, 8, 15),  # Independence Day
            date(2024, 10, 2),  # Mahatma Gandhi Jayanti
            date(2024, 11, 1),  # Diwali Laxmi Pujan (Muhurat Trading only)
            date(2024, 11, 15), # Gurunanak Jayanti
            date(2024, 12, 25), # Christmas

            # 2025
            date(2025, 2, 26),  # Mahashivratri
            date(2025, 3, 14),  # Holi
            date(2025, 3, 31),  # Id-Ul-Fitr
            date(2025, 4, 14),  # Dr. Baba Saheb Ambedkar Jayanti
            date(2025, 4, 18),  # Good Friday
            date(2025, 5, 1),   # Maharashtra Day
            date(2025, 8, 15),  # Independence Day
            date(2025, 8, 27),  # Ganesh Chaturthi
            date(2025, 10, 2),  # Mahatma Gandhi Jayanti
            date(2025, 10, 21), # Diwali Laxmi Pujan
            date(2025, 10, 22), # Diwali Balipratipada
            date(2025, 11, 5),  # Gurunanak Jayanti
            date(2025, 12, 25), # Christmas

            # 2026
            date(2026, 1, 26),  # Republic Day
            date(2026, 3, 4),   # Holi
            date(2026, 3, 20),  # Id-Ul-Fitr
            date(2026, 4, 3),   # Good Friday
            date(2026, 4, 14),  # Dr Ambedkar Jayanti
            date(2026, 5, 1),   # Maharashtra Day
            date(2026, 8, 15),  # Independence Day
            date(2026, 10, 2),  # Gandhi Jayanti
            date(2026, 11, 10), # Diwali
            date(2026, 12, 25), # Christmas
        ]

        self._holidays.update(holidays_list)
        # Seed Muhurat trading special sessions
        self._special_sessions[date(2024, 11, 1)] = "Diwali Muhurat Trading (18:00 - 19:00 IST)"
        self._special_sessions[date(2025, 10, 21)] = "Diwali Muhurat Trading"
