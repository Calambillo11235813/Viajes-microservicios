"""
services.py — Lógica de negocio para generación de reels turísticos (CU-07).

Encapsula el pipeline completo de procesamiento de video con IA:
1. Divide el video en bloques de N segundos.
2. Puntúa el fotograma central de cada bloque con MobileNetV2 + OpenCV.
3. Selecciona los mejores bloques por puntuación (ranking).
4. Reordena cronológicamente para mantener coherencia narrativa.
5. Concatena los clips, aplica pista musical y renderiza el reel final.

Este módulo NO conoce HTTP ni Django; solo recibe rutas de archivos
y parámetros numéricos, y devuelve un diccionario con los resultados.
"""

import os
import time
from datetime import datetime
from typing import Any

import cv2
import numpy as np
from moviepy.editor import AudioFileClip, VideoFileClip, concatenate_videoclips

from .model_loader import scorer

# ── Constantes de optimización ────────────────────────────────────────────────
ANALISIS_HEIGHT = 480
SALIDA_HEIGHT = 720
VIDEO_BITRATE = "800k"
AUDIO_BITRATE = "128k"


def generar_reel(
    ruta_video: str,
    ruta_audio: str,
    directorio_salida: str,
    duracion_reel: int = 60,
    duracion_clip: int = 10,
) -> dict[str, Any]:
    """Genera un reel turístico seleccionando los mejores fragmentos con IA.

    Pipeline optimizado:
        1. Carga video original + proxy 480p para análisis.
        2. Extrae fotogramas centrales y puntúa en batch con MobileNetV2.
        3. Selecciona los N bloques con mayor puntuación.
        4. Reordena cronológicamente.
        5. Extrae subclips del original, concatena, musicaliza y renderiza 720p.

    Args:
        ruta_video: Ruta absoluta al archivo de video de entrada.
        ruta_audio: Ruta absoluta al archivo de audio (pista musical).
        directorio_salida: Directorio donde se guardará el reel generado.
        duracion_reel: Duración deseada del reel en segundos (default 60).
        duracion_clip: Duración de cada fragmento individual en segundos (default 10).

    Returns:
        Diccionario con metadatos del procesamiento, incluyendo
        ``tiempos_procesamiento`` con segundos por fase.

    Raises:
        ValueError: Si el video es demasiado corto o los parámetros son inválidos.
        FileNotFoundError: Si los archivos de entrada no existen.
        RuntimeError: Si ocurre un error durante el renderizado.
    """
    t_inicio_total = time.perf_counter()
    tiempos: dict[str, float] = {}

    # ── Validaciones ──────────────────────────────────────────────────
    if not os.path.exists(ruta_video):
        raise FileNotFoundError(f"Video no encontrado: {ruta_video}")
    if not os.path.exists(ruta_audio):
        raise FileNotFoundError(f"Audio no encontrado: {ruta_audio}")
    if duracion_reel < duracion_clip:
        raise ValueError(
            f"La duración del reel ({duracion_reel}s) debe ser mayor o igual "
            f"a la duración de cada clip ({duracion_clip}s)."
        )

    video_original: VideoFileClip | None = None
    video_analisis: VideoFileClip | None = None
    video_final: VideoFileClip | None = None
    musica: AudioFileClip | None = None
    clips_extraidos: list[VideoFileClip] = []

    try:
        # ── 1. Cargar video y calcular bloques ────────────────────────
        t_carga = time.perf_counter()
        video_original = VideoFileClip(ruta_video)
        if video_original.h > ANALISIS_HEIGHT:
            video_analisis = video_original.resize(height=ANALISIS_HEIGHT)
        else:
            video_analisis = video_original

        duracion_original = video_original.duration
        cantidad_clips_necesarios = int(duracion_reel / duracion_clip)
        tiempos["carga_seg"] = round(time.perf_counter() - t_carga, 2)

        if duracion_original < duracion_reel:
            raise ValueError(
                f"El video ({duracion_original:.1f}s) es más corto que la "
                f"duración solicitada para el reel ({duracion_reel}s)."
            )

        bloques_tiempos: list[float] = []
        tiempo_actual = 0.0
        while tiempo_actual + duracion_clip <= duracion_original:
            bloques_tiempos.append(tiempo_actual)
            tiempo_actual += duracion_clip

        if len(bloques_tiempos) < cantidad_clips_necesarios:
            raise ValueError(
                f"No hay suficientes bloques ({len(bloques_tiempos)}) para "
                f"seleccionar {cantidad_clips_necesarios} clips."
            )

        # ── 2. Extraer fotogramas (480p) y puntuar en batch ───────────
        t_scoring = time.perf_counter()
        fotogramas_bgr: list[np.ndarray] = []
        for t_inicio in bloques_tiempos:
            t_medio = t_inicio + (duracion_clip / 2)
            fotograma_rgb = video_analisis.get_frame(t_medio)
            fotogramas_bgr.append(cv2.cvtColor(fotograma_rgb, cv2.COLOR_RGB2BGR))

        scores = scorer.puntuar_fotogramas_batch(fotogramas_bgr)
        resultados_bloques: list[dict[str, float]] = [
            {
                "inicio": round(t_inicio, 2),
                "fin": round(t_inicio + duracion_clip, 2),
                "score": round(score, 2),
            }
            for t_inicio, score in zip(bloques_tiempos, scores)
        ]
        tiempos["scoring_seg"] = round(time.perf_counter() - t_scoring, 2)

        # ── 3. Selección por ranking ───────────────────────────────────
        bloques_top = sorted(
            resultados_bloques,
            key=lambda x: x['score'],
            reverse=True,
        )[:cantidad_clips_necesarios]

        # ── 4. Reordenamiento cronológico ─────────────────────────────
        bloques_top_cronologicos = sorted(bloques_top, key=lambda x: x['inicio'])

        # ── 5. Extracción, concatenación y musicalización ─────────────
        t_extraccion = time.perf_counter()
        for bloque in bloques_top_cronologicos:
            subclip = video_original.subclip(bloque['inicio'], bloque['fin'])
            clips_extraidos.append(subclip)

        video_final = concatenate_videoclips(clips_extraidos, method="compose")
        musica = AudioFileClip(ruta_audio).subclip(0, video_final.duration)
        video_final = video_final.without_audio().set_audio(musica)
        tiempos["extraccion_seg"] = round(time.perf_counter() - t_extraccion, 2)

        # Escalar salida a 720p si excede el límite
        if video_final.h > SALIDA_HEIGHT:
            video_final = video_final.resize(height=SALIDA_HEIGHT)

        # ── 6. Renderizado ────────────────────────────────────────────
        os.makedirs(directorio_salida, exist_ok=True)
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        nombre_archivo = f"reel_{timestamp}.mp4"
        ruta_salida = os.path.join(directorio_salida, nombre_archivo)

        t_render = time.perf_counter()
        video_final.write_videofile(
            ruta_salida,
            codec="libx264",
            audio_codec="aac",
            fps=24,
            preset="ultrafast",
            bitrate=VIDEO_BITRATE,
            audio_bitrate=AUDIO_BITRATE,
            logger=None,
        )
        tiempos["render_seg"] = round(time.perf_counter() - t_render, 2)

        duracion_final = round(video_final.duration, 2)
        tiempos["total_seg"] = round(time.perf_counter() - t_inicio_total, 2)
        print(
            f"[CU-07] tiempos: carga={tiempos['carga_seg']}s "
            f"scoring={tiempos['scoring_seg']}s extraccion={tiempos['extraccion_seg']}s "
            f"render={tiempos['render_seg']}s total={tiempos['total_seg']}s"
        )

        return {
            "ruta_archivo": ruta_salida,
            "nombre_archivo": nombre_archivo,
            "duracion_reel": duracion_final,
            "clips_seleccionados": len(bloques_top_cronologicos),
            "clips_analizados": len(resultados_bloques),
            "fragmentos": bloques_top_cronologicos,
            "tiempos_procesamiento": tiempos,
        }

    except Exception as e:
        if isinstance(e, (ValueError, FileNotFoundError)):
            raise
        raise RuntimeError(f"Error al renderizar el reel: {e}") from e

    finally:
        for clip in clips_extraidos:
            try:
                clip.close()
            except Exception:
                pass
        if video_analisis is not None and video_analisis is not video_original:
            try:
                video_analisis.close()
            except Exception:
                pass
        if video_original is not None:
            try:
                video_original.close()
            except Exception:
                pass
        if musica is not None:
            try:
                musica.close()
            except Exception:
                pass
        if video_final is not None:
            try:
                video_final.close()
            except Exception:
                pass
