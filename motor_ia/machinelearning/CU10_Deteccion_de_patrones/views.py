import json
import os
from django.conf import settings
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

@csrf_exempt
def reglas_asociacion(request):
    if request.method == 'GET':
        json_path = os.path.join(
            os.path.dirname(os.path.abspath(__file__)),
            'api_reglas',
            'reglas_asociacion_final.json',
        )
        try:
            with open(json_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            return JsonResponse({'status': 'success', 'total': len(data), 'reglas': data}, status=200)
        except FileNotFoundError:
            return JsonResponse({'status': 'error', 'mensaje': 'Archivo de reglas no encontrado'}, status=404)
    return JsonResponse({'error': 'Método no permitido'}, status=405)