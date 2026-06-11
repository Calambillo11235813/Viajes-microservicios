/**
 * Configuración de producción.
 *
 * Cambia CORE_IP a la IP/dominio de tu EC2 o load balancer en producción.
 */

// ── Core Transaccional (Spring Boot / GraphQL) ──────────────────
const CORE_IP = '18.218.178.228';
const CORE_PORT = '9090';

export const environment = {
  production: true,

  // URL base del microservicio Core Transaccional
  coreBaseUrl: `http://${CORE_IP}:${CORE_PORT}`,

  // Endpoint GraphQL (usado por GraphqlService y AuthService)
  graphqlUrl: `http://${CORE_IP}:${CORE_PORT}/graphql`,
};
