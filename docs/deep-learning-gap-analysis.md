# Deep Learning & Temporal Sequence Modeling Gap Analysis

**Phase**: Phase 11 — Clean-Room Native Deep Learning Model Engine  
**Target Platform**: QuantLab (`E:/VS CODE MAIN/PROJECTS/Quant-lab`)  
**Audit Date**: September 2026  

---

## 1. Context & Motivation

QuantLab currently utilizes robust Classical Machine Learning baselines (LightGBM/HistGradientBoosting, Ridge Regression, Random Forest) through the `QuantPredictionModel` and `QuantLabQlibModelBridge` interfaces. While GBDT models excel on cross-sectional tabular features (Alpha158, Alpha360), they treat sequential time steps as flattened feature columns, losing natural recurrence and dynamic temporal path dependencies.

To address this gap without falling into the empirical traps of naive deep learning, QuantLab designs a native **Temporal Deep Learning Engine** with mathematical guarantees around Point-In-Time scaling, Purge & Embargo protection, and strict walk-forward benchmarking against Classical ML baselines.

---

## 2. Quantitative Gap Analysis Matrix

| Dimensional Area | Existing QuantLab Baseline | Reference Repo (`Stock-Prediction-Models`) | Phase 11 Target Implementation |
| :--- | :--- | :--- | :--- |
| **Input Representation** | 2D Tabular Matrix $(N \times D)$ | 3D Matrix with Look-ahead / Leakage | 3D Temporal Tensor $(N \times T \times D)$ with Strict Per-Symbol Lookback Buffer ($T=60$) |
| **Normalization** | Tabular Scaler per fold | Global `MinMaxScaler` across entire dataset | `PointInTimeScaler` (Z-Score & Robust Median Scaler fit strictly on training fold) |
| **Sequence Windows** | Single-bar snapshots | Sliding window with boundary overlap | Multi-symbol sequence window generator with zero cross-symbol leakage and zero future bars |
| **Purge & Embargo** | Walk-forward split | No purge / No embargo | Purge overlapping horizon $H$ and embargo $E$ bars between train and validation partitions |
| **Model Architectures** | GBDT, Ridge, Random Forest | Keras/TF 1.x LSTM/GRU/RNN | Vectorized NumPy/SciPy Sequence Engine + Optional PyTorch: `TemporalGRUModel`, `TemporalLSTMModel`, `Temporal1DCNNModel`, `TemporalAttentionModel` |
| **Evaluation Metrics** | IC, Rank IC, Sharpe, R², MAE | Visual MSE on Price (Spurious) | OOS Information Coefficient (IC), Rank IC, Information Ratio (IR), Directional Accuracy, Max Drawdown |
| **Classical Comparison** | Standalone GBDT | Absent | Rigorous Walk-Forward Baseline Comparison (Deep Learning vs LightGBM vs Ensemble) |
| **Production Fallback** | CPU Single-thread / Multi-thread | GPU / Unchecked CUDA | 100% Deterministic CPU vectorized fallback with explicit random seed controls |

---

## 3. Detailed Architecture Specifications

### A. Point-In-Time Sequence Generation (`sequence_generator.py`)
- Given tabular panel data with columns `[timestamp, symbol, feature_1, ..., feature_k, target]`:
- Groups data by `symbol`, sorted strictly by `timestamp ASC`.
- Constructs 3D sequences: $\mathbf{X}_i \in \mathbb{R}^{T \times D}$ where $T$ is the lookback window (e.g. 60 bars) and $D$ is feature dimension.
- Aligns target $y_i$ to the forward horizon ($T+1, T+5, T+20$) relative to the final bar of the sequence window.
- Ensures no sequence spans across symbol boundaries.

### B. Point-In-Time Scaler (`scaler.py`)
- Standardizes features along feature dimension using parameters computed purely from training fold:
  $$\mu_j = \frac{1}{N_{train} \cdot T} \sum_{i, t} X_{i, t, j}, \quad \sigma_j = \sqrt{\frac{1}{N_{train} \cdot T} \sum_{i, t} (X_{i, t, j} - \mu_j)^2 + \epsilon}$$
- Robust median/IQR scaling option for heavy-tailed financial time series.

### C. Purge & Embargo Logic (`purge_embargo.py`)
- Prevents train/test information leakage caused by overlapping sequence windows and multi-step forward return labels.
- Purge: Removes training samples whose forward target window overlaps with the test set start.
- Embargo: Excludes initial test-following samples to eliminate auto-correlation spillover.

### D. Vectorized Deep Learning Architectures (`models.py`)
- **`TemporalGRUModel`**: Gated Recurrent Unit model with reset and update gates, capturing short-to-medium memory dependencies.
- **`TemporalLSTMModel`**: Long Short-Term Memory model with input, forget, and output gates for persistent memory states.
- **`Temporal1DCNNModel`**: Dilated 1D temporal convolution model extracting multi-scale temporal patterns.
- **`TemporalAttentionModel`**: Temporal self-attention computing interpretable alpha importance over historical time steps.

### E. Model Registry & Ensemble (`adapter.py`, `ensemble.py`)
- Bridges all deep learning sequence models into QuantLab's canonical `QuantPredictionModel` interface.
- Serializes model weights, scaling parameters, hyperparameters, and cryptographic SHA-256 provenance hashes.
- `DeepLearningEnsembleModel` dynamically combines Classical GBDT and Recurrent Deep Learning predictions.
