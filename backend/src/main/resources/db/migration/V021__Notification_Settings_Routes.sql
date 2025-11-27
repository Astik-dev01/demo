-- Add notification-settings routes for all authenticated users

INSERT INTO taskflow.available_routes (id, method, path, description, is_public, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'GET', '/notification-settings', 'Get notification settings', false, NOW(), NOW()),
    (gen_random_uuid(), 'PUT', '/notification-settings', 'Update notification settings', false, NOW(), NOW()),
    (gen_random_uuid(), 'GET', '/notification-settings/telegram/status', 'Get Telegram connection status', false, NOW(), NOW()),
    (gen_random_uuid(), 'GET', '/notification-settings/telegram/link', 'Generate Telegram link', false, NOW(), NOW()),
    (gen_random_uuid(), 'DELETE', '/notification-settings/telegram', 'Unlink Telegram', false, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Grant access to USER role
INSERT INTO taskflow.role_linked_available_routes (role_id, available_route_id)
SELECT r.id, ar.id
FROM taskflow.roles r, taskflow.available_routes ar
WHERE r.name = 'USER'
  AND ar.path LIKE '/notification-settings%'
ON CONFLICT DO NOTHING;

-- Grant access to ADMIN role
INSERT INTO taskflow.role_linked_available_routes (role_id, available_route_id)
SELECT r.id, ar.id
FROM taskflow.roles r, taskflow.available_routes ar
WHERE r.name = 'ADMIN'
  AND ar.path LIKE '/notification-settings%'
ON CONFLICT DO NOTHING;

-- Also add notifications/unread/count route for USER if missing
INSERT INTO taskflow.available_routes (id, method, path, description, is_public, created_at, updated_at)
VALUES (gen_random_uuid(), 'GET', '/notifications/unread/count', 'Get unread notifications count', false, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO taskflow.role_linked_available_routes (role_id, available_route_id)
SELECT r.id, ar.id
FROM taskflow.roles r, taskflow.available_routes ar
WHERE r.name = 'USER'
  AND ar.path = '/notifications/unread/count'
  AND ar.method = 'GET'
ON CONFLICT DO NOTHING;
