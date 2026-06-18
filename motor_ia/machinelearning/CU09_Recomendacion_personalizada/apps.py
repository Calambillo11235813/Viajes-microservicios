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
        pass