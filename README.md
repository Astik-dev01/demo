# TaskFlow

Modern project management system with Kanban boards, task tracking, and team collaboration.

## Tech Stack

### Backend
- **Java 21** + **Spring Boot 3.3.5**
- **PostgreSQL** - Primary database
- **Redis** - Caching and sessions
- **MinIO** - File storage (S3-compatible)
- **Flyway** - Database migrations
- **JWT** - Authentication
- **MapStruct** - DTO mapping

### Frontend
- **Next.js 14** (App Router)
- **TypeScript**
- **Tailwind CSS**
- **shadcn/ui** - UI components
- **React Hook Form** + **Zod** - Form validation
- **Axios** - HTTP client

## Features

- **Project Management** - Create and manage projects with customizable workflows
- **Kanban Boards** - Drag-and-drop task management
- **Task Tracking** - Priorities, statuses, assignees, due dates
- **Team Collaboration** - Invite members, assign roles
- **Admin Panel** - User, role, and permission management
- **Multi-language** - English, Russian, Kyrgyz support
- **Real-time Updates** - WebSocket notifications
- **File Attachments** - Upload and manage files via MinIO

## Project Structure

```
evo/
├── backend/                 # Spring Boot API
│   ├── src/main/java/kg/taskflow/
│   │   ├── config/         # Configuration classes
│   │   ├── controller/     # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── mapper/         # MapStruct mappers
│   │   ├── repository/     # Spring Data repositories
│   │   ├── security/       # JWT & security
│   │   └── service/        # Business logic
│   └── src/main/resources/
│       └── db/migration/   # Flyway migrations
│
├── frontend/               # Next.js application
│   ├── src/
│   │   ├── app/           # App Router pages
│   │   ├── components/    # React components
│   │   ├── services/      # API services
│   │   ├── hooks/         # Custom hooks
│   │   └── lib/           # Utilities
│   └── public/            # Static assets
│
└── docker/                 # Docker configurations
    ├── backend/
    │   └── Dockerfile
    └── frontend/
        ├── Dockerfile
        └── docker-compose.yml
```

## Getting Started

### Prerequisites

- Java 21
- Node.js 20+
- PostgreSQL 15+
- Redis (optional)
- MinIO (optional)

### Backend Setup

```bash
cd backend

# Configure database in application.properties
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
docker-compose up -d
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
| `REDIS_HOST` | Redis host | - |

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

## Admin Panel

Access the admin panel at `/admin` (requires ADMIN role):

- **Users** - Manage system users
- **Roles** - Configure system roles
- **Permissions** - Set up route-based permissions
- **Projects** - View/manage all projects
- **Tasks** - View/manage all tasks
- **Handbooks** - Manage statuses, priorities, tags

## License

Private - All rights reserved

## Support

For issues and feature requests, please contact the development team.
