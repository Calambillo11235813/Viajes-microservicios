from django.apps import AppConfig
import joblib
import os

class Cu09RecomendacionPersonalizadaConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'machinelearning.CU09_Recomendacion_personalizada'

    modelo_rf = None
    le_perfil = None
    le_categoria = None

    def ready(self):
        ruta_modelos = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'model_files')

        try:
            cls = type(self)
            # Nombres corregidos para que coincidan con los archivos reales
            cls.modelo_rf = joblib.load(os.path.join(ruta_modelos, 'modelo_recomendador_rutas.pkl'))
            cls.le_perfil = joblib.load(os.path.join(ruta_modelos, 'le_perfil.pkl'))
            cls.le_categoria = joblib.load(os.path.join(ruta_modelos, 'le_categoria.pkl'))
            print("[CU09] Modelo Random Forest y encoders cargados correctamente desde model_files/")
        except Exception as e:
            print(f"[CU09] ERROR al cargar los modelos: {e}")