-- V012: Route Access System
-- Система управления доступами к API маршрутам

-- 1. Добавляем новые колонки в sys_roles
ALTER TABLE sys_roles
    ADD COLUMN IF NOT EXISTS code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS name_ru VARCHAR(255),
    ADD COLUMN IF NOT EXISTS name_en VARCHAR(255),
    ADD COLUMN IF NOT EXISTS priority INTEGER DEFAULT 100,
    ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;

-- Заполняем code из name для существующих ролей
UPDATE sys_roles SET code = name WHERE code IS NULL;
UPDATE sys_roles SET name_ru = name, name_en = name WHERE name_ru IS NULL;
UPDATE sys_roles SET active = TRUE WHERE active IS NULL;

-- Делаем code уникальным после заполнения
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'sys_roles_code_unique'
    ) THEN
        ALTER TABLE sys_roles ADD CONSTRAINT sys_roles_code_unique UNIQUE (code);
    END IF;
END $$;

-- 2. Создаём таблицу доступных маршрутов
CREATE TABLE IF NOT EXISTS sys_available_routes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                VARCHAR(255) NOT NULL UNIQUE,
    description_ru      VARCHAR(500),
    description_en      VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,
    updated_by          UUID,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_sys_available_routes_code ON sys_available_routes(code);
CREATE INDEX IF NOT EXISTS idx_sys_available_routes_deleted ON sys_available_routes(is_deleted);

-- 3. Создаём связующую таблицу роли-маршруты
CREATE TABLE IF NOT EXISTS sys_role_linked_available_routes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id             UUID NOT NULL REFERENCES sys_roles(id) ON DELETE CASCADE,
    available_route_id  UUID NOT NULL REFERENCES sys_available_routes(id) ON DELETE CASCADE,
    method_get          BOOLEAN NOT NULL DEFAULT FALSE,
    method_post         BOOLEAN NOT NULL DEFAULT FALSE,
    method_put          BOOLEAN NOT NULL DEFAULT FALSE,
    method_delete       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,

    CONSTRAINT sys_role_route_unique UNIQUE(role_id, available_route_id)
);

CREATE INDEX IF NOT EXISTS idx_role_linked_routes_role ON sys_role_linked_available_routes(role_id);
CREATE INDEX IF NOT EXISTS idx_role_linked_routes_route ON sys_role_linked_available_routes(available_route_id);

-- 4. Добавляем колонку active в sys_user_roles если её нет
ALTER TABLE sys_user_roles ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;
UPDATE sys_user_roles SET active = TRUE WHERE active IS NULL;
