import React, { useState, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
  Dimensions,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { Video, ResizeMode } from 'expo-av';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';

const { width: SCREEN_WIDTH } = Dimensions.get('window');

/** Estados posibles de la pantalla de Reels */
type EstadoReel = 'inicial' | 'analizando' | 'resultado';

/**
 * Pantalla de Generación Automática de Reels Turísticos.
 *
 * Permite al usuario seleccionar un video de su galería y simula un
 * procesamiento de Deep Learning para crear un reel turístico.
 */
export default function GenerarReelScreen() {
  const [estado, setEstado] = useState<EstadoReel>('inicial');
  const [videoUri, setVideoUri] = useState<string | null>(null);
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
        simularProcesamientoIA();
      }
    }
  };

  /**
   * Simula el análisis de IA con un delay de 4 segundos.
   */
  const simularProcesamientoIA = () => {
    setEstado('analizando');

    setTimeout(() => {
      setEstado('resultado');
    }, 4000);
  };

  /**
   * Reinicia el estado para volver a intentar con otro video.
   */
  const nuevaGeneracion = () => {
    setEstado('inicial');
    setVideoUri(null);
  };

  /**
   * Acción falsa para simular que se guarda el reel.
   */
  const guardarReel = () => {
    Alert.alert('¡Éxito!', 'Tu reel turístico ha sido guardado en tu galería.');
  };

  return (
    <View style={[globalStyles.safeAreaContainer, styles.container]}>
      {/* ─── ESTADO INICIAL ─── */}
      {estado === 'inicial' && (
        <View style={styles.centerContent}>
          <View style={styles.iconCircle}>
            <Ionicons name="film-outline" size={48} color={COLORS.secondary} />
          </View>
          <Text style={styles.title}>Reels Turísticos</Text>
          <Text style={styles.description}>
            Selecciona un video de tus viajes y nuestra IA generará un reel optimizado con las mejores escenas.
          </Text>

          <TouchableOpacity
            style={[globalStyles.btnPrimary, styles.btnUpload]}
            onPress={seleccionarVideo}
            activeOpacity={0.8}
          >
            <Ionicons name="cloud-upload-outline" size={22} color={COLORS.textLight} />
            <Text style={TYPOGRAPHY.buttonText}>Subir Video de Galería</Text>
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
              Analizando escenas y generando reel con Deep Learning...
            </Text>
          </View>
        </View>
      )}

      {/* ─── ESTADO RESULTADO ─── */}
      {estado === 'resultado' && videoUri && (
        <View style={styles.resultContainer}>
          <View style={styles.successHeader}>
            <Ionicons name="sparkles" size={24} color={COLORS.accent} />
            <Text style={styles.successText}>¡Tu Reel está listo!</Text>
            <Ionicons name="sparkles" size={24} color={COLORS.accent} />
          </View>

          <View style={styles.videoWrapper}>
            <Video
              ref={videoRef}
              source={{ uri: videoUri }}
              style={styles.videoPlayer}
              resizeMode={ResizeMode.COVER}
              isLooping
              shouldPlay
              isMuted={false}
            />
          </View>

          <View style={styles.actionButtons}>
            <TouchableOpacity style={styles.btnSave} onPress={guardarReel} activeOpacity={0.8}>
              <Ionicons name="download-outline" size={20} color={COLORS.textLight} />
              <Text style={TYPOGRAPHY.buttonText}>Guardar Reel</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.btnRetry} onPress={nuevaGeneracion} activeOpacity={0.8}>
              <Ionicons name="refresh" size={18} color={COLORS.secondary} />
              <Text style={styles.btnRetryText}>Nueva Generación</Text>
            </TouchableOpacity>
          </View>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: COLORS.background,
  },
  centerContent: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: SPACING.xl,
  },
  iconCircle: {
    width: 96,
    height: 96,
    borderRadius: 48,
    backgroundColor: COLORS.secondary + '15',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: SPACING.lg,
    borderWidth: 2,
    borderColor: COLORS.secondary + '40',
  },
  title: {
    ...TYPOGRAPHY.h1,
    color: COLORS.primary,
    marginBottom: SPACING.sm,
    textAlign: 'center',
  },
  description: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
    textAlign: 'center',
    marginBottom: SPACING.xxl,
    lineHeight: 24,
  },
  btnUpload: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: SPACING.sm,
    width: '100%',
    paddingVertical: SPACING.md + 4,
    borderRadius: 16,
    elevation: 4,
    shadowColor: COLORS.primary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },

  /* ── Analizando ── */
  analyzingCard: {
    backgroundColor: COLORS.surface,
    padding: SPACING.xxl,
    borderRadius: 24,
    alignItems: 'center',
    width: '100%',
    elevation: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.1,
    shadowRadius: 16,
  },
  analyzingTitle: {
    ...TYPOGRAPHY.h2,
    color: COLORS.primary,
    marginTop: SPACING.xl,
    marginBottom: SPACING.sm,
  },
  analyzingText: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
    textAlign: 'center',
    lineHeight: 22,
  },

  /* ── Resultado ── */
  resultContainer: {
    flex: 1,
    alignItems: 'center',
    paddingTop: SPACING.lg,
  },
  successHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: SPACING.sm,
    marginBottom: SPACING.lg,
  },
  successText: {
    ...TYPOGRAPHY.h2,
    color: COLORS.primary,
  },
  videoWrapper: {
    width: SCREEN_WIDTH - SPACING.md * 2,
    height: (SCREEN_WIDTH - SPACING.md * 2) * (16 / 9), // Proporción vertical de reel (16:9)
    maxHeight: '65%',
    backgroundColor: '#000',
    borderRadius: 20,
    overflow: 'hidden',
    elevation: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.2,
    shadowRadius: 12,
  },
  videoPlayer: {
    width: '100%',
    height: '100%',
  },
  actionButtons: {
    width: '100%',
    paddingHorizontal: SPACING.lg,
    marginTop: SPACING.xl,
    gap: SPACING.md,
  },
  btnSave: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: SPACING.sm,
    backgroundColor: COLORS.accent,
    paddingVertical: SPACING.md,
    borderRadius: 14,
    elevation: 4,
    shadowColor: COLORS.accent,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  btnRetry: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: SPACING.xs,
    paddingVertical: SPACING.sm,
  },
  btnRetryText: {
    ...TYPOGRAPHY.body,
    color: COLORS.secondary,
    fontWeight: '600',
  },
});
