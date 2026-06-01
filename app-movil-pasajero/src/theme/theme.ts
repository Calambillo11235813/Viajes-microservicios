import { StyleSheet, Dimensions } from 'react-native';

const { width, height } = Dimensions.get('window');

/**
 * Paleta de Colores de Alta Fidelidad para la Aplicación de Viajes
 * Diseñada bajo principios de accesibilidad, contraste (WCAG) y estética premium.
 */
export const COLORS = {
  // Identidad Visual
  primary: '#1E3A8A',       // Azul Marino Profundo (Representa seguridad, confianza y seriedad institucional)
  secondary: '#0284C7',     // Azul Cielo Corporativo (Para elementos interactivos primarios y navegación)
  accent: '#F59E0B',        // Ámbar/Oro Pasajero (Para selección de asientos, estrellas y llamadas a la acción críticas)
  
  // Estados del Sistema
  success: '#10B981',       // Verde Esmeralda (Boletos activos, pagos aprobados, check-in completado)
  danger: '#EF4444',        // Rojo Carmesí (Cancelaciones, errores de red, alarmas de proximidad)
  warning: '#F59E0B',       // Ámbar (Buses demorados, advertencias de cambios de itinerario)
  info: '#3B82F6',          // Azul Informativo (Notificaciones del sistema)

  // Neutros y Superficies
  background: '#F8FAFC',    // Gris Pizarra Ultra Claro (Fondo de pantalla que reduce la fatiga visual)
  surface: '#FFFFFF',       // Blanco Puro (Para tarjetas, contenedores modales y campos de entrada)
  border: '#E2E8F0',        // Gris Bordes (Líneas divisorias finas y limpias)
  placeholder: '#94A3B8',   // Gris Desactivado (Textos de guía en inputs y estados deshabilitados)

  // Tipografía Básica
  textPrimary: '#0F172A',   // Pizarra Oscuro (Máxima legibilidad para títulos y datos cruciales)
  textSecondary: '#475569', // Gris Carbón (Para descripciones, subtítulos y etiquetas de menor jerarquía)
  textLight: '#FFFFFF',     // Blanco Texto (Contraste sobre fondos oscuros o botones primarios)
};

/**
 * Sistema de Espaciado Basado en la Regla de los 8pt
 * Garantiza consistencia matemática y visual en pantallas de cualquier densidad de píxeles.
 */
export const SPACING = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

/**
 * Dimensiones del Dispositivo para Cálculos Dinámicos de UI
 */
export const SCREEN_METRICS = {
  width,
  height,
  isSmallDevice: width < 375,
};

/**
 * Sistema Tipográfico Estándar
 * NOTA: Usa fuentes nativas (San Francisco en iOS, Roboto en Android) para máxima fluidez.
 */
export const TYPOGRAPHY = {
  h1: {
    fontSize: 26,
    fontWeight: '700' as const,
    color: COLORS.textPrimary,
    letterSpacing: -0.5,
  },
  h2: {
    fontSize: 20,
    fontWeight: '600' as const,
    color: COLORS.textPrimary,
    letterSpacing: -0.3,
  },
  h3: {
    fontSize: 16,
    fontWeight: '600' as const,
    color: COLORS.textPrimary,
  },
  bodyLarge: {
    fontSize: 16,
    fontWeight: '400' as const,
    color: COLORS.textSecondary,
    lineHeight: 24,
  },
  body: {
    fontSize: 14,
    fontWeight: '400' as const,
    color: COLORS.textSecondary,
    lineHeight: 20,
  },
  caption: {
    fontSize: 12,
    fontWeight: '500' as const,
    color: COLORS.placeholder,
    letterSpacing: 0.2,
  },
  buttonText: {
    fontSize: 16,
    fontWeight: '600' as const,
    color: COLORS.textLight,
  },
};

/**
 * Estilos Globales Reutilizables (Layout, Tarjetas, Inputs, Botones)
 * Diseñado para evitar código duplicado en las vistas de pasajes y alertas.
 */
export const globalStyles = StyleSheet.create({
  // Contenedor Base de Pantallas
  safeAreaContainer: {
    flex: 1,
    backgroundColor: COLORS.background,
  },
  mainContainer: {
    flex: 1,
    paddingHorizontal: SPACING.md,
    paddingTop: SPACING.sm,
  },

  // Componente Tarjeta Elegante (Card UI)
  card: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: SPACING.md,
    marginVertical: SPACING.sm,
    borderWidth: 1,
    borderColor: COLORS.border,
    // Sombra Premium para iOS
    shadowColor: COLORS.textPrimary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.04,
    shadowRadius: 12,
    // Elevación Limpia para Android
    elevation: 3,
  },

  // Campos de Entrada de Texto (Inputs)
  inputContainer: {
    marginVertical: SPACING.sm,
  },
  inputLabel: {
    ...TYPOGRAPHY.caption,
    color: COLORS.textSecondary,
    marginBottom: SPACING.xs,
    textTransform: 'uppercase',
  },
  inputField: {
    backgroundColor: COLORS.surface,
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 10,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.sm,
    fontSize: 15,
    color: COLORS.textPrimary,
  },
  inputFieldActive: {
    borderColor: COLORS.secondary,
    borderWidth: 1.5,
  },

  // Botones Universales
  btnPrimary: {
    backgroundColor: COLORS.secondary,
    borderRadius: 12,
    paddingVertical: SPACING.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: SPACING.sm,
  },
  btnAccent: {
    backgroundColor: COLORS.accent,
    borderRadius: 12,
    paddingVertical: SPACING.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: SPACING.sm,
  },
  btnSecondary: {
    backgroundColor: 'transparent',
    borderRadius: 12,
    paddingVertical: SPACING.md,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderColor: COLORS.secondary,
    marginVertical: SPACING.sm,
  },
  btnDisabled: {
    backgroundColor: COLORS.border,
  },

  // Utilidades de Layout Frecuentes
  rowBetween: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  rowCenter: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  badge: {
    paddingHorizontal: SPACING.sm,
    paddingVertical: SPACING.xs,
    borderRadius: 99,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
