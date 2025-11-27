-- Add notification-settings routes for all authenticated users

INSERT INTO taskflow.sys_available_routes (id, code, description_en, description_ru, created_at, updated_at)
VALUES
    (gen_random_uuid(), '/notification-settings', 'Get notification settings', 'Получение настроек уведомлений', NOW(), NOW()),
    (gen_random_uuid(), '/notification-settings/telegram/status', 'Get Telegram connection status', 'Статус подключения Telegram', NOW(), NOW()),
    (gen_random_uuid(), '/notification-settings/telegram/link', 'Generate Telegram link', 'Генерация ссылки Telegram', NOW(), NOW()),
    (gen_random_uuid(), '/notification-settings/telegram', 'Unlink Telegram', 'Отключение Telegram', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Grant access to USER role with method permissions
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get, method_put, method_delete)
SELECT r.id, ar.id, true, true, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'USER'
  AND ar.code LIKE '/notification-settings%'
  AND NOT EXISTS (
      SELECT 1 FROM taskflow.sys_role_linked_available_routes rlar
      WHERE rlar.role_id = r.id AND rlar.available_route_id = ar.id
  );

-- Grant access to ADMIN role with method permissions
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get, method_put, method_delete)
SELECT r.id, ar.id, true, true, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'ADMIN'
  AND ar.code LIKE '/notification-settings%'
  AND NOT EXISTS (
      SELECT 1 FROM taskflow.sys_role_linked_available_routes rlar
      WHERE rlar.role_id = r.id AND rlar.available_route_id = ar.id
  );

-- Also add notifications/unread/count route for USER if missing
INSERT INTO taskflow.sys_available_routes (id, code, description_en, description_ru, created_at, updated_at)
VALUES (gen_random_uuid(), '/notifications/unread/count', 'Get unread notifications count', 'Количество непрочитанных уведомлений', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get)
SELECT r.id, ar.id, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'USER'
  AND ar.code = '/notifications/unread/count'
  AND NOT EXISTS (
      SELECT 1 FROM taskflow.sys_role_linked_available_routes rlar
      WHERE rlar.role_id = r.id AND rlar.available_route_id = ar.id
  );
