-- V007: Create teams and invitations tables

-- Teams table
CREATE TABLE teams (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    owner_id            UUID NOT NULL REFERENCES sys_users(id),
    avatar_url          VARCHAR(500),
    is_public           BOOLEAN DEFAULT FALSE,
    is_deleted          BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);

-- Team members table
CREATE TABLE team_members (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id             UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES sys_users(id) ON DELETE CASCADE,
    role                VARCHAR(50) DEFAULT 'MEMBER',
    joined_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(team_id, user_id)
);

-- Invitations table
CREATE TABLE invitations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL,
    token               VARCHAR(100) NOT NULL UNIQUE,
    type                VARCHAR(20) NOT NULL,
    target_id           UUID NOT NULL,
    role_id             UUID,
    invited_by          UUID NOT NULL REFERENCES sys_users(id),
    expires_at          TIMESTAMP NOT NULL,
    accepted_at         TIMESTAMP,
    accepted_by         UUID REFERENCES sys_users(id),
    status              VARCHAR(20) DEFAULT 'PENDING',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_teams_owner ON teams(owner_id);
CREATE INDEX idx_team_members_team ON team_members(team_id);
CREATE INDEX idx_team_members_user ON team_members(user_id);
CREATE INDEX idx_invitations_token ON invitations(token);
CREATE INDEX idx_invitations_email ON invitations(email);
