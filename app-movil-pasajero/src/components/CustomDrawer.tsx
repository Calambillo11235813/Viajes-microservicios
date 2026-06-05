import React, { useRef, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TouchableWithoutFeedback,
  Animated,
  Dimensions,
  StatusBar,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '@/context/AuthContext';
import { COLORS, SPACING, TYPOGRAPHY } from '@/theme/theme';

const { width: SCREEN_WIDTH } = Dimensions.get('window');
/** Ancho del panel lateral: 75% de la pantalla */
const DRAWER_WIDTH = SCREEN_WIDTH * 0.75;

/**
 * Elemento de navegación dentro del Drawer.
 */
interface DrawerItem {
  /** Clave única para identificar la sección activa */
  key: string;
  /** Etiqueta visible del elemento */
  label: string;
  /** Nombre del ícono de Ionicons */
  icon: keyof typeof Ionicons.glyphMap;
  /** Ícono cuando el elemento está seleccionado */
  iconFocused: keyof typeof Ionicons.glyphMap;
}

interface CustomDrawerProps {
  /** Indica si el drawer está visible */
  isOpen: boolean;
  /** Callback para cerrar el drawer */
  onClose: () => void;
  /** Sección activa actualmente */
  activeSection: string;
  /** Callback cuando el usuario selecciona una sección */
  onNavigate: (section: string) => void;
}

/** Secciones disponibles en el menú lateral */
const DRAWER_ITEMS: DrawerItem[] = [
  { key: 'Buscar', label: 'Buscar Viajes', icon: 'search-outline', iconFocused: 'search' },
  { key: 'MisViajes', label: 'Mis Viajes', icon: 'bus-outline', iconFocused: 'bus' },
  { key: 'BuscarImagen', label: 'Buscar por Imagen', icon: 'camera-outline', iconFocused: 'camera' },
  { key: 'TraducirVisual', label: 'Traducción Visual', icon: 'language-outline', iconFocused: 'language' },
  { key: 'Notificaciones', label: 'Notificaciones', icon: 'notifications-outline', iconFocused: 'notifications' },
  { key: 'GenerarReel', label: 'Crear Reel Turístico', icon: 'film-outline', iconFocused: 'film' },
  { key: 'Perfil', label: 'Mi Perfil', icon: 'person-outline', iconFocused: 'person' },
];

/**
 * Componente de menú lateral animado (Drawer) construido 100% con la
 * API nativa `Animated` de React Native. No requiere `react-native-reanimated`,
 * `react-native-gesture-handler` ni ninguna librería nativa adicional.
 *
 * Incluye:
 * - Panel deslizable con animación `spring` desde el borde izquierdo.
 * - Overlay semitransparente que cierra el drawer al tocarlo.
 * - Avatar y nombre del usuario autenticado.
 * - Ítems de navegación con indicador visual de sección activa.
 * - Botón de cerrar sesión en la parte inferior.
 *
 * @param {CustomDrawerProps} props
 */
export default function CustomDrawer({ isOpen, onClose, activeSection, onNavigate }: CustomDrawerProps) {
  const { user, logout } = useAuth();

  /** Valor animado: 0 = cerrado, 1 = abierto */
  const animValue = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.spring(animValue, {
      toValue: isOpen ? 1 : 0,
      useNativeDriver: true,
      friction: 12,
      tension: 65,
    }).start();
  }, [isOpen, animValue]);

  /** Desplazamiento horizontal del panel */
  const translateX = animValue.interpolate({
    inputRange: [0, 1],
    outputRange: [-DRAWER_WIDTH, 0],
  });

  /** Opacidad del overlay oscuro */
  const overlayOpacity = animValue.interpolate({
    inputRange: [0, 1],
    outputRange: [0, 0.5],
  });

  const handleItemPress = useCallback((key: string) => {
    onNavigate(key);
    onClose();
  }, [onNavigate, onClose]);

  const handleLogout = useCallback(() => {
    onClose();
    logout();
  }, [onClose, logout]);

  const inicial = user?.nombreCompleto?.charAt(0)?.toUpperCase() || 'U';

  return (
    <>
      {/* ─── Overlay oscuro (fondo) ─── */}
      {isOpen && (
        <TouchableWithoutFeedback onPress={onClose}>
          <Animated.View style={[styles.overlay, { opacity: overlayOpacity }]} />
        </TouchableWithoutFeedback>
      )}

      {/* ─── Panel lateral ─── */}
      <Animated.View style={[styles.drawerPanel, { transform: [{ translateX }] }]}>  
        <StatusBar barStyle="light-content" />

        {/* ─── Cabecera con perfil ─── */}
        <View style={styles.drawerHeader}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{inicial}</Text>
          </View>
          <Text style={styles.userName} numberOfLines={1}>
            {user?.nombreCompleto || 'Pasajero'}
          </Text>
          <Text style={styles.userEmail} numberOfLines={1}>
            {user?.email || ''}
          </Text>
        </View>

        {/* ─── Separador ─── */}
        <View style={styles.separator} />

        {/* ─── Ítems de navegación ─── */}
        <View style={styles.menuContainer}>
          {DRAWER_ITEMS.map((item) => {
            const isActive = activeSection === item.key;
            return (
              <TouchableOpacity
                key={item.key}
                style={[styles.menuItem, isActive && styles.menuItemActive]}
                onPress={() => handleItemPress(item.key)}
                activeOpacity={0.7}
              >
                <Ionicons
                  name={isActive ? item.iconFocused : item.icon}
                  size={22}
                  color={isActive ? COLORS.secondary : COLORS.textSecondary}
                />
                <Text style={[styles.menuLabel, isActive && styles.menuLabelActive]}>
                  {item.label}
                </Text>
                {isActive && <View style={styles.activeIndicator} />}
              </TouchableOpacity>
            );
          })}
        </View>

        {/* ─── Botón de cerrar sesión (pie del drawer) ─── */}
        <View style={styles.drawerFooter}>
          <View style={styles.separator} />
          <TouchableOpacity style={styles.logoutButton} onPress={handleLogout} activeOpacity={0.7}>
            <Ionicons name="log-out-outline" size={22} color={COLORS.danger} />
            <Text style={styles.logoutText}>Cerrar Sesión</Text>
          </TouchableOpacity>
        </View>
      </Animated.View>
    </>
  );
}

