import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';

type Idioma = 'ES_EN' | 'EN_ES';
type EstadoTraduccion = 'enfocando' | 'traduciendo' | 'resultado';

/**
 * Pantalla de Traducción Visual (CU-08).
 * Utiliza la cámara en tiempo real para simular detección de texto (OCR)
 * y traducción usando procesamiento de lenguaje natural (NLP).
 */
export default function TraducirVisualScreen() {
  const [permiso, pedirPermiso] = useCameraPermissions();
  const [idioma, setIdioma] = useState<Idioma>('EN_ES');
  const [estado, setEstado] = useState<EstadoTraduccion>('enfocando');
  const [textoTraducido, setTextoTraducido] = useState('');

  // Solicitar permisos al montar si no se han determinado
  useEffect(() => {
    if (permiso && !permiso.granted && permiso.canAskAgain) {
      pedirPermiso();
    }
  }, [permiso, pedirPermiso]);

  if (!permiso) {
    return <View style={globalStyles.safeAreaContainer} />;
  }

  if (!permiso.granted) {
    return (
      <View style={[globalStyles.safeAreaContainer, styles.centerContent]}>
        <Ionicons name="camera-outline" size={64} color={COLORS.placeholder} />
        <Text style={styles.permissionText}>
          Necesitamos acceso a tu cámara para poder traducir los carteles y textos en tu entorno.
        </Text>
        <TouchableOpacity style={globalStyles.btnPrimary} onPress={pedirPermiso}>
          <Text style={TYPOGRAPHY.buttonText}>Conceder Permiso</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const alternarIdioma = () => {
    setIdioma((prev) => (prev === 'EN_ES' ? 'ES_EN' : 'EN_ES'));
    // Si estaba en resultado, volver a enfocar al cambiar idioma
    if (estado === 'resultado') setEstado('enfocando');
  };

  const traducirTexto = () => {
    setEstado('traduciendo');

    // Simulación de OCR y NLP (2.5 segundos)
    setTimeout(() => {
      if (idioma === 'EN_ES') {
        setTextoTraducido('MOCK: EXIT ➔ SALIDA');
      } else {
        setTextoTraducido('MOCK: PARADA ➔ BUS STOP');
      }
      setEstado('resultado');
    }, 2500);
  };

  const reintentar = () => {
    setEstado('enfocando');
    setTextoTraducido('');
  };

  return (
    <View style={styles.container}>
      {/* ─── CÁMARA DE FONDO ─── */}
      <CameraView style={styles.camera} facing="back">
        
        {/* ─── OVERLAY SUPERIOR (Selector de Idioma) ─── */}
        <View style={styles.topOverlay}>
          <TouchableOpacity style={styles.langSelector} onPress={alternarIdioma} activeOpacity={0.8}>
            <Text style={styles.langText}>
              {idioma === 'EN_ES' ? 'Inglés' : 'Español'}
            </Text>
            <Ionicons name="swap-horizontal" size={20} color={COLORS.textLight} />
            <Text style={styles.langText}>
              {idioma === 'EN_ES' ? 'Español' : 'Inglés'}
            </Text>
          </TouchableOpacity>
        </View>

        {/* ─── ZONA CENTRAL (Enfoque / Procesando / Resultado) ─── */}
        <View style={styles.centerOverlay}>
          
          {estado === 'enfocando' && (
            <View style={styles.focusFrame}>
              {/* Esquinas del recuadro de enfoque */}
              <View style={[styles.corner, styles.topLeft]} />
              <View style={[styles.corner, styles.topRight]} />
              <View style={[styles.corner, styles.bottomLeft]} />
              <View style={[styles.corner, styles.bottomRight]} />
              <Text style={styles.focusText}>Enfoca el texto aquí</Text>
            </View>
          )}

          {estado === 'traduciendo' && (
            <View style={styles.processingCard}>
              <ActivityIndicator size="large" color={COLORS.primary} />
              <Text style={styles.processingText}>Aplicando OCR y NLP...</Text>
            </View>
          )}

          {estado === 'resultado' && (
            <View style={styles.resultCard}>
              <Ionicons name="language" size={24} color={COLORS.success} />
              <Text style={styles.resultText}>{textoTraducido}</Text>
            </View>
          )}
        </View>

        {/* ─── OVERLAY INFERIOR (Botones) ─── */}
        <View style={styles.bottomOverlay}>
          {estado === 'enfocando' && (
            <TouchableOpacity style={styles.btnTranslate} onPress={traducirTexto} activeOpacity={0.9}>
              <Ionicons name="scan-outline" size={24} color={COLORS.textLight} />
              <Text style={styles.btnTranslateText}>Traducir Texto</Text>
            </TouchableOpacity>
          )}

          {estado === 'resultado' && (
            <TouchableOpacity style={styles.btnRetry} onPress={reintentar} activeOpacity={0.8}>
              <Ionicons name="refresh" size={20} color={COLORS.textLight} />
              <Text style={styles.btnRetryText}>Nueva Traducción</Text>
            </TouchableOpacity>
          )}
        </View>

      </CameraView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
  },
  centerContent: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: SPACING.xl,
    backgroundColor: COLORS.background,
  },
  permissionText: {
    ...TYPOGRAPHY.bodyLarge,
    color: COLORS.textSecondary,
    textAlign: 'center',
    marginVertical: SPACING.lg,
  },
  camera: {
    flex: 1,
  },

  /* ── Overlays Generales ── */
  topOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    paddingTop: SPACING.xl,
    alignItems: 'center',
    zIndex: 10,
  },
  bottomOverlay: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    paddingBottom: SPACING.xxl * 1.5,
    alignItems: 'center',
    zIndex: 10,
  },
  centerOverlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },

  /* ── Selector de Idioma ── */
  langSelector: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.6)',
    paddingVertical: SPACING.sm,
    paddingHorizontal: SPACING.lg,
    borderRadius: 20,
    gap: SPACING.md,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  langText: {
    ...TYPOGRAPHY.bodyLarge,
    color: COLORS.textLight,
    fontWeight: '600',
  },

  /* ── Recuadro de Enfoque ── */
  focusFrame: {
    width: 280,
    height: 150,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0,0,0,0.2)',
  },
  focusText: {
    ...TYPOGRAPHY.body,
    color: 'rgba(255, 255, 255, 0.8)',
    backgroundColor: 'rgba(0,0,0,0.5)',
    paddingHorizontal: SPACING.sm,
    paddingVertical: 4,
    borderRadius: 8,
  },
  corner: {
    position: 'absolute',
    width: 30,
    height: 30,
    borderColor: COLORS.accent,
  },
  topLeft: { top: 0, left: 0, borderTopWidth: 4, borderLeftWidth: 4 },
  topRight: { top: 0, right: 0, borderTopWidth: 4, borderRightWidth: 4 },
  bottomLeft: { bottom: 0, left: 0, borderBottomWidth: 4, borderLeftWidth: 4 },
  bottomRight: { bottom: 0, right: 0, borderBottomWidth: 4, borderRightWidth: 4 },

  /* ── Botones Inferiores ── */
  btnTranslate: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.primary,
    paddingVertical: SPACING.md,
    paddingHorizontal: SPACING.xl,
    borderRadius: 30,
    gap: SPACING.sm,
    elevation: 6,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  btnTranslateText: {
    ...TYPOGRAPHY.h3,
    color: COLORS.textLight,
  },
  btnRetry: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(0,0,0,0.7)',
    paddingVertical: SPACING.md,
    paddingHorizontal: SPACING.xl,
    borderRadius: 30,
    gap: SPACING.sm,
    borderWidth: 1,
    borderColor: COLORS.textLight,
  },
  btnRetryText: {
    ...TYPOGRAPHY.bodyLarge,
    color: COLORS.textLight,
  },

  /* ── Estados de IA ── */
  processingCard: {
    backgroundColor: COLORS.surface,
    padding: SPACING.xl,
    borderRadius: 20,
    alignItems: 'center',
    elevation: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
  },
  processingText: {
    ...TYPOGRAPHY.bodyLarge,
    color: COLORS.primary,
    marginTop: SPACING.md,
    fontWeight: '600',
  },
  resultCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    padding: SPACING.lg,
    borderRadius: 16,
    alignItems: 'center',
    maxWidth: '80%',
    flexDirection: 'row',
    gap: SPACING.md,
    elevation: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 10,
  },
  resultText: {
    ...TYPOGRAPHY.h3,
    color: COLORS.textPrimary,
  },
});
