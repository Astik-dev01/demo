-- V008: Create notifications table

-- Notifications table
CREATE TABLE sys_notifications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES sys_users(id) ON DELETE CASCADE,
    type                VARCHAR(50) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    message             TEXT,
    data                JSONB DEFAULT '{}',
    is_read             BOOLEAN DEFAULT FALSE,
    read_at             TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_notifications_user ON sys_notifications(user_id);
CREATE INDEX idx_notifications_read ON sys_notifications(user_id, is_read);
CREATE INDEX idx_notifications_created ON sys_notifications(created_at DESC);
