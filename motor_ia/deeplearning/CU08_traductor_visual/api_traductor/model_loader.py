"""
model_loader.py -- Singleton para el lector OCR de EasyOCR (CU-08).

Carga el modelo de reconocimiento optico de caracteres (OCR) basado en
Deep Learning una unica vez al arrancar Django. EasyOCR utiliza internamente
dos redes neuronales:
    - CRAFT (Character Region Awareness For Text detection): detecta regiones
      de texto en la imagen.
    - CRNN (Convolutional Recurrent Neural Network): reconoce los caracteres
      dentro de cada region detectada.

Idiomas soportados: espanol (es), ingles (en), italiano (it), frances (fr),
                    aleman (de).
"""

import easyocr

# Idiomas soportados por la plataforma de viajes
IDIOMAS_SOPORTADOS = {
    'es': 'Espanol',
    'en': 'Ingles',
    'it': 'Italiano',
    'fr': 'Frances',
    'de': 'Aleman',
}


class LectorOCR:
    """Singleton que carga EasyOCR y mantiene readers cacheados por idioma.

    EasyOCR necesita un Reader por cada combinacion de idiomas. Para evitar
    recargar modelos en cada peticion, se cachean los readers ya creados.

    Attributes:
        _readers: Diccionario que mapea idiomas a instancias de easyocr.Reader.

    Example:
        >>> from .model_loader import lector_ocr
        >>> resultados = lector_ocr.detectar_texto('ruta/imagen.jpg', 'es')
    """

    _instance = None

    def __new__(cls) -> "LectorOCR":
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._readers: dict[str, easyocr.Reader] = {}
        return cls._instance

    def _obtener_reader(self, idioma: str) -> easyocr.Reader:
        """Obtiene o crea un Reader de EasyOCR para el idioma especificado.

        Args:
            idioma: Codigo de idioma (ej: 'es', 'en', 'fr').

        Returns:
            Instancia de easyocr.Reader configurada para el idioma.

        Raises:
            ValueError: Si el idioma no esta soportado.
        """
        if idioma not in IDIOMAS_SOPORTADOS:
            raise ValueError(
                f"Idioma '{idioma}' no soportado. "
                f"Idiomas validos: {list(IDIOMAS_SOPORTADOS.keys())}"
            )

        if idioma not in self._readers:
            print(f"[CU-08] Cargando modelo OCR para idioma: {IDIOMAS_SOPORTADOS[idioma]}...")
            self._readers[idioma] = easyocr.Reader(
                [idioma],
                gpu=False,
                verbose=False,
            )
            print(f"[CU-08] Modelo OCR para {IDIOMAS_SOPORTADOS[idioma]} cargado.")

        return self._readers[idioma]

    def detectar_texto(self, ruta_imagen: str, idioma: str) -> list[tuple]:
        """Ejecuta OCR sobre una imagen y retorna las detecciones crudas.

        Args:
            ruta_imagen: Ruta absoluta a la imagen a procesar.
            idioma: Codigo del idioma del texto en la imagen.

        Returns:
            Lista de tuplas (bounding_box, texto, confianza) de EasyOCR.
        """
        reader = self._obtener_reader(idioma)
        return reader.readtext(ruta_imagen)


# Instancia global (Singleton)
lector_ocr = LectorOCR()
