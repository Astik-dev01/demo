-- V023: Seed comprehensive demo data for a realistic project management experience

-- =====================================================
-- USERS (password for all: password123)
-- =====================================================
INSERT INTO sys_users (id, email, password_hash, first_name, last_name, phone, is_active, is_email_verified, created_at) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'alex.petrov@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Александр', 'Петров', '+996555111001', true, true, NOW() - INTERVAL '90 days'),
    ('a1000000-0000-0000-0000-000000000002', 'maria.kim@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Мария', 'Ким', '+996555111002', true, true, NOW() - INTERVAL '85 days'),
    ('a1000000-0000-0000-0000-000000000003', 'dmitry.ivanov@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Дмитрий', 'Иванов', '+996555111003', true, true, NOW() - INTERVAL '80 days'),
    ('a1000000-0000-0000-0000-000000000004', 'elena.smirnova@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Елена', 'Смирнова', '+996555111004', true, true, NOW() - INTERVAL '75 days'),
    ('a1000000-0000-0000-0000-000000000005', 'artem.kozlov@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Артём', 'Козлов', '+996555111005', true, true, NOW() - INTERVAL '70 days'),
    ('a1000000-0000-0000-0000-000000000006', 'anna.volkova@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Анна', 'Волкова', '+996555111006', true, true, NOW() - INTERVAL '65 days'),
    ('a1000000-0000-0000-0000-000000000007', 'sergey.morozov@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Сергей', 'Морозов', '+996555111007', true, true, NOW() - INTERVAL '60 days'),
    ('a1000000-0000-0000-0000-000000000008', 'olga.fedorova@taskflow.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q', 'Ольга', 'Фёдорова', '+996555111008', true, true, NOW() - INTERVAL '55 days')
ON CONFLICT (email) DO NOTHING;

-- Assign roles to new users
INSERT INTO sys_user_roles (user_id, role_id)
SELECT u.id, r.id FROM sys_users u, sys_roles r
WHERE u.email IN ('alex.petrov@taskflow.kg', 'maria.kim@taskflow.kg') AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO sys_user_roles (user_id, role_id)
SELECT u.id, r.id FROM sys_users u, sys_roles r
WHERE u.email IN ('dmitry.ivanov@taskflow.kg', 'elena.smirnova@taskflow.kg', 'artem.kozlov@taskflow.kg', 'anna.volkova@taskflow.kg', 'sergey.morozov@taskflow.kg', 'olga.fedorova@taskflow.kg') AND r.name = 'USER'
ON CONFLICT DO NOTHING;

-- =====================================================
-- TEAMS
-- =====================================================
INSERT INTO teams (id, name, description, owner_id, is_public, created_at) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'Backend Developers', 'Команда backend разработчиков', 'a1000000-0000-0000-0000-000000000001', true, NOW() - INTERVAL '60 days'),
    ('b1000000-0000-0000-0000-000000000002', 'Frontend Developers', 'Команда frontend разработчиков', 'a1000000-0000-0000-0000-000000000002', true, NOW() - INTERVAL '58 days'),
    ('b1000000-0000-0000-0000-000000000003', 'QA Team', 'Команда тестирования', 'a1000000-0000-0000-0000-000000000004', true, NOW() - INTERVAL '55 days'),
    ('b1000000-0000-0000-0000-000000000004', 'DevOps', 'Инфраструктура и деплой', 'a1000000-0000-0000-0000-000000000005', false, NOW() - INTERVAL '50 days')
ON CONFLICT DO NOTHING;

-- Team members
INSERT INTO team_members (team_id, user_id, role, joined_at) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'OWNER', NOW() - INTERVAL '60 days'),
    ('b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000003', 'MEMBER', NOW() - INTERVAL '55 days'),
    ('b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000007', 'MEMBER', NOW() - INTERVAL '50 days'),
    ('b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000002', 'OWNER', NOW() - INTERVAL '58 days'),
    ('b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000006', 'MEMBER', NOW() - INTERVAL '52 days'),
    ('b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000004', 'OWNER', NOW() - INTERVAL '55 days'),
    ('b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000008', 'MEMBER', NOW() - INTERVAL '48 days'),
    ('b1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000005', 'OWNER', NOW() - INTERVAL '50 days')
ON CONFLICT DO NOTHING;

