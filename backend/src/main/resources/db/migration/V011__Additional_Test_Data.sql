-- V011: Additional comprehensive test data

-- Create more users (password: password123)
INSERT INTO sys_users (id, email, password_hash, first_name, last_name, phone, is_active, is_email_verified, created_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440004', 'alice@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Alice', 'Johnson', '+996555111222', true, true, NOW() - INTERVAL '10 days'),
    ('550e8400-e29b-41d4-a716-446655440005', 'bob@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Bob', 'Wilson', '+996555333444', true, true, NOW() - INTERVAL '8 days'),
    ('550e8400-e29b-41d4-a716-446655440006', 'carol@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Carol', 'Martinez', '+996555555666', true, true, NOW() - INTERVAL '5 days'),
    ('550e8400-e29b-41d4-a716-446655440007', 'david@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'David', 'Brown', '+996555777888', true, true, NOW() - INTERVAL '3 days');

-- Assign USER role to new users
INSERT INTO sys_user_roles (user_id, role_id)
SELECT '550e8400-e29b-41d4-a716-446655440004', id FROM sys_roles WHERE name = 'USER';
INSERT INTO sys_user_roles (user_id, role_id)
SELECT '550e8400-e29b-41d4-a716-446655440005', id FROM sys_roles WHERE name = 'USER';
INSERT INTO sys_user_roles (user_id, role_id)
SELECT '550e8400-e29b-41d4-a716-446655440006', id FROM sys_roles WHERE name = 'USER';
INSERT INTO sys_user_roles (user_id, role_id)
SELECT '550e8400-e29b-41d4-a716-446655440007', id FROM sys_roles WHERE name = 'USER';

-- Create teams
INSERT INTO teams (id, name, description, owner_id, is_public, is_deleted, created_at) VALUES
    ('aa0e8400-e29b-41d4-a716-446655440001', 'Development Team', 'Core development team for TaskFlow', '550e8400-e29b-41d4-a716-446655440001', true, false, NOW() - INTERVAL '7 days'),
    ('aa0e8400-e29b-41d4-a716-446655440002', 'Design Team', 'UI/UX design team', '550e8400-e29b-41d4-a716-446655440002', true, false, NOW() - INTERVAL '5 days'),
    ('aa0e8400-e29b-41d4-a716-446655440003', 'Marketing Team', 'Marketing and growth team', '550e8400-e29b-41d4-a716-446655440003', false, false, NOW() - INTERVAL '3 days');

-- Add team members
INSERT INTO team_members (team_id, user_id, role, joined_at) VALUES
    ('aa0e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'MEMBER', NOW() - INTERVAL '6 days'),
    ('aa0e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440004', 'MEMBER', NOW() - INTERVAL '5 days'),
    ('aa0e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440005', 'MEMBER', NOW() - INTERVAL '4 days'),
    ('aa0e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440003', 'MEMBER', NOW() - INTERVAL '4 days'),
    ('aa0e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440006', 'ADMIN', NOW() - INTERVAL '3 days');

-- Create additional projects
INSERT INTO projects (id, name, key, description, owner_id, color, is_public, is_archived, is_deleted, created_at) VALUES
    ('660e8400-e29b-41d4-a716-446655440002', 'Mobile App', 'MOBILE', 'TaskFlow mobile application for iOS and Android', '550e8400-e29b-41d4-a716-446655440002', '#10B981', false, false, false, NOW() - INTERVAL '14 days'),
    ('660e8400-e29b-41d4-a716-446655440003', 'Marketing Website', 'WEB', 'Public marketing website and landing pages', '550e8400-e29b-41d4-a716-446655440003', '#F59E0B', true, false, false, NOW() - INTERVAL '10 days'),
    ('660e8400-e29b-41d4-a716-446655440004', 'API Integration', 'API', 'Third-party API integrations and webhooks', '550e8400-e29b-41d4-a716-446655440001', '#8B5CF6', false, false, false, NOW() - INTERVAL '7 days');

-- Create boards for new projects
INSERT INTO boards (id, project_id, name, description, is_default, is_deleted, created_at) VALUES
    ('770e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440002', 'Mobile Sprint Board', 'Sprint planning for mobile', true, false, NOW() - INTERVAL '14 days'),
    ('770e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440003', 'Content Board', 'Content and pages tracking', true, false, NOW() - INTERVAL '10 days'),
    ('770e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440004', 'Integration Board', 'API integrations tracking', true, false, NOW() - INTERVAL '7 days');

-- Create columns for new boards
INSERT INTO board_columns (id, board_id, name, position, color, wip_limit, is_deleted, created_at) VALUES
    -- Mobile board columns
    ('880e8400-e29b-41d4-a716-446655440010', '770e8400-e29b-41d4-a716-446655440002', 'Backlog', 0, '#64748B', NULL, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440011', '770e8400-e29b-41d4-a716-446655440002', 'Sprint', 1, '#3B82F6', 8, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440012', '770e8400-e29b-41d4-a716-446655440002', 'Testing', 2, '#F59E0B', 3, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440013', '770e8400-e29b-41d4-a716-446655440002', 'Done', 3, '#22C55E', NULL, false, NOW()),
    -- Web board columns
    ('880e8400-e29b-41d4-a716-446655440020', '770e8400-e29b-41d4-a716-446655440003', 'Ideas', 0, '#64748B', NULL, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440021', '770e8400-e29b-41d4-a716-446655440003', 'Writing', 1, '#3B82F6', 5, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440022', '770e8400-e29b-41d4-a716-446655440003', 'Review', 2, '#8B5CF6', 3, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440023', '770e8400-e29b-41d4-a716-446655440003', 'Published', 3, '#22C55E', NULL, false, NOW()),
    -- API board columns
    ('880e8400-e29b-41d4-a716-446655440030', '770e8400-e29b-41d4-a716-446655440004', 'To Do', 0, '#64748B', NULL, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440031', '770e8400-e29b-41d4-a716-446655440004', 'Development', 1, '#3B82F6', 4, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440032', '770e8400-e29b-41d4-a716-446655440004', 'Testing', 2, '#F59E0B', 2, false, NOW()),
    ('880e8400-e29b-41d4-a716-446655440033', '770e8400-e29b-41d4-a716-446655440004', 'Deployed', 3, '#22C55E', NULL, false, NOW());

-- Create tags for projects
INSERT INTO tags (id, project_id, category_id, name, color, is_deleted, created_at) VALUES
    -- Demo project tags
    (gen_random_uuid(), '660e8400-e29b-41d4-a716-446655440001', (SELECT id FROM hb_tag_category WHERE alias = 'type'), 'Feature', '#3B82F6', false, NOW()),
    (gen_random_uuid(), '660e8400-e29b-41d4-a716-446655440001', (SELECT id FROM hb_tag_category WHERE alias = 'type'), 'Bug', '#EF4444', false, NOW()),
    (gen_random_uuid(), '660e8400-e29b-41d4-a716-446655440001', (SELECT id FROM hb_tag_category WHERE alias = 'type'), 'Enhancement', '#10B981', false, NOW()),
    (gen_random_uuid(), '660e8400-e29b-41d4-a716-446655440001', (SELECT id FROM hb_tag_category WHERE alias = 'component'), 'Frontend', '#8B5CF6', false, NOW()),
    (gen_random_uuid(), '660e8400-e29b-41d4-a716-446655440001', (SELECT id FROM hb_tag_category WHERE alias = 'component'), 'Backend', '#F59E0B', false, NOW()),
    -- Mobile project tags
    (gen_random_uuid(), '660e8400-e29b-41d4-a716-446655440002', (SELECT id FROM hb_tag_category WHERE alias = 'type'), 'iOS', '#64748B', false, NOW()),
    (gen_random_uuid(), '660e8400-e29b-41d4-a716-446655440002', (SELECT id FROM hb_tag_category WHERE alias = 'type'), 'Android', '#22C55E', false, NOW()),
    (gen_random_uuid(), '660e8400-e29b-41d4-a716-446655440002', (SELECT id FROM hb_tag_category WHERE alias = 'type'), 'UI', '#EC4899', false, NOW());

-- Create more tasks for Demo project
INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, is_archived, is_deleted, created_at)
SELECT
    '990e8400-e29b-41d4-a716-446655440004',
    '660e8400-e29b-41d4-a716-446655440001',
    '770e8400-e29b-41d4-a716-446655440001',
    '880e8400-e29b-41d4-a716-446655440001',
    4,
    'Add user profile page',
    'Create a page where users can view and edit their profile information',
    '550e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440003',
    (SELECT id FROM hb_task_priority WHERE alias = 'medium'),
    (SELECT id FROM hb_task_status WHERE alias = 'todo'),
    1,
    (CURRENT_DATE + INTERVAL '7 days')::DATE,
    8,
    false,
    false,
    NOW() - INTERVAL '2 days';

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, is_archived, is_deleted, created_at)
SELECT
    '990e8400-e29b-41d4-a716-446655440005',
    '660e8400-e29b-41d4-a716-446655440001',
    '770e8400-e29b-41d4-a716-446655440001',
    '880e8400-e29b-41d4-a716-446655440002',
    5,
    'Implement drag and drop for tasks',
    'Allow users to drag tasks between columns on the board view',
    '550e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440002',
    (SELECT id FROM hb_task_priority WHERE alias = 'high'),
    (SELECT id FROM hb_task_status WHERE alias = 'in_progress'),
    1,
    (CURRENT_DATE + INTERVAL '3 days')::DATE,
    16,
    false,
    false,
    NOW() - INTERVAL '1 day';

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, is_archived, is_deleted, created_at)
SELECT
    '990e8400-e29b-41d4-a716-446655440006',
    '660e8400-e29b-41d4-a716-446655440001',
    '770e8400-e29b-41d4-a716-446655440001',
    '880e8400-e29b-41d4-a716-446655440003',
    6,
    'Fix login page validation',
    'Email validation is not working correctly on the login form',
    '550e8400-e29b-41d4-a716-446655440003',
    '550e8400-e29b-41d4-a716-446655440004',
    (SELECT id FROM hb_task_priority WHERE alias = 'highest'),
    (SELECT id FROM hb_task_status WHERE alias = 'review'),
    0,
    (CURRENT_DATE + INTERVAL '1 day')::DATE,
    2,
    false,
    false,
    NOW() - INTERVAL '3 days';

-- Create tasks for Mobile project
INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, is_archived, is_deleted, created_at)
SELECT
    '990e8400-e29b-41d4-a716-446655440010',
    '660e8400-e29b-41d4-a716-446655440002',
    '770e8400-e29b-41d4-a716-446655440002',
    '880e8400-e29b-41d4-a716-446655440010',
    1,
    'Set up React Native project',
    'Initialize the React Native project with TypeScript and navigation',
    '550e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440005',
    (SELECT id FROM hb_task_priority WHERE alias = 'high'),
    (SELECT id FROM hb_task_status WHERE alias = 'todo'),
    0,
    (CURRENT_DATE + INTERVAL '5 days')::DATE,
    24,
    false,
    false,
    NOW() - INTERVAL '1 day';

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, is_archived, is_deleted, created_at)
SELECT
    '990e8400-e29b-41d4-a716-446655440011',
    '660e8400-e29b-41d4-a716-446655440002',
    '770e8400-e29b-41d4-a716-446655440002',
    '880e8400-e29b-41d4-a716-446655440011',
    2,
    'Design login screen',
    'Create the mobile login screen following the design specs',
    '550e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440006',
    (SELECT id FROM hb_task_priority WHERE alias = 'medium'),
    (SELECT id FROM hb_task_status WHERE alias = 'in_progress'),
    0,
    (CURRENT_DATE + INTERVAL '4 days')::DATE,
    8,
    false,
    false,
    NOW();

-- Create time entries
INSERT INTO time_entries (id, task_id, user_id, description, started_at, ended_at, duration_minutes, is_billable, is_running, is_deleted, created_at) VALUES
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'Working on authentication flow', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '4 hours', 240, true, false, false, NOW() - INTERVAL '2 days'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'Implementing JWT refresh', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '3 hours', 180, true, false, false, NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', 'Drag and drop library setup', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '3 hours', 120, true, false, false, NOW() - INTERVAL '5 hours'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'Database schema design', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '6 hours', 360, true, false, false, NOW() - INTERVAL '3 days'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440004', 'Bug fix and testing', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '2 hours', 120, true, false, false, NOW() - INTERVAL '1 day');

