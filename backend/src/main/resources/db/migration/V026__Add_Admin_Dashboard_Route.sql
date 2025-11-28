-- Add admin-dashboard route
INSERT INTO taskflow.sys_available_routes (id, code, description_en, description_ru, created_at, updated_at)
VALUES (gen_random_uuid(), '/analytics/admin-dashboard', 'Get admin dashboard statistics', 'Панель администратора', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Grant access to ADMIN role only
INSERT INTO taskflow.sys_role_linked_available_routes (id, role_id, available_route_id, method_get)
SELECT gen_random_uuid(), r.id, ar.id, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.code = 'ADMIN' AND ar.code = '/analytics/admin-dashboard'
AND NOT EXISTS (
    SELECT 1 FROM taskflow.sys_role_linked_available_routes rla
    WHERE rla.role_id = r.id AND rla.available_route_id = ar.id
);
