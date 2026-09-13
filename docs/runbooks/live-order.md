# Runbook: Live Order Lifecycle & Troubleshooting

### Symptoms
- Order preview fails or confirmation returns `REVIEW_REQUIRED` / `REVALIDATION_FAILED`.

### Immediate Actions
1. Check order error message on the confirmation modal.
2. Verify market hours (09:15 to 15:30 IST on trading days).
3. Check `ProviderHealth` in Part 19 Monitoring: ensure data age < 180s.
4. Verify account available cash vs order value.

### Safety Checks
- Verify `Emergency Stop` is not active in `/api/v1/broker/safety-lock`.
- Ensure price complies with NSE tick size (0.05).
