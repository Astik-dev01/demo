-- V003: Create projects and related tables

-- Projects table
CREATE TABLE projects (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(200) NOT NULL,
    key                 VARCHAR(10) NOT NULL UNIQUE,
    description         TEXT,
    owner_id            UUID NOT NULL REFERENCES sys_users(id),
    type_id             UUID REFERENCES hb_project_type(id),
    color               VARCHAR(7) DEFAULT '#3B82F6',
    icon                VARCHAR(50),
    is_public           BOOLEAN DEFAULT FALSE,
    is_archived         BOOLEAN DEFAULT FALSE,
    archived_at         TIMESTAMP,
    settings            JSONB DEFAULT '{}',
    is_deleted          BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          UUID,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID
);

-- Project members table
CREATE TABLE project_members (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES sys_users(id) ON DELETE CASCADE,
    role_id             UUID NOT NULL REFERENCES hb_role_in_project(id),
    joined_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    invited_by          UUID REFERENCES sys_users(id),
    is_active           BOOLEAN DEFAULT TRUE,
    is_deleted          BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(project_id, user_id)
);

-- Indexes
CREATE INDEX idx_projects_owner ON projects(owner_id);
CREATE INDEX idx_projects_key ON projects(key);
CREATE INDEX idx_projects_deleted ON projects(is_deleted);
CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);
