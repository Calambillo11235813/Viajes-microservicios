from django.urls import path
from . import views

urlpatterns = [
    path('api/v1/recomendar-ruta/', views.predecir_ruta_recomendada, name='recomendar_ruta'),
]