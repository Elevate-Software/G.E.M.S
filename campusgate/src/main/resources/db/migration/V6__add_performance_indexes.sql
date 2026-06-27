CREATE INDEX idx_entry_logs_timestamp ON entry_logs(timestamp);
CREATE INDEX idx_entry_logs_access_result ON entry_logs(access_result);
CREATE INDEX idx_credentials_status_expiry ON access_credentials(status, expiry_date);

-- Partial index for active credentials
CREATE INDEX idx_credentials_valid ON access_credentials(status) WHERE status = 'VALID';
