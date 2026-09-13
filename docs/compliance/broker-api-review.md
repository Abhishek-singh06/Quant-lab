# Broker API & Regulatory Compliance Review

**Notice:** *This document provides technical design and compliance architecture documentation. It does NOT constitute formal legal advice.*

## 1. Regulatory Context
- **Regulatory Framework:** SEBI Circulars on Algorithmic Trading and Retail API Usage (2024/2025 updates)
- **Review Status:** `APPROVED_FOR_MANUAL_LIVE_ORDERS`
- **Classification:** **Manual User-Initiated Orders**. Orders are prepared algorithmically by the QuantLab intelligence pipeline, but **each order requires explicit manual human confirmation** before submission to the broker.

## 2. Technical Compliance Controls
1. **No Autonomous Execution:** Fully autonomous order routing is hardcoded to `DISABLED`.
2. **Audit Trail Immutability:** Every order preserves signal ID, signal confidence, model version, risk budget, execution timestamp, and confirmed user identity.
3. **Traceability:** Normalized order intents generate unique `order_intent_id` and `idempotency_key` headers.
4. **Rate Limiting:** Adheres to broker API throttle thresholds (max 10 order requests/second).
5. **Session Expiry & Token Security:** OAuth access tokens expire every 24 hours per Indian exchange standards.
