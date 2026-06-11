import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, ActivityIndicator, TouchableOpacity, RefreshControl } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useMyTrips, ViajeConsolidado } from '@/hooks/useMyTrips';
import { useTripFeedback } from '@/hooks/useTripFeedback';
import TripFeedbackModal, { TripFeedbackSubmit } from '@/components/TripFeedbackModal';
import { COLORS, TYPOGRAPHY, globalStyles } from '@/theme/theme';
import { styles } from './styles/MyTripsScreen.styles';

type FiltroViajes = 'CONFIRMADA' | 'COMPLETADA';

function formatearFecha(iso: string): string {
  const fecha = new Date(iso);
  if (Number.isNaN(fecha.getTime())) return iso;
  return fecha.toLocaleDateString('es-BO', {
    weekday: 'short',
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

function formatearHora(iso: string): string {
  const fecha = new Date(iso);
  if (Number.isNaN(fecha.getTime())) return '--:--';
  return fecha.toLocaleTimeString('es-BO', { hour: '2-digit', minute: '2-digit' });
}

function esViajeCalificable(viaje: ViajeConsolidado): boolean {
  if (viaje.estadoReserva === 'CANCELADA') return false;
  if (viaje.estadoReserva === 'COMPLETADA') return true;

  const llegada = new Date(viaje.fechaHoraLlegada);
  const yaTermino = !Number.isNaN(llegada.getTime()) && llegada.getTime() < Date.now();
  return viaje.estadoReserva === 'CONFIRMADA' && yaTermino;
}

export default function MyTripsScreen() {
  const { viajes, loading, error, refetch, cancelarReserva, cancelLoading } = useMyTrips();
  const {
    feedbackLoading,
    cargarFeedbackEnviado,
    feedbackYaEnviado,
    registrarFeedback,
  } = useTripFeedback();
  const [viajeFeedback, setViajeFeedback] = useState<ViajeConsolidado | null>(null);
  const [filtroViajes, setFiltroViajes] = useState<FiltroViajes>('COMPLETADA');

  const viajesFiltrados = viajes.filter((viaje) => viaje.estadoReserva === filtroViajes);
  const totalConfirmados = viajes.filter((viaje) => viaje.estadoReserva === 'CONFIRMADA').length;
  const totalCompletados = viajes.filter((viaje) => viaje.estadoReserva === 'COMPLETADA').length;

  useEffect(() => {
    cargarFeedbackEnviado(viajes.map((viaje) => viaje.idViaje));
  }, [cargarFeedbackEnviado, viajes]);

  const getStatusColor = (estado: string) => {
    switch (estado) {
      case 'CONFIRMADA': return COLORS.success;
      case 'PENDIENTE': return COLORS.warning;
      case 'CANCELADA': return COLORS.danger;
      case 'COMPLETADA': return COLORS.accent;
      default: return COLORS.secondary;
    }
  };

  const handleSubmitFeedback = async (feedback: TripFeedbackSubmit): Promise<boolean> => {
    if (!viajeFeedback) return false;

    return registrarFeedback({
      idViaje: viajeFeedback.idViaje,
      idReserva: viajeFeedback.idsReservas[0],
      calificacion: feedback.calificacion,
      comentario: feedback.comentario,
    });
  };

  const renderItem = ({ item }: { item: ViajeConsolidado }) => {
    const isCancellable = item.estadoReserva === 'PENDIENTE' || item.estadoReserva === 'CONFIRMADA';
    const canSendFeedback = esViajeCalificable(item) && !feedbackYaEnviado(item.idViaje);

    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.routeText}>{item.ciudadOrigen} → {item.ciudadDestino}</Text>
          <View style={[styles.badge, { backgroundColor: getStatusColor(item.estadoReserva) + '20' }]}>
            <Text style={[styles.badgeText, { color: getStatusColor(item.estadoReserva) }]}>
              {item.estadoReserva}
            </Text>
          </View>
        </View>
        
        <View style={styles.cardBody}>
          <View style={styles.scheduleContainer}>
            <View style={styles.scheduleItem}>
              <Ionicons name="airplane-outline" size={16} color={COLORS.secondary} />
              <View style={styles.scheduleTextBlock}>
                <Text style={styles.scheduleLabel}>Salida</Text>
                <Text style={styles.scheduleTime}>{formatearHora(item.fechaHoraSalida)}</Text>
                <Text style={styles.scheduleDate}>{formatearFecha(item.fechaHoraSalida)}</Text>
              </View>
            </View>

            <View style={styles.scheduleDivider}>
              <Ionicons name="arrow-forward" size={18} color={COLORS.placeholder} />
            </View>

            <View style={styles.scheduleItem}>
              <Ionicons name="flag-outline" size={16} color={COLORS.accent} />
              <View style={styles.scheduleTextBlock}>
                <Text style={styles.scheduleLabel}>Llegada</Text>
                <Text style={styles.scheduleTime}>{formatearHora(item.fechaHoraLlegada)}</Text>
                <Text style={styles.scheduleDate}>{formatearFecha(item.fechaHoraLlegada)}</Text>
              </View>
            </View>
          </View>

          <View style={styles.row}>
            <Text style={styles.label}>Reserva creada:</Text>
            <Text style={styles.value}>{new Date(item.fechaCreacion).toLocaleDateString()}</Text>
          </View>
          <View style={styles.row}>
            <Text style={styles.label}>Pasajeros:</Text>
            <Text style={styles.value}>{item.cantidadPasajeros}</Text>
          </View>
          <View style={styles.row}>
            <Text style={styles.label}>Monto Pagado:</Text>
            <Text style={[styles.value, { color: COLORS.accent, fontWeight: 'bold' }]}>
              Bs. {item.montoTotalPagado.toFixed(2)}
            </Text>
          </View>
        </View>

        {(isCancellable || canSendFeedback) && (
          <View style={styles.cardFooter}>
            {canSendFeedback && (
              <TouchableOpacity
                style={[styles.feedbackButton, feedbackLoading && { opacity: 0.5 }]}
                onPress={() => setViajeFeedback(item)}
                disabled={feedbackLoading}
              >
                <Ionicons name="star" size={14} color={COLORS.textLight} />
                <Text style={styles.feedbackButtonText}>Calificar viaje</Text>
              </TouchableOpacity>
            )}

            {isCancellable && (
              <TouchableOpacity
                style={[styles.cancelButton, cancelLoading && { opacity: 0.5 }]}
                onPress={() => cancelarReserva(item.idsReservas)}
                disabled={cancelLoading}
              >
                <Text style={styles.cancelButtonText}>Cancelar Reserva</Text>
              </TouchableOpacity>
            )}
          </View>
        )}
      </View>
    );
  };

  if (loading && !viajes.length) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color={COLORS.primary} />
      </View>
    );
  }

  if (error && !viajes.length) {
    return (
      <View style={styles.centerContainer}>
        <Text style={styles.errorText}>No se pudo cargar el historial.</Text>
        <TouchableOpacity style={globalStyles.btnPrimary} onPress={() => refetch()}>
          <Text style={TYPOGRAPHY.buttonText}>Reintentar</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={globalStyles.safeAreaContainer}>
      <View style={styles.filterContainer}>
        <TouchableOpacity
          style={[
            styles.filterButton,
            filtroViajes === 'COMPLETADA' && styles.filterButtonActive,
          ]}
          onPress={() => setFiltroViajes('COMPLETADA')}
        >
          <Text
            style={[
              styles.filterButtonText,
              filtroViajes === 'COMPLETADA' && styles.filterButtonTextActive,
            ]}
          >
            Completados ({totalCompletados})
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[
            styles.filterButton,
            filtroViajes === 'CONFIRMADA' && styles.filterButtonActive,
          ]}
          onPress={() => setFiltroViajes('CONFIRMADA')}
        >
          <Text
            style={[
              styles.filterButtonText,
              filtroViajes === 'CONFIRMADA' && styles.filterButtonTextActive,
            ]}
          >
            Confirmados ({totalConfirmados})
          </Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={viajesFiltrados}
        keyExtractor={(item) => `${item.idViaje}-${item.estadoReserva}`}
        renderItem={renderItem}
        contentContainerStyle={styles.listContent}
        initialNumToRender={5}
        refreshControl={
          <RefreshControl refreshing={loading} onRefresh={refetch} colors={[COLORS.primary]} />
        }
        ListEmptyComponent={
          !loading ? (
            <View style={styles.emptyContainer}>
              <Text style={styles.emptyText}>
                No tienes viajes {filtroViajes === 'COMPLETADA' ? 'completados' : 'confirmados'} para mostrar.
              </Text>
            </View>
          ) : null
        }
      />
      <TripFeedbackModal
        visible={viajeFeedback !== null}
        routeLabel={
          viajeFeedback
            ? `${viajeFeedback.ciudadOrigen} → ${viajeFeedback.ciudadDestino}`
            : ''
        }
        loading={feedbackLoading}
        onClose={() => setViajeFeedback(null)}
        onSubmit={handleSubmitFeedback}
      />
    </View>
  );
}
