# Estado de Implementación — Motor IA (Microservicio B)

> **Instrucción para el agente:** Al completar un caso de uso, marcar `[x]`, actualizar este resumen y crear un archivo con fecha en esta carpeta.

## Resumen

| Ítem | Estado |
|------|--------|
| Framework | Django 5.2 + DRF configurado |
| Servidor local | `python manage.py runserver` → `http://127.0.0.1:8000/` |
| Casos de uso activos | 3 de 5 (CU06, CU07, CU08) |
| Estado general | En desarrollo |

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

- [ ] **CU09: Recomendación personalizada de destinos**
- [ ] **CU10: Detección de patrones de viaje**

