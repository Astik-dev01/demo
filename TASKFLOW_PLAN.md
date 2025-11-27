# 📋 TaskFlow — Система управления проектами

> **Полный план разработки fullstack приложения**
> 
> Версия: 1.0.0  
> Дата: 25.11.2025  
> Стек: Next.js 14 + Spring Boot 3.x + PostgreSQL + Redis

---

## 📑 Содержание

1. [Обзор проекта](#1-обзор-проекта)
2. [Архитектура системы](#2-архитектура-системы)
3. [Структура базы данных](#3-структура-базы-данных)
4. [Справочники (Handbooks)](#4-справочники-handbooks)
5. [Основные сущности](#5-основные-сущности)
6. [API Endpoints](#6-api-endpoints)
7. [Система ролей и доступа](#7-система-ролей-и-доступа)
8. [Структура Frontend](#8-структура-frontend)
9. [Состояние (Zustand Stores)](#9-состояние-zustand-stores)
10. [UI Компоненты](#10-ui-компоненты)
11. [Docker & Deployment](#11-docker--deployment)
12. [План реализации](#12-план-реализации)
13. [Технические требования](#13-технические-требования)

---

## 1. Обзор проекта

### 1.1 Описание

**TaskFlow** — современная система управления проектами с Kanban-досками, трекингом времени и командной работой. Ориентирована на малые и средние команды разработчиков.

### 1.2 Ключевые функции

| Функция | Описание |
|---------|----------|
| 📊 **Kanban-доски** | Визуальное управление задачами с drag-and-drop |
| 👥 **Командная работа** | Приглашения, роли в проекте, комментарии |
| ⏱️ **Трекинг времени** | Учёт затраченного времени на задачи |
| 📁 **Документы** | Прикрепление файлов к задачам и проектам |
| 📈 **Аналитика** | Отчёты по продуктивности, burndown charts |
| 🔔 **Уведомления** | Email, Telegram, in-app уведомления |
| 🏷️ **Теги и метки** | Гибкая категоризация задач |
| 📅 **Дедлайны** | Календарь, напоминания |

### 1.3 Целевая аудитория

- Стартапы и малые IT-команды
- Фрилансеры и агентства
- Образовательные проекты
- Open-source команды

---

## 2. Архитектура системы

### 2.1 Общая архитектура

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              КЛИЕНТ                                      │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    Next.js 14 (App Router)                       │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │   │
│  │  │  Pages   │  │Components│  │  Hooks   │  │  Zustand Stores  │ │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────────────┘ │   │
│  │  ┌──────────────────────────────────────────────────────────┐   │   │
│  │  │              React Hook Form + Zod Validation            │   │   │
│  │  └──────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS / REST API
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                              СЕРВЕР                                      │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    Spring Boot 3.x (Java 21)                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │   │
│  │  │Controller│  │ Service  │  │Repository│  │   Security       │ │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────────────┘ │   │
│  │  ┌──────────────────────────────────────────────────────────┐   │   │
│  │  │         JWT Auth + Route-based Access Control            │   │   │
│  │  └──────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
            ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
            │ PostgreSQL  │ │    Redis    │ │    MinIO    │
            │   (Data)    │ │   (Cache)   │ │   (Files)   │
            └─────────────┘ └─────────────┘ └─────────────┘
```

### 2.2 Структура Backend (Spring Boot)

```
backend/
├── src/main/java/kg/taskflow/
│   ├── TaskFlowApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── OpenApiConfig.java
│   │   ├── RedisConfig.java
│   │   ├── MinioConfig.java
│   │   └── WebMvcConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── ProjectController.java
│   │   ├── BoardController.java
│   │   ├── ColumnController.java
│   │   ├── TaskController.java
│   │   ├── CommentController.java
│   │   ├── TimeEntryController.java
│   │   ├── TeamController.java
│   │   ├── InvitationController.java
│   │   ├── NotificationController.java
│   │   ├── FileController.java
│   │   ├── AnalyticsController.java
│   │   └── hb/                          # Справочники
│   │       ├── TaskPriorityController.java
│   │       ├── TaskStatusController.java
│   │       ├── ProjectTypeController.java
│   │       ├── TagCategoryController.java
│   │       └── RoleInProjectController.java
│   ├── service/
│   │   ├── impl/
│   │   └── hb/
│   │       └── impl/
│   ├── db/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Project.java
│   │   │   ├── Board.java
│   │   │   ├── BoardColumn.java
│   │   │   ├── Task.java
│   │   │   ├── TaskComment.java
│   │   │   ├── TaskAttachment.java
│   │   │   ├── TimeEntry.java
│   │   │   ├── Team.java
│   │   │   ├── TeamMember.java
│   │   │   ├── Invitation.java
│   │   │   ├── Notification.java
│   │   │   ├── Tag.java
│   │   │   ├── TaskTag.java
│   │   │   └── hb/
│   │   │       ├── HBTaskPriority.java
│   │   │       ├── HBTaskStatus.java
│   │   │       ├── HBProjectType.java
│   │   │       ├── HBTagCategory.java
│   │   │       └── HBRoleInProject.java
│   │   ├── repository/
│   │   │   └── hb/
│   │   └── enums/
│   ├── dto/
│   │   ├── auth/
│   │   ├── user/
│   │   ├── project/
│   │   ├── board/
│   │   ├── task/
│   │   ├── comment/
│   │   ├── timeentry/
│   │   ├── team/
│   │   ├── notification/
│   │   ├── analytics/
│   │   └── hb/
│   ├── mapper/
│   │   └── hb/
│   ├── exception/
│   │   └── handler/
│   ├── security/
│   │   ├── jwt/
│   │   └── route/
│   └── util/
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   ├── db/migration/              # Flyway миграции
│   │   ├── V001__Initial_Schema.sql
│   │   ├── V002__Create_Users.sql
│   │   ├── V003__Create_Projects.sql
│   │   ├── V004__Create_Boards.sql
│   │   ├── V005__Create_Tasks.sql
│   │   ├── V006__Create_Handbooks.sql
│   │   ├── V007__Create_Teams.sql
│   │   ├── V008__Create_Time_Tracking.sql
│   │   ├── V009__Create_Notifications.sql
│   │   └── V010__Create_Route_Access.sql
│   └── messages/
│       ├── messages.properties
│       ├── messages_ru.properties
│       └── messages_ky.properties
└── build.gradle
```

### 2.3 Структура Frontend (Next.js 14)

```
frontend/
├── src/
│   ├── app/
│   │   ├── (auth)/
│   │   │   ├── login/
│   │   │   │   └── page.tsx
│   │   │   ├── register/
│   │   │   │   └── page.tsx
│   │   │   ├── forgot-password/
│   │   │   │   └── page.tsx
│   │   │   └── layout.tsx
│   │   ├── (dashboard)/
│   │   │   ├── layout.tsx
│   │   │   ├── page.tsx                    # Dashboard
│   │   │   ├── projects/
│   │   │   │   ├── page.tsx                # Список проектов
│   │   │   │   ├── new/
│   │   │   │   │   └── page.tsx            # Создание проекта
│   │   │   │   └── [projectId]/
│   │   │   │       ├── page.tsx            # Детали проекта
│   │   │   │       ├── settings/
│   │   │   │       │   └── page.tsx
│   │   │   │       └── boards/
│   │   │   │           └── [boardId]/
│   │   │   │               └── page.tsx    # Kanban доска
│   │   │   ├── tasks/
│   │   │   │   ├── page.tsx                # Мои задачи
│   │   │   │   └── [taskId]/
│   │   │   │       └── page.tsx            # Детали задачи
│   │   │   ├── teams/
│   │   │   │   ├── page.tsx
│   │   │   │   └── [teamId]/
│   │   │   │       └── page.tsx
│   │   │   ├── calendar/
│   │   │   │   └── page.tsx
│   │   │   ├── time-tracking/
│   │   │   │   └── page.tsx
│   │   │   ├── analytics/
│   │   │   │   └── page.tsx
│   │   │   ├── notifications/
│   │   │   │   └── page.tsx
│   │   │   ├── settings/
│   │   │   │   ├── page.tsx
│   │   │   │   ├── profile/
│   │   │   │   │   └── page.tsx
│   │   │   │   └── preferences/
│   │   │   │       └── page.tsx
│   │   │   └── admin/                      # Только для ADMIN
│   │   │       ├── layout.tsx
│   │   │       ├── users/
│   │   │       │   └── page.tsx
│   │   │       ├── handbooks/
│   │   │       │   ├── page.tsx
│   │   │       │   ├── priorities/
│   │   │       │   │   └── page.tsx
│   │   │       │   ├── statuses/
│   │   │       │   │   └── page.tsx
│   │   │       │   ├── project-types/
│   │   │       │   │   └── page.tsx
│   │   │       │   └── tag-categories/
│   │   │       │       └── page.tsx
│   │   │       └── system/
│   │   │           └── page.tsx
│   │   ├── api/                            # API Routes (если нужно)
│   │   ├── layout.tsx
│   │   ├── loading.tsx
│   │   ├── error.tsx
│   │   ├── not-found.tsx
│   │   └── globals.css
│   ├── components/
│   │   ├── ui/                             # Базовые UI компоненты
│   │   │   ├── button.tsx
│   │   │   ├── input.tsx
│   │   │   ├── select.tsx
│   │   │   ├── modal.tsx
│   │   │   ├── dropdown.tsx
│   │   │   ├── avatar.tsx
│   │   │   ├── badge.tsx
│   │   │   ├── card.tsx
│   │   │   ├── table.tsx
│   │   │   ├── pagination.tsx
│   │   │   ├── skeleton.tsx
│   │   │   ├── toast.tsx
│   │   │   ├── tooltip.tsx
│   │   │   ├── tabs.tsx
│   │   │   ├── calendar.tsx
│   │   │   └── date-picker.tsx
│   │   ├── layout/
│   │   │   ├── sidebar.tsx
│   │   │   ├── header.tsx
│   │   │   ├── breadcrumb.tsx
│   │   │   └── footer.tsx
│   │   ├── auth/
│   │   │   ├── login-form.tsx
│   │   │   ├── register-form.tsx
│   │   │   └── auth-guard.tsx
│   │   ├── projects/
│   │   │   ├── project-card.tsx
│   │   │   ├── project-list.tsx
│   │   │   ├── project-form.tsx
│   │   │   └── project-members.tsx
│   │   ├── boards/
│   │   │   ├── kanban-board.tsx
│   │   │   ├── board-column.tsx
│   │   │   ├── column-header.tsx
│   │   │   └── add-column-button.tsx
│   │   ├── tasks/
│   │   │   ├── task-card.tsx
│   │   │   ├── task-detail.tsx
│   │   │   ├── task-form.tsx
│   │   │   ├── task-comments.tsx
│   │   │   ├── task-attachments.tsx
│   │   │   ├── task-time-entries.tsx
│   │   │   ├── task-checklist.tsx
│   │   │   └── task-filters.tsx
│   │   ├── teams/
│   │   │   ├── team-card.tsx
│   │   │   ├── team-members.tsx
│   │   │   └── invitation-form.tsx
│   │   ├── time-tracking/
│   │   │   ├── timer-widget.tsx
│   │   │   ├── time-entry-list.tsx
│   │   │   └── time-report.tsx
│   │   ├── analytics/
│   │   │   ├── burndown-chart.tsx
│   │   │   ├── velocity-chart.tsx
│   │   │   ├── team-workload.tsx
│   │   │   └── task-distribution.tsx
│   │   ├── notifications/
│   │   │   ├── notification-bell.tsx
│   │   │   ├── notification-list.tsx
│   │   │   └── notification-item.tsx
│   │   ├── admin/
│   │   │   ├── handbook-table.tsx
│   │   │   ├── handbook-form.tsx
│   │   │   └── user-management.tsx
│   │   └── shared/
│   │       ├── empty-state.tsx
│   │       ├── loading-spinner.tsx
│   │       ├── error-boundary.tsx
│   │       ├── confirm-dialog.tsx
│   │       └── search-input.tsx
│   ├── hooks/
│   │   ├── use-auth.ts
│   │   ├── use-projects.ts
│   │   ├── use-boards.ts
│   │   ├── use-tasks.ts
│   │   ├── use-teams.ts
│   │   ├── use-notifications.ts
│   │   ├── use-timer.ts
│   │   ├── use-debounce.ts
│   │   ├── use-local-storage.ts
│   │   └── use-media-query.ts
│   ├── stores/                             # Zustand stores
│   │   ├── auth-store.ts
│   │   ├── project-store.ts
│   │   ├── board-store.ts
│   │   ├── task-store.ts
│   │   ├── team-store.ts
│   │   ├── notification-store.ts
│   │   ├── timer-store.ts
│   │   ├── ui-store.ts
│   │   └── handbook-store.ts
│   ├── services/                           # API services
│   │   ├── api.ts                          # Axios instance
│   │   ├── auth.service.ts
│   │   ├── user.service.ts
│   │   ├── project.service.ts
│   │   ├── board.service.ts
│   │   ├── task.service.ts
│   │   ├── comment.service.ts
│   │   ├── time-entry.service.ts
│   │   ├── team.service.ts
│   │   ├── notification.service.ts
│   │   ├── file.service.ts
│   │   ├── analytics.service.ts
│   │   └── handbook.service.ts
│   ├── lib/
│   │   ├── utils.ts
│   │   ├── constants.ts
│   │   ├── validators.ts
│   │   └── date-utils.ts
│   ├── types/
│   │   ├── auth.types.ts
│   │   ├── user.types.ts
│   │   ├── project.types.ts
│   │   ├── board.types.ts
│   │   ├── task.types.ts
│   │   ├── team.types.ts
│   │   ├── notification.types.ts
│   │   ├── analytics.types.ts
│   │   ├── handbook.types.ts
│   │   └── api.types.ts
│   └── middleware.ts
├── public/
│   ├── icons/
│   ├── images/
│   └── favicon.ico
├── tailwind.config.ts
├── next.config.js
├── tsconfig.json
├── package.json
└── Dockerfile
```

---

## 3. Структура базы данных

### 3.1 ER-диаграмма (упрощённая)

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     users       │       │    projects     │       │     boards      │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id              │───┐   │ id              │───┐   │ id              │
│ email           │   │   │ name            │   │   │ name            │
│ password_hash   │   │   │ description     │   │   │ project_id (FK) │──┐
│ first_name      │   │   │ owner_id (FK)   │◄──┘   │ position        │  │
│ last_name       │   │   │ type_id (FK)    │       │ is_default      │  │
│ avatar_url      │   │   │ status          │       └─────────────────┘  │
│ ...             │   │   │ ...             │                            │
└─────────────────┘   │   └─────────────────┘                            │
        │             │           │                                       │
        │             │           │                                       │
        ▼             │           ▼                                       │
┌─────────────────┐   │   ┌─────────────────┐       ┌─────────────────┐  │
│  team_members   │   │   │ project_members │       │  board_columns  │◄─┘
├─────────────────┤   │   ├─────────────────┤       ├─────────────────┤
│ id              │   │   │ id              │       │ id              │
│ team_id (FK)    │   │   │ project_id (FK) │       │ board_id (FK)   │───┐
│ user_id (FK)    │◄──┘   │ user_id (FK)    │       │ name            │   │
│ role_id (FK)    │       │ role_id (FK)    │       │ position        │   │
│ ...             │       │ ...             │       │ color           │   │
└─────────────────┘       └─────────────────┘       │ wip_limit       │   │
                                                    └─────────────────┘   │
                                                                          │
                                  ┌───────────────────────────────────────┘
                                  ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     tasks       │       │  task_comments  │       │  time_entries   │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id              │───┬──▶│ id              │       │ id              │
│ title           │   │   │ task_id (FK)    │       │ task_id (FK)    │◄─┐
│ description     │   │   │ user_id (FK)    │       │ user_id (FK)    │  │
│ column_id (FK)  │   │   │ content         │       │ started_at      │  │
│ assignee_id(FK) │   │   │ ...             │       │ ended_at        │  │
│ priority_id(FK) │   │   └─────────────────┘       │ duration        │  │
│ position        │   │                             │ description     │  │
│ due_date        │   │   ┌─────────────────┐       └─────────────────┘  │
│ estimated_hours │   │   │task_attachments │                            │
│ ...             │   │   ├─────────────────┤                            │
└─────────────────┘   └──▶│ id              │                            │
        │                 │ task_id (FK)    │                            │
        │                 │ file_name       │                            │
        │                 │ file_url        │                            │
        │                 │ ...             │                            │
        │                 └─────────────────┘                            │
        │                                                                │
        │                 ┌─────────────────┐                            │
        │                 │   task_tags     │                            │
        └────────────────▶├─────────────────┤                            │
                          │ task_id (FK)    │                            │
                          │ tag_id (FK)     │                            │
                          └─────────────────┘                            │
                                  │                                      │
                                  ▼                                      │
                          ┌─────────────────┐                            │
                          │      tags       │                            │
                          ├─────────────────┤                            │
                          │ id              │────────────────────────────┘
                          │ name            │
                          │ color           │
                          │ category_id(FK) │
                          │ project_id (FK) │
                          └─────────────────┘
```

### 3.2 Полный список таблиц

#### Системные таблицы

| Таблица | Описание |
|---------|----------|
| `sys_users` | Пользователи системы |
| `sys_roles` | Системные роли |
| `sys_user_roles` | Связь пользователей и ролей |
| `sys_available_routes` | Доступные маршруты API |
| `sys_role_linked_available_routes` | Связь ролей и маршрутов |
| `sys_logs_request` | Логи запросов |
| `sys_notifications` | Уведомления |
| `sys_refresh_tokens` | Refresh токены JWT |

#### Справочники (Handbooks)

| Таблица | Описание |
|---------|----------|
| `hb_task_priority` | Приоритеты задач |
| `hb_task_status` | Статусы задач |
| `hb_project_type` | Типы проектов |
| `hb_tag_category` | Категории тегов |
| `hb_role_in_project` | Роли в проекте |

#### Бизнес-таблицы

| Таблица | Описание |
|---------|----------|
| `projects` | Проекты |
| `project_members` | Участники проектов |
| `boards` | Канбан-доски |
| `board_columns` | Колонки досок |
| `tasks` | Задачи |
| `task_comments` | Комментарии к задачам |
| `task_attachments` | Вложения к задачам |
| `task_checklists` | Чек-листы задач |
| `task_checklist_items` | Элементы чек-листов |
| `task_tags` | Связь задач и тегов |
| `tags` | Теги |
| `time_entries` | Записи трекинга времени |
| `teams` | Команды |
| `team_members` | Участники команд |
| `invitations` | Приглашения |

---

## 4. Справочники (Handbooks)

### 4.1 HBTaskPriority — Приоритеты задач

```sql
CREATE TABLE hb_task_priority (
    id              BIGSERIAL PRIMARY KEY,
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    color           VARCHAR(7) NOT NULL DEFAULT '#6B7280',  -- HEX цвет
    icon            VARCHAR(50),                             -- Иконка (lucide)
    level           INTEGER NOT NULL DEFAULT 0,              -- Числовой уровень
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Начальные данные
INSERT INTO hb_task_priority (alias, name_ru, name_ky, name_en, color, icon, level) VALUES
    ('CRITICAL', 'Критический', 'Критикалык', 'Critical', '#DC2626', 'alert-circle', 4),
    ('HIGH', 'Высокий', 'Жогорку', 'High', '#F97316', 'arrow-up', 3),
    ('MEDIUM', 'Средний', 'Орточо', 'Medium', '#EAB308', 'minus', 2),
    ('LOW', 'Низкий', 'Төмөн', 'Low', '#22C55E', 'arrow-down', 1),
    ('NONE', 'Без приоритета', 'Приоритетсиз', 'No Priority', '#6B7280', 'circle', 0);
```

### 4.2 HBTaskStatus — Статусы задач

```sql
CREATE TABLE hb_task_status (
    id              BIGSERIAL PRIMARY KEY,
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    color           VARCHAR(7) NOT NULL DEFAULT '#6B7280',
    icon            VARCHAR(50),
    is_final        BOOLEAN NOT NULL DEFAULT FALSE,   -- Завершающий статус
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,   -- Статус по умолчанию
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Начальные данные
INSERT INTO hb_task_status (alias, name_ru, name_ky, name_en, color, icon, is_final, is_default) VALUES
    ('BACKLOG', 'Бэклог', 'Бэклог', 'Backlog', '#6B7280', 'inbox', false, true),
    ('TODO', 'К выполнению', 'Аткарууга', 'To Do', '#3B82F6', 'circle', false, false),
    ('IN_PROGRESS', 'В работе', 'Иште', 'In Progress', '#F59E0B', 'loader', false, false),
    ('IN_REVIEW', 'На проверке', 'Текшерүүдө', 'In Review', '#8B5CF6', 'eye', false, false),
    ('DONE', 'Выполнено', 'Аткарылды', 'Done', '#22C55E', 'check-circle', true, false),
    ('CANCELLED', 'Отменено', 'Жокко чыгарылды', 'Cancelled', '#EF4444', 'x-circle', true, false);
```

### 4.3 HBProjectType — Типы проектов

```sql
CREATE TABLE hb_project_type (
    id              BIGSERIAL PRIMARY KEY,
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    icon            VARCHAR(50),
    description_ru  TEXT,
    description_ky  TEXT,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Начальные данные
INSERT INTO hb_project_type (alias, name_ru, name_ky, name_en, icon) VALUES
    ('SOFTWARE', 'Разработка ПО', 'ПО иштеп чыгуу', 'Software Development', 'code'),
    ('MARKETING', 'Маркетинг', 'Маркетинг', 'Marketing', 'megaphone'),
    ('DESIGN', 'Дизайн', 'Дизайн', 'Design', 'palette'),
    ('RESEARCH', 'Исследование', 'Изилдөө', 'Research', 'search'),
    ('EDUCATION', 'Образование', 'Билим берүү', 'Education', 'graduation-cap'),
    ('OTHER', 'Другое', 'Башка', 'Other', 'folder');
```

### 4.4 HBTagCategory — Категории тегов

```sql
CREATE TABLE hb_tag_category (
    id              BIGSERIAL PRIMARY KEY,
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    color           VARCHAR(7) NOT NULL DEFAULT '#6B7280',
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Начальные данные
INSERT INTO hb_tag_category (alias, name_ru, name_ky, name_en, color) VALUES
    ('FEATURE', 'Функционал', 'Функционал', 'Feature', '#3B82F6'),
    ('BUG', 'Баг', 'Баг', 'Bug', '#EF4444'),
    ('IMPROVEMENT', 'Улучшение', 'Жакшыртуу', 'Improvement', '#8B5CF6'),
    ('DOCUMENTATION', 'Документация', 'Документация', 'Documentation', '#F59E0B'),
    ('TESTING', 'Тестирование', 'Тестирлөө', 'Testing', '#10B981'),
    ('CUSTOM', 'Пользовательский', 'Колдонуучунун', 'Custom', '#6B7280');
```

### 4.5 HBRoleInProject — Роли в проекте

```sql
CREATE TABLE hb_role_in_project (
    id              BIGSERIAL PRIMARY KEY,
    alias           VARCHAR(50) NOT NULL UNIQUE,
    name_ru         VARCHAR(100) NOT NULL,
    name_ky         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    description_ru  TEXT,
    permissions     JSONB DEFAULT '[]',      -- Массив разрешений
    level           INTEGER NOT NULL DEFAULT 0,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- Начальные данные
INSERT INTO hb_role_in_project (alias, name_ru, name_ky, name_en, level, permissions) VALUES
    ('OWNER', 'Владелец', 'Ээси', 'Owner', 100, 
        '["all"]'),
    ('ADMIN', 'Администратор', 'Администратор', 'Admin', 80, 
        '["manage_members", "manage_boards", "manage_tasks", "delete_tasks", "manage_settings"]'),
    ('MEMBER', 'Участник', 'Катышуучу', 'Member', 50, 
        '["create_tasks", "edit_own_tasks", "comment", "track_time"]'),
    ('VIEWER', 'Наблюдатель', 'Байкоочу', 'Viewer', 10, 
        '["view_tasks", "comment"]');
```

---

## 5. Основные сущности

### 5.1 Users (Пользователи)

```sql
CREATE TABLE sys_users (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100),
    avatar_url          VARCHAR(500),
    phone               VARCHAR(20),
    timezone            VARCHAR(50) DEFAULT 'Asia/Bishkek',
    language            VARCHAR(5) DEFAULT 'ru',
    email_verified      BOOLEAN DEFAULT FALSE,
    email_verified_at   TIMESTAMP,
    last_login_at       TIMESTAMP,
    is_active           BOOLEAN DEFAULT TRUE,
    deleted             BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          BIGINT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_at          TIMESTAMP,
    updated_by          BIGINT
);

CREATE INDEX idx_users_email ON sys_users(email);
CREATE INDEX idx_users_deleted ON sys_users(deleted);
```

### 5.2 Projects (Проекты)

```sql
CREATE TABLE projects (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    key                 VARCHAR(10) NOT NULL UNIQUE,    -- Короткий ключ (TF, PRJ)
    description         TEXT,
    owner_id            BIGINT NOT NULL REFERENCES sys_users(id),
    type_id             BIGINT REFERENCES hb_project_type(id),
    color               VARCHAR(7) DEFAULT '#3B82F6',
    icon                VARCHAR(50),
    is_public           BOOLEAN DEFAULT FALSE,
    is_archived         BOOLEAN DEFAULT FALSE,
    archived_at         TIMESTAMP,
    settings            JSONB DEFAULT '{}',
    deleted             BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          BIGINT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_at          TIMESTAMP,
    updated_by          BIGINT
);

CREATE INDEX idx_projects_owner ON projects(owner_id);
CREATE INDEX idx_projects_key ON projects(key);
CREATE INDEX idx_projects_deleted ON projects(deleted);
```

### 5.3 Project Members (Участники проекта)

```sql
CREATE TABLE project_members (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES projects(id),
    user_id             BIGINT NOT NULL REFERENCES sys_users(id),
    role_id             BIGINT NOT NULL REFERENCES hb_role_in_project(id),
    joined_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    invited_by          BIGINT REFERENCES sys_users(id),
    is_active           BOOLEAN DEFAULT TRUE,
    deleted             BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(project_id, user_id)
);

CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);
```

### 5.4 Boards (Доски)

```sql
CREATE TABLE boards (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES projects(id),
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    position            INTEGER NOT NULL DEFAULT 0,
    is_default          BOOLEAN DEFAULT FALSE,
    settings            JSONB DEFAULT '{}',
    deleted             BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_at          TIMESTAMP,
    updated_by          BIGINT
);

CREATE INDEX idx_boards_project ON boards(project_id);
```

### 5.5 Board Columns (Колонки доски)

```sql
CREATE TABLE board_columns (
    id                  BIGSERIAL PRIMARY KEY,
    board_id            BIGINT NOT NULL REFERENCES boards(id),
    name                VARCHAR(100) NOT NULL,
    color               VARCHAR(7) DEFAULT '#E5E7EB',
    position            INTEGER NOT NULL DEFAULT 0,
    wip_limit           INTEGER,                          -- Work In Progress лимит
    status_id           BIGINT REFERENCES hb_task_status(id),
    deleted             BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE INDEX idx_board_columns_board ON board_columns(board_id);
```

### 5.6 Tasks (Задачи)

```sql
CREATE TABLE tasks (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES projects(id),
    board_id            BIGINT NOT NULL REFERENCES boards(id),
    column_id           BIGINT NOT NULL REFERENCES board_columns(id),
    parent_task_id      BIGINT REFERENCES tasks(id),       -- Для подзадач
    
    -- Идентификация
    number              INTEGER NOT NULL,                   -- Номер в проекте (TF-1, TF-2)
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    
    -- Исполнители
    reporter_id         BIGINT NOT NULL REFERENCES sys_users(id),
    assignee_id         BIGINT REFERENCES sys_users(id),
    
    -- Классификация
    priority_id         BIGINT REFERENCES hb_task_priority(id),
    status_id           BIGINT REFERENCES hb_task_status(id),
    
    -- Позиция на доске
    position            INTEGER NOT NULL DEFAULT 0,
    
    -- Даты
    due_date            DATE,
    start_date          DATE,
    completed_at        TIMESTAMP,
    
    -- Оценка времени
    estimated_hours     DECIMAL(10,2),
    spent_hours         DECIMAL(10,2) DEFAULT 0,
    
    -- Дополнительно
    is_archived         BOOLEAN DEFAULT FALSE,
    archived_at         TIMESTAMP,
    
    -- Soft delete
    deleted             BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          BIGINT,
    
    -- Audit
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_at          TIMESTAMP,
    updated_by          BIGINT,
    
    UNIQUE(project_id, number)
);

CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_board ON tasks(board_id);
CREATE INDEX idx_tasks_column ON tasks(column_id);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_parent ON tasks(parent_task_id);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_deleted ON tasks(deleted);
```

### 5.7 Task Comments (Комментарии)

```sql
CREATE TABLE task_comments (
    id                  BIGSERIAL PRIMARY KEY,
    task_id             BIGINT NOT NULL REFERENCES tasks(id),
    user_id             BIGINT NOT NULL REFERENCES sys_users(id),
    parent_comment_id   BIGINT REFERENCES task_comments(id),  -- Для ответов
    content             TEXT NOT NULL,
    is_edited           BOOLEAN DEFAULT FALSE,
    edited_at           TIMESTAMP,
    deleted             BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE INDEX idx_task_comments_task ON task_comments(task_id);
CREATE INDEX idx_task_comments_user ON task_comments(user_id);
```

### 5.8 Time Entries (Трекинг времени)

```sql
CREATE TABLE time_entries (
    id                  BIGSERIAL PRIMARY KEY,
    task_id             BIGINT NOT NULL REFERENCES tasks(id),
    user_id             BIGINT NOT NULL REFERENCES sys_users(id),
    description         TEXT,
    started_at          TIMESTAMP NOT NULL,
    ended_at            TIMESTAMP,
    duration_minutes    INTEGER,                           -- Длительность в минутах
    is_billable         BOOLEAN DEFAULT TRUE,
    is_running          BOOLEAN DEFAULT FALSE,             -- Таймер запущен
    deleted             BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE INDEX idx_time_entries_task ON time_entries(task_id);
CREATE INDEX idx_time_entries_user ON time_entries(user_id);
CREATE INDEX idx_time_entries_started ON time_entries(started_at);
```

### 5.9 Tags (Теги)

```sql
CREATE TABLE tags (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES projects(id),
    category_id         BIGINT REFERENCES hb_tag_category(id),
    name                VARCHAR(50) NOT NULL,
    color               VARCHAR(7) DEFAULT '#6B7280',
    deleted             BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(project_id, name)
);

CREATE TABLE task_tags (
    task_id             BIGINT NOT NULL REFERENCES tasks(id),
    tag_id              BIGINT NOT NULL REFERENCES tags(id),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY(task_id, tag_id)
);
```

### 5.10 Task Attachments (Вложения)

```sql
CREATE TABLE task_attachments (
    id                  BIGSERIAL PRIMARY KEY,
    task_id             BIGINT NOT NULL REFERENCES tasks(id),
    uploaded_by         BIGINT NOT NULL REFERENCES sys_users(id),
    file_name           VARCHAR(255) NOT NULL,
    file_path           VARCHAR(500) NOT NULL,
    file_size           BIGINT NOT NULL,                   -- Размер в байтах
    mime_type           VARCHAR(100),
    deleted             BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_attachments_task ON task_attachments(task_id);
```

### 5.11 Teams (Команды)

```sql
CREATE TABLE teams (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    owner_id            BIGINT NOT NULL REFERENCES sys_users(id),
    avatar_url          VARCHAR(500),
    is_public           BOOLEAN DEFAULT FALSE,
    deleted             BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE TABLE team_members (
    id                  BIGSERIAL PRIMARY KEY,
    team_id             BIGINT NOT NULL REFERENCES teams(id),
    user_id             BIGINT NOT NULL REFERENCES sys_users(id),
    role                VARCHAR(50) DEFAULT 'MEMBER',       -- OWNER, ADMIN, MEMBER
    joined_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(team_id, user_id)
);
```

### 5.12 Invitations (Приглашения)

```sql
CREATE TABLE invitations (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) NOT NULL,
    token               VARCHAR(100) NOT NULL UNIQUE,
    type                VARCHAR(20) NOT NULL,               -- PROJECT, TEAM
    target_id           BIGINT NOT NULL,                    -- project_id или team_id
    role_id             BIGINT,                             -- Роль для приглашения
    invited_by          BIGINT NOT NULL REFERENCES sys_users(id),
    expires_at          TIMESTAMP NOT NULL,
    accepted_at         TIMESTAMP,
    accepted_by         BIGINT REFERENCES sys_users(id),
    status              VARCHAR(20) DEFAULT 'PENDING',      -- PENDING, ACCEPTED, EXPIRED, CANCELLED
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invitations_token ON invitations(token);
CREATE INDEX idx_invitations_email ON invitations(email);
```

### 5.13 Notifications (Уведомления)

```sql
CREATE TABLE sys_notifications (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES sys_users(id),
    type                VARCHAR(50) NOT NULL,               -- TASK_ASSIGNED, COMMENT_ADDED, etc.
    title               VARCHAR(200) NOT NULL,
    message             TEXT,
    data                JSONB DEFAULT '{}',                 -- Дополнительные данные
    is_read             BOOLEAN DEFAULT FALSE,
    read_at             TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON sys_notifications(user_id);
CREATE INDEX idx_notifications_read ON sys_notifications(user_id, is_read);
```

---

## 6. API Endpoints

### 6.1 Authentication (Аутентификация)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/auth/register` | Регистрация | Public |
| POST | `/api/auth/login` | Вход | Public |
| POST | `/api/auth/logout` | Выход | Auth |
| POST | `/api/auth/refresh` | Обновление токена | Auth |
| POST | `/api/auth/forgot-password` | Восстановление пароля | Public |
| POST | `/api/auth/reset-password` | Сброс пароля | Public |
| GET | `/api/auth/me` | Текущий пользователь | Auth |

### 6.2 Users (Пользователи)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| GET | `/api/users/{id}` | Получить пользователя | Auth |
| PUT | `/api/users/{id}` | Обновить профиль | Owner/Admin |
| PUT | `/api/users/{id}/avatar` | Загрузить аватар | Owner |
| PUT | `/api/users/{id}/password` | Сменить пароль | Owner |
| POST | `/api/users/filter` | Поиск пользователей | Auth |
| DELETE | `/api/users/{id}` | Удалить пользователя | Admin |

### 6.3 Projects (Проекты)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/projects` | Создать проект | Auth |
| PUT | `/api/projects/{id}` | Обновить проект | Owner/Admin |
| GET | `/api/projects/{id}` | Получить проект | Member |
| DELETE | `/api/projects/{id}` | Удалить проект | Owner |
| POST | `/api/projects/filter` | Список проектов | Auth |
| PUT | `/api/projects/{id}/archive` | Архивировать | Owner/Admin |

### 6.4 Project Members (Участники проекта)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| GET | `/api/projects/{id}/members` | Список участников | Member |
| POST | `/api/projects/{id}/members` | Добавить участника | Admin |
| PUT | `/api/projects/{id}/members/{userId}` | Изменить роль | Admin |
| DELETE | `/api/projects/{id}/members/{userId}` | Удалить участника | Admin |
| POST | `/api/projects/{id}/invite` | Отправить приглашение | Admin |

### 6.5 Boards (Доски)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/projects/{projectId}/boards` | Создать доску | Admin |
| PUT | `/api/boards/{id}` | Обновить доску | Admin |
| GET | `/api/boards/{id}` | Получить доску | Member |
| DELETE | `/api/boards/{id}` | Удалить доску | Admin |
| POST | `/api/projects/{projectId}/boards/filter` | Список досок | Member |

### 6.6 Board Columns (Колонки)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/boards/{boardId}/columns` | Создать колонку | Admin |
| PUT | `/api/columns/{id}` | Обновить колонку | Admin |
| DELETE | `/api/columns/{id}` | Удалить колонку | Admin |
| PUT | `/api/boards/{boardId}/columns/reorder` | Изменить порядок | Admin |

### 6.7 Tasks (Задачи)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/tasks` | Создать задачу | Member |
| PUT | `/api/tasks/{id}` | Обновить задачу | Member |
| GET | `/api/tasks/{id}` | Получить задачу | Member |
| DELETE | `/api/tasks/{id}` | Удалить задачу | Member/Admin |
| POST | `/api/tasks/filter` | Фильтрация задач | Member |
| PUT | `/api/tasks/{id}/move` | Переместить задачу | Member |
| PUT | `/api/tasks/{id}/assign` | Назначить исполнителя | Member |
| GET | `/api/tasks/my` | Мои задачи | Auth |

### 6.8 Task Comments (Комментарии)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/tasks/{taskId}/comments` | Добавить комментарий | Member |
| PUT | `/api/comments/{id}` | Редактировать | Owner |
| DELETE | `/api/comments/{id}` | Удалить | Owner/Admin |
| GET | `/api/tasks/{taskId}/comments` | Список комментариев | Member |

### 6.9 Task Attachments (Вложения)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/tasks/{taskId}/attachments` | Загрузить файл | Member |
| GET | `/api/attachments/{id}/download` | Скачать файл | Member |
| DELETE | `/api/attachments/{id}` | Удалить файл | Owner/Admin |

### 6.10 Time Entries (Трекинг времени)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/time-entries` | Создать запись | Auth |
| PUT | `/api/time-entries/{id}` | Обновить | Owner |
| DELETE | `/api/time-entries/{id}` | Удалить | Owner |
| POST | `/api/time-entries/filter` | Фильтрация | Auth |
| POST | `/api/time-entries/start` | Запустить таймер | Auth |
| POST | `/api/time-entries/stop` | Остановить таймер | Auth |
| GET | `/api/time-entries/running` | Текущий таймер | Auth |

### 6.11 Tags (Теги)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/projects/{projectId}/tags` | Создать тег | Admin |
| PUT | `/api/tags/{id}` | Обновить | Admin |
| DELETE | `/api/tags/{id}` | Удалить | Admin |
| GET | `/api/projects/{projectId}/tags` | Список тегов | Member |
| POST | `/api/tasks/{taskId}/tags` | Добавить тег к задаче | Member |
| DELETE | `/api/tasks/{taskId}/tags/{tagId}` | Убрать тег | Member |

### 6.12 Teams (Команды)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/teams` | Создать команду | Auth |
| PUT | `/api/teams/{id}` | Обновить | Owner/Admin |
| GET | `/api/teams/{id}` | Получить | Member |
| DELETE | `/api/teams/{id}` | Удалить | Owner |
| POST | `/api/teams/filter` | Список команд | Auth |
| POST | `/api/teams/{id}/members` | Добавить участника | Admin |
| DELETE | `/api/teams/{id}/members/{userId}` | Удалить участника | Admin |

### 6.13 Invitations (Приглашения)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| GET | `/api/invitations/{token}` | Получить приглашение | Public |
| POST | `/api/invitations/{token}/accept` | Принять | Auth |
| POST | `/api/invitations/{token}/decline` | Отклонить | Auth |
| DELETE | `/api/invitations/{id}` | Отменить | Admin |

### 6.14 Notifications (Уведомления)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| GET | `/api/notifications` | Список уведомлений | Auth |
| PUT | `/api/notifications/{id}/read` | Отметить прочитанным | Owner |
| PUT | `/api/notifications/read-all` | Прочитать все | Auth |
| GET | `/api/notifications/unread-count` | Количество непрочитанных | Auth |

### 6.15 Analytics (Аналитика)

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/analytics/project/{id}/overview` | Обзор проекта | Member |
| POST | `/api/analytics/project/{id}/burndown` | Burndown chart | Member |
| POST | `/api/analytics/project/{id}/velocity` | Velocity chart | Member |
| POST | `/api/analytics/project/{id}/workload` | Нагрузка команды | Admin |
| POST | `/api/analytics/user/productivity` | Личная продуктивность | Auth |
| POST | `/api/analytics/time-report` | Отчёт по времени | Auth |

### 6.16 Handbooks (Справочники)

Все справочники следуют единому паттерну:

| Метод | Endpoint | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/hb/{entity}` | Создать | Admin |
| PUT | `/api/hb/{entity}` | Обновить | Admin |
| DELETE | `/api/hb/{entity}/{id}` | Удалить | Admin |
| GET | `/api/hb/{entity}/{id}` | Получить по ID | Auth |
| POST | `/api/hb/{entity}/filter` | Фильтрация | Auth |

**Справочники:**
- `/api/hb/task-priorities`
- `/api/hb/task-statuses`
- `/api/hb/project-types`
- `/api/hb/tag-categories`
- `/api/hb/roles-in-project`

---

## 7. Система ролей и доступа

### 7.1 Системные роли

| Код | Название | Описание | Приоритет |
|-----|----------|----------|-----------|
| `SUPERADMIN` | Супер-администратор | Полный доступ ко всей системе | 1 |
| `ADMIN` | Администратор | Управление пользователями и справочниками | 10 |
| `USER` | Пользователь | Стандартный доступ | 50 |

### 7.2 Роли в проекте

| Код | Название | Права |
|-----|----------|-------|
| `OWNER` | Владелец | Полный контроль над проектом |
| `ADMIN` | Администратор | Управление участниками, досками, настройками |
| `MEMBER` | Участник | Создание/редактирование задач, комментарии |
| `VIEWER` | Наблюдатель | Только просмотр, комментарии |

### 7.3 Матрица доступа к маршрутам

```sql
-- Пример настройки доступа для USER
INSERT INTO sys_role_linked_available_routes (role_id, available_route_id, method_get, method_post, method_put, method_delete)
SELECT 
    (SELECT id FROM sys_roles WHERE code = 'USER'),
    r.id,
    true,   -- GET
    true,   -- POST  
    true,   -- PUT
    false   -- DELETE (только для ADMIN)
FROM sys_available_routes r
WHERE r.code IN (
    '/api/projects',
    '/api/projects/filter',
    '/api/tasks',
    '/api/tasks/filter',
    '/api/time-entries',
    '/api/notifications'
);
```

---

## 8. Структура Frontend

### 8.1 Роутинг (App Router)

```
/                           → Redirect to /login or /dashboard
├── (auth)/
│   ├── /login              → Страница входа
│   ├── /register           → Страница регистрации
│   └── /forgot-password    → Восстановление пароля
│
├── (dashboard)/
│   ├── /                   → Dashboard (обзор)
│   │
│   ├── /projects
│   │   ├── /               → Список проектов
│   │   ├── /new            → Создание проекта
│   │   └── /[projectId]
│   │       ├── /           → Детали проекта
│   │       ├── /settings   → Настройки проекта
│   │       └── /boards/[boardId]  → Kanban доска
│   │
│   ├── /tasks
│   │   ├── /               → Мои задачи
│   │   └── /[taskId]       → Детали задачи
│   │
│   ├── /teams
│   │   ├── /               → Мои команды
│   │   └── /[teamId]       → Детали команды
│   │
│   ├── /calendar           → Календарь задач
│   │
│   ├── /time-tracking      → Трекинг времени
│   │
│   ├── /analytics          → Аналитика
│   │
│   ├── /notifications      → Уведомления
│   │
│   ├── /settings
│   │   ├── /               → Основные настройки
│   │   ├── /profile        → Профиль
│   │   └── /preferences    → Предпочтения
│   │
│   └── /admin              → Только для ADMIN
│       ├── /users          → Управление пользователями
│       ├── /handbooks      → Справочники
│       │   ├── /priorities
│       │   ├── /statuses
│       │   ├── /project-types
│       │   └── /tag-categories
│       └── /system         → Системные настройки
```

### 8.2 Layouts

```tsx
// app/(auth)/layout.tsx
export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full">
        {children}
      </div>
    </div>
  );
}

// app/(dashboard)/layout.tsx
export default function DashboardLayout({ children }) {
  return (
    <div className="min-h-screen flex">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <Header />
        <main className="flex-1 p-6 bg-gray-50">
          {children}
        </main>
      </div>
    </div>
  );
}
```

---

## 9. Состояние (Zustand Stores)

### 9.1 Auth Store

```typescript
// stores/auth-store.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
  roles: string[];
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  
  // Actions
  setAuth: (user: User, accessToken: string, refreshToken: string) => void;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: true,
      
      setAuth: (user, accessToken, refreshToken) => set({
        user,
        accessToken,
        refreshToken,
        isAuthenticated: true,
        isLoading: false,
      }),
      
      logout: () => set({
        user: null,
        accessToken: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,
      }),
      
      updateUser: (userData) => set((state) => ({
        user: state.user ? { ...state.user, ...userData } : null,
      })),
      
      setLoading: (loading) => set({ isLoading: loading }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
      }),
    }
  )
);
```

### 9.2 Project Store

```typescript
// stores/project-store.ts
import { create } from 'zustand';

interface Project {
  id: number;
  name: string;
  key: string;
  description: string;
  color: string;
  type: { id: number; nameRu: string };
  owner: { id: number; firstName: string; lastName: string };
  membersCount: number;
  tasksCount: number;
  isArchived: boolean;
}

interface ProjectState {
  projects: Project[];
  currentProject: Project | null;
  isLoading: boolean;
  error: string | null;
  
  // Pagination
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  
  // Filters
  filters: {
    search: string;
    typeId: number | null;
    isArchived: boolean;
  };
  
  // Actions
  setProjects: (projects: Project[], pagination: any) => void;
  setCurrentProject: (project: Project | null) => void;
  addProject: (project: Project) => void;
  updateProject: (id: number, data: Partial<Project>) => void;
  removeProject: (id: number) => void;
  setFilters: (filters: Partial<ProjectState['filters']>) => void;
  setPage: (page: number) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
}

export const useProjectStore = create<ProjectState>((set) => ({
  projects: [],
  currentProject: null,
  isLoading: false,
  error: null,
  page: 1,
  size: 15,
  totalElements: 0,
  totalPages: 0,
  filters: {
    search: '',
    typeId: null,
    isArchived: false,
  },
  
  setProjects: (projects, pagination) => set({
    projects,
    page: pagination.page,
    totalElements: pagination.totalElements,
    totalPages: pagination.totalPages,
  }),
  
  setCurrentProject: (project) => set({ currentProject: project }),
  
  addProject: (project) => set((state) => ({
    projects: [project, ...state.projects],
    totalElements: state.totalElements + 1,
  })),
  
  updateProject: (id, data) => set((state) => ({
    projects: state.projects.map((p) =>
      p.id === id ? { ...p, ...data } : p
    ),
    currentProject: state.currentProject?.id === id
      ? { ...state.currentProject, ...data }
      : state.currentProject,
  })),
  
  removeProject: (id) => set((state) => ({
    projects: state.projects.filter((p) => p.id !== id),
    totalElements: state.totalElements - 1,
  })),
  
  setFilters: (filters) => set((state) => ({
    filters: { ...state.filters, ...filters },
    page: 1,
  })),
  
  setPage: (page) => set({ page }),
  setLoading: (loading) => set({ isLoading: loading }),
  setError: (error) => set({ error }),
}));
```

### 9.3 Board Store (Kanban)

```typescript
// stores/board-store.ts
import { create } from 'zustand';

interface Task {
  id: number;
  number: number;
  title: string;
  description: string;
  columnId: number;
  position: number;
  priority: { id: number; alias: string; color: string };
  assignee: { id: number; firstName: string; avatarUrl: string } | null;
  tags: { id: number; name: string; color: string }[];
  commentsCount: number;
  attachmentsCount: number;
  dueDate: string | null;
}

interface Column {
  id: number;
  name: string;
  color: string;
  position: number;
  wipLimit: number | null;
  tasks: Task[];
}

interface Board {
  id: number;
  name: string;
  projectId: number;
  columns: Column[];
}

interface BoardState {
  board: Board | null;
  isLoading: boolean;
  isDragging: boolean;
  
  // Actions
  setBoard: (board: Board) => void;
  addColumn: (column: Column) => void;
  updateColumn: (columnId: number, data: Partial<Column>) => void;
  removeColumn: (columnId: number) => void;
  reorderColumns: (columns: Column[]) => void;
  
  addTask: (columnId: number, task: Task) => void;
  updateTask: (taskId: number, data: Partial<Task>) => void;
  removeTask: (taskId: number) => void;
  moveTask: (taskId: number, targetColumnId: number, targetPosition: number) => void;
  
  setDragging: (isDragging: boolean) => void;
  setLoading: (loading: boolean) => void;
}

export const useBoardStore = create<BoardState>((set) => ({
  board: null,
  isLoading: false,
  isDragging: false,
  
  setBoard: (board) => set({ board }),
  
  addColumn: (column) => set((state) => ({
    board: state.board
      ? { ...state.board, columns: [...state.board.columns, column] }
      : null,
  })),
  
  updateColumn: (columnId, data) => set((state) => ({
    board: state.board
      ? {
          ...state.board,
          columns: state.board.columns.map((col) =>
            col.id === columnId ? { ...col, ...data } : col
          ),
        }
      : null,
  })),
  
  removeColumn: (columnId) => set((state) => ({
    board: state.board
      ? {
          ...state.board,
          columns: state.board.columns.filter((col) => col.id !== columnId),
        }
      : null,
  })),
  
  reorderColumns: (columns) => set((state) => ({
    board: state.board ? { ...state.board, columns } : null,
  })),
  
  addTask: (columnId, task) => set((state) => ({
    board: state.board
      ? {
          ...state.board,
          columns: state.board.columns.map((col) =>
            col.id === columnId
              ? { ...col, tasks: [...col.tasks, task] }
              : col
          ),
        }
      : null,
  })),
  
  updateTask: (taskId, data) => set((state) => ({
    board: state.board
      ? {
          ...state.board,
          columns: state.board.columns.map((col) => ({
            ...col,
            tasks: col.tasks.map((task) =>
              task.id === taskId ? { ...task, ...data } : task
            ),
          })),
        }
      : null,
  })),
  
  removeTask: (taskId) => set((state) => ({
    board: state.board
      ? {
          ...state.board,
          columns: state.board.columns.map((col) => ({
            ...col,
            tasks: col.tasks.filter((task) => task.id !== taskId),
          })),
        }
      : null,
  })),
  
  moveTask: (taskId, targetColumnId, targetPosition) => set((state) => {
    if (!state.board) return state;
    
    let movedTask: Task | null = null;
    
    // Remove task from source column
    const columnsWithoutTask = state.board.columns.map((col) => ({
      ...col,
      tasks: col.tasks.filter((task) => {
        if (task.id === taskId) {
          movedTask = task;
          return false;
        }
        return true;
      }),
    }));
    
    if (!movedTask) return state;
    
    // Add task to target column
    const finalColumns = columnsWithoutTask.map((col) => {
      if (col.id === targetColumnId) {
        const newTasks = [...col.tasks];
        newTasks.splice(targetPosition, 0, {
          ...movedTask!,
          columnId: targetColumnId,
          position: targetPosition,
        });
        return { ...col, tasks: newTasks };
      }
      return col;
    });
    
    return { board: { ...state.board, columns: finalColumns } };
  }),
  
  setDragging: (isDragging) => set({ isDragging }),
  setLoading: (loading) => set({ isLoading: loading }),
}));
```

### 9.4 Timer Store

```typescript
// stores/timer-store.ts
import { create } from 'zustand';

interface RunningTimer {
  id: number;
  taskId: number;
  taskTitle: string;
  projectKey: string;
  taskNumber: number;
  startedAt: string;
  description: string;
}

interface TimerState {
  runningTimer: RunningTimer | null;
  elapsedSeconds: number;
  isRunning: boolean;
  
  // Actions
  startTimer: (timer: RunningTimer) => void;
  stopTimer: () => void;
  setElapsedSeconds: (seconds: number) => void;
  tick: () => void;
}

export const useTimerStore = create<TimerState>((set) => ({
  runningTimer: null,
  elapsedSeconds: 0,
  isRunning: false,
  
  startTimer: (timer) => set({
    runningTimer: timer,
    elapsedSeconds: 0,
    isRunning: true,
  }),
  
  stopTimer: () => set({
    runningTimer: null,
    elapsedSeconds: 0,
    isRunning: false,
  }),
  
  setElapsedSeconds: (seconds) => set({ elapsedSeconds: seconds }),
  
  tick: () => set((state) => ({
    elapsedSeconds: state.elapsedSeconds + 1,
  })),
}));
```

### 9.5 UI Store

```typescript
// stores/ui-store.ts
import { create } from 'zustand';

interface UIState {
  sidebarCollapsed: boolean;
  theme: 'light' | 'dark' | 'system';
  isMobile: boolean;
  
  // Modals
  modals: {
    createTask: boolean;
    createProject: boolean;
    taskDetail: { open: boolean; taskId: number | null };
    inviteMember: boolean;
    confirmDelete: { open: boolean; onConfirm: () => void; title: string };
  };
  
  // Actions
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setTheme: (theme: UIState['theme']) => void;
  setIsMobile: (isMobile: boolean) => void;
  
  openModal: (modal: keyof UIState['modals'], data?: any) => void;
  closeModal: (modal: keyof UIState['modals']) => void;
}

export const useUIStore = create<UIState>((set) => ({
  sidebarCollapsed: false,
  theme: 'system',
  isMobile: false,
  
  modals: {
    createTask: false,
    createProject: false,
    taskDetail: { open: false, taskId: null },
    inviteMember: false,
    confirmDelete: { open: false, onConfirm: () => {}, title: '' },
  },
  
  toggleSidebar: () => set((state) => ({
    sidebarCollapsed: !state.sidebarCollapsed,
  })),
  
  setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),
  setTheme: (theme) => set({ theme }),
  setIsMobile: (isMobile) => set({ isMobile }),
  
  openModal: (modal, data) => set((state) => ({
    modals: {
      ...state.modals,
      [modal]: typeof state.modals[modal] === 'boolean' 
        ? true 
        : { open: true, ...data },
    },
  })),
  
  closeModal: (modal) => set((state) => ({
    modals: {
      ...state.modals,
      [modal]: typeof state.modals[modal] === 'boolean'
        ? false
        : { ...state.modals[modal], open: false },
    },
  })),
}));
```

---

## 10. UI Компоненты

### 10.1 Дизайн-система

**Цветовая палитра:**

```css
:root {
  /* Primary */
  --primary-50: #EFF6FF;
  --primary-500: #3B82F6;
  --primary-600: #2563EB;
  --primary-700: #1D4ED8;
  
  /* Neutral */
  --gray-50: #F9FAFB;
  --gray-100: #F3F4F6;
  --gray-200: #E5E7EB;
  --gray-500: #6B7280;
  --gray-700: #374151;
  --gray-900: #111827;
  
  /* Status */
  --success: #22C55E;
  --warning: #F59E0B;
  --error: #EF4444;
  --info: #3B82F6;
  
  /* Priority colors */
  --priority-critical: #DC2626;
  --priority-high: #F97316;
  --priority-medium: #EAB308;
  --priority-low: #22C55E;
}
```

### 10.2 Основные компоненты

#### Button

```tsx
// components/ui/button.tsx
import { cva, type VariantProps } from 'class-variance-authority';
import { forwardRef } from 'react';
import { cn } from '@/lib/utils';

const buttonVariants = cva(
  'inline-flex items-center justify-center rounded-lg font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
  {
    variants: {
      variant: {
        default: 'bg-primary-600 text-white hover:bg-primary-700',
        secondary: 'bg-gray-100 text-gray-700 hover:bg-gray-200',
        outline: 'border border-gray-300 bg-white hover:bg-gray-50',
        ghost: 'hover:bg-gray-100',
        destructive: 'bg-red-600 text-white hover:bg-red-700',
        link: 'text-primary-600 underline-offset-4 hover:underline',
      },
      size: {
        sm: 'h-8 px-3 text-sm',
        md: 'h-10 px-4',
        lg: 'h-12 px-6 text-lg',
        icon: 'h-10 w-10',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'md',
    },
  }
);

interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  isLoading?: boolean;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, isLoading, children, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(buttonVariants({ variant, size, className }))}
        disabled={isLoading}
        {...props}
      >
        {isLoading && (
          <svg className="mr-2 h-4 w-4 animate-spin" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" opacity="0.25" />
            <path fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
        )}
        {children}
      </button>
    );
  }
);
```

#### Task Card (Kanban)

```tsx
// components/tasks/task-card.tsx
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Avatar, Badge } from '@/components/ui';
import { MessageSquare, Paperclip, Clock } from 'lucide-react';

interface TaskCardProps {
  task: Task;
  onClick: () => void;
}

export function TaskCard({ task, onClick }: TaskCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: task.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      onClick={onClick}
      className={cn(
        'bg-white rounded-lg border border-gray-200 p-3 cursor-pointer',
        'hover:shadow-md hover:border-gray-300 transition-all',
        isDragging && 'opacity-50 shadow-lg'
      )}
    >
      {/* Tags */}
      {task.tags.length > 0 && (
        <div className="flex flex-wrap gap-1 mb-2">
          {task.tags.slice(0, 3).map((tag) => (
            <Badge
              key={tag.id}
              style={{ backgroundColor: tag.color }}
              className="text-xs text-white"
            >
              {tag.name}
            </Badge>
          ))}
          {task.tags.length > 3 && (
            <Badge variant="secondary" className="text-xs">
              +{task.tags.length - 3}
            </Badge>
          )}
        </div>
      )}

      {/* Title */}
      <h4 className="font-medium text-gray-900 mb-2 line-clamp-2">
        {task.title}
      </h4>

      {/* Footer */}
      <div className="flex items-center justify-between mt-3">
        <div className="flex items-center gap-2">
          {/* Task number */}
          <span className="text-xs text-gray-500">
            #{task.number}
          </span>
          
          {/* Priority */}
          {task.priority && (
            <div
              className="w-2 h-2 rounded-full"
              style={{ backgroundColor: task.priority.color }}
              title={task.priority.nameRu}
            />
          )}
        </div>

        <div className="flex items-center gap-3">
          {/* Comments count */}
          {task.commentsCount > 0 && (
            <div className="flex items-center text-gray-400 text-xs">
              <MessageSquare className="w-3 h-3 mr-1" />
              {task.commentsCount}
            </div>
          )}

          {/* Attachments count */}
          {task.attachmentsCount > 0 && (
            <div className="flex items-center text-gray-400 text-xs">
              <Paperclip className="w-3 h-3 mr-1" />
              {task.attachmentsCount}
            </div>
          )}

          {/* Due date */}
          {task.dueDate && (
            <div className={cn(
              'flex items-center text-xs',
              isOverdue(task.dueDate) ? 'text-red-500' : 'text-gray-400'
            )}>
              <Clock className="w-3 h-3 mr-1" />
              {formatDate(task.dueDate)}
            </div>
          )}

          {/* Assignee */}
          {task.assignee && (
            <Avatar
              src={task.assignee.avatarUrl}
              alt={task.assignee.firstName}
              size="xs"
            />
          )}
        </div>
      </div>
    </div>
  );
}
```

---

## 11. Docker & Deployment

### 11.1 Docker Compose (Development)

```yaml
# docker-compose.yml
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:16-alpine
    container_name: taskflow-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: taskflow_db
      POSTGRES_USER: taskflow_user
      POSTGRES_PASSWORD: ${DB_PASSWORD:-taskflow_secret}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U taskflow_user -d taskflow_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: taskflow-redis
    restart: unless-stopped
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # MinIO Object Storage
  minio:
    image: minio/minio:latest
    container_name: taskflow-minio
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_USER:-taskflow_minio}
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD:-minio_secret_123}
    volumes:
      - minio_data:/data
    ports:
      - "9000:9000"
      - "9001:9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Backend (Spring Boot)
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: taskflow-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/taskflow_db
      SPRING_DATASOURCE_USERNAME: taskflow_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-taskflow_secret}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      MINIO_ENDPOINT: http://minio:9000
      MINIO_ACCESS_KEY: ${MINIO_USER:-taskflow_minio}
      MINIO_SECRET_KEY: ${MINIO_PASSWORD:-minio_secret_123}
      JWT_SECRET: ${JWT_SECRET:-your-256-bit-secret-key-here}
      JWT_EXPIRATION: 86400000
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

  # Frontend (Next.js)
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: taskflow-frontend
    restart: unless-stopped
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080/api
      NEXT_PUBLIC_APP_URL: http://localhost:3000
    ports:
      - "3000:3000"
    depends_on:
      - backend

volumes:
  postgres_data:
  redis_data:
  minio_data:
```

### 11.2 Backend Dockerfile

```dockerfile
# backend/Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install timezone
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Bishkek /etc/localtime

# Copy gradle files
COPY gradlew .
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew build -x test -x bootJar --no-daemon || true

# Copy source code
COPY src/ src/

# Build application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Bishkek /etc/localtime && \
    addgroup -S appuser && adduser -S appuser -G appuser

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Xmx1g -Xms512m \
    -XX:+UseContainerSupport \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
```

### 11.3 Frontend Dockerfile

```dockerfile
# frontend/Dockerfile
FROM node:20-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci

FROM node:20-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
ENV NEXT_TELEMETRY_DISABLED 1
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT 3000
ENV HOSTNAME "0.0.0.0"

CMD ["node", "server.js"]
```

### 11.4 Coolify Deployment

Для деплоя на **Coolify** создаём конфигурацию:

```yaml
# coolify-compose.yml
version: '3.8'

services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: ${DB_URL}
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: ${REDIS_HOST}
      MINIO_ENDPOINT: ${MINIO_ENDPOINT}
      JWT_SECRET: ${JWT_SECRET}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 90s

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
      args:
        NEXT_PUBLIC_API_URL: ${API_URL}
    environment:
      NEXT_PUBLIC_API_URL: ${API_URL}
```

---

## 12. План реализации

### Фаза 1: Инфраструктура (1-2 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 1.1 | Инициализация Spring Boot проекта | 🔴 High |
| 1.2 | Инициализация Next.js проекта | 🔴 High |
| 1.3 | Настройка Docker Compose | 🔴 High |
| 1.4 | Настройка PostgreSQL + Flyway | 🔴 High |
| 1.5 | Настройка Redis | 🟡 Medium |
| 1.6 | Настройка MinIO | 🟡 Medium |

### Фаза 2: Аутентификация (2-3 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 2.1 | JWT аутентификация (Backend) | 🔴 High |
| 2.2 | Миграции: users, roles, tokens | 🔴 High |
| 2.3 | Auth API endpoints | 🔴 High |
| 2.4 | Login/Register формы (Frontend) | 🔴 High |
| 2.5 | Auth Guard и middleware | 🔴 High |
| 2.6 | Zustand auth store | 🔴 High |

### Фаза 3: Справочники (1-2 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 3.1 | Миграции справочников | 🔴 High |
| 3.2 | HBTaskPriority CRUD | 🔴 High |
| 3.3 | HBTaskStatus CRUD | 🔴 High |
| 3.4 | HBProjectType CRUD | 🟡 Medium |
| 3.5 | HBTagCategory CRUD | 🟡 Medium |
| 3.6 | Admin UI для справочников | 🟡 Medium |

### Фаза 4: Проекты (2-3 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 4.1 | Миграции: projects, members | 🔴 High |
| 4.2 | Project CRUD API | 🔴 High |
| 4.3 | Project members API | 🔴 High |
| 4.4 | Список проектов UI | 🔴 High |
| 4.5 | Создание проекта UI | 🔴 High |
| 4.6 | Детали проекта UI | 🔴 High |
| 4.7 | Приглашения в проект | 🟡 Medium |

### Фаза 5: Kanban доски (3-4 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 5.1 | Миграции: boards, columns, tasks | 🔴 High |
| 5.2 | Board CRUD API | 🔴 High |
| 5.3 | Column CRUD API | 🔴 High |
| 5.4 | Task CRUD API | 🔴 High |
| 5.5 | Kanban доска UI | 🔴 High |
| 5.6 | Drag & Drop задач | 🔴 High |
| 5.7 | Фильтрация задач | 🟡 Medium |
| 5.8 | Quick actions | 🟡 Medium |

### Фаза 6: Детали задачи (2-3 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 6.1 | Task detail API | 🔴 High |
| 6.2 | Комментарии API | 🔴 High |
| 6.3 | Вложения API | 🟡 Medium |
| 6.4 | Теги API | 🟡 Medium |
| 6.5 | Task detail modal/page | 🔴 High |
| 6.6 | Комментарии UI | 🔴 High |
| 6.7 | Вложения UI | 🟡 Medium |
| 6.8 | Чек-листы | 🟢 Low |

### Фаза 7: Трекинг времени (2 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 7.1 | Миграции: time_entries | 🔴 High |
| 7.2 | Time entries API | 🔴 High |
| 7.3 | Timer widget | 🔴 High |
| 7.4 | Time entries list | 🔴 High |
| 7.5 | Time reports | 🟡 Medium |

### Фаза 8: Уведомления (1-2 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 8.1 | Notifications API | 🟡 Medium |
| 8.2 | Notification bell | 🟡 Medium |
| 8.3 | Notifications page | 🟡 Medium |
| 8.4 | Email notifications | 🟢 Low |

### Фаза 9: Аналитика (2 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 9.1 | Analytics API | 🟡 Medium |
| 9.2 | Dashboard overview | 🟡 Medium |
| 9.3 | Project analytics | 🟡 Medium |
| 9.4 | Burndown chart | 🟢 Low |
| 9.5 | Velocity chart | 🟢 Low |

### Фаза 10: Финализация (2-3 дня)

| # | Задача | Приоритет |
|---|--------|-----------|
| 10.1 | Тестирование | 🔴 High |
| 10.2 | Оптимизация | 🟡 Medium |
| 10.3 | Документация | 🟡 Medium |
| 10.4 | Деплой на Coolify | 🔴 High |

---

## 13. Технические требования

### 13.1 Backend Dependencies

```gradle
// build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.5'
    id 'io.spring.dependency-management' version '1.1.6'
}

java {
    sourceCompatibility = '21'
}

dependencies {
    // Spring Boot Core
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    
    // Database
    runtimeOnly 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'
    
    // MinIO
    implementation 'io.minio:minio:8.5.9'
    
    // Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9'
    
    // Mapping
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
    
    // Utilities
    implementation 'org.apache.commons:commons-lang3'
    implementation 'commons-io:commons-io:2.15.1'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:postgresql'
}
```

### 13.2 Frontend Dependencies

```json
// package.json
{
  "name": "taskflow-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint"
  },
  "dependencies": {
    "next": "14.2.15",
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    
    "zustand": "^4.5.5",
    "react-hook-form": "^7.53.0",
    "@hookform/resolvers": "^3.9.0",
    "zod": "^3.23.8",
    
    "axios": "^1.7.7",
    "@tanstack/react-query": "^5.56.2",
    
    "@dnd-kit/core": "^6.1.0",
    "@dnd-kit/sortable": "^8.0.0",
    "@dnd-kit/utilities": "^3.2.2",
    
    "date-fns": "^3.6.0",
    "lucide-react": "^0.446.0",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.1.1",
    "tailwind-merge": "^2.5.2",
    
    "recharts": "^2.12.7",
    "react-hot-toast": "^2.4.1",
    "@radix-ui/react-dialog": "^1.1.1",
    "@radix-ui/react-dropdown-menu": "^2.1.1",
    "@radix-ui/react-popover": "^1.1.1",
    "@radix-ui/react-select": "^2.1.1",
    "@radix-ui/react-tabs": "^1.1.0",
    "@radix-ui/react-tooltip": "^1.1.2"
  },
  "devDependencies": {
    "typescript": "^5.6.2",
    "@types/node": "^22.5.5",
    "@types/react": "^18.3.8",
    "@types/react-dom": "^18.3.0",
    "tailwindcss": "^3.4.12",
    "postcss": "^8.4.47",
    "autoprefixer": "^10.4.20",
    "eslint": "^8.57.1",
    "eslint-config-next": "14.2.15"
  }
}
```

### 13.3 Переменные окружения

```bash
# .env.example

# Database
DB_URL=jdbc:postgresql://localhost:5432/taskflow_db
DB_USER=taskflow_user
DB_PASSWORD=your_secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=taskflow_minio
MINIO_SECRET_KEY=minio_secret_123
MINIO_BUCKET=taskflow-files

# JWT
JWT_SECRET=your-256-bit-secret-key-change-in-production
JWT_EXPIRATION=86400000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000

# Frontend
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_APP_URL=http://localhost:3000
```

---

## 📊 Итого

| Метрика | Значение |
|---------|----------|
| **Таблиц в БД** | ~25 |
| **API Endpoints** | ~80 |
| **React компонентов** | ~60 |
| **Zustand stores** | 8 |
| **Справочников (HB)** | 5 |
| **Системных ролей** | 3 |
| **Ролей в проекте** | 4 |
| **Ориентировочное время** | 3-4 недели |

---

**Готов к реализации! 🚀**

Следующий шаг: начинаем с Фазы 1 — инициализация проектов и настройка инфраструктуры.
