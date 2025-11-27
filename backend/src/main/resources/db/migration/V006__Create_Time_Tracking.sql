-- V006: Create time tracking tables

-- Time entries table
CREATE TABLE time_entries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id             UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES sys_users(id),
    description         TEXT,
    started_at          TIMESTAMP NOT NULL,
    ended_at            TIMESTAMP,
    duration_minutes    INTEGER,
    is_billable         BOOLEAN DEFAULT TRUE,
    is_running          BOOLEAN DEFAULT FALSE,
    is_deleted          BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);

-- Indexes
CREATE INDEX idx_time_entries_task ON time_entries(task_id);
CREATE INDEX idx_time_entries_user ON time_entries(user_id);
CREATE INDEX idx_time_entries_started ON time_entries(started_at);
