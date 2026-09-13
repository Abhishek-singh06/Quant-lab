# Part 20 — Broker Integration & Live Execution Architecture

## 1. Primary Principle: Manual User Confirmation Only
QuantLab operates under strict **Manual User Confirmation Mode**. There is **NO automatic autonomous live order execution**.

```
                 SIGNAL
                    ↓
                  RISK
                    ↓
             SAFETY / COMPLIANCE GATES
                    ↓
             USER CONFIRMATION (Explicit Checkbox & Place Order)
                    ↓
             AUTHORITATIVE REVALIDATION (Market Hours, Stale Feed, Risk Budget)
                    ↓
             ORDER VALIDATION (Tick Size, Precision, Bounds)
                    ↓
               BROKER API (Zerodha Kite / Angel One / Sandbox)
                    ↓
                 ORDER
                    ↓
               EXECUTION
                    ↓
             RECONCILIATION
                    ↓
               PORTFOLIO
                    ↓
              MONITORING
                    ↓
               AUDIT TRAIL
```

## 2. Broker Abstraction
QuantLab defines the `BrokerTradingProvider` interface, decoupling the core trading platform from specific broker protocols:
- `authenticate()`
- `getAccount()`
- `getPositions()`
- `getOrders()`
- `placeOrder()`
- `modifyOrder()`
- `cancelOrder()`
- `getTrades()`
- `logout()`

Supported providers:
1. `ZerodhaKiteTradingProvider`: Official Kite Connect API 3.0 protocol implementation with SHA-256 request checksum and API rate limiting (10 req/s).
2. `MockSandboxTradingProvider`: Safe local simulator for paper testing, CI builds, and unit tests without real capital or live credentials.

## 3. Safety Controls & Circuit Breakers
- **Global Kill Switch (`TradingSafetyLockEntity`)**: Instantly disables new live order submissions across all accounts when triggered.
- **Daily Loss Guard**: Rejects orders if projected cumulative day loss exceeds configured limits.
- **Stale Market Data Gate**: Blocks live orders if market data feeds are older than 180 seconds or report `CRITICAL`/`UNAVAILABLE` status.
- **Market Hours Enforcement**: Checks NSE trading calendar, national holidays, and trading session windows (09:15 to 15:30 IST).
- **Portfolio Reconciliation**: Compares broker position ledger with local database, raising alerts on discrepancies.

## 4. Production Defaults
- `LIVE_TRADING_ENABLED=false`
- `AUTOMATED_LIVE_TRADING_ENABLED=false`
- `REQUIRE_USER_CONFIRMATION=true`
- `PAPER_TRADING=true`
