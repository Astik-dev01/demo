#!/bin/bash

# TaskFlow Backend Deployment Script for Digital Ocean Droplet
# Usage: ./deploy.sh [command]
# Commands: setup, start, stop, restart, logs, ssl, status

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_FILE="docker-compose.yml"

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if .env file exists
check_env() {
    if [ ! -f .env ]; then
        log_error ".env file not found!"
        log_info "Copy .env.example to .env and update values:"
        log_info "  cp .env.example .env"
        log_info "  nano .env"
        exit 1
    fi
}

# Initial server setup
setup() {
    log_info "Setting up server..."

    # Update system
    sudo apt-get update && sudo apt-get upgrade -y

    # Install Docker
    if ! command -v docker &> /dev/null; then
        log_info "Installing Docker..."
        curl -fsSL https://get.docker.com -o get-docker.sh
        sudo sh get-docker.sh
        sudo usermod -aG docker $USER
        rm get-docker.sh
        log_warn "Please log out and log back in for Docker permissions to take effect"
    else
        log_info "Docker already installed"
    fi

    # Install Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_info "Installing Docker Compose..."
        sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose
    else
        log_info "Docker Compose already installed"
    fi

    # Create SSL directory
    mkdir -p ssl

    log_info "Setup complete!"
    log_info "Next steps:"
    log_info "  1. Copy .env.example to .env: cp .env.example .env"
    log_info "  2. Update .env with your values: nano .env"
    log_info "  3. Start services: ./deploy.sh start"
}

# Start all services
start() {
    check_env
    log_info "Starting TaskFlow services..."
    docker-compose -f $COMPOSE_FILE up -d --build
    log_info "Services started!"
    log_info "Checking health..."
    sleep 10
    status
}

# Stop all services
stop() {
    log_info "Stopping TaskFlow services..."
    docker-compose -f $COMPOSE_FILE down
    log_info "Services stopped!"
}

# Restart all services
restart() {
    log_info "Restarting TaskFlow services..."
    docker-compose -f $COMPOSE_FILE restart
    log_info "Services restarted!"
}

# View logs
logs() {
    local service=${1:-""}
    if [ -n "$service" ]; then
        docker-compose -f $COMPOSE_FILE logs -f $service
    else
        docker-compose -f $COMPOSE_FILE logs -f
    fi
}

# Setup SSL with Let's Encrypt
ssl() {
    check_env
    source .env

    if [ -z "$DOMAIN" ] || [ -z "$EMAIL" ]; then
        log_error "DOMAIN and EMAIL must be set in .env file"
        exit 1
    fi

    log_info "Setting up SSL for $DOMAIN..."

    # Create temporary nginx config for certbot challenge
    cat > nginx.conf.tmp << 'EOF'
events { worker_connections 1024; }
http {
    server {
        listen 80;
        server_name _;
        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
        }
        location / {
            return 200 'OK';
        }
    }
}
EOF

    # Start nginx with temporary config
    docker run -d --name temp-nginx \
        -p 80:80 \
        -v $(pwd)/nginx.conf.tmp:/etc/nginx/nginx.conf:ro \
        -v taskflow-certbot-webroot:/var/www/certbot \
        nginx:alpine

    # Get certificate
    docker run --rm \
        -v taskflow-certbot-webroot:/var/www/certbot \
        -v taskflow-certbot-certs:/etc/letsencrypt \
        certbot/certbot certonly --webroot \
        --webroot-path=/var/www/certbot \
        --email $EMAIL \
        --agree-tos \
        --no-eff-email \
        -d $DOMAIN

    # Stop temporary nginx
    docker stop temp-nginx && docker rm temp-nginx
    rm nginx.conf.tmp

    # Update nginx.conf with correct domain
    sed -i "s/your-domain.com/$DOMAIN/g" nginx.conf

    log_info "SSL certificate obtained successfully!"
    log_info "Restart services to apply: ./deploy.sh restart"
}

# Check status of all services
status() {
    log_info "Service Status:"
    docker-compose -f $COMPOSE_FILE ps

    echo ""
    log_info "Health Checks:"

    # Check backend health
    if curl -sf http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        echo -e "  Backend API: ${GREEN}✓ Healthy${NC}"
    else
        echo -e "  Backend API: ${RED}✗ Unhealthy${NC}"
    fi

    # Check postgres
    if docker exec taskflow-postgres pg_isready -U taskflow > /dev/null 2>&1; then
        echo -e "  PostgreSQL:  ${GREEN}✓ Healthy${NC}"
    else
        echo -e "  PostgreSQL:  ${RED}✗ Unhealthy${NC}"
    fi

    # Check redis
    if docker exec taskflow-redis redis-cli ping > /dev/null 2>&1; then
        echo -e "  Redis:       ${GREEN}✓ Healthy${NC}"
    else
        echo -e "  Redis:       ${RED}✗ Unhealthy${NC}"
    fi

    # Check minio
    if curl -sf http://localhost:9000/minio/health/live > /dev/null 2>&1; then
        echo -e "  MinIO:       ${GREEN}✓ Healthy${NC}"
    else
        echo -e "  MinIO:       ${RED}✗ Unhealthy${NC}"
    fi
}

# Backup database
backup() {
    local backup_file="backup_$(date +%Y%m%d_%H%M%S).sql"
    log_info "Creating database backup: $backup_file"
    docker exec taskflow-postgres pg_dump -U taskflow taskflow_db > $backup_file
    log_info "Backup created: $backup_file"
}

# Show help
help() {
    echo "TaskFlow Backend Deployment Script"
    echo ""
    echo "Usage: ./deploy.sh [command]"
    echo ""
    echo "Commands:"
    echo "  setup    - Initial server setup (install Docker, Docker Compose)"
    echo "  start    - Build and start all services"
    echo "  stop     - Stop all services"
    echo "  restart  - Restart all services"
    echo "  logs     - View logs (optionally specify service: logs backend)"
    echo "  ssl      - Setup SSL certificate with Let's Encrypt"
    echo "  status   - Check status of all services"
    echo "  backup   - Create database backup"
    echo "  help     - Show this help message"
}

# Main
case "${1:-help}" in
    setup)   setup ;;
    start)   start ;;
    stop)    stop ;;
    restart) restart ;;
    logs)    logs $2 ;;
    ssl)     ssl ;;
    status)  status ;;
    backup)  backup ;;
    help)    help ;;
    *)       log_error "Unknown command: $1"; help; exit 1 ;;
esac
