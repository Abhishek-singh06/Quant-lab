"""
Paper Trading Clock supporting real-time LIVE_CLOCK and deterministic REPLAY_CLOCK.
Enforces timezone-aware timestamp synchronization (IST and UTC) and official Indian exchange calendar.
"""

from datetime import datetime, timezone, time, timedelta
from typing import Optional
from zoneinfo import ZoneInfo

from app.paper.schemas import ClockType
from app.data_acquisition.trading_calendar import IndianTradingCalendar

# Indian Standard Time (UTC+5:30)
IST_TZ = timezone(timedelta(hours=5, minutes=30), name="IST")


class PaperTradingClock:
    """
    Clock provider for paper trading sessions.
    - LIVE_CLOCK: returns current wall-clock UTC timestamp.
    - REPLAY_CLOCK: advances deterministically step-by-step through recorded ticks/bars.
    - Integrates with IndianTradingCalendar to respect NSE holidays and sessions.
    """

    def __init__(
        self,
        clock_type: ClockType = ClockType.LIVE_CLOCK,
        initial_replay_time: Optional[datetime] = None,
        calendar: Optional[IndianTradingCalendar] = None
    ):
        self.clock_type = clock_type
        self._current_replay_time = initial_replay_time
        self.calendar = calendar or IndianTradingCalendar()

    def now(self) -> datetime:
        if self.clock_type == ClockType.LIVE_CLOCK:
            return datetime.now(timezone.utc)
        return self._current_replay_time if self._current_replay_time is not None else datetime.now(timezone.utc)

    def advance_to(self, new_time: datetime):
        """Advances replay clock to next event timestamp."""
        if self.clock_type == ClockType.REPLAY_CLOCK:
            if self._current_replay_time is not None and new_time < self._current_replay_time:
                raise ValueError(f"Cannot rewind REPLAY_CLOCK backwards: {new_time} < {self._current_replay_time}")
            self._current_replay_time = new_time

    def to_ist(self, dt: datetime) -> datetime:
        """Converts any datetime to IST (Indian Standard Time)."""
        if dt.tzinfo is None:
            # Assume UTC if naive
            dt_utc = dt.replace(tzinfo=timezone.utc)
        else:
            dt_utc = dt.astimezone(timezone.utc)
        return dt_utc.astimezone(IST_TZ)

    def is_market_open(self, dt: Optional[datetime] = None) -> bool:
        """
        Checks if the provided timestamp falls within official NSE/BSE trading sessions:
        1. Checks trading calendar for holidays and weekend rules.
        2. Checks regular session time window (09:15 to 15:30 IST).
        3. Supports special Muhurat trading sessions if applicable.
        """
        curr = dt or self.now()
        ist_dt = self.to_ist(curr)
        trading_date = ist_dt.date()

        # Check if date is a valid trading day on the Indian exchange calendar
        if not self.calendar.is_trading_day(trading_date):
            return False

        current_time = ist_dt.time()

        # Check special session (e.g. Diwali Muhurat trading evening window)
        if trading_date in self.calendar._special_sessions:
            # Muhurat trading usually 18:00 to 19:15 IST
            muhurat_open = time(18, 0)
            muhurat_close = time(19, 15)
            if muhurat_open <= current_time <= muhurat_close:
                return True

        # Regular NSE equity session: 09:15 to 15:30 IST
        market_open_time = time(9, 15)
        market_close_time = time(15, 30)

        return market_open_time <= current_time <= market_close_time
