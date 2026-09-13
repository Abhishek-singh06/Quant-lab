# Runbook: Portfolio Reconciliation Mismatch

### Symptoms
- Monitoring alert `BROKER_RECONCILIATION_MISMATCH` triggered.
- Quantity or average price discrepancy between local position ledger and broker portfolio.

### Immediate Actions
1. Navigate to `/live-trading` -> Reconciliation Panel.
2. Inspect discrepancy details (e.g. out-of-band trade executed on broker mobile app).
3. Synchronize local position ledger with authoritative broker state.