-- =====================================================
-- PROJECTS
-- =====================================================
INSERT INTO projects (id, name, key, description, owner_id, color, is_public, created_at) VALUES
    ('c1000000-0000-0000-0000-000000000001', 'E-Commerce Platform', 'ECOM', 'Разработка платформы электронной коммерции с полным функционалом: каталог товаров, корзина, оплата, доставка', 'a1000000-0000-0000-0000-000000000001', '#3B82F6', false, NOW() - INTERVAL '60 days'),
    ('c1000000-0000-0000-0000-000000000002', 'Mobile Banking App', 'BANK', 'Мобильное приложение для банкинга: переводы, платежи, история операций, push-уведомления', 'a1000000-0000-0000-0000-000000000002', '#10B981', false, NOW() - INTERVAL '55 days'),
    ('c1000000-0000-0000-0000-000000000003', 'HR Management System', 'HRMS', 'Система управления персоналом: учёт сотрудников, отпуска, KPI, зарплаты', 'a1000000-0000-0000-0000-000000000001', '#8B5CF6', false, NOW() - INTERVAL '45 days'),
    ('c1000000-0000-0000-0000-000000000004', 'CRM System', 'CRM', 'CRM система для отдела продаж: лиды, сделки, воронка продаж, аналитика', 'a1000000-0000-0000-0000-000000000003', '#F59E0B', false, NOW() - INTERVAL '40 days'),
    ('c1000000-0000-0000-0000-000000000005', 'DevOps Infrastructure', 'DEVOPS', 'Настройка CI/CD, мониторинг, логирование, автоматизация деплоя', 'a1000000-0000-0000-0000-000000000005', '#EF4444', false, NOW() - INTERVAL '35 days')
ON CONFLICT DO NOTHING;

