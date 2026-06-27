CREATE TABLE gates (
    id BIGSERIAL PRIMARY KEY,
    gate_name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    is_operational BOOLEAN NOT NULL DEFAULT true,
    operating_hours_start TIME,
    operating_hours_end TIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
