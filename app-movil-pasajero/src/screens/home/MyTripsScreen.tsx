import React from 'react';
import { View, Text, StyleSheet, FlatList, ActivityIndicator, TouchableOpacity, RefreshControl } from 'react-native';
import { useMyTrips, ViajeConsolidado } from '@/hooks/useMyTrips';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';

export default function MyTripsScreen() {
  const { viajes, loading, error, refetch, cancelarReserva, cancelLoading } = useMyTrips();

  const getStatusColor = (estado: string) => {
    switch (estado) {
      case 'CONFIRMADA': return COLORS.success;
      case 'PENDIENTE': return COLORS.warning;
      case 'CANCELADA': return COLORS.danger;
      default: return COLORS.secondary;
    }
  };

  const renderItem = ({ item }: { item: ViajeConsolidado }) => {
    const isCancellable = item.estadoReserva === 'PENDIENTE' || item.estadoReserva === 'CONFIRMADA';

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
          <View style={styles.row}>
            <Text style={styles.label}>Fecha Salida:</Text>
            <Text style={styles.value}>{new Date(item.fechaHoraSalida).toLocaleDateString()}</Text>
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

        {isCancellable && (
          <View style={styles.cardFooter}>
            <TouchableOpacity 
              style={[styles.cancelButton, cancelLoading && { opacity: 0.5 }]}
              onPress={() => cancelarReserva(item.idsReservas)}
              disabled={cancelLoading}
            >
              <Text style={styles.cancelButtonText}>Cancelar Reserva</Text>
            </TouchableOpacity>
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
      <FlatList
        data={viajes}
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
              <Text style={styles.emptyText}>No tienes reservas de viajes aún.</Text>
            </View>
          ) : null
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: COLORS.background,
  },
  listContent: {
    padding: SPACING.md,
    paddingBottom: SPACING.xxl,
  },
  card: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: SPACING.lg,
    marginBottom: SPACING.md,
    borderWidth: 1,
    borderColor: COLORS.border,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: SPACING.sm,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
    paddingBottom: SPACING.sm,
  },
  routeText: {
    ...TYPOGRAPHY.h3,
    color: COLORS.primary,
  },
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  badgeText: {
    fontSize: 10,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  cardBody: {
    marginBottom: SPACING.sm,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 4,
  },
  label: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
  },
  value: {
    ...TYPOGRAPHY.body,
    color: COLORS.textPrimary,
  },
  cardFooter: {
    marginTop: SPACING.sm,
    alignItems: 'flex-end',
  },
  cancelButton: {
    paddingVertical: SPACING.xs,
    paddingHorizontal: SPACING.sm,
    borderWidth: 1,
    borderColor: COLORS.danger,
    borderRadius: 8,
  },
  cancelButtonText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.danger,
    fontWeight: '600',
  },
  errorText: {
    ...TYPOGRAPHY.body,
    color: COLORS.danger,
    marginBottom: SPACING.md,
  },
  emptyContainer: {
    padding: SPACING.xl,
    alignItems: 'center',
  },
  emptyText: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
    textAlign: 'center',
  },
});
