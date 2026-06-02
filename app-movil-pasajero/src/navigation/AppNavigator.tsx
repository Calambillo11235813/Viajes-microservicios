import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import LoginScreen from '@/screens/auth/LoginScreen';
import HomeScreen from '@/screens/home/HomeScreen';
import SearchResultsScreen from '@/screens/home/SearchResultsScreen';
import SeatSelectionScreen from '@/screens/home/SeatSelectionScreen';
import PaymentScreen from '@/screens/home/PaymentScreen';

export type RootStackParamList = {
  Login: undefined;
  Home: undefined;
  SearchResults: { origen: string; destino: string; fecha: string };
  SeatSelection: { idViaje: string };
  Payment: { reservas: string; montoTotal: number };
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator 
        initialRouteName="Login"
        screenOptions={{
          headerShown: false, // Ocultar el header por defecto para un diseño más limpio
        }}
      >
        <Stack.Screen name="Login" component={LoginScreen} />
        <Stack.Screen name="Home" component={HomeScreen} />
        <Stack.Screen name="SearchResults" component={SearchResultsScreen} />
        <Stack.Screen name="SeatSelection" component={SeatSelectionScreen} />
        <Stack.Screen name="Payment" component={PaymentScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}