import React, { useState, useMemo, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Image,
  ActivityIndicator,
  FlatList,
  Alert,
  Platform,
  DeviceEventEmitter,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import DateTimePicker from '@react-native-community/datetimepicker';
import { COLORS, SPACING, globalStyles } from '@/theme/theme';
import { CONFIG } from '@/utils/config';
import { useQuery } from '@apollo/client/react';
import { apolloClient } from '@/graphql/client';
import { BUSCAR_VIAJES_DESTINO_TURISTICO, LISTAR_ORIGENES_DESTINO_TURISTICO } from '@/graphql/queries/viajes';
import SelectModal from '@/components/SelectModal';
import TripCard from '@/components/TripCard';
import { styles } from './styles/BuscarImagenScreen.styles';

interface DestinoDetectado {
  /** Nombre legible mostrado al usuario */
  nombre: string;
  /** Identificador del catálogo (valor devuelto por la IA / BD) */
  nombreCatalogo: string;
  departamento: string;
  confianza: number;
}

/** Nombres amigables para la respuesta de predicción de la IA */
const NOMBRES_DESTINO_DISPLAY: Record<string, string> = {
  Uyuni: 'Salar de Uyuni',
  Cristo_ConCordia: 'Cristo de la ConCordia',
};

function formatearNombreDestino(codigoDestino: string): { nombre: string; nombreCatalogo: string } {
  const nombreCatalogo = codigoDestino.trim();
  const nombre = NOMBRES_DESTINO_DISPLAY[nombreCatalogo] ?? nombreCatalogo.replace(/_/g, ' ');
  return { nombre, nombreCatalogo };
}

/** Estados posibles de la pantalla */
type EstadoPantalla = 'inicial' | 'imagen_seleccionada' | 'analizando' | 'resultado';

/**
 * Pantalla de búsqueda de destinos mediante imágenes (CU-06).
 *
 * Permite al usuario seleccionar una foto de un paisaje boliviano desde la galería,
 * simula un análisis de IA (mock con setTimeout) y muestra un destino detectado.
 *
 * Diseñada para funcionar sin dependencias nativas pesadas, utilizando
 * únicamente `expo-image-picker` (compatible con Expo Go).
 */
export default function BuscarImagenScreen() {
  const [estado, setEstado] = useState<EstadoPantalla>('inicial');
  const [imagenUri, setImagenUri] = useState<string | null>(null);
  const [destinoDetectado, setDestinoDetectado] = useState<DestinoDetectado | null>(null);

  const [viajesData, setViajesData] = useState<any[]>([]);
  const [loadingViajes, setLoadingViajes] = useState<boolean>(false);
  const [errorViajes, setErrorViajes] = useState<Error | null>(null);

  // --- Estados para Paginación y Filtros ---
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [filtroFecha, setFiltroFecha] = useState('');
  const [filtroOrigen, setFiltroOrigen] = useState('');

  // Estados para modales de UI
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showOriginModal, setShowOriginModal] = useState(false);
  const [dateObj, setDateObj] = useState(new Date());

  const { data: origenesData, loading: loadingOrigenes } = useQuery<any>(LISTAR_ORIGENES_DESTINO_TURISTICO, {
    variables: { nombreDestino: destinoDetectado?.nombreCatalogo ?? '' },
    skip: !destinoDetectado,
    fetchPolicy: 'network-only',
  });

  const origenesOpciones = useMemo(() => {
    const origenes = origenesData?.listarOrigenesHaciaDestinoTuristico?.origenes ?? [];
    return ['Todos', ...origenes];
  }, [origenesData]);

  useEffect(() => {
    const departamento = origenesData?.listarOrigenesHaciaDestinoTuristico?.departamento;
    if (!departamento) return;
    setDestinoDetectado((prev) => {
      if (!prev || prev.departamento === departamento) return prev;
      return { ...prev, departamento };
    });
  }, [origenesData]);

  useEffect(() => {
    if (filtroOrigen && !origenesOpciones.includes(filtroOrigen)) {
      setFiltroOrigen('');
    }
  }, [origenesOpciones, filtroOrigen]);

  const onChangeDate = (event: any, selectedDate?: Date) => {
    setShowDatePicker(Platform.OS === 'ios');
    if (selectedDate) {
      setDateObj(selectedDate);
      setFiltroFecha(selectedDate.toISOString().split('T')[0]);
    }
  };

  const onSelectOrigen = (option: string) => {
    if (option === 'Todos') {
      setFiltroOrigen('');
    } else {
      setFiltroOrigen(option);
    }
    setShowOriginModal(false);
  };

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
   * Envía la imagen al Motor IA (Django) para su análisis real mediante Deep Learning.
   */
  const analizarImagen = async () => {
    if (!imagenUri) return;

    setEstado('analizando');

    try {
      // 1. Preparar el archivo de imagen para envío (multipart/form-data)
      const filename = imagenUri.split('/').pop() || 'imagen.jpg';
      const match = /\.(\w+)$/.exec(filename);
      const type = match ? `image/${match[1]}` : 'image/jpeg';

      const formData = new FormData();
      // Nota: React Native requiere este formato específico (any) para enviar archivos
      formData.append('imagen', {
        uri: imagenUri,
        name: filename,
        type: type,
      } as any);

      // 2. Hacer la petición POST al endpoint de predicción (Django Motor IA)
      const response = await fetch(CONFIG.AI_API_URL, {
        method: 'POST',
        body: formData,
        headers: {
          'Accept': 'application/json',
        },
      });

      const json = await response.json();

      // El servidor responde con status != 200 y { error } ante fallos.
      if (!response.ok) {
        throw new Error(json.error || 'Error desconocido en el servidor de IA.');
      }

      // 3. Procesar respuesta y actualizar estado.
      // Respuesta reconocida:   { reconocido: true, destino: "Cristo_ConCordia", confianza: 0.95 }
      // Respuesta no reconocida: { reconocido: false, mensaje: "...", confianza_maxima: 0.4 }
      if (!json.reconocido) {
        Alert.alert(
          'Sin coincidencias',
          json.mensaje || 'La imagen no corresponde a ningún destino conocido.'
        );
        setEstado('imagen_seleccionada');
        return;
      }

      const { nombre, nombreCatalogo } = formatearNombreDestino(json.destino);

      setDestinoDetectado({
        nombre,
        nombreCatalogo,
        departamento: '',
        confianza: Math.round(json.confianza * 100), // Convertir 0.95 a 95%
      });
      setFiltroOrigen('');
      setEstado('resultado');

    } catch (error: any) {
      console.error('Error al analizar imagen con IA:', error);
      Alert.alert(
        'Error de Conexión',
        error.message || 'No se pudo conectar con el motor de Inteligencia Artificial. Verifica que el servidor Django esté corriendo y la IP sea correcta.'
      );
      setEstado('imagen_seleccionada');
    }
  };

  /**
   * Reinicia la pantalla al estado inicial para una nueva búsqueda.
   */
  const reiniciar = () => {
    setEstado('inicial');
    setImagenUri(null);
    setDestinoDetectado(null);
    setViajesData([]);
    setErrorViajes(null);
    setPage(0);
    setHasMore(true);
    setFiltroFecha('');
    setFiltroOrigen('');
    setDateObj(new Date());
  };

  const aplicarFiltros = () => {
    setPage(0);
    setHasMore(true);
    setViajesData([]);
    // Usamos timeout para dejar que el estado se actualice antes de cargar
    setTimeout(() => {
      cargarRutas(0);
    }, 0);
  };

  const verRutasDisponibles = () => {
    aplicarFiltros();
  };

  const cargarRutas = async (pageNum: number) => {
    if (!destinoDetectado || loadingViajes) return;
    if (pageNum === 0) {
      setViajesData([]);
    }

    setLoadingViajes(true);
    setErrorViajes(null);
    try {
      const response = await apolloClient.query({
        query: BUSCAR_VIAJES_DESTINO_TURISTICO,
        variables: {
          nombreDestino: destinoDetectado.nombreCatalogo,
          page: pageNum,
          size: 10,
          fecha: filtroFecha || null,
          origen: filtroOrigen || null
        },
        fetchPolicy: 'network-only'
      });

      const data: any = response.data;
      const nuevosViajes = data?.buscarViajesPorDestinoTuristico?.viajesDisponibles || [];
      if (nuevosViajes.length < 10) {
        setHasMore(false);
      }

      if (pageNum === 0) {
        setViajesData(nuevosViajes);
      } else {
        setViajesData(prev => [...prev, ...nuevosViajes]);
      }
      setPage(pageNum);
    } catch (err: any) {
      console.error("Error GraphQL al cargar viajes:", err);
      setErrorViajes(err);
    } finally {
      setLoadingViajes(false);
    }
  };

  const loadMore = () => {
    if (!loadingViajes && hasMore && viajesData.length > 0) {
      cargarRutas(page + 1);
    }
  };

  const renderHeader = () => (
    <View style={styles.scrollContent}>
      {/* ─── Encabezado ─── */}
      <View style={styles.headerSection}>
        <View style={styles.iconCircle}>
          <Ionicons name="camera" size={32} color={COLORS.textLight} />
        </View>
        <Text style={styles.title}>Buscar por Imagen</Text>
        <Text style={styles.description}>
          Sube una foto de un paisaje boliviano y nuestra IA identificará el destino turístico.
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
              <Text style={styles.resultDetail}>
                {destinoDetectado.departamento ? `${destinoDetectado.departamento}, Bolivia` : 'Bolivia'}
              </Text>
            </View>
            <View style={styles.resultRow}>
              <Ionicons name="analytics-outline" size={16} color={COLORS.textSecondary} />
              <Text style={styles.resultDetail}>Confianza: {destinoDetectado.confianza}%</Text>
            </View>

            <View style={styles.confidenceBarBackground}>
              <View style={[styles.confidenceBarFill, { width: `${destinoDetectado.confianza}%` }]} />
            </View>
          </View>

          {/* Filtros Opcionales */}
          <View style={styles.filtersContainer}>
            <Text style={styles.filtersTitle}>Filtros (Opcionales)</Text>
            <View style={globalStyles.inputContainer}>
              <Text style={globalStyles.inputLabel}>Origen</Text>
              <TouchableOpacity
                style={globalStyles.inputField}
                onPress={() => !loadingOrigenes && origenesOpciones.length > 1 && setShowOriginModal(true)}
                activeOpacity={loadingOrigenes || origenesOpciones.length <= 1 ? 1 : 0.7}
              >
                <Text style={{ color: filtroOrigen ? COLORS.textPrimary : COLORS.placeholder }}>
                  {loadingOrigenes
                    ? 'Cargando orígenes...'
                    : filtroOrigen || 'Seleccionar Origen (Todos)'}
                </Text>
              </TouchableOpacity>
            </View>
            <View style={globalStyles.inputContainer}>
              <Text style={globalStyles.inputLabel}>Fecha (YYYY-MM-DD)</Text>
              <TouchableOpacity
                style={globalStyles.inputField}
                onPress={() => setShowDatePicker(true)}
              >
                <Text style={{ color: filtroFecha ? COLORS.textPrimary : COLORS.placeholder }}>
                  {filtroFecha || 'Seleccionar fecha'}
                </Text>
              </TouchableOpacity>

              {showDatePicker && (
                <DateTimePicker
                  value={dateObj}
                  mode="date"
                  display="default"
                  onChange={onChangeDate}
                />
              )}
            </View>
          </View>

          <TouchableOpacity style={styles.btnRoutes} onPress={verRutasDisponibles} activeOpacity={0.8}>
            <Ionicons name="navigate-outline" size={20} color={COLORS.textLight} />
            <Text style={styles.btnText}>Ver rutas disponibles</Text>
          </TouchableOpacity>

          {loadingViajes && <ActivityIndicator size="small" color={COLORS.primary} style={{ marginVertical: SPACING.md }} />}
          {errorViajes && <Text style={{ color: COLORS.danger, textAlign: 'center' }}>Error al cargar viajes: {errorViajes.message}</Text>}

          {viajesData.length > 0 && (
            <View style={styles.tripsContainer}>
              <Text style={styles.tripsHeader}>Viajes Encontrados</Text>
            </View>
          )}

          <TouchableOpacity style={styles.btnNewSearch} onPress={reiniciar} activeOpacity={0.8}>
            <Ionicons name="camera-outline" size={18} color={COLORS.secondary} />
            <Text style={styles.btnNewSearchText}>Nueva búsqueda</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );

  const handleSeleccionarViaje = (viaje: any) => {
    DeviceEventEmitter.emit('NAVIGATE_SEARCH_STACK', {
      screen: 'SeatSelection',
      params: { idViaje: String(viaje.idViaje) },
    });
  };

  const renderItem = ({ item: viaje }: { item: any }) => (
    <TripCard
      idViaje={String(viaje.idViaje)}
      ciudadOrigen={viaje.ciudadOrigen}
      ciudadDestino={viaje.ciudadDestino}
      fechaHoraSalida={viaje.fechaHoraSalida}
      fechaHoraLlegada={viaje.fechaHoraLlegada}
      precioBase={viaje.precioBase}
      tipoBus={viaje.tipoBus}
      capacidadTotalAsientos={viaje.capacidadTotalAsientos}
      onPress={() => handleSeleccionarViaje(viaje)}
    />
  );

  const renderFooter = () => {
    if (!loadingViajes) return <View style={{ height: SPACING.xxl }} />;
    return (
      <View style={{ padding: SPACING.lg, alignItems: 'center' }}>
        <ActivityIndicator size="small" color={COLORS.primary} />
        <Text style={{ color: COLORS.textSecondary, marginTop: SPACING.xs }}>Cargando más viajes...</Text>
      </View>
    );
  };

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <FlatList
        style={globalStyles.safeAreaContainer}
        ListHeaderComponent={renderHeader}
        data={viajesData}
        keyExtractor={(item) => item.idViaje.toString()}
        renderItem={renderItem}
        contentContainerStyle={styles.listContainer}
        ListFooterComponent={renderFooter}
        onEndReached={loadMore}
        onEndReachedThreshold={0.5}
        showsVerticalScrollIndicator={false}
      />
      <SelectModal
        visible={showOriginModal}
        title="Seleccionar Origen"
        options={origenesOpciones}
        onSelect={onSelectOrigen}
        onClose={() => setShowOriginModal(false)}
      />
    </View>
  );
}
