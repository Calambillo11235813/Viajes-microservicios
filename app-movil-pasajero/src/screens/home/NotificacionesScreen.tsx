import React, { useState, useCallback, useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Switch,
  Alert,
  Platform,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as Notifications from 'expo-notifications';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';

/* ─────────────────── Configuración Global de Notificaciones ─────────────────── */

/**
 * Handler global: define cómo se comportan las notificaciones cuando la app
 * está en primer plano. Sin esto, las alertas nativas no aparecerían
 * mientras el usuario tiene la app abierta.
 */
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,   // Mostrar banner visual
    shouldPlaySound: true,   // Reproducir sonido
    shouldSetBadge: true,    // Actualizar badge del ícono de la app
    shouldShowBanner: true,  // Banner en la barra superior
    shouldShowList: true,    // Aparecer en el centro de notificaciones del OS
  }),
});

/* ─────────────────── Tipos ─────────────────── */

type TipoNotificacion = 'exito' | 'alerta' | 'info';

interface Notificacion {
  id: string;
  tipo: TipoNotificacion;
  titulo: string;
  mensaje: string;
  tiempo: string;
  leido: boolean;
}

/* ─────────────────── Helpers ─────────────────── */

const TIPO_CONFIG: Record<TipoNotificacion, {
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

/** Datos iniciales de ejemplo (mock) */
const MOCK_NOTIFICACIONES: Notificacion[] = [
  {
    id: '1',
    tipo: 'exito',
    titulo: 'Compra confirmada',
    mensaje: 'Tu reserva para el viaje La Paz → Cochabamba ha sido confirmada exitosamente. ¡Buen viaje!',
    tiempo: 'Hace 5 min',
    leido: false,
  },
  {
    id: '2',
    tipo: 'alerta',
    titulo: 'Bus con retraso',
    mensaje: 'El bus de las 14:30 con destino a Sucre presenta un retraso de 25 minutos. Disculpe las molestias.',
    tiempo: 'Hace 20 min',
    leido: false,
  },
  {
    id: '3',
    tipo: 'info',
    titulo: 'Nuevo destino disponible',
    mensaje: 'Ahora puedes reservar viajes directos a Uyuni desde La Paz. ¡Explora el salar!',
    tiempo: 'Hace 1 hora',
    leido: false,
  },
];

/* ─────────────────── Componente ─────────────────── */

/**
 * Centro de Notificaciones (CU-13).
 *
 * Presenta un listado de alertas push simuladas con tipificación visual
 * (éxito / alerta / info), un interruptor general y acciones de lectura.
 */
export default function NotificacionesScreen() {
  const [notificaciones, setNotificaciones] = useState<Notificacion[]>(MOCK_NOTIFICACIONES);
  const [pushActivo, setPushActivo] = useState(true);
  const [permisosConcedidos, setPermisosConcedidos] = useState(false);

  /**
   * Solicita permisos de notificación al sistema operativo.
   * En Android 13+ se requiere permiso explícito; en iOS siempre.
   */
  const solicitarPermisos = useCallback(async () => {
    if (Platform.OS === 'android') {
      // Canal de Android obligatorio para Expo SDK 54+
      await Notifications.setNotificationChannelAsync('viajes', {
        name: 'Viajes',
        importance: Notifications.AndroidImportance.HIGH,
        sound: 'default',
        vibrationPattern: [0, 250, 250, 250],
      });
    }

    const { status } = await Notifications.requestPermissionsAsync();
    setPermisosConcedidos(status === 'granted');

    if (status !== 'granted') {
      Alert.alert(
        'Permisos denegados',
        'No podrás recibir notificaciones push. Actívalas desde los ajustes de tu dispositivo.',
      );
    }
  }, []);

  // Solicitar permisos automáticamente al montar la pantalla
  useEffect(() => {
    solicitarPermisos();
  }, [solicitarPermisos]);

  /**
   * Automatización reactiva: cuando el Switch (pushActivo) está activo
   * Y los permisos fueron concedidos, dispara una notificación local
   * cada 5 segundos simulando eventos del backend.
   * Al apagar el Switch o desmontar el componente, limpia el intervalo.
   */
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    // Limpiar cualquier intervalo previo antes de evaluar
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }

    if (pushActivo && permisosConcedidos) {
      intervalRef.current = setInterval(async () => {
        await Notifications.scheduleNotificationAsync({
          content: {
            title: '🚌 ¡Actualización de tu viaje!',
            body: 'Tu bus hacia Cochabamba está abordando en el Andén 5.',
            sound: 'default',
            data: { tipo: 'alerta', idViaje: 999 },
          },
          trigger: {
            type: Notifications.SchedulableTriggerInputTypes.TIME_INTERVAL,
            seconds: 1, // Se dispara 1s después de ser programada
          },
        });
      }, 5000); // Programa una nueva notificación cada 5 segundos
    }

    // Cleanup: se ejecuta al cambiar pushActivo, permisosConcedidos, o al desmontar
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [pushActivo, permisosConcedidos]);

  /** Marca todas como leídas */
  const marcarTodoLeido = useCallback(() => {
    setNotificaciones((prev) => prev.map((n) => ({ ...n, leido: true })));
  }, []);

  /** Elimina una notificación individualmente con swipe conceptual (tap) */
  const eliminarNotificacion = useCallback((id: string) => {
    setNotificaciones((prev) => prev.filter((n) => n.id !== id));
  }, []);

  /** Marca una sola como leída */
  const marcarLeida = useCallback((id: string) => {
    setNotificaciones((prev) =>
      prev.map((n) => (n.id === id ? { ...n, leido: true } : n)),
    );
  }, []);

  /** Contador de no leídas */
  const noLeidas = notificaciones.filter((n) => !n.leido).length;

  /* ── Renderizado de cada tarjeta ── */
  const renderItem = ({ item }: { item: Notificacion }) => {
    const config = TIPO_CONFIG[item.tipo];

    return (
      <TouchableOpacity
        style={[
          styles.card,
          { borderLeftColor: config.color },
          !item.leido && { backgroundColor: config.bgColor },
        ]}
        onPress={() => marcarLeida(item.id)}
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
            <Text style={styles.cardTime}>{item.tiempo}</Text>
          </View>
          <TouchableOpacity
            style={styles.deleteButton}
            onPress={() => eliminarNotificacion(item.id)}
            hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
          >
            <Ionicons name="close-circle-outline" size={20} color={COLORS.placeholder} />
          </TouchableOpacity>
        </View>
        <Text style={styles.cardMessage}>{item.mensaje}</Text>
        {!item.leido && <View style={[styles.unreadDot, { backgroundColor: config.color }]} />}
      </TouchableOpacity>
    );
  };

  /* ── Estado vacío ── */
  const renderEmpty = () => (
    <View style={styles.emptyContainer}>
      <Ionicons name="notifications-off-outline" size={64} color={COLORS.placeholder} />
      <Text style={styles.emptyTitle}>Todo en orden</Text>
      <Text style={styles.emptyText}>
        No tienes notificaciones nuevas por el momento, ¡prepárate para tu próximo viaje!
      </Text>
    </View>
  );

  return (
    <View style={globalStyles.safeAreaContainer}>
      {/* ─── Controles superiores ─── */}
      <View style={styles.controlsContainer}>
        {/* Switch de notificaciones push */}
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
            onValueChange={setPushActivo}
            trackColor={{ false: COLORS.border, true: COLORS.secondary + '50' }}
            thumbColor={pushActivo ? COLORS.secondary : COLORS.placeholder}
          />
        </View>

        {/* Barra de acciones */}
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

      {/* ─── Lista de notificaciones ─── */}
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
      />
    </View>
  );
}

