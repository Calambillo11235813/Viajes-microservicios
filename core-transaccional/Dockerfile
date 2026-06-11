# ============= STAGE 1: Build =============
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de Gradle (cachea dependencias en capa separada)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Dar permisos al wrapper y descargar dependencias (capa cacheada)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copiar el código fuente
COPY src/ src/

# Compilar el JAR (sin ejecutar tests)
RUN ./gradlew bootJar --no-daemon -x test

# ============= STAGE 2: Runtime =============
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Copiar el JAR desde el builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Cambiar a usuario no-root
USER spring:spring

# Puerto por defecto (sobreescribible con SERVER_PORT en .env)
EXPOSE 9090

# Healthcheck usando Actuator
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${SERVER_PORT:-9090}/actuator/health || exit 1

# Ejecutar con configuraciones optimizadas para contenedores
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
