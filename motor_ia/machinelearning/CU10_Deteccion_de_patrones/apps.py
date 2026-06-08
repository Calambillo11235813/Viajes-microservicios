import json
import os

from django.apps import AppConfig


class Cu10DeteccionPatronesViajeConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'machinelearning.CU10_Deteccion_de_patrones'

    reglas_asociacion = None

    def ready(self):
        json_path = os.path.join(
            os.path.dirname(os.path.abspath(__file__)),
            'model_files',
            'reglas_asociacion_final.json',
        )

        try:
            with open(json_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            type(self).reglas_asociacion = data
            print(
                f"[CU10] Reglas de asociación cargadas correctamente desde model_files/ "
                f"({len(data)} reglas)"
            )
        except Exception as e:
            print(f"[CU10] ERROR al cargar reglas de asociación: {e}")
