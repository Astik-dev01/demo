-- V005: Create tasks and related tables

-- Tasks table
CREATE TABLE tasks (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    board_id            UUID NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    column_id           UUID NOT NULL REFERENCES board_columns(id),
    parent_task_id      UUID REFERENCES tasks(id),

    -- Identification
    number              INTEGER NOT NULL,
    title               VARCHAR(500) NOT NULL,
    description         TEXT,

    -- Assignees
    reporter_id         UUID NOT NULL REFERENCES sys_users(id),
    assignee_id         UUID REFERENCES sys_users(id),

    -- Classification
    priority_id         UUID REFERENCES hb_task_priority(id),
    status_id           UUID REFERENCES hb_task_status(id),

    -- Position
    position            INTEGER NOT NULL DEFAULT 0,

    -- Dates
    due_date            DATE,
    start_date          DATE,
    completed_at        TIMESTAMP,

    -- Time tracking
    estimated_hours     DECIMAL(10,2),
    spent_hours         DECIMAL(10,2) DEFAULT 0,

    -- Flags
    is_archived         BOOLEAN DEFAULT FALSE,
    archived_at         TIMESTAMP,

    -- Soft delete
    is_deleted          BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          UUID,

    -- Audit
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,

    UNIQUE(project_id, number)
);

-- Tags table
CREATE TABLE tags (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    category_id         UUID REFERENCES hb_tag_category(id),
    name                VARCHAR(50) NOT NULL,
    color               VARCHAR(7) DEFAULT '#6B7280',
    is_deleted          BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(project_id, name)
);

-- Task tags junction table
CREATE TABLE task_tags (
    task_id             UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    tag_id              UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY(task_id, tag_id)
);

-- Task comments table
CREATE TABLE task_comments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id             UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES sys_users(id),
    parent_comment_id   UUID REFERENCES task_comments(id),
    content             TEXT NOT NULL,
    is_edited           BOOLEAN DEFAULT FALSE,
    edited_at           TIMESTAMP,
    is_deleted          BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);

-- Task attachments table
CREATE TABLE task_attachments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id             UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    uploaded_by         UUID NOT NULL REFERENCES sys_users(id),
    file_name           VARCHAR(255) NOT NULL,
    file_path           VARCHAR(500) NOT NULL,
    file_size           BIGINT NOT NULL,
    mime_type           VARCHAR(100),
    is_deleted          BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_board ON tasks(board_id);
CREATE INDEX idx_tasks_column ON tasks(column_id);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_parent ON tasks(parent_task_id);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_deleted ON tasks(is_deleted);
CREATE INDEX idx_task_comments_task ON task_comments(task_id);
CREATE INDEX idx_task_comments_user ON task_comments(user_id);
CREATE INDEX idx_task_attachments_task ON task_attachments(task_id);
