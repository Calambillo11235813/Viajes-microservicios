import { ApolloClient, InMemoryCache, HttpLink } from '@apollo/client';
import { CONFIG } from '../utils/config';

export const apolloClient = new ApolloClient({
  // Asegúrate de cambiar la IP local a la IP de tu computadora (por ejemplo 192.168.1.100) 
  // si pruebas desde un dispositivo físico Android/iOS, ya que localhost no funcionará.
  link: new HttpLink({
    uri: CONFIG.GRAPHQL_URL,
  }),
  cache: new InMemoryCache(),
});