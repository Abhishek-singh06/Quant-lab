# QuantLab News & Corporate Intelligence Pipeline (Part 5)

## 1. Overview & Core Architecture

The QuantLab News and Corporate Intelligence Pipeline ingests legitimate financial news and official corporate disclosures (NSE filings, board meetings, quarterly results, shareholding patterns), resolves entities, classifies events, calculates financial-impact sentiment, and provides point-in-time access without look-ahead bias.

### Pipeline Flow

```
NEWS / CORPORATE SOURCE (NSE Filings, Licensed Feeds, RSS)
            ↓
  Source Adapter (`NSECorporateFilingsAdapter`, `NewsDataProvider`)
            ↓
  Event Deduplication & Hashing (SHA-256 Content Hash, Canonical URLs)
            ↓
  News Clustering (Syndicated grouping to prevent sentiment inflation)
            ↓
  Entity Resolution (`EntityResolutionService`: Tickers, Names, Aliases with Confidence)
            ↓
  Event Classification (25+ Event Taxonomy: Results, Splits, Contracts, SEBI Actions)
            ↓
  Event Fact Extraction (Dividends, Revenues, Contract Values)
            ↓
  Sentiment & Importance Analysis (Text Sentiment vs. Financial Impact Sentiment)
            ↓
  Point-in-Time Persistence (`information_available_at` timestamp)
            ↓
  PostgreSQL (`news_articles`, `corporate_events`, `news_article_entities`)
            ↓
  Point-in-Time Feature Generator & Corporate Intelligence APIs
```

---

## 2. Event Taxonomy

| Event Category | Key Triggers & Structured Data | Importance |
|----------------|--------------------------------|------------|
| `EARNINGS_RESULT` | Q1-Q4 quarterly revenue, net profit, margins, YoY growth | 0.90 |
| `DIVIDEND` | Interim/final dividend per share, ex-date, record date | 0.75 |
| `BUYBACK` | Share repurchase program, tender/open market price | 0.75 |
| `STOCK_SPLIT` / `BONUS` | Sub-division ratio, bonus share issue | 0.75 |
| `ACQUISITION` / `MERGER` | Target company, deal value, ownership stake | 0.90 |
| `NEW_CONTRACT` | Customer, contract value, duration, scope | 0.75 |
| `MANAGEMENT_CHANGE` / `CEO_CHANGE` / `CFO_CHANGE` | Executive resignation, appointment | 0.70 |
| `REGULATORY_ACTION` | SEBI penalty, regulatory probe, court dispute | 0.90 |
| `BOARD_MEETING` | Meeting agenda, dividend proposal | 0.60 |
| `CREDIT_RATING` | CRISIL, ICRA, CARE rating upgrade/downgrade | 0.60 |

---

## 3. Financial Impact Sentiment vs. Text Sentiment

A critical principle of the pipeline is that **generic NLP sentiment does not equal financial market impact**:

- *Example:* `"Company profit falls 15% YoY but beats street expectations by 8%"` contains negative language (`falls`), but has a **positive financial impact** because the market expected worse.
- The pipeline computes:
  1. `sentiment_score`: Standard textual polarity $[-1.0 \dots +1.0]$.
  2. `financial_impact_score`: Market impact adjusted for expectation dynamics and material outcome $[-1.0 \dots +1.0]$.

---

## 4. Look-Ahead Bias Prevention: Event Date vs. Publication Date

For any financial model evaluating date $T$:

$$\text{Information Allowed} \iff \text{information\_available\_at} \le T$$

### Critical Distinctions:
- **Economic Period End Date:** e.g. Q1 period ending **March 31**.
- **Publication / Announcement Timestamp:** e.g. Audited results released on **May 15 at 16:30 IST**.
- **Model Rule:** A model evaluating historical features on **April 10** CANNOT observe the May 15 results.

### Automated Look-Ahead Bias Verification:
All 5 mandatory tests in [`test_news_lookahead_bias.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/tests/test_news_lookahead_bias.py) pass:
1. Article published at 15:00 is NOT returned when queried at 14:59.
2. Article published at 15:00 IS returned when queried at 15:01.
3. Feature generated at 14:59 is 100% UNCHANGED when future news is inserted.
4. Corporate result for Q1 announced in May does not appear in features generated in April.
5. Future dividend announcement on June 1 does not affect historical features on May 20.

---

## 5. Exponential Signal Time Decay

To prevent stale news from distorting current market signals:

$$\text{Weight}(t) = \exp\left( -\ln(2) \cdot \frac{\text{Age in Hours}}{\text{Half-Life in Hours}} \right)$$

Default half-life: **24.0 hours** (configurable per event type).

---

## 6. REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/news/timeline/{symbol}?asOfTime=...` | Point-in-time company intelligence timeline |
| `GET` | `/api/v1/news/articles/{symbol}?asOfTime=...` | Historical news articles available by timestamp $T$ |
| `GET` | `/api/v1/news/events/{symbol}?asOfTime=...` | Historical corporate events available by timestamp $T$ |
| `POST` | `/api/v1/news/ingest` | Trigger news and corporate disclosures ingestion run |
