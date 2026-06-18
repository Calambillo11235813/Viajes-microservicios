from django.apps import AppConfig


class ApiDestinosConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'api_destinos'
    verbose_name = 'CU-06 Buscar Destinos'

    def ready(self):
        # from .model_loader import clasificador  # noqa: F401 — carga el modelo al arrancar
        pass
