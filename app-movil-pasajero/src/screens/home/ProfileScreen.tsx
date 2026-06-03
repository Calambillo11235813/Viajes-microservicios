import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useAuth } from '@/context/AuthContext';
import { COLORS, SPACING, TYPOGRAPHY, globalStyles } from '@/theme/theme';

export default function ProfileScreen() {
  const { user, logout } = useAuth();

  return (
    <View style={globalStyles.safeAreaContainer}>
      <View style={styles.headerBackground}>
        <View style={styles.avatarLarge}>
          <Text style={styles.avatarLargeText}>{user?.nombreCompleto?.charAt(0) || 'U'}</Text>
        </View>
      </View>

      <View style={styles.content}>
        <Text style={styles.name}>{user?.nombreCompleto || 'Usuario'}</Text>
        <Text style={styles.role}>Rol ID: {user?.idRol || 'N/A'}</Text>

        <View style={styles.infoCard}>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Email</Text>
            <Text style={styles.infoValue}>{user?.email}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>ID Usuario</Text>
            <Text style={styles.infoValue}>{user?.idUsuario}</Text>
          </View>
        </View>

        <TouchableOpacity style={styles.logoutButton} onPress={logout}>
          <Text style={styles.logoutButtonText}>Cerrar Sesión</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
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