-- =====================================================
-- BOARDS
-- =====================================================
INSERT INTO boards (id, project_id, name, description, is_default, created_at) VALUES
    ('d1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'Sprint Board', 'Основная доска спринта', true, NOW() - INTERVAL '60 days'),
    ('d1000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'Backlog', 'Бэклог продукта', false, NOW() - INTERVAL '58 days'),
    ('d1000000-0000-0000-0000-000000000003', 'c1000000-0000-0000-0000-000000000002', 'Development', 'Разработка', true, NOW() - INTERVAL '55 days'),
    ('d1000000-0000-0000-0000-000000000004', 'c1000000-0000-0000-0000-000000000003', 'Main Board', 'Основная доска', true, NOW() - INTERVAL '45 days'),
    ('d1000000-0000-0000-0000-000000000005', 'c1000000-0000-0000-0000-000000000004', 'Sales Pipeline', 'Воронка продаж', true, NOW() - INTERVAL '40 days'),
    ('d1000000-0000-0000-0000-000000000006', 'c1000000-0000-0000-0000-000000000005', 'Infrastructure', 'Инфраструктурные задачи', true, NOW() - INTERVAL '35 days')
ON CONFLICT DO NOTHING;

-- =====================================================
-- BOARD COLUMNS
-- =====================================================
INSERT INTO board_columns (id, board_id, name, position, color, wip_limit, created_at) VALUES
    -- E-Commerce Sprint Board
    ('e1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001', 'To Do', 0, '#64748B', NULL, NOW() - INTERVAL '60 days'),
    ('e1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001', 'In Progress', 1, '#3B82F6', 5, NOW() - INTERVAL '60 days'),
    ('e1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000001', 'Code Review', 2, '#8B5CF6', 3, NOW() - INTERVAL '60 days'),
    ('e1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000001', 'Testing', 3, '#F59E0B', 4, NOW() - INTERVAL '60 days'),
    ('e1000000-0000-0000-0000-000000000005', 'd1000000-0000-0000-0000-000000000001', 'Done', 4, '#22C55E', NULL, NOW() - INTERVAL '60 days'),
    -- Mobile Banking Board
    ('e1000000-0000-0000-0000-000000000006', 'd1000000-0000-0000-0000-000000000003', 'Backlog', 0, '#64748B', NULL, NOW() - INTERVAL '55 days'),
    ('e1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000003', 'In Development', 1, '#3B82F6', 4, NOW() - INTERVAL '55 days'),
    ('e1000000-0000-0000-0000-000000000008', 'd1000000-0000-0000-0000-000000000003', 'QA', 2, '#F59E0B', 3, NOW() - INTERVAL '55 days'),
    ('e1000000-0000-0000-0000-000000000009', 'd1000000-0000-0000-0000-000000000003', 'Released', 3, '#22C55E', NULL, NOW() - INTERVAL '55 days'),
    -- HR System Board
    ('e1000000-0000-0000-0000-000000000010', 'd1000000-0000-0000-0000-000000000004', 'To Do', 0, '#64748B', NULL, NOW() - INTERVAL '45 days'),
    ('e1000000-0000-0000-0000-000000000011', 'd1000000-0000-0000-0000-000000000004', 'In Progress', 1, '#3B82F6', 6, NOW() - INTERVAL '45 days'),
    ('e1000000-0000-0000-0000-000000000012', 'd1000000-0000-0000-0000-000000000004', 'Done', 2, '#22C55E', NULL, NOW() - INTERVAL '45 days'),
    -- CRM Board
    ('e1000000-0000-0000-0000-000000000013', 'd1000000-0000-0000-0000-000000000005', 'New', 0, '#64748B', NULL, NOW() - INTERVAL '40 days'),
    ('e1000000-0000-0000-0000-000000000014', 'd1000000-0000-0000-0000-000000000005', 'Working', 1, '#3B82F6', 5, NOW() - INTERVAL '40 days'),
    ('e1000000-0000-0000-0000-000000000015', 'd1000000-0000-0000-0000-000000000005', 'Completed', 2, '#22C55E', NULL, NOW() - INTERVAL '40 days'),
    -- DevOps Board
    ('e1000000-0000-0000-0000-000000000016', 'd1000000-0000-0000-0000-000000000006', 'Planned', 0, '#64748B', NULL, NOW() - INTERVAL '35 days'),
    ('e1000000-0000-0000-0000-000000000017', 'd1000000-0000-0000-0000-000000000006', 'In Progress', 1, '#3B82F6', 3, NOW() - INTERVAL '35 days'),
    ('e1000000-0000-0000-0000-000000000018', 'd1000000-0000-0000-0000-000000000006', 'Deployed', 2, '#22C55E', NULL, NOW() - INTERVAL '35 days')
ON CONFLICT DO NOTHING;

-- =====================================================
-- TAGS
-- =====================================================
INSERT INTO tags (id, project_id, name, color, created_at) VALUES
    ('f1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'bug', '#EF4444', NOW() - INTERVAL '55 days'),
    ('f1000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'feature', '#22C55E', NOW() - INTERVAL '55 days'),
    ('f1000000-0000-0000-0000-000000000003', 'c1000000-0000-0000-0000-000000000001', 'enhancement', '#3B82F6', NOW() - INTERVAL '55 days'),
    ('f1000000-0000-0000-0000-000000000004', 'c1000000-0000-0000-0000-000000000001', 'urgent', '#F97316', NOW() - INTERVAL '55 days'),
    ('f1000000-0000-0000-0000-000000000005', 'c1000000-0000-0000-0000-000000000001', 'documentation', '#6366F1', NOW() - INTERVAL '55 days'),
    ('f1000000-0000-0000-0000-000000000006', 'c1000000-0000-0000-0000-000000000002', 'security', '#DC2626', NOW() - INTERVAL '50 days'),
    ('f1000000-0000-0000-0000-000000000007', 'c1000000-0000-0000-0000-000000000002', 'performance', '#F59E0B', NOW() - INTERVAL '50 days'),
    ('f1000000-0000-0000-0000-000000000008', 'c1000000-0000-0000-0000-000000000002', 'ui/ux', '#8B5CF6', NOW() - INTERVAL '50 days')
ON CONFLICT DO NOTHING;

-- =====================================================
-- TASKS - E-Commerce Platform (25 tasks)
-- =====================================================
INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at, completed_at)
SELECT
    '01000001-0000-0000-0000-000000000001'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000005',
    1,
    'Настройка проекта и CI/CD',
    'Создать структуру проекта, настроить Docker, GitHub Actions, деплой на staging',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000005',
    (SELECT id FROM hb_task_priority WHERE alias = 'highest' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'done' LIMIT 1),
    0,
    NOW() - INTERVAL '50 days',
    16, 18,
    NOW() - INTERVAL '58 days',
    NOW() - INTERVAL '52 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at, completed_at)
SELECT
    '01000001-0000-0000-0000-000000000002'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000005',
    2,
    'Дизайн базы данных',
    'Спроектировать схему БД: пользователи, товары, заказы, платежи, доставка. Создать ERD диаграмму.',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000003',
    (SELECT id FROM hb_task_priority WHERE alias = 'highest' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'done' LIMIT 1),
    1,
    NOW() - INTERVAL '48 days',
    24, 28,
    NOW() - INTERVAL '56 days',
    NOW() - INTERVAL '49 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at, completed_at)
SELECT
    '01000001-0000-0000-0000-000000000003'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000005',
    3,
    'REST API аутентификации',
    'Реализовать JWT аутентификацию, регистрацию, логин, восстановление пароля, OAuth2',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000003',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'done' LIMIT 1),
    2,
    NOW() - INTERVAL '40 days',
    32, 35,
    NOW() - INTERVAL '50 days',
    NOW() - INTERVAL '42 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000004'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000004',
    4,
    'Каталог товаров API',
    'CRUD операции для товаров, категорий, фильтрация, поиск, пагинация',
    'a1000000-0000-0000-0000-000000000002',
    'a1000000-0000-0000-0000-000000000007',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'review' LIMIT 1),
    0,
    NOW() + INTERVAL '3 days',
    40, 32,
    NOW() - INTERVAL '20 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000005'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000003',
    5,
    'Корзина покупок',
    'Добавление/удаление товаров, изменение количества, сохранение для авторизованных пользователей',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000003',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'review' LIMIT 1),
    0,
    NOW() + INTERVAL '5 days',
    24, 20,
    NOW() - INTERVAL '15 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000006'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000002',
    6,
    'Интеграция платёжной системы',
    'Подключить Stripe/PayPal, обработка платежей, вебхуки, возвраты',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000003',
    (SELECT id FROM hb_task_priority WHERE alias = 'highest' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'in_progress' LIMIT 1),
    0,
    NOW() + INTERVAL '7 days',
    40, 15,
    NOW() - INTERVAL '10 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000007'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000002',
    7,
    'UI каталога товаров',
    'Верстка страницы каталога, карточки товаров, фильтры, сортировка',
    'a1000000-0000-0000-0000-000000000002',
    'a1000000-0000-0000-0000-000000000006',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'in_progress' LIMIT 1),
    1,
    NOW() + INTERVAL '5 days',
    32, 18,
    NOW() - INTERVAL '12 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000008'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000001',
    8,
    'Личный кабинет пользователя',
    'Профиль, история заказов, избранное, настройки уведомлений',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000006',
    (SELECT id FROM hb_task_priority WHERE alias = 'medium' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'todo' LIMIT 1),
    0,
    NOW() + INTERVAL '14 days',
    24, 0,
    NOW() - INTERVAL '5 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000009'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000001',
    9,
    'Админ-панель управления товарами',
    'CRUD товаров, загрузка изображений, управление категориями, импорт/экспорт',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000007',
    (SELECT id FROM hb_task_priority WHERE alias = 'medium' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'todo' LIMIT 1),
    1,
    NOW() + INTERVAL '21 days',
    40, 0,
    NOW() - INTERVAL '3 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000010'::uuid,
    'c1000000-0000-0000-0000-000000000001',
    'd1000000-0000-0000-0000-000000000001',
    'e1000000-0000-0000-0000-000000000001',
    10,
    'Email уведомления',
    'Отправка писем: подтверждение заказа, статус доставки, промо-рассылки',
    'a1000000-0000-0000-0000-000000000002',
    NULL,
    (SELECT id FROM hb_task_priority WHERE alias = 'low' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'todo' LIMIT 1),
    2,
    NOW() + INTERVAL '30 days',
    16, 0,
    NOW() - INTERVAL '2 days'
ON CONFLICT DO NOTHING;

-- =====================================================
-- TASKS - Mobile Banking (15 tasks)
-- =====================================================
INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at, completed_at)
SELECT
    '01000001-0000-0000-0000-000000000011'::uuid,
    'c1000000-0000-0000-0000-000000000002',
    'd1000000-0000-0000-0000-000000000003',
    'e1000000-0000-0000-0000-000000000009',
    1,
    'Биометрическая аутентификация',
    'Face ID, Touch ID, отпечаток пальца для Android',
    'a1000000-0000-0000-0000-000000000002',
    'a1000000-0000-0000-0000-000000000003',
    (SELECT id FROM hb_task_priority WHERE alias = 'highest' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'done' LIMIT 1),
    0,
    NOW() - INTERVAL '30 days',
    40, 45,
    NOW() - INTERVAL '50 days',
    NOW() - INTERVAL '32 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000012'::uuid,
    'c1000000-0000-0000-0000-000000000002',
    'd1000000-0000-0000-0000-000000000003',
    'e1000000-0000-0000-0000-000000000008',
    2,
    'Переводы между счетами',
    'Внутренние переводы, переводы в другие банки, SWIFT',
    'a1000000-0000-0000-0000-000000000002',
    'a1000000-0000-0000-0000-000000000007',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'review' LIMIT 1),
    0,
    NOW() + INTERVAL '5 days',
    32, 28,
    NOW() - INTERVAL '20 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000013'::uuid,
    'c1000000-0000-0000-0000-000000000002',
    'd1000000-0000-0000-0000-000000000003',
    'e1000000-0000-0000-0000-000000000007',
    3,
    'Push-уведомления о транзакциях',
    'Мгновенные уведомления о списаниях, поступлениях, подозрительной активности',
    'a1000000-0000-0000-0000-000000000002',
    'a1000000-0000-0000-0000-000000000006',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'in_progress' LIMIT 1),
    0,
    NOW() + INTERVAL '7 days',
    24, 12,
    NOW() - INTERVAL '10 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000014'::uuid,
    'c1000000-0000-0000-0000-000000000002',
    'd1000000-0000-0000-0000-000000000003',
    'e1000000-0000-0000-0000-000000000006',
    4,
    'Оплата коммунальных услуг',
    'Интеграция с поставщиками услуг, шаблоны платежей, автоплатежи',
    'a1000000-0000-0000-0000-000000000002',
    'a1000000-0000-0000-0000-000000000003',
    (SELECT id FROM hb_task_priority WHERE alias = 'medium' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'todo' LIMIT 1),
    0,
    NOW() + INTERVAL '14 days',
    32, 0,
    NOW() - INTERVAL '5 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000015'::uuid,
    'c1000000-0000-0000-0000-000000000002',
    'd1000000-0000-0000-0000-000000000003',
    'e1000000-0000-0000-0000-000000000006',
    5,
    'История операций с фильтрами',
    'Выписка за период, фильтр по категориям, экспорт в PDF/Excel',
    'a1000000-0000-0000-0000-000000000002',
    NULL,
    (SELECT id FROM hb_task_priority WHERE alias = 'medium' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'todo' LIMIT 1),
    1,
    NOW() + INTERVAL '21 days',
    24, 0,
    NOW() - INTERVAL '3 days'
