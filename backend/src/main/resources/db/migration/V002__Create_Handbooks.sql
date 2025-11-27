-- V002: Create handbook tables

-- Task Priority
CREATE TABLE hb_task_priority (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    color           VARCHAR(7) NOT NULL DEFAULT '#6B7280',
    icon            VARCHAR(50),
    level           INTEGER NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Task Status
CREATE TABLE hb_task_status (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    color           VARCHAR(7) NOT NULL DEFAULT '#6B7280',
    icon            VARCHAR(50),
    is_final        BOOLEAN NOT NULL DEFAULT FALSE,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Project Type
CREATE TABLE hb_project_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    icon            VARCHAR(50),
    description_ru  TEXT,
    description_ky  TEXT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Tag Category
CREATE TABLE hb_tag_category (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    color           VARCHAR(7) NOT NULL DEFAULT '#6B7280',
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Role in Project
CREATE TABLE hb_role_in_project (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    description_ru  TEXT,
    permissions     JSONB DEFAULT '[]',
    level           INTEGER NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Insert default task priorities
INSERT INTO hb_task_priority (alias, name_ru, name_ky, name_en, color, icon, level) VALUES
    ('CRITICAL', 'Критический', 'Критикалык', 'Critical', '#DC2626', 'alert-circle', 4),
    ('HIGH', 'Высокий', 'Жогорку', 'High', '#F97316', 'arrow-up', 3),
    ('MEDIUM', 'Средний', 'Орточо', 'Medium', '#EAB308', 'minus', 2),
    ('LOW', 'Низкий', 'Төмөн', 'Low', '#22C55E', 'arrow-down', 1),
    ('NONE', 'Без приоритета', 'Приоритетсиз', 'No Priority', '#6B7280', 'circle', 0);

-- Insert default task statuses
INSERT INTO hb_task_status (alias, name_ru, name_ky, name_en, color, icon, is_final, is_default) VALUES
    ('BACKLOG', 'Бэклог', 'Бэклог', 'Backlog', '#6B7280', 'inbox', false, true),
    ('TODO', 'К выполнению', 'Аткарууга', 'To Do', '#3B82F6', 'circle', false, false),
    ('IN_PROGRESS', 'В работе', 'Иште', 'In Progress', '#F59E0B', 'loader', false, false),
    ('IN_REVIEW', 'На проверке', 'Текшерүүдө', 'In Review', '#8B5CF6', 'eye', false, false),
    ('DONE', 'Выполнено', 'Аткарылды', 'Done', '#22C55E', 'check-circle', true, false),
    ('CANCELLED', 'Отменено', 'Жокко чыгарылды', 'Cancelled', '#EF4444', 'x-circle', true, false);

-- Insert default project types
INSERT INTO hb_project_type (alias, name_ru, name_ky, name_en, icon) VALUES
    ('SOFTWARE', 'Разработка ПО', 'ПО иштеп чыгуу', 'Software Development', 'code'),
    ('MARKETING', 'Маркетинг', 'Маркетинг', 'Marketing', 'megaphone'),
    ('DESIGN', 'Дизайн', 'Дизайн', 'Design', 'palette'),
    ('RESEARCH', 'Исследование', 'Изилдөө', 'Research', 'search'),
    ('EDUCATION', 'Образование', 'Билим берүү', 'Education', 'graduation-cap'),
    ('OTHER', 'Другое', 'Башка', 'Other', 'folder');

-- Insert default tag categories
INSERT INTO hb_tag_category (alias, name_ru, name_ky, name_en, color) VALUES
    ('FEATURE', 'Функционал', 'Функционал', 'Feature', '#3B82F6'),
    ('BUG', 'Баг', 'Баг', 'Bug', '#EF4444'),
    ('IMPROVEMENT', 'Улучшение', 'Жакшыртуу', 'Improvement', '#8B5CF6'),
    ('DOCUMENTATION', 'Документация', 'Документация', 'Documentation', '#F59E0B'),
    ('TESTING', 'Тестирование', 'Тестирлөө', 'Testing', '#10B981'),
    ('CUSTOM', 'Пользовательский', 'Колдонуучунун', 'Custom', '#6B7280');

-- Insert default project roles
INSERT INTO hb_role_in_project (alias, name_ru, name_ky, name_en, level, permissions) VALUES
    ('OWNER', 'Владелец', 'Ээси', 'Owner', 100, '["all"]'),
    ('ADMIN', 'Администратор', 'Администратор', 'Admin', 80, '["manage_members", "manage_boards", "manage_tasks", "delete_tasks", "manage_settings"]'),
    ('MEMBER', 'Участник', 'Катышуучу', 'Member', 50, '["create_tasks", "edit_own_tasks", "comment", "track_time"]'),
    ('VIEWER', 'Наблюдатель', 'Байкоочу', 'Viewer', 10, '["view_tasks", "comment"]');
