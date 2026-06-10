"""
URL configuration for motor_ia project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.conf import settings
from django.conf.urls.static import static
from django.contrib import admin
from django.urls import include, path

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from motor_ia import views

@csrf_exempt
def home_view(request):
    return JsonResponse({
        "status": "ok",
        "message": "Motor de IA ejecutándose correctamente.",
        "endpoints": [
            "/api/predict/",
            "/api/generar-reel/",
            "/api/traducir-imagen/",
            "/api/recomendar-ruta/",
            "/api/reglas-asociacion/",
            "/api/segmentar-usuario/"
        ]
    })

urlpatterns = [
    path('', home_view),
    path('admin/', admin.site.urls),
    path('api/health/', views.health_check, name='health_check'),
    path('api/predict/', include('api_destinos.urls')),
    path('api/generar-reel/', include('api_reels.urls')),
    path('api/traducir-imagen/', include('api_traductor.urls')),
    path('api/recomendar-ruta/', include('machinelearning.CU09_Recomendacion_personalizada.urls')),
    path('api/reglas-asociacion/', include('machinelearning.CU10_Deteccion_de_patrones.urls')),
    path('api/segmentar-usuario/', include('machinelearning.CU11_Segementacion_clientes.urls')),
]

# Servir archivos media (reels generados) en modo desarrollo
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
