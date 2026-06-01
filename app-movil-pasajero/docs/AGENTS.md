# Contexto del Agente: Frontend Móvil (React Native)
**Proyecto:** Agencia de Viajes Interdepartamental (Segundo Parcial SW2)
**Rol:** React Native Senior Developer

## 1. Stack Tecnológico Estricto
* **Framework:** React Native (TypeScript estricto). No uses `any`.
* **Gestor de Estado y Fetching:** Apollo Client (o URQL) para el consumo de la API GraphQL. 
* **Navegación:** React Navigation (Stack y Tab Navigation).
* **Estilos:** StyleSheet nativo o TailwindCSS (NativeWind). Mantén un diseño limpio, moderno y accesible.

## 2. Reglas de Arquitectura y Comunicación
* **Cero REST Tradicional para Reservas:** La comunicación principal con el backend (Microservicio A) se realiza EXCLUSIVAMENTE mediante mutaciones y queries de GraphQL. No crees servicios con `axios` o `fetch` hacia endpoints REST para el flujo de reservas o catálogo.
* **Hardware Nativo:** La aplicación hace uso intensivo de los sensores del dispositivo. Asegúrate de implementar el manejo correcto de permisos (Permisos de Cámara para búsqueda visual/traducción y GPS para geolocalización de terminales/paradas).
* **Estructura de Componentes:** Utiliza Functional Components y Hooks. Separa la lógica de negocio (Custom Hooks) de la capa de presentación (UI).

## 3. Integración con Inteligencia Artificial (Microservicio B)
* Para los Casos de Uso de Deep Learning (Búsqueda por imágenes, Traducción visual), la app debe capturar la imagen/video, optimizar su peso localmente y enviarla al Motor IA alojado en Google Cloud Platform. 
* Maneja estados de carga (Loaders/Skeletons) amigables, ya que la inferencia de IA y la carga de imágenes pesadas puede tomar un par de segundos.

## 4. Estándares de Aplicación
* **Convenciones de Nomenclatura:** Utiliza `PascalCase` para nombres de archivos, componentes (UI) y pantallas. Utiliza `camelCase` para variables, funciones, métodos y Custom Hooks.
* **Manejo de Estados de Red:** Toda petición GraphQL (query/mutation) debe gestionar e ilustrar en la UI sus tres estados: `loading` (esqueletos de carga/spinners), `error` (pantallas de fallback amigables) y `success`.
* **Importaciones Limpias:** Configura y utiliza rutas absolutas (alias) como `@/components`, `@/screens` y `@/hooks` para evitar el anidamiento profundo de importaciones (`../../../`).
* **Rendimiento:** Implementa `FlatList` con optimización de memoria (ej. `initialNumToRender`) para catálogos largos de viajes y usa `useMemo`/`useCallback` donde sea estrictamente necesario para evitar re-renderizados.
* **Comentarios:** El código debe estar debidamente documentado. Utiliza JSDoc para documentar componentes, props y hooks complejos. Añade comentarios en línea para explicar lógica de negocio no trivial o decisiones arquitectónicas importantes.