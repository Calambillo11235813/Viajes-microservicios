/**
 * Script que lee el archivo .env y genera los archivos environment.ts
 * Ejecutar con: node set-env.js
 *
 * Esto permite que la IP del Core Transaccional se configure desde .env
 * sin hardcodear valores en el código fuente.
 */

const fs = require('fs');
const path = require('path');

// Leer .env
const envPath = path.resolve(__dirname, '.env');

if (!fs.existsSync(envPath)) {
  console.error('❌ No se encontró el archivo .env');
  console.error('   Copia .env.example a .env y ajusta los valores.');
  process.exit(1);
}

const envContent = fs.readFileSync(envPath, 'utf-8');
const envVars = {};

envContent.split('\n').forEach(line => {
  const trimmed = line.trim();
  if (trimmed && !trimmed.startsWith('#')) {
    const [key, ...valueParts] = trimmed.split('=');
    envVars[key.trim()] = valueParts.join('=').trim();
  }
});

const coreIp = envVars.CORE_TRANSACCIONAL_IP || 'localhost';
const corePort = envVars.CORE_TRANSACCIONAL_PORT || '9090';

// Template para environment.ts
const envFileContent = `/**
 * Configuración centralizada del frontend-web-admin.
 *
 * ⚠️  Este archivo es AUTO-GENERADO por set-env.js desde el .env
 *     NO editar manualmente. Cambia los valores en .env y ejecuta:
 *     node set-env.js
 */

const CORE_IP = '${coreIp}';
const CORE_PORT = '${corePort}';

export const environment = {
  production: false,

  // URL base del microservicio Core Transaccional
  coreBaseUrl: \`http://\${CORE_IP}:\${CORE_PORT}\`,

  // Endpoint GraphQL (usado por GraphqlService y AuthService)
  graphqlUrl: \`http://\${CORE_IP}:\${CORE_PORT}/graphql\`,
};
`;

// Template para environment.prod.ts
const envProdFileContent = `/**
 * Configuración de producción.
 *
 * ⚠️  Este archivo es AUTO-GENERADO por set-env.js desde el .env
 *     NO editar manualmente. Cambia los valores en .env y ejecuta:
 *     node set-env.js
 */

const CORE_IP = '${coreIp}';
const CORE_PORT = '${corePort}';

export const environment = {
  production: true,

  // URL base del microservicio Core Transaccional
  coreBaseUrl: \`http://\${CORE_IP}:\${CORE_PORT}\`,

  // Endpoint GraphQL (usado por GraphqlService y AuthService)
  graphqlUrl: \`http://\${CORE_IP}:\${CORE_PORT}/graphql\`,
};
`;

// Escribir archivos
const envDir = path.resolve(__dirname, 'src', 'environments');
if (!fs.existsSync(envDir)) {
  fs.mkdirSync(envDir, { recursive: true });
}

fs.writeFileSync(path.join(envDir, 'environment.ts'), envFileContent);
fs.writeFileSync(path.join(envDir, 'environment.prod.ts'), envProdFileContent);

console.log('✅ Archivos de entorno generados:');
console.log(`   CORE_IP:   ${coreIp}`);
console.log(`   CORE_PORT: ${corePort}`);
console.log(`   → src/environments/environment.ts`);
console.log(`   → src/environments/environment.prod.ts`);
