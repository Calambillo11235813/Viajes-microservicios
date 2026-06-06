import { StyleSheet } from 'react-native';
import { COLORS, SPACING, TYPOGRAPHY, SCREEN_METRICS } from '@/theme/theme';

export const styles = StyleSheet.create({
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
