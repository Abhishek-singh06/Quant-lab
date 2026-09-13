"""Horizon-Specific Model Registry.

Manages model versions, status (CANDIDATE, VALIDATED, CHAMPION, RETIRED),
and enforces Point-in-Time Model Availability timestamps.
"""

from typing import Dict, List, Optional
from datetime import datetime

from app.horizons.schemas import TradingHorizon, ModelStatus
from app.horizons.models.base import HorizonModel


class HorizonModelRegistry:
    """Registry maintaining active and historical models by horizon."""

    def __init__(self):
        self._models: Dict[tuple, HorizonModel] = {}
        self._champions: Dict[TradingHorizon, str] = {}

    def register_model(self, model: HorizonModel, is_champion: bool = False):
        key = (model.horizon, model.model_id, model.model_version)
        self._models[key] = model
        if is_champion:
            model.status = ModelStatus.CHAMPION
            self._champions[model.horizon] = model.model_version

    def get_model(self, horizon: TradingHorizon, model_id: str, model_version: str) -> Optional[HorizonModel]:
        return self._models.get((horizon, model_id, model_version))

    def get_champion_model(
        self,
        horizon: TradingHorizon,
        as_of: Optional[datetime] = None
    ) -> Optional[HorizonModel]:
        """Return the active champion model for horizon, validating model availability timestamp."""
        champ_ver = self._champions.get(horizon)
        if not champ_ver:
            for (h, m_id, m_ver), model in self._models.items():
                if h == horizon and model.status in [ModelStatus.CHAMPION, ModelStatus.VALIDATED]:
                    if as_of is None or (model.model_availability_timestamp and model.model_availability_timestamp <= as_of):
                        return model
            return None

        for (h, m_id, m_ver), model in self._models.items():
            if h == horizon and m_ver == champ_ver:
                if as_of is None or (model.model_availability_timestamp and model.model_availability_timestamp <= as_of):
                    return model
        return None

    def list_models_by_horizon(self, horizon: TradingHorizon) -> List[HorizonModel]:
        return [m for (h, _, _), m in self._models.items() if h == horizon]
