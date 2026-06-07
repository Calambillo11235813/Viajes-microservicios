from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .apps import Cu09RecomendacionPersonalizadaConfig
import json
import numpy as np

@csrf_exempt
def predecir_ruta_recomendada(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)
            perfil = data.get('perfil_pasajero')
            categoria = data.get('categoria_preferida')
            monto = float(data.get('monto_total_pagado', 0.0))
            # Nueva variable: cantidad de pasajeros (obligatoria, con default 1)
            cantidad = int(data.get('cantidad_pasajeros', 1))

            # Validaciones rápidas
            if not perfil or not categoria:
                return JsonResponse({'status': 'error', 'mensaje': 'Faltan campos: perfil_pasajero y/o categoria_preferida'}, status=400)

            # Verificar que los modelos estén cargados
            if (Cu09RecomendacionPersonalizadaConfig.modelo_rf is None or
                Cu09RecomendacionPersonalizadaConfig.le_perfil is None or
                Cu09RecomendacionPersonalizadaConfig.le_categoria is None):
                return JsonResponse({'status': 'error', 'mensaje': 'Modelos de IA no cargados. Contacte al administrador.'}, status=503)

            # Transformar textos a números
            try:
                perfil_num = Cu09RecomendacionPersonalizadaConfig.le_perfil.transform([perfil])[0]
                categoria_num = Cu09RecomendacionPersonalizadaConfig.le_categoria.transform([categoria])[0]
            except ValueError as e:
                return JsonResponse({'status': 'error', 'mensaje': f'Categoría o Perfil no reconocido: {str(e)}'}, status=400)

            # Vector de entrada con 4 características (igual que el entrenamiento)
            nuevo_pasajero = np.array([[perfil_num, categoria_num, monto, cantidad]])
            ruta_recomendada = Cu09RecomendacionPersonalizadaConfig.modelo_rf.predict(nuevo_pasajero)[0]

            # (Opcional) Obtener probabilidades
            probabilidades = Cu09RecomendacionPersonalizadaConfig.modelo_rf.predict_proba(nuevo_pasajero)[0]
            top_indices = np.argsort(probabilidades)[-3:][::-1]
            top_rutas = [int(Cu09RecomendacionPersonalizadaConfig.modelo_rf.classes_[i]) for i in top_indices]
            top_probabilidades = [float(probabilidades[i]) for i in top_indices]

            return JsonResponse({
                'status': 'success',
                'modulo': 'CU-09 Recomendacion Personalizada',
                'datos_recibidos': data,
                'prediccion_id_ruta': int(ruta_recomendada),
                'top_3_rutas': top_rutas,
                'top_3_probabilidades': top_probabilidades
            }, status=200)

        except json.JSONDecodeError:
            return JsonResponse({'status': 'error', 'mensaje': 'Formato JSON inválido'}, status=400)
        except Exception as e:
            return JsonResponse({'status': 'error', 'mensaje': str(e)}, status=500)

    return JsonResponse({'mensaje': 'Método no permitido. Use POST.'}, status=405)