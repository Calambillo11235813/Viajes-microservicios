# Solución: Análisis de Imagen con Motor IA — 05 de Junio de 2026

Documento de referencia con el diagnóstico completo, la causa raíz y los pasos para reproducir y verificar la solución del error `[TypeError: Network request failed]` al enviar imágenes desde la app móvil al microservicio **Motor IA** (Django).

**Relacionado con:** [errores-2026-06-05.md](./errores-2026-06-05.md)

---

## 1. Contexto

| Componente | Tecnología | URL / Puerto |
|---|---|---|
| App móvil | React Native + Expo | Cliente en dispositivo físico |
| Microservicio A (Transaccional) | Spring Boot + GraphQL | `http://192.168.0.5:9090/graphql` |
| Microservicio B (Motor IA) | Django + TensorFlow | `http://192.168.0.5:8000/api/predict/` |

**Flujo afectado (CU-06 — Buscar por imagen):**

1. El usuario selecciona o captura una foto en `BuscarImagenScreen.tsx`.
2. La app arma un `FormData` con el campo `imagen`.
3. Se envía un `POST` multipart a Django.
4. Django ejecuta el modelo MobileNetV2 y responde con el destino detectado.

**Síntoma reportado:**

```
ERROR  Error al analizar imagen con IA: [TypeError: Network request failed]
```

---

## 2. Arquitectura de la conexión

```
┌─────────────────────┐         Wi-Fi (192.168.0.x)         ┌──────────────────────────┐
│  Celular (Expo Go)  │ ──────────────────────────────────► │  PC de desarrollo        │
│                     │                                     │  IP: 192.168.0.5         │
│  fetch() POST       │                                     │                          │
│  FormData + imagen  │                                     │  ┌────────────────────┐  │
└─────────────────────┘                                     │  │ Spring Boot :9090  │  │  ✅ Login OK
                                                              │  └────────────────────┘  │
                                                              │  ┌────────────────────┐  │
                                                              │  │ Django :8000       │  │  ❌ Bloqueado
                                                              │  │ env\Scripts\       │  │     por firewall
                                                              │  │ python.exe         │  │
                                                              │  └────────────────────┘  │
                                                              └──────────────────────────┘
```

---

## 3. Hipótesis evaluadas

| Hipótesis | Resultado | Motivo |
|---|---|---|
| IP incorrecta en `config.ts` | Descartada parcialmente | El login GraphQL a la misma IP sí funciona |
| `ALLOWED_HOSTS` vacío en Django | Corregida antes, no era la causa final | Se cambió a `['*']` en `settings.py` |
| `Content-Type` manual en `fetch` | Corregida antes, no era la causa final | Se eliminó la cabecera forzada |
| Cleartext HTTP bloqueado por Android | **Descartada** | GraphQL también usa `http://` al mismo host y funciona |
| Firewall de Windows bloqueando puerto 8000 | **Causa raíz confirmada** | Ver sección 4 |

---

## 4. Diagnóstico (causa raíz)

### 4.1 Evidencia en el servidor Django

La terminal de Django mostraba el servidor activo:

```
Starting development server at http://0.0.0.0:8000/
```

Pero **nunca aparecía** una línea como:

```
"POST /api/predict/ HTTP/1.1" 200
```

**Conclusión:** la petición del celular no llegaba al proceso de Python.

### 4.2 Prueba de conectividad al puerto 8000

Desde PowerShell en la misma PC:

```powershell
Test-NetConnection -ComputerName 192.168.0.5 -Port 8000
```

| Resultado antes de la solución | Interpretación |
|---|---|
| Timeout ~50 segundos | Paquete **descartado por firewall** |
| `TcpTestSucceeded : False` | Puerto inaccesible desde la red |

> Un puerto **cerrado** (sin servicio escuchando) rechaza al instante. Un **timeout largo** indica que el firewall descarta el tráfico sin responder.

### 4.3 Estado del Firewall de Windows

```powershell
Get-NetFirewallProfile -PolicyStore ActiveStore | Format-Table Name,Enabled
```

| Perfil | Estado |
|---|---|
| Domain | Enabled |
| Private | Enabled |
| Public | Enabled |

La red Wi-Fi activa (`FAMILIA RODRIGUEZ 2`) estaba clasificada como perfil **Public**.

### 4.4 Reglas existentes para Python (insuficientes)

Existían reglas de entrada para `python.exe`, pero solo apuntaban al **Python global**:

```
C:\users\dell\appdata\local\programs\python\python311\python.exe
```

Django se ejecutaba desde el **entorno virtual**, un ejecutable distinto:

```
D:\Viajes-microservicios\env\Scripts\python.exe
```

**Por eso Spring Boot (Java, puerto 9090) funcionaba y Django (Python del venv, puerto 8000) no:** Java ya tenía permiso de firewall; el Python del venv no.

---

## 5. Solución aplicada

### 5.1 Regla de Firewall por puerto (solución principal)

Se creó una regla **por puerto TCP 8000**, independiente del ejecutable de Python usado:

```powershell
# Ejecutar PowerShell como Administrador
New-NetFirewallRule `
  -DisplayName "Motor IA Django 8000" `
  -Direction Inbound `
  -Protocol TCP `
  -LocalPort 8000 `
  -Action Allow `
  -Profile Any
```

**Verificación:**

```powershell
Test-NetConnection -ComputerName 192.168.0.5 -Port 8000
# TcpTestSucceeded : True
```

### 5.2 Levantar Django escuchando en todas las interfaces

El servidor debe arrancar con `0.0.0.0`, no solo `127.0.0.1`:

```powershell
cd D:\Viajes-microservicios\motor_ia
..\env\Scripts\activate
python manage.py runserver 0.0.0.0:8000
```

