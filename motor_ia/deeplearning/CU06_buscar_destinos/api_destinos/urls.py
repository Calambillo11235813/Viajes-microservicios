from django.urls import path

from . import views

app_name = 'api_destinos'

urlpatterns = [
    path('', views.predecir_destino, name='predecir_destino'),
]
