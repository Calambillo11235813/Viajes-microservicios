import React from 'react';
import { View, Text, StyleSheet, FlatList, ActivityIndicator, TouchableOpacity } from 'react-native';
import { useQuery } from '@apollo/client/react';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BUSCAR_VIAJES } from '@/graphql/queries/viajes';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles, SCREEN_METRICS } from '@/theme/theme';
import TripCard from '@/components/TripCard';
import { SearchStackParamList } from '@/navigation/SearchStackNavigator';

type Props = NativeStackScreenProps<SearchStackParamList, 'SearchResults'>;

/**
 * Pantalla de resultados de búsqueda.
 * Muestra la lista de viajes disponibles para la ruta y fecha seleccionadas.
 *
 * @param {Props} props - Recibe origen, destino y fecha por parámetros de ruta.
 */
export default function SearchResultsScreen({ route, navigation }: Props) {
  const { origen, destino, fecha } = route.params;

  const { loading, error, data, refetch } = useQuery<any>(BUSCAR_VIAJES, {
    variables: { origen, destino, fecha },
    fetchPolicy: 'network-only' // Asegurar datos frescos
  });

  const renderHeader = () => (
    <View style={styles.header}>
      <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
        <Text style={styles.backButtonText}>← Volver</Text>
      </TouchableOpacity>
      <View style={styles.headerTitleContainer}>
        <Text style={styles.headerTitle}>{origen} a {destino}</Text>
        <Text style={styles.headerSubtitle}>{fecha}</Text>
      </View>
    </View>
  );

  if (loading) {
    return (
      <View style={globalStyles.safeAreaContainer}>
        {renderHeader()}
        <View style={styles.centerContainer}>
          <ActivityIndicator size="large" color={COLORS.primary} />
          <Text style={styles.loadingText}>Buscando viajes disponibles...</Text>
        </View>
      </View>
    );
  }

  if (error) {
    return (
      <View style={globalStyles.safeAreaContainer}>
        {renderHeader()}
        <View style={styles.centerContainer}>
          <Text style={styles.errorTitle}>Oops!</Text>
          <Text style={styles.errorText}>No pudimos cargar los viajes. Verifica tu conexión.</Text>
          <TouchableOpacity style={globalStyles.btnPrimary} onPress={() => refetch()}>
            <Text style={TYPOGRAPHY.buttonText}>Reintentar</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  const viajes = data?.buscarRutasYHorariosDisponibles || [];

  return (
    <View style={globalStyles.safeAreaContainer}>
      {renderHeader()}

      <FlatList
        data={viajes}
        keyExtractor={(item) => item.idViaje.toString()}
        contentContainerStyle={styles.listContainer}
        initialNumToRender={5}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyTitle}>Sin resultados</Text>
            <Text style={styles.emptyText}>No encontramos viajes programados para esta fecha y ruta.</Text>
            <TouchableOpacity style={globalStyles.btnSecondary} onPress={() => navigation.goBack()}>
              <Text style={[TYPOGRAPHY.buttonText, { color: COLORS.secondary }]}>Cambiar búsqueda</Text>
            </TouchableOpacity>
          </View>
        }
        renderItem={({ item }) => (
          <TripCard
            idViaje={item.idViaje}
            ciudadOrigen={item.ciudadOrigen}
            ciudadDestino={item.ciudadDestino}
            fechaHoraSalida={item.fechaHoraSalida}
            fechaHoraLlegada={item.fechaHoraLlegada}
            precioBase={item.precioBase}
            tipoBus={item.tipoBus}
            capacidadTotalAsientos={item.capacidadTotalAsientos}
            onPress={() => {
              navigation.navigate('SeatSelection', { idViaje: item.idViaje });
            }}
          />
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  header: {
    paddingTop: SPACING.xl + 70,
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
  headerTitleContainer: {
    alignItems: 'flex-start',
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
  errorTitle: {
    ...TYPOGRAPHY.h2,
    color: COLORS.danger,
    marginBottom: SPACING.xs,
  },
  errorText: {
    ...TYPOGRAPHY.body,
    textAlign: 'center',
    marginBottom: SPACING.lg,
    color: COLORS.textSecondary,
  },
  listContainer: {
    padding: SPACING.md,
    flexGrow: 1,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: SCREEN_METRICS.height * 0.2,
  },
  emptyTitle: {
    ...TYPOGRAPHY.h3,
    color: COLORS.textPrimary,
    marginBottom: SPACING.xs,
  },
  emptyText: {
    ...TYPOGRAPHY.body,
    textAlign: 'center',
    marginBottom: SPACING.xl,
    color: COLORS.textSecondary,
  },
});