ON CONFLICT DO NOTHING;

-- =====================================================
-- TASKS - HR Management (10 tasks)
-- =====================================================
INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at, completed_at)
SELECT
    '01000001-0000-0000-0000-000000000016'::uuid,
    'c1000000-0000-0000-0000-000000000003',
    'd1000000-0000-0000-0000-000000000004',
    'e1000000-0000-0000-0000-000000000012',
    1,
    'Модуль учёта сотрудников',
    'Карточки сотрудников, документы, контакты, история должностей',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000007',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'done' LIMIT 1),
    0,
    NOW() - INTERVAL '20 days',
    32, 36,
    NOW() - INTERVAL '40 days',
    NOW() - INTERVAL '22 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000017'::uuid,
    'c1000000-0000-0000-0000-000000000003',
    'd1000000-0000-0000-0000-000000000004',
    'e1000000-0000-0000-0000-000000000011',
    2,
    'Система отпусков',
    'Заявки на отпуск, согласование руководителем, календарь отпусков',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000003',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'in_progress' LIMIT 1),
    0,
    NOW() + INTERVAL '10 days',
    24, 16,
    NOW() - INTERVAL '15 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000018'::uuid,
    'c1000000-0000-0000-0000-000000000003',
    'd1000000-0000-0000-0000-000000000004',
    'e1000000-0000-0000-0000-000000000010',
    3,
    'KPI и оценка эффективности',
    'Настройка KPI, периодические оценки, отчёты по эффективности',
    'a1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000006',
    (SELECT id FROM hb_task_priority WHERE alias = 'medium' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'todo' LIMIT 1),
    0,
    NOW() + INTERVAL '21 days',
    40, 0,
    NOW() - INTERVAL '5 days'
