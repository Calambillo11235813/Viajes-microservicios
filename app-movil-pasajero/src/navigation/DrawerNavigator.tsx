import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, DeviceEventEmitter } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import CustomDrawer from '@/components/CustomDrawer';
import SearchStackNavigator, { SearchStackParamList } from '@/navigation/SearchStackNavigator';
import MyTripsScreen from '@/screens/home/MyTripsScreen';
import BuscarImagenScreen from '@/screens/home/BuscarImagenScreen';
import TraducirVisualScreen from '@/screens/home/TraducirVisualScreen';
import NotificacionesScreen from '@/screens/home/NotificacionesScreen';
import GenerarReelScreen from '@/screens/home/GenerarReelScreen';
import ProfileScreen from '@/screens/home/ProfileScreen';
import { COLORS, SPACING, TYPOGRAPHY } from '@/theme/theme';

/** Número simulado de notificaciones no leídas */
const NOTIF_BADGE_COUNT = 3;

/**
 * Navegador principal con Drawer personalizado.
 *
 * Reemplaza al `BottomTabNavigator` anterior para ofrecer un menú lateral
 * deslizable sin dependencias nativas (`react-native-reanimated`,
 * `react-native-gesture-handler`).
 *
 * Administra tres secciones:
 * - **Buscar**: Renderiza el `SearchStackNavigator` completo (Home → Resultados → Asientos → Pago).
 * - **MisViajes**: Renderiza la pantalla `MyTripsScreen` con un header propio.
 * - **Perfil**: Renderiza la pantalla `ProfileScreen`.
 *
 * Cada sección se renderiza condicionalmente según el estado `activeSection`.
 */
type SearchStackNavigation = {
  screen: keyof SearchStackParamList;
  params?: SearchStackParamList[keyof SearchStackParamList];
};

