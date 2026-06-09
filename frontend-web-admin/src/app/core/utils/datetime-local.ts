/** Convierte fecha/hora del backend (LocalDateTime) al formato datetime-local sin cambiar zona horaria. */
export function toDatetimeLocalValue(fechaHoraSalida: string): string {
  return fechaHoraSalida.replace(' ', 'T').slice(0, 16);
}

/** Convierte valor datetime-local a formato ISO para el backend (LocalDateTime). */
export function fromDatetimeLocalValue(value: string): string {
  if (!value) return value;
  return value.length === 16 ? `${value}:00` : value;
}
