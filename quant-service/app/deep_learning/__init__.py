"""QuantLab Clean-Room Deep Learning & Temporal Sequence Modeling Engine.

Provides point-in-time safe sequence generation, robust temporal scaling,
purge & embargo protection, vectorized recurrent/convolutional architectures,
and model registry adapters for quantitative research.
"""

from app.deep_learning.scaler import PointInTimeScaler
from app.deep_learning.sequence_generator import TemporalSequenceGenerator, SequenceDataset
from app.deep_learning.purge_embargo import PurgeAndEmbargoValidator, TimeSeriesSplitPurged
from app.deep_learning.models import (
    TemporalGRUModel,
    TemporalLSTMModel,
    Temporal1DCNNModel,
    TemporalAttentionModel,
)
from app.deep_learning.adapter import DeepLearningModelAdapter
from app.deep_learning.ensemble import WalkForwardEnsembleComparator

__all__ = [
    "PointInTimeScaler",
    "TemporalSequenceGenerator",
    "SequenceDataset",
    "PurgeAndEmbargoValidator",
    "TimeSeriesSplitPurged",
    "TemporalGRUModel",
    "TemporalLSTMModel",
    "Temporal1DCNNModel",
    "TemporalAttentionModel",
    "DeepLearningModelAdapter",
    "WalkForwardEnsembleComparator",
]
