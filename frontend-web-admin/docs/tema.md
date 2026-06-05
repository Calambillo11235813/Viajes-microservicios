# Tema y Sistema de Diseño (Tailwind CSS)

Este documento define la paleta de colores, tipografía y estilos base que se utilizarán en el frontend de la aplicación de Viajes, asegurando una experiencia de usuario (UX) premium y consistente.

## 1. Paleta de Colores

Se utilizará una paleta moderna y profesional, enfocada en inspirar confianza y dinamismo (ideal para una agencia de viajes).

### Colores Principales (Brand)
- **Primary (Azul Viaje):** `blue-600` (`#2563eb`) - Para botones principales, enlaces y elementos destacados.
  - *Hover:* `blue-700` (`#1d4ed8`)
  - *Fondo claro:* `blue-50` (`#eff6ff`)
- **Secondary (Cian/Teal):** `teal-500` (`#14b8a6`) - Para acentos, badges de "Nuevo" o elementos secundarios que necesiten resaltar.

### Colores Neutros (Estructura y Texto)
- **Fondo de la App:** `gray-50` (`#f9fafb`) - Fondo principal de la aplicación, más suave que el blanco puro.
- **Superficies (Cards, Modales):** `bg-white` (`#ffffff`) - Para contenedores de contenido.
- **Texto Principal:** `gray-800` (`#1f2937`) - Para encabezados y texto importante.
- **Texto Secundario:** `gray-500` (`#6b7280`) - Para subtítulos, descripciones y placeholders.
- **Bordes y Divisores:** `gray-200` (`#e5e7eb`) - Para separar secciones sutilmente.

### Colores de Estado (Feedback)
- **Éxito (Activo/Disponible):** `green-600` (`#16a34a`)
  - *Fondo/Badge:* `bg-green-100 text-green-800`
- **Advertencia (En Mantenimiento/Pendiente):** `yellow-500` (`#eab308`)
  - *Fondo/Badge:* `bg-yellow-100 text-yellow-800`
- **Error/Peligro (Cancelado/Eliminar):** `red-600` (`#dc2626`)
  - *Fondo/Badge:* `bg-red-100 text-red-800`

---

## 2. Tipografía

Se recomienda utilizar fuentes modernas sin serifa (sans-serif) para una lectura clara en pantallas digitales.

- **Fuente Principal:** `Inter` o `Roboto` (configurada como sans en Tailwind).
- **Encabezados (H1, H2):** Font-weight `semibold` o `bold`, color `gray-800`.
- **Texto Regular (p, span):** Tamaño `text-sm` o `text-base`, color `gray-600`.

---

## 3. Elementos de Interfaz (UI Components)

### Botones
- **Botón Primario:**
  ```html
  <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg shadow-sm font-medium transition-colors">
    Acción
  </button>
  ```
- **Botón Secundario (Outline):**
  ```html
  <button class="border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 px-4 py-2 rounded-lg shadow-sm font-medium transition-colors">
    Cancelar
  </button>
  ```
- **Botón Peligro:**
  ```html
  <button class="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg shadow-sm font-medium transition-colors">
    Eliminar
  </button>
  ```

### Tarjetas (Cards)
Para envolver contenido, tablas o formularios:
```html
<div class="bg-white rounded-xl shadow-md border border-gray-100 p-6">
  <!-- Contenido -->
</div>
```

### Entradas de Formulario (Inputs)
```html
<input type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm px-3 py-2 border">
```

### Badges (Etiquetas de Estado)
```html
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
  Activo
</span>
```

---

## 4. Efectos y Animaciones

- **Sombras:** Se prioriza el uso de `shadow-sm` para botones e inputs, y `shadow-md` para tarjetas.
- **Bordes Redondeados:** Se utiliza `rounded-lg` o `rounded-xl` para un aspecto moderno y amigable.
- **Transiciones:** Añadir `transition-colors duration-200` a botones y enlaces para cambios de estado (hover) suaves.
- **Glassmorphism (opcional):** Para modales o headers superpuestos, usar fondos semitransparentes con desenfoque: `bg-white/80 backdrop-blur-md`.
