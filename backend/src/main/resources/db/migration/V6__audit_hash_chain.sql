ALTER TABLE t_operation_log ADD COLUMN previous_hash VARCHAR(128);
ALTER TABLE t_operation_log ADD COLUMN entry_hash VARCHAR(128) NOT NULL DEFAULT '';

CREATE INDEX idx_operation_log_time ON t_operation_log(operation_time);
CREATE INDEX idx_operation_log_hash ON t_operation_log(entry_hash);
