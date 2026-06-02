import React, { useState } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  ActivityIndicator, 
  TouchableOpacity, 
  Alert, 
  FlatList,
  TextInput,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleProp,
  ViewStyle,
  TextStyle
} from 'react-native';
import { useQuery, useMutation } from '@apollo/client/react';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '@/navigation/AppNavigator';
import { OBTENER_MAPA_ASIENTOS, SELECCIONAR_ASIENTO_Y_RESERVAR } from '@/graphql/queries/asientos';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles, SCREEN_METRICS } from '@/theme/theme';

type Props = NativeStackScreenProps<RootStackParamList, 'SeatSelection'>;

/**
 * Datos de pasajero asociados a un asiento seleccionado.
 */
interface PasajeroData {
  numeroAsiento: string;
  nombrePasajero: string;
  tipoPasajero: string;
}

/**
 * Pantalla de selección de asientos y reserva (CU-02).
 * Permite seleccionar múltiples asientos y rellenar datos de pasajero
 * para cada uno de ellos antes de confirmar la reserva.
 *
 * @param {Props} props - Recibe idViaje por parámetros de ruta.
 */
export default function SeatSelectionScreen({ route, navigation }: Props) {
  const { idViaje } = route.params;

  // Estado: lista de pasajeros (uno por asiento seleccionado)
  const [pasajeros, setPasajeros] = useState<PasajeroData[]>([]);

  // Consultas GraphQL
  const { loading: mapLoading, error: mapError, data: mapData, refetch } = useQuery<any>(OBTENER_MAPA_ASIENTOS, {
    variables: { idViaje: parseInt(idViaje) },
    fetchPolicy: 'network-only'
  });

  const [reservarMutation, { loading: resLoading }] = useMutation<any>(SELECCIONAR_ASIENTO_Y_RESERVAR);

  /**
   * Alterna la selección de un asiento.
   * Si ya estaba seleccionado lo quita; si no, lo añade con datos vacíos.
   */
  const toggleSeat = (numeroAsiento: string) => {
    setPasajeros(prev => {
      const exists = prev.find(p => p.numeroAsiento === numeroAsiento);
      if (exists) {
        // Deseleccionar
        return prev.filter(p => p.numeroAsiento !== numeroAsiento);
      } else {
        // Seleccionar nuevo asiento
        return [...prev, { numeroAsiento, nombrePasajero: '', tipoPasajero: 'Adulto' }];
      }
    });
  };

  /**
   * Actualiza los datos de pasajero para un asiento específico.
   */
  const updatePasajero = (numeroAsiento: string, field: keyof PasajeroData, value: string) => {
    setPasajeros(prev => prev.map(p => 
      p.numeroAsiento === numeroAsiento ? { ...p, [field]: value } : p
    ));
  };

  /**
   * Ejecuta la reserva secuencialmente para todos los asientos seleccionados.
   * La mutación del backend solo acepta un asiento a la vez.
   */
  const handleReservar = async () => {
    if (pasajeros.length === 0) {
      Alert.alert('Atención', 'Debes seleccionar al menos un asiento disponible.');
      return;
    }

    // Validar que todos tengan nombre
    const sinNombre = pasajeros.find(p => !p.nombrePasajero.trim());
    if (sinNombre) {
      Alert.alert('Datos incompletos', `Falta el nombre del pasajero para el asiento ${sinNombre.numeroAsiento}.`);
      return;
    }

    const asientosTexto = pasajeros.map(p => `• Asiento ${p.numeroAsiento}: ${p.nombrePasajero} (${p.tipoPasajero})`).join('\n');

    Alert.alert(
      'Confirmar Reserva',
      `Vas a reservar ${pasajeros.length} asiento(s):\n\n${asientosTexto}\n\n¿Continuar?`,
      [
        { text: 'Cancelar', style: 'cancel' },
        { 
          text: 'Confirmar', 
          onPress: async () => {
            try {
              const reservasGeneradas: { idReserva: string; montoEstimado: number; numeroAsiento: string }[] = [];
              let montoTotal = 0;

              // Reservar secuencialmente cada asiento
              for (const pasajero of pasajeros) {
                const { data } = await reservarMutation({
                  variables: {
                    idUsuario: 1, // MOCK: Usuario de prueba test@test.com
                    idViaje: parseInt(idViaje),
                    numeroAsiento: pasajero.numeroAsiento,
                    nombrePasajero: pasajero.nombrePasajero,
                    tipoPasajero: pasajero.tipoPasajero
                  }
                });
                const reserva = data.seleccionarAsientoYReservar;
                montoTotal += reserva.montoEstimado;
                reservasGeneradas.push({
                  idReserva: reserva.idReserva,
                  montoEstimado: reserva.montoEstimado,
                  numeroAsiento: pasajero.numeroAsiento,
                });
              }

              // Navegar a la pantalla de pago con los datos de las reservas
              navigation.navigate('Payment', {
                reservas: JSON.stringify(reservasGeneradas),
                montoTotal,
              });
            } catch (error: any) {
              Alert.alert('Error al reservar', error.message || 'Ocurrió un error inesperado.');
            }
          }
        }
      ]
    );
  };

  const renderHeader = () => (
    <View style={styles.header}>
      <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
        <Text style={styles.backButtonText}>← Volver</Text>
      </TouchableOpacity>
      <Text style={styles.headerTitle}>Selección de Asientos</Text>
      <Text style={styles.headerSubtitle}>
        {pasajeros.length > 0
          ? `${pasajeros.length} asiento(s) seleccionado(s)`
          : 'Toca los asientos que desees reservar'}
      </Text>
    </View>
  );

  const renderSeat = ({ item }: { item: any }) => {
    const isOccupied = item.ocupado;
    const isSelected = pasajeros.some(p => p.numeroAsiento === item.numeroAsiento);

    let seatStyle: StyleProp<ViewStyle> = [styles.seat];
    let textStyle: StyleProp<TextStyle> = [styles.seatText];

    if (isOccupied) {
      seatStyle.push(styles.seatOccupied);
      textStyle.push(styles.seatTextOccupied);
    } else if (isSelected) {
      seatStyle.push(styles.seatSelected);
      textStyle.push(styles.seatTextSelected);
    } else {
      seatStyle.push(styles.seatAvailable);
      textStyle.push(styles.seatTextAvailable);
    }

    return (
      <TouchableOpacity 
        style={seatStyle}
        disabled={isOccupied || resLoading}
        onPress={() => toggleSeat(item.numeroAsiento)}
      >
        <Text style={textStyle}>{item.numeroAsiento}</Text>
      </TouchableOpacity>
    );
  };

  /**
   * Renderiza el formulario de datos de pasajero para un asiento individual.
   */
  const renderPasajeroForm = (pasajero: PasajeroData, index: number) => (
    <View key={pasajero.numeroAsiento} style={styles.pasajeroCard}>
      <View style={styles.pasajeroCardHeader}>
        <Text style={styles.pasajeroCardTitle}>Asiento {pasajero.numeroAsiento}</Text>
        <TouchableOpacity onPress={() => toggleSeat(pasajero.numeroAsiento)}>
          <Text style={styles.removeText}>✕ Quitar</Text>
        </TouchableOpacity>
      </View>

      <View style={globalStyles.inputContainer}>
        <Text style={globalStyles.inputLabel}>Nombre Completo</Text>
        <TextInput 
          style={globalStyles.inputField}
          placeholder="Ej. Juan Pérez"
          placeholderTextColor={COLORS.placeholder}
          value={pasajero.nombrePasajero}
          onChangeText={(text) => updatePasajero(pasajero.numeroAsiento, 'nombrePasajero', text)}
          editable={!resLoading}
        />
      </View>

      <View style={globalStyles.inputContainer}>
        <Text style={globalStyles.inputLabel}>Tipo de Pasajero</Text>
        <View style={styles.typeSelectorContainer}>
          <TouchableOpacity 
            style={[styles.typeOption, pasajero.tipoPasajero === 'Adulto' && styles.typeOptionSelected]}
            onPress={() => updatePasajero(pasajero.numeroAsiento, 'tipoPasajero', 'Adulto')}
          >
            <Text style={[styles.typeOptionText, pasajero.tipoPasajero === 'Adulto' && styles.typeOptionTextSelected]}>Adulto</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.typeOption, pasajero.tipoPasajero === 'Niño' && styles.typeOptionSelected]}
            onPress={() => updatePasajero(pasajero.numeroAsiento, 'tipoPasajero', 'Niño')}
          >
            <Text style={[styles.typeOptionText, pasajero.tipoPasajero === 'Niño' && styles.typeOptionTextSelected]}>Niño</Text>
          </TouchableOpacity>
        </View>
      </View>
    </View>
  );

  if (mapLoading) {
    return (
      <View style={globalStyles.safeAreaContainer}>
        {renderHeader()}
        <View style={styles.centerContainer}>
          <ActivityIndicator size="large" color={COLORS.primary} />
          <Text style={styles.loadingText}>Cargando mapa de asientos...</Text>
        </View>
      </View>
    );
  }

  if (mapError) {
    return (
      <View style={globalStyles.safeAreaContainer}>
        {renderHeader()}
        <View style={styles.centerContainer}>
          <Text style={styles.errorText}>No pudimos cargar los asientos.</Text>
          <TouchableOpacity style={globalStyles.btnPrimary} onPress={() => refetch()}>
            <Text style={TYPOGRAPHY.buttonText}>Reintentar</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  const asientos = mapData?.obtenerMapaAsientos || [];

  return (
    <KeyboardAvoidingView 
      style={globalStyles.safeAreaContainer}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      {renderHeader()}
      
      <ScrollView contentContainerStyle={styles.scrollContent}>
        
        {/* Leyenda */}
        <View style={styles.legendContainer}>
          <View style={styles.legendItem}>
            <View style={[styles.legendColor, styles.seatAvailable]} />
            <Text style={styles.legendText}>Libre</Text>
          </View>
          <View style={styles.legendItem}>
            <View style={[styles.legendColor, styles.seatOccupied]} />
            <Text style={styles.legendText}>Ocupado</Text>
          </View>
          <View style={styles.legendItem}>
            <View style={[styles.legendColor, styles.seatSelected]} />
            <Text style={styles.legendText}>Seleccionado</Text>
          </View>
        </View>

        {/* Mapa de asientos (Grid) */}
        <View style={styles.busContainer}>
          <Text style={styles.busFront}>FRENTE DEL BUS</Text>
          <FlatList 
            data={asientos}
            keyExtractor={(item) => item.numeroAsiento}
            numColumns={4}
            scrollEnabled={false}
            columnWrapperStyle={styles.rowWrapper}
            contentContainerStyle={styles.gridContainer}
            renderItem={renderSeat}
          />
        </View>

        {/* Formularios de Pasajeros (uno por asiento) */}
        {pasajeros.length > 0 && (
          <View>
            <Text style={styles.sectionTitle}>Datos de Pasajeros ({pasajeros.length})</Text>
            {pasajeros.map((p, i) => renderPasajeroForm(p, i))}

            <TouchableOpacity 
              style={[globalStyles.btnPrimary, resLoading && { opacity: 0.7 }]}
              onPress={handleReservar}
              disabled={resLoading}
            >
              {resLoading ? (
                <ActivityIndicator color={COLORS.textLight} />
              ) : (
                <Text style={TYPOGRAPHY.buttonText}>
                  Confirmar y Reservar ({pasajeros.length} asiento{pasajeros.length > 1 ? 's' : ''})
                </Text>
              )}
            </TouchableOpacity>
          </View>
        )}
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  header: {
    paddingTop: SPACING.xl,
    paddingHorizontal: SPACING.md,
    paddingBottom: SPACING.md,
    backgroundColor: COLORS.surface,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
  },
  backButton: {
    marginBottom: SPACING.sm,
  },
  backButtonText: {
    ...TYPOGRAPHY.body,
    color: COLORS.secondary,
    fontWeight: '600',
  },
  headerTitle: {
    ...TYPOGRAPHY.h2,
    color: COLORS.primary,
  },
  headerSubtitle: {
    ...TYPOGRAPHY.caption,
    color: COLORS.textSecondary,
    marginTop: 2,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: SPACING.xl,
  },
  loadingText: {
    ...TYPOGRAPHY.body,
    marginTop: SPACING.md,
    color: COLORS.textSecondary,
  },
  errorText: {
    ...TYPOGRAPHY.body,
    color: COLORS.danger,
    marginBottom: SPACING.md,
  },
  scrollContent: {
    padding: SPACING.md,
    paddingBottom: SPACING.xxl,
  },
  legendContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: SPACING.lg,
    backgroundColor: COLORS.surface,
    padding: SPACING.sm,
    borderRadius: 8,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  legendColor: {
    width: 16,
    height: 16,
    borderRadius: 4,
    marginRight: 8,
  },
  legendText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.textSecondary,
  },
  busContainer: {
    backgroundColor: '#E0E5EC',
    borderRadius: 24,
    padding: SPACING.md,
    alignItems: 'center',
    marginBottom: SPACING.xl,
  },
  busFront: {
    ...TYPOGRAPHY.caption,
    color: COLORS.textSecondary,
    fontWeight: 'bold',
    marginBottom: SPACING.lg,
    borderBottomWidth: 2,
    borderBottomColor: COLORS.border,
    paddingBottom: SPACING.sm,
    width: '100%',
    textAlign: 'center',
  },
  gridContainer: {
    alignItems: 'center',
  },
  rowWrapper: {
    justifyContent: 'center',
    marginBottom: SPACING.md,
    gap: SPACING.sm,
  },
  seat: {
    width: 48,
    height: 48,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    borderWidth: 1,
  },
  seatAvailable: {
    backgroundColor: COLORS.surface,
    borderColor: COLORS.primary,
  },
  seatOccupied: {
    backgroundColor: COLORS.border,
    borderColor: COLORS.textSecondary,
  },
  seatSelected: {
    backgroundColor: COLORS.primary,
    borderColor: COLORS.primary,
  },
  seatText: {
    ...TYPOGRAPHY.body,
    fontWeight: 'bold',
  },
  seatTextAvailable: {
    color: COLORS.primary,
  },
  seatTextOccupied: {
    color: COLORS.textSecondary,
  },
  seatTextSelected: {
    color: COLORS.textLight,
  },
  sectionTitle: {
    ...TYPOGRAPHY.h3,
    color: COLORS.textPrimary,
    marginBottom: SPACING.md,
  },
  pasajeroCard: {
    backgroundColor: COLORS.surface,
    padding: SPACING.lg,
    borderRadius: 16,
    marginBottom: SPACING.md,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  pasajeroCardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: SPACING.md,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
    paddingBottom: SPACING.sm,
  },
  pasajeroCardTitle: {
    ...TYPOGRAPHY.h3,
    color: COLORS.primary,
  },
  removeText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.danger,
    fontWeight: '600',
  },
  typeSelectorContainer: {
    flexDirection: 'row',
    gap: SPACING.md,
  },
  typeOption: {
    flex: 1,
    paddingVertical: SPACING.sm,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 8,
  },
  typeOptionSelected: {
    backgroundColor: COLORS.secondary,
    borderColor: COLORS.secondary,
  },
  typeOptionText: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
  },
  typeOptionTextSelected: {
    color: COLORS.textLight,
    fontWeight: 'bold',
  }
});
