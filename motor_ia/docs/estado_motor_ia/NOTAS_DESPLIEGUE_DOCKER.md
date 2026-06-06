# Notas Importantes para el Despliegue en Docker (Motor IA)

**Fecha de creación:** 2026-06-05
**Contexto:** Preparación para la futura contenerización (Docker) del Microservicio B (Motor IA basado en Django, TensorFlow y PyTorch).

Estas notas documentan consideraciones arquitectónicas críticas que deben aplicarse cuando se escriba el `Dockerfile` para producción, debido al alto peso de las librerías de Inteligencia Artificial.

---

## 1. Tamaño de la Imagen Docker

Al incluir librerías matemáticas y de Deep Learning como TensorFlow y PyTorch, la imagen final del contenedor Docker pesará entre **3 GB y 5 GB**. Esto es completamente normal en proyectos de IA, pero requiere prever suficiente espacio en el registro de contenedores (ej. Google Artifact Registry) y en el servidor de despliegue.

## 2. Optimización de PyTorch (CPU vs GPU)

Actualmente en `requirements.txt` tenemos instaladas las dependencias genéricas. Dependiendo de dónde se despliegue el motor (GCP Cloud Run, Compute Engine, etc.), la instalación debe ajustarse en el Dockerfile:

### Despliegue SIN GPU (Ej. Google Cloud Run)
Si el servidor no tiene tarjeta gráfica, instalar el PyTorch genérico (con soporte CUDA) añadirá ~2GB innecesarios a la imagen.
* **Solución:** En el Dockerfile, instalar la versión explícita para CPU. Pesa ~200 MB en lugar de 2 GB.
* **Comando recomendado:** `pip install torch torchvision --index-url https://download.pytorch.org/whl/cpu`

### Despliegue CON GPU (Ej. Compute Engine con Nvidia T4)
Si el servidor tendrá GPU (recomendado para procesamiento rápido de videos en CU07 y OCR en CU08), las compilaciones pueden ser problemáticas.
* **Solución:** Utilizar una imagen base oficial preconfigurada en el `Dockerfile` (ej. `FROM pytorch/pytorch:latest-cuda11.8-cudnn8-runtime` o equivalente) que ya incluye los binarios de NVIDIA optimizados.

## 3. Persistencia de Modelos de IA en el Contenedor

Las redes neuronales necesitan cargar "pesos" (archivos `.h5`, `.keras`, `.pth`). Los contenedores Docker son "efímeros", lo que significa que si el modelo de EasyOCR o MobileNet se descarga de internet durante la ejecución, **se perderá cada vez que el contenedor se reinicie o escale**. Descargar 150MB cada vez que arranca el servicio causaría tiempos de respuesta inaceptables (Cold Starts severos).

* **Solución:** Durante el proceso de *build* de la imagen Docker (`docker build`), se debe incluir un paso (ej. ejecutar un script Python) que instancie los modelos para forzar su descarga.
* De esta manera, los archivos `.pth` (de EasyOCR) y los pesos de Keras (MobileNet) quedarán "congelados" dentro del sistema de archivos de la imagen Docker.
* **Resultado:** Cuando el contenedor arranque en producción, iniciará en segundos y funcionará 100% offline sin descargar nada.

---
*Nota: Revisar este documento antes de redactar el Dockerfile y el pipeline de CI/CD.*
