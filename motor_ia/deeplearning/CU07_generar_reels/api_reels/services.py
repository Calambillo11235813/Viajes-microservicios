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
from datetime import datetime
from typing import Any

import numpy as np
from moviepy.editor import AudioFileClip, VideoFileClip, concatenate_videoclips

from .model_loader import scorer


def generar_reel(
    ruta_video: str,
    ruta_audio: str,
    directorio_salida: str,
    duracion_reel: int = 60,
    duracion_clip: int = 5,
) -> dict[str, Any]:
    """Genera un reel turístico seleccionando los mejores fragmentos con IA.

    Pipeline:
        1. Carga el video y calcula la cantidad de bloques posibles.
        2. Evalúa el fotograma central de cada bloque con ``scorer.puntuar_fotograma``.
        3. Selecciona los N bloques con mayor puntuación.
        4. Reordena cronológicamente.
        5. Concatena, aplica audio y renderiza a MP4 (H.264 / AAC).

    Args:
        ruta_video: Ruta absoluta al archivo de video de entrada.
        ruta_audio: Ruta absoluta al archivo de audio (pista musical).
        directorio_salida: Directorio donde se guardará el reel generado.
        duracion_reel: Duración deseada del reel en segundos (default 60).
        duracion_clip: Duración de cada fragmento individual en segundos (default 5).

    Returns:
        Diccionario con metadatos del procesamiento:
            - ``ruta_archivo``: Ruta absoluta del reel generado.
            - ``nombre_archivo``: Nombre del archivo generado.
            - ``duracion_reel``: Duración real del reel en segundos.
            - ``clips_seleccionados``: Cantidad de clips en el reel.
            - ``clips_analizados``: Total de bloques analizados.
            - ``fragmentos``: Lista de dicts con inicio, fin y score de cada clip.

    Raises:
        ValueError: Si el video es demasiado corto o los parámetros son inválidos.
        FileNotFoundError: Si los archivos de entrada no existen.
        RuntimeError: Si ocurre un error durante el renderizado.
    """
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

    # ── 1. Cargar video y calcular bloques ────────────────────────────
    video = VideoFileClip(ruta_video)
    duracion_original = video.duration
    cantidad_clips_necesarios = int(duracion_reel / duracion_clip)

    if duracion_original < duracion_reel:
        video.close()
        raise ValueError(
            f"El video ({duracion_original:.1f}s) es más corto que la "
            f"duración solicitada para el reel ({duracion_reel}s)."
        )

    # Dividir en bloques consecutivos de duracion_clip segundos
    bloques_tiempos: list[float] = []
    tiempo_actual = 0.0
    while tiempo_actual + duracion_clip <= duracion_original:
        bloques_tiempos.append(tiempo_actual)
        tiempo_actual += duracion_clip

    if len(bloques_tiempos) < cantidad_clips_necesarios:
        video.close()
        raise ValueError(
            f"No hay suficientes bloques ({len(bloques_tiempos)}) para "
            f"seleccionar {cantidad_clips_necesarios} clips."
        )

    # ── 2. Puntuar fotograma central de cada bloque con IA ───────────
    resultados_bloques: list[dict[str, float]] = []
    for t_inicio in bloques_tiempos:
        t_medio = t_inicio + (duracion_clip / 2)
        fotograma = video.get_frame(t_medio)  # array RGB (H, W, 3)

        # moviepy devuelve RGB; OpenCV espera BGR para Laplacian
        import cv2
        fotograma_bgr = cv2.cvtColor(fotograma, cv2.COLOR_RGB2BGR)

        score = scorer.puntuar_fotograma(fotograma_bgr)
        resultados_bloques.append({
            "inicio": round(t_inicio, 2),
            "fin": round(t_inicio + duracion_clip, 2),
            "score": round(score, 2),
        })

    # ── 3. Selección por ranking (mejores puntuaciones) ──────────────
    bloques_top = sorted(
        resultados_bloques,
        key=lambda x: x['score'],
        reverse=True,
    )[:cantidad_clips_necesarios]

    # ── 4. Reordenamiento cronológico ────────────────────────────────
    bloques_top_cronologicos = sorted(bloques_top, key=lambda x: x['inicio'])

    # ── 5. Extracción, concatenación y musicalización ────────────────
    clips_extraidos = []
    for bloque in bloques_top_cronologicos:
        subclip = video.subclip(bloque['inicio'], bloque['fin'])
        clips_extraidos.append(subclip)

    video_final = concatenate_videoclips(clips_extraidos, method="compose")

    # Ajustar música a la duración exacta del reel
    musica = AudioFileClip(ruta_audio).subclip(0, video_final.duration)
    video_final = video_final.without_audio().set_audio(musica)

    # ── 6. Renderizado ───────────────────────────────────────────────
    os.makedirs(directorio_salida, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    nombre_archivo = f"reel_{timestamp}.mp4"
    ruta_salida = os.path.join(directorio_salida, nombre_archivo)

    try:
        video_final.write_videofile(
            ruta_salida,
            codec="libx264",
            audio_codec="aac",
            fps=24,
            preset="ultrafast",
            logger=None,
        )
    except Exception as e:
        raise RuntimeError(f"Error al renderizar el reel: {e}") from e
    finally:
        # Liberar recursos de moviepy
        video.close()
        video_final.close()
        musica.close()

    return {
        "ruta_archivo": ruta_salida,
        "nombre_archivo": nombre_archivo,
        "duracion_reel": round(video_final.duration, 2),
        "clips_seleccionados": len(bloques_top_cronologicos),
        "clips_analizados": len(resultados_bloques),
        "fragmentos": bloques_top_cronologicos,
    }
