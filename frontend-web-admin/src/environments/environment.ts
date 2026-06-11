/**
 * Configuración centralizada del frontend-web-admin.
 *
 * Cambia SOLO la IP/puerto aquí para apuntar a otro entorno
 * (local, EC2 producción, etc.). Todos los servicios leen de este archivo.
 */

// ── Core Transaccional (Spring Boot / GraphQL) ──────────────────
const CORE_IP = '18.218.178.228';
const CORE_PORT = '9090';

export const environment = {
  production: false,

  // URL base del microservicio Core Transaccional
  coreBaseUrl: `http://${CORE_IP}:${CORE_PORT}`,

  // Endpoint GraphQL (usado por GraphqlService y AuthService)
  graphqlUrl: `http://${CORE_IP}:${CORE_PORT}/graphql`,
};
