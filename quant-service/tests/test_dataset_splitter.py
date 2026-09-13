"""Tests for chronological dataset splitting."""

import pandas as pd
import pytest
from app.warehouse.dataset_splitter import ChronologicalDatasetSplitter


def test_chronological_splitting_ordering():
    dates = pd.date_range(start="2020-01-01", periods=100, freq="B").strftime("%Y-%m-%d").tolist()
    df = pd.DataFrame({'date': dates, 'val': range(100)})

    train, val, test = ChronologicalDatasetSplitter.train_val_test_split(df, 0.7, 0.15, 0.15)

    assert len(train) == 70
    assert len(val) == 15
    assert len(test) == 15

    # Strictly chronological: max(train.date) < min(val.date) < min(test.date)
    assert train['date'].max() < val['date'].min()
    assert val['date'].max() < test['date'].min()
