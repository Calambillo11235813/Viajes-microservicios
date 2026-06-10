package webhooks

import (
	"bytes"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"net/textproto"
	"strings"
	"time"

	"automatizacion_auditoria/config"
)

const telegramHTTPTimeout = 30 * time.Second

// SendPDFToTelegram envía el PDF al webhook de n8n configurado para reenvío a Telegram.
// n8n espera el binario en el campo multipart "file" con Content-Type application/pdf.
func SendPDFToTelegram(pdfData []byte, filename string) error {
	webhookURL := config.Load().N8NTelegramWebhookURL
	if webhookURL == "" {
		return fmt.Errorf("N8N_TELEGRAM_WEBHOOK_URL no configurada")
	}

	if !strings.HasSuffix(strings.ToLower(filename), ".pdf") {
		filename += ".pdf"
	}

	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)

	part, err := createPDFFormFile(writer, "file", filename)
	if err != nil {
		return fmt.Errorf("crear campo file: %w", err)
	}

	if _, err := io.Copy(part, bytes.NewReader(pdfData)); err != nil {
		return fmt.Errorf("escribir PDF en multipart: %w", err)
	}

	if err := writer.Close(); err != nil {
		return fmt.Errorf("cerrar multipart writer: %w", err)
	}

	req, err := http.NewRequest(http.MethodPost, webhookURL, body)
	if err != nil {
		return fmt.Errorf("crear peticion HTTP: %w", err)
	}
	req.Header.Set("Content-Type", writer.FormDataContentType())

	client := &http.Client{Timeout: telegramHTTPTimeout}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("enviar PDF a n8n: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode < http.StatusOK || resp.StatusCode >= http.StatusMultipleChoices {
		respBody, readErr := io.ReadAll(resp.Body)
		if readErr != nil {
			return fmt.Errorf("n8n respondio con status %d", resp.StatusCode)
		}
		return fmt.Errorf("n8n respondio con status %d: %s", resp.StatusCode, string(respBody))
	}

	return nil
}

func createPDFFormFile(writer *multipart.Writer, fieldName, filename string) (io.Writer, error) {
	header := make(textproto.MIMEHeader)
	header.Set("Content-Disposition", fmt.Sprintf(`form-data; name="%s"; filename="%s"`, fieldName, filename))
	header.Set("Content-Type", "application/pdf")
	return writer.CreatePart(header)
}
