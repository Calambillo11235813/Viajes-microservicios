"""URLs para la app api_reels (CU-07 Generar Reels Turísticos)."""

from django.urls import path

from . import views

app_name = 'api_reels'

urlpatterns = [
    path('', views.generar_reel_view, name='generar_reel'),
]
