# =============================================================================
# StreamFlix Frontend - Multi-stage Docker Build
# Base image: Node 20 Alpine (minimal, secure)
# =============================================================================

# Stage 1: Dependencies
FROM node:20-alpine AS deps
WORKDIR /app

# Copy package files
COPY package.json package-lock.json ./

# Install dependencies (production only for smaller image)
RUN npm ci --only=production && \
    npm cache clean --force

# Stage 2: Builder
FROM node:20-alpine AS builder
WORKDIR /app

# Copy dependencies from deps stage
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Install all dependencies (including devDependencies for build)
RUN npm ci

# Build the application
ARG VITE_API_BASE_URL
ARG VITE_CDN_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_CDN_URL=$VITE_CDN_URL

RUN npm run build

# Stage 3: Production
FROM nginx:1.27-alpine AS production

# Security: Run as non-root user
RUN addgroup -g 1001 -S streamflix && \
    adduser -u 1001 -S streamflix -G streamflix

# Copy custom nginx config
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Security: Remove unnecessary files and set permissions
RUN rm -rf /etc/nginx/conf.d/default.conf && \
    chown -R streamflix:streamflix /usr/share/nginx/html && \
    chown -R streamflix:streamflix /var/cache/nginx && \
    chown -R streamflix:streamflix /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown streamflix:streamflix /var/run/nginx.pid

# Switch to non-root user
USER streamflix

# Expose port 8080 (non-privileged)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
