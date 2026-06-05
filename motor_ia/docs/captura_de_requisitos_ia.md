# Captura de Requisitos Funcionales - Motor de Inteligencia Artificial (Microservicio B)

## 📌 Propósito del Documento
Este archivo contiene exclusivamente los Casos de Uso de Inteligencia Artificial que deben ser implementados en el **Microservicio B (Motor IA)** utilizando Python y Django.
* **Responsabilidad:** Este microservicio actúa como el cerebro analítico de la plataforma. Recibe peticiones HTTP con datos crudos (imágenes, videos o JSON con historiales) desde el Core Transaccional, ejecuta la inferencia matemática y devuelve resultados estructurados.
* **Límites:** El agente de IA **NO** debe conectar este código a PostgreSQL, **NO** debe manejar pagos ni emitir PDFs. Tampoco debe programar interfaces de usuario.

## 🧠 Casos de Uso a Implementar

A continuación se detallan los flujos exactos que el agente debe mapear a Servicios de Machine Learning (ML) y Controladores REST en Django. Los casos están divididos por su paradigma algorítmico:

### 📸 Modelos de Deep Learning (Visión Computacional y NLP)
Estos endpoints procesarán archivos multimedia. Se espera el uso de librerías como TensorFlow/Keras, OpenCV o Tesseract.

* [cite_start]**CU06: Buscar destinos mediante imágenes:** * *Descripción:* Analizar una fotografía subida por el usuario e identificar que lugar es [cite: 1734].
  * *Técnica esperada:* Redes Neuronales Convolucionales (CNN) / Visión por computadora.

* **CU07: Generar videos turísticos automáticamente:**
  * [cite_start]*Descripción:* Analizar videos largos grabados por el usuario, extraer automáticamente las mejores escenas y generar clips cortos (reels) con transiciones[cite: 1736].
  * *Técnica esperada:* Deep Learning para análisis de escenas y detección de momentos clave.

* **CU08: Traducir texto mediante video:**
  * [cite_start]*Descripción:* Procesar frames de la cámara en tiempo real para detectar y traducir textos del entorno (ej. menús, letreros o señaléticas)[cite: 1735].
  * *Técnica esperada:* Reconocimiento Óptico de Caracteres (OCR) combinado con Procesamiento de Lenguaje Natural (NLP).

### 📊 Modelos de Machine Learning (Análisis Predictivo de Datos)
Estos endpoints procesarán conjuntos de datos JSON. Se espera el uso de librerías como Scikit-Learn o Pandas.

* **CU09: Recomendación personalizada de destinos:**
  * [cite_start]*Descripción:* Sistema de sugerencias que aprende del historial de navegación, presupuesto y reservas pasadas del usuario para ofrecer destinos adaptados a sus preferencias[cite: 1737].
  * *Técnica esperada:* Machine Learning Supervisado (algoritmos como Random Forest, Decision Trees o Gradient Boosting).

* **CU10: Detección de patrones de viaje:**
  * [cite_start]*Descripción:* Análisis de datos masivos para descubrir asociaciones frecuentes entre destinos (ej. detectar que los usuarios que viajan a la ciudad A suelen visitar la ciudad B) para sugerir rutas complementarias automáticamente[cite: 1738].
  * *Técnica esperada:* Machine Learning No Supervisado (Reglas de Asociación, Algoritmo Apriori).