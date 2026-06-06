"""Configuracion de la app api_traductor (CU-08 Traducir Texto Mediante Imagen)."""

from django.apps import AppConfig


class ApiTraductorConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'api_traductor'
    verbose_name = 'CU-08 Traducir Texto Mediante Imagen'

    def ready(self) -> None:
        """Precarga los modelos OCR mas usados al arrancar Django.

        Antes los readers se cargaban de forma perezosa (lazy) en la primera
        peticion de cada idioma, lo que hacia que esa primera traduccion
        tardara varios segundos extra mientras EasyOCR cargaba las redes
        CRAFT + CRNN. Aqui los precargamos para los idiomas mas comunes
        (espanol e ingles) y asi la primera peticion ya responde rapido.

        Se usa la guarda ``RUN_MAIN`` para evitar que el autoreloader de
        Django cargue los modelos dos veces: con ``runserver`` el proceso que
        realmente atiende las peticiones es el unico con ``RUN_MAIN == 'true'``
        (el proceso vigilante no tiene la variable). Si se ejecuta con
        ``--noreload``, los readers se cargaran de forma perezosa en la primera
        peticion.
        """
        import os

        from .model_loader import lector_ocr

        if os.environ.get('RUN_MAIN') != 'true':
            return

        try:
            lector_ocr.precargar('es', 'en')
        except Exception as e:  # pragma: no cover - defensivo en arranque
            print(f"[CU-08] No se pudieron precargar los modelos OCR: {e}")
