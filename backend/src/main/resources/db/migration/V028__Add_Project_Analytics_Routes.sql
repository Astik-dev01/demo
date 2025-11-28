-- Add project analytics routes

-- Project analytics overview route
INSERT INTO taskflow.sys_available_routes (id, code, description_en, description_ru, created_at, updated_at)
VALUES (gen_random_uuid(), '/analytics/projects/{id}', 'Get project analytics', 'Аналитика проекта', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Project burndown chart route
INSERT INTO taskflow.sys_available_routes (id, code, description_en, description_ru, created_at, updated_at)
VALUES (gen_random_uuid(), '/analytics/projects/{id}/burndown', 'Get project burndown chart', 'Burndown диаграмма проекта', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Project velocity chart route
INSERT INTO taskflow.sys_available_routes (id, code, description_en, description_ru, created_at, updated_at)
VALUES (gen_random_uuid(), '/analytics/projects/{id}/velocity', 'Get project velocity chart', 'Velocity диаграмма проекта', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Grant all project analytics routes to USER, MODERATOR, ADMIN
INSERT INTO taskflow.sys_role_linked_available_routes (id, role_id, available_route_id, method_get)
SELECT gen_random_uuid(), r.id, ar.id, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.code IN ('USER', 'MODERATOR', 'ADMIN')
AND ar.code IN ('/analytics/projects/{id}', '/analytics/projects/{id}/burndown', '/analytics/projects/{id}/velocity')
AND NOT EXISTS (
    SELECT 1 FROM taskflow.sys_role_linked_available_routes rla
    WHERE rla.role_id = r.id AND rla.available_route_id = ar.id
);
