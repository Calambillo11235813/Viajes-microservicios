"""Configuracion de la app api_traductor (CU-08 Traducir Texto Mediante Imagen)."""

from django.apps import AppConfig


class ApiTraductorConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'api_traductor'
    verbose_name = 'CU-08 Traducir Texto Mediante Imagen'

    def ready(self) -> None:
        """Importa el singleton para que este disponible al arrancar.

        Nota: A diferencia de CU06/CU07, NO precargamos los modelos OCR aqui
        porque EasyOCR carga modelos por idioma bajo demanda. Esto evita
        cargar modelos innecesarios al arrancar Django. El Singleton se crea
        pero los readers se cargan lazy en la primera peticion de cada idioma.
        """
        from .model_loader import lector_ocr  # noqa: F401
