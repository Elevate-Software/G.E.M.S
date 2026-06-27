CREATE TABLE entry_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    gate_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    direction VARCHAR(50) NOT NULL,
    access_result VARCHAR(50) NOT NULL,
    denial_reason VARCHAR(255),
    scanned_by_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (gate_id) REFERENCES gates(id),
    FOREIGN KEY (scanned_by_id) REFERENCES users(id)
);

CREATE INDEX idx_entry_logs_user_timestamp ON entry_logs(user_id, timestamp);
CREATE INDEX idx_entry_logs_gate_timestamp ON entry_logs(gate_id, timestamp);