-- Create task comments
INSERT INTO task_comments (id, task_id, user_id, content, is_edited, is_deleted, created_at) VALUES
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'This is high priority for the next release.', false, false, NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'I can start working on this tomorrow.', false, false, NOW() - INTERVAL '12 hours'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'Please make sure to handle token refresh properly.', false, false, NOW() - INTERVAL '2 days'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'Done! Added automatic refresh when token expires.', false, false, NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440003', 'Which library are you planning to use for drag and drop?', false, false, NOW() - INTERVAL '6 hours'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', '@dnd-kit - it has great React support and accessibility.', false, false, NOW() - INTERVAL '5 hours'),
    (gen_random_uuid(), '990e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440004', 'Fixed the regex pattern for email validation.', false, false, NOW() - INTERVAL '4 hours');

-- Create notifications
INSERT INTO sys_notifications (id, user_id, type, title, message, data, is_read, created_at) VALUES
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', 'TASK_ASSIGNED', 'New task assigned', 'You have been assigned to "Implement drag and drop for tasks"', '{"taskId": "990e8400-e29b-41d4-a716-446655440005"}', false, NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', 'TASK_COMMENTED', 'New comment', 'Jane commented on "Implement drag and drop for tasks"', '{"taskId": "990e8400-e29b-41d4-a716-446655440005"}', false, NOW() - INTERVAL '6 hours'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', 'TASK_ASSIGNED', 'New task assigned', 'You have been assigned to "Add user profile page"', '{"taskId": "990e8400-e29b-41d4-a716-446655440004"}', true, NOW() - INTERVAL '2 days'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440004', 'TASK_ASSIGNED', 'New task assigned', 'You have been assigned to "Fix login page validation"', '{"taskId": "990e8400-e29b-41d4-a716-446655440006"}', false, NOW() - INTERVAL '3 days'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'TASK_COMPLETED', 'Task completed', '"Design database schema" has been marked as done', '{"taskId": "990e8400-e29b-41d4-a716-446655440003"}', true, NOW() - INTERVAL '2 days'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'DEADLINE_REMINDER', 'Deadline approaching', '"Fix login page validation" is due tomorrow', '{"taskId": "990e8400-e29b-41d4-a716-446655440006"}', false, NOW() - INTERVAL '1 hour'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005', 'PROJECT_INVITE', 'Project invitation', 'You have been invited to join "Mobile App" project', '{"projectId": "660e8400-e29b-41d4-a716-446655440002"}', false, NOW() - INTERVAL '12 hours'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440006', 'MENTION', 'You were mentioned', 'John mentioned you in a comment on "Design login screen"', '{"taskId": "990e8400-e29b-41d4-a716-446655440011"}', false, NOW() - INTERVAL '2 hours');

-- Update task counters
UPDATE tasks t SET spent_hours = (
    SELECT COALESCE(SUM(duration_minutes) / 60.0, 0)
    FROM time_entries te
    WHERE te.task_id = t.id AND te.is_deleted = false
);
