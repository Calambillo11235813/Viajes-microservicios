import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
  ActivityIndicator,
  ScrollView,
  Alert,
  Dimensions,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';

const { width: SCREEN_WIDTH } = Dimensions.get('window');

/**
 * Destinos simulados que el "motor de IA" puede "detectar".
 * Se elige uno al azar para hacer la simulación más dinámica.
 */
const DESTINOS_SIMULADOS = [
  { nombre: 'Salar de Uyuni', departamento: 'Potosí', confianza: 96 },
  { nombre: 'Lago Titicaca', departamento: 'La Paz', confianza: 93 },
  { nombre: 'Misiones Jesuíticas de Chiquitos', departamento: 'Santa Cruz', confianza: 89 },
  { nombre: 'Valle de la Luna', departamento: 'La Paz', confianza: 91 },
  { nombre: 'Parque Nacional Torotoro', departamento: 'Potosí', confianza: 87 },
  { nombre: 'Cerro Rico de Potosí', departamento: 'Potosí', confianza: 94 },
];

/** Estados posibles de la pantalla */
type EstadoPantalla = 'inicial' | 'imagen_seleccionada' | 'analizando' | 'resultado';

/**
 * Pantalla de búsqueda de destinos mediante imágenes (CU-06).
 *
 * Permite al usuario seleccionar o capturar una foto de un paisaje boliviano,
 * simula un análisis de IA (mock con setTimeout) y muestra un destino detectado.
 *
 * Diseñada para funcionar sin dependencias nativas pesadas, utilizando
 * únicamente `expo-image-picker` (compatible con Expo Go).
 */
