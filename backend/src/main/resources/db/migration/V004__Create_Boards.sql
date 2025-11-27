-- V004: Create boards and columns tables

-- Boards table
CREATE TABLE boards (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    position            INTEGER NOT NULL DEFAULT 0,
    is_default          BOOLEAN DEFAULT FALSE,
    settings            JSONB DEFAULT '{}',
    is_deleted          BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID
);

-- Board columns table
CREATE TABLE board_columns (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    board_id            UUID NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    name                VARCHAR(100) NOT NULL,
    color               VARCHAR(7) DEFAULT '#E5E7EB',
    position            INTEGER NOT NULL DEFAULT 0,
    wip_limit           INTEGER,
    status_id           UUID REFERENCES hb_task_status(id),
    is_deleted          BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);

-- Indexes
CREATE INDEX idx_boards_project ON boards(project_id);
CREATE INDEX idx_board_columns_board ON board_columns(board_id);
