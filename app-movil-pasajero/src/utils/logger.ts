function horaLocal(): string {
  return new Date().toLocaleString('es-BO', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}

export const appLog = {
  info: (etiqueta: string, ...args: unknown[]) =>
    console.log(`[${horaLocal()}] [${etiqueta}]`, ...args),
  warn: (etiqueta: string, ...args: unknown[]) =>
    console.warn(`[${horaLocal()}] [${etiqueta}]`, ...args),
  error: (etiqueta: string, ...args: unknown[]) =>
    console.error(`[${horaLocal()}] [${etiqueta}]`, ...args),
};
