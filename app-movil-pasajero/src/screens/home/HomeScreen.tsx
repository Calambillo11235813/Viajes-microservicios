import React, { useState, useMemo } from 'react';
import { View, Text, TouchableOpacity, ActivityIndicator, Alert, Platform } from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';
import { useQuery } from '@apollo/client/react';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { SearchStackParamList } from '@/navigation/SearchStackNavigator';
import { GET_RUTAS } from '@/graphql/queries/viajes';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';
import SelectModal from '@/components/SelectModal';
import { styles } from './styles/HomeScreen.styles';

type Props = NativeStackScreenProps<SearchStackParamList, 'Home'>;

/**
 * Pantalla principal (Inicio) de la aplicación de pasajeros.
 * Permite buscar rutas y horarios (CU-01).
 *
 * @param {Props} props - Propiedades de navegación.
 */
export default function HomeScreen({ navigation }: Props) {
  const { data, loading, error } = useQuery<any>(GET_RUTAS);

  const [origen, setOrigen] = useState('');
  const [destino, setDestino] = useState('');
  const [dateObj, setDateObj] = useState(new Date());
  const [fecha, setFecha] = useState(dateObj.toISOString().split('T')[0]); // YYYY-MM-DD

  const [showDatePicker, setShowDatePicker] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalType, setModalType] = useState<'origen' | 'destino'>('origen');

  const onChangeDate = (event: any, selectedDate?: Date) => {
    setShowDatePicker(Platform.OS === 'ios');
    if (selectedDate) {
      setDateObj(selectedDate);
      setFecha(selectedDate.toISOString().split('T')[0]);
    }
  };

  // Extraer origenes únicos y destinos únicos basados en las rutas disponibles
  const origenesUnicos = useMemo(() => {
    if (!data?.listarRutas) return [];
    const origenes = data.listarRutas.map((r: any) => r.ciudadOrigen);
    return [...new Set(origenes)] as string[];
  }, [data]);

  const destinosUnicos = useMemo(() => {
    if (!data?.listarRutas) return [];
    let rutas = data.listarRutas;
    if (origen) {
      rutas = rutas.filter((r: any) => r.ciudadOrigen === origen);
    }
    const destinos = rutas.map((r: any) => r.ciudadDestino);
    return [...new Set(destinos)] as string[];
  }, [data, origen]);

  const handleOpenModal = (type: 'origen' | 'destino') => {
    setModalType(type);
    setModalVisible(true);
  };

  const handleSelect = (option: string) => {
    if (modalType === 'origen') {
      setOrigen(option);
      if (option === destino) setDestino(''); // Reset destino si es igual al nuevo origen
    } else {
      setDestino(option);
    }
  };

  /**
   * Maneja el envío del formulario.
   * Valida que todos los campos estén completos y la fecha tenga formato válido.
   */
  const handleSearch = () => {
    if (!origen || !destino || !fecha) {
      Alert.alert('Datos incompletos', 'Por favor selecciona origen, destino y fecha para buscar.');
      return;
    }

    // Validar formato simple de fecha YYYY-MM-DD
    const regex = /^\d{4}-\d{2}-\d{2}$/;
    if (!regex.test(fecha)) {
      Alert.alert('Formato inválido', 'La fecha debe tener el formato YYYY-MM-DD.');
      return;
    }

    navigation.navigate('SearchResults', { origen, destino, fecha });
  };

  return (
    <View style={globalStyles.safeAreaContainer}>
      <View style={styles.header}>
        <Text style={styles.greeting}>Bienvenido</Text>
        <Text style={styles.subtitle}>¿A dónde quieres viajar hoy?</Text>
      </View>

      <View style={[globalStyles.mainContainer, { justifyContent: 'flex-start', paddingBottom: 0 }]}>
        {loading ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color={COLORS.primary} />
            <Text style={styles.loadingText}>Cargando rutas...</Text>
          </View>
        ) : error ? (
          <View style={styles.errorContainer}>
            <Text style={styles.errorText}>No se pudieron cargar las rutas. Revisa tu conexión.</Text>
          </View>
        ) : (
          <View style={globalStyles.card}>
            <Text style={styles.cardTitle}>Buscar Pasajes</Text>

            {/* Origen */}
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

            {/* Destino */}
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

            {/* Fecha */}
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
        )}
      </View>

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