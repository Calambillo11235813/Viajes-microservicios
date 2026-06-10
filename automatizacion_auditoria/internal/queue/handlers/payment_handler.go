package handlers

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	"github.com/google/uuid"
	"github.com/hibiken/asynq"
	"github.com/redis/go-redis/v9"

	"automatizacion_auditoria/internal/blockchain"
	"automatizacion_auditoria/internal/pdf"
	"automatizacion_auditoria/internal/storage"
	"automatizacion_auditoria/internal/webhooks"
)

const telegramSentKeyPrefix = "pago_exitoso:telegram:"
const telegramSentTTL = 30 * 24 * time.Hour

// PaymentHandler procesa la tarea tasks:pago_exitoso.
type PaymentHandler struct {
	redis *redis.Client
}

// NewPaymentHandler crea el handler con las dependencias necesarias.
func NewPaymentHandler(redisClient *redis.Client) *PaymentHandler {
	return &PaymentHandler{redis: redisClient}
}

// HandlePaymentSuccessTask ejecuta el pipeline PDF → Telegram → Blockchain → S3.
// Si Telegram ya se envió en un intento previo, los reintentos omiten ese paso.
func (h *PaymentHandler) HandlePaymentSuccessTask(ctx context.Context, t *asynq.Task) error {
	taskID := resolveObjectID(ctx)
	retryCount, _ := asynq.GetRetryCount(ctx)
	if retryCount > 0 {
		log.Printf("[pago_exitoso] reintento #%d id=%s", retryCount, taskID)
	} else {
		log.Printf("[pago_exitoso] iniciando tarea id=%s", taskID)
	}

	var payload map[string]interface{}
	if err := json.Unmarshal(t.Payload(), &payload); err != nil {
		log.Printf("[pago_exitoso] ERROR parseando payload id=%s: %v", taskID, err)
		return fmt.Errorf("parsear payload de pago exitoso: %w", err)
	}
	log.Printf("[pago_exitoso] payload recibido: nombre=%v origen=%v destino=%v",
		payload["nombre"], payload["origen"], payload["destino"])

	log.Printf("[pago_exitoso] generando PDF id=%s", taskID)
	pdfBytes, err := pdf.GenerateTicketPDF(payload)
	if err != nil {
		log.Printf("[pago_exitoso] ERROR generando PDF id=%s: %v", taskID, err)
		return fmt.Errorf("generar boleto PDF: %w", err)
	}
	log.Printf("[pago_exitoso] PDF generado correctamente id=%s bytes=%d", taskID, len(pdfBytes))

	if err := h.sendTelegramIfNeeded(ctx, taskID, pdfBytes); err != nil {
		return err
	}

	log.Printf("[pago_exitoso] calculando hash SHA-256 id=%s", taskID)
	hashHex, err := blockchain.CalculateSHA256(pdfBytes)
	if err != nil {
		log.Printf("[pago_exitoso] ERROR calculando hash id=%s: %v", taskID, err)
		return fmt.Errorf("calcular hash SHA-256 del boleto: %w", err)
	}
	log.Printf("[pago_exitoso] hash SHA-256 calculado id=%s hash=%s", taskID, hashHex)

	log.Printf("[pago_exitoso] registrando hash en blockchain id=%s", taskID)
	txHash, err := blockchain.RegisterTicketHash(ctx, hashHex)
	if err != nil {
		log.Printf("[pago_exitoso] ERROR certificando en blockchain id=%s: %v", taskID, err)
		return fmt.Errorf("certificar boleto en blockchain: %w", err)
	}
	if txHash != "" {
		log.Printf("[pago_exitoso] certificacion blockchain completada id=%s txHash=%s hash=%s", taskID, txHash, hashHex)
	} else {
		log.Printf("[pago_exitoso] hash ya registrado en blockchain id=%s hash=%s", taskID, hashHex)
	}

	s3Key := fmt.Sprintf("boletos/%s.pdf", taskID)
	log.Printf("[pago_exitoso] subiendo PDF a S3 id=%s key=%s", taskID, s3Key)
	storedKey, err := storage.UploadPDFToS3(ctx, pdfBytes, s3Key)
	if err != nil {
		log.Printf("[pago_exitoso] ERROR subiendo a S3 id=%s: %v", taskID, err)
		return fmt.Errorf("guardar boleto en S3: %w", err)
	}
	log.Printf("[pago_exitoso] PDF guardado en S3 id=%s key=%s", taskID, storedKey)

	log.Printf("[pago_exitoso] tarea completada exitosamente id=%s", taskID)
	return nil
}

func (h *PaymentHandler) sendTelegramIfNeeded(ctx context.Context, taskID string, pdfBytes []byte) error {
	sent, err := h.isTelegramSent(ctx, taskID)
	if err != nil {
		log.Printf("[pago_exitoso] advertencia verificando envio previo a Telegram id=%s: %v", taskID, err)
	}

	if sent {
		log.Printf("[pago_exitoso] Telegram ya enviado en intento previo, omitiendo reenvio id=%s", taskID)
		return nil
	}

	log.Printf("[pago_exitoso] enviando PDF a Telegram via n8n id=%s", taskID)
	if err := webhooks.SendPDFToTelegram(pdfBytes, "boleto.pdf"); err != nil {
		log.Printf("[pago_exitoso] ERROR enviando a Telegram id=%s: %v", taskID, err)
		return fmt.Errorf("enviar boleto a Telegram via n8n: %w", err)
	}

	if err := h.markTelegramSent(ctx, taskID); err != nil {
		log.Printf("[pago_exitoso] advertencia marcando Telegram como enviado id=%s: %v", taskID, err)
	}

	log.Printf("[pago_exitoso] PDF enviado a Telegram correctamente id=%s", taskID)
	return nil
}

func (h *PaymentHandler) telegramSentKey(taskID string) string {
	return telegramSentKeyPrefix + taskID
}

func (h *PaymentHandler) isTelegramSent(ctx context.Context, taskID string) (bool, error) {
	n, err := h.redis.Exists(ctx, h.telegramSentKey(taskID)).Result()
	if err != nil {
		return false, err
	}
	return n > 0, nil
}

func (h *PaymentHandler) markTelegramSent(ctx context.Context, taskID string) error {
	return h.redis.Set(ctx, h.telegramSentKey(taskID), "1", telegramSentTTL).Err()
}

func resolveObjectID(ctx context.Context) string {
	if taskID, ok := asynq.GetTaskID(ctx); ok && taskID != "" {
		return taskID
	}
	return uuid.New().String()
}
