import os
from celery import shared_task
from django.conf import settings
from django.core.files.storage import default_storage
from django.core.files.base import ContentFile
from .services import generar_reel

@shared_task
def generar_reel_task(ruta_video, ruta_audio, duracion_reel, duracion_clip):
    """
    Tarea asíncrona que procesa el video para generar un reel turístico.
    """
    directorio_salida = os.path.join(settings.MEDIA_ROOT, 'reels', 'tmp')
    
    try:
        resultado = generar_reel(
            ruta_video=ruta_video,
            ruta_audio=ruta_audio,
            directorio_salida=directorio_salida,
            duracion_reel=duracion_reel,
            duracion_clip=duracion_clip,
        )
        
        # Guardar en Cloud Storage
        nombre_archivo = resultado['nombre_archivo']
        ruta_local_generada = resultado['ruta_archivo']

        with open(ruta_local_generada, 'rb') as f:
            video_bytes = f.read()
        
        path_en_storage = default_storage.save(f'reels/{nombre_archivo}', ContentFile(video_bytes))
        url_descarga = default_storage.url(path_en_storage)
        
        # Limpiar el archivo local generado
        os.remove(ruta_local_generada)
        
        # Retornar los datos del resultado
        return {
            'status': 'completed',
            'url': url_descarga,
            'duracion_reel': resultado['duracion_reel'],
            'clips_seleccionados': resultado['clips_seleccionados'],
            'tiempos_procesamiento': resultado.get('tiempos_procesamiento'),
        }

    except Exception as e:
        return {
            'status': 'failed',
            'error': str(e)
        }
    finally:
        # Limpiar archivos temporales de entrada
        for ruta in [ruta_video, ruta_audio]:
            try:
                os.remove(ruta)
            except OSError:
                pass

    from celery import shared_task

@shared_task
def test_task():
    return "OK"
