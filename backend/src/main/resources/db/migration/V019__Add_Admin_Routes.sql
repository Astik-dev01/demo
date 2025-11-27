-- Add admin routes for admin panel
INSERT INTO sys_available_routes (id, code, description_ru, description_en, created_at, is_deleted)
VALUES
  (gen_random_uuid(), '/admin/users', 'Управление пользователями', 'User management', NOW(), false),
  (gen_random_uuid(), '/admin/roles', 'Управление системными ролями', 'System role management', NOW(), false),
  (gen_random_uuid(), '/admin/routes', 'Управление маршрутами API', 'API route management', NOW(), false),
  (gen_random_uuid(), '/admin/permissions', 'Управление правами доступа', 'Permission management', NOW(), false),
  (gen_random_uuid(), '/admin/projects', 'Управление всеми проектами', 'All projects management', NOW(), false),
  (gen_random_uuid(), '/admin/tasks', 'Управление всеми задачами', 'All tasks management', NOW(), false)
ON CONFLICT (code) DO NOTHING;

-- Link admin routes to ADMIN role with full access (GET, POST, PUT, DELETE)
INSERT INTO sys_role_linked_available_routes (id, role_id, available_route_id, method_get, method_post, method_put, method_delete, created_at)
SELECT
  gen_random_uuid(),
  r.id,
  ar.id,
  true,
  true,
  true,
  true,
  NOW()
FROM sys_roles r
CROSS JOIN sys_available_routes ar
WHERE r.name = 'ADMIN'
  AND ar.code IN ('/admin/users', '/admin/roles', '/admin/routes', '/admin/permissions', '/admin/projects', '/admin/tasks')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_linked_available_routes rlar
    WHERE rlar.role_id = r.id AND rlar.available_route_id = ar.id
  );
