 Estado de Implementación - Motor IA (Microservicio B)

> **⚙️ INSTRUCCIÓN CRÍTICA PARA EL AGENTE DE IA:** > Actúas como el desarrollador Científico de Datos / Backend de este microservicio. Cada vez que completes la implementación (Vista en Django, carga del modelo `.h5`/`.pkl` en `apps.py` y el procesamiento del payload) de un Caso de Uso, debes marcar la casilla correspondiente cambiando `[ ]` por `[x]` y agregar un breve resumen de los archivos modificados o librerías utilizadas debajo del caso de uso.

## 📊 Resumen de Progreso
- **Estado General:** 🚧 En Desarrollo
- **Framework Base:** Django y Django REST Framework (Configurado)
- **Modelos Matemáticos:** Entrenamientos en Google Colab (Pendiente de exportar e importar archivos estáticos)

---

## 📸 Modelos de Deep Learning (Visión Computacional y NLP)

- [ ] **CU06: Buscar destinos mediante imágenes**
  - *Notas:* Pendiente crear endpoint que reciba la imagen, cargar la red neuronal convolucional (CNN) y retornar destinos similares.
- [ ] **CU07: Generar videos turísticos automáticamente**
  - *Notas:* Pendiente lógica de extracción de frames clave y análisis de escenas.
- [ ] **CU08: Traducir texto mediante video**
  - *Notas:* Pendiente de integrar tubería (pipeline) de OCR con procesamiento de lenguaje natural (NLP).

---

## 📊 Modelos de Machine Learning (Análisis Predictivo de Datos)

- [ ] **CU09: Recomendación personalizada de destinos**
  - *Notas:* Pendiente cargar modelo de Machine Learning Supervisado (ej. Random Forest) para inferencia basada en perfil del viajero.
- [ ] **CU10: Detección de patrones de viaje**
  - *Notas:* Pendiente implementar ejecución del algoritmo Apriori / Reglas de Asociación para sugerir rutas complementarias.