"""Tests unitarios para el pipeline de generación de reels (CU-07)."""

import os
import shutil
import tempfile
from unittest.mock import patch

from django.test import SimpleTestCase
from moviepy.editor import AudioClip, ColorClip


class GenerarReelServiceTest(SimpleTestCase):
    """Pruebas del servicio generar_reel con video fixture corto."""

    temp_dir: str
    ruta_video: str
    ruta_audio: str
    directorio_salida: str

    @classmethod
    def setUpClass(cls) -> None:
        super().setUpClass()
        cls.temp_dir = tempfile.mkdtemp(prefix="cu07_test_")
        cls.directorio_salida = os.path.join(cls.temp_dir, "salida")
        cls.ruta_video = os.path.join(cls.temp_dir, "fixture.mp4")
        cls.ruta_audio = os.path.join(cls.temp_dir, "fixture.mp3")

        # Video de 90s (suficiente para reel 30s con clips de 10s)
        clip = ColorClip(size=(640, 480), color=(30, 120, 200), duration=90)
        clip.write_videofile(
            cls.ruta_video,
            fps=24,
            codec="libx264",
            preset="ultrafast",
            logger=None,
        )
        clip.close()

        audio = AudioClip(lambda t: 0.1 * (t % 1), duration=90, fps=44100)
        audio.write_audiofile(cls.ruta_audio, logger=None)
        audio.close()

    @classmethod
    def tearDownClass(cls) -> None:
        shutil.rmtree(cls.temp_dir, ignore_errors=True)
        super().tearDownClass()

    @patch("api_reels.services.scorer.puntuar_fotogramas_batch")
    def test_generar_reel_produce_archivo_y_tiempos(self, mock_batch) -> None:
        """Verifica salida MP4, metadatos y registro de tiempos por fase."""
        mock_batch.return_value = [float(i) for i in range(9)]

        from api_reels.services import generar_reel

        resultado = generar_reel(
            ruta_video=self.ruta_video,
            ruta_audio=self.ruta_audio,
            directorio_salida=self.directorio_salida,
            duracion_reel=30,
            duracion_clip=10,
        )

        self.assertTrue(os.path.exists(resultado["ruta_archivo"]))
        self.assertEqual(resultado["clips_seleccionados"], 3)
        self.assertEqual(resultado["clips_analizados"], 9)
        self.assertIn("tiempos_procesamiento", resultado)
        tiempos = resultado["tiempos_procesamiento"]
        self.assertIn("scoring_seg", tiempos)
        self.assertIn("render_seg", tiempos)
        self.assertIn("total_seg", tiempos)
        mock_batch.assert_called_once()

    def test_video_corto_lanza_value_error(self) -> None:
        """Un video más corto que duracion_reel debe fallar con ValueError."""
        from api_reels.services import generar_reel

        with self.assertRaises(ValueError):
            generar_reel(
                ruta_video=self.ruta_video,
                ruta_audio=self.ruta_audio,
                directorio_salida=self.directorio_salida,
                duracion_reel=120,
                duracion_clip=10,
            )
