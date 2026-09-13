# Runbook: Emergency Stop / Global Kill Switch

### Triggering Emergency Stop
1. Click **EMERGENCY STOP** button in the top navigation or Live Trading page.
2. Confirm prompt: *"Activate Global Emergency Stop?"*
3. System immediately sets `is_active=true` on `trading_safety_locks` (ID: `EMERGENCY_STOP`).
4. All future live order preview and placement requests fail immediately with `IllegalStateException`.
5. Open/existing positions are **NOT** automatically liquidated (liquidation remains an explicit operator choice).

# Runbook: Live Trading Recovery

### Resuming Normal Trading After Emergency Stop
1. Verify market data health is `HEALTHY` in Part 19 Monitoring.
2. Confirm risk engine bounds and daily loss guards are clear.
3. Run portfolio reconciliation to confirm 0 position discrepancies.
4. Execute `POST /api/v1/broker/safety-lock/release` with authorized user credentials.
