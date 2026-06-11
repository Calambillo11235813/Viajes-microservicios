"""
views.py — Vista REST para generación de reels turísticos (CU-07).

Recibe un video y un audio por HTTP (multipart/form-data), invoca el
servicio de IA para seleccionar los mejores fragmentos y devuelve un
JSON con los metadatos del reel generado junto con la URL de descarga.

Endpoint: POST /api/generar-reel/
"""

import os

from django.conf import settings
from django.core.files.storage import default_storage
from django.core.files.base import ContentFile
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

from .services import generar_reel

# Tipos MIME permitidos
TIPOS_VIDEO_VALIDOS = {
    'video/mp4', 'video/mpeg', 'video/quicktime', 'video/x-msvideo',
    'video/x-matroska', 'video/webm',
}
TIPOS_AUDIO_VALIDOS = {
    'audio/mpeg', 'audio/mp3', 'audio/wav', 'audio/ogg',
    'audio/x-wav', 'audio/aac',
}


def _guardar_archivo_temporal(uploaded_file, directorio: str) -> str:
    """Guarda un archivo subido en disco y retorna su ruta absoluta.

    Args:
        uploaded_file: Objeto UploadedFile de Django.
        directorio: Directorio donde guardar el archivo temporal.

    Returns:
        Ruta absoluta del archivo guardado.
    """
    os.makedirs(directorio, exist_ok=True)
    ruta = os.path.join(directorio, uploaded_file.name)
    with open(ruta, 'wb') as destino:
        for chunk in uploaded_file.chunks():
            destino.write(chunk)
    return ruta


@csrf_exempt
def generar_reel_view(request) -> JsonResponse:
    """Genera un reel turístico a partir de un video y una pista de audio.

    Recibe archivos por ``multipart/form-data`` y parámetros opcionales:

    - ``video`` (File, requerido): Video largo de entrada.
    - ``audio`` (File, requerido): Pista musical para el reel.
    - ``duracion_reel`` (int, opcional): Duración deseada en segundos (default 60).
    - ``duracion_clip`` (int, opcional): Duración de cada fragmento (default 10).

    Returns:
        JsonResponse con metadatos del reel generado y URL de descarga,
        o un mensaje de error con el código HTTP apropiado.
    """
    # ── Validar método HTTP ──────────────────────────────────────────
    if request.method != 'POST':
        return JsonResponse(
            {'error': 'Método no permitido. Usa POST.'},
            status=405,
        )

    # ── Validar presencia de archivos ────────────────────────────────
    if 'video' not in request.FILES:
        return JsonResponse(
            {'error': 'Falta el archivo "video" en el formulario.'},
            status=400,
        )
    if 'audio' not in request.FILES:
        return JsonResponse(
            {'error': 'Falta el archivo "audio" en el formulario.'},
            status=400,
        )

    archivo_video = request.FILES['video']
    archivo_audio = request.FILES['audio']

    # ── Validar tipos MIME ───────────────────────────────────────────
    if archivo_video.content_type not in TIPOS_VIDEO_VALIDOS:
        return JsonResponse(
            {
                'error': f'Tipo de video no soportado: {archivo_video.content_type}. '
                         f'Tipos válidos: {", ".join(sorted(TIPOS_VIDEO_VALIDOS))}',
            },
            status=400,
        )
    if archivo_audio.content_type not in TIPOS_AUDIO_VALIDOS:
        return JsonResponse(
            {
                'error': f'Tipo de audio no soportado: {archivo_audio.content_type}. '
                         f'Tipos válidos: {", ".join(sorted(TIPOS_AUDIO_VALIDOS))}',
            },
            status=400,
        )

    # ── Leer parámetros opcionales ───────────────────────────────────
    try:
        duracion_reel = int(request.POST.get('duracion_reel', 60))
        duracion_clip = int(request.POST.get('duracion_clip', 10))
    except (ValueError, TypeError):
        return JsonResponse(
            {'error': 'Los parámetros duracion_reel y duracion_clip deben ser enteros.'},
            status=400,
        )

    if duracion_reel not in (15, 30, 45, 60):
        return JsonResponse(
            {'error': 'duracion_reel debe ser 15, 30, 45 o 60 segundos.'},
            status=400,
        )
    if duracion_clip < 1 or duracion_clip > duracion_reel:
        return JsonResponse(
            {'error': f'duracion_clip debe estar entre 1 y {duracion_reel}.'},
            status=400,
        )

    # ── Guardar archivos temporales ──────────────────────────────────
    directorio_tmp = os.path.join(settings.MEDIA_ROOT, 'reels', 'tmp')
    try:
        ruta_video = _guardar_archivo_temporal(archivo_video, directorio_tmp)
        ruta_audio = _guardar_archivo_temporal(archivo_audio, directorio_tmp)
    except OSError as e:
        return JsonResponse(
            {'error': f'Error al guardar archivos temporales: {e}'},
            status=500,
        )

    # ── Invocar servicio de IA de manera SÍNCRONA ───────────────────
    try:
        resultado = generar_reel(
            ruta_video=ruta_video,
            ruta_audio=ruta_audio,
            directorio_salida=directorio_tmp,
            duracion_reel=duracion_reel,
            duracion_clip=duracion_clip,
        )
        
        # Guardar en Storage
        nombre_archivo = resultado['nombre_archivo']
        ruta_local_generada = resultado['ruta_archivo']

        with open(ruta_local_generada, 'rb') as f:
            video_bytes = f.read()
        
        path_en_storage = default_storage.save(f'reels/{nombre_archivo}', ContentFile(video_bytes))
        url_descarga = default_storage.url(path_en_storage)
        
        # Comentado a petición: no limpiamos el archivo local para poder verlo en la carpeta
        # os.remove(ruta_local_generada)

        return JsonResponse({
            'exito': True,
            'mensaje': 'Reel generado exitosamente.',
            'archivo_descarga': url_descarga,
            'duracion_reel': resultado['duracion_reel'],
            'clips_seleccionados': resultado['clips_seleccionados'],
            'clips_analizados': resultado['clips_analizados'],
            'tiempos_procesamiento': resultado.get('tiempos_procesamiento'),
        }, status=200)

    except Exception as e:
        return JsonResponse({'error': f'Error al generar el reel: {e}'}, status=500)
    finally:
        # Limpiar archivos temporales de entrada
        for ruta in [ruta_video, ruta_audio]:
            try:
                os.remove(ruta)
            except OSError:
                pass
