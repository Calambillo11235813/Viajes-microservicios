# Captura de Requisitos - Frontend Administrativo

Este documento especifica los requisitos funcionales, no funcionales, actores del sistema y catálogo de casos de uso para el **Frontend Web Administrativo (Angular)** de la Agencia de Viajes Interdepartamental (Grupo 18 - SW2).

---

## 👥 1. Actores del Sistema (Panel Administrativo)

### 1.1. Administrador
* **Descripción:** Es el usuario operativo responsable de controlar la logística cotidiana de la agencia de viajes.
* **Objetivo en el Sistema:** Mantener actualizado el catálogo de servicios, flotas, horarios y resolver incidencias de rutas para asegurar la continuidad del servicio transaccional.

### 1.2. Gerente
* **Descripción:** Es el usuario estratégico de alto nivel que toma decisiones comerciales y logísticas basadas en datos.
* **Objetivo en el Sistema:** Monitorear el rendimiento financiero y operativo de la plataforma a través de indicadores clave de rendimiento (KPIs) y tableros interactivos de Business Intelligence (BI).

---

## 📋 2. Requisitos Funcionales (RF)

### RF01 - Autenticación y Autorización
El sistema debe permitir el inicio de sesión seguro y gestionar el acceso restringido y basado en roles jerárquicos (`Administrador` y `Gerente`).

### RF02 - Generar Reportes y KPIs en Tiempo Real
El sistema debe procesar y renderizar de forma gráfica y tabular los datos analíticos recolectados por el API Gateway.
* **RF02.1. KPI de Ocupación:** Tasa de ocupación promedio de asientos por ruta interdepartamental.
* **RF02.2. KPI de Ingresos:** Ingreso total mensual desglosado por empresa.
* **RF02.3. KPI de Demanda:** Frecuencia de horarios y días con mayor demanda y saturación en la compra de boletos.

### RF03 - Gestión del Catálogo de Viajes (CRUD)
El sistema debe permitir al Administrador registrar, modificar, listar y dar de baja lógica los elementos que componen la oferta de transporte: rutas interdepartamentales, terminales de origen/destino y precios.

### RF04 - Gestión de Flotas y Personal (CRUD)
El sistema debe permitir al Administrador administrar el inventario de buses (flotas, capacidades, amenidades) y la asignación de choferes asignados a cada salida programada.

### RF05 - Control de Incidencias Operativas
El sistema debe permitir registrar retrasos, averías o cambios de fuerza mayor en los viajes programados, notificando de manera indirecta a los pasajeros afectados.

---

## 🚀 3. Catálogo de Casos de Uso (CU)

### 3.1. Casos de Uso del Administrador (Gestión Operativa)
* **CU-A01: Autenticar Usuario Administrativo:** Permitir el ingreso al panel utilizando credenciales institucionales y validar el rol de Administrador.
* **CU-A02: Gestionar Rutas Interdepartamentales:** Crear, modificar o desactivar rutas de viaje (ej. Santa Cruz - La Paz), definiendo distancias y paradas permitidas.
* **CU-A03: Gestionar Flotas de Buses:** Registrar nuevos buses en el sistema, ingresando datos como número de placa, capacidad total de asientos, empresa dueña y tipo de servicio (Leito, Semicama).
* **CU-A04: Programar Horarios y Viajes:** Crear salidas específicas asignando una ruta, una flota de bus, una fecha y una hora de salida exacta.
* **CU-A05: Modificar Tarifas y Precios:** Ajustar el precio base de los boletos por asiento según factores de temporada o tipo de bus.
* **CU-A06: Asignar Choferes y Personal:** Asociar choferes habilitados a un viaje programado específico para mantener el control logístico.
* **CU-A07: Registrar Incidencias de Viajes:** Cambiar el estado de un viaje (ej. de 'PROGRAMADO' a 'RETRASADO' o 'CANCELADO') e ingresar la justificación operativa.

### 3.2. Casos de Uso del Gerente (Análisis Estratégico)
* **CU-G01: Autenticar Usuario Gerencial:** Permitir el ingreso al panel validando el rol de Gerente para habilitar las vistas analíticas.
* **CU-G02: Visualizar Dashboard de Inteligencia de Negocios (BI):** Desplegar componentes gráficos interactivos (gráficos de barras, líneas y mapas de calor) con los datos consolidados del sistema.
* **CU-G03: Consultar Tasa de Ocupación por Ruta:** Filtrar y analizar qué rutas viajan con mayor porcentaje de asientos vendidos para optimizar la logística.
* **CU-G04: Auditar Ingresos por Empresa de Transporte:** Visualizar reportes financieros tabulares que muestren cuánto dinero ha generado cada empresa en el mes en curso.
* **CU-G05: Analizar Tendencias de Demanda:** Consultar qué franjas horarias son las más codiciadas por los clientes para sugerir la apertura de nuevos horarios.
* **CU-G06: Exportar Reportes de Desempeño:** Descargar la información analítica visualizada en formatos limpios como PDF o archivos CSV/Excel para juntas corporativas.

---

## 🛠️ 4. Requisitos No Funcionales (RNF)

* **RNF01 - Framework y Lenguaje:** El desarrollo se realizará estrictamente en Angular (con TypeScript) bajo modo estricto.
* **RNF02 - Integración Backend:** El consumo de datos se realizará preferentemente a través de GraphQL o en su defecto API REST comunicándose con el API Gateway.
* **RNF03 - Diseño y Usabilidad:** La interfaz debe ser limpia, moderna y responsiva, utilizando Tailwind CSS para garantizar una visualización fluida tanto en laptops como en tablets operativas.
* **RNF04 - Rendimiento:** Las vistas administrativas deben cargar ágilmente, optimizando el renderizado de tablas grandes mediante paginación del lado del servidor y "lazy loading" a nivel de enrutamiento modular de Angular.
* **RNF05 - Despliegue (Multinube):** El código debe estar preparado para su construcción (build) e integración en contenedores Docker para el pipeline CI/CD en un entorno multinube (AWS/Azure).
