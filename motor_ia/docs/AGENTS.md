# Reglas de Entorno y Contexto para Agentes de IA (AGENTS.md)

## 📌 Contexto del Proyecto
Estás operando en el **Microservicio B (Motor de Inteligencia Artificial)** de una plataforma de Agencia de Viajes. Este componente actúa como el cerebro analítico del sistema.
* **Responsabilidad Principal:** Ejecutar modelos de Machine Learning (Supervisado/No Supervisado) y Deep Learning para inferencia en tiempo real y análisis de datos.
* **Límites Arquitectónicos:** Este servicio **NO** gestiona el inventario transaccional de pasajes, **NO** procesa pagos y **NO** emite documentos. Funciona como una API interna que recibe peticiones (con payloads JSON o URLs de imágenes de Amazon S3) desde el Microservicio A, procesa los algoritmos y devuelve predicciones estructuradas.

## 🛠️ Stack Tecnológico
* **Lenguaje:** Python 3.10+
* **Framework Web:** Django (y Django REST Framework para la API).
* **Librerías de IA Esperadas:** TensorFlow/Keras o PyTorch (para Deep Learning), Scikit-Learn (para Machine Learning), OpenCV/Tesseract (para OCR y Visión).
* **Despliegue Objetivo:** Google Cloud Platform (GCP) con aceleración por GPU.
* **Base de Datos:** No posee base de datos relacional para el negocio. Utiliza SQLite (`db.sqlite3`) temporalmente solo para el panel de administración interno de Django o registros de uso de la API.

## 🚀 Comandos de Ejecución Local
* Instalar dependencias: `pip install -r requirements.txt`
* Ejecutar servidor de desarrollo: `python manage.py runserver`
* Aplicar migraciones internas: `python manage.py migrate`

---

## 📝 Estándares de Código y Documentación (Python/Django)

### 1. Documentación (Docstrings y Type Hints)
Todo el código debe ser explícito y fácil de mantener.
* Utiliza **Type Hints** (tipado estático de Python) en todos los parámetros y retornos de funciones (ej. `def predict_destination(user_data: dict) -> list:`).
* Documenta las clases y funciones utilizando el estándar de docstrings de **Google Style**. Debe explicar qué algoritmo matemático se está utilizando y qué formato de datos espera de entrada.

### 2. Convenciones de Arquitectura (Django para ML)
Mantén la lógica de Inteligencia Artificial separada de la lógica web:
* **Capa Web (`views.py` / `urls.py`):** Solo debe validar el JSON entrante, invocar al servicio de IA y retornar la respuesta (JSON). No pongas lógica matemática aquí.
* **Capa de Servicios (`services/` o `ml_models/`):** Crea archivos separados dentro de tus aplicaciones (ej. en la app `recomendaciones`) para encapsular la carga de los modelos pre-entrenados (`.h5`, `.pkl`) y la ejecución de la inferencia. 
* **Carga de Modelos (Singleton):** Los modelos de IA son pesados. El agente debe asegurarse de que los modelos se carguen en memoria una sola vez al arrancar el servidor Django (por ejemplo, en el archivo `apps.py`), y no en cada petición web.

### 3. Buenas Prácticas y Manejo de Errores
* Nomenclatura: Usa `snake_case` para variables, funciones y nombres de archivos. Usa `PascalCase` para las clases.
* Captura excepciones específicas de las librerías matemáticas (ej. tensores con dimensiones incorrectas, imágenes corruptas o valores nulos en Pandas) y devuelve códigos HTTP `400 Bad Request` o `422 Unprocessable Entity` con mensajes claros en JSON.

---
> **⚡ INSTRUCCIÓN CRÍTICA PARA EL AGENTE:** Antes de codificar cualquier endpoint, revisa el archivo `@docs/captura_de_requisitos_ia.md` para entender qué algoritmo específico (CNN, Apriori, Random Forest) corresponde a cada caso de uso.