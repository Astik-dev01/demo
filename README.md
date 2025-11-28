<div align="center">

# 🚀 TaskFlow

### Modern Project Management System

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-000000?style=for-the-badge&logo=next.js&logoColor=white)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)

**Современная система управления проектами с Kanban-досками, отслеживанием задач, командной работой и Telegram-уведомлениями**

[🌐 Demo](https://taskflow-test.netlify.app) · [📖 API Docs](https://api.freewayspace.space/api/swagger-ui.html)

</div>

---

## 📋 О проекте

TaskFlow — это полнофункциональная система управления проектами, разработанная для команд любого размера. Система предоставляет интуитивный интерфейс для организации работы, отслеживания прогресса и эффективной коммуникации внутри команды.

### ✨ Ключевые возможности

| Категория | Возможности |
|-----------|-------------|
| **📊 Проекты** | Создание проектов, настраиваемые workflows, архивирование |
| **📋 Kanban-доски** | Drag-and-drop, WIP-лимиты, множественные доски |
| **✅ Задачи** | Приоритеты, статусы, исполнители, сроки, подзадачи, теги |
| **👥 Команды** | Приглашения, роли участников, управление доступом |
| **💬 Комментарии** | Обсуждения задач, @упоминания |
| **📎 Файлы** | Загрузка и управление вложениями |
| **🔔 Уведомления** | In-app + Telegram push-уведомления |
| **🌍 Языки** | Русский, English, Кыргызча |

---

## 🛠 Технологический стек

<table>
<tr>
<td width="50%" valign="top">

### Backend
| Технология | Версия |
|------------|--------|
| Java | 21 |
| Spring Boot | 3.3.5 |
| PostgreSQL | 15+ |
| Redis | 7+ |
| MinIO | Latest |
| Flyway | 10.10 |

</td>
<td width="50%" valign="top">

### Frontend
| Технология | Версия |
|------------|--------|
| Next.js | 14 |
| TypeScript | 5 |
| Tailwind CSS | 3.4 |
| shadcn/ui | Latest |
| Zustand | 4.5 |
| React Hook Form | 7 |

</td>
</tr>
</table>

---

## 🔐 Тестовые пользователи

> **Demo URL:** https://taskflow-test.netlify.app

| Email | Пароль | Роль | Описание |
|-------|--------|------|----------|
| `admin@taskflow.kg` | `password123` | 🔴 ADMIN | Полный доступ к системе и админ-панели |
| `john@taskflow.kg` | `password123` | 🟢 USER | Обычный пользователь |
| `jane@taskflow.kg` | `password123` | 🟢 USER | Обычный пользователь |
| `alex.petrov@taskflow.kg` | `password123` | 🔴 ADMIN | Демо администратор |
| `maria.kim@taskflow.kg` | `password123` | 🔴 ADMIN | Демо администратор |
| `dmitry.ivanov@taskflow.kg` | `password123` | 🟢 USER | Backend разработчик |
| `elena.smirnova@taskflow.kg` | `password123` | 🟢 USER | QA инженер |
| `artem.kozlov@taskflow.kg` | `password123` | 🟢 USER | DevOps инженер |
| `anna.volkova@taskflow.kg` | `password123` | 🟢 USER | Frontend разработчик |

---

## 🎯 Демо проекты

В системе созданы демо-проекты для тестирования:

| Проект | Ключ | Описание |
|--------|------|----------|
| **E-Commerce Platform** | ECOM | Платформа электронной коммерции |
| **Mobile Banking App** | BANK | Мобильное банковское приложение |
| **HR Management System** | HRMS | Система управления персоналом |
| **CRM System** | CRM | CRM для отдела продаж |
| **DevOps Infrastructure** | DEVOPS | Настройка CI/CD и мониторинг |

---

## 🚀 Быстрый старт

### Требования

- Java 21
- Node.js 20+
- PostgreSQL 15+
- Redis 7+ (опционально)
- MinIO (опционально)

### Backend

```bash
cd backend

# Настройте базу данных в application.properties
# spring.datasource.url=jdbc:postgresql://localhost:5432/taskflow_db

# Запуск
./gradlew bootRun
```

API: `http://localhost:8080/api`

### Frontend

```bash
cd frontend

# Установка зависимостей
npm install

# Создание .env.local
echo "NEXT_PUBLIC_API_URL=http://localhost:8080/api" > .env.local

# Запуск
npm run dev
```

Frontend: `http://localhost:3000`

---

## 🔧 Админ-панель

Доступ: `/admin` (требуется роль ADMIN)

| Раздел | Возможности |
|--------|-------------|
| **👤 Пользователи** | CRUD, назначение ролей, блокировка |
| **🎭 Роли** | Создание кастомных ролей |
| **🔐 Права доступа** | Матрица прав (GET/POST/PUT/DELETE) |
| **📁 Проекты** | Просмотр, архивация, удаление |
| **📋 Задачи** | Просмотр и управление всеми задачами |
| **📚 Справочники** | Статусы, приоритеты, типы проектов |
| **🤖 AI Генератор** | Генерация форм и таблиц с помощью AI |

---

## 🤖 AI-генерация форм и таблиц

TaskFlow использует искусственный интеллект для генерации UI компонентов на основе описания на естественном языке.

### Возможности

| Компонент | Описание |
|-----------|----------|
| **DynamicForm** | Рендеринг формы на основе JSON-схемы |
| **DynamicTable** | Рендеринг таблицы с сортировкой, фильтрацией, пагинацией |
| **AI Генератор** | Интерфейс для генерации схем через Groq API (Llama 3.1 70B) |

### Как использовать

1. **Откройте AI Генератор:** `/admin/ai-generator`
2. **Опишите форму или таблицу** на естественном языке:
   ```
   Создай форму регистрации пользователя с полями:
   имя, email, пароль, телефон и чекбокс согласия
   ```
3. **Получите JSON-схему** и используйте её в компонентах

### Пример интеграции

```typescript
// 1. Схема формы (можно сгенерировать через AI или написать вручную)
const userFormSchema = {
  title: 'Регистрация',
  fields: [
    { name: 'email', label: 'Email', type: 'email', required: true },
    { name: 'password', label: 'Пароль', type: 'password', required: true },
  ],
  submitButtonText: 'Зарегистрироваться'
};

// 2. Использование компонента
<DynamicForm
  schema={userFormSchema}
  onSubmit={(data) => api.register(data)}
/>
```

### Демо-страница

Пример использования AI-сгенерированных форм и таблиц: `/admin/handbooks/tags`

---

## 📱 Telegram интеграция

### Настройка

1. Создайте бота через [@BotFather](https://t.me/BotFather)
2. Настройте переменные окружения:
   ```env
   TELEGRAM_BOT_TOKEN=your_bot_token
   TELEGRAM_BOT_USERNAME=your_bot_username
   ```
3. Пользователи подключают Telegram в настройках профиля

### Типы уведомлений

- ✅ Назначение задачи
- 🎉 Завершение задачи
- 💬 Новый комментарий
- 📢 @Упоминание
- 📨 Приглашение в проект/команду
- ⏰ Напоминания о дедлайнах (3 дня, 1 день, сегодня)

---

## 📁 Структура проекта

```
evo/
├── backend/                    # Spring Boot API
│   ├── src/main/java/
│   │   └── kg/taskflow/
│   │       ├── config/         # Конфигурации
│   │       ├── controller/     # REST контроллеры
│   │       ├── db/entity/      # JPA сущности
│   │       ├── dto/            # DTO объекты
│   │       ├── service/        # Бизнес-логика
│   │       └── telegram/       # Telegram бот
│   └── src/main/resources/
│       └── db/migration/       # Flyway миграции (24)
│
├── frontend/                   # Next.js приложение
│   └── src/
│       ├── app/               # App Router
│       ├── components/        # React компоненты
│       ├── services/          # API сервисы
│       └── stores/            # Zustand хранилища
│
└── .github/workflows/         # CI/CD пайплайны
```

---

## 🔄 CI/CD

- **Backend:** GitHub Actions → Docker → GHCR → DigitalOcean
- **Frontend:** Netlify (автодеплой из main)
- **Миграции:** Flyway (автоматически при старте)

---

## 📄 Лицензия

Private - Все права защищены

---

<div align="center">

**Made with ❤️ by Serikov Astan**

</div>