/* ─────────────────── Estilos ─────────────────── */

const styles = StyleSheet.create({
  /* ── Controles Superiores ── */
  controlsContainer: {
    backgroundColor: COLORS.surface,
    paddingHorizontal: SPACING.md,
    paddingTop: SPACING.sm,
    paddingBottom: SPACING.md,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
  },
  switchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: SPACING.sm,
  },
  switchLabel: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: SPACING.sm,
  },
  switchText: {
    ...TYPOGRAPHY.body,
    color: COLORS.textPrimary,
    fontWeight: '500',
  },
  actionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: SPACING.xs,
  },
  countText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.textSecondary,
  },
  markAllText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.secondary,
    fontWeight: '700',
  },


  /* ── Lista ── */
  listContent: {
    paddingHorizontal: SPACING.md,
    paddingTop: SPACING.sm,
    paddingBottom: SPACING.xxl,
  },

  /* ── Tarjeta ── */
  card: {
    backgroundColor: COLORS.surface,
    borderRadius: 14,
    padding: SPACING.md,
    marginBottom: SPACING.sm,
    borderLeftWidth: 4,
    borderWidth: 1,
    borderColor: COLORS.border,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    position: 'relative',
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: SPACING.sm,
  },
  iconCircle: {
    width: 38,
    height: 38,
    borderRadius: 19,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: SPACING.sm,
  },
  cardHeaderText: {
    flex: 1,
  },
  cardTitle: {
    ...TYPOGRAPHY.h3,
    color: COLORS.textPrimary,
    marginBottom: 2,
  },
  cardTime: {
    ...TYPOGRAPHY.caption,
    color: COLORS.placeholder,
  },
  deleteButton: {
    padding: SPACING.xs,
  },
  cardMessage: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
    lineHeight: 20,
    paddingLeft: 38 + SPACING.sm, // Alineado con el texto del header
  },
  unreadDot: {
    position: 'absolute',
    top: SPACING.sm,
    right: SPACING.sm,
    width: 8,
    height: 8,
    borderRadius: 4,
  },

  /* ── Estado Vacío ── */
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: SPACING.xl,
  },
  emptyTitle: {
    ...TYPOGRAPHY.h2,
    color: COLORS.textPrimary,
    marginTop: SPACING.lg,
    marginBottom: SPACING.sm,
  },
  emptyText: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
    textAlign: 'center',
    lineHeight: 22,
  },
});
