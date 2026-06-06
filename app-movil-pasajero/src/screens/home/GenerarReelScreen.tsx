import React, { useState, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
  ScrollView,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import * as DocumentPicker from 'expo-document-picker';
import * as FileSystem from 'expo-file-system/legacy';
import * as MediaLibrary from 'expo-media-library';
import { Video, ResizeMode } from 'expo-av';
import { COLORS, TYPOGRAPHY, globalStyles } from '@/theme/theme';
import { CONFIG } from '@/utils/config';
import { styles } from './styles/GenerarReelScreen.styles';

/** Duraciones permitidas para el reel */
const DURACIONES_REEL = [15, 30, 45, 60] as const;

/** Estados posibles de la pantalla de Reels */
type EstadoReel = 'inicial' | 'analizando' | 'resultado';

/** Estadísticas devueltas por la IA */
interface EstadisticasReel {
  clipsSeleccionados: number;
  clipsAnalizados: number;
  duracion: number;
}

/**
 * Pantalla de Generación Automática de Reels Turísticos (CU-07).
 *
 * Permite al usuario seleccionar un video + audio de su galería,
 * enviarlos al Motor IA (Django) para generar un reel optimizado
 * con las mejores escenas seleccionadas por Deep Learning (MobileNetV2).
 */
export default function GenerarReelScreen() {
  const [estado, setEstado] = useState<EstadoReel>('inicial');
  const [videoUri, setVideoUri] = useState<string | null>(null);
  const [videoNombre, setVideoNombre] = useState<string | null>(null);
  const [audioUri, setAudioUri] = useState<string | null>(null);
  const [audioNombre, setAudioNombre] = useState<string | null>(null);
  const [duracionReel, setDuracionReel] = useState<number>(60);
  const [reelResultUrl, setReelResultUrl] = useState<string | null>(null);
  const [estadisticas, setEstadisticas] = useState<EstadisticasReel | null>(null);
  const [isPlaying, setIsPlaying] = useState<boolean>(true);
  const videoRef = useRef<Video>(null);

  /**
   * Abre la galería del dispositivo forzando la selección de videos.
   */
  const seleccionarVideo = async () => {
    const permiso = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permiso.granted) {
      Alert.alert('Permiso denegado', 'Necesitamos acceso a tu galería para subir el video.');
      return;
    }

    const resultado = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Videos,
      allowsEditing: true,
      quality: 1,
    });

    if (!resultado.canceled && resultado.assets && resultado.assets.length > 0) {
      const asset = resultado.assets[0];
      if (asset?.uri) {
        setVideoUri(asset.uri);
        const nombre = asset.uri.split('/').pop() || 'video.mp4';
        setVideoNombre(nombre.length > 25 ? nombre.substring(0, 22) + '...' : nombre);
      }
    }
  };

  /**
   * Abre el selector de documentos para elegir un archivo de audio (MP3).
   */
  const seleccionarAudio = async () => {
    try {
      const resultado = await DocumentPicker.getDocumentAsync({
        type: 'audio/*',
        copyToCacheDirectory: true,
      });

      if (!resultado.canceled && resultado.assets && resultado.assets.length > 0) {
        const asset = resultado.assets[0];
        if (asset?.uri) {
          setAudioUri(asset.uri);
          const nombre = asset.name || asset.uri.split('/').pop() || 'audio.mp3';
          setAudioNombre(nombre.length > 25 ? nombre.substring(0, 22) + '...' : nombre);
        }
      }
    } catch (error) {
      Alert.alert('Error', 'No se pudo seleccionar el archivo de audio.');
    }
  };

  /**
   * Envía el video y audio al Motor IA (Django) para generar el reel con Deep Learning.
   */
  const generarReelConIA = async () => {
    if (!videoUri || !audioUri) return;

    setEstado('analizando');

    try {
      // 1. Preparar archivos para envío (multipart/form-data)
      const videoFilename = videoUri.split('/').pop() || 'video.mp4';
      const audioFilename = audioUri.split('/').pop() || 'audio.mp3';

      const formData = new FormData();
      formData.append('video', {
        uri: videoUri,
        name: videoFilename,
        type: 'video/mp4',
      } as any);
      formData.append('audio', {
        uri: audioUri,
        name: audioFilename,
        type: 'audio/mpeg',
      } as any);
      formData.append('duracion_reel', duracionReel.toString());

      // 2. Enviar al Motor IA
      const response = await fetch(CONFIG.AI_REEL_URL, {
        method: 'POST',
        body: formData,
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'multipart/form-data',
        },
      });

      const json = await response.json();

      if (!response.ok || !json.exito) {
        throw new Error(json.error || json.mensaje || 'Error desconocido al generar el reel.');
      }

      // 3. Construir URL completa del reel generado
      const urlReel = `${CONFIG.AI_BASE_URL}${json.archivo_descarga}`;
      setReelResultUrl(urlReel);

      setEstadisticas({
        clipsSeleccionados: json.clips_seleccionados,
        clipsAnalizados: json.clips_analizados,
        duracion: json.duracion_reel,
      });

      setEstado('resultado');

    } catch (error: any) {
      console.error('Error al generar reel con IA:', error);
      Alert.alert(
        'Error de Conexion',
        error.message || 'No se pudo conectar con el motor de IA. Verifica que el servidor Django este corriendo.'
      );
      setEstado('inicial');
    }
  };

  /**
   * Reinicia el estado para volver a intentar con otro video.
   */
  const nuevaGeneracion = () => {
    setEstado('inicial');
    setVideoUri(null);
    setVideoNombre(null);
    setAudioUri(null);
    setAudioNombre(null);
    setReelResultUrl(null);
    setEstadisticas(null);
    setIsPlaying(true);
  };

  /**
   * Descarga el reel generado y lo guarda en la galería del dispositivo.
   */
  const guardarReel = async () => {
    if (!reelResultUrl) return;

    try {
      // Solicitar permiso SOLO de escritura para guardar en galería (evita error de permisos de AUDIO en Android)
      const { status } = await MediaLibrary.requestPermissionsAsync(true);
      if (status !== 'granted') {
        Alert.alert('Permiso denegado', 'Necesitamos acceso a tu galería para guardar el reel.');
        return;
      }

      Alert.alert('Descargando...', 'Guardando el reel en tu galeria. Esto puede tardar unos segundos.');

      // Descargar el archivo del servidor Django
      const nombreArchivo = reelResultUrl.split('/').pop() || 'reel.mp4';
      const rutaLocal = `${FileSystem.cacheDirectory}${nombreArchivo}`;

      const descarga = await FileSystem.downloadAsync(reelResultUrl, rutaLocal);

      if (descarga.status !== 200) {
        throw new Error('No se pudo descargar el archivo del servidor.');
      }

      // Guardar en la galería del dispositivo
      const asset = await MediaLibrary.createAssetAsync(descarga.uri);
      await MediaLibrary.createAlbumAsync('Viajes IA - Reels', asset, false);

      Alert.alert('¡Exito!', 'Tu reel turistico ha sido guardado en tu galeria (album: Viajes IA - Reels).');

    } catch (error: any) {
      console.error('Error al guardar reel:', error);
      Alert.alert('Error', error.message || 'No se pudo guardar el reel en tu galeria.');
    }
  };

  /** ¿Están ambos archivos seleccionados? */
  const listoParaGenerar = videoUri !== null && audioUri !== null;

  return (
    <ScrollView
      style={[globalStyles.safeAreaContainer, styles.container]}
      contentContainerStyle={styles.scrollContent}
      showsVerticalScrollIndicator={false}
    >
      {/* ─── ESTADO INICIAL ─── */}
      {estado === 'inicial' && (
        <View style={styles.centerContent}>
          <View style={styles.iconCircle}>
            <Ionicons name="film-outline" size={48} color={COLORS.secondary} />
          </View>
          <Text style={styles.title}>Reels Turisticos</Text>
          <Text style={styles.description}>
            Selecciona un video y una pista de audio. Nuestra IA seleccionara las mejores escenas para crear tu reel.
          </Text>

          {/* ── Selección de Video ── */}
          <TouchableOpacity
            style={[styles.fileSelector, videoUri && styles.fileSelectorSelected]}
            onPress={seleccionarVideo}
            activeOpacity={0.8}
          >
            <Ionicons
              name={videoUri ? 'videocam' : 'videocam-outline'}
              size={24}
              color={videoUri ? COLORS.success : COLORS.textSecondary}
            />
            <View style={styles.fileSelectorTextContainer}>
              <Text style={[styles.fileSelectorLabel, videoUri && styles.fileSelectorLabelSelected]}>
                {videoUri ? 'Video seleccionado' : 'Seleccionar video'}
              </Text>
              {videoNombre && (
                <Text style={styles.fileNameText}>{videoNombre}</Text>
              )}
            </View>
            {videoUri && (
              <Ionicons name="checkmark-circle" size={22} color={COLORS.success} />
            )}
          </TouchableOpacity>

          {/* ── Selección de Audio ── */}
          <TouchableOpacity
            style={[styles.fileSelector, audioUri && styles.fileSelectorSelected]}
            onPress={seleccionarAudio}
            activeOpacity={0.8}
          >
            <Ionicons
              name={audioUri ? 'musical-notes' : 'musical-notes-outline'}
              size={24}
              color={audioUri ? COLORS.success : COLORS.textSecondary}
            />
            <View style={styles.fileSelectorTextContainer}>
              <Text style={[styles.fileSelectorLabel, audioUri && styles.fileSelectorLabelSelected]}>
                {audioUri ? 'Audio seleccionado' : 'Seleccionar audio (MP3)'}
              </Text>
              {audioNombre && (
                <Text style={styles.fileNameText}>{audioNombre}</Text>
              )}
            </View>
            {audioUri && (
              <Ionicons name="checkmark-circle" size={22} color={COLORS.success} />
            )}
          </TouchableOpacity>

          {/* ── Selector de Duración ── */}
          <View style={styles.durationSection}>
            <Text style={styles.durationLabel}>Duracion del reel</Text>
            <View style={styles.durationChips}>
              {DURACIONES_REEL.map((dur) => (
                <TouchableOpacity
                  key={dur}
                  style={[styles.chip, duracionReel === dur && styles.chipSelected]}
                  onPress={() => setDuracionReel(dur)}
                  activeOpacity={0.8}
                >
                  <Text style={[styles.chipText, duracionReel === dur && styles.chipTextSelected]}>
                    {dur}s
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>

          {/* ── Botón Generar ── */}
          <TouchableOpacity
            style={[
              globalStyles.btnPrimary,
              styles.btnGenerate,
              !listoParaGenerar && styles.btnDisabled,
            ]}
            onPress={generarReelConIA}
            activeOpacity={0.8}
            disabled={!listoParaGenerar}
          >
            <Ionicons name="sparkles" size={22} color={COLORS.textLight} />
            <Text style={TYPOGRAPHY.buttonText}>Generar Reel con IA</Text>
          </TouchableOpacity>
        </View>
      )}

      {/* ─── ESTADO ANALIZANDO ─── */}
      {estado === 'analizando' && (
        <View style={styles.centerContent}>
          <View style={styles.analyzingCard}>
            <ActivityIndicator size="large" color={COLORS.primary} style={{ transform: [{ scale: 1.5 }] }} />
            <Text style={styles.analyzingTitle}>Generando Magia...</Text>
            <Text style={styles.analyzingText}>
              La IA esta analizando cada escena de tu video con Deep Learning (MobileNetV2) para seleccionar los mejores momentos.
            </Text>
            <Text style={styles.analyzingHint}>
              Esto puede tardar entre 30 segundos y 2 minutos dependiendo del largo del video.
            </Text>
          </View>
        </View>
      )}

      {/* ─── ESTADO RESULTADO ─── */}
      {estado === 'resultado' && reelResultUrl && (
        <View style={styles.resultContainer}>
          <View style={styles.successHeader}>
            <Ionicons name="sparkles" size={24} color={COLORS.accent} />
            <Text style={styles.successText}>¡Tu Reel esta listo!</Text>
            <Ionicons name="sparkles" size={24} color={COLORS.accent} />
          </View>

          {/* Estadísticas */}
          {estadisticas && (
            <View style={styles.statsContainer}>
              <View style={styles.statItem}>
                <Ionicons name="film-outline" size={18} color={COLORS.secondary} />
                <Text style={styles.statText}>
                  {estadisticas.clipsSeleccionados} de {estadisticas.clipsAnalizados} clips seleccionados
                </Text>
              </View>
              <View style={styles.statItem}>
                <Ionicons name="time-outline" size={18} color={COLORS.secondary} />
                <Text style={styles.statText}>
                  Duracion: {estadisticas.duracion}s
                </Text>
              </View>
            </View>
          )}

          {/* Reproductor de video con pausa personalizada */}
          <TouchableOpacity
            style={styles.videoWrapper}
            activeOpacity={0.95}
            onPress={() => setIsPlaying(!isPlaying)}
          >
            <Video
              ref={videoRef}
              source={{ uri: reelResultUrl }}
              style={styles.videoPlayer}
              resizeMode={ResizeMode.COVER}
              isLooping
              shouldPlay={isPlaying}
              isMuted={false}
            />
            {!isPlaying && (
              <View style={styles.pauseOverlay}>
                <Ionicons name="play-circle" size={80} color="rgba(255,255,255,0.8)" />
              </View>
            )}
          </TouchableOpacity>

          <View style={styles.actionButtons}>
            <TouchableOpacity style={styles.btnSave} onPress={guardarReel} activeOpacity={0.8}>
              <Ionicons name="download-outline" size={20} color={COLORS.textLight} />
              <Text style={TYPOGRAPHY.buttonText}>Guardar en Galeria</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.btnRetry} onPress={nuevaGeneracion} activeOpacity={0.8}>
              <Ionicons name="refresh" size={18} color={COLORS.secondary} />
              <Text style={styles.btnRetryText}>Nueva Generacion</Text>
            </TouchableOpacity>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
