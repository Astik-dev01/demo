# Multi-stage Dockerfile for Next.js production deployment

# Stage 1: Dependencies
FROM node:20-alpine AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app

# Copy package files from frontend directory
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci

# Stage 2: Builder
FROM node:20-alpine AS builder
WORKDIR /app

# Copy dependencies from deps stage
COPY --from=deps /app/node_modules ./node_modules
# Copy all frontend source code
COPY frontend/ .

# Set environment variables for build
ENV NEXT_TELEMETRY_DISABLED=1
ENV NODE_ENV=production

# Build-time environment variables (required for Next.js build)
ARG NEXT_PUBLIC_API_URL=https://fs-api-test.kgdigit.tech/api
ARG NEXT_PUBLIC_SOCKET_URL=wss://fs-api-test.kgdigit.tech
ARG NEXT_PUBLIC_JWT_SECRET=default_jwt_secret_for_build_only_replace_in_production_minimum_32_chars

ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV NEXT_PUBLIC_SOCKET_URL=${NEXT_PUBLIC_SOCKET_URL}
ENV NEXT_PUBLIC_JWT_SECRET=${NEXT_PUBLIC_JWT_SECRET}

# Build the application
RUN npm run build

# Stage 3: Runner
FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# Create non-root user
RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

# Install curl for health checks
RUN apk add --no-cache curl

# Copy necessary files for standalone build
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

# Set correct permissions
RUN chown -R nextjs:nodejs /app

USER nextjs

# Аргументы для портов
ARG EXTERNAL_PORT=3000
ARG INTERNAL_PORT=3000

# Expose internal port (где Next.js запускается)
EXPOSE ${INTERNAL_PORT}

# Environment variables для портов
ENV EXTERNAL_PORT=${EXTERNAL_PORT}
ENV INTERNAL_PORT=${INTERNAL_PORT}
ENV PORT=${INTERNAL_PORT}
ENV HOSTNAME="0.0.0.0"

# Start the application
CMD ["node", "server.js"]