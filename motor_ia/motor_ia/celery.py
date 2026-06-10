import os
from celery import Celery

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'motor_ia.settings')
app = Celery('motor_ia')
app.config_from_object('django.conf:settings', namespace='CELERY')
app.autodiscover_tasks()
