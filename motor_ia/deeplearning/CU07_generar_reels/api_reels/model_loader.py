"""
model_loader.py — Singleton para el modelo de puntuación de fotogramas (CU-07).

Carga MobileNetV2 (transfer learning, sin capa de clasificación) una única vez
al arrancar Django. Se usa para evaluar la riqueza visual de cada fotograma
y así seleccionar los mejores segmentos de un video para generar reels.

Algoritmo de puntuación:
    score = (riqueza_visual * 0.7) + (nitidez_normalizada * 0.3)

    - Riqueza visual: magnitud L2 del vector de 1280 features de MobileNetV2.
      A mayor magnitud, más elementos reconoce la red en la imagen.
    - Nitidez: varianza del Laplaciano (cv2). Penaliza frames borrosos o con
      movimiento excesivo. Se acota a un máximo de 500 para normalizar.
"""

import warnings

import cv2
import numpy as np

warnings.filterwarnings("ignore")

import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'  # Silencia advertencias de TensorFlow

import tensorflow as tf
from tensorflow.keras.applications.mobilenet_v2 import MobileNetV2, preprocess_input


class ReelScorer:
    """Singleton que carga MobileNetV2 y puntúa fotogramas por calidad visual.

    Attributes:
        modelo: Instancia de MobileNetV2 sin capa top, con pooling promedio.

    Example:
        >>> from .model_loader import scorer
        >>> puntuacion = scorer.puntuar_fotograma(frame_bgr)
    """

    _instance = None

    def __new__(cls) -> "ReelScorer":
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._cargar_modelo()
        return cls._instance

    def _cargar_modelo(self) -> None:
        """Carga MobileNetV2 pre-entrenado en ImageNet (sin capa top)."""
        print("[CU-07] Cargando MobileNetV2 para puntuacion de fotogramas...")
        self.modelo = MobileNetV2(
            weights='imagenet',
            include_top=False,
            pooling='avg',
        )
        print("[CU-07] MobileNetV2 cargado exitosamente.")

    def _calcular_nitidez(self, frame: np.ndarray) -> float:
        """Calcula la varianza del Laplaciano (nitidez) de un fotograma BGR."""
        gris = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        return float(cv2.Laplacian(gris, cv2.CV_64F).var())

    def _combinar_puntuacion(self, riqueza_visual: float, nitidez: float) -> float:
        """Combina riqueza visual y nitidez con pesos 70/30."""
        return (riqueza_visual * 0.7) + (min(nitidez, 500) / 500 * 0.3)

    def puntuar_fotograma(self, frame: np.ndarray) -> float:
        """Evalúa la calidad de un fotograma combinando IA y visión computacional.

        Combina dos métricas:
        - Riqueza visual (70%): Magnitud L2 del vector de features de MobileNetV2.
        - Nitidez (30%): Varianza del Laplaciano (OpenCV). Se acota a 500 max.

        Args:
            frame: Fotograma en formato BGR (numpy array de OpenCV).

        Returns:
            Puntuación final del fotograma (float, sin rango fijo).
        """
        nitidez = self._calcular_nitidez(frame)

        img_resized = cv2.resize(frame, (224, 224))
        img_rgb = cv2.cvtColor(img_resized, cv2.COLOR_BGR2RGB)
        img_array = np.expand_dims(img_rgb, axis=0)
        img_preprocesada = preprocess_input(img_array.astype(np.float32))

        features = self.modelo.predict(img_preprocesada, verbose=0)
        riqueza_visual = float(np.linalg.norm(features))

        return self._combinar_puntuacion(riqueza_visual, nitidez)

    def puntuar_fotogramas_batch(self, frames_bgr: list[np.ndarray]) -> list[float]:
        """Puntúa múltiples fotogramas en una sola inferencia MobileNetV2.

        Reduce el overhead de llamadas sucesivas a ``predict`` al evaluar
        todos los bloques del video en batch.

        Args:
            frames_bgr: Lista de fotogramas en formato BGR (OpenCV).

        Returns:
            Lista de puntuaciones en el mismo orden que ``frames_bgr``.
        """
        if not frames_bgr:
            return []

        nitidez_scores = [self._calcular_nitidez(frame) for frame in frames_bgr]

        batch_rgb = []
        for frame in frames_bgr:
            resized = cv2.resize(frame, (224, 224))
            batch_rgb.append(cv2.cvtColor(resized, cv2.COLOR_BGR2RGB))

        batch = np.stack(batch_rgb, axis=0).astype(np.float32)
        batch = preprocess_input(batch)
        features = self.modelo.predict(batch, verbose=0)

        return [
            self._combinar_puntuacion(float(np.linalg.norm(feat)), nitidez_scores[i])
            for i, feat in enumerate(features)
        ]


# Instancia global (Singleton) — se carga al importar el módulo
scorer = ReelScorer()