ON CONFLICT DO NOTHING;

-- =====================================================
-- TASKS - CRM System (8 tasks)
-- =====================================================
INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000019'::uuid,
    'c1000000-0000-0000-0000-000000000004',
    'd1000000-0000-0000-0000-000000000005',
    'e1000000-0000-0000-0000-000000000014',
    1,
    'Воронка продаж',
    'Визуализация воронки, drag-n-drop сделок, конверсия по этапам',
    'a1000000-0000-0000-0000-000000000003',
    'a1000000-0000-0000-0000-000000000006',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'in_progress' LIMIT 1),
    0,
    NOW() + INTERVAL '7 days',
    32, 20,
    NOW() - INTERVAL '15 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000020'::uuid,
    'c1000000-0000-0000-0000-000000000004',
    'd1000000-0000-0000-0000-000000000005',
    'e1000000-0000-0000-0000-000000000013',
    2,
    'Импорт лидов из Excel',
    'Загрузка контактов из файла, маппинг полей, дедупликация',
    'a1000000-0000-0000-0000-000000000003',
    'a1000000-0000-0000-0000-000000000007',
    (SELECT id FROM hb_task_priority WHERE alias = 'medium' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'todo' LIMIT 1),
    0,
    NOW() + INTERVAL '14 days',
    16, 0,
    NOW() - INTERVAL '5 days'
ON CONFLICT DO NOTHING;

