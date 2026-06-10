FROM python:3.11-slim

# Configurar variables de entorno de Python
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# Instalar dependencias del sistema requeridas por OpenCV y otras librerías
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Instalar PyTorch CPU explícitamente primero para asegurar versión optimizada
RUN pip install --no-cache-dir torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu

# Copiar requirements y hacer la instalación
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar el código del microservicio
COPY . .

# Configurar variables dummy para el comando check
ENV SECRET_KEY=dummy-key-for-build
ENV DEBUG=False
ENV ALLOWED_HOSTS=*

# "Calentar" los modelos de IA: al ejecutar check, se disparan los AppConfig.ready()
# que a su vez descargan/cargan los pesos de EasyOCR, MobileNet, etc.
RUN python manage.py check

# Copiar y dar permisos al script de inicio
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

EXPOSE 8080

# Ejecutar el servicio y worker
CMD ["/app/start.sh"]
