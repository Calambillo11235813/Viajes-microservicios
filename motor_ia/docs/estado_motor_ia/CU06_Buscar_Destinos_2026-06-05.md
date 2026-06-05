# CU06 — Buscar destinos mediante imágenes

**Fecha:** 2026-06-05  
**Estado:** Implementado y probado localmente

---

## Objetivo

Recibir una imagen del usuario, clasificarla con una CNN y devolver el destino turístico reconocido con su nivel de confianza.

---

## Endpoint

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| URL | `http://127.0.0.1:8000/api/predict/` |
| Body | `form-data`, campo `imagen` (tipo File) |
| Umbral de confianza | 75% |

### Respuesta exitosa

```json
{
  "reconocido": true,
  "destino": "Uyuni",
  "confianza": 0.92
}
```

### Sin reconocimiento

```json
{
  "reconocido": false,
  "mensaje": "La imagen no corresponde a ningún destino conocido.",
  "confianza_maxima": 0.45
}
```

---

## Modelo

- **Arquitectura:** MobileNetV2 (transfer learning)
- **Archivos:** `modelo_destinos_final.keras`, `class_names_final.pkl`
- **Clases:** `Cristo_ConCordia`, `Samaipata`, `Uyuni`
- **Carga:** Singleton en `model_loader.py`, precargado al arrancar Django vía `apps.py`

---

## Estructura de archivos

```
deeplearning/CU06_buscar_destinos/
├── api_destinos/
│   ├── apps.py
│   ├── model_loader.py
│   ├── urls.py
│   └── views.py
└── model_files/
    ├── class_names_final.pkl
    └── modelo_destinos_final.keras
```

---

## Configuración Django

- `motor_ia/settings.py` — app registrada como `api_destinos.apps.ApiDestinosConfig`; ruta `CU06_buscar_destinos` en `sys.path`
- `motor_ia/urls.py` — ruta `api/predict/` → `api_destinos.urls`

---

## Trabajo realizado (2026-06-05)

1. **Corrección de arranque:** `CU-06_buscar_destinos` no era importable en Python (guion inválido). Se creó la app `api_destinos` con nombre válido.
2. **Organización:** Código movido a `deeplearning/CU06_buscar_destinos/api_destinos/`.
3. **Limpieza:** Eliminados archivos duplicados en la raíz del caso de uso (`views.py`, `urls.py`, `model_loader.py`, `apps.py`).
4. **Rutas:** Corregida duplicación `/api/predict/api/predict/`; endpoint final en `/api/predict/`.
5. **Verificación:** `manage.py check` sin errores; modelo carga al iniciar el servidor; prueba manual con Postman.

---

## Librerías utilizadas

- Django 5.2, Django REST Framework
- TensorFlow / Keras
- NumPy, Pillow

---

## Pendientes / mejoras futuras

- Integración con Microservicio A (app móvil / backend principal)