-- =====================================================
-- TASKS - DevOps (7 tasks)
-- =====================================================
INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at, completed_at)
SELECT
    '01000001-0000-0000-0000-000000000021'::uuid,
    'c1000000-0000-0000-0000-000000000005',
    'd1000000-0000-0000-0000-000000000006',
    'e1000000-0000-0000-0000-000000000018',
    1,
    'Настройка Kubernetes кластера',
    'Развернуть k8s кластер, настроить namespace, RBAC, Ingress',
    'a1000000-0000-0000-0000-000000000005',
    'a1000000-0000-0000-0000-000000000005',
    (SELECT id FROM hb_task_priority WHERE alias = 'highest' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'done' LIMIT 1),
    0,
    NOW() - INTERVAL '15 days',
    40, 48,
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '18 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000022'::uuid,
    'c1000000-0000-0000-0000-000000000005',
    'd1000000-0000-0000-0000-000000000006',
    'e1000000-0000-0000-0000-000000000017',
    2,
    'Мониторинг с Prometheus + Grafana',
    'Сбор метрик, алерты, дашборды для всех сервисов',
    'a1000000-0000-0000-0000-000000000005',
    'a1000000-0000-0000-0000-000000000005',
    (SELECT id FROM hb_task_priority WHERE alias = 'high' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'in_progress' LIMIT 1),
    0,
    NOW() + INTERVAL '7 days',
    24, 16,
    NOW() - INTERVAL '12 days'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, board_id, column_id, number, title, description, reporter_id, assignee_id, priority_id, status_id, position, due_date, estimated_hours, spent_hours, created_at)
SELECT
    '01000001-0000-0000-0000-000000000023'::uuid,
    'c1000000-0000-0000-0000-000000000005',
    'd1000000-0000-0000-0000-000000000006',
    'e1000000-0000-0000-0000-000000000016',
    3,
    'Централизованное логирование ELK',
    'Elasticsearch, Logstash, Kibana для сбора и анализа логов',
    'a1000000-0000-0000-0000-000000000005',
    NULL,
    (SELECT id FROM hb_task_priority WHERE alias = 'medium' LIMIT 1),
    (SELECT id FROM hb_task_status WHERE alias = 'todo' LIMIT 1),
    0,
    NOW() + INTERVAL '21 days',
    32, 0,
    NOW() - INTERVAL '5 days'
ON CONFLICT DO NOTHING;

-- =====================================================
-- TASK COMMENTS
-- =====================================================
INSERT INTO task_comments (id, task_id, user_id, content, created_at) VALUES
    ('00000009-0000-0000-0000-000000000001', '01000001-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000001', 'Какой платёжный провайдер используем? Stripe или PayPal?', NOW() - INTERVAL '8 days'),
    ('00000009-0000-0000-0000-000000000002', '01000001-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000003', 'Думаю лучше начать со Stripe - у них проще интеграция и лучше документация', NOW() - INTERVAL '8 days' + INTERVAL '2 hours'),
    ('00000009-0000-0000-0000-000000000003', '01000001-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000001', 'Согласен, начнём со Stripe. Потом добавим PayPal как альтернативу', NOW() - INTERVAL '7 days'),
    ('00000009-0000-0000-0000-000000000004', '01000001-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000002', 'Макеты готовы в Figma, ссылка в описании задачи', NOW() - INTERVAL '10 days'),
    ('00000009-0000-0000-0000-000000000005', '01000001-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000006', 'Спасибо! Начала работу над версткой', NOW() - INTERVAL '9 days'),
    ('00000009-0000-0000-0000-000000000006', '01000001-0000-0000-0000-000000000012', 'a1000000-0000-0000-0000-000000000002', 'Не забудьте про лимиты на переводы - нужно согласовать с безопасностью', NOW() - INTERVAL '15 days'),
    ('00000009-0000-0000-0000-000000000007', '01000001-0000-0000-0000-000000000012', 'a1000000-0000-0000-0000-000000000007', 'Уже связался с отделом безопасности, ждём требования', NOW() - INTERVAL '14 days'),
    ('00000009-0000-0000-0000-000000000008', '01000001-0000-0000-0000-000000000013', 'a1000000-0000-0000-0000-000000000006', 'Какой сервис используем для push - Firebase?', NOW() - INTERVAL '8 days'),
    ('00000009-0000-0000-0000-000000000009', '01000001-0000-0000-0000-000000000013', 'a1000000-0000-0000-0000-000000000002', 'Да, Firebase Cloud Messaging для обеих платформ', NOW() - INTERVAL '7 days'),
    ('00000009-0000-0000-0000-000000000010', '01000001-0000-0000-0000-000000000017', 'a1000000-0000-0000-0000-000000000003', 'Нужно учесть разные типы отпусков: ежегодный, учебный, без сохранения ЗП', NOW() - INTERVAL '12 days'),
    ('00000009-0000-0000-0000-000000000011', '01000001-0000-0000-0000-000000000017', 'a1000000-0000-0000-0000-000000000001', 'Да, и не забудьте про больничные', NOW() - INTERVAL '11 days'),
    ('00000009-0000-0000-0000-000000000012', '01000001-0000-0000-0000-000000000019', 'a1000000-0000-0000-0000-000000000003', 'Воронка должна быть настраиваемой - разные команды используют разные этапы', NOW() - INTERVAL '10 days'),
    ('00000009-0000-0000-0000-000000000013', '01000001-0000-0000-0000-000000000022', 'a1000000-0000-0000-0000-000000000005', 'Grafana дашборды готовы для backend сервисов, осталось frontend', NOW() - INTERVAL '5 days'),
    ('00000009-0000-0000-0000-000000000014', '01000001-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000004', 'Провела тестирование API каталога - нашла несколько багов, создала тикеты', NOW() - INTERVAL '3 days'),
    ('00000009-0000-0000-0000-000000000015', '01000001-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000008', 'Code review завершён, есть пара замечаний по оптимизации запросов', NOW() - INTERVAL '2 days')
