from django.urls import path
from . import views

urlpatterns = [
    path('', views.reglas_asociacion, name='reglas_asociacion'),
]