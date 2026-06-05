"""
services.py -- Logica de negocio para traduccion visual (CU-08).

Pipeline:
1. Recibe la ruta de una imagen y los idiomas (origen/destino).
2. Ejecuta OCR con EasyOCR para detectar textos en la imagen.
3. Filtra detecciones por umbral de confianza (>50%).
4. Traduce cada texto detectado con GoogleTranslator.
5. Extrae coordenadas de bounding box (top_left, bottom_right).
6. Retorna un diccionario con todas las detecciones.

Este modulo NO conoce HTTP ni Django; solo recibe rutas de archivos
y parametros, y devuelve un diccionario con los resultados.
"""

from typing import Any

from deep_translator import GoogleTranslator

from .model_loader import IDIOMAS_SOPORTADOS, lector_ocr

# Umbral minimo de confianza para considerar una deteccion valida
UMBRAL_CONFIANZA = 0.5


def procesar_traduccion(
    ruta_imagen: str,
    idioma_origen: str = 'es',
    idioma_destino: str = 'en',
) -> dict[str, Any]:
    """Detecta texto en una imagen con OCR y lo traduce al idioma destino.

    Pipeline:
        1. Valida los idiomas de origen y destino.
        2. Ejecuta EasyOCR sobre la imagen para detectar texto.
        3. Filtra detecciones con confianza > 50%.
        4. Traduce cada texto con GoogleTranslator.
        5. Extrae coordenadas de bounding box para la app movil.

    Args:
        ruta_imagen: Ruta absoluta a la imagen a procesar.
        idioma_origen: Codigo del idioma del texto en la imagen (ej: 'es').
        idioma_destino: Codigo del idioma al que traducir (ej: 'en').

    Returns:
        Diccionario con:
            - ``idioma_origen``: Codigo del idioma de origen.
            - ``idioma_destino``: Codigo del idioma de destino.
            - ``total_detecciones``: Cantidad de textos detectados (filtrados).
            - ``detecciones``: Lista de dicts con texto, traduccion, confianza
              y coordenadas de cada deteccion.

    Raises:
        ValueError: Si un idioma no esta soportado o son iguales.
        FileNotFoundError: Si la imagen no existe.
        RuntimeError: Si ocurre un error durante el OCR o la traduccion.
    """
    # -- Validaciones -------------------------------------------------------
    if idioma_origen not in IDIOMAS_SOPORTADOS:
        raise ValueError(
            f"Idioma de origen '{idioma_origen}' no soportado. "
            f"Validos: {list(IDIOMAS_SOPORTADOS.keys())}"
        )
    if idioma_destino not in IDIOMAS_SOPORTADOS:
        raise ValueError(
            f"Idioma de destino '{idioma_destino}' no soportado. "
            f"Validos: {list(IDIOMAS_SOPORTADOS.keys())}"
        )
    if idioma_origen == idioma_destino:
        raise ValueError(
            f"El idioma de origen y destino no pueden ser iguales ('{idioma_origen}')."
        )

    # -- 1. Deteccion OCR con Deep Learning ---------------------------------
    import os
    if not os.path.exists(ruta_imagen):
        raise FileNotFoundError(f"Imagen no encontrada: {ruta_imagen}")

    try:
        resultados_ocr = lector_ocr.detectar_texto(ruta_imagen, idioma_origen)
    except Exception as e:
        raise RuntimeError(f"Error en el motor OCR: {e}") from e

    # -- 2. Filtrado por confianza y traduccion -----------------------------
    traductor = GoogleTranslator(source=idioma_origen, target=idioma_destino)
    detecciones: list[dict[str, Any]] = []

    for (caja_delimitadora, texto, confianza) in resultados_ocr:
        if confianza < UMBRAL_CONFIANZA:
            continue

        # Traducir el texto detectado
        try:
            traduccion = traductor.translate(texto)
        except Exception:
            traduccion = "[Error en traduccion]"

        # Extraer coordenadas de bounding box
        # caja_delimitadora tiene 4 esquinas: [top_left, top_right, bottom_right, bottom_left]
        puntos = [list(map(int, p)) for p in caja_delimitadora]
        top_left = puntos[0]
        bottom_right = puntos[2]

        detecciones.append({
            "texto_original": texto,
            "traduccion": traduccion if traduccion else texto,
            "confianza": round(float(confianza), 2),
            "coordenadas": {
                "top_left": top_left,
                "bottom_right": bottom_right,
            },
        })

    return {
        "idioma_origen": idioma_origen,
        "idioma_destino": idioma_destino,
        "total_detecciones": len(detecciones),
        "detecciones": detecciones,
    }
