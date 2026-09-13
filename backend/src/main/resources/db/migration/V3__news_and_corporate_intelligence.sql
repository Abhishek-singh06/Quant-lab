-- ==========================================================
-- QuantLab Migration V3: News and Corporate Intelligence Schema
-- ==========================================================

-- 1. News Sources Registry
CREATE TABLE IF NOT EXISTS market_data.news_sources (
    id BIGSERIAL PRIMARY KEY,
    source_name VARCHAR(64) NOT NULL UNIQUE,
    source_type VARCHAR(32) NOT NULL,
    credibility_level VARCHAR(32) NOT NULL DEFAULT 'GENERAL_NEWS',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    base_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 2. News Articles
CREATE TABLE IF NOT EXISTS market_data.news_articles (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL,
    source_name VARCHAR(64) NOT NULL,
    title VARCHAR(512) NOT NULL,
    description VARCHAR(2048),
    url VARCHAR(1024),
    canonical_url VARCHAR(1024),
    content_hash VARCHAR(64) NOT NULL UNIQUE,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    language VARCHAR(16) DEFAULT 'en',
    author VARCHAR(128),
    importance_score NUMERIC(5, 4) DEFAULT 0.50,
    sentiment_score NUMERIC(5, 4) DEFAULT 0.0,
    financial_impact_score NUMERIC(5, 4) DEFAULT 0.0,
    sentiment_label VARCHAR(32) DEFAULT 'NEUTRAL',
    status VARCHAR(32) NOT NULL DEFAULT 'PROCESSED',
    cluster_id VARCHAR(64),
    duplicate_of_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_news_info_avail ON market_data.news_articles(information_available_at);
CREATE INDEX IF NOT EXISTS idx_news_published_at ON market_data.news_articles(published_at);
CREATE INDEX IF NOT EXISTS idx_news_source_id ON market_data.news_articles(source_id);
CREATE INDEX IF NOT EXISTS idx_news_cluster_id ON market_data.news_articles(cluster_id);

-- 3. News Article Entity Links
CREATE TABLE IF NOT EXISTS market_data.news_article_entities (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES market_data.news_articles(id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    confidence NUMERIC(5, 4) NOT NULL DEFAULT 1.0,
    match_type VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_nae_article_id ON market_data.news_article_entities(article_id);
CREATE INDEX IF NOT EXISTS idx_nae_instrument_id ON market_data.news_article_entities(instrument_id);
CREATE INDEX IF NOT EXISTS idx_nae_symbol ON market_data.news_article_entities(symbol);

-- 4. Corporate Events Intelligence
CREATE TABLE IF NOT EXISTS market_data.corporate_events (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    event_subtype VARCHAR(64),
    title VARCHAR(512) NOT NULL,
    description VARCHAR(2048),
    event_date DATE,
    period VARCHAR(32),
    announced_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    effective_at TIMESTAMP WITH TIME ZONE,
    source VARCHAR(64) NOT NULL,
    source_url VARCHAR(1024),
    importance_score NUMERIC(5, 4) DEFAULT 0.50,
    financial_impact_score NUMERIC(5, 4) DEFAULT 0.0,
    sentiment_label VARCHAR(32) DEFAULT 'NEUTRAL',
    confidence NUMERIC(5, 4) DEFAULT 1.0,
    structured_payload TEXT,
    version INT NOT NULL DEFAULT 1,
    superseded_by_id BIGINT,
    is_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_corp_evt_inst ON market_data.corporate_events(instrument_id);
CREATE INDEX IF NOT EXISTS idx_corp_evt_symbol ON market_data.corporate_events(symbol);
CREATE INDEX IF NOT EXISTS idx_corp_evt_info_avail ON market_data.corporate_events(information_available_at);
CREATE INDEX IF NOT EXISTS idx_corp_evt_type ON market_data.corporate_events(event_type);
CREATE INDEX IF NOT EXISTS idx_corp_evt_date ON market_data.corporate_events(event_date);

-- 5. Corporate Event Entity Links
CREATE TABLE IF NOT EXISTS market_data.corporate_event_entities (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES market_data.corporate_events(id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    role VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cee_event_id ON market_data.corporate_event_entities(event_id);
CREATE INDEX IF NOT EXISTS idx_cee_instrument_id ON market_data.corporate_event_entities(instrument_id);

-- 6. News Ingestion Runs & Errors
CREATE TABLE IF NOT EXISTS market_data.news_ingestion_runs (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    provider VARCHAR(64) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL DEFAULT 'RUNNING',
    articles_received INT NOT NULL DEFAULT 0,
    articles_inserted INT NOT NULL DEFAULT 0,
    duplicates_count INT NOT NULL DEFAULT 0,
    events_extracted INT NOT NULL DEFAULT 0,
    entity_matches INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    duration_ms BIGINT,
    error_message VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS market_data.news_processing_errors (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64),
    provider VARCHAR(64) NOT NULL,
    article_url VARCHAR(1024),
    error_category VARCHAR(32) NOT NULL,
    reason VARCHAR(1024) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Seed initial sources
INSERT INTO market_data.news_sources (source_name, source_type, credibility_level, enabled, base_url)
VALUES
    ('NSE_FILINGS', 'EXCHANGE', 'EXCHANGE_DISCLOSURE', TRUE, 'https://api.nseindia.com'),
    ('ECONOMIC_TIMES', 'NEWS', 'LICENSED_NEWS', TRUE, 'https://economictimes.indiatimes.com'),
    ('MINT', 'NEWS', 'LICENSED_NEWS', TRUE, 'https://www.livemint.com'),
    ('BUSINESS_STANDARD', 'NEWS', 'LICENSED_NEWS', TRUE, 'https://www.business-standard.com'),
    ('MOCK_NEWS', 'MOCK', 'SOCIAL_MISC', TRUE, 'http://localhost')
ON CONFLICT (source_name) DO NOTHING;
