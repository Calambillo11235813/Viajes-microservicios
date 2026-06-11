# ============= STAGE 1: Build =============
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar archivos de dependencias (capa cacheada)
COPY package.json package-lock.json* ./

# Instalar dependencias
RUN npm ci

# Copiar el código fuente y archivos de configuración
COPY . .

# Variables de build: IP del Core Transaccional
# Se pasan con: docker build --build-arg CORE_IP=18.218.178.228 ...
ARG CORE_IP=localhost
ARG CORE_PORT=9090

# Generar el .env para que set-env.js lo lea durante el build
RUN echo "CORE_TRANSACCIONAL_IP=${CORE_IP}" > .env && \
    echo "CORE_TRANSACCIONAL_PORT=${CORE_PORT}" >> .env

# Generar environment.ts desde .env y compilar Angular (SSR)
RUN npm run build

# ============= STAGE 2: Runtime =============
FROM node:20-alpine

WORKDIR /app

# Crear usuario no-root
RUN addgroup -S angular && adduser -S angular -G angular

# Copiar solo los artefactos de producción
COPY --from=builder /app/dist/ ./dist/
COPY --from=builder /app/package.json ./

# Instalar solo dependencias de producción (express, @angular/ssr)
RUN npm ci --omit=dev 2>/dev/null || npm install --omit=dev

# Cambiar a usuario no-root
USER angular:angular

# Puerto del servidor SSR (Express)
ENV PORT=4000
EXPOSE 4000

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT}/login || exit 1

# Ejecutar el servidor SSR de Angular
CMD ["node", "dist/frontend-web-admin/server/server.mjs"]
