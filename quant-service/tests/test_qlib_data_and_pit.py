"""Tests for Qlib Provider, Loader, Handler, and Leak-Free Data Partitioning."""

import pytest
import numpy as np
import pandas as pd

from app.qlib.provider import QuantLabQlibDataProvider
from app.qlib.loader import QuantLabDataLoader
from app.qlib.handler import (
    QuantLabDataHandler,
    CSZScoreProcessor,
    CSRankProcessor,
    RobustZScoreProcessor,
    MinMaxProcessor,
    WinsorizeProcessor,
)
from app.qlib.dataset import QuantLabDatasetH


def test_qlib_provider_and_pit_filtering():
    provider = QuantLabQlibDataProvider()
    instruments = ["RELIANCE", "TCS"]
    
    # Query with future as_of filter
    data = provider.get_market_data(
        instruments=instruments,
        start_time="2023-01-01",
        end_time="2023-03-31",
        as_of="2023-02-15",
    )
    
    assert not data.empty
    max_dt = data.index.get_level_values("datetime").max()
    assert max_dt <= pd.to_datetime("2023-02-15")
    assert "information_available_at" in data.columns
    assert "run_id" in data.columns


def test_qlib_loader_and_target_separation():
    loader = QuantLabDataLoader(feature_type="Alpha158", target_horizon=1)
    features_df, target_series, metadata_df = loader.load_data(
        instruments=["RELIANCE", "TCS"],
        start_time="2023-01-01",
        end_time="2023-03-31",
    )

    assert not features_df.empty
    assert not target_series.empty
    assert not metadata_df.empty
    assert len(features_df) == len(target_series)
    assert len(features_df) == len(metadata_df)
    assert "information_available_at" in metadata_df.columns


def test_qlib_data_handler_leak_free_splits():
    handler = QuantLabDataHandler(
        instruments=["RELIANCE", "TCS"],
        start_time="2023-01-01",
        end_time="2023-06-30",
        processors=[RobustZScoreProcessor(), WinsorizeProcessor()],
    )

    dataset = handler.setup_dataset(
        train_range=("2023-01-01", "2023-03-31"),
        valid_range=("2023-04-01", "2023-04-30"),
        test_range=("2023-05-01", "2023-06-30"),
    )

    assert isinstance(dataset, QuantLabDatasetH)
    assert set(dataset.list_segments()) == {"train", "valid", "test"}

    X_train, y_train = dataset.prepare("train", col_set=["feature", "label"])
    X_val, y_val = dataset.prepare("valid", col_set=["feature", "label"])
    X_test, y_test = dataset.prepare("test", col_set=["feature", "label"])

    assert len(X_train) > 0
    assert len(X_val) > 0
    assert len(X_test) > 0

    # Ensure train timestamps do not overlap with validation or test
    train_max_dt = X_train.index.get_level_values("datetime").max()
    val_min_dt = X_val.index.get_level_values("datetime").min()
    test_min_dt = X_test.index.get_level_values("datetime").min()

    assert train_max_dt < val_min_dt
    assert val_min_dt < test_min_dt
