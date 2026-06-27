CREATE TABLE incident_reports (
    id BIGSERIAL PRIMARY KEY,
    reported_by_id BIGINT NOT NULL,
    offender_id BIGINT,
    gate_id BIGINT,
    description TEXT NOT NULL,
    incident_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    resolved_by_id BIGINT,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reported_by_id) REFERENCES users(id),
    FOREIGN KEY (offender_id) REFERENCES users(id),
    FOREIGN KEY (gate_id) REFERENCES gates(id),
    FOREIGN KEY (resolved_by_id) REFERENCES users(id)
);
