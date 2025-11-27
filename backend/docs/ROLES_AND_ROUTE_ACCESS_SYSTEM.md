# Система управления ролями и доступами к API маршрутам

## 📋 Содержание

1. [Обзор системы](#обзор-системы)
2. [Архитектура](#архитектура)
3. [Структура базы данных](#структура-базы-данных)
4. [Компоненты системы](#компоненты-системы)
5. [Как это работает](#как-это-работает)
6. [Миграции и настройка](#миграции-и-настройка)
7. [Примеры использования](#примеры-использования)
8. [API для управления доступами](#api-для-управления-доступами)
9. [Best Practices](#best-practices)

---

## Обзор системы

Система управления доступами реализует **гранулярный контроль доступа** к API endpoints на уровне:
- **Роли** (Role) - кто может получить доступ
- **Маршруты** (Route) - к чему можно получить доступ
- **HTTP методы** (GET, POST, PUT, DELETE) - что можно делать

### Ключевые особенности

✅ **Гибридный режим** - поддержка старых `@PreAuthorize` аннотаций и новой системы route-based доступов
✅ **In-memory кэш** - высокая производительность проверки доступов (O(1) complexity)
✅ **Автоматическое обновление** - scheduled refresh кэша каждые 5 минут
✅ **Multiple roles per user** - пользователь может иметь несколько ролей одновременно
✅ **Fine-grained control** - отдельный контроль для каждого HTTP метода
✅ **Database-driven** - все конфигурации хранятся в БД, не требуют перезапуска приложения

---

## Архитектура

### Компоненты системы

```
┌─────────────────────────────────────────────────────────────────┐
│                      HTTP Request                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │   JWT Authentication Filter                              │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│            RouteAccessInterceptor                               │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  1. Check if path is excluded (auth, swagger, etc.)     │   │
│  │  2. Check if user is authenticated                       │   │
│  │  3. Check for legacy @PreAuthorize (hybrid mode)        │   │
│  │  4. Normalize URI (remove IDs, query params)            │   │
│  │  5. Call RouteAccessService.hasAccess()                 │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              RouteAccessService                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  1. Get current user from SecurityContext               │   │
│  │  2. Get user's active roles                             │   │
│  │  3. Check access for each role via RouteCacheService   │   │
│  │  4. Return true if ANY role has access                  │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              RouteCacheService (In-Memory Cache)                │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ConcurrentHashMap<String, RouteAccessCacheDto>         │   │
│  │  Key: "roleCode:routeCode"                              │   │
│  │  Value: {methodGet, methodPost, methodPut, methodDelete}│   │
│  │                                                          │   │
│  │  O(1) lookup complexity                                 │   │
│  │  Thread-safe with ReentrantReadWriteLock               │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   PostgreSQL Database                           │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────────┐  │
│  │  sys_roles     │  │ sys_available_ │  │ sys_role_linked_ │  │
│  │                │  │ routes         │  │ available_routes │  │
│  │  - id          │  │ - id           │  │ - role_id        │  │
│  │  - code        │  │ - code         │  │ - route_id       │  │
│  │  - name_ru     │  │ - description  │  │ - method_get     │  │
│  │  - priority    │  └────────────────┘  │ - method_post    │  │
│  │  - is_system   │                      │ - method_put     │  │
│  └────────────────┘                      │ - method_delete  │  │
│                                          └──────────────────┘  │
│  ┌────────────────┐                                            │
│  │ sys_user_roles │  Many-to-Many                             │
│  │ - user_id      │  User ←→ Role                             │
│  │ - role_id      │                                            │
│  │ - active       │                                            │
│  └────────────────┘                                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Структура базы данных

### 1. Таблица `sys_roles`

Хранит все роли системы с их метаданными.

```sql
CREATE TABLE sys_roles (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,  -- SUPPORT_DISPATCHER, SUPPORT_TECHNICIAN, etc.
    name_ru             VARCHAR(255) NOT NULL,        -- Название роли на русском
    name_ky             VARCHAR(255),                 -- Название на кыргызском
    name_en             VARCHAR(255),                 -- Название на английском
    description         TEXT,                         -- Описание полномочий роли
    active              BOOLEAN NOT NULL DEFAULT true,
    priority            INTEGER DEFAULT 100,          -- Приоритет для сортировки (меньше = выше)
    is_system           BOOLEAN NOT NULL DEFAULT false, -- Системная роль (нельзя удалить)
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          BIGINT,
    deleted             BOOLEAN NOT NULL DEFAULT false
);
```

**Пример данных:**
```sql
INSERT INTO sys_roles (code, name_ru, description, priority, is_system)
VALUES
    ('SUPERADMIN', 'Супер-администратор', 'Полный доступ ко всем модулям', 1, true),
    ('SUPPORT_ADMINISTRATOR', 'ТП. Администратор', 'Администратор системы техподдержки', 10, true),
    ('SUPPORT_DISPATCHER', 'ТП. Диспетчер', 'Распределение заявок и управление очередью', 20, false),
    ('SUPPORT_TECHNICIAN', 'ТП. Техник', 'Выполнение технических заявок', 30, false),
    ('SUPPORT_OPERATOR', 'ТП. Оператор', 'Создание заявок и отслеживание статуса', 40, false);
```

### 2. Таблица `sys_available_routes`

Регистр всех доступных API endpoints в системе.

```sql
CREATE TABLE sys_available_routes (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(255) NOT NULL UNIQUE,  -- '/api/support/tickets', 'POST /api/auth/login'
    description_ru      VARCHAR(500),                  -- Описание endpoint на русском
    description_ky      VARCHAR(500),                  -- Описание на кыргызском
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT,
    deleted             BOOLEAN NOT NULL DEFAULT false
);
```

**Важные замечания:**
- `code` может содержать **только путь** (`/api/support/tickets`) или **метод + путь** (`POST /api/director/analytics/general-kpi`)
- Если указан метод в code, то используется точное совпадение
- Если метод не указан, применяется к любому методу (зависит от `method_*` колонок в связующей таблице)

**Пример данных:**
```sql
INSERT INTO sys_available_routes (code, description_ru, description_ky)
VALUES
    ('/api/support/tickets', 'Управление заявками техподдержки', 'Техникалык колдоону арыздарды башкаруу'),
    ('/api/support/tickets/filter', 'Фильтрация заявок', 'Арыздарды чыпкалоо'),
    ('POST /api/director/analytics/general-kpi', 'Получить общие KPI показатели', 'Жалпы KPI көрсөткүчтөрдү алуу'),
    ('/api/auth/qr/generate', 'QR-авторизация: генерация QR-кода', 'QR-авторизация: QR-кодду түзүү');
```

### 3. Таблица `sys_role_linked_available_routes`

Связующая таблица many-to-many между ролями и маршрутами с гранулярным контролем HTTP методов.

```sql
CREATE TABLE sys_role_linked_available_routes (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL REFERENCES sys_roles(id),
    available_route_id  BIGINT NOT NULL REFERENCES sys_available_routes(id),
    method_get          BOOLEAN NOT NULL DEFAULT false,
    method_post         BOOLEAN NOT NULL DEFAULT false,
    method_put          BOOLEAN NOT NULL DEFAULT false,
    method_delete       BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,

    UNIQUE(role_id, available_route_id)  -- Одна роль не может иметь дубликатов для одного маршрута
);
```

**Пример данных:**
```sql
-- Роль SUPPORT_DISPATCHER имеет полный доступ к заявкам
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
VALUES
    (
        (SELECT id FROM sys_roles WHERE code = 'SUPPORT_DISPATCHER'),
        (SELECT id FROM sys_available_routes WHERE code = '/api/support/tickets'),
        true,  -- GET - просмотр заявок
        true,  -- POST - создание заявок
        true,  -- PUT - редактирование заявок
        false  -- DELETE - удаление запрещено
    );

-- Роль SUPPORT_OPERATOR имеет только чтение и создание заявок
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
VALUES
    (
        (SELECT id FROM sys_roles WHERE code = 'SUPPORT_OPERATOR'),
        (SELECT id FROM sys_available_routes WHERE code = '/api/support/tickets'),
        true,  -- GET - просмотр заявок
        true,  -- POST - создание заявок
        false, -- PUT - редактирование запрещено
        false  -- DELETE - удаление запрещено
    );
```

### 4. Таблица `sys_user_roles`

Many-to-many связь пользователей и ролей.

```sql
CREATE TABLE sys_user_roles (
    user_id             BIGINT NOT NULL REFERENCES sys_users(id),
    role_id             BIGINT NOT NULL REFERENCES sys_roles(id),
    assigned_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by         BIGINT,
    active              BOOLEAN NOT NULL DEFAULT true,

    PRIMARY KEY (user_id, role_id)
);
```

**Важно:** Пользователь может иметь **несколько активных ролей одновременно**. Доступ предоставляется, если **хотя бы одна** из активных ролей имеет разрешение.

---

## Компоненты системы

### 1. RouteAccessInterceptor

**Файл:** `RouteAccessInterceptor.java`

**Назначение:** Перехватывает все HTTP запросы и проверяет права доступа перед вызовом контроллера.

**Основные методы:**
- `preHandle()` - главный метод проверки доступа
- `isExcludedPath()` - проверка исключенных путей (auth, swagger, websocket)
- `hasLegacyAuthorization()` - проверка наличия `@PreAuthorize` (гибридный режим)
- `normalizeUri()` - нормализация URI для сопоставления с маршрутами в БД

**Исключенные пути (не проверяются):**
```java
private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
    "/auth/",                                      // Аутентификация
    "/swagger-ui/",                                // Swagger UI
    "/api-docs/",                                  // OpenAPI docs
    "/actuator/health",                            // Health check
    "/actuator/info",                              // Info endpoint
    "/ws/",                                        // WebSocket
    "/support/hb/equipment/categories/icon",       // Иконки категорий
    "/support/hb/equipment/subcategories/icon",    // Иконки подкатегорий
    "OPTIONS"                                      // CORS preflight
));
```

**Нормализация URI:**
```java
// Примеры нормализации:
"/api/tickets/123"          → "/api/tickets"          // удаление числовых ID
"/api/tickets/123/comments" → "/api/tickets/comments" // удаление ID в середине пути
"/api/tickets?page=1"       → "/api/tickets"          // удаление query параметров
"/api/tickets/"             → "/api/tickets"          // удаление trailing slash
```

**Конфигурация:**
```properties
# В application.properties можно отключить интерцептор
app.route-access.enabled=true  # По умолчанию включен
```

### 2. RouteAccessService

**Файл:** `RouteAccessService.java`

**Назначение:** Высокоуровневый сервис для проверки доступов пользователей.

**Основные методы:**

```java
// Проверить доступ для текущего пользователя
boolean hasAccess(String routeCode, String httpMethod);

// Проверить доступ для конкретного пользователя
boolean hasAccess(User user, String routeCode, String httpMethod);

// Получить все доступные маршруты для пользователя
List<String> getAccessibleRoutes(String httpMethod);

// Проверить административный доступ
boolean hasAdminAccess();

// Создать или обновить доступ роли к маршруту
void grantAccess(String roleCode, String routeCode,
                 boolean methodGet, boolean methodPost,
                 boolean methodPut, boolean methodDelete);

// Отозвать доступ роли к маршруту
void revokeAccess(String roleCode, String routeCode);

// Обновить кэш
void refreshCache();
void refreshRoleCache(String roleCode);
```

**Логика проверки доступа:**
1. Получить текущего пользователя из `SecurityContext`
2. Получить все **активные роли** пользователя
3. Проверить доступ через кэш для **каждой роли**
4. Вернуть `true`, если **хотя бы одна роль** имеет доступ

### 3. RouteCacheService

**Файл:** `RouteCacheService.java`

**Назначение:** In-memory кэш для высокопроизводительной проверки доступов.

**Структура кэша:**
```java
// ConcurrentHashMap для thread-safe доступа
private final ConcurrentHashMap<String, RouteAccessCacheDto> accessCache;

// Ключ кэша: "roleCode:routeCode"
String cacheKey = "SUPPORT_DISPATCHER:/api/support/tickets";

// Значение кэша: DTO с правами доступа
RouteAccessCacheDto {
    String roleCode;
    String routeCode;
    Boolean methodGet;
    Boolean methodPost;
    Boolean methodPut;
    Boolean methodDelete;
}
```

**Основные методы:**

```java
// Инициализация кэша при старте приложения
@PostConstruct
void initializeCache();

// Проверить доступ (O(1) complexity)
boolean hasAccess(String roleCode, String routeCode, String httpMethod);

// Получить детальную информацию о доступе
Optional<RouteAccessCacheDto> getAccess(String roleCode, String routeCode);

// Обновление кэша
void refreshFullCache();              // Полное обновление
void refreshRoleAccess(String role);  // Обновление для конкретной роли
void refreshRouteAccess(String route);// Обновление для конкретного маршрута

// Управление кэшем
void evictAccess(String role, String route); // Удалить конкретную запись
void clearCache();                            // Полная очистка

// Статистика
CacheStatistics getCacheStatistics();
```

**Thread Safety:**
- Использует `ReentrantReadWriteLock` для синхронизации
- Множественные читатели могут работать одновременно
- Только один поток может обновлять кэш
- `ConcurrentHashMap` обеспечивает безопасность на уровне операций

### 4. RouteAccessCacheScheduler

**Файл:** `RouteAccessCacheScheduler.java`

**Назначение:** Автоматическое обновление кэша по расписанию.

**Расписание:**
```java
// Обновление каждые 5 минут
@Scheduled(fixedRate = 300000) // 5 * 60 * 1000 = 300000ms
public void scheduledCacheRefresh() {
    cacheService.refreshFullCache();
}

// Проверка целостности каждую минуту
@Scheduled(fixedRate = 60000)
public void checkCacheIntegrity() {
    CacheStatistics stats = cacheService.getCacheStatistics();
    log.info("Cache integrity check - Total entries: {}, Last refresh: {}",
             stats.totalEntries(), stats.lastRefresh());
}
```

### 5. RouteAccessController

**Файл:** `RouteAccessController.java`

**Назначение:** REST API для управления доступами (только для администраторов).

**Endpoints:**

```java
// Получить все доступы для роли
GET /api/admin/route-access/role/{roleCode}

// Обновить кэш вручную
POST /api/admin/route-access/cache/refresh

// Получить статистику кэша
GET /api/admin/route-access/cache/stats
```

---

## Как это работает

### Поток проверки доступа

```
1. HTTP Request → Spring Security → JWT Authentication
   ↓
2. RouteAccessInterceptor.preHandle()
   ↓
3. Check: Is path excluded? (auth, swagger, etc.)
   YES → Allow request
   NO → Continue
   ↓
4. Check: Is user authenticated?
   NO → Deny (Spring Security handles this)
   YES → Continue
   ↓
5. Check: Has @PreAuthorize annotation? (hybrid mode)
   YES → Use Spring Security @PreAuthorize
   NO → Continue with route access check
   ↓
6. Normalize URI: /api/tickets/123 → /api/tickets
   ↓
7. RouteAccessService.hasAccess(normalizedUri, httpMethod)
   ↓
8. Get current user's active roles
   ↓
9. For each role: RouteCacheService.hasAccess(roleCode, routeCode, httpMethod)
   ↓
10. Check cache: ConcurrentHashMap.get("roleCode:routeCode")
    ↓
    Cache HIT → Return permissions
    Cache MISS → Load from DB → Cache it → Return permissions
    ↓
11. Return TRUE if ANY role has access, FALSE otherwise
    ↓
12. If FALSE → HTTP 403 Forbidden
    If TRUE → Continue to Controller
```

### Пример проверки доступа

**Сценарий:** Пользователь `dispatcher1` (роли: `SUPPORT_DISPATCHER`, `QR_MANAGER`) делает запрос:
```
POST /api/support/tickets/filter
```

**Процесс:**

1. **Interceptor нормализует URI:**
   ```
   POST /api/support/tickets/filter → /api/support/tickets/filter
   ```

2. **Service получает активные роли:**
   ```java
   roles = ["SUPPORT_DISPATCHER", "QR_MANAGER"]
   ```

3. **Cache проверяет доступ для каждой роли:**
   ```java
   // Проверка SUPPORT_DISPATCHER
   cacheKey = "SUPPORT_DISPATCHER:/api/support/tickets/filter"
   access = {methodGet: true, methodPost: true, methodPut: true, methodDelete: false}
   hasAccess("POST") → true ✅

   // Можно не проверять QR_MANAGER, т.к. уже есть доступ
   ```

4. **Результат:** Доступ разрешен ✅

---

## Миграции и настройка

### Шаг 1: Создание таблиц (V001)

```sql
-- V001__Initial_Schema.sql

-- Таблица ролей
CREATE TABLE sys_roles (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    name_ru             VARCHAR(255) NOT NULL,
    name_ky             VARCHAR(255),
    name_en             VARCHAR(255),
    description         TEXT,
    active              BOOLEAN NOT NULL DEFAULT true,
    priority            INTEGER DEFAULT 100,
    is_system           BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          BIGINT,
    deleted             BOOLEAN NOT NULL DEFAULT false
);

-- Таблица доступных маршрутов
CREATE TABLE sys_available_routes (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(255) NOT NULL UNIQUE,
    description_ru      VARCHAR(500),
    description_ky      VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT,
    deleted             BOOLEAN NOT NULL DEFAULT false
);

-- Связующая таблица роли-маршруты
CREATE TABLE sys_role_linked_available_routes (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL REFERENCES sys_roles(id),
    available_route_id  BIGINT NOT NULL REFERENCES sys_available_routes(id),
    method_get          BOOLEAN NOT NULL DEFAULT false,
    method_post         BOOLEAN NOT NULL DEFAULT false,
    method_put          BOOLEAN NOT NULL DEFAULT false,
    method_delete       BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,

    UNIQUE(role_id, available_route_id)
);

-- Таблица пользователи-роли
CREATE TABLE sys_user_roles (
    user_id             BIGINT NOT NULL REFERENCES sys_users(id),
    role_id             BIGINT NOT NULL REFERENCES sys_roles(id),
    assigned_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by         BIGINT,
    active              BOOLEAN NOT NULL DEFAULT true,

    PRIMARY KEY (user_id, role_id)
);
```

### Шаг 2: Создание ролей (V002)

```sql
-- V002__Create_Roles.sql

INSERT INTO sys_roles (code, name_ru, name_ky, description, priority, is_system)
VALUES
    (
        'SUPERADMIN',
        'Супер-администратор',
        'Супер-администратор',
        'Главный администратор системы с полным доступом ко всем модулям',
        1,
        true
    ),
    (
        'SUPPORT_ADMINISTRATOR',
        'ТП. Администратор',
        'ТК. Администратор',
        'Администратор системы технической поддержки',
        10,
        true
    ),
    (
        'SUPPORT_DISPATCHER',
        'ТП. Диспетчер',
        'ТК. Диспетчер',
        'Диспетчер для распределения заявок и управления очередью',
        20,
        false
    ),
    (
        'SUPPORT_TECHNICIAN',
        'ТП. Техник',
        'ТК. Техник',
        'Технический специалист для выполнения заявок',
        30,
        false
    ),
    (
        'SUPPORT_OPERATOR',
        'ТП. Оператор',
        'ТК. Оператор',
        'Оператор АЗС для создания заявок и отслеживания статуса',
        40,
        false
    );
```

### Шаг 3: Регистрация маршрутов (V003)

```sql
-- V003__Register_Routes.sql

-- Регистрируем все endpoints модуля Support System
INSERT INTO sys_available_routes (code, description_ru, description_ky)
VALUES
    -- Аутентификация
    ('/api/auth/login', 'Вход в систему', 'Системага кирүү'),
    ('/api/auth/logout', 'Выход из системы', 'Системадан чыгуу'),
    ('/api/auth/refresh', 'Обновление токена', 'Токенди жаңыртуу'),

    -- Заявки (tickets)
    ('/api/support/tickets', 'Управление заявками', 'Арыздарды башкаруу'),
    ('/api/support/tickets/filter', 'Фильтрация заявок', 'Арыздарды чыпкалоо'),
    ('/api/support/tickets/my', 'Мои заявки', 'Менин арыздарым'),
    ('/api/support/tickets/assign', 'Назначить заявку', 'Арызды дайындоо'),

    -- Оборудование
    ('/api/support/hb/equipment', 'Справочник оборудования', 'Жабдуулардын каттамасы'),
    ('/api/support/hb/equipment/filter', 'Фильтрация оборудования', 'Жабдууларды чыпкалоо'),
    ('/api/support/hb/equipment-categories', 'Категории оборудования', 'Жабдуу категориялары'),
    ('/api/support/hb/equipment-subcategories', 'Подкатегории оборудования', 'Жабдуу субкатегориялары')
ON CONFLICT (code) DO NOTHING;
```

### Шаг 4: Назначение доступов (V004)

```sql
-- V004__Assign_Route_Access.sql

DO $$
DECLARE
    superadmin_role_id BIGINT;
    dispatcher_role_id BIGINT;
    technician_role_id BIGINT;
    operator_role_id BIGINT;
    route_record RECORD;
BEGIN
    -- Получаем ID ролей
    SELECT id INTO superadmin_role_id FROM sys_roles WHERE code = 'SUPERADMIN';
    SELECT id INTO dispatcher_role_id FROM sys_roles WHERE code = 'SUPPORT_DISPATCHER';
    SELECT id INTO technician_role_id FROM sys_roles WHERE code = 'SUPPORT_TECHNICIAN';
    SELECT id INTO operator_role_id FROM sys_roles WHERE code = 'SUPPORT_OPERATOR';

    -- SUPERADMIN получает полный доступ ко всем маршрутам
    FOR route_record IN SELECT id FROM sys_available_routes WHERE deleted = false
    LOOP
        INSERT INTO sys_role_linked_available_routes (
            role_id, available_route_id,
            method_get, method_post, method_put, method_delete
        ) VALUES (
            superadmin_role_id, route_record.id,
            true, true, true, true
        ) ON CONFLICT (role_id, available_route_id) DO NOTHING;
    END LOOP;

    -- SUPPORT_DISPATCHER получает полный доступ к заявкам
    INSERT INTO sys_role_linked_available_routes (
        role_id, available_route_id,
        method_get, method_post, method_put, method_delete
    )
    SELECT
        dispatcher_role_id,
        id,
        true,  -- GET
        true,  -- POST
        true,  -- PUT
        false  -- DELETE запрещен
    FROM sys_available_routes
    WHERE code LIKE '/api/support/tickets%' AND deleted = false
    ON CONFLICT (role_id, available_route_id) DO NOTHING;

    -- SUPPORT_TECHNICIAN получает доступ к своим заявкам
    INSERT INTO sys_role_linked_available_routes (
        role_id, available_route_id,
        method_get, method_post, method_put, method_delete
    )
    SELECT
        technician_role_id,
        id,
        true,  -- GET - просмотр своих заявок
        false, -- POST - создание запрещено
        true,  -- PUT - обновление статуса разрешено
        false  -- DELETE запрещен
    FROM sys_available_routes
    WHERE code IN ('/api/support/tickets/my', '/api/support/tickets')
      AND deleted = false
    ON CONFLICT (role_id, available_route_id) DO NOTHING;

    -- SUPPORT_OPERATOR получает доступ создания заявок
    INSERT INTO sys_role_linked_available_routes (
        role_id, available_route_id,
        method_get, method_post, method_put, method_delete
    )
    SELECT
        operator_role_id,
        id,
        true,  -- GET - просмотр заявок
        true,  -- POST - создание заявок
        false, -- PUT - редактирование запрещено
        false  -- DELETE запрещен
    FROM sys_available_routes
    WHERE code IN ('/api/support/tickets', '/api/support/tickets/filter', '/api/support/tickets/my')
      AND deleted = false
    ON CONFLICT (role_id, available_route_id) DO NOTHING;

    RAISE NOTICE 'Route access configuration completed successfully';
END $$;
```

### Шаг 5: Добавление новой роли и маршрутов (пример)

**Сценарий:** Добавляем роль `QR_MANAGER` с доступом к генерации QR-кодов.

```sql
-- V020__Add_QR_Manager_Role.sql

DO $$
DECLARE
    qr_manager_role_id BIGINT;
    qr_generate_route_id BIGINT;
    qr_generate_pdf_route_id BIGINT;
BEGIN
    -- 1. Создаем роль QR_MANAGER
    INSERT INTO sys_roles (
        code, name_ru, name_ky, description, active
    ) VALUES (
        'QR_MANAGER',
        'Менеджер QR-кодов',
        'QR-код менеджери',
        'Управление генерацией QR-кодов для авторизации операторов',
        true
    ) RETURNING id INTO qr_manager_role_id;

    RAISE NOTICE 'Создана роль QR_MANAGER (id: %)', qr_manager_role_id;

    -- 2. Регистрируем маршруты QR генерации (если еще не зарегистрированы)
    INSERT INTO sys_available_routes (code, description_ru, description_ky)
    VALUES
        ('/api/auth/qr/generate', 'QR-авторизация: генерация QR-кода', 'QR-авторизация: QR-кодду түзүү'),
        ('/api/auth/qr/generate-pdf', 'QR-авторизация: генерация PDF с QR-кодами', 'QR-авторизация: QR-коддору менен PDF түзүү')
    ON CONFLICT (code) DO NOTHING;

    -- 3. Получаем ID маршрутов
    SELECT id INTO qr_generate_route_id
    FROM sys_available_routes
    WHERE code = '/api/auth/qr/generate';

    SELECT id INTO qr_generate_pdf_route_id
    FROM sys_available_routes
    WHERE code = '/api/auth/qr/generate-pdf';

    -- 4. Добавляем доступ к QR генерации для роли QR_MANAGER
    INSERT INTO sys_role_linked_available_routes (
        role_id, available_route_id,
        method_get, method_post, method_put, method_delete
    ) VALUES
        (qr_manager_role_id, qr_generate_route_id, false, true, false, false),
        (qr_manager_role_id, qr_generate_pdf_route_id, false, true, false, false);

    RAISE NOTICE 'Добавлены права на QR генерацию для роли QR_MANAGER';

    -- 5. Назначаем роль QR_MANAGER пользователям dispatcher1 и dispatcher2
    INSERT INTO sys_user_roles (user_id, role_id, assigned_at, active)
    SELECT u.id, qr_manager_role_id, NOW(), true
    FROM sys_users u
    WHERE u.username IN ('dispatcher1', 'dispatcher2')
    ON CONFLICT (user_id, role_id) DO UPDATE SET active = true;

    RAISE NOTICE 'Роль QR_MANAGER назначена dispatcher1 и dispatcher2';

END $$;
```

---

## Примеры использования

### Пример 1: Проверка доступа в Service

```java
@Service
@RequiredArgsConstructor
public class TicketService {

    private final RouteAccessService routeAccessService;

    public void deleteTicket(Long ticketId) {
        // Проверяем, имеет ли пользователь право на DELETE
        if (!routeAccessService.hasAccess("/api/support/tickets", "DELETE")) {
            throw new AccessDeniedException("You don't have permission to delete tickets");
        }

        // ... логика удаления
    }
}
```

### Пример 2: Получение доступных маршрутов для UI

```java
@RestController
@RequestMapping("/api/user/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final RouteAccessService routeAccessService;

    @GetMapping("/routes")
    public List<String> getAccessibleRoutes(@RequestParam String method) {
        // Вернуть все маршруты, доступные текущему пользователю для указанного HTTP метода
        return routeAccessService.getAccessibleRoutes(method);
    }
}
```

**Ответ API:**
```json
{
  "success": true,
  "result": [
    "/api/support/tickets",
    "/api/support/tickets/filter",
    "/api/support/tickets/my",
    "/api/support/hb/equipment",
    "/api/support/hb/equipment-categories"
  ]
}
```

### Пример 3: Ручное обновление кэша

```java
@RestController
@RequestMapping("/api/admin/route-access")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
public class RouteAccessAdminController {

    private final RouteAccessService routeAccessService;

    @PostMapping("/cache/refresh")
    public ResponseEntity<String> refreshCache() {
        routeAccessService.refreshCache();
        return ResponseEntity.ok("Cache refreshed successfully");
    }

    @PostMapping("/cache/refresh/role/{roleCode}")
    public ResponseEntity<String> refreshRoleCache(@PathVariable String roleCode) {
        routeAccessService.refreshRoleCache(roleCode);
        return ResponseEntity.ok("Role cache refreshed for: " + roleCode);
    }

    @GetMapping("/cache/stats")
    public ResponseEntity<CacheStatistics> getCacheStats() {
        return ResponseEntity.ok(routeAccessService.getCacheStatistics());
    }
}
```

### Пример 4: Назначение доступов программно

```java
@Service
@RequiredArgsConstructor
public class RoleManagementService {

    private final RouteAccessService routeAccessService;

    public void grantTicketAccessToRole(String roleCode) {
        // Дать роли полный доступ к заявкам
        routeAccessService.grantAccess(
            roleCode,
            "/api/support/tickets",
            true,  // GET
            true,  // POST
            true,  // PUT
            false  // DELETE запрещен
        );
    }

    public void revokeTicketAccess(String roleCode) {
        // Отозвать доступ роли к заявкам
        routeAccessService.revokeAccess(roleCode, "/api/support/tickets");
    }
}
```

### Пример 5: Добавление нового endpoint с доступом

**Шаг 1:** Создать контроллер
```java
@RestController
@RequestMapping("/api/director/analytics")
@RequiredArgsConstructor
public class DirectorAnalyticsController {

    @PostMapping("/general-kpi")
    public ResponseEntity<KpiResponse> getGeneralKpi(@RequestBody KpiRequest request) {
        // ... логика
    }
}
```

**Шаг 2:** Создать миграцию для регистрации маршрута
```sql
-- V027__Add_Director_Analytics_Routes.sql

-- Регистрируем маршрут
INSERT INTO sys_available_routes (code, description_ru, description_ky)
VALUES (
    'POST /api/director/analytics/general-kpi',
    'Получить общие KPI показатели',
    'Жалпы KPI көрсөткүчтөрдү алуу'
) ON CONFLICT (code) DO NOTHING;

-- Привязываем к роли SUPPORT_DIRECTOR
INSERT INTO sys_role_linked_available_routes (
    role_id,
    available_route_id,
    method_get,
    method_post,
    method_put,
    method_delete
)
SELECT
    (SELECT id FROM sys_roles WHERE code = 'SUPPORT_DIRECTOR'),
    (SELECT id FROM sys_available_routes WHERE code = 'POST /api/director/analytics/general-kpi'),
    false,  -- GET
    true,   -- POST
    false,  -- PUT
    false   -- DELETE
ON CONFLICT (role_id, available_route_id) DO NOTHING;
```

---

## API для управления доступами

### Администраторский API

**Base URL:** `/api/admin/route-access`

**Требуется роль:** `SUPERADMIN` или `SUPPORT_ADMINISTRATOR`

#### 1. Получить доступы для роли

```http
GET /api/admin/route-access/role/{roleCode}
```

**Ответ:**
```json
{
  "success": true,
  "result": [
    {
      "routeCode": "/api/support/tickets",
      "roleCode": "SUPPORT_DISPATCHER",
      "methodGet": true,
      "methodPost": true,
      "methodPut": true,
      "methodDelete": false
    },
    {
      "routeCode": "/api/support/tickets/filter",
      "roleCode": "SUPPORT_DISPATCHER",
      "methodGet": false,
      "methodPost": true,
      "methodPut": false,
      "methodDelete": false
    }
  ]
}
```

#### 2. Обновить кэш

```http
POST /api/admin/route-access/cache/refresh
```

**Ответ:**
```json
{
  "success": true,
  "message": "Cache refreshed successfully",
  "result": null
}
```

#### 3. Получить статистику кэша

```http
GET /api/admin/route-access/cache/stats
```

**Ответ:**
```json
{
  "success": true,
  "result": {
    "totalEntries": 529,
    "lastRefresh": "2025-11-19T15:15:17.149616",
    "activeAccessEntries": 487
  }
}
```

---

## Best Practices

### 1. Именование ролей

✅ **Правильно:**
```
SUPPORT_DISPATCHER    // Модуль + роль
SUPPORT_TECHNICIAN
SUPPORT_OPERATOR
QR_MANAGER            // Функциональное назначение
```

❌ **Неправильно:**
```
DISP                  // Слишком короткое
supportDispatcher     // camelCase вместо UPPER_SNAKE_CASE
Dispatcher-Role       // Использование дефисов
```

### 2. Регистрация маршрутов

✅ **Правильно:**
```sql
-- Указывайте метод в code, если endpoint специфичен для метода
INSERT INTO sys_available_routes (code, description_ru)
VALUES ('POST /api/director/analytics/general-kpi', 'Получить общие KPI');

-- Не указывайте метод, если endpoint поддерживает разные методы
INSERT INTO sys_available_routes (code, description_ru)
VALUES ('/api/support/tickets', 'Управление заявками');
```

❌ **Неправильно:**
```sql
-- Смешанный стиль
INSERT INTO sys_available_routes (code, description_ru)
VALUES ('POST /api/tickets', 'Заявки'),  -- Без префикса /api/support
       ('/tickets/filter', 'Фильтр');     -- Без /api
```

### 3. Назначение доступов

✅ **Правильно:**
```sql
-- Используйте подзапросы для получения ID
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, ...)
SELECT
    (SELECT id FROM sys_roles WHERE code = 'SUPPORT_DISPATCHER'),
    (SELECT id FROM sys_available_routes WHERE code = '/api/support/tickets'),
    true, true, true, false
ON CONFLICT (role_id, available_route_id) DO NOTHING;
```

❌ **Неправильно:**
```sql
-- Хардкод ID (может измениться между окружениями)
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, ...)
VALUES (3, 45, true, true, true, false);
```

### 4. Миграции

✅ **Правильно:**
- Используйте транзакции в DO блоках
- Добавляйте `ON CONFLICT DO NOTHING` для идемпотентности
- Используйте `RAISE NOTICE` для логирования прогресса
- Проверяйте существование зависимостей (`IF ... IS NULL THEN RAISE EXCEPTION`)

❌ **Неправильно:**
- Не проверять существование данных перед вставкой
- Не использовать идемпотентные операции
- Захардкодить ID вместо подзапросов

### 5. Кэширование

✅ **Правильно:**
```java
// Обновлять кэш после изменения прав доступа
routeAccessService.grantAccess(roleCode, routeCode, ...);
routeAccessService.refreshRoleCache(roleCode); // Кэш обновится автоматически

// Полное обновление после массовых изменений
bulkUpdateAccess();
routeAccessService.refreshCache();
```

❌ **Неправильно:**
```java
// Забывать обновлять кэш
routeAccessService.grantAccess(...);
// Кэш обновится только через 5 минут по расписанию!
```

### 6. Множественные роли

✅ **Правильно:**
```java
// Пользователь может иметь несколько ролей
INSERT INTO sys_user_roles (user_id, role_id, active)
VALUES
    (dispatcher1_id, dispatcher_role_id, true),
    (dispatcher1_id, qr_manager_role_id, true);

// Система проверит доступ для ОБЕИХ ролей
// Доступ будет предоставлен, если ЛЮБАЯ роль имеет разрешение
```

❌ **Неправильно:**
```java
// Пытаться реализовать "главную" роль
// Система уже поддерживает множественные роли из коробки
```

### 7. Проверка доступа в коде

✅ **Правильно:**
```java
// В контроллере - пусть интерцептор сделает всю работу
@PostMapping("/tickets")
public ResponseEntity<?> createTicket(@RequestBody TicketRequest request) {
    // RouteAccessInterceptor уже проверил доступ
    return ticketService.create(request);
}

// В сервисе - проверяйте дополнительные условия
public void deleteTicket(Long id) {
    if (!routeAccessService.hasAccess("/api/support/tickets", "DELETE")) {
        throw new AccessDeniedException("Delete not allowed");
    }
    // ...
}
```

❌ **Неправильно:**
```java
// Дублировать проверки в контроллере
@PostMapping("/tickets")
public ResponseEntity<?> createTicket(...) {
    // НЕ НУЖНО - интерцептор уже проверил!
    if (!routeAccessService.hasAccess(...)) {
        throw new AccessDeniedException();
    }
}
```

### 8. Гибридный режим (@PreAuthorize)

✅ **Правильно:**
```java
// Использовать @PreAuthorize для сложных проверок
@PreAuthorize("hasRole('SUPERADMIN') or (hasRole('DISPATCHER') and #ticketId == principal.assignedTicketId)")
public void updateTicket(Long ticketId, ...) {
    // RouteAccessInterceptor пропустит проверку, т.к. есть @PreAuthorize
}

// Использовать route access для простых проверок
@PostMapping("/tickets")
public ResponseEntity<?> createTicket(...) {
    // RouteAccessInterceptor проверит через систему маршрутов
}
```

❌ **Неправильно:**
```java
// Смешивать оба подхода на одном endpoint
@PreAuthorize("hasRole('DISPATCHER')")  // ← Удалить это
@PostMapping("/tickets")  // ← Route access достаточно
public ResponseEntity<?> createTicket(...) { }
```

---

## Заключение

Эта система обеспечивает:

✅ **Гибкость** - легко добавлять новые роли и маршруты через миграции
✅ **Производительность** - O(1) проверка доступа благодаря кэшу
✅ **Безопасность** - гранулярный контроль на уровне HTTP методов
✅ **Масштабируемость** - поддержка множественных ролей на пользователя
✅ **Простота** - автоматическая проверка через интерцептор
✅ **Совместимость** - гибридный режим со старыми `@PreAuthorize` аннотациями

Система легко переносится в другие проекты - достаточно скопировать:
1. Структуру таблиц (4 таблицы)
2. Java классы (6 файлов)
3. Настроить исключенные пути в `RouteAccessInterceptor`
4. Создать миграции с вашими ролями и маршрутами

---

## Дополнительные ресурсы

**Исходный код:**
- `RouteAccessInterceptor.java` - перехватчик запросов
- `RouteAccessService.java` - высокоуровневый сервис
- `RouteCacheService.java` - кэш-сервис
- `RouteAccessCacheScheduler.java` - планировщик обновлений
- `RouteAccessController.java` - API управления

**Миграции (примеры):**
- `V001__Initial_Schema.sql` - создание таблиц
- `V020__Add_QR_Manager_Role.sql` - добавление роли QR_MANAGER
- `V027__Add_Director_Analytics_Routes.sql` - добавление аналитики директора

**Конфигурация:**
```properties
# application.properties
app.route-access.enabled=true  # Включить/выключить систему
```

---

**Автор документации:** Claude Code
**Дата создания:** 2025-11-19
**Версия системы:** 1.0.0
