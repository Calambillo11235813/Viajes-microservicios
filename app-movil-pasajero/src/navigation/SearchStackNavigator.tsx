import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import HomeScreen from '@/screens/home/HomeScreen';
import SearchResultsScreen from '@/screens/home/SearchResultsScreen';
import SeatSelectionScreen from '@/screens/home/SeatSelectionScreen';
import PaymentScreen from '@/screens/home/PaymentScreen';

export type SearchStackParamList = {
  Home: undefined;
  SearchResults: { origen: string; destino: string; fecha: string };
  SeatSelection: { idViaje: string };
  Payment: { reservas: string; montoTotal: number };
};

const Stack = createNativeStackNavigator<SearchStackParamList>();

type SearchStackNavigatorProps = {
  initialScreen?: keyof SearchStackParamList;
  initialParams?: SearchStackParamList[keyof SearchStackParamList];
};

export default function SearchStackNavigator({
  initialScreen = 'Home',
  initialParams,
}: SearchStackNavigatorProps) {
  return (
    <Stack.Navigator
      initialRouteName={initialScreen}
      screenOptions={{ headerShown: false }}
    >
      <Stack.Screen name="Home" component={HomeScreen} />
      <Stack.Screen name="SearchResults" component={SearchResultsScreen} />
      <Stack.Screen
        name="SeatSelection"
        component={SeatSelectionScreen}
        initialParams={
          initialScreen === 'SeatSelection'
            ? (initialParams as SearchStackParamList['SeatSelection'])
            : undefined
        }
      />
      <Stack.Screen
        name="Payment"
        component={PaymentScreen}
        initialParams={
          initialScreen === 'Payment'
            ? (initialParams as SearchStackParamList['Payment'])
            : undefined
        }
      />
    </Stack.Navigator>
  );
}
