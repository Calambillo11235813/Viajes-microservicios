import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';

/**
 * Propiedades para el componente TripCard.
 * Define la estructura de datos que recibe la tarjeta para renderizar un viaje.
 */
export interface TripCardProps {
  idViaje: string;
  ciudadOrigen: string;
  ciudadDestino: string;
  fechaHoraSalida: string;
  fechaHoraLlegada: string;
  precioBase: number;
  tipoBus: string;
  capacidadTotalAsientos: number;
  onPress: () => void;
}

/**
 * Componente visual que representa un viaje disponible en forma de tarjeta.
 * Muestra el origen, destino, horarios, precio y disponibilidad de asientos.
 *
 * @param {TripCardProps} props - Propiedades del viaje.
 */
export default function TripCard({
  ciudadOrigen,
  ciudadDestino,
  fechaHoraSalida,
  fechaHoraLlegada,
  precioBase,
  tipoBus,
  capacidadTotalAsientos,
  onPress
}: TripCardProps) {
  // Format dates: From "2024-05-10T14:30:00" to time only for display
  const formatTime = (isoString: string) => {
    try {
      const date = new Date(isoString);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return isoString;
    }
  };

  const formatDate = (isoString: string) => {
    try {
      const date = new Date(isoString);
      return date.toLocaleDateString([], { day: '2-digit', month: 'short' });
    } catch {
      return isoString;
    }
  };

  return (
    <TouchableOpacity 
      style={globalStyles.card} 
      activeOpacity={0.7}
      onPress={onPress}
    >
      {/* Encabezado: Fecha y Precio */}
      <View style={[globalStyles.rowBetween, { marginBottom: SPACING.md }]}>
        <View style={styles.dateBadge}>
          <Text style={styles.dateText}>{formatDate(fechaHoraSalida)}</Text>
        </View>
        <Text style={styles.priceText}>Bs. {precioBase.toFixed(2)}</Text>
      </View>

      {/* Ruta: Origen -> Destino */}
      <View style={styles.routeContainer}>
        <View style={styles.routeColumn}>
          <Text style={styles.timeText}>{formatTime(fechaHoraSalida)}</Text>
          <Text style={styles.cityText} numberOfLines={1}>{ciudadOrigen}</Text>
        </View>
        
        <View style={styles.routeLineContainer}>
          <View style={styles.dot} />
          <View style={styles.line} />
          <View style={[styles.dot, { backgroundColor: COLORS.secondary }]} />
        </View>

        <View style={[styles.routeColumn, { alignItems: 'flex-end' }]}>
          <Text style={styles.timeText}>{formatTime(fechaHoraLlegada)}</Text>
          <Text style={styles.cityText} numberOfLines={1}>{ciudadDestino}</Text>
        </View>
      </View>

      {/* Pie de la tarjeta: Info del Bus */}
      <View style={styles.footerContainer}>
        <View style={globalStyles.rowCenter}>
          <Text style={styles.busTypeText}>{tipoBus}</Text>
        </View>
        <Text style={styles.seatsText}>{capacidadTotalAsientos} Asientos disp.</Text>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  dateBadge: {
    backgroundColor: COLORS.background,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.xs,
    borderRadius: 8,
  },
  dateText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.primary,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  priceText: {
    ...TYPOGRAPHY.h2,
    color: COLORS.accent,
  },
  routeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: SPACING.md,
  },
  routeColumn: {
    flex: 1,
  },
  timeText: {
    ...TYPOGRAPHY.h3,
    marginBottom: SPACING.xs,
  },
  cityText: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
  },
  routeLineContainer: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: SPACING.sm,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: COLORS.primary,
  },
  line: {
    flex: 1,
    height: 2,
    backgroundColor: COLORS.border,
    borderStyle: 'dashed',
  },
  footerContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: COLORS.border,
    paddingTop: SPACING.sm,
  },
  busTypeText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.secondary,
    fontWeight: '600',
    backgroundColor: COLORS.background,
    paddingHorizontal: SPACING.sm,
    paddingVertical: 2,
    borderRadius: 4,
  },
  seatsText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.success,
  }
});
