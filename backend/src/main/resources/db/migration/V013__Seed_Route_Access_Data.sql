-- V013: Seed route access data
-- Начальные данные для системы контроля доступа к маршрутам

-- 1. Регистрируем все доступные маршруты
INSERT INTO sys_available_routes (code, description_ru, description_en) VALUES
    -- Projects
    ('/projects', 'Управление проектами', 'Project management'),
    ('/projects/my', 'Мои проекты', 'My projects'),
    ('/projects/my/filter', 'Фильтрация моих проектов', 'Filter my projects'),
    ('/projects/public', 'Публичные проекты', 'Public projects'),
    ('/projects/public/filter', 'Фильтрация публичных проектов', 'Filter public projects'),
    ('/projects/key', 'Проект по ключу', 'Project by key'),
    ('/projects/members', 'Члены проекта', 'Project members'),

    -- Tasks
    ('/tasks', 'Управление задачами', 'Task management'),
    ('/tasks/my', 'Мои задачи', 'My tasks'),
    ('/tasks/key', 'Задача по ключу', 'Task by key'),
    ('/tasks/project', 'Задачи проекта', 'Project tasks'),
    ('/tasks/board', 'Задачи доски', 'Board tasks'),
    ('/tasks/column', 'Задачи колонки', 'Column tasks'),
    ('/tasks/move', 'Перемещение задачи', 'Move task'),
    ('/tasks/archive', 'Архивация задачи', 'Archive task'),
    ('/tasks/tags', 'Теги задач', 'Task tags'),
    ('/tasks/tags/project', 'Теги проекта', 'Project tags'),
    ('/tasks/comments', 'Комментарии задач', 'Task comments'),
    ('/tasks/attachments', 'Вложения задач', 'Task attachments'),

    -- Boards
    ('/boards', 'Управление досками', 'Board management'),
    ('/boards/project', 'Доски проекта', 'Project boards'),
    ('/boards/columns', 'Колонки доски', 'Board columns'),
    ('/boards/columns/reorder', 'Перестановка колонок', 'Reorder columns'),

    -- Time Tracking
    ('/time', 'Учёт времени', 'Time tracking'),
    ('/time/my', 'Мой учёт времени', 'My time entries'),
    ('/time/task', 'Время по задаче', 'Task time entries'),
    ('/time/timer/start', 'Запуск таймера', 'Start timer'),
    ('/time/timer/stop', 'Остановка таймера', 'Stop timer'),
    ('/time/timer/running', 'Активный таймер', 'Running timer'),
    ('/time/report/weekly', 'Недельный отчёт', 'Weekly report'),

    -- Teams
    ('/teams', 'Управление командами', 'Team management'),
    ('/teams/my', 'Мои команды', 'My teams'),
    ('/teams/members', 'Члены команды', 'Team members'),
    ('/teams/projects', 'Проекты команды', 'Team projects'),

    -- Users
    ('/users', 'Управление пользователями', 'User management'),
    ('/users/me', 'Текущий пользователь', 'Current user'),
    ('/users/search', 'Поиск пользователей', 'Search users'),

    -- Notifications
    ('/notifications', 'Уведомления', 'Notifications'),
    ('/notifications/unread-count', 'Количество непрочитанных', 'Unread count'),
    ('/notifications/read', 'Отметить прочитанным', 'Mark as read'),
    ('/notifications/read-all', 'Прочитать все', 'Mark all as read'),

    -- Analytics
    ('/analytics', 'Аналитика', 'Analytics'),
    ('/analytics/project', 'Аналитика проекта', 'Project analytics'),
    ('/analytics/user', 'Аналитика пользователя', 'User analytics'),

    -- Handbooks (справочники)
    ('/handbooks/project-types', 'Типы проектов', 'Project types'),
    ('/handbooks/project-roles', 'Роли в проектах', 'Project roles'),
    ('/handbooks/task-statuses', 'Статусы задач', 'Task statuses'),
    ('/handbooks/task-priorities', 'Приоритеты задач', 'Task priorities'),
    ('/handbooks/tag-categories', 'Категории тегов', 'Tag categories')
ON CONFLICT (code) DO NOTHING;

-- 2. Назначаем доступы для ролей

-- ADMIN получает полный доступ ко всем маршрутам
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT
    r.id,
    ar.id,
    true,
    true,
    true,
    true
FROM sys_roles r
CROSS JOIN sys_available_routes ar
WHERE r.name = 'ADMIN' AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- USER получает доступ к основным операциям
-- Проекты (полный доступ)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, true, true
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'USER' AND ar.code IN (
    '/projects', '/projects/my', '/projects/my/filter', '/projects/public', '/projects/public/filter',
    '/projects/key', '/projects/members'
) AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- Задачи (полный доступ)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, true, true
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'USER' AND ar.code IN (
    '/tasks', '/tasks/my', '/tasks/key', '/tasks/project', '/tasks/board', '/tasks/column',
    '/tasks/move', '/tasks/archive', '/tasks/tags', '/tasks/tags/project', '/tasks/comments', '/tasks/attachments'
) AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- Доски (полный доступ)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, true, true
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'USER' AND ar.code IN (
    '/boards', '/boards/project', '/boards/columns', '/boards/columns/reorder'
) AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- Учёт времени (полный доступ)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, true, true
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'USER' AND ar.code IN (
    '/time', '/time/my', '/time/task', '/time/timer/start', '/time/timer/stop',
    '/time/timer/running', '/time/report/weekly'
) AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- Команды (полный доступ)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, true, true
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'USER' AND ar.code IN (
    '/teams', '/teams/my', '/teams/members', '/teams/projects'
) AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- Пользователи (только чтение и обновление своего профиля)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, false, true, false
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'USER' AND ar.code IN ('/users', '/users/me', '/users/search') AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- Уведомления (полный доступ)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, true, true
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'USER' AND ar.code IN (
    '/notifications', '/notifications/unread-count', '/notifications/read', '/notifications/read-all'
) AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- Аналитика (только чтение)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, false, false
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'USER' AND ar.code IN ('/analytics', '/analytics/project', '/analytics/user') AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- Справочники (только чтение для USER)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, false, false, false
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'USER' AND ar.code IN (
    '/handbooks/project-types', '/handbooks/project-roles', '/handbooks/task-statuses',
    '/handbooks/task-priorities', '/handbooks/tag-categories'
) AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;

-- MODERATOR получает такие же права как USER плюс запись в справочники
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, true, false
FROM sys_roles r, sys_available_routes ar
WHERE r.name = 'MODERATOR' AND ar.is_deleted = false
ON CONFLICT (role_id, available_route_id) DO NOTHING;