const styles = StyleSheet.create({
  /* ── Overlay ── */
  overlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: '#000',
    zIndex: 90,
  },

  /* ── Panel ── */
  drawerPanel: {
    position: 'absolute',
    top: 0,
    left: 0,
    bottom: 0,
    width: DRAWER_WIDTH,
    backgroundColor: COLORS.surface,
    zIndex: 100,
    elevation: 16,
    shadowColor: '#000',
    shadowOffset: { width: 4, height: 0 },
    shadowOpacity: 0.25,
    shadowRadius: 12,
  },

  /* ── Cabecera ── */
  drawerHeader: {
    backgroundColor: COLORS.primary,
    paddingTop: SPACING.xxl + SPACING.md,
    paddingBottom: SPACING.lg,
    paddingHorizontal: SPACING.lg,
  },
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: COLORS.accent,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: SPACING.sm,
    borderWidth: 2,
    borderColor: 'rgba(255,255,255,0.3)',
  },
  avatarText: {
    fontSize: 24,
    fontWeight: '700',
    color: COLORS.textLight,
  },
  userName: {
    ...TYPOGRAPHY.h3,
    color: COLORS.textLight,
    marginBottom: 2,
  },
  userEmail: {
    ...TYPOGRAPHY.caption,
    color: 'rgba(255,255,255,0.7)',
  },

  /* ── Separador ── */
  separator: {
    height: 1,
    backgroundColor: COLORS.border,
    marginHorizontal: SPACING.md,
  },

  /* ── Menú ── */
  menuContainer: {
    flex: 1,
    paddingTop: SPACING.sm,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: SPACING.md,
    paddingHorizontal: SPACING.lg,
    marginHorizontal: SPACING.sm,
    borderRadius: 12,
    marginBottom: 2,
  },
  menuItemActive: {
    backgroundColor: COLORS.secondary + '15',
  },
  menuLabel: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
    fontWeight: '500',
    marginLeft: SPACING.md,
    flex: 1,
  },
  menuLabelActive: {
    color: COLORS.secondary,
    fontWeight: '700',
  },
  activeIndicator: {
    width: 4,
    height: 20,
    borderRadius: 2,
    backgroundColor: COLORS.secondary,
  },

  /* ── Pie ── */
  drawerFooter: {
    paddingBottom: SPACING.xl,
  },
  logoutButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: SPACING.md,
    paddingHorizontal: SPACING.lg,
    marginHorizontal: SPACING.sm,
    borderRadius: 12,
    marginTop: SPACING.sm,
  },
  logoutText: {
    ...TYPOGRAPHY.body,
    color: COLORS.danger,
    fontWeight: '600',
    marginLeft: SPACING.md,
  },
});
