-- V001: Create system roles and users tables

-- System roles table
CREATE TABLE sys_roles (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(50) NOT NULL UNIQUE,
    description         TEXT,
    is_system           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- System users table
CREATE TABLE sys_users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    avatar_url          VARCHAR(500),
    phone               VARCHAR(20),
    timezone            VARCHAR(50) DEFAULT 'Asia/Bishkek',
    language            VARCHAR(5) DEFAULT 'ru',
    is_email_verified   BOOLEAN DEFAULT FALSE,
    email_verified_at   TIMESTAMP,
    last_login_at       TIMESTAMP,
    is_active           BOOLEAN DEFAULT TRUE,
    is_deleted          BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          UUID,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID
);

-- User roles junction table
CREATE TABLE sys_user_roles (
    user_id             UUID NOT NULL REFERENCES sys_users(id) ON DELETE CASCADE,
    role_id             UUID NOT NULL REFERENCES sys_roles(id) ON DELETE CASCADE,
    assigned_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(user_id, role_id)
);

-- Refresh tokens table
CREATE TABLE sys_refresh_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token               VARCHAR(500) NOT NULL UNIQUE,
    user_id             UUID NOT NULL REFERENCES sys_users(id) ON DELETE CASCADE,
    expires_at          TIMESTAMP NOT NULL,
    revoked             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_users_email ON sys_users(email);
CREATE INDEX idx_users_deleted ON sys_users(is_deleted);
CREATE INDEX idx_refresh_tokens_user ON sys_refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON sys_refresh_tokens(token);

-- Insert default roles
INSERT INTO sys_roles (name, description, is_system) VALUES
    ('ADMIN', 'System administrator with full access', true),
    ('USER', 'Regular user', true),
    ('MODERATOR', 'Moderator with limited admin access', true);
