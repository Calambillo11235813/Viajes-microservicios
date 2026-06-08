# Estado de Implementación — Motor IA (Microservicio B)

> **Instrucción para el agente:** Al completar un caso de uso, marcar `[x]`, actualizar este resumen y crear un archivo con fecha en esta carpeta.

## Resumen

| Ítem | Estado |
|------|--------|
| Framework | Django 5.2 + DRF configurado |
| Servidor local | `python manage.py runserver` → `http://127.0.0.1:8000/` |
| Casos de uso activos | 6 de 6 (CU06–CU11) |
| Estado general | Implementado localmente; pendiente integración completa con Microservicio A |

---

## Deep Learning (Visión / NLP)

- [x] **CU06: Buscar destinos mediante imágenes**
  - Endpoint `POST /api/predict/` operativo. CNN (MobileNetV2) con 3 destinos: `Cristo_ConCordia`, `Samaipata`, `Uyuni`.
  - Detalle: [CU06_Buscar_Destinos_2026-06-05.md](./CU06_Buscar_Destinos_2026-06-05.md)
- [x] **CU07: Generar videos turísticos automáticamente**
  - Endpoint `POST /api/generar-reel/` operativo. MobileNetV2 + OpenCV para selección inteligente de escenas.
  - Detalle: [CU07_Generar_Reels_2026-06-05.md](./CU07_Generar_Reels_2026-06-05.md)
- [x] **CU08: Traducir texto mediante video**
  - Endpoint `POST /api/traducir-imagen/` operativo. EasyOCR (CRAFT+CRNN) + GoogleTranslator. 5 idiomas: es, en, it, fr, de.
  - Detalle: [CU08_Traductor_Visual_2026-06-05.md](./CU08_Traductor_Visual_2026-06-05.md)

---

## Machine Learning (Análisis predictivo)

- [x] **CU09: Recomendación personalizada de destinos**
  - Endpoint `POST /api/recomendar-ruta/api/v1/recomendar-ruta/` operativo. Random Forest + LabelEncoders.
  - Integrado con `core-transaccional` vía `RecomendacionService`.
  - Detalle: [CU09_Recomendacion_Personalizada_2026-06-08.md](./CU09_Recomendacion_Personalizada_2026-06-08.md)
- [x] **CU10: Detección de patrones de viaje**
  - Endpoint `GET /api/reglas-asociacion/` operativo. Reglas de asociación (Apriori) precalculadas en JSON.
  - Detalle: [CU10_Deteccion_Patrones_2026-06-08.md](./CU10_Deteccion_Patrones_2026-06-08.md)
- [x] **CU11: Segmentación de clientes**
  - Endpoints `POST /api/segmentar-usuario/segmentar-usuario/` y `GET /api/segmentar-usuario/estadisticas-clusters/` operativos. K-Means + StandardScaler.
  - Detalle: [CU11_Segmentacion_Clientes_2026-06-08.md](./CU11_Segmentacion_Clientes_2026-06-08.md)

---

## Mapa rápido de endpoints

| CU | Método | URL completa |
|----|--------|--------------|
| CU06 | POST | `/api/predict/` |
| CU07 | POST | `/api/generar-reel/` |
| CU08 | POST | `/api/traducir-imagen/` |
| CU09 | POST | `/api/recomendar-ruta/api/v1/recomendar-ruta/` |
| CU10 | GET | `/api/reglas-asociacion/` |
| CU11 | POST | `/api/segmentar-usuario/segmentar-usuario/` |
| CU11 | GET | `/api/segmentar-usuario/estadisticas-clusters/` |

---

## Despliegue

- Notas Docker: [NOTAS_DESPLIEGUE_DOCKER.md](./NOTAS_DESPLIEGUE_DOCKER.md)
