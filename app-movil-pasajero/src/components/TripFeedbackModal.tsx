import React, { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Modal,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, SPACING, TYPOGRAPHY } from '@/theme/theme';

export interface TripFeedbackSubmit {
  calificacion: number;
  comentario: string;
}

interface TripFeedbackModalProps {
  visible: boolean;
  routeLabel: string;
  loading: boolean;
  onClose: () => void;
  onSubmit: (feedback: TripFeedbackSubmit) => Promise<boolean>;
}

/**
 * Modal post-viaje para capturar calificación 1-5 y comentario opcional.
 */
export default function TripFeedbackModal({
  visible,
  routeLabel,
  loading,
  onClose,
  onSubmit,
}: TripFeedbackModalProps) {
  const [calificacion, setCalificacion] = useState(0);
  const [comentario, setComentario] = useState('');

  useEffect(() => {
    if (visible) {
      setCalificacion(0);
      setComentario('');
    }
  }, [visible]);

  const handleSubmit = async () => {
    const sent = await onSubmit({ calificacion, comentario });
    if (sent) {
      onClose();
    }
  };

  return (
    <Modal visible={visible} animationType="slide" transparent>
      <View style={styles.overlay}>
        <View style={styles.modalContainer}>
          <View style={styles.header}>
            <View>
              <Text style={styles.title}>Califica tu viaje</Text>
              <Text style={styles.subtitle}>{routeLabel}</Text>
            </View>
            <TouchableOpacity onPress={onClose} style={styles.closeButton} disabled={loading}>
              <Ionicons name="close" size={22} color={COLORS.textSecondary} />
            </TouchableOpacity>
          </View>

          <Text style={styles.label}>Tu experiencia</Text>
          <View style={styles.starsRow}>
            {[1, 2, 3, 4, 5].map((star) => (
              <TouchableOpacity
                key={star}
                onPress={() => setCalificacion(star)}
                disabled={loading}
                style={styles.starButton}
              >
                <Ionicons
                  name={star <= calificacion ? 'star' : 'star-outline'}
                  size={34}
                  color={COLORS.accent}
                />
              </TouchableOpacity>
            ))}
          </View>

          <Text style={styles.label}>Comentario opcional</Text>
          <TextInput
            style={styles.commentInput}
            value={comentario}
            onChangeText={setComentario}
            placeholder="Cuéntanos cómo fue el viaje..."
            placeholderTextColor={COLORS.placeholder}
            multiline
            numberOfLines={4}
            textAlignVertical="top"
            editable={!loading}
            maxLength={500}
          />
          <Text style={styles.counter}>{comentario.length}/500</Text>

          <TouchableOpacity
            style={[styles.submitButton, (loading || calificacion === 0) && styles.submitButtonDisabled]}
            onPress={handleSubmit}
            disabled={loading || calificacion === 0}
          >
            {loading ? (
              <ActivityIndicator color={COLORS.textLight} />
            ) : (
              <Text style={styles.submitButtonText}>Enviar calificación</Text>
            )}
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.55)',
    justifyContent: 'flex-end',
  },
  modalContainer: {
    backgroundColor: COLORS.surface,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: SPACING.lg,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    marginBottom: SPACING.lg,
  },
  title: {
    ...TYPOGRAPHY.h2,
    color: COLORS.primary,
  },
  subtitle: {
    ...TYPOGRAPHY.caption,
    color: COLORS.textSecondary,
    marginTop: SPACING.xs,
  },
  closeButton: {
    padding: SPACING.xs,
  },
  label: {
    ...TYPOGRAPHY.caption,
    color: COLORS.textSecondary,
    fontWeight: '700',
    marginBottom: SPACING.sm,
    textTransform: 'uppercase',
  },
  starsRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginBottom: SPACING.lg,
  },
  starButton: {
    padding: SPACING.xs,
  },
  commentInput: {
    minHeight: 110,
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 12,
    padding: SPACING.md,
    color: COLORS.textPrimary,
    backgroundColor: COLORS.background,
    fontSize: 15,
  },
  counter: {
    ...TYPOGRAPHY.caption,
    color: COLORS.placeholder,
    alignSelf: 'flex-end',
    marginTop: SPACING.xs,
    marginBottom: SPACING.md,
  },
  submitButton: {
    backgroundColor: COLORS.accent,
    borderRadius: 12,
    paddingVertical: SPACING.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  submitButtonDisabled: {
    opacity: 0.55,
  },
  submitButtonText: {
    ...TYPOGRAPHY.buttonText,
  },
});
