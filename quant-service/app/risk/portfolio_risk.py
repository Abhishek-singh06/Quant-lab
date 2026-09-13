"""
Portfolio Risk and Covariance Calculator for QuantLab Part 14.
Calculates portfolio variance, covariance matrices, and annualized portfolio volatility.
"""

from typing import Dict, List, Optional
import numpy as np
import pandas as pd


class PortfolioRiskCalculator:
    """Calculates multi-asset portfolio variance and volatility."""

    def calculate_portfolio_volatility(
        self,
        weights: np.ndarray,
        cov_matrix: np.ndarray,
        annualization_factor: float = np.sqrt(252.0)
    ) -> float:
        """
        Portfolio Variance = w^T * Sigma * w
        Portfolio Volatility = sqrt(Portfolio Variance) * sqrt(252)
        """
        if len(weights) == 0 or cov_matrix.size == 0:
            return 0.0

        weights = np.asarray(weights, dtype=float)
        cov_matrix = np.asarray(cov_matrix, dtype=float)

        if weights.shape[0] != cov_matrix.shape[0] or cov_matrix.shape[0] != cov_matrix.shape[1]:
            raise ValueError(f"Weight vector shape {weights.shape} incompatible with covariance matrix {cov_matrix.shape}")

        port_var = float(np.dot(weights.T, np.dot(cov_matrix, weights)))
        port_var = max(0.0, port_var)
        
        daily_vol = np.sqrt(port_var)
        annualized_vol = daily_vol * annualization_factor
        return float(annualized_vol)

    def compute_sample_covariance_matrix(
        self,
        returns_df: pd.DataFrame
    ) -> np.ndarray:
        """Computes sample daily covariance matrix strictly from historical point-in-time returns."""
        if returns_df.empty:
            return np.array([[]])
        return returns_df.cov().values
