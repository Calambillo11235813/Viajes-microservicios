import io

import numpy as np
import tensorflow as tf
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from PIL import Image

from .model_loader import clasificador


def preprocesar_imagen(imagen_bytes, target_size=(224, 224)):
    """Convierte bytes de imagen a array listo para MobileNetV2."""
    try:
        img = Image.open(io.BytesIO(imagen_bytes)).convert('RGB')
        img = img.resize(target_size)
        img_array = np.array(img)
        img_array = np.expand_dims(img_array, axis=0)
        img_array = tf.keras.applications.mobilenet_v2.preprocess_input(img_array)
        return img_array
    except Exception as e:
        raise ValueError(f"Error al procesar la imagen: {e}") from e


@csrf_exempt
def predecir_destino(request):
    if request.method != 'POST':
        return JsonResponse({'error': 'Método no permitido. Usa POST.'}, status=405)

    if 'imagen' not in request.FILES:
        return JsonResponse({'error': 'Falta el archivo "imagen".'}, status=400)

    imagen_file = request.FILES['imagen']
    if not imagen_file.content_type.startswith('image/'):
        return JsonResponse({'error': 'El archivo debe ser una imagen.'}, status=400)

    try:
        imagen_bytes = imagen_file.read()
        img_array = preprocesar_imagen(imagen_bytes)
        clase, confianza = clasificador.predict(img_array)

        umbral = 0.75
        if confianza < umbral:
            return JsonResponse({
                'reconocido': False,
                'mensaje': 'La imagen no corresponde a ningún destino conocido.',
                'confianza_maxima': confianza,
            })

        return JsonResponse({
            'reconocido': True,
            'destino': clase,
            'confianza': confianza,
        })
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)
