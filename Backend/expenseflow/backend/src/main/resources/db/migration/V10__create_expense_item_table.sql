CREATE TABLE expense_item (
    id BIGSERIAL PRIMARY KEY,
    expense_claim_id BIGINT NOT NULL REFERENCES expense_claim(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES expense_category(id),
    amount NUMERIC(12,2) NOT NULL,
    expense_date DATE NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    merchant_name VARCHAR(255),
    description VARCHAR(500),
    project_or_client_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
