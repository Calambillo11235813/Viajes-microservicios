# CU08 -- Traducir Texto Mediante Imagen (OCR + NLP)

**Fecha:** 2026-06-05
**Estado:** Implementado y probado localmente

---

## Objetivo

Recibir una imagen del usuario (ej: foto de un menu, letrero o senaletica),
detectar el texto presente usando OCR basado en Deep Learning (EasyOCR),
traducirlo al idioma solicitado y devolver los textos con sus traducciones
y las coordenadas de ubicacion en la imagen (bounding boxes).

---

## Endpoint

| Campo | Valor |
|-------|-------|
| Metodo | `POST` |
| URL | `http://127.0.0.1:8000/api/traducir-imagen/` |
| Body | `multipart/form-data`, campo `imagen` (File) |
| Parametros opcionales | `idioma_origen` (str, default `'es'`), `idioma_destino` (str, default `'en'`) |

### Idiomas soportados

| Codigo | Idioma |
|--------|--------|
| `es` | Espanol |
| `en` | Ingles |
| `it` | Italiano |
| `fr` | Frances |
| `de` | Aleman |

### Respuesta exitosa

```json
{
  "exito": true,
  "mensaje": "Texto detectado y traducido exitosamente.",
  "idioma_origen": "es",
  "idioma_destino": "en",
  "total_detecciones": 12,
  "detecciones": [
    {
      "texto_original": "PLATOS FUERTES",
      "traduccion": "STRONG DISHES",
      "confianza": 0.57,
      "coordenadas": {
        "top_left": [62, 50],
        "bottom_right": [248, 106]
      }
    }
  ]
}
```

### Sin detecciones

```json
{
  "exito": false,
  "mensaje": "No se detecto texto en la imagen con suficiente confianza.",
  "idioma_origen": "es",
  "idioma_destino": "en",
  "total_detecciones": 0,
  "detecciones": []
}
```

---

## Modelo e Inteligencia Artificial

- **Motor OCR:** EasyOCR (Deep Learning)
  - **CRAFT** (Character Region Awareness For Text detection): detecta regiones de texto
  - **CRNN** (Convolutional Recurrent Neural Network): reconoce caracteres
- **Motor de Traduccion:** GoogleTranslator (deep_translator)
- **Carga:** Singleton `LectorOCR` con readers cacheados por idioma (lazy loading)
- **Umbral de confianza:** 50%

### Pipeline de procesamiento

1. Recibir imagen y parametros de idioma.
2. Ejecutar EasyOCR para detectar textos (con redes CRAFT + CRNN).
3. Filtrar detecciones con confianza > 50%.
4. Traducir cada texto con GoogleTranslator.
5. Extraer coordenadas de bounding box (top_left, bottom_right).
6. Devolver JSON con textos, traducciones y coordenadas.

---

## Estructura de archivos

```
deeplearning/CU08_traductor_visual/
+-- api_traductor/
    |-- __init__.py
    |-- apps.py            <- AppConfig (lazy loading del OCR)
    |-- model_loader.py    <- Singleton LectorOCR (EasyOCR)
    |-- services.py        <- Logica OCR + traduccion + coordenadas
    |-- urls.py            <- Ruta interna
    +-- views.py           <- Vista REST (validacion + invocacion)
```

---

## Configuracion Django

- `motor_ia/settings.py`:
  - App registrada como `api_traductor.apps.ApiTraductorConfig`
  - Ruta `CU08_traductor_visual` en `sys.path`
- `motor_ia/urls.py`:
  - Ruta `api/traducir-imagen/` -> `api_traductor.urls`

---

## Trabajo realizado (2026-06-05)

1. **Desarrollo en Colab:** Dos funciones funcionales (traduccion basica y con coordenadas).
2. **Integracion Django:** Creada app `api_traductor` siguiendo el patron de CU06/CU07.
3. **Separacion de capas:** Logica de IA en `services.py` / `model_loader.py`.
4. **Lazy loading:** Los modelos OCR se cargan bajo demanda por idioma (no al arrancar).
5. **5 idiomas:** Espanol, Ingles, Italiano, Frances, Aleman.

---

## Librerias utilizadas

- Django 5.2, Django REST Framework
- EasyOCR (CRAFT + CRNN para OCR)
- deep_translator (GoogleTranslator)
- OpenCV (procesamiento de imagen)

---

## Pendientes / mejoras futuras

- Soporte para procesamiento de video en tiempo real (stream de frames)
- Cache de traducciones frecuentes para reducir llamadas a Google Translate
- Agregar mas idiomas segun necesidad
- Integracion con Microservicio A (app movil / backend principal)
