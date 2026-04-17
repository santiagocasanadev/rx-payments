CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(50) PRIMARY KEY,
    amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id VARCHAR(50) PRIMARY KEY,
    aggregate_id VARCHAR(50),
    event_type VARCHAR(50),
    payload TEXT,
    correlation_id VARCHAR(100),
    status VARCHAR(20),
    created_at TIMESTAMP
);
