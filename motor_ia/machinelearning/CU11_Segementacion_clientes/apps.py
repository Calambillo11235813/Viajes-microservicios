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
        pass