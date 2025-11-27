-- V022: Add routes for file upload endpoints

-- Insert routes for file management
INSERT INTO taskflow.sys_available_routes (id, code, description_en, description_ru, created_at, updated_at)
VALUES
    (gen_random_uuid(), '/files/upload', 'Upload file', 'Загрузка файла', NOW(), NOW()),
    (gen_random_uuid(), '/files/upload/multiple', 'Upload multiple files', 'Загрузка нескольких файлов', NOW(), NOW()),
    (gen_random_uuid(), '/files/upload/base64', 'Upload base64 file', 'Загрузка файла в base64', NOW(), NOW()),
    (gen_random_uuid(), '/files/download', 'Download file', 'Скачивание файла', NOW(), NOW()),
    (gen_random_uuid(), '/files/view', 'View file', 'Просмотр файла', NOW(), NOW()),
    (gen_random_uuid(), '/files/presigned-url', 'Get presigned URL', 'Получение временной ссылки', NOW(), NOW()),
    (gen_random_uuid(), '/files/list', 'List files', 'Список файлов', NOW(), NOW()),
    (gen_random_uuid(), '/users/me/avatar', 'Upload avatar', 'Загрузка аватара', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Grant permissions to USER role for file operations
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, false, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'USER'
  AND ar.code IN ('/files/upload', '/files/upload/multiple', '/files/upload/base64', '/files/download', '/files/view', '/files/presigned-url', '/files/list', '/users/me/avatar')
ON CONFLICT (role_id, available_route_id) DO UPDATE
SET method_get = true, method_post = true, method_delete = true;

-- Grant permissions to ADMIN role for file operations (including delete)
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, true, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'ADMIN'
  AND ar.code IN ('/files/upload', '/files/upload/multiple', '/files/upload/base64', '/files/download', '/files/view', '/files/presigned-url', '/files/list', '/users/me/avatar')
ON CONFLICT (role_id, available_route_id) DO UPDATE
SET method_get = true, method_post = true, method_put = true, method_delete = true;

-- Add routes for task attachments (if not already covered by /tasks pattern)
INSERT INTO taskflow.sys_available_routes (id, code, description_en, description_ru, created_at, updated_at)
VALUES
    (gen_random_uuid(), '/tasks/*/attachments', 'Task attachments', 'Вложения задачи', NOW(), NOW()),
    (gen_random_uuid(), '/tasks/*/attachments/multiple', 'Upload multiple task attachments', 'Загрузка нескольких вложений', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Grant permissions for task attachments to USER role
INSERT INTO taskflow.sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT r.id, ar.id, true, true, false, true
FROM taskflow.sys_roles r, taskflow.sys_available_routes ar
WHERE r.name = 'USER'
  AND ar.code IN ('/tasks/*/attachments', '/tasks/*/attachments/multiple')
ON CONFLICT (role_id, available_route_id) DO UPDATE
SET method_get = true, method_post = true, method_delete = true;
