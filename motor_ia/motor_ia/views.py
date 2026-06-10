from django.http import JsonResponse

def health_check(request):
    """
    Endpoint para balanceadores de carga y Kubernetes.
    Retorna 200 OK si el servicio está levantado.
    """
    return JsonResponse({"status": "ok"})
