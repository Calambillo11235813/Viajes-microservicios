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
            
            # --- PARCHE WINDOWS ---
            # EasyOCR intenta imprimir una barra de progreso con el caracter '█' (\u2588)
            # al descargar modelos, lo que causa UnicodeEncodeError en consola Windows cp1252.
            # Sobrescribimos el hook de progreso para que sea silencioso.
            import easyocr.utils
            easyocr.utils.progress_hook = lambda count, blockSize, totalSize: None
            # ----------------------
            
            self._readers[idioma] = easyocr.Reader(
                [idioma],
                gpu=False,
                verbose=False,
            )
            print(f"[CU-08] Modelo OCR para {IDIOMAS_SOPORTADOS[idioma]} cargado.")

        return self._readers[idioma]

    def precargar(self, *idiomas: str) -> None:
        """Carga por adelantado los readers de los idiomas indicados.

        Pensado para llamarse al arrancar Django (en ``apps.py``) y asi evitar
        que la primera peticion de cada idioma pague el costo de cargar las
        redes neuronales de EasyOCR.

        Args:
            *idiomas: Codigos de idioma a precargar (ej: 'es', 'en').
        """
        for idioma in idiomas:
            if idioma in IDIOMAS_SOPORTADOS:
                self._obtener_reader(idioma)

    def detectar_texto(
        self, ruta_imagen: str, idioma: str, max_lado: int = 1600
    ) -> list[tuple]:
        """Ejecuta OCR sobre una imagen y retorna las detecciones crudas.

        Para acelerar la inferencia en CPU, la imagen se reduce si su lado
        mayor supera ``max_lado`` pixeles. Las coordenadas de los bounding
        boxes se reescalan al tamano original para no alterar el contrato de
        la API.

        Args:
            ruta_imagen: Ruta absoluta a la imagen a procesar.
            idioma: Codigo del idioma del texto en la imagen.
            max_lado: Lado mayor maximo (px) antes de redimensionar.

        Returns:
            Lista de tuplas (bounding_box, texto, confianza) de EasyOCR.
        """
        reader = self._obtener_reader(idioma)

        import cv2

        imagen = cv2.imread(ruta_imagen)
        if imagen is None:
            # Fallback: dejar que EasyOCR lea el archivo directamente.
            return reader.readtext(ruta_imagen)

        alto, ancho = imagen.shape[:2]
        lado_mayor = max(alto, ancho)
        escala = 1.0
        if lado_mayor > max_lado:
            escala = max_lado / lado_mayor
            nuevo_tam = (int(ancho * escala), int(alto * escala))
            imagen = cv2.resize(imagen, nuevo_tam, interpolation=cv2.INTER_AREA)

        resultados = reader.readtext(imagen)
        if escala == 1.0:
            return resultados

        # Reescalar las coordenadas al tamano original de la imagen.
        factor = 1.0 / escala
        resultados_reescalados = []
        for (caja, texto, confianza) in resultados:
            caja_original = [[coord * factor for coord in punto] for punto in caja]
            resultados_reescalados.append((caja_original, texto, confianza))
        return resultados_reescalados


# Instancia global (Singleton)
lector_ocr = LectorOCR()
