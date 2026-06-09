import React, { useCallback, useEffect, useMemo } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  Switch,
  ActivityIndicator,
  RefreshControl,
  DeviceEventEmitter,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useQuery, useMutation } from '@apollo/client/react';
import { COLORS, globalStyles } from '@/theme/theme';
import { styles } from './styles/NotificacionesScreen.styles';
import { useAuth } from '@/context/AuthContext';
import { usePushNotifications } from '@/context/PushNotificationsContext';
import { OBTENER_NOTIFICACIONES_USUARIO } from '@/graphql/queries/notificaciones';
import {
  MARCAR_NOTIFICACION_LEIDA,
  MARCAR_TODAS_NOTIFICACIONES_LEIDAS,
} from '@/graphql/mutations/notificaciones';
import { appLog } from '@/utils/logger';
import { NOTIFICACIONES_REFRESH_EVENT } from '@/utils/notificationsEvents';

type TipoVisual = 'exito' | 'alerta' | 'info';

interface NotificacionItem {
  id: string;
  tipo: string;
  titulo: string;
  mensaje: string;
  fechaCreacion: string;
  leido: boolean;
}

interface ObtenerNotificacionesData {
  obtenerNotificacionesUsuario: {
    contenido: NotificacionItem[];
    totalNoLeidas: number;
  };
}

const TIPO_CONFIG: Record<TipoVisual, {
  color: string;
  bgColor: string;
  icon: keyof typeof Ionicons.glyphMap;
}> = {
  exito: {
    color: COLORS.success,
    bgColor: COLORS.success + '12',
    icon: 'checkmark-circle',
  },
  alerta: {
    color: COLORS.danger,
    bgColor: COLORS.danger + '12',
    icon: 'warning',
  },
  info: {
    color: COLORS.info,
    bgColor: COLORS.info + '12',
    icon: 'information-circle',
  },
};

function mapTipoVisual(tipo: string): TipoVisual {
  switch (tipo) {
    case 'EMERGENCIA_RUTA':
    case 'CANCELACION':
    case 'RETRASO':
      return 'alerta';
    case 'DOCUMENTACION_FALTANTE':
    case 'CAMBIO_HORARIO':
      return 'info';
    default:
      return 'exito';
  }
}

function formatearTiempo(fechaIso: string): string {
  const fecha = new Date(fechaIso);
  if (Number.isNaN(fecha.getTime())) return fechaIso;

  const diffMs = Date.now() - fecha.getTime();
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return 'Ahora';
  if (diffMin < 60) return `Hace ${diffMin} min`;
  const diffHoras = Math.floor(diffMin / 60);
  if (diffHoras < 24) return `Hace ${diffHoras} h`;
  const diffDias = Math.floor(diffHoras / 24);
  return `Hace ${diffDias} d`;
}

