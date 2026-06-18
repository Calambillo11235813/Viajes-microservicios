"""Configuración de la app api_reels (CU-07 Generar Reels Turísticos)."""

from django.apps import AppConfig


class ApiReelsConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'api_reels'
    verbose_name = 'CU-07 Generar Reels Turísticos'

    def ready(self) -> None:
        """Precarga el modelo MobileNetV2 al arrancar Django (Singleton)."""
        # from .model_loader import scorer  # noqa: F401 — carga el modelo al arrancar
        pass
