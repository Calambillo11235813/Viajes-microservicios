/**
 * Configuración centralizada del frontend-web-admin.
 *
 * ⚠️  Este archivo es AUTO-GENERADO por set-env.js desde el .env
 *     NO editar manualmente. Cambia los valores en .env y ejecuta:
 *     node set-env.js
 */

const CORE_IP = '18.218.178.228';
const CORE_PORT = '9090';

export const environment = {
  production: false,

  // URL base del microservicio Core Transaccional
  coreBaseUrl: `http://${CORE_IP}:${CORE_PORT}`,

  // Endpoint GraphQL (usado por GraphqlService y AuthService)
  graphqlUrl: `http://${CORE_IP}:${CORE_PORT}/graphql`,
};
