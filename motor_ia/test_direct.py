import os
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'motor_ia.settings')
import django
django.setup()

import sys
sys.path.append('/app')
from deeplearning.CU07_generar_reels.api_reels.tasks import generar_reel_task

print('Iniciando prueba directa con IA original...')
resultado = generar_reel_task(
    ruta_video='/app/video.mp4',
    ruta_audio='/app/audio.mp3',
    duracion_reel=15,
    duracion_clip=5
)
print('RESULTADO:', resultado)
