from django.urls import path
from . import views

urlpatterns = [
    path('segmentar-usuario/', views.segmentar_usuario, name='segmentar_usuario'),
    path('estadisticas-clusters/', views.estadisticas_clusters, name='estadisticas_clusters'),
]