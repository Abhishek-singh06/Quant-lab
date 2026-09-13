# Stock-Prediction-Models Reference Architecture & Methodological Analysis

**Reference Repository**: [huseinzol05/Stock-Prediction-Models](https://github.com/huseinzol05/Stock-Prediction-Models)  
**Target Platform**: QuantLab (`E:/VS CODE MAIN/PROJECTS/Quant-lab`)  
**Audit Date**: September 2026  

---

## 1. Executive Summary

`huseinzol05/Stock-Prediction-Models` is an archived, popular open-source repository containing over 50 deep learning notebooks applied to financial time-series. While the repository provides broad pedagogical coverage of sequence models (RNN, LSTM, GRU, Bi-LSTM, Seq2Seq, Temporal Attention, 1D-CNN, DQN, Evolution Strategies), an adversarial quantitative audit reveals critical methodological flaws that render naive reproductions invalid for live institutional trading.

QuantLab conducts this reference audit to isolate the useful mathematical mechanics of temporal sequence modeling while systematically eliminating all econometric and statistical biases.

---

## 2. Anatomical Audit of Reference Architecture

The reference repository organizes models into several families:
1. **Recurrent Models**: Standard RNN, Vanilla LSTM, Stacked LSTM, Bidirectional LSTM, GRU, Stacked GRU.
2. **Seq2Seq & Attention**: Encoder-Decoder LSTM architectures with Luong and Bahdanau attention mechanisms.
3. **Convolutional Time Series**: 1D Temporal Dilated Convolutions (WaveNet-style / Temporal Convolutional Networks).
4. **Reinforcement Learning & Heuristics**: Deep Q-Network (DQN), Double DQN, Policy Gradient, Evolution Strategies (ES) agent.

---

## 3. Critical Methodological Flaws in Reference Models

Our quantitative audit identified five fundamental structural defects in naive stock prediction workflows:

### A. The "Lag Illusion" (Naive Price Level Target)
- **Reference Flaw**: Reference notebooks train neural networks to predict the absolute closing price at $T+1$: $\hat{P}_{t+1} \approx f(P_{t}, P_{t-1}, \dots)$.
- **Why It Fails**: In non-stationary random walks ($P_{t+1} = P_t + \epsilon_{t+1}$), a model minimizing Mean Squared Error (MSE) simply learns the identity function $\hat{P}_{t+1} \approx P_t$. Plots show visually stunning alignment, but when examined closely, the prediction is merely a 1-day lagged copy of the input.
- **QuantLab Native Rule**: Models must predict **stationarized forward returns** ($r_{t+k} = \frac{P_{t+k} - P_t}{P_t}$) or directional classification ($y = \mathbf{1}_{r_{t+k} > 0}$), never raw price levels.

### B. Global Normalization Leakage (Future Look-Ahead)
- **Reference Flaw**: `MinMaxScaler().fit_transform(df)` is executed on the entire dataset *before* slicing into train and test segments.
- **Why It Fails**: The global $\min(X)$ and $\max(X)$ contain future extreme price points, allowing the network during training to anticipate future market highs and lows.
- **QuantLab Native Rule**: `PointInTimeScaler` must be strictly fitted on the training split only ($\mu_{train}, \sigma_{train}$) and frozen during out-of-sample (OOS) validation and inference.

### C. Sequence Overlap & Cross-Fold Contamination (Lack of Purge & Embargo)
- **Reference Flaw**: Rolling sliding windows of length $L=60$ are constructed without accounting for sample overlap across train/test splits.
- **Why It Fails**: If test window starts at index $T$, the train window at index $T-1$ already shares 59 bars of identical historical input, leaking information into the test partition. Furthermore, overlapping forward return targets require purging the label horizon ($H$) and embargoing post-test samples.
- **QuantLab Native Rule**: Strict **Purge & Embargo** rules must be applied during walk-forward cross-validation.

### D. Multi-Symbol Cross-Contamination
- **Reference Flaw**: Sliding sequences concatenate multiple symbols sequentially without resetting the lookback buffer across symbol boundaries.
- **Why It Fails**: The initial 60 bars of Symbol B contain trailing bars from Symbol A.
- **QuantLab Native Rule**: Sequence generators must partition and window data strictly by symbol, rejecting cross-symbol sequence bleeding.

### E. Zero Market Frictions & Survivorship Bias
- **Reference Flaw**: Trading simulations assume frictionless instantaneous execution at mid-price without Securities Transaction Tax (STT), exchange turnover charges, stamp duty, SEBI turnover fees, or bid-ask slippage.
- **QuantLab Native Rule**: Walk-forward backtests must incorporate realistic Indian market transaction costs (15 bps round-trip for equity delivery/intraday).

---

## 4. Key Architectural Ideas Retained

1. **Temporal Gating & Memory Cells**: GRU (Gated Recurrent Unit) and LSTM architectures effectively capture multi-scale temporal dependencies without exploding/vanishing gradients.
2. **Temporal 1D Convolution**: Dilated 1D convolutions efficiently capture local pattern hierarchies and multi-day price momentum shapes.
3. **Self-Attention over Sequence Steps**: Calculating temporal attention weights allows inspection of which historical days ($t-60$ to $t$) contributed most to the alpha prediction.

---

## 5. Architectural Comparison Matrix

| Capability | Reference Notebooks | QuantLab Native DL Engine |
| :--- | :--- | :--- |
| **Prediction Target** | Absolute Price $P_{t+1}$ (Spurious) | Forward Returns ($r_{t+1}, r_{t+5}, r_{t+20}$) / Sign |
| **Feature Scaling** | Global `MinMaxScaler` (Look-ahead) | `PointInTimeScaler` (Strict Train-Only) |
| **Cross-Validation** | Single Random/Arbitrary Split | Walk-Forward with Purge & Embargo |
| **Symbol Isolation** | None (Cross-symbol bleeding) | Strict Per-Symbol Sequence Generation |
| **Baseline Benchmark** | None (Assumes DL is best) | Strict Comparison vs LightGBM & Ridge |
| **Execution Frictions** | Zero costs / Frictionless | Realistic NSE/BSE Transaction Costs |
| **Model Registry** | Ad-hoc notebook state | `QuantPredictionModel` with SHA-256 Provenance |