ON CONFLICT DO NOTHING;

-- =====================================================
-- TASK TAGS
-- =====================================================
INSERT INTO task_tags (task_id, tag_id, created_at) VALUES
    ('01000001-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000002', NOW() - INTERVAL '55 days'),
    ('01000001-0000-0000-0000-000000000002', 'f1000000-0000-0000-0000-000000000002', NOW() - INTERVAL '54 days'),
    ('01000001-0000-0000-0000-000000000003', 'f1000000-0000-0000-0000-000000000002', NOW() - INTERVAL '48 days'),
    ('01000001-0000-0000-0000-000000000004', 'f1000000-0000-0000-0000-000000000002', NOW() - INTERVAL '18 days'),
    ('01000001-0000-0000-0000-000000000005', 'f1000000-0000-0000-0000-000000000002', NOW() - INTERVAL '13 days'),
    ('01000001-0000-0000-0000-000000000006', 'f1000000-0000-0000-0000-000000000004', NOW() - INTERVAL '8 days'),
    ('01000001-0000-0000-0000-000000000007', 'f1000000-0000-0000-0000-000000000003', NOW() - INTERVAL '10 days'),
    ('01000001-0000-0000-0000-000000000011', 'f1000000-0000-0000-0000-000000000006', NOW() - INTERVAL '48 days'),
    ('01000001-0000-0000-0000-000000000012', 'f1000000-0000-0000-0000-000000000006', NOW() - INTERVAL '18 days'),
    ('01000001-0000-0000-0000-000000000013', 'f1000000-0000-0000-0000-000000000008', NOW() - INTERVAL '8 days')
ON CONFLICT DO NOTHING;

