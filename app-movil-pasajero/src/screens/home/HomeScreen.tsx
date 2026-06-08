import React, { useState, useMemo, useCallback, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
  Platform,
  FlatList,
} from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';
import { useQuery } from '@apollo/client/react';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { SearchStackParamList } from '@/navigation/SearchStackNavigator';
import {
  GET_RUTAS,
  OBTENER_RECOMENDACION_RUTA,
  GetRutasData,
  ObtenerRecomendacionRutaData,
  ObtenerRecomendacionRutaVars,
  RutaResumen,
} from '@/graphql/queries/viajes';
import { useAuth } from '@/context/AuthContext';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';
import SelectModal from '@/components/SelectModal';
import RecommendationCard from '@/components/RecommendationCard';
import { styles } from './styles/HomeScreen.styles';

type Props = NativeStackScreenProps<SearchStackParamList, 'Home'>;
type HomeSection = 'recommendation' | 'searchForm';

/**
 * Pantalla principal (Inicio) de la aplicación de pasajeros.
 * Permite buscar rutas y horarios (CU-01) y muestra recomendación personalizada (CU-09).
 */
export default function HomeScreen({ navigation }: Props) {
  const { user } = useAuth();

  /** GraphQL devuelve idUsuario como string (tipo ID); la query CU-09 exige Int. */
  const idUsuarioNum = useMemo(() => {
    if (user?.idUsuario == null) return undefined;
    const parsed = Number(user.idUsuario);
    return Number.isFinite(parsed) ? parsed : undefined;
  }, [user?.idUsuario]);

  const { data, loading, error } = useQuery<GetRutasData>(GET_RUTAS, {
    variables: { pagina: 0, tamanio: 500 },
    fetchPolicy: 'cache-first',
  });

  const {
    data: recomendacionData,
    loading: recomendacionLoading,
    error: recomendacionError,
  } = useQuery<ObtenerRecomendacionRutaData, ObtenerRecomendacionRutaVars>(
    OBTENER_RECOMENDACION_RUTA,
    {
      variables: { idUsuario: idUsuarioNum ?? 0 },
      skip: idUsuarioNum == null,
      fetchPolicy: 'network-only',
      nextFetchPolicy: 'cache-first',
    }
  );

  const [origen, setOrigen] = useState('');
  const [destino, setDestino] = useState('');
  const [dateObj, setDateObj] = useState(new Date());
  const [fecha, setFecha] = useState(dateObj.toISOString().split('T')[0]);

  const [showDatePicker, setShowDatePicker] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalType, setModalType] = useState<'origen' | 'destino'>('origen');

  const rutas = useMemo<RutaResumen[]>(() => data?.listarRutas?.contenido ?? [], [data]);

  const sections = useMemo<HomeSection[]>(
    () => ['recommendation', 'searchForm'],
    []
  );

  const rutaRecomendada = useMemo(() => {
    const recomendacion = recomendacionData?.obtenerRecomendacionRuta;
    if (!recomendacion?.rutaRecomendadaId || recomendacion.advertencia) {
      return null;
    }

    return (
      rutas.find(
        (ruta) => Number(ruta.id) === recomendacion.rutaRecomendadaId
      ) ?? null
    );
  }, [recomendacionData, rutas]);

  const probabilidadPrincipal = useMemo(() => {
    const recomendacion = recomendacionData?.obtenerRecomendacionRuta;
    if (!recomendacion?.rutaRecomendadaId) return null;

    return (
      recomendacion.topRutas.find(
        (topRuta) => topRuta.idRuta === recomendacion.rutaRecomendadaId
      )?.probabilidad ?? null
    );
  }, [recomendacionData]);

  useEffect(() => {
    console.log('[CU-09] Usuario en contexto:', user);
    console.log('[CU-09] idUsuarioNum:', idUsuarioNum, '| skip query:', idUsuarioNum == null);
  }, [user, idUsuarioNum]);

  useEffect(() => {
    if (recomendacionLoading) {
      console.log('[CU-09] Enviando/consultando obtenerRecomendacionRuta...');
    }
  }, [recomendacionLoading]);

  useEffect(() => {
    if (recomendacionError) {
      console.log('[CU-09] Error GraphQL:', recomendacionError.message);
    }
  }, [recomendacionError]);

  useEffect(() => {
    if (!recomendacionData?.obtenerRecomendacionRuta) return;

    const rec = recomendacionData.obtenerRecomendacionRuta;
    console.log('[CU-09] Respuesta recomendacion:', JSON.stringify(rec));
    console.log('[CU-09] Rutas en catalogo:', rutas.length);

    if (rec.advertencia) {
      console.log('[CU-09] Tarjeta oculta: advertencia ->', rec.advertencia);
    } else if (!rec.rutaRecomendadaId) {
      console.log('[CU-09] Tarjeta oculta: sin rutaRecomendadaId');
    } else if (!rutaRecomendada) {
      console.log(
        '[CU-09] Tarjeta oculta: ruta id',
        rec.rutaRecomendadaId,
        'no existe en listarRutas'
      );
    } else {
      console.log(
        '[CU-09] Tarjeta visible:',
        rutaRecomendada.ciudadOrigen,
        '->',
        rutaRecomendada.ciudadDestino
      );
    }
  }, [recomendacionData, rutas.length, rutaRecomendada]);

  const origenesUnicos = useMemo(() => {
    const origenes = rutas.map((r) => r.ciudadOrigen);
    return [...new Set(origenes)] as string[];
  }, [rutas]);

  const destinosUnicos = useMemo(() => {
    let rutasFiltradas = rutas;
    if (origen) {
      rutasFiltradas = rutas.filter((r) => r.ciudadOrigen === origen);
    }
    const destinos = rutasFiltradas.map((r) => r.ciudadDestino);
    return [...new Set(destinos)] as string[];
  }, [rutas, origen]);

  const onChangeDate = (_event: unknown, selectedDate?: Date) => {
    setShowDatePicker(Platform.OS === 'ios');
    if (selectedDate) {
      setDateObj(selectedDate);
      setFecha(selectedDate.toISOString().split('T')[0]);
    }
  };

  const handleOpenModal = useCallback((type: 'origen' | 'destino') => {
    setModalType(type);
    setModalVisible(true);
  }, []);

  const handleSelect = useCallback(
    (option: string) => {
      if (modalType === 'origen') {
        setOrigen(option);
        if (option === destino) setDestino('');
      } else {
        setDestino(option);
      }
    },
    [destino, modalType]
  );

  const handleSearch = useCallback(() => {
    if (!origen || !destino || !fecha) {
      Alert.alert(
        'Datos incompletos',
        'Por favor selecciona origen, destino y fecha para buscar.'
      );
      return;
    }

    const regex = /^\d{4}-\d{2}-\d{2}$/;
    if (!regex.test(fecha)) {
      Alert.alert('Formato inválido', 'La fecha debe tener el formato YYYY-MM-DD.');
      return;
    }

    navigation.navigate('SearchResults', { origen, destino, fecha });
  }, [destino, fecha, navigation, origen]);

  const handlePressRecommendation = useCallback(() => {
    if (!rutaRecomendada) return;

    navigation.navigate('SearchResults', {
      origen: rutaRecomendada.ciudadOrigen,
      destino: rutaRecomendada.ciudadDestino,
      fecha,
    });
  }, [fecha, navigation, rutaRecomendada]);

  const renderSearchCard = () => (
    <View style={globalStyles.card}>
      <Text style={styles.cardTitle}>Buscar Pasajes</Text>

      <View style={globalStyles.inputContainer}>
        <Text style={globalStyles.inputLabel}>Origen</Text>
        <TouchableOpacity
          style={globalStyles.inputField}
          onPress={() => handleOpenModal('origen')}
        >
          <Text style={{ color: origen ? COLORS.textPrimary : COLORS.placeholder }}>
            {origen || 'Selecciona origen'}
          </Text>
        </TouchableOpacity>
      </View>

      <View style={globalStyles.inputContainer}>
        <Text style={globalStyles.inputLabel}>Destino</Text>
        <TouchableOpacity
          style={[globalStyles.inputField, !origen && styles.inputDisabled]}
          onPress={() => origen && handleOpenModal('destino')}
          activeOpacity={origen ? 0.7 : 1}
        >
          <Text style={{ color: destino ? COLORS.textPrimary : COLORS.placeholder }}>
            {destino || (origen ? 'Selecciona destino' : 'Primero selecciona origen')}
          </Text>
        </TouchableOpacity>
      </View>

      <View style={globalStyles.inputContainer}>
        <Text style={globalStyles.inputLabel}>Fecha</Text>
        <TouchableOpacity
          style={globalStyles.inputField}
          onPress={() => setShowDatePicker(true)}
        >
          <Text style={{ color: fecha ? COLORS.textPrimary : COLORS.placeholder }}>
            {fecha || 'Seleccionar fecha'}
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

      <TouchableOpacity
        style={[globalStyles.btnPrimary, { marginTop: SPACING.lg }]}
        onPress={handleSearch}
      >
        <Text style={TYPOGRAPHY.buttonText}>Buscar Viajes</Text>
      </TouchableOpacity>
    </View>
  );

  const renderRecommendationSection = () => {
    if (idUsuarioNum == null) {
      console.log('[CU-09] Tarjeta oculta: usuario no autenticado');
      return null;
    }

    if (recomendacionLoading) {
      return (
        <RecommendationCard
          loading
          ciudadOrigen=""
          ciudadDestino=""
          onPress={() => undefined}
        />
      );
    }

    if (recomendacionError || !rutaRecomendada) {
      if (recomendacionError) {
        console.log('[CU-09] render: error, no se muestra tarjeta');
      }
      return null;
    }

    return (
      <RecommendationCard
        ciudadOrigen={rutaRecomendada.ciudadOrigen}
        ciudadDestino={rutaRecomendada.ciudadDestino}
        categoriaPreferida={
          recomendacionData?.obtenerRecomendacionRuta.categoriaPreferida
        }
        probabilidad={probabilidadPrincipal}
        onPress={handlePressRecommendation}
      />
    );
  };

  const renderSection = ({ item }: { item: HomeSection }) => {
    if (item === 'recommendation') {
      return renderRecommendationSection();
    }

    if (loading) {
      return (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={COLORS.primary} />
          <Text style={styles.loadingText}>Cargando rutas...</Text>
        </View>
      );
    }

    if (error) {
      return (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>
            No se pudieron cargar las rutas. Revisa tu conexión.
          </Text>
        </View>
      );
    }

    return renderSearchCard();
  };

  return (
    <View style={globalStyles.safeAreaContainer}>
      <View style={styles.header}>
        <Text style={styles.greeting}>Bienvenido</Text>
        <Text style={styles.subtitle}>¿A dónde quieres viajar hoy?</Text>
      </View>

      <FlatList
        data={sections}
        keyExtractor={(item) => item}
        renderItem={renderSection}
        initialNumToRender={2}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
      />

      <SelectModal
        visible={modalVisible}
        title={modalType === 'origen' ? 'Seleccionar Origen' : 'Seleccionar Destino'}
        options={modalType === 'origen' ? origenesUnicos : destinosUnicos}
        onSelect={handleSelect}
        onClose={() => setModalVisible(false)}
      />
    </View>
  );
}
