# RBAC Implementation Plan

Este plan detalla los cambios solicitados para implementar el Control de Acceso Basado en Roles (RBAC) tanto en la base de datos como en la interfaz de usuario de Angular.

## Proposed Changes

### Backend (core-transaccional)

#### [MODIFY] [DatabaseSeeder.java](file:///c:/Users/usuario/OneDrive%20-%20Universidad%20Privada%20del%20Valle/Gabriel/SW2/Segundo%20Parcial/Proyecto_Clonar/Viajes-microservicios/core-transaccional/src/main/java/com/agencia/viajes/transaccional/config/DatabaseSeeder.java)
Se actualizará el método `insertarUsuarios` para insertar de forma estática los 3 usuarios requeridos con sus respectivas contraseñas encriptadas y roles asociados:
1. **Administrador** (`admin@viajes.com` / `admin123` / Rol 1)
2. **Cliente** (`cliente@viajes.com` / `cliente123` / Rol 2)
3. **Gerente** (`gerente@viajes.com` / `gerente123` / Rol 3)

### Frontend (frontend-web-admin)

#### [MODIFY] [inventory-dashboard.ts](file:///c:/Users/usuario/OneDrive%20-%20Universidad%20Privada%20del%20Valle/Gabriel/SW2/Segundo%20Parcial/Proyecto_Clonar/Viajes-microservicios/frontend-web-admin/src/app/features/inventory/inventory-dashboard/inventory-dashboard.ts)
- Se inyectará el servicio de autenticación para exponer el rol del usuario logueado en la clase del componente (ej. `rolId = this.authService.getCurrentUser()?.idRol`).

#### [MODIFY] [inventory-dashboard.html](file:///c:/Users/usuario/OneDrive%20-%20Universidad%20Privada%20del%20Valle/Gabriel/SW2/Segundo%20Parcial/Proyecto_Clonar/Viajes-microservicios/frontend-web-admin/src/app/features/inventory/inventory-dashboard/inventory-dashboard.html)
Se modificarán las opciones del menú lateral izquierdo (Sidebar) utilizando directivas estructuradas de Angular (`@if` o `*ngIf`) para mostrar u ocultar pestañas según la regla de negocio:
- **Cliente (Rol 2):** Buscar Viajes, Itinerarios, Reservas.
- **Gerente (Rol 3):** Solo Reportes (Dashboard BI).
- **Administrador (Rol 1):** Gestión de Rutas, Programación de Viajes, Flotas y Personal.

#### [MODIFY] [app.routes.ts](file:///c:/Users/usuario/OneDrive%20-%20Universidad%20Privada%20del%20Valle/Gabriel/SW2/Segundo%20Parcial/Proyecto_Clonar/Viajes-microservicios/frontend-web-admin/src/app/app.routes.ts)
- Se consolidarán las rutas de `admin`, `gerencia` y `cliente` para que utilicen el mismo Layout (`InventoryDashboard`), de modo que el sidebar se aplique correctamente a todos los roles y redirija a los componentes correspondientes.

## User Review Required

> [!IMPORTANT]
> - ¿Estás de acuerdo con centralizar todas las vistas (Admin, Gerente, Cliente) dentro del mismo componente `InventoryDashboard` (que actúa como el Layout principal con el Sidebar) para manejar la lógica de los roles en un solo lugar?
> - ¿Deseas que renombre el componente de `InventoryDashboard` a algo más genérico como `MainLayout` o mantenemos el nombre actual?
