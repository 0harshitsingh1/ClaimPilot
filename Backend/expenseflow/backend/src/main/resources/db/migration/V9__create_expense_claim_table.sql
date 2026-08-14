CREATE SEQUENCE expense_claim_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE expense_claim (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(100) NOT NULL UNIQUE,
    employee_id BIGINT NOT NULL REFERENCES app_user(id),
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    submitted_at TIMESTAMP,
    approved_at TIMESTAMP,
    reimbursed_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