-- =====================================================
-- TIME ENTRIES (for completed and in-progress tasks)
-- =====================================================
INSERT INTO time_entries (id, task_id, user_id, duration_minutes, description, started_at, created_at) VALUES
    ('00000008-0000-0000-0000-000000000001', '01000001-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000005', 480, 'Настройка Docker и docker-compose', NOW() - INTERVAL '56 days', NOW() - INTERVAL '56 days'),
    ('00000008-0000-0000-0000-000000000002', '01000001-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000005', 360, 'Настройка GitHub Actions', NOW() - INTERVAL '55 days', NOW() - INTERVAL '55 days'),
    ('00000008-0000-0000-0000-000000000003', '01000001-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000005', 240, 'Тестирование деплоя на staging', NOW() - INTERVAL '53 days', NOW() - INTERVAL '53 days'),
    ('00000008-0000-0000-0000-000000000004', '01000001-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000003', 480, 'Проектирование схемы БД', NOW() - INTERVAL '54 days', NOW() - INTERVAL '54 days'),
    ('00000008-0000-0000-0000-000000000005', '01000001-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000003', 480, 'Создание миграций', NOW() - INTERVAL '53 days', NOW() - INTERVAL '53 days'),
    ('00000008-0000-0000-0000-000000000006', '01000001-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000003', 360, 'Оптимизация индексов', NOW() - INTERVAL '51 days', NOW() - INTERVAL '51 days'),
    ('00000008-0000-0000-0000-000000000007', '01000001-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000003', 360, 'Документация ERD', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),
    ('00000008-0000-0000-0000-000000000008', '01000001-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000003', 480, 'Базовая JWT аутентификация', NOW() - INTERVAL '48 days', NOW() - INTERVAL '48 days'),
    ('00000008-0000-0000-0000-000000000009', '01000001-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000003', 480, 'Refresh токены', NOW() - INTERVAL '46 days', NOW() - INTERVAL '46 days'),
    ('00000008-0000-0000-0000-000000000010', '01000001-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000003', 480, 'OAuth2 интеграция', NOW() - INTERVAL '44 days', NOW() - INTERVAL '44 days'),
    ('00000008-0000-0000-0000-000000000011', '01000001-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000003', 480, 'Исследование Stripe API', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
    ('00000008-0000-0000-0000-000000000012', '01000001-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000003', 420, 'Базовая интеграция', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    ('00000008-0000-0000-0000-000000000013', '01000001-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000006', 480, 'Верстка карточек товаров', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    ('00000008-0000-0000-0000-000000000014', '01000001-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000006', 360, 'Фильтры и сортировка', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
    ('00000008-0000-0000-0000-000000000015', '01000001-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000006', 240, 'Адаптивная верстка', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days')
ON CONFLICT DO NOTHING;

-- =====================================================
-- PROJECT MEMBERS
-- =====================================================
INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001',
       (SELECT id FROM hb_role_in_project WHERE alias = 'OWNER' LIMIT 1), NOW() - INTERVAL '60 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000001' AND user_id = 'a1000000-0000-0000-0000-000000000001');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000003',
       (SELECT id FROM hb_role_in_project WHERE alias = 'ADMIN' LIMIT 1), NOW() - INTERVAL '58 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000001' AND user_id = 'a1000000-0000-0000-0000-000000000003');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000006',
       (SELECT id FROM hb_role_in_project WHERE alias = 'MEMBER' LIMIT 1), NOW() - INTERVAL '55 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000001' AND user_id = 'a1000000-0000-0000-0000-000000000006');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000007',
       (SELECT id FROM hb_role_in_project WHERE alias = 'MEMBER' LIMIT 1), NOW() - INTERVAL '52 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000001' AND user_id = 'a1000000-0000-0000-0000-000000000007');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000004',
       (SELECT id FROM hb_role_in_project WHERE alias = 'MEMBER' LIMIT 1), NOW() - INTERVAL '50 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000001' AND user_id = 'a1000000-0000-0000-0000-000000000004');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000002',
       (SELECT id FROM hb_role_in_project WHERE alias = 'OWNER' LIMIT 1), NOW() - INTERVAL '55 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000002' AND user_id = 'a1000000-0000-0000-0000-000000000002');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000003',
       (SELECT id FROM hb_role_in_project WHERE alias = 'ADMIN' LIMIT 1), NOW() - INTERVAL '52 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000002' AND user_id = 'a1000000-0000-0000-0000-000000000003');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000006',
       (SELECT id FROM hb_role_in_project WHERE alias = 'MEMBER' LIMIT 1), NOW() - INTERVAL '50 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000002' AND user_id = 'a1000000-0000-0000-0000-000000000006');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000007',
       (SELECT id FROM hb_role_in_project WHERE alias = 'MEMBER' LIMIT 1), NOW() - INTERVAL '48 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000002' AND user_id = 'a1000000-0000-0000-0000-000000000007');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001',
       (SELECT id FROM hb_role_in_project WHERE alias = 'OWNER' LIMIT 1), NOW() - INTERVAL '45 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000003' AND user_id = 'a1000000-0000-0000-0000-000000000001');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000003',
       (SELECT id FROM hb_role_in_project WHERE alias = 'MEMBER' LIMIT 1), NOW() - INTERVAL '42 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000003' AND user_id = 'a1000000-0000-0000-0000-000000000003');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000003',
       (SELECT id FROM hb_role_in_project WHERE alias = 'OWNER' LIMIT 1), NOW() - INTERVAL '40 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000004' AND user_id = 'a1000000-0000-0000-0000-000000000003');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000006',
       (SELECT id FROM hb_role_in_project WHERE alias = 'MEMBER' LIMIT 1), NOW() - INTERVAL '38 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000004' AND user_id = 'a1000000-0000-0000-0000-000000000006');

INSERT INTO project_members (id, project_id, user_id, role_id, joined_at)
SELECT gen_random_uuid(), 'c1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000005',
       (SELECT id FROM hb_role_in_project WHERE alias = 'OWNER' LIMIT 1), NOW() - INTERVAL '35 days'
WHERE NOT EXISTS (SELECT 1 FROM project_members WHERE project_id = 'c1000000-0000-0000-0000-000000000005' AND user_id = 'a1000000-0000-0000-0000-000000000005');
