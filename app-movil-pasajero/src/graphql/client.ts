import { ApolloClient, InMemoryCache, HttpLink } from '@apollo/client';

export const apolloClient = new ApolloClient({
  // Asegúrate de cambiar la IP local a la IP de tu computadora (por ejemplo 192.168.1.100) 
  // si pruebas desde un dispositivo físico Android/iOS, ya que localhost no funcionará.
  link: new HttpLink({
    uri: 'http://192.168.0.5:8080/graphql',
  }),
  cache: new InMemoryCache(),
});