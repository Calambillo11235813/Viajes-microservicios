import { StyleSheet } from 'react-native';
import { COLORS, SPACING, TYPOGRAPHY } from '@/theme/theme';

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
  errorText: {
    ...TYPOGRAPHY.body,
    color: COLORS.danger,
    marginBottom: SPACING.md,
  },
  scrollContent: {
    padding: SPACING.md,
    paddingBottom: SPACING.xxl,
  },
  legendContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: SPACING.lg,
    backgroundColor: COLORS.surface,
    padding: SPACING.sm,
    borderRadius: 8,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  legendColor: {
    width: 16,
    height: 16,
    borderRadius: 4,
    marginRight: 8,
  },
  legendText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.textSecondary,
  },
  busContainer: {
    backgroundColor: '#E0E5EC',
    borderRadius: 24,
    padding: SPACING.md,
    alignItems: 'center',
    marginBottom: SPACING.xl,
  },
  busFront: {
    ...TYPOGRAPHY.caption,
    color: COLORS.textSecondary,
    fontWeight: 'bold',
    marginBottom: SPACING.lg,
    borderBottomWidth: 2,
    borderBottomColor: COLORS.border,
    paddingBottom: SPACING.sm,
    width: '100%',
    textAlign: 'center',
  },
  gridContainer: {
    alignItems: 'center',
  },
  rowWrapper: {
    justifyContent: 'center',
    marginBottom: SPACING.md,
    gap: SPACING.sm,
  },
  seat: {
    width: 48,
    height: 48,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    borderWidth: 1,
  },
  seatAvailable: {
    backgroundColor: COLORS.surface,
    borderColor: COLORS.primary,
  },
  seatOccupied: {
    backgroundColor: COLORS.border,
    borderColor: COLORS.textSecondary,
  },
  seatSelected: {
    backgroundColor: COLORS.primary,
    borderColor: COLORS.primary,
  },
  seatText: {
    ...TYPOGRAPHY.body,
    fontWeight: 'bold',
  },
  seatTextAvailable: {
    color: COLORS.primary,
  },
  seatTextOccupied: {
    color: COLORS.textSecondary,
  },
  seatTextSelected: {
    color: COLORS.textLight,
  },
  sectionTitle: {
    ...TYPOGRAPHY.h3,
    color: COLORS.textPrimary,
    marginBottom: SPACING.md,
  },
  pasajeroCard: {
    backgroundColor: COLORS.surface,
    padding: SPACING.lg,
    borderRadius: 16,
    marginBottom: SPACING.md,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  pasajeroCardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: SPACING.md,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
    paddingBottom: SPACING.sm,
  },
  pasajeroCardTitle: {
    ...TYPOGRAPHY.h3,
    color: COLORS.primary,
  },
  removeText: {
    ...TYPOGRAPHY.caption,
    color: COLORS.danger,
    fontWeight: '600',
  },
  typeSelectorContainer: {
    flexDirection: 'row',
    gap: SPACING.md,
  },
  typeOption: {
    flex: 1,
    paddingVertical: SPACING.sm,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 8,
  },
  typeOptionSelected: {
    backgroundColor: COLORS.secondary,
    borderColor: COLORS.secondary,
  },
  typeOptionText: {
    ...TYPOGRAPHY.body,
    color: COLORS.textSecondary,
  },
  typeOptionTextSelected: {
    color: COLORS.textLight,
    fontWeight: 'bold',
  },
});
