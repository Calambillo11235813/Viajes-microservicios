import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Alert,
  ActivityIndicator,
  ScrollView,
  Image,
  DeviceEventEmitter,
} from 'react-native';
import { useMutation } from '@apollo/client/react';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { SearchStackParamList } from '@/navigation/SearchStackNavigator';
import { REALIZAR_PAGO } from '@/graphql/queries/pagos';
import { COLORS, TYPOGRAPHY, globalStyles } from '@/theme/theme';
import { styles } from './styles/PaymentScreen.styles';

type Props = NativeStackScreenProps<SearchStackParamList, 'Payment'>;

/**
 * Información de una reserva pendiente de pago.
 */
interface ReservaPago {
  idReserva: string;
  montoEstimado: number;
  numeroAsiento: string;
}

/**
 * Pantalla de procesamiento de pago (CU-03).
 * Simula el flujo de pago mediante QR o transferencia bancaria.
 * Recibe la lista de reservas generadas en el paso anterior
 * y permite al usuario "acreditar" el pago de forma simulada.
 *
 * @param {Props} props - Recibe reservas y montoTotal por parámetros de ruta.
 */
export default function PaymentScreen({ route, navigation }: Props) {
  const { reservas, montoTotal } = route.params;
  const reservasParsed: ReservaPago[] = JSON.parse(reservas);

  // Estado del método de pago seleccionado
  const [metodoPago, setMetodoPago] = useState<'QR' | 'TRANSFERENCIA' | null>(null);
  // Paso del flujo: 'seleccion' -> 'procesando' -> 'confirmado'
  const [paso, setPaso] = useState<'seleccion' | 'procesando' | 'confirmado'>('seleccion');

  const [realizarPago, { loading }] = useMutation<any>(REALIZAR_PAGO);

  /**
   * Simula la verificación del pago y ejecuta la mutación para cada reserva.
   */
  const handleConfirmarPago = async () => {
    if (!metodoPago) {
      Alert.alert('Atención', 'Selecciona un método de pago primero.');
      return;
    }

    setPaso('procesando');

    try {
      // Procesar pago para cada reserva secuencialmente
      for (const reserva of reservasParsed) {
        // Redondear a 2 decimales para evitar errores de precisión Float vs BigDecimal
        const montoRedondeado = Math.round(reserva.montoEstimado * 100) / 100;
        await realizarPago({
          variables: {
            idReserva: parseInt(reserva.idReserva),
            metodoPagoUsado: metodoPago,
            montoTransaccion: montoRedondeado,
            acreditado: true, // Simulado: siempre acreditado
            cuponDescuentoAplicado: null,
          },
        });
      }

      setPaso('confirmado');
    } catch (error: any) {
      setPaso('seleccion');
      Alert.alert('Error en el pago', error.message || 'No se pudo procesar el pago.');
    }
  };

  const renderHeader = () => (
    <View style={styles.header}>
      {paso !== 'confirmado' && (
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
          <Text style={styles.backButtonText}>← Volver</Text>
        </TouchableOpacity>
      )}
      <Text style={styles.headerTitle}>
        {paso === 'confirmado' ? '¡Pago Exitoso!' : 'Realizar Pago'}
      </Text>
      <Text style={styles.headerSubtitle}>
        {paso === 'seleccion' && 'Elige tu método de pago'}
        {paso === 'procesando' && 'Verificando acreditación...'}
        {paso === 'confirmado' && 'Tu reserva ha sido confirmada'}
      </Text>
    </View>
  );

  // ─── PASO 3: Confirmación ───
  if (paso === 'confirmado') {
    return (
      <View style={globalStyles.safeAreaContainer}>
        {renderHeader()}
        <ScrollView contentContainerStyle={styles.confirmContainer}>
          <View style={styles.successIcon}>
            <Text style={styles.successIconText}>✓</Text>
          </View>

          <Text style={styles.successTitle}>Pago Acreditado</Text>
          <Text style={styles.successSubtitle}>
            Se ha procesado el pago de {reservasParsed.length} asiento(s) por un total de Bs. {montoTotal.toFixed(2)}
          </Text>

          <View style={styles.receiptCard}>
            <Text style={styles.receiptTitle}>Comprobante</Text>
            <View style={styles.receiptRow}>
              <Text style={styles.receiptLabel}>Método:</Text>
              <Text style={styles.receiptValue}>{metodoPago === 'QR' ? 'Código QR' : 'Transferencia Bancaria'}</Text>
            </View>
            <View style={styles.receiptRow}>
              <Text style={styles.receiptLabel}>Monto Total:</Text>
              <Text style={[styles.receiptValue, { color: COLORS.success }]}>Bs. {montoTotal.toFixed(2)}</Text>
            </View>
            <View style={styles.receiptRow}>
              <Text style={styles.receiptLabel}>Asientos:</Text>
              <Text style={styles.receiptValue}>{reservasParsed.map(r => r.numeroAsiento).join(', ')}</Text>
            </View>
            <View style={styles.receiptRow}>
              <Text style={styles.receiptLabel}>Estado:</Text>
              <Text style={[styles.receiptValue, { color: COLORS.success }]}>CONFIRMADO</Text>
            </View>
          </View>

          <TouchableOpacity
            style={globalStyles.btnPrimary}
            onPress={() => {
              // Volver al inicio del stack de búsqueda internamente
              navigation.popToTop();
              // Emitir evento para cambiar la pestaña en el DrawerNavigator padre
              DeviceEventEmitter.emit('NAVIGATE_DRAWER', 'MisViajes');
            }}
          >
            <Text style={TYPOGRAPHY.buttonText}>Ver Mis Viajes</Text>
          </TouchableOpacity>
        </ScrollView>
      </View>
    );
  }

  // ─── PASO 2: Procesando (spinner) ───
  if (paso === 'procesando') {
    return (
      <View style={globalStyles.safeAreaContainer}>
        {renderHeader()}
        <View style={styles.centerContainer}>
          <ActivityIndicator size="large" color={COLORS.primary} />
          <Text style={styles.processingText}>Procesando pago...</Text>
          <Text style={styles.processingSubtext}>
            Verificando acreditación con la pasarela de pago
          </Text>
        </View>
      </View>
    );
  }

  // ─── PASO 1: Selección de método de pago ───
  return (
    <View style={globalStyles.safeAreaContainer}>
      {renderHeader()}
      <ScrollView contentContainerStyle={styles.scrollContent}>

        {/* Resumen de la reserva */}
        <View style={styles.summaryCard}>
          <Text style={styles.summaryTitle}>Resumen de Reserva</Text>
          {reservasParsed.map((r) => (
            <View key={r.idReserva} style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Asiento {r.numeroAsiento}</Text>
              <Text style={styles.summaryValue}>Bs. {r.montoEstimado.toFixed(2)}</Text>
            </View>
          ))}
          <View style={[styles.summaryRow, styles.summaryTotalRow]}>
            <Text style={styles.summaryTotalLabel}>Total a Pagar</Text>
            <Text style={styles.summaryTotalValue}>Bs. {montoTotal.toFixed(2)}</Text>
          </View>
        </View>

        {/* Métodos de pago */}
        <Text style={styles.sectionTitle}>Método de Pago</Text>

        {/* Opción QR */}
        <TouchableOpacity
          style={[styles.paymentOption, metodoPago === 'QR' && styles.paymentOptionSelected]}
          onPress={() => setMetodoPago('QR')}
        >
          <View style={styles.paymentIconContainer}>
            <Text style={styles.paymentIcon}>📱</Text>
          </View>
          <View style={styles.paymentOptionContent}>
            <Text style={[styles.paymentOptionTitle, metodoPago === 'QR' && styles.paymentOptionTitleSelected]}>
              Pago con QR
            </Text>
            <Text style={styles.paymentOptionDesc}>Escanea el código QR con tu app bancaria</Text>
          </View>
          <View style={[styles.radioOuter, metodoPago === 'QR' && styles.radioOuterSelected]}>
            {metodoPago === 'QR' && <View style={styles.radioInner} />}
          </View>
        </TouchableOpacity>

        {/* Opción Transferencia */}
        <TouchableOpacity
          style={[styles.paymentOption, metodoPago === 'TRANSFERENCIA' && styles.paymentOptionSelected]}
          onPress={() => setMetodoPago('TRANSFERENCIA')}
        >
          <View style={styles.paymentIconContainer}>
            <Text style={styles.paymentIcon}>🏦</Text>
          </View>
          <View style={styles.paymentOptionContent}>
            <Text style={[styles.paymentOptionTitle, metodoPago === 'TRANSFERENCIA' && styles.paymentOptionTitleSelected]}>
              Transferencia Bancaria
            </Text>
            <Text style={styles.paymentOptionDesc}>Realiza una transferencia a nuestra cuenta</Text>
          </View>
          <View style={[styles.radioOuter, metodoPago === 'TRANSFERENCIA' && styles.radioOuterSelected]}>
            {metodoPago === 'TRANSFERENCIA' && <View style={styles.radioInner} />}
          </View>
        </TouchableOpacity>

        {/* Datos de pago según el método seleccionado */}
        {metodoPago === 'QR' && (
          <View style={styles.paymentDetailsCard}>
            <Text style={styles.paymentDetailsTitle}>Código QR de Pago</Text>
            <View style={styles.qrPlaceholder}>
              <View style={styles.qrBox}>
                <Image
                  source={{ uri: `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=PagoViaje-Monto-${montoTotal.toFixed(2)}` }}
                  style={styles.qrImage}
                />
              </View>
            </View>
            <Text style={styles.paymentNote}>
              Escanea este código con tu aplicación bancaria para completar el pago.
            </Text>
          </View>
        )}

        {metodoPago === 'TRANSFERENCIA' && (
          <View style={styles.paymentDetailsCard}>
            <Text style={styles.paymentDetailsTitle}>Datos de Cuenta</Text>
            <View style={styles.bankDetail}>
              <Text style={styles.bankLabel}>Banco:</Text>
              <Text style={styles.bankValue}>Banco Nacional de Bolivia</Text>
            </View>
            <View style={styles.bankDetail}>
              <Text style={styles.bankLabel}>Cuenta:</Text>
              <Text style={styles.bankValue}>1234-5678-9012</Text>
            </View>
            <View style={styles.bankDetail}>
              <Text style={styles.bankLabel}>Titular:</Text>
              <Text style={styles.bankValue}>Agencia de Viajes S.A.</Text>
            </View>
            <View style={styles.bankDetail}>
              <Text style={styles.bankLabel}>Monto:</Text>
              <Text style={[styles.bankValue, { color: COLORS.accent, fontWeight: 'bold' }]}>
                Bs. {montoTotal.toFixed(2)}
              </Text>
            </View>
            <Text style={styles.paymentNote}>
              Realiza la transferencia y presiona "Confirmar Pago" para verificar la acreditación.
            </Text>
          </View>
        )}

        {/* Botón de confirmar */}
        {metodoPago && (
          <TouchableOpacity
            style={[globalStyles.btnPrimary, loading && { opacity: 0.7 }]}
            onPress={handleConfirmarPago}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color={COLORS.textLight} />
            ) : (
              <Text style={TYPOGRAPHY.buttonText}>Confirmar Pago</Text>
            )}
          </TouchableOpacity>
        )}
      </ScrollView>
    </View>
  );
}
