# Etapa de compilación
FROM golang:alpine AS builder

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Instalar dependencias del sistema necesarias (git, certificados)
RUN apk add --no-cache git ca-certificates tzdata

# Copiar primero los archivos de dependencias para aprovechar la caché de Docker
COPY go.mod go.sum ./
RUN go mod download

# Copiar todo el código fuente del microservicio
COPY . .

# Compilar el binario del Worker
# CGO_ENABLED=0 asegura que el binario sea estático e independiente
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o worker ./cmd/worker

# ==========================================
# Etapa de Producción (Imagen final súper ligera)
FROM alpine:latest

WORKDIR /root/

# Instalar certificados CA para que las conexiones HTTPS (S3, Blockchain, Telegram) funcionen
RUN apk --no-cache add ca-certificates tzdata

# Copiar el binario desde la etapa de compilación
COPY --from=builder /app/worker .

# El contenedor ejecutará este comando por defecto
CMD ["./worker"]
