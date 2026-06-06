import { StyleSheet } from 'react-native';
import { COLORS, SPACING, TYPOGRAPHY } from '@/theme/theme';

export const styles = StyleSheet.create({
  headerBackground: {
    backgroundColor: COLORS.primary,
    height: 150,
    justifyContent: 'flex-end',
    alignItems: 'center',
    paddingBottom: 20,
  },
  avatarLarge: {
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: COLORS.accent,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: -70,
    borderWidth: 4,
    borderColor: COLORS.background,
  },
  avatarLargeText: {
    fontSize: 40,
    fontWeight: 'bold',
    color: COLORS.textLight,
  },
  content: {
    padding: SPACING.xl,
    paddingTop: 80,
    alignItems: 'center',
  },
  name: {
    ...TYPOGRAPHY.h1,
    marginBottom: 4,
  },
  role: {
    ...TYPOGRAPHY.caption,
    marginBottom: SPACING.xl,
  },
  infoCard: {
    width: '100%',
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: SPACING.lg,
    borderWidth: 1,
    borderColor: COLORS.border,
    marginBottom: SPACING.xl,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: SPACING.sm,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
  },
  infoLabel: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
  },
  infoValue: {
    ...TYPOGRAPHY.body,
    color: COLORS.textPrimary,
    fontWeight: '600',
  },
  logoutButton: {
    paddingVertical: SPACING.md,
    paddingHorizontal: SPACING.xl,
    backgroundColor: COLORS.danger + '20',
    borderRadius: 12,
  },
  logoutButtonText: {
    ...TYPOGRAPHY.body,
    color: COLORS.danger,
    fontWeight: 'bold',
  },
});
