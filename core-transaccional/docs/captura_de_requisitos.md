# Captura de Requisitos Funcionales - Core Transaccional (Microservicio A)

## 📌 Propósito del Documento
Este archivo contiene exclusivamente los Casos de Uso que deben ser implementados en el **Microservicio A (Core Transaccional)**. 
* **Responsabilidad:** Este componente actúa como el motor principal del negocio, gestionando operaciones CRUD, validaciones transaccionales estrictas (ACID) en PostgreSQL y exponiendo la API mediante GraphQL.
* **Límites:** El agente de IA **NO** debe implementar aquí lógicas de generación de PDF, Inteligencia Artificial, ni Blockchain (estas pertenecen a otros microservicios).

## 👥 Actores del Sistema (Core)
* **Pasajero (Cliente):** Usuario final que busca, reserva, paga y gestiona sus viajes.
* **Administrador:** Personal que gestiona el catálogo de rutas, flotas y usuarios.
* **Gerente:** Usuario que visualiza estadísticas y finanzas.

## 📋 Casos de Uso a Implementar en este Microservicio

A continuación se detallan los flujos exactos que el agente debe mapear a Controladores (GraphQL Resolvers) y Servicios (Spring Boot):

### 🛒 Flujo del Viajero (Cliente)
* **CU-01 Buscar rutas y horarios disponibles:** Consultar las rutas de transporte y horarios disponibles según origen, destino y fecha.
* **CU-02 Seleccionar asiento y reservar:** Desplegar el mapa gráfico de asientos, bloquear el asiento temporalmente en el inventario y confirmar la reserva provisional.
* **CU-03 Realizar pago:** Integrar la pasarela de pagos (QR/Transferencia) y verificar la acreditación. *(Nota para el Agente: Al confirmar el pago, se debe emitir un evento a Redis para que otro microservicio genere el boleto).*
* **CU-05 Gestionar perfil de usuario:** CRUD para que el cliente modifique sus datos personales de contacto en el sistema.
* **CU-06 Cancelar reserva:** Validar plazos permitidos, anular la reserva confirmada y liberar el asiento en la base de datos.
* **CU-07 Consultar historial de viajes:** Recuperar y visualizar la lista de viajes realizados y reservas pasadas asociadas al usuario logueado.

### ⚙️ Flujo Administrativo y Operativo
* **CU-08 Gestionar Usuarios:** Dar de alta, editar permisos o bloquear cuentas de usuarios mediante un panel con privilegios elevados.
* **CU-09 Gestionar Rutas y Horarios:** Administrar el catálogo definiendo orígenes, destinos, precios y verificando que no existan conflictos de horario.
* **CU-10 Generar Reportes de Ventas:** Filtrar transacciones exitosas en un rango de fechas y calcular los totales financieros para la gerencia.
* **CU-11 Gestionar Flota de Buses:** Registrar, editar o dar de baja unidades (placa, capacidad, modelo) y asignarlas a rutas específicas.