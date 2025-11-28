-- V025: Add missing /notifications/unread route

-- Add the /notifications/unread route
INSERT INTO taskflow.sys_available_routes (id, code, description_en, description_ru, created_at, updated_at)
VALUES (gen_random_uuid(), '/notifications/unread', 'Get unread notifications', 'Непрочитанные уведомления', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Grant access to USER role
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get)
SELECT r.id, ar.id, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'USER'
  AND ar.code = '/notifications/unread'
  AND NOT EXISTS (
      SELECT 1 FROM taskflow.sys_role_linked_available_routes rlar
      WHERE rlar.role_id = r.id AND rlar.available_route_id = ar.id
  );

-- Grant access to ADMIN role
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get)
SELECT r.id, ar.id, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'ADMIN'
  AND ar.code = '/notifications/unread'
  AND NOT EXISTS (
      SELECT 1 FROM taskflow.sys_role_linked_available_routes rlar
      WHERE rlar.role_id = r.id AND rlar.available_route_id = ar.id
  );

-- Grant access to MODERATOR role
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get)
SELECT r.id, ar.id, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'MODERATOR'
  AND ar.code = '/notifications/unread'
  AND NOT EXISTS (
      SELECT 1 FROM taskflow.sys_role_linked_available_routes rlar
      WHERE rlar.role_id = r.id AND rlar.available_route_id = ar.id
  );
