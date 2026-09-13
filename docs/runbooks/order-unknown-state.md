# Runbook: Unknown Order Execution State

### Critical Safety Rule
**HTTP timeout != order rejection.** Never blindly retry order placement when an order request times out.

### Symptoms
- Order status marked as `UNKNOWN_EXECUTION_STATE`.
- Network timeout occurred during broker API dispatch.

### Immediate Actions
1. **DO NOT SUBMIT ANOTHER ORDER.**
2. Query broker order book directly using the `order_intent_id` / `idempotency_key`.
3. Check if broker assigned a `broker_order_id`.

### Resolution
- If order exists at broker: Update local record with broker status (`OPEN`, `FILLED`, `REJECTED`).
- If order does NOT exist at broker: Safely transition local status to `FAILED`.
