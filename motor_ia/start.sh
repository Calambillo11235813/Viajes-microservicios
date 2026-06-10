#!/bin/bash
# start.sh
# Inicia el worker de Celery en background y Gunicorn en foreground

echo "Iniciando worker de Celery..."
celery -A motor_ia worker --loglevel=info &

echo "Iniciando Gunicorn..."
exec gunicorn motor_ia.wsgi:application --bind 0.0.0.0:8080 --workers 1 --timeout 120
