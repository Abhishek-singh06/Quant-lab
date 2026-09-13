# PART 11 & 12: Production Quant Prediction Model Engine & Walk-Forward Training System

## 1. Overview & Architectural Philosophy

QuantLab's predictive intelligence platform implements an institutional econometric framework for Indian equity markets.

### Critical Anti-Leakage & Statistical Tenets:
1. **Zero Lookahead Bias & Point-in-Time Availability ($T_{\text{avail}} \le T$):**
   - Features at date $T$ only ingest signals available on or before the close of day $T$.
   - Target labels are strictly calculated forward ($T+1$ forward return, $T+1 \to T+5$ realized volatility).
2. **Strict Test Partition Isolation:**
   - Preprocessing transformers (winsorizing, median imputation, robust scaling) are **fitted strictly on the training partition ($X_{\text{train}}$)** and only *transformed* onto validation ($X_{\text{val}}$) and out-of-sample test ($X_{\text{test}}$) partitions.
3. **Purged & Embargoed Cross-Validation:**
   - Multi-day target labels (such as 5-day realized volatility) span several trading periods.
   - When splitting consecutive folds, a **Purging window ($H=5$ days)** eliminates train samples immediately preceding the test start date to prevent overlapping forward label contamination.
   - An **Embargo window ($E=2$ days)** drops early test samples following a train boundary to eliminate auto-correlation spillover.
4. **No Deep Learning / Neural Network Policy:**
   - Deep neural architectures (LSTMs, Transformers, MLPs) suffer severe non-stationarity, catastrophic overfitting, and interpretability failure in Indian equity cross-sections.
   - We utilize regularized linear/logistic models, tree ensembles (Random Forest, Gradient Boosting), and strict naive econometric baselines.
5. **Multi-Disciplinary Signal Fusion:**
   - Cross-sectional features fuse Part 9 Technical features, Part 8 Point-in-Time Fundamentals, Part 6 Institutional FII/DII flow signals, Part 7 Global Market Regime features, and Part 10 Market Regime classifications.

---

## 2. Model Taxonomy & Target Horizons

| Model ID | Target Variable | Horizon | Formulation | Algorithms Implemented | Primary Evaluation Metric | Baseline Comparison |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Model 1 (Regression)** | Expected Return | 1-Day Forward ($T+1$) | $E[R_{t+1}]$ | Ridge Regression, Random Forest Regressor, Gradient Boosting Regressor | Out-of-Sample $R^2$, RMSE, Directional Accuracy, Information Coefficient (IC) | Zero-return baseline, Historical mean return |
| **Model 2 (Classification)** | Direction Probability | 1-Day Forward ($T+1$) | $P(R_{t+1} > 0)$ | Logistic Regression (L2), Random Forest Classifier, Gradient Boosting Classifier | ROC-AUC, Brier Score, Balanced Accuracy, Precision/Recall | Majority class baseline, Random coin toss |
| **Model 3 (Volatility)** | Realized Volatility | 5-Day Forward ($T+1 \to T+5$) | $\sigma_{t+1:t+5}$ | Persistence Baseline, Historical Rolling Volatility, Random Forest Regressor, Gradient Boosting Regressor | Realized RMSE, MAE, Volatility Correlation | Volatility Persistence baseline ($\sigma_{t-5:t}$) |

---

## 3. Walk-Forward Evaluation Workflow

QuantLab evaluates all models across historical time series using both **Expanding Window** and **Rolling Window** walk-forward cross-validation.

```
       Fold 1:  [--- TRAIN ---] [P] [--- VAL ---] [E] [=== TEST (Out of Sample) ===]
       Fold 2:  [------ TRAIN ------] [P] [--- VAL ---] [E] [=== TEST (Out of Sample) ===]
       Fold 3:  [--------- TRAIN ---------] [P] [--- VAL ---] [E] [=== TEST (Out of Sample) ===]
       (Expanding Window Mode)
```

1. **Training Partition ($X_{\text{train}}, y_{\text{train}}$):**
   - Fits preprocessor scaling parameters.
   - Trains candidate model pool (Baseline, Linear, Trees, Gradient Boosting).
2. **Validation Partition ($X_{\text{val}}, y_{\text{val}}$):**
   - Evaluates candidate models on unseen data.
   - Selects Champion Model (highest validation IC / ROC-AUC / lowest RMSE).
3. **Out-of-Sample Test Partition ($X_{\text{test}}, y_{\text{test}}$):**
   - Evaluates Champion Model strictly once on sacred out-of-sample data.
   - Records out-of-sample metrics, cross-sectional ranking decile spreads, and regime-conditioned metrics (Bull, Bear, Sideways, High Vol).

---

## 4. REST API Endpoints

### Quant Prediction API (`/api/v1/models/*`):
- `GET /api/v1/models/active` — List registered production models with status, version, hyperparameters, and feature importance.
- `GET /api/v1/models/{modelId}` — Retrieve details and metadata for a specific quant model.
- `GET /api/v1/models/predictions/latest?symbol={symbol}` — Retrieve multi-model point-in-time prediction (Expected return, Direction probability, Volatility forecast, Regime context).

### Walk-Forward API (`/api/v1/walk-forward/*`):
- `GET /api/v1/walk-forward/runs` — List historical walk-forward execution runs.
- `GET /api/v1/walk-forward/runs/latest` — Get the most recent walk-forward run with fold summaries and champion models.
- `GET /api/v1/walk-forward/runs/{runId}/folds` — Inspect individual chronological folds with train/val/test date ranges, purging intervals, and out-of-sample test results.

---

## 5. PostgreSQL Schema (`V9__quant_prediction_and_walk_forward.sql`)

- **`dataset_snapshots`**: Immutable point-in-time dataset definitions, feature manifests, and SHA-256 data hashes.
- **`model_registry`**: Model versioning, hyperparameter records, training date ranges, feature importance weights, and active champion status.
- **`model_predictions`**: Point-in-time predictions ($E[R_{t+1}]$, $P(R>0)$, $\sigma_{5d}$, regime state, feature contributions) timestamped at inference date $T$.
- **`walk_forward_runs`**: Walk-forward runs recording window type (EXPANDING/ROLLING), fold count, purging/embargo parameters, and overall out-of-sample metrics.
- **`walk_forward_folds`**: Chronological fold breakdowns containing train/val/test boundaries, champion model IDs, candidate model scores, and out-of-sample test metrics.
