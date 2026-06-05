"""URLs para la app api_traductor (CU-08 Traducir Texto Mediante Imagen)."""

from django.urls import path

from . import views

app_name = 'api_traductor'

urlpatterns = [
    path('', views.traducir_imagen_view, name='traducir_imagen'),
]
