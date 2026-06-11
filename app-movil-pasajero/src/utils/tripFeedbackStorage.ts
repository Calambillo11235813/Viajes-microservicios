import AsyncStorage from '@react-native-async-storage/async-storage';

const FEEDBACK_PREFIX = 'feedback';

function buildFeedbackKey(idUsuario: number, idViaje: number | string): string {
  return `${FEEDBACK_PREFIX}:${idUsuario}:${idViaje}`;
}

export async function hasTripFeedback(idUsuario: number, idViaje: number | string): Promise<boolean> {
  const value = await AsyncStorage.getItem(buildFeedbackKey(idUsuario, idViaje));
  return value === 'true';
}

export async function markTripFeedbackSent(idUsuario: number, idViaje: number | string): Promise<void> {
  await AsyncStorage.setItem(buildFeedbackKey(idUsuario, idViaje), 'true');
}

export async function loadSentTripFeedback(
  idUsuario: number,
  idViajes: Array<number | string>
): Promise<Set<string>> {
  const uniqueIds = Array.from(new Set(idViajes.map(String)));
  const entries = await Promise.all(
    uniqueIds.map(async (idViaje) => ({
      idViaje,
      sent: await hasTripFeedback(idUsuario, idViaje),
    }))
  );

  return new Set(entries.filter((entry) => entry.sent).map((entry) => entry.idViaje));
}
