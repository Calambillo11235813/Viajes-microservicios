import { useState, useMemo, useEffect } from 'react';
import { useQuery, useMutation } from '@apollo/client/react';
import { CONSULTAR_HISTORIAL_VIAJES, CANCELAR_RESERVA } from '@/graphql/queries/historial';
import { useAuth } from '@/context/AuthContext';
import { Alert } from 'react-native';
import { appLog } from '@/utils/logger';

interface ConsultarHistorialViajesData {
  consultarHistorialViajes: {
    contenido: ViajeRaw[];
    totalPaginas: number;
    paginaActual: number;
    tieneSiguiente: boolean;
  };
}

export interface ViajeRaw {
  idReserva: string;
  idViaje: string;
  ciudadOrigen: string;
  ciudadDestino: string;
  fechaHoraSalida: string;
  fechaHoraLlegada: string;
  fechaCreacion: string;
  estadoReserva: string;
  montoTotalPagado: number;
  cantidadPasajeros: number;
}

export interface ViajeConsolidado {
  idViaje: string;
  idsReservas: string[];
  ciudadOrigen: string;
  ciudadDestino: string;
  fechaHoraSalida: string;
  fechaHoraLlegada: string;
  fechaCreacion: string;
  estadoReserva: string;
  montoTotalPagado: number;
  cantidadPasajeros: number;
}

export const useMyTrips = () => {
  const { user, token } = useAuth();
  const idUsuario = user?.idUsuario ? Number(user.idUsuario) : undefined;
  const [cancelLoading, setCancelLoading] = useState(false);

  const { data, loading, error, refetch } = useQuery<ConsultarHistorialViajesData>(
    CONSULTAR_HISTORIAL_VIAJES,
    {
      variables: { idUsuario, pagina: 0, tamanio: 250 },
      skip: !idUsuario,
      fetchPolicy: 'cache-and-network',
    }
  );

  const [cancelarReservaMutation] = useMutation<any>(CANCELAR_RESERVA);

  const viajesRaw = data?.consultarHistorialViajes?.contenido;

  useEffect(() => {
    appLog.info('Historial', 'Usuario:', user);
    appLog.info('Historial', 'idUsuario:', idUsuario, '| tieneToken:', Boolean(token));
    appLog.info('Historial', 'skip query:', !idUsuario);
  }, [user, idUsuario, token]);

  useEffect(() => {
    if (loading) {
      appLog.info('Historial', 'Cargando consultarHistorialViajes...');
    }
  }, [loading]);

  useEffect(() => {
    if (error) {
      appLog.error('Historial', 'Error GraphQL:', error.message);
      appLog.error('Historial', 'Detalle completo:', JSON.stringify(error, null, 2));
    }
  }, [error]);

  useEffect(() => {
    if (!data?.consultarHistorialViajes) return;

    const pagina = data.consultarHistorialViajes;
    appLog.info('Historial', 'Respuesta OK:', {
      totalRegistros: pagina.contenido.length,
      paginaActual: pagina.paginaActual,
      totalPaginas: pagina.totalPaginas,
      tieneSiguiente: pagina.tieneSiguiente,
    });
  }, [data]);

  const viajes = useMemo(() => {
    const map = new Map<string, ViajeConsolidado>();
    const raw = viajesRaw || [];
    raw.forEach(v => {
      // Agrupar por viaje y estado para no mezclar canceladas con confirmadas
      const key = `${v.idViaje}-${v.estadoReserva}`;
      if (!map.has(key)) {
        map.set(key, {
          idViaje: v.idViaje,
          idsReservas: [v.idReserva],
          ciudadOrigen: v.ciudadOrigen,
          ciudadDestino: v.ciudadDestino,
          fechaHoraSalida: v.fechaHoraSalida,
          fechaHoraLlegada: v.fechaHoraLlegada,
          fechaCreacion: v.fechaCreacion,
          estadoReserva: v.estadoReserva,
          montoTotalPagado: v.montoTotalPagado,
          cantidadPasajeros: v.cantidadPasajeros
        });
      } else {
        const existing = map.get(key)!;
        existing.idsReservas.push(v.idReserva);
        existing.montoTotalPagado += v.montoTotalPagado;
        existing.cantidadPasajeros += v.cantidadPasajeros;
        // Mantenemos la fechaCreacion de la más reciente
        if (new Date(v.fechaCreacion) > new Date(existing.fechaCreacion)) {
          existing.fechaCreacion = v.fechaCreacion;
        }
      }
    });
    return Array.from(map.values()).sort((a, b) => new Date(b.fechaCreacion).getTime() - new Date(a.fechaCreacion).getTime());
  }, [viajesRaw]);

  const cancelarReserva = (idsReservas: string[]) => {
    if (!idUsuario || idsReservas.length === 0) return;
    
    Alert.alert(
      'Cancelar Reserva',
      `¿Estás seguro de que deseas cancelar los ${idsReservas.length} asiento(s) de este viaje?`,
      [
        { text: 'No', style: 'cancel' },
        { 
          text: 'Sí, Cancelar', 
          style: 'destructive',
          onPress: async () => {
            setCancelLoading(true);
            try {
              let exitoTotal = true;
              for (const id of idsReservas) {
                const { data } = await cancelarReservaMutation({
                  variables: {
                    idReserva: parseInt(id),
                    idUsuario
                  }
                });
                if (!data?.cancelarReserva?.idReserva) {
                  exitoTotal = false;
                }
              }
              if (exitoTotal) {
                Alert.alert('Éxito', 'La reserva fue cancelada exitosamente.');
              } else {
                Alert.alert('Advertencia', 'Algunos asientos no pudieron ser cancelados.');
              }
            } catch (err: any) {
              Alert.alert('Error', err.message || 'Ocurrió un error al intentar cancelar.');
            } finally {
              setCancelLoading(false);
              refetch();
            }
          }
        }
      ]
    );
  };

  return {
    viajes,
    loading,
    error,
    refetch,
    cancelarReserva,
    cancelLoading
  };
};
