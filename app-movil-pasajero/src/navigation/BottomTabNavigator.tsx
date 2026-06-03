import React from 'react';
import { StyleSheet, TouchableOpacity, Text, View } from 'react-native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';
import SearchStackNavigator from '@/navigation/SearchStackNavigator';
import MyTripsScreen from '@/screens/home/MyTripsScreen';
import ProfileScreen from '@/screens/home/ProfileScreen';
import { useAuth } from '@/context/AuthContext';
import { COLORS, TYPOGRAPHY, SPACING } from '@/theme/theme';

export type BottomTabParamList = {
  Buscar: undefined;
  MisViajes: undefined;
  Perfil: undefined;
};

const Tab = createBottomTabNavigator<BottomTabParamList>();

export default function BottomTabNavigator() {
  const { logout } = useAuth();

  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerStyle: {
          backgroundColor: COLORS.surface,
          shadowColor: 'transparent',
          elevation: 0,
          borderBottomWidth: 1,
          borderBottomColor: COLORS.border,
        },
        headerTintColor: COLORS.primary,
        headerTitleStyle: {
          ...TYPOGRAPHY.h3,
        },
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap = 'search';

          if (route.name === 'Buscar') {
            iconName = focused ? 'search' : 'search-outline';
          } else if (route.name === 'MisViajes') {
            iconName = focused ? 'bus' : 'bus-outline';
          } else if (route.name === 'Perfil') {
            iconName = focused ? 'person' : 'person-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: COLORS.secondary,
        tabBarInactiveTintColor: COLORS.textSecondary,
        tabBarStyle: {
          backgroundColor: COLORS.surface,
          borderTopWidth: 1,
          borderTopColor: COLORS.border,
          paddingBottom: 5,
          paddingTop: 5,
          height: 60,
        },
      })}
    >
      <Tab.Screen 
        name="Buscar" 
        component={SearchStackNavigator} 
        options={{ title: 'Buscar Viajes' }}
      />
      <Tab.Screen 
        name="MisViajes" 
        component={MyTripsScreen} 
        options={{ title: 'Mis Viajes' }}
      />
      <Tab.Screen 
        name="Perfil" 
        component={ProfileScreen} 
        options={{ 
          title: 'Mi Perfil',
          headerRight: () => (
            <TouchableOpacity onPress={logout} style={styles.logoutButton}>
              <Ionicons name="log-out-outline" size={24} color={COLORS.danger} />
            </TouchableOpacity>
          )
        }}
      />
    </Tab.Navigator>
  );
}

const styles = StyleSheet.create({
  logoutButton: {
    marginRight: SPACING.md,
    padding: SPACING.xs,
  },
});
