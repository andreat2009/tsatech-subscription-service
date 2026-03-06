CREATE TABLE customer_subscription (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    plan_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    next_billing_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_customer_subscription_customer ON customer_subscription(customer_id);
CREATE INDEX idx_customer_subscription_status ON customer_subscription(status);
