"""
views.py -- Vista REST para traduccion visual de texto (CU-08).

Recibe una imagen por HTTP (multipart/form-data), ejecuta OCR con
EasyOCR para detectar texto, lo traduce con GoogleTranslator y devuelve
un JSON con los textos detectados, traducciones y coordenadas.

Endpoint: POST /api/traducir-imagen/
"""

import os
import tempfile

from django.conf import settings
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

from .model_loader import IDIOMAS_SOPORTADOS
from .services import procesar_traduccion

# Tipos MIME de imagen permitidos
TIPOS_IMAGEN_VALIDOS = {
    'image/jpeg', 'image/png', 'image/bmp', 'image/webp',
    'image/tiff', 'image/gif',
}


import uuid

def _guardar_imagen_temporal(uploaded_file) -> str:
    """Guarda una imagen subida en un archivo temporal seguro y retorna su ruta.

    Args:
        uploaded_file: Objeto UploadedFile de Django.

    Returns:
        Ruta absoluta del archivo temporal.
    """
    directorio_tmp = os.path.join(settings.MEDIA_ROOT, 'traductor', 'tmp')
    os.makedirs(directorio_tmp, exist_ok=True)
    
    # Generar un nombre seguro (UUID) para evitar bugs de OpenCV en Windows
    # al intentar leer rutas con caracteres como ñ, tildes, etc.
    _, extension = os.path.splitext(uploaded_file.name)
    nombre_seguro = f"{uuid.uuid4().hex}{extension}"
    ruta = os.path.join(directorio_tmp, nombre_seguro)
    
    with open(ruta, 'wb') as destino:
        for chunk in uploaded_file.chunks():
            destino.write(chunk)
    return ruta


@csrf_exempt
def traducir_imagen_view(request) -> JsonResponse:
    """Detecta y traduce texto en una imagen usando OCR + NLP.

    Recibe archivos por ``multipart/form-data`` y parametros opcionales:

    - ``imagen`` (File, requerido): Imagen con texto a traducir.
    - ``idioma_origen`` (str, opcional): Idioma del texto en la imagen (default 'es').
    - ``idioma_destino`` (str, opcional): Idioma al que traducir (default 'en').

    Idiomas soportados: es (espanol), en (ingles), it (italiano),
                        fr (frances), de (aleman).

    Returns:
        JsonResponse con detecciones, traducciones y coordenadas,
        o un mensaje de error con el codigo HTTP apropiado.
    """
    # -- Validar metodo HTTP ------------------------------------------------
    if request.method != 'POST':
        return JsonResponse(
            {'error': 'Metodo no permitido. Usa POST.'},
            status=405,
        )

    # -- Validar presencia de imagen ----------------------------------------
    if 'imagen' not in request.FILES:
        return JsonResponse(
            {'error': 'Falta el archivo "imagen" en el formulario.'},
            status=400,
        )

    archivo_imagen = request.FILES['imagen']

    # -- Validar tipo MIME --------------------------------------------------
    if archivo_imagen.content_type not in TIPOS_IMAGEN_VALIDOS:
        return JsonResponse(
            {
                'error': f'Tipo de imagen no soportado: {archivo_imagen.content_type}. '
                         f'Tipos validos: {", ".join(sorted(TIPOS_IMAGEN_VALIDOS))}',
            },
            status=400,
        )

    # -- Leer parametros de idioma ------------------------------------------
    idioma_origen = request.POST.get('idioma_origen', 'es').strip().lower()
    idioma_destino = request.POST.get('idioma_destino', 'en').strip().lower()

    if idioma_origen not in IDIOMAS_SOPORTADOS:
        return JsonResponse(
            {
                'error': f"Idioma de origen '{idioma_origen}' no soportado. "
                         f"Validos: {list(IDIOMAS_SOPORTADOS.keys())}",
            },
            status=400,
        )
    if idioma_destino not in IDIOMAS_SOPORTADOS:
        return JsonResponse(
            {
                'error': f"Idioma de destino '{idioma_destino}' no soportado. "
                         f"Validos: {list(IDIOMAS_SOPORTADOS.keys())}",
            },
            status=400,
        )
    if idioma_origen == idioma_destino:
        return JsonResponse(
            {'error': 'El idioma de origen y destino no pueden ser iguales.'},
            status=400,
        )

    # -- Guardar imagen temporal --------------------------------------------
    try:
        ruta_imagen = _guardar_imagen_temporal(archivo_imagen)
    except OSError as e:
        return JsonResponse(
            {'error': f'Error al guardar la imagen: {e}'},
            status=500,
        )

    # -- Invocar servicio de IA ---------------------------------------------
    try:
        resultado = procesar_traduccion(
            ruta_imagen=ruta_imagen,
            idioma_origen=idioma_origen,
            idioma_destino=idioma_destino,
        )
    except ValueError as e:
        return JsonResponse({'error': str(e)}, status=400)
    except FileNotFoundError as e:
        return JsonResponse({'error': str(e)}, status=400)
    except RuntimeError as e:
        return JsonResponse({'error': str(e)}, status=500)
    finally:
        # Limpiar archivo temporal
        try:
            os.remove(ruta_imagen)
        except OSError:
            pass

    # -- Respuesta ----------------------------------------------------------
    if resultado['total_detecciones'] == 0:
        return JsonResponse(
            {
                'exito': False,
                'mensaje': 'No se detecto texto en la imagen con suficiente confianza.',
                'idioma_origen': idioma_origen,
                'idioma_destino': idioma_destino,
                'total_detecciones': 0,
                'detecciones': [],
            },
            status=422,
        )

    return JsonResponse({
        'exito': True,
        'mensaje': 'Texto detectado y traducido exitosamente.',
        **resultado,
    })
