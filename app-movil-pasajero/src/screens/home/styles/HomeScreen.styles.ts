import { StyleSheet } from 'react-native';
import { COLORS, SPACING, TYPOGRAPHY } from '@/theme/theme';

export const styles = StyleSheet.create({
  header: {
    backgroundColor: COLORS.primary,
    paddingHorizontal: SPACING.md,
    paddingTop: SPACING.xxl + 70,
    paddingBottom: SPACING.xl,
    borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
  },
  greeting: {
    ...TYPOGRAPHY.h1,
    color: COLORS.textLight,
  },
  subtitle: {
    ...TYPOGRAPHY.bodyLarge,
    color: COLORS.textLight,
    opacity: 0.8,
    marginTop: SPACING.xs,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    ...TYPOGRAPHY.body,
    marginTop: SPACING.sm,
    color: COLORS.textSecondary,
  },
  errorContainer: {
    padding: SPACING.md,
    backgroundColor: COLORS.surface,
    borderRadius: 8,
    borderLeftWidth: 4,
    borderLeftColor: COLORS.danger,
    marginTop: SPACING.md,
  },
  errorText: {
    ...TYPOGRAPHY.body,
    color: COLORS.danger,
  },
  cardTitle: {
    ...TYPOGRAPHY.h2,
    marginBottom: SPACING.md,
    color: COLORS.textPrimary,
  },
  inputDisabled: {
    backgroundColor: COLORS.background,
    opacity: 0.6,
  },
});
