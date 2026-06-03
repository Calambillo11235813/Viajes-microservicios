import { StatusBar } from 'expo-status-bar';
import { StyleSheet } from 'react-native';
import { ApolloProvider } from '@apollo/client/react';
import { apolloClient } from '@/graphql/client';
import AppNavigator from '@/navigation/AppNavigator';
import { AuthProvider } from '@/context/AuthContext';
import { View } from 'react-native';

export default function App() {
  return (
    <View style={styles.container}>
      <ApolloProvider client={apolloClient}>
        <AuthProvider>
          <AppNavigator />
          <StatusBar style="auto" />
        </AuthProvider>
      </ApolloProvider>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
