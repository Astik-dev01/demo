# Multi-stage Dockerfile for Next.js production deployment

# Stage 1: Dependencies
FROM node:20-alpine AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app

# Copy package files from frontend directory
COPY frontend/package.json frontend/package-lock.json* ./

# Install ALL dependencies (including devDependencies for build)
RUN npm ci --include=dev

# Stage 2: Builder
FROM node:20-alpine AS builder
WORKDIR /app

# Copy dependencies from deps stage
COPY --from=deps /app/node_modules ./node_modules

# Copy all frontend source code
COPY frontend/ .

# Set environment variables for build
ENV NEXT_TELEMETRY_DISABLED=1

# Build-time environment variables (required for Next.js build)
ARG NEXT_PUBLIC_API_URL=https://demo-api-test.kgdigit.tech/api
ARG NEXT_PUBLIC_SOCKET_URL=wss://demo-api-test.kgdigit.tech
ARG NEXT_PUBLIC_STORAGE_URL=https://demo-api-test.kgdigit.tech/files
ARG NEXT_PUBLIC_UPLOAD_URL=https://demo-api-test.kgdigit.tech/api/files

ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV NEXT_PUBLIC_SOCKET_URL=${NEXT_PUBLIC_SOCKET_URL}
ENV NEXT_PUBLIC_STORAGE_URL=${NEXT_PUBLIC_STORAGE_URL}
ENV NEXT_PUBLIC_UPLOAD_URL=${NEXT_PUBLIC_UPLOAD_URL}

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

# Port configuration
ARG EXTERNAL_PORT=3000
ARG INTERNAL_PORT=3000

EXPOSE ${INTERNAL_PORT}

ENV EXTERNAL_PORT=${EXTERNAL_PORT}
ENV INTERNAL_PORT=${INTERNAL_PORT}
ENV PORT=${INTERNAL_PORT}
ENV HOSTNAME="0.0.0.0"

# Start the application
CMD ["node", "server.js"]
