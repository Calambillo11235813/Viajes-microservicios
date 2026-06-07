import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';

/**
 * Propiedades del componente de recomendación personalizada (CU-09).
 */
export interface RecommendationCardProps {
  ciudadOrigen: string;
  ciudadDestino: string;
  probabilidad?: number | null;
  categoriaPreferida?: string | null;
  loading?: boolean;
  onPress: () => void;
}

/**
 * Tarjeta pasiva que muestra la ruta recomendada por el motor IA.
 * Diseñada para integrarse en el Home sin interrumpir la búsqueda principal.
 */
export default function RecommendationCard({
  ciudadOrigen,
  ciudadDestino,
  probabilidad,
  categoriaPreferida,
  loading = false,
  onPress,
}: RecommendationCardProps) {
  if (loading) {
    return (
      <View style={[globalStyles.card, styles.card]}>
        <View style={styles.skeletonBadge} />
        <View style={styles.skeletonTitle} />
        <View style={styles.skeletonLine} />
      </View>
    );
  }

  const porcentaje =
    typeof probabilidad === 'number'
      ? `${Math.round(probabilidad * 100)}% de afinidad`
      : null;

  return (
    <TouchableOpacity
      style={[globalStyles.card, styles.card]}
      onPress={onPress}
      activeOpacity={0.75}
    >
      <Text style={styles.badge}>Recomendado para ti</Text>
      <Text style={styles.route}>
        {ciudadOrigen} → {ciudadDestino}
      </Text>
      {categoriaPreferida ? (
        <Text style={styles.meta}>{categoriaPreferida}</Text>
      ) : null}
      {porcentaje ? <Text style={styles.confidence}>{porcentaje}</Text> : null}
      <Text style={styles.cta}>Ver viajes disponibles</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    borderLeftWidth: 4,
    borderLeftColor: COLORS.accent,
  },
  badge: {
    ...TYPOGRAPHY.caption,
    color: COLORS.accent,
    fontWeight: '700',
    textTransform: 'uppercase',
    marginBottom: SPACING.sm,
  },
  route: {
    ...TYPOGRAPHY.h2,
    color: COLORS.textPrimary,
    marginBottom: SPACING.xs,
  },
  meta: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
    marginBottom: SPACING.xs,
  },
  confidence: {
    ...TYPOGRAPHY.caption,
    color: COLORS.success,
    fontWeight: '600',
    marginBottom: SPACING.sm,
  },
  cta: {
    ...TYPOGRAPHY.body,
    color: COLORS.secondary,
    fontWeight: '600',
  },
  skeletonBadge: {
    width: 140,
    height: 12,
    backgroundColor: COLORS.border,
    borderRadius: 6,
    marginBottom: SPACING.sm,
  },
  skeletonTitle: {
    width: '70%',
    height: 20,
    backgroundColor: COLORS.border,
    borderRadius: 6,
    marginBottom: SPACING.sm,
  },
  skeletonLine: {
    width: '50%',
    height: 14,
    backgroundColor: COLORS.border,
    borderRadius: 6,
  },
});