### 5.3 Configuración Django para desarrollo local

En `motor_ia/motor_ia/settings.py`:

```python
ALLOWED_HOSTS = ['*']
```

### 5.4 Configuración de la app móvil

En `app-movil-pasajero/src/utils/config.ts`:

```typescript
const LOCAL_IP = '192.168.0.5'; // IP LAN de la PC (ipconfig)

export const CONFIG = {
  GRAPHQL_URL: `http://${LOCAL_IP}:9090/graphql`,
  AI_API_URL: `http://${LOCAL_IP}:8000/api/predict/`,
};
```

> Si cambias de red Wi-Fi, vuelve a ejecutar `ipconfig` y actualiza `LOCAL_IP`.

---

## 6. Correcciones adicionales en el frontend

Una vez resuelta la red, aparecieron dos bugs lógicos en `BuscarImagenScreen.tsx` que habrían impedido el flujo correcto.

### 6.1 Regex del tipo MIME de la imagen

**Antes (incorrecto):**

```typescript
const match = /\\.(\\w+)$/.exec(filename);
const type = match ? `image/${match[1]}` : `image`;
```

El doble escape hacía que nunca coincidiera con extensiones como `.jpg`. El `type` quedaba como `'image'` (sin `/`), y Django rechazaba con HTTP 400 al validar `content_type.startswith('image/')`.

**Después (correcto):**

```typescript
const match = /\.(\w+)$/.exec(filename);
const type = match ? `image/${match[1]}` : 'image/jpeg';
```

### 6.2 Contrato de respuesta con Django

**Antes:** el frontend validaba `json.exito`, campo que **no existe** en la API.

**Respuesta real de Django** (`api_destinos/views.py`):

| Caso | HTTP | JSON |
|---|---|---|
| Destino reconocido | 200 | `{ "reconocido": true, "destino": "Cristo_ConCordia", "confianza": 0.95 }` |
| Sin coincidencias | 200 | `{ "reconocido": false, "mensaje": "...", "confianza_maxima": 0.4 }` |
| Error de validación | 400 | `{ "error": "..." }` |
| Error interno | 500 | `{ "error": "..." }` |

**Después:** se usa `json.reconocido` y se muestra alerta amigable cuando no hay coincidencias.

### 6.3 Cabecera `Content-Type` en multipart

**No** establecer manualmente `'Content-Type': 'multipart/form-data'` en el `fetch`. React Native debe generar el `boundary` automáticamente. Solo se envía:

```typescript
headers: {
  'Accept': 'application/json',
},
```

---

## 7. Checklist de verificación

Usar esta lista cada vez que se pruebe el flujo desde un dispositivo físico:

- [ ] **IP correcta:** `ipconfig` → actualizar `LOCAL_IP` en `config.ts`
- [ ] **Django corriendo:** `python manage.py runserver 0.0.0.0:8000`
- [ ] **Puerto accesible:** `Test-NetConnection 192.168.0.5 -Port 8000` → `TcpTestSucceeded : True`
- [ ] **Regla firewall activa:** existe `"Motor IA Django 8000"` con `Action = Allow`
- [ ] **Celular y PC en la misma red Wi-Fi**
- [ ] **Expo recargado** tras cambios en JS (`r` en la terminal o shake → Reload)
- [ ] **Log Django:** al analizar imagen debe aparecer `"POST /api/predict/ HTTP/1.1" 200`

### Prueba rápida desde la PC (sin celular)

```powershell
# Crear un archivo de prueba y enviarlo con curl (si está instalado)
curl -X POST http://192.168.0.5:8000/api/predict/ -F "imagen=@ruta\a\imagen.jpg"
```

---

## 8. Troubleshooting futuro

| Síntoma | Qué revisar |
|---|---|
| `Network request failed` | Firewall, IP, puerto 8000, `runserver 0.0.0.0:8000` |
| Django no muestra la petición | Casi siempre firewall o IP incorrecta |
| HTTP 400 "El archivo debe ser una imagen" | Revisar `type` en FormData (regex MIME) |
| Alerta "Sin coincidencias" | Comportamiento esperado si confianza < 75% |
| Login OK pero IA falla | Problema específico del puerto 8000, no de Android/cleartext |
| Cambiaste de red Wi-Fi | Nueva IP → actualizar `config.ts` y re-verificar firewall |

### Alternativa con USB (adb reverse)

Si no puedes modificar el firewall, con el celular conectado por USB:

```powershell
adb reverse tcp:8000 tcp:8000
```

Y en `config.ts` temporalmente:

```typescript
const LOCAL_IP = '127.0.0.1';
```

---

## 9. Archivos modificados

| Archivo | Cambio |
|---|---|
| `app-movil-pasajero/src/utils/config.ts` | Centralización de IP y URLs de microservicios |
| `app-movil-pasajero/src/screens/home/BuscarImagenScreen.tsx` | Regex MIME, parseo de respuesta Django, sin `Content-Type` manual |
| `motor_ia/motor_ia/settings.py` | `ALLOWED_HOSTS = ['*']` |
| Firewall de Windows | Regla `"Motor IA Django 8000"` (TCP 8000 inbound) |

---

## 10. Resumen ejecutivo

El error **no era de Android ni de cleartext HTTP**. El login GraphQL demostró que la app puede hablar con la PC por HTTP en la LAN.

La causa real fue el **Firewall de Windows**, que bloqueaba el puerto **8000** porque Django corría con el Python del **entorno virtual** (`env\Scripts\python.exe`), no con el Python global que sí tenía reglas de permiso.

La solución definitiva fue una **regla de firewall por puerto** (8000/TCP), más correcciones menores en el frontend para alinear el contrato de la API y el tipo MIME de la imagen.
