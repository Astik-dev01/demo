-- V030: Fix missing permissions for /notifications/unread/count route
-- Grant access to ADMIN and MODERATOR roles

-- Grant access to ADMIN role
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get)
SELECT r.id, ar.id, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'ADMIN'
  AND ar.code = '/notifications/unread/count'
  AND NOT EXISTS (
      SELECT 1 FROM taskflow.sys_role_linked_available_routes rlar
      WHERE rlar.role_id = r.id AND rlar.available_route_id = ar.id
  );

-- Grant access to MODERATOR role
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get)
SELECT r.id, ar.id, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'MODERATOR'
  AND ar.code = '/notifications/unread/count'
  AND NOT EXISTS (
      SELECT 1 FROM taskflow.sys_role_linked_available_routes rlar
      WHERE rlar.role_id = r.id AND rlar.available_route_id = ar.id
  );
