import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { useAuth } from '@/context/AuthContext';
import { globalStyles } from '@/theme/theme';
import { styles } from './styles/ProfileScreen.styles';

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
