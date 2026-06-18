from django.apps import AppConfig
import joblib
import os

class Cu09RecomendacionPersonalizadaConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'machinelearning.CU09_Recomendacion_personalizada'

    modelo_rf = None
    le_perfil = None
    le_categoria = None

    @classmethod
    def load_models(cls):
        if cls.modelo_rf is not None:
            return
        ruta_modelos = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'model_files')
        try:
            cls.modelo_rf = joblib.load(os.path.join(ruta_modelos, 'modelo_recomendador_rutas.pkl'))
            cls.le_perfil = joblib.load(os.path.join(ruta_modelos, 'le_perfil.pkl'))
            cls.le_categoria = joblib.load(os.path.join(ruta_modelos, 'le_categoria.pkl'))
            print("[CU09] Modelos cargados correctamente (Lazy Load)")
        except Exception as e:
            print(f"[CU09] ERROR al cargar los modelos: {e}")

    def ready(self):
        pass