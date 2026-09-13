"""Tests for Indian Trading Calendar and Gap Detection."""

import pytest
from datetime import date

from app.data_acquisition.trading_calendar import IndianTradingCalendar


class TestIndianTradingCalendar:

    def test_weekend_identification(self):
        cal = IndianTradingCalendar()
        # 2024-06-08 is Saturday, 2024-06-09 is Sunday
        assert cal.is_trading_day(date(2024, 6, 8)) is False
        assert cal.is_trading_day(date(2024, 6, 9)) is False
        # 2024-06-10 is Monday (Trading day)
        assert cal.is_trading_day(date(2024, 6, 10)) is True

    def test_exchange_holiday_identification(self):
        cal = IndianTradingCalendar()
        # Republic Day: 2024-01-26
        assert cal.is_trading_day(date(2024, 1, 26)) is False
        # Independence Day: 2024-08-15
        assert cal.is_trading_day(date(2024, 8, 15)) is False
        # Gandhi Jayanti: 2024-10-02
        assert cal.is_trading_day(date(2024, 10, 2)) is False

    def test_muhurat_trading_special_session(self):
        cal = IndianTradingCalendar()
        # Diwali Muhurat session on 2024-11-01
        assert cal.is_trading_day(date(2024, 11, 1)) is True

    def test_missing_session_gap_detection(self):
        cal = IndianTradingCalendar()
        start = date(2024, 6, 3)   # Monday
        end = date(2024, 6, 7)     # Friday
        # Total expected days: 5 (Mon to Fri)
        expected = cal.get_trading_days_between(start, end)
        assert len(expected) == 5

        # Simulate feed missing Wednesday (2024-06-05)
        actual = [date(2024, 6, 3), date(2024, 6, 4), date(2024, 6, 6), date(2024, 6, 7)]
        missing = cal.detect_missing_sessions(actual, start, end)
        assert len(missing) == 1
        assert missing[0] == date(2024, 6, 5)
