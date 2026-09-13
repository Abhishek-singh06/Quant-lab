# Runbook: Broker Disconnection

### Symptoms
- Broker account connection status shows `DISCONNECTED`, `TOKEN_EXPIRED`, or `AUTHENTICATION_REQUIRED`.
- Live order previews or placements fail with connection errors.

### Immediate Actions
1. Halt new live order preview requests.
2. Check if daily OAuth token has expired (Kite tokens expire at 06:00 AM IST).
3. Re-authenticate via official broker login flow to obtain a fresh session token.

### Recovery
- Trigger `/api/v1/broker/accounts/{id}/reconciliation` after re-establishing connection to verify position sync.
