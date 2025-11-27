-- Seed test data for development

-- Insert test users (password: password123 - BCrypt encoded)
INSERT INTO sys_users (id, email, password_hash, first_name, last_name, is_active, is_email_verified, created_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'admin@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Admin', 'User', true, true, NOW()),
    ('550e8400-e29b-41d4-a716-446655440002', 'john@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'John', 'Doe', true, true, NOW()),
    ('550e8400-e29b-41d4-a716-446655440003', 'jane@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Jane', 'Smith', true, true, NOW());

-- Link admin user to ADMIN role
INSERT INTO sys_user_roles (user_id, role_id)
SELECT '550e8400-e29b-41d4-a716-446655440001', id FROM sys_roles WHERE name = 'ADMIN';

-- Link regular users to USER role
INSERT INTO sys_user_roles (user_id, role_id)
SELECT '550e8400-e29b-41d4-a716-446655440002', id FROM sys_roles WHERE name = 'USER';

INSERT INTO sys_user_roles (user_id, role_id)
SELECT '550e8400-e29b-41d4-a716-446655440003', id FROM sys_roles WHERE name = 'USER';

-- Insert handbook: Task Priorities
INSERT INTO hb_task_priority (id, alias, name_ru, name_ky, name_en, color, level, icon, is_deleted, created_at) VALUES
    (gen_random_uuid(), 'lowest', 'Самый низкий', 'Эң төмөн', 'Lowest', '#64748B', 1, 'chevrons-down', false, NOW()),
    (gen_random_uuid(), 'low', 'Низкий', 'Төмөн', 'Low', '#22C55E', 2, 'chevron-down', false, NOW()),
    (gen_random_uuid(), 'medium', 'Средний', 'Орточо', 'Medium', '#F59E0B', 3, 'minus', false, NOW()),
    (gen_random_uuid(), 'high', 'Высокий', 'Жогору', 'High', '#F97316', 4, 'chevron-up', false, NOW()),
    (gen_random_uuid(), 'highest', 'Самый высокий', 'Эң жогору', 'Highest', '#EF4444', 5, 'chevrons-up', false, NOW());

-- Insert handbook: Task Statuses
INSERT INTO hb_task_status (id, alias, name_ru, name_ky, name_en, color, icon, is_final, is_default, is_deleted, created_at) VALUES
    (gen_random_uuid(), 'todo', 'К выполнению', 'Аткарылуучу', 'To Do', '#64748B', 'circle', false, true, false, NOW()),
    (gen_random_uuid(), 'in_progress', 'В работе', 'Иштелүүдө', 'In Progress', '#3B82F6', 'loader', false, false, false, NOW()),
    (gen_random_uuid(), 'review', 'На проверке', 'Текшерүүдө', 'In Review', '#8B5CF6', 'eye', false, false, false, NOW()),
    (gen_random_uuid(), 'done', 'Выполнено', 'Аткарылды', 'Done', '#22C55E', 'check-circle', true, false, false, NOW());

-- Insert handbook: Project Types
INSERT INTO hb_project_type (id, alias, name_ru, name_ky, name_en, icon, is_deleted, created_at) VALUES
    (gen_random_uuid(), 'software', 'Разработка ПО', 'Программа иштеп чыгуу', 'Software Development', 'code', false, NOW()),
    (gen_random_uuid(), 'marketing', 'Маркетинг', 'Маркетинг', 'Marketing', 'megaphone', false, NOW()),
    (gen_random_uuid(), 'design', 'Дизайн', 'Дизайн', 'Design', 'palette', false, NOW()),
    (gen_random_uuid(), 'business', 'Бизнес', 'Бизнес', 'Business', 'briefcase', false, NOW());

-- Insert handbook: Roles in Project
INSERT INTO hb_role_in_project (id, alias, name_ru, name_ky, name_en, level, permissions, is_deleted, created_at) VALUES
    (gen_random_uuid(), 'owner', 'Владелец', 'Ээси', 'Owner', 100, '["*"]', false, NOW()),
    (gen_random_uuid(), 'admin', 'Администратор', 'Админ', 'Admin', 80, '["manage_members", "manage_boards", "manage_tasks", "delete_tasks"]', false, NOW()),
    (gen_random_uuid(), 'member', 'Участник', 'Катышуучу', 'Member', 50, '["create_tasks", "edit_own_tasks", "comment"]', false, NOW()),
    (gen_random_uuid(), 'viewer', 'Наблюдатель', 'Байкоочу', 'Viewer', 10, '["view"]', false, NOW());

-- Insert handbook: Tag Categories
INSERT INTO hb_tag_category (id, alias, name_ru, name_ky, name_en, is_deleted, created_at) VALUES
    (gen_random_uuid(), 'type', 'Тип', 'Түрү', 'Type', false, NOW()),
    (gen_random_uuid(), 'component', 'Компонент', 'Компонент', 'Component', false, NOW()),
    (gen_random_uuid(), 'label', 'Метка', 'Белги', 'Label', false, NOW());

-- Create a demo project for admin user
INSERT INTO projects (id, name, key, description, owner_id, color, is_public, is_archived, is_deleted, created_at) VALUES
    ('660e8400-e29b-41d4-a716-446655440001', 'Demo Project', 'DEMO', 'A demo project for testing TaskFlow features', '550e8400-e29b-41d4-a716-446655440001', '#3B82F6', false, false, false, NOW());

-- Create a board for the demo project
INSERT INTO boards (id, project_id, name, description, is_default, is_deleted, created_at) VALUES
    ('770e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', 'Main Board', 'Main development board', true, false, NOW());

-- Create columns for the board
INSERT INTO board_columns (id, board_id, name, position, color, wip_limit, is_deleted, created_at) VALUES
    ('880e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', 'To Do', 0, '#64748B', NULL, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440001', 'In Progress', 1, '#3B82F6', 5, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440001', 'Review', 2, '#8B5CF6', 3, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440001', 'Done', 3, '#22C55E', NULL, false, NOW());

-- Create sample tasks
INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, is_archived, is_deleted, created_at)
SELECT
    '990e8400-e29b-41d4-a716-446655440001',
    '660e8400-e29b-41d4-a716-446655440001',
    '770e8400-e29b-41d4-a716-446655440001',
    '880e8400-e29b-41d4-a716-446655440001',
    1,
    'Setup project infrastructure',
    'Configure Docker, database, and development environment',
    '550e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440002',
    (SELECT id FROM hb_task_priority WHERE alias = 'high'),
    (SELECT id FROM hb_task_status WHERE alias = 'todo'),
    0,
    false,
    false,
    NOW();

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, is_archived, is_deleted, created_at)
SELECT
    '990e8400-e29b-41d4-a716-446655440002',
    '660e8400-e29b-41d4-a716-446655440001',
    '770e8400-e29b-41d4-a716-446655440001',
    '880e8400-e29b-41d4-a716-446655440002',
    2,
    'Implement user authentication',
    'Create login, registration, and JWT authentication',
    '550e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440002',
    (SELECT id FROM hb_task_priority WHERE alias = 'highest'),
    (SELECT id FROM hb_task_status WHERE alias = 'in_progress'),
    0,
    false,
    false,
    NOW();

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, is_archived, is_deleted, created_at)
SELECT
    '990e8400-e29b-41d4-a716-446655440003',
    '660e8400-e29b-41d4-a716-446655440001',
    '770e8400-e29b-41d4-a716-446655440001',
    '880e8400-e29b-41d4-a716-446655440004',
    3,
    'Design database schema',
    'Create ERD and implement migrations',
    '550e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440003',
    (SELECT id FROM hb_task_priority WHERE alias = 'medium'),
    (SELECT id FROM hb_task_status WHERE alias = 'done'),
    0,
    false,
    false,
    NOW() - INTERVAL '2 days';

-- Update the completed task
UPDATE tasks SET completed_at = NOW() - INTERVAL '1 day' WHERE id = '990e8400-e29b-41d4-a716-446655440003';
