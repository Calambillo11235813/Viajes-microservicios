import React, { useRef } from 'react';
import { View, Text, FlatList, ActivityIndicator, TouchableOpacity, Platform } from 'react-native';
import { useQuery } from '@apollo/client/react';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BUSCAR_VIAJES, BuscarViajesData, BuscarViajesVars } from '@/graphql/queries/viajes';
import { useNavegacionTracking } from '@/hooks/useNavegacionTracking';
import { COLORS, TYPOGRAPHY, globalStyles } from '@/theme/theme';
import TripCard from '@/components/TripCard';
import { SearchStackParamList } from '@/navigation/SearchStackNavigator';
import { styles } from './styles/SearchResultsScreen.styles';

type Props = NativeStackScreenProps<SearchStackParamList, 'SearchResults'>;

/**
 * Pantalla de resultados de búsqueda.
 * Muestra la lista de viajes disponibles para la ruta y fecha seleccionadas.
 *
 * @param {Props} props - Recibe origen, destino y fecha por parámetros de ruta.
 */
export default function SearchResultsScreen({ route, navigation }: Props) {
  const { origen, destino, fecha } = route.params;
  const { idUsuario, registrarVisualizacionRuta } = useNavegacionTracking();
  const screenEntryTime = useRef<number>(Date.now());

  const { loading, error, data, refetch } = useQuery<BuscarViajesData, BuscarViajesVars>(
    BUSCAR_VIAJES,
    {
      variables: { origen, destino, fecha, idUsuario },
      fetchPolicy: 'network-only',
    }
  );

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

  const viajes = data?.buscarRutasYHorariosDisponibles?.contenido ?? [];

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
              const permanencia = Math.floor((Date.now() - screenEntryTime.current) / 1000);
              void registrarVisualizacionRuta({
                idRuta: Number(item.idRuta),
                idRutaVista: Number(item.idRuta),
                origen: item.ciudadOrigen,
                destino: item.ciudadDestino,
                ciudadOrigenVista: item.ciudadOrigen,
                ciudadDestinoVista: item.ciudadDestino,
                categoriaVista: item.categoriaTuristica,
                tiempoPermanenciaSeg: permanencia,
                dispositivo: Platform.OS,
              });
              navigation.navigate('SeatSelection', { idViaje: item.idViaje });
            }}
          />
        )}
      />
    </View>
  );
}
