# Documentación de modelos IA — CU-09 Recomendación personalizada

## Archivos en `model_files/`

| Archivo | Tipo | Propósito |
|---------|------|-----------|
| `modelo_recomendador_rutas.pkl` | Modelo entrenado (Random Forest) | Predice el id_ruta destino basado en las características del pasajero. |
| `le_perfil.pkl` | LabelEncoder serializado | Transforma el perfil del pasajero (Económico, Estándar, Premium) a valores numéricos (0, 1, 2). |
| `le_categoria.pkl` | LabelEncoder serializado | Transforma la categoría turística vista por el usuario (Altiplano, Valle, Sur, Historico, Colonial, Interdepartamental, Normal) a valores numéricos (0 a 6). |

**Formato de serialización:** pickle / joblib (compatible con scikit‑learn 1.3‑1.5).

**Librería utilizada para cargar en Django:** joblib (`import joblib`).

---

## Características del modelo `modelo_recomendador_rutas.pkl`

### Algoritmo

RandomForestClassifier de scikit‑learn.

| Parámetro | Valor |
|-----------|-------|
| Número de árboles (`n_estimators`) | 100 |
| Semilla aleatoria (`random_state`) | 42 |
| Otras configuraciones | Por defecto (`max_depth=None`, `min_samples_split=2`, etc.) |

### Variables de entrada (features)

El modelo fue entrenado con 4 características numéricas, en este orden exacto:

| Orden | Nombre | Tipo | Fuente en la BD / lógica |
|-------|--------|------|--------------------------|
| 0 | `perfil_num` | entero (0‑2) | Transformado desde `perfil_pasajero` (Económico→0, Estándar→1, Premium→2) |
| 1 | `categoria_num` | entero (0‑6) | Transformado desde `categoria_preferida` (última categoría vista por el usuario) |
| 2 | `monto_total_pagado` | float | Monto total pagado en la reserva (o presupuesto estimado) |
| 3 | `cantidad_pasajeros` | entero (1‑4 típicamente) | Número de pasajeros para los que se reserva |

### Variable de salida

`id_ruta` (entero del 1 al 24, correspondiente a la tabla RUTA_DESTINO en PostgreSQL).

El modelo asigna una de las 24 rutas posibles (aunque solo ~16 aparecen con frecuencia en el entrenamiento).

### Métricas de rendimiento (obtenidas en Colab)

| Métrica | Valor |
|---------|-------|
| Accuracy (exactitud) | 0.8107 (81.07%) |
| Macro F1-score | 0.76 |
| Weighted F1-score | 0.81 |
| Clases con mejor rendimiento | ruta 5 (Santa Cruz → Oruro), ruta 13 (Oruro → Potosí), ruta 9 (La Paz → Oruro) |

⚠️ **Nota sobre la calidad:** El modelo fue entrenado con datos sintéticos + sesgo lógico (85% comportamiento racional) y validado con datos reales de PostgreSQL. Alcanza un 81% de precisión en condiciones de laboratorio. Para entornos productivos se recomienda monitorear la desviación de conceptos (concept drift) y reentrenar periódicamente con datos reales nuevos.

---

## Encoders (`le_perfil.pkl`, `le_categoria.pkl`)

### Clases conocidas (mapeo texto → número)

#### `le_perfil` (`perfil_pasajero`)

| Texto | Número |
|-------|--------|
| `"Económico"` | 0 |
| `"Estándar"` | 1 |
| `"Premium"` | 2 |

#### `le_categoria` (`categoria_preferida`)

| Texto | Número |
|-------|--------|
| `"Altiplano"` | 0 |
| `"Colonial"` | 1 |
| `"Historico"` | 2 |
| `"Interdepartamental"` | 3 |
| `"Normal"` | 4 |
| `"Sur"` | 5 |
| `"Valle"` | 6 |

Si durante la inferencia se recibe un valor no contemplado en esta lista, el microservicio retornará un error 400 indicando "Categoría o Perfil no reconocido".

---

## Integración con Django (motor IA)

### Carga de modelos (en `apps.py`)

```python
cls.modelo_rf = joblib.load(os.path.join(ruta_modelos, 'modelo_recomendador_rutas.pkl'))
cls.le_perfil = joblib.load(os.path.join(ruta_modelos, 'le_perfil.pkl'))
cls.le_categoria = joblib.load(os.path.join(ruta_modelos, 'le_categoria.pkl'))
```

### Uso en la vista (`views.py`)

```python
perfil_num = le_perfil.transform([perfil])[0]
categoria_num = le_categoria.transform([categoria])[0]
X = np.array([[perfil_num, categoria_num, monto, cantidad_pasajeros]])
ruta = modelo_rf.predict(X)[0]
probabilidades = modelo_rf.predict_proba(X)[0]
```

### Requisitos del entorno de ejecución

| Dependencia | Versión |
|-------------|---------|
| Python | 3.10+ |
| scikit‑learn | >= 1.3.0 (debe coincidir con la versión usada en el entrenamiento) |
| joblib | >= 1.2.0 |
| numpy | >= 1.24.0 |

📦 Las versiones exactas están en el `requirements.txt` del proyecto Django.

---

## Origen de los archivos

Los archivos fueron entrenados en Google Colab utilizando el siguiente proceso documentado previamente:

1. Extracción de datos reales desde PostgreSQL (1000 usuarios, 12k reservas, 24 rutas).
2. Inferencia de `perfil_pasajero` por percentiles de gasto total (33% y 67%).
3. Inferencia de `categoria_preferida` como la categoría turística más reservada por cada usuario.
4. Generación de datos sintéticos de navegación (DynamoDB) para simular el historial de búsquedas (6,468 interacciones).
5. Fusión de todas las fuentes en un dataset maestro con 4 características.
6. Entrenamiento de Random Forest con `train_test_split` (80/20) y `random_state=42`.
7. Serialización de modelo y encoders con `joblib.dump()`.

Los archivos resultantes se copiaron manualmente a la carpeta `model_files/` del microservicio Django.

---

## Advertencias técnicas

| Tema | Detalle |
|------|---------|
| Versión de scikit‑learn | El archivo `.pkl` guarda la estructura interna del modelo, que no es compatible entre versiones mayores de scikit‑learn. Si se actualiza la librería en el servidor Django, es obligatorio reentrenar y volver a serializar el modelo desde un entorno con la misma versión. |
| Memoria | Al cargarse en `apps.py` durante el inicio del servidor, estos tres archivos ocupan aproximadamente 150‑200 MB en RAM (típico para un Random Forest de 100 árboles con 24 clases). |
| Rendimiento | La inferencia típica es < 10 ms por solicitud en CPU. No se necesita GPU para este modelo. |

---

## Relación con otros componentes

| Componente | Relación |
|------------|----------|
| Core Transaccional (Spring Boot) | Consume este modelo a través del endpoint REST `/api/v1/recomendar-ruta/` (ver `views.py` y `urls.py`). |
| App móvil (React Native) | No llama directamente a este modelo; lo hace a través del Core Transaccional (GraphQL) o directamente al endpoint si se decide exponerlo (no recomendado en producción). |
| Base de datos | El modelo no almacena ningún estado; solo predice. Los datos de entrenamiento originales se obtuvieron de PostgreSQL (vía CSV) y DynamoDB (sintético). |

---

## Historial de versiones

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | 2026‑06‑07 | Versión inicial con modelo de 4 características, precisión 81%. Encoders para 7 categorías y 3 perfiles. |

Última revisión: 2026‑06‑07 – por [Equipo de Desarrollo / Grupo 18]
