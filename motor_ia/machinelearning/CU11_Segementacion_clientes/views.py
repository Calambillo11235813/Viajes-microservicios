import json
import numpy as np
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .apps import ApiSegmentacionConfig

@csrf_exempt
def segmentar_usuario(request):
    """
    Endpoint POST: Recibe características del usuario o id_usuario (opcional)
    y devuelve el cluster asignado.
    
    Ejemplo de body:
    {
        "total_gastado": 5000.0,
        "num_reservas": 15,
        "rutas_distintas": 10,
        "promedio_pasajeros": 2.0
    }
    
    O si tienes los datos en tu base de datos, puedes enviar solo id_usuario
    y este endpoint consultaría la BD (requiere lógica adicional).
    """
    if request.method != 'POST':
        return JsonResponse({'error': 'Método no permitido. Use POST.'}, status=405)
    
    # Verificar que los modelos estén cargados
    ApiSegmentacionConfig.load_models()
    if (ApiSegmentacionConfig.kmeans_model is None or 
        ApiSegmentacionConfig.scaler is None or
        ApiSegmentacionConfig.features is None):
        return JsonResponse({'error': 'Modelos no cargados'}, status=503)
    
    try:
        data = json.loads(request.body)
        
        # Extraer características (deben existir todas)
        features_required = ApiSegmentacionConfig.features
        features_values = []
        for f in features_required:
            if f not in data:
                return JsonResponse({'error': f'Falta característica: {f}'}, status=400)
            features_values.append(float(data[f]))
        
        # Convertir a array 2D y escalar
        X = np.array([features_values])
        X_scaled = ApiSegmentacionConfig.scaler.transform(X)
        
        # Predecir cluster
        cluster = int(ApiSegmentacionConfig.kmeans_model.predict(X_scaled)[0])
        
        return JsonResponse({
            'status': 'success',
            'cluster': cluster,
            'caracteristicas': dict(zip(features_required, features_values))
        }, status=200)
        
    except json.JSONDecodeError:
        return JsonResponse({'error': 'JSON inválido'}, status=400)
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)


@csrf_exempt
def estadisticas_clusters(request):
    """
    Endpoint GET: Devuelve estadísticas de los clusters (cantidad de usuarios,
    centroides en valores originales) para dashboards del gerente.
    
    NOTA: Este endpoint requiere que se haya ejecutado el clustering previamente
    y se haya guardado la asignación de clusters por usuario.
    Si no tienes esa asignación persistente, puedes calcularla bajo demanda
    (pero sería costoso). Por simplicidad, devolvemos los centroides guardados
    y sugerimos que la asignación se calcule en el momento de consulta.
    """
    if request.method != 'GET':
        return JsonResponse({'error': 'Método no permitido. Use GET.'}, status=405)
    
    ApiSegmentacionConfig.load_models()
    if ApiSegmentacionConfig.kmeans_model is None:
        return JsonResponse({'error': 'Modelos no cargados'}, status=503)
    
    try:
        # Obtener centroides en escala original (requiere el escalador)
        centroides_scaled = ApiSegmentacionConfig.kmeans_model.cluster_centers_
        centroides_original = ApiSegmentacionConfig.scaler.inverse_transform(centroides_scaled)
        
        clusters_info = []
        for i, centroide in enumerate(centroides_original):
            info = {
                'cluster': i,
                'centroide': dict(zip(ApiSegmentacionConfig.features, centroide.round(2).tolist()))
            }
            clusters_info.append(info)
        
        return JsonResponse({
            'status': 'success',
            'n_clusters': len(clusters_info),
            'clusters': clusters_info,
            'mensaje': 'Para asignación de usuarios a clusters, use /segmentar-usuario/ con POST'
        }, status=200)
        
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)