export default function BuscarImagenScreen() {
  const [estado, setEstado] = useState<EstadoPantalla>('inicial');
  const [imagenUri, setImagenUri] = useState<string | null>(null);
  const [destinoDetectado, setDestinoDetectado] = useState<typeof DESTINOS_SIMULADOS[0] | null>(null);

  /**
   * Abre la galería del dispositivo para seleccionar una imagen.
   * Solicita permisos automáticamente si no están concedidos.
   */
  const seleccionarDeGaleria = async () => {
    const permiso = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permiso.granted) {
      Alert.alert('Permiso denegado', 'Necesitamos acceso a tu galería para analizar la imagen.');
      return;
    }

    const resultado = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.8,
    });

    if (!resultado.canceled && resultado.assets && resultado.assets.length > 0) {
      const asset = resultado.assets[0];
      if (asset?.uri) {
        setImagenUri(asset.uri);
        setEstado('imagen_seleccionada');
        setDestinoDetectado(null);
      }
    }
  };

  /**
   * Abre la cámara del dispositivo para tomar una foto.
   * Solicita permisos automáticamente si no están concedidos.
   */
  const tomarFoto = async () => {
    const permiso = await ImagePicker.requestCameraPermissionsAsync();
    if (!permiso.granted) {
      Alert.alert('Permiso denegado', 'Necesitamos acceso a tu cámara para tomar la foto.');
      return;
    }

    const resultado = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.8,
    });

    if (!resultado.canceled && resultado.assets && resultado.assets.length > 0) {
      const uri = resultado.assets[0];
      if (uri) {
        setImagenUri(resultado.assets[0].uri);
        setEstado('imagen_seleccionada');
        setDestinoDetectado(null);
      }
    }
  };

  /**
   * Simula el análisis de IA con un delay de 3 segundos.
   * Selecciona un destino aleatorio del arreglo de mocks.
   */
  const analizarImagen = () => {
    setEstado('analizando');

    setTimeout(() => {
      const indice = Math.floor(Math.random() * DESTINOS_SIMULADOS.length);
      setDestinoDetectado(DESTINOS_SIMULADOS[indice]);
      setEstado('resultado');
    }, 3000);
  };

  /**
   * Reinicia la pantalla al estado inicial para una nueva búsqueda.
   */
  const reiniciar = () => {
    setEstado('inicial');
    setImagenUri(null);
    setDestinoDetectado(null);
  };

  return (
    <ScrollView
      style={globalStyles.safeAreaContainer}
      contentContainerStyle={styles.scrollContent}
      showsVerticalScrollIndicator={false}
    >
      {/* ─── Encabezado ─── */}
      <View style={styles.headerSection}>
        <View style={styles.iconCircle}>
          <Ionicons name="camera" size={32} color={COLORS.textLight} />
        </View>
        <Text style={styles.title}>Buscar por Imagen</Text>
        <Text style={styles.description}>
          Sube o toma una foto de un paisaje boliviano y nuestra IA identificará el destino turístico.
        </Text>
      </View>

      {/* ─── Zona de imagen ─── */}
      {imagenUri ? (
        <View style={styles.imageContainer}>
          <Image source={{ uri: imagenUri }} style={styles.previewImage} resizeMode="cover" />

          {estado === 'analizando' && (
            <View style={styles.analyzingOverlay}>
              <ActivityIndicator size="large" color={COLORS.textLight} />
              <Text style={styles.analyzingText}>Analizando paisaje con IA...</Text>
              <View style={styles.progressBarContainer}>
                <View style={styles.progressBarFill} />
              </View>
            </View>
          )}
        </View>
      ) : (
        <View style={styles.placeholderContainer}>
          <Ionicons name="image-outline" size={64} color={COLORS.placeholder} />
          <Text style={styles.placeholderText}>
            Selecciona una imagen para comenzar el análisis
          </Text>
        </View>
      )}

      {/* ─── Botones de acción ─── */}
      {estado === 'inicial' && (
        <View style={styles.actionsContainer}>
          <TouchableOpacity style={styles.btnGallery} onPress={seleccionarDeGaleria} activeOpacity={0.8}>
            <Ionicons name="images-outline" size={22} color={COLORS.textLight} />
            <Text style={styles.btnText}>Subir de Galería</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.btnCamera} onPress={tomarFoto} activeOpacity={0.8}>
            <Ionicons name="camera-outline" size={22} color={COLORS.secondary} />
            <Text style={styles.btnCameraText}>Tomar Foto</Text>
          </TouchableOpacity>
        </View>
      )}

      {estado === 'imagen_seleccionada' && (
        <View style={styles.actionsContainer}>
          <TouchableOpacity style={styles.btnAnalyze} onPress={analizarImagen} activeOpacity={0.8}>
            <Ionicons name="sparkles" size={22} color={COLORS.textLight} />
            <Text style={styles.btnText}>Analizar con IA</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.btnRetry} onPress={reiniciar} activeOpacity={0.8}>
            <Ionicons name="refresh-outline" size={20} color={COLORS.textSecondary} />
            <Text style={styles.btnRetryText}>Elegir otra imagen</Text>
          </TouchableOpacity>
        </View>
      )}

      {/* ─── Resultado de IA ─── */}
      {estado === 'resultado' && destinoDetectado && (
        <View style={styles.resultCard}>
          <View style={styles.resultHeader}>
            <Ionicons name="checkmark-circle" size={24} color={COLORS.success} />
            <Text style={styles.resultTitle}>Destino Detectado</Text>
          </View>

          <View style={styles.resultBody}>
            <Text style={styles.destinoNombre}>{destinoDetectado.nombre}</Text>
            <View style={styles.resultRow}>
              <Ionicons name="location-outline" size={16} color={COLORS.textSecondary} />
              <Text style={styles.resultDetail}>{destinoDetectado.departamento}, Bolivia</Text>
            </View>
            <View style={styles.resultRow}>
              <Ionicons name="analytics-outline" size={16} color={COLORS.textSecondary} />
              <Text style={styles.resultDetail}>Confianza: {destinoDetectado.confianza}%</Text>
            </View>

            <View style={styles.confidenceBarBackground}>
              <View style={[styles.confidenceBarFill, { width: `${destinoDetectado.confianza}%` }]} />
            </View>
          </View>

          <TouchableOpacity style={styles.btnRoutes} activeOpacity={0.8}>
            <Ionicons name="navigate-outline" size={20} color={COLORS.textLight} />
            <Text style={styles.btnText}>Ver rutas disponibles</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.btnNewSearch} onPress={reiniciar} activeOpacity={0.8}>
            <Ionicons name="camera-outline" size={18} color={COLORS.secondary} />
            <Text style={styles.btnNewSearchText}>Nueva búsqueda</Text>
          </TouchableOpacity>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scrollContent: {
    paddingBottom: SPACING.xxl * 2,
  },

  /* ── Encabezado ── */
  headerSection: {
    alignItems: 'center',
    paddingTop: SPACING.lg,
    paddingHorizontal: SPACING.lg,
    paddingBottom: SPACING.md,
  },
  iconCircle: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: COLORS.secondary,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: SPACING.md,
    shadowColor: COLORS.secondary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  title: {
    ...TYPOGRAPHY.h1,
    textAlign: 'center',
    marginBottom: SPACING.xs,
  },
  description: {
    ...TYPOGRAPHY.body,
    textAlign: 'center',
    color: COLORS.textSecondary,
    lineHeight: 22,
    paddingHorizontal: SPACING.md,
  },

  /* ── Imagen ── */
  imageContainer: {
    width: SCREEN_WIDTH - (SPACING.md * 2),
    marginHorizontal: SPACING.md,
    marginTop: SPACING.md,
    borderRadius: 16,
    //overflow: 'hidden',
    backgroundColor: '#e2e8f0', // Fondo gris claro temporal para ver si el contenedor se dibuja
    elevation: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
  },
  previewImage: {
    width: SCREEN_WIDTH - (SPACING.md * 2),
    height: 240,
    borderRadius: 16,
  },
  analyzingOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(15, 23, 42, 0.75)',
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 16,
  },
  analyzingText: {
    ...TYPOGRAPHY.h3,
    color: COLORS.textLight,
    marginTop: SPACING.md,
  },
  progressBarContainer: {
    width: '60%',
    height: 4,
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: 2,
    marginTop: SPACING.md,
    overflow: 'hidden',
  },
  progressBarFill: {
    width: '70%',
    height: '100%',
    backgroundColor: COLORS.accent,
    borderRadius: 2,
  },

  /* ── Placeholder ── */
  placeholderContainer: {
    marginHorizontal: SPACING.md,
    marginTop: SPACING.md,
    height: 200,
    borderRadius: 16,
    borderWidth: 2,
    borderColor: COLORS.border,
    borderStyle: 'dashed',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: COLORS.surface,
  },
  placeholderText: {
    ...TYPOGRAPHY.body,
    color: COLORS.placeholder,
    marginTop: SPACING.sm,
    textAlign: 'center',
    paddingHorizontal: SPACING.xl,
  },

  /* ── Botones ── */
  actionsContainer: {
    paddingHorizontal: SPACING.md,
    marginTop: SPACING.lg,
    gap: SPACING.sm,
  },
  btnGallery: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: COLORS.secondary,
    paddingVertical: SPACING.md,
    borderRadius: 14,
    gap: SPACING.sm,
    elevation: 3,
    shadowColor: COLORS.secondary,
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.25,
    shadowRadius: 6,
  },
  btnCamera: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'transparent',
    paddingVertical: SPACING.md,
    borderRadius: 14,
    borderWidth: 1.5,
    borderColor: COLORS.secondary,
    gap: SPACING.sm,
  },
  btnCameraText: {
    ...TYPOGRAPHY.buttonText,
    color: COLORS.secondary,
  },
  btnAnalyze: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: COLORS.accent,
    paddingVertical: SPACING.md,
    borderRadius: 14,
    gap: SPACING.sm,
    elevation: 3,
    shadowColor: COLORS.accent,
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.25,
    shadowRadius: 6,
  },
  btnRetry: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: SPACING.sm,
    gap: SPACING.xs,
  },
  btnRetryText: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
  },
  btnText: {
    ...TYPOGRAPHY.buttonText,
    color: COLORS.textLight,
  },

  /* ── Resultado ── */
  resultCard: {
    marginHorizontal: SPACING.md,
    marginTop: SPACING.lg,
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: SPACING.lg,
    borderWidth: 1,
    borderColor: COLORS.border,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
  },
  resultHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: SPACING.sm,
    marginBottom: SPACING.md,
    paddingBottom: SPACING.sm,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
  },
  resultTitle: {
    ...TYPOGRAPHY.h3,
    color: COLORS.success,
  },
  resultBody: {
    marginBottom: SPACING.md,
  },
  destinoNombre: {
    ...TYPOGRAPHY.h2,
    color: COLORS.primary,
    marginBottom: SPACING.sm,
  },
  resultRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: SPACING.xs,
    marginBottom: 4,
  },
  resultDetail: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
  },
  confidenceBarBackground: {
    width: '100%',
    height: 6,
    backgroundColor: COLORS.border,
    borderRadius: 3,
    marginTop: SPACING.sm,
    overflow: 'hidden',
  },
  confidenceBarFill: {
    height: '100%',
    backgroundColor: COLORS.success,
    borderRadius: 3,
  },
  btnRoutes: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: COLORS.primary,
    paddingVertical: SPACING.md,
    borderRadius: 14,
    gap: SPACING.sm,
    marginBottom: SPACING.sm,
  },
  btnNewSearch: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: SPACING.sm,
    gap: SPACING.xs,
  },
  btnNewSearchText: {
    ...TYPOGRAPHY.body,
    color: COLORS.secondary,
    fontWeight: '600',
  },
});
