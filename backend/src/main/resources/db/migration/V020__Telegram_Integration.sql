-- Telegram Integration
-- Add Telegram fields to sys_users and create notification settings table

-- Add Telegram fields to sys_users
ALTER TABLE sys_users ADD COLUMN IF NOT EXISTS employee_code VARCHAR(8) UNIQUE;
ALTER TABLE sys_users ADD COLUMN IF NOT EXISTS telegram_chat_id BIGINT;
ALTER TABLE sys_users ADD COLUMN IF NOT EXISTS telegram_username VARCHAR(50);

-- Create indexes for Telegram fields
CREATE INDEX IF NOT EXISTS idx_sys_users_employee_code ON sys_users(employee_code);
CREATE INDEX IF NOT EXISTS idx_sys_users_telegram_chat_id ON sys_users(telegram_chat_id);

-- Create notification settings table
CREATE TABLE IF NOT EXISTS user_notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES sys_users(id) ON DELETE CASCADE,

    -- Notification channels
    telegram_enabled BOOLEAN DEFAULT FALSE,
    email_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,

    -- Notification types
    notify_task_assigned BOOLEAN DEFAULT TRUE,
    notify_task_commented BOOLEAN DEFAULT TRUE,
    notify_task_completed BOOLEAN DEFAULT TRUE,
    notify_mentioned BOOLEAN DEFAULT TRUE,
    notify_deadline_reminder BOOLEAN DEFAULT TRUE,
    notify_project_invite BOOLEAN DEFAULT TRUE,

    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_notification_settings_user_id ON user_notification_settings(user_id);

-- Generate employee codes for existing users
DO $$
DECLARE
    user_record RECORD;
    new_code VARCHAR(8);
    chars VARCHAR(36) := 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
BEGIN
    FOR user_record IN SELECT id FROM sys_users WHERE employee_code IS NULL LOOP
        LOOP
            new_code := 'TF' ||
                substring(chars from (floor(random() * 36) + 1)::int for 1) ||
                substring(chars from (floor(random() * 36) + 1)::int for 1) ||
                substring(chars from (floor(random() * 36) + 1)::int for 1) ||
                substring(chars from (floor(random() * 36) + 1)::int for 1) ||
                substring(chars from (floor(random() * 36) + 1)::int for 1) ||
                substring(chars from (floor(random() * 36) + 1)::int for 1);

            -- Check if code already exists
            IF NOT EXISTS (SELECT 1 FROM sys_users WHERE employee_code = new_code) THEN
                UPDATE sys_users SET employee_code = new_code WHERE id = user_record.id;
                EXIT;
            END IF;
        END LOOP;
    END LOOP;
END $$;
