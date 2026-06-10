package main

import (
	"fmt"
	"log"
	"os"

	"automatizacion_auditoria/config"
	"automatizacion_auditoria/internal/pdf"
	"automatizacion_auditoria/internal/webhooks"
)

func main() {
	cfg := config.Load()
	if cfg.N8NTelegramWebhookURL == "" {
		log.Fatal("N8N_TELEGRAM_WEBHOOK_URL no configurada en .env")
	}

	payload := map[string]interface{}{
		"nombre":  "Juan Pérez",
		"origen":  "Ciudad de México",
		"destino": "Cancún",
		"fecha":   "2026-06-15",
		"hora":    "08:30",
		"asiento": "12A",
		"email":   "juan.perez@example.com",
	}

	log.Println("[test-telegram] generando PDF de prueba...")
	pdfBytes, err := pdf.GenerateTicketPDF(payload)
	if err != nil {
		log.Fatalf("[test-telegram] error generando PDF: %v", err)
	}
	log.Printf("[test-telegram] PDF generado (%d bytes)", len(pdfBytes))

	log.Printf("[test-telegram] enviando a n8n: %s", cfg.N8NTelegramWebhookURL)
	if err := webhooks.SendPDFToTelegram(pdfBytes, "boleto.pdf"); err != nil {
		log.Fatalf("[test-telegram] error enviando PDF: %v", err)
	}

	fmt.Println("[test-telegram] PDF enviado correctamente")
	fmt.Println("  Campo multipart: file")
	fmt.Println("  Content-Type:    application/pdf")
	fmt.Println("  Filename:        boleto.pdf")
	fmt.Println("Verifica en n8n que el nodo Webhook tenga binary field 'file' y en Telegram el documento.")

	os.Exit(0)
}
