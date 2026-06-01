Casos de Uso Asignados a esta Carpeta

Hacer el login es lo basico

* **CU-01:** Buscar rutas y horarios.
* **CU-02:** Seleccionar asiento y reservar.
* **CU-03:** Realizar pago (QR, billeteras).
* **CU-04:** Modificar o cancelar reserva.
* **CU-06:** Buscar destinos mediante imágenes (IA).
* **CU-08:** Traducir texto mediante video (IA).
* **CU-13:** Notificaciones push de estado del viaje.
* **CU-14:** Alerta de proximidad de parada (GPS).


### CU-13: Notificaciones push de estado del viaje
**Descripción:** Enviar alertas automáticas y en tiempo real al usuario sobre cambios importantes en su itinerario o estado del bus.
**Flujo Principal:**
1. El sistema detecta un cambio de estado (ej. bus retrasado o llegada a terminal).
2. El servicio de notificaciones genera el mensaje personalizado.
3. El sistema envía la notificación push al dispositivo móvil del pasajero.
4. El usuario recibe la alerta en su pantalla.

### CU-14: Alerta de proximidad de parada
**Descripción:** Notificar al pasajero mediante una señal sonora o vibración cuando el bus se aproxima a su punto de descenso.
**Flujo Principal:**
1. El sistema monitorea la ubicación del bus en tiempo real mediante GPS.
2. El sistema calcula la distancia restante al punto de parada del pasajero.
3. Al alcanzar un umbral de distancia mínimo, se activa la alerta.
4. El dispositivo del usuario emite la señal de alerta.