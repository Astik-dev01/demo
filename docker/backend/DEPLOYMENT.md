# TaskFlow Backend - Digital Ocean Deployment Guide

## Prerequisites

- Digital Ocean account
- Domain name pointed to your Droplet IP
- SSH access to the Droplet

## Quick Start

### 1. Create a Droplet

1. Go to Digital Ocean → Create → Droplets
2. Choose **Ubuntu 22.04 LTS**
3. Select plan (recommended: **Basic $12/mo** - 2GB RAM, 1 vCPU)
4. Choose datacenter region
5. Add SSH key for authentication
6. Create Droplet

### 2. Point Domain to Droplet

Add an A record in your DNS settings:
```
Type: A
Host: api (or @ for root domain)
Value: <your-droplet-ip>
TTL: 3600
```

### 3. Connect to Droplet

```bash
ssh root@<your-droplet-ip>
```

### 4. Clone Repository

```bash
cd /opt
git clone <your-repository-url> taskflow
cd taskflow/docker/backend
```

### 5. Initial Setup

```bash
chmod +x deploy.sh
./deploy.sh setup
```

This will:
- Update the system
- Install Docker and Docker Compose
- Create necessary directories

**Important:** Log out and log back in after setup for Docker permissions.

### 6. Configure Environment

```bash
cp .env.example .env
nano .env
```

Update these values:
```env
# Strong passwords (generate with: openssl rand -base64 32)
POSTGRES_PASSWORD=<strong-password>
REDIS_PASSWORD=<strong-password>
MINIO_ROOT_PASSWORD=<strong-password>

# JWT Secret (generate with: openssl rand -hex 32)
JWT_SECRET=<64-character-hex-string>

# Your domains
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
DOMAIN=api.your-domain.com
EMAIL=your-email@example.com
```

### 7. Start Services

```bash
./deploy.sh start
```

### 8. Setup SSL Certificate

```bash
./deploy.sh ssl
./deploy.sh restart
```

### 9. Verify Deployment

```bash
./deploy.sh status
```

You should see all services as healthy.

## Available Commands

| Command | Description |
|---------|-------------|
| `./deploy.sh setup` | Initial server setup |
| `./deploy.sh start` | Build and start all services |
| `./deploy.sh stop` | Stop all services |
| `./deploy.sh restart` | Restart all services |
| `./deploy.sh logs` | View all logs |
| `./deploy.sh logs backend` | View backend logs only |
| `./deploy.sh ssl` | Setup SSL certificate |
| `./deploy.sh status` | Check service health |
| `./deploy.sh backup` | Create database backup |

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Digital Ocean Droplet                │
│                                                         │
│  ┌─────────┐    ┌─────────┐    ┌─────────────────────┐ │
│  │  Nginx  │───▶│ Backend │───▶│ PostgreSQL / Redis  │ │
│  │ :80/443 │    │  :8080  │    │      / MinIO        │ │
│  └─────────┘    └─────────┘    └─────────────────────┘ │
│       │                                                 │
│       ▼                                                 │
│  ┌─────────┐                                           │
│  │ Certbot │ (SSL auto-renewal)                        │
│  └─────────┘                                           │
└─────────────────────────────────────────────────────────┘
```

## Security Notes

1. **Firewall**: Only ports 80 and 443 are exposed externally
2. **SSL**: Automatic HTTPS with Let's Encrypt
3. **Passwords**: All services use strong, unique passwords
4. **Network**: Internal Docker network isolates services

## Updating the Application

```bash
cd /opt/taskflow
git pull origin main
cd docker/backend
./deploy.sh start  # Rebuilds and restarts
```

## Monitoring

### View Logs
```bash
# All services
./deploy.sh logs

# Specific service
./deploy.sh logs backend
./deploy.sh logs postgres
./deploy.sh logs nginx
```

### Check Resources
```bash
docker stats
```

## Backup & Restore

### Create Backup
```bash
./deploy.sh backup
```

### Restore Backup
```bash
docker exec -i taskflow-postgres psql -U taskflow taskflow_db < backup_file.sql
```

## Troubleshooting

### Backend won't start
```bash
./deploy.sh logs backend
```
Common issues:
- Database not ready: Wait for postgres healthcheck
- Wrong credentials: Check .env file

### SSL certificate issues
```bash
# Check nginx config
docker exec taskflow-nginx nginx -t

# View certbot logs
docker logs taskflow-certbot
```

### Database connection issues
```bash
# Check postgres status
docker exec taskflow-postgres pg_isready -U taskflow

# Connect to database
docker exec -it taskflow-postgres psql -U taskflow -d taskflow_db
```

## Resource Requirements

| Service | Memory | CPU |
|---------|--------|-----|
| PostgreSQL | ~256MB | Low |
| Redis | ~64MB | Low |
| MinIO | ~128MB | Low |
| Backend | 512MB-1GB | Medium |
| Nginx | ~32MB | Low |
| **Total** | **~1.5GB** | - |

Recommended Droplet: **2GB RAM / 1 vCPU** ($12/mo)