export default function NotificacionesScreen() {
  const { user } = useAuth();
  const idUsuario = user?.idUsuario ? Number(user.idUsuario) : undefined;
  const { pushActivo, togglePush, pushRemotoDisponible, avisoPush } = usePushNotifications();

  const pollListaMs = pushRemotoDisponible ? 15000 : 10000;

  const { data, loading, refetch, networkStatus, error } = useQuery<ObtenerNotificacionesData>(
    OBTENER_NOTIFICACIONES_USUARIO,
    {
      variables: { idUsuario, estado: 'TODAS', pagina: 0, tamanio: 50 },
      skip: !idUsuario,
      fetchPolicy: 'cache-and-network',
      pollInterval: idUsuario ? pollListaMs : 0,
      notifyOnNetworkStatusChange: true,
    },
  );

  const refrescarLista = useCallback(() => {
    if (!idUsuario) return;
    refetch({ fetchPolicy: 'network-only' });
  }, [idUsuario, refetch]);

  useEffect(() => {
    const subRefresh = DeviceEventEmitter.addListener(
      NOTIFICACIONES_REFRESH_EVENT,
      refrescarLista,
    );

    return () => subRefresh.remove();
  }, [refrescarLista]);

  useEffect(() => {
    refrescarLista();
  }, [idUsuario, refrescarLista]);

  const [marcarLeidaMutation] = useMutation(MARCAR_NOTIFICACION_LEIDA);
  const [marcarTodasMutation] = useMutation(MARCAR_TODAS_NOTIFICACIONES_LEIDAS);

  const notificaciones = useMemo(
    () => data?.obtenerNotificacionesUsuario.contenido ?? [],
    [data],
  );

  const noLeidas = data?.obtenerNotificacionesUsuario.totalNoLeidas ?? 0;
  const refreshing = networkStatus === 4;

  useEffect(() => {
    if (loading) {
      appLog.info('Notif Móvil', 'Cargando notificaciones para usuario', idUsuario);
      return;
    }
    if (error) {
      appLog.warn('Notif Móvil', 'Error al cargar notificaciones:', error.message);
      return;
    }
    if (data?.obtenerNotificacionesUsuario) {
      const { contenido, totalNoLeidas } = data.obtenerNotificacionesUsuario;
      appLog.info('Notif Móvil', 'Notificaciones cargadas:', {
        total: contenido.length,
        noLeidas: totalNoLeidas,
        ultimas: contenido.slice(0, 3).map((n) => ({ id: n.id, tipo: n.tipo, titulo: n.titulo, leido: n.leido })),
      });
    }
  }, [data, loading, error, idUsuario]);

  const marcarLeida = useCallback(
    async (id: string) => {
      try {
        appLog.info('Notif Móvil', 'Marcando como leída:', id);
        await marcarLeidaMutation({ variables: { id } });
        await refetch();
        appLog.info('Notif Móvil', 'Notificación marcada como leída:', id);
      } catch (error) {
        appLog.warn('Notif Móvil', 'Error al marcar leída:', error);
      }
    },
    [marcarLeidaMutation, refetch],
  );

  const marcarTodoLeido = useCallback(async () => {
    if (!idUsuario) return;
    try {
      appLog.info('Notif Móvil', 'Marcando todas como leídas, usuario:', idUsuario);
      await marcarTodasMutation({ variables: { idUsuario } });
      await refetch();
      appLog.info('Notif Móvil', 'Todas las notificaciones marcadas como leídas');
    } catch (error) {
      appLog.warn('Notif Móvil', 'Error al marcar todas:', error);
    }
  }, [idUsuario, marcarTodasMutation, refetch]);

  const renderItem = ({ item }: { item: NotificacionItem }) => {
    const tipoVisual = mapTipoVisual(item.tipo);
    const config = TIPO_CONFIG[tipoVisual];

    return (
      <TouchableOpacity
        style={[
          styles.card,
          { borderLeftColor: config.color },
          !item.leido && { backgroundColor: config.bgColor },
        ]}
        onPress={() => !item.leido && marcarLeida(item.id)}
        activeOpacity={0.8}
      >
        <View style={styles.cardHeader}>
          <View style={[styles.iconCircle, { backgroundColor: config.color + '20' }]}>
            <Ionicons name={config.icon} size={22} color={config.color} />
          </View>
          <View style={styles.cardHeaderText}>
            <Text style={styles.cardTitle} numberOfLines={1}>
              {item.titulo}
            </Text>
            <Text style={styles.cardTime}>{formatearTiempo(item.fechaCreacion)}</Text>
          </View>
        </View>
        <Text style={styles.cardMessage}>{item.mensaje}</Text>
        {!item.leido && <View style={[styles.unreadDot, { backgroundColor: config.color }]} />}
      </TouchableOpacity>
    );
  };

  const renderEmpty = () => {
    if (loading) {
      return (
        <View style={styles.emptyContainer}>
          <ActivityIndicator size="large" color={COLORS.secondary} />
          <Text style={styles.emptyText}>Cargando notificaciones...</Text>
        </View>
      );
    }

    return (
      <View style={styles.emptyContainer}>
        <Ionicons name="notifications-off-outline" size={64} color={COLORS.placeholder} />
        <Text style={styles.emptyTitle}>Todo en orden</Text>
        <Text style={styles.emptyText}>
          No tienes notificaciones nuevas por el momento, ¡prepárate para tu próximo viaje!
        </Text>
      </View>
    );
  };

  return (
    <View style={globalStyles.safeAreaContainer}>
      <View style={styles.controlsContainer}>
        {!pushRemotoDisponible && avisoPush ? (
          <View style={styles.expoGoBanner}>
            <Ionicons name="information-circle" size={18} color={COLORS.info} />
            <Text style={styles.expoGoBannerText}>{avisoPush}</Text>
          </View>
        ) : (
          <View style={styles.switchRow}>
            <View style={styles.switchLabel}>
              <Ionicons
                name={pushActivo ? 'notifications' : 'notifications-off'}
                size={20}
                color={pushActivo ? COLORS.secondary : COLORS.placeholder}
              />
              <Text style={styles.switchText}>Recibir notificaciones push</Text>
            </View>
            <Switch
              value={pushActivo}
              onValueChange={togglePush}
              trackColor={{ false: COLORS.border, true: COLORS.secondary + '50' }}
              thumbColor={pushActivo ? COLORS.secondary : COLORS.placeholder}
            />
          </View>
        )}

        <View style={styles.actionsRow}>
          <Text style={styles.countText}>
            {noLeidas > 0 ? `${noLeidas} sin leer` : 'Todas leídas'}
          </Text>
          {noLeidas > 0 && (
            <TouchableOpacity onPress={marcarTodoLeido} activeOpacity={0.7}>
              <Text style={styles.markAllText}>Marcar todo como leído</Text>
            </TouchableOpacity>
          )}
        </View>
      </View>

      <FlatList
        data={notificaciones}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[
          styles.listContent,
          notificaciones.length === 0 && { flex: 1 },
        ]}
        ListEmptyComponent={renderEmpty}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={() => {
              appLog.info('Notif Móvil', 'Refrescando lista de notificaciones');
              refetch();
            }}
          />
        }
      />
    </View>
  );
}