export default function DrawerNavigator() {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [activeSection, setActiveSection] = useState('Buscar');
  const [searchStackNav, setSearchStackNav] = useState<SearchStackNavigation | null>(null);

  const openDrawer = useCallback(() => setIsDrawerOpen(true), []);
  const closeDrawer = useCallback(() => setIsDrawerOpen(false), []);

  const handleNavigate = useCallback((section: string) => {
    if (section === 'Buscar') {
      setSearchStackNav(null);
    }
    setActiveSection(section);
  }, []);

  useEffect(() => {
    const subDrawer = DeviceEventEmitter.addListener('NAVIGATE_DRAWER', (section: string) => {
      setActiveSection(section);
    });

    const subSearch = DeviceEventEmitter.addListener(
      'NAVIGATE_SEARCH_STACK',
      (payload: SearchStackNavigation) => {
        setSearchStackNav(payload);
        setActiveSection('Buscar');
      }
    );

    return () => {
      subDrawer.remove();
      subSearch.remove();
    };
  }, []);

  /**
   * Obtiene el título del header según la sección activa.
   */
  const getHeaderTitle = (): string => {
    switch (activeSection) {
      case 'Buscar': return 'Buscar Viajes';
      case 'MisViajes': return 'Mis Viajes';
      case 'BuscarImagen': return 'Buscar por Imagen';
      case 'TraducirVisual': return 'Traducción Visual';
      case 'Notificaciones': return 'Notificaciones';
      case 'GenerarReel': return 'Crear Reel Turístico';
      case 'Perfil': return 'Mi Perfil';
      default: return '';
    }
  };

  /**
   * Renderiza la pantalla correspondiente a la sección activa.
   * Se usa renderizado condicional para evitar dependencias de enrutamiento.
   */
  const renderActiveScreen = () => {
    switch (activeSection) {
      case 'Buscar':
        return (
          <SearchStackNavigator
            key={
              searchStackNav
                ? `${searchStackNav.screen}-${JSON.stringify(searchStackNav.params ?? {})}`
                : 'default'
            }
            initialScreen={searchStackNav?.screen}
            initialParams={searchStackNav?.params}
          />
        );
      case 'MisViajes':
        return <MyTripsScreen />;
      case 'BuscarImagen':
        return <BuscarImagenScreen />;
      case 'TraducirVisual':
        return <TraducirVisualScreen />;
      case 'Notificaciones':
        return <NotificacionesScreen />;
      case 'GenerarReel':
        return <GenerarReelScreen />;
      case 'Perfil':
        return <ProfileScreen />;
      default:
        return <SearchStackNavigator key="default" />;
    }
  };

  /** Si estamos en la sección "Buscar", el SearchStackNavigator ya tiene su propio header */
  const showCustomHeader = activeSection !== 'Buscar';

  return (
    <View style={styles.container}>
      {/* ─── Header global con hamburguesa ─── */}
      {showCustomHeader && (
        <View style={styles.header}>
          <TouchableOpacity onPress={openDrawer} style={styles.hamburgerButton} activeOpacity={0.7}>
            <Ionicons name="menu" size={28} color={COLORS.primary} />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>{getHeaderTitle()}</Text>
          {/* Campanita de notificaciones en el header */}
          <TouchableOpacity
            style={styles.bellButton}
            onPress={() => handleNavigate('Notificaciones')}
            activeOpacity={0.7}
          >
            <Ionicons name="notifications-outline" size={24} color={COLORS.primary} />
            {activeSection !== 'Notificaciones' && NOTIF_BADGE_COUNT > 0 && (
              <View style={styles.bellBadge}>
                <Text style={styles.bellBadgeText}>{NOTIF_BADGE_COUNT}</Text>
              </View>
            )}
          </TouchableOpacity>
        </View>
      )}

      {/* ─── Contenido principal ─── */}
      <View style={styles.content}>
        {renderActiveScreen()}
      </View>

      {/* ─── Botón hamburguesa flotante para la sección Buscar (que tiene su propio header) ─── */}
      {!showCustomHeader && (
        <>
          <TouchableOpacity
            style={styles.floatingHamburger}
            onPress={openDrawer}
            activeOpacity={0.8}
          >
            <Ionicons name="menu" size={26} color={COLORS.textLight} />
          </TouchableOpacity>
          {/* Campanita flotante en la sección Buscar */}
          <TouchableOpacity
            style={styles.floatingBell}
            onPress={() => handleNavigate('Notificaciones')}
            activeOpacity={0.8}
          >
            <Ionicons name="notifications-outline" size={22} color={COLORS.textLight} />
            {NOTIF_BADGE_COUNT > 0 && (
              <View style={styles.floatingBellBadge}>
                <Text style={styles.floatingBellBadgeText}>{NOTIF_BADGE_COUNT}</Text>
              </View>
            )}
          </TouchableOpacity>
        </>
      )}

      {/* ─── Drawer lateral animado ─── */}
      <CustomDrawer
        isOpen={isDrawerOpen}
        onClose={closeDrawer}
        activeSection={activeSection}
        onNavigate={handleNavigate}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.background,
  },

  /* ── Header personalizado ── */
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: SPACING.xxl,
    paddingBottom: SPACING.md,
    paddingHorizontal: SPACING.md,
    backgroundColor: COLORS.surface,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
  },
  hamburgerButton: {
    padding: SPACING.xs,
    marginRight: SPACING.sm,
  },
  headerTitle: {
    ...TYPOGRAPHY.h3,
    color: COLORS.primary,
    flex: 1,
  },
  bellButton: {
    padding: SPACING.xs,
    position: 'relative',
  },
  bellBadge: {
    position: 'absolute',
    top: 0,
    right: -2,
    backgroundColor: COLORS.danger,
    width: 18,
    height: 18,
    borderRadius: 9,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: COLORS.surface,
  },
  bellBadgeText: {
    fontSize: 10,
    fontWeight: '700',
    color: COLORS.textLight,
  },

  /* ── Contenido ── */
  content: {
    flex: 1,
  },

  /* ── Botón hamburguesa flotante (para sección Buscar) ── */
  floatingHamburger: {
    position: 'absolute',
    top: SPACING.xxl + SPACING.xs,
    left: SPACING.md,
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: COLORS.primary + 'CC',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 50,
    elevation: 6,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  floatingBell: {
    position: 'absolute',
    top: SPACING.xxl + SPACING.xs,
    right: SPACING.md,
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: COLORS.primary + 'CC',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 50,
    elevation: 6,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  floatingBellBadge: {
    position: 'absolute',
    top: -2,
    right: -2,
    backgroundColor: COLORS.danger,
    width: 18,
    height: 18,
    borderRadius: 9,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: COLORS.primary,
  },
  floatingBellBadgeText: {
    fontSize: 10,
    fontWeight: '700',
    color: COLORS.textLight,
  },
});
