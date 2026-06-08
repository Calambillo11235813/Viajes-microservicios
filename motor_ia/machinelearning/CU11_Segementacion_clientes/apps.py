from django.apps import AppConfig
import joblib
import json
import os

class ApiSegmentacionConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'machinelearning.CU11_Segementacion_clientes'

    kmeans_model = None
    scaler = None
    features = None

    def ready(self):
        # Obtener la ruta de los archivos de modelo
        base_dir = os.path.dirname(os.path.abspath(__file__))
        model_dir = os.path.join(base_dir, 'model_files')
        
        try:
            cls = type(self)
            cls.kmeans_model = joblib.load(os.path.join(model_dir, 'kmeans_model.pkl'))
            cls.scaler = joblib.load(os.path.join(model_dir, 'kmeans_scaler.pkl'))
            with open(os.path.join(model_dir, 'kmeans_features.json'), 'r') as f:
                cls.features = json.load(f)
            print("[CU11] Modelo KMeans, escalador y features cargados correctamente")
        except Exception as e:
            print(f"[CU11] ERROR al cargar modelos: {e}")