# TaskFlow

Modern project management system with Kanban boards, task tracking, team collaboration, and Telegram notifications.

## Tech Stack

### Backend
- **Java 21** + **Spring Boot 3.3.5**
- **PostgreSQL** - Primary database
- **Redis** - Caching and sessions
- **MinIO** - File storage (S3-compatible)
- **Flyway** - Database migrations
- **JWT** - Authentication
- **MapStruct** - DTO mapping
- **Telegram Bot API** - Push notifications

### Frontend
- **Next.js 14** (App Router)
- **TypeScript**
- **Tailwind CSS**
- **shadcn/ui** - UI components (Radix UI)
- **Zustand** - State management
- **React Hook Form** + **Zod** - Form validation
- **Axios** - HTTP client

## Features

### Core Features
- **Project Management** - Create and manage projects with customizable workflows
- **Kanban Boards** - Drag-and-drop task management
- **Task Tracking** - Priorities, statuses, assignees, due dates, subtasks
- **Team Collaboration** - Invite members, assign roles, manage permissions
- **Comments** - Task discussions with @mentions
- **File Attachments** - Upload and manage files via MinIO

### Admin Panel
- **User Management** - CRUD operations, role assignment, block/unblock
- **Role Management** - Create custom roles with permissions
- **Permission Matrix** - Route-based access control (GET/POST/PUT/DELETE)
- **Project Overview** - Archive, delete, restore projects
- **Task Overview** - View and manage all tasks
- **Handbooks** - Statuses, priorities, project types, tag categories

### Notifications
- **In-App Notifications** - Real-time notification center
- **Telegram Integration** - Push notifications to Telegram
- **Notification Types**:
  - Task assigned
  - Task completed
  - New comment
  - @Mention
  - Project/Team invitation
  - Deadline reminders (3 days, 1 day, today)
- **User Preferences** - Enable/disable by channel and type

### Internationalization
- **Multi-language** - English, Russian, Kyrgyz support
- **Localized UI** - All labels and messages translated

## Project Structure

```
evo/
├── backend/                 # Spring Boot API
│   ├── src/main/java/kg/taskflow/
│   │   ├── config/         # Configuration classes
│   │   ├── controller/     # REST controllers
│   │   ├── db/
│   │   │   ├── entity/     # JPA entities
│   │   │   └── repository/ # Spring Data repositories
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── mapper/         # MapStruct mappers
│   │   ├── scheduler/      # Scheduled tasks (deadlines)
│   │   ├── security/       # JWT & security filters
│   │   ├── service/        # Business logic
│   │   └── telegram/       # Telegram bot integration
│   └── src/main/resources/
│       └── db/migration/   # Flyway migrations (21 migrations)
│
├── frontend/               # Next.js application
│   ├── src/
│   │   ├── app/           # App Router pages
│   │   │   ├── (auth)/    # Login, Register
│   │   │   └── (dashboard)/
│   │   │       ├── admin/ # Admin panel pages
│   │   │       ├── projects/ # Project pages
│   │   │       └── settings/ # User settings
│   │   ├── components/    # React components
│   │   │   └── ui/        # shadcn/ui components
│   │   ├── contexts/      # React contexts
│   │   ├── hooks/         # Custom hooks
│   │   ├── services/      # API services
│   │   ├── stores/        # Zustand stores
│   │   └── types/         # TypeScript types
│   └── public/            # Static assets
│
└── docker/                 # Docker configurations
    ├── backend/
    │   ├── Dockerfile
    │   └── docker-compose.simple.yml
    └── frontend/
        ├── Dockerfile
        └── docker-compose.yml
```

## Getting Started

### Prerequisites

- Java 21
- Node.js 20+
- PostgreSQL 15+
- Redis (optional, for caching)
- MinIO (optional, for file storage)

### Backend Setup

```bash
cd backend

# Configure database in application.properties or application-local.properties
# spring.datasource.url=jdbc:postgresql://localhost:5432/taskflow_db
# spring.datasource.username=your_user
# spring.datasource.password=your_password

# Run with Gradle
./gradlew bootRun
```

API will be available at `http://localhost:8080/api`

Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Create .env.local
echo "NEXT_PUBLIC_API_URL=http://localhost:8080/api" > .env.local

# Run development server
npm run dev
```

Frontend will be available at `http://localhost:3000`

### Default Admin Account

```
Email: admin@taskflow.kg
Password: admin123
```

## Docker Deployment

### Build and Run with Docker Compose

```bash
# Frontend
cd docker/frontend
docker-compose up -d

# Backend (configure environment variables)
cd docker/backend
docker-compose -f docker-compose.simple.yml up -d
```

### Environment Variables

#### Backend
| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL connection URL | - |
| `DB_USER` | Database username | - |
| `DB_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT signing key | - |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | - |
| `MINIO_ENDPOINT` | MinIO server URL | - |
| `MINIO_ACCESS_KEY` | MinIO access key | - |
| `MINIO_SECRET_KEY` | MinIO secret key | - |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `TELEGRAM_BOT_TOKEN` | Telegram bot token | - |
| `TELEGRAM_BOT_USERNAME` | Telegram bot username | - |

#### Frontend
| Variable | Description | Default |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend API URL | - |
| `NEXT_PUBLIC_SOCKET_URL` | WebSocket URL | - |
| `NEXT_PUBLIC_STORAGE_URL` | File storage URL | - |

## API Documentation

Interactive API documentation is available via Swagger UI:
- Development: `http://localhost:8080/api/swagger-ui.html`
- Production: `https://your-domain.com/api/swagger-ui.html`

## Database Migrations

Migrations are managed by Flyway and run automatically on startup.

Location: `backend/src/main/resources/db/migration/`

Naming convention: `V{version}__{description}.sql`

Current migrations:
- V001-V010: Core schema (users, projects, boards, tasks)
- V011-V015: Permissions and roles
- V016-V019: Attachments and analytics
- V020: Telegram integration
- V021: Notification settings routes

## Admin Panel

Access the admin panel at `/admin` (requires ADMIN role):

| Section | Features |
|---------|----------|
| **Dashboard** | System overview and statistics |
| **Users** | Create, edit, block/unblock, assign roles, soft delete/restore |
| **Roles** | Create custom roles, toggle active status |
| **Permissions** | Permission matrix with HTTP method controls |
| **Projects** | View all projects, archive/unarchive, delete/restore |
| **Tasks** | View all tasks, delete/restore |
| **Handbooks** | |
| - Statuses | Task statuses with colors and final flag |
| - Priorities | Task priorities with levels |
| - Project Types | Project categorization |
| - Tag Categories | Tag organization |

## Telegram Notifications Setup

1. Create a bot via [@BotFather](https://t.me/BotFather)
2. Get the bot token and username
3. Configure environment variables:
   ```
   TELEGRAM_BOT_ENABLED=true
   TELEGRAM_BOT_TOKEN=your_bot_token
   TELEGRAM_BOT_USERNAME=your_bot_username
   ```
4. Users can connect their Telegram in Settings page
5. Notifications are sent based on user preferences

### Supported Bot Commands
- `/start <code>` - Link Telegram account
- `/status` - Check connection status
- `/help` - Show available commands
- `/unlink` - Disconnect Telegram account

## Scheduled Tasks

| Task | Schedule | Description |
|------|----------|-------------|
| Route Cache Refresh | Every 5 minutes | Updates permission cache |
| Deadline Reminders | Daily at 9:00 AM | Sends reminders for upcoming deadlines |

## License

Private - All rights reserved

## Support

For issues and feature requests, please contact the development team.
