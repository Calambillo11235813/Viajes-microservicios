package main

import (
	"encoding/json"
	"fmt"
	"log"
	"os"

	"github.com/hibiken/asynq"

	"automatizacion_auditoria/config"
	"automatizacion_auditoria/internal/queue/tasks"
)

func main() {
	cfg := config.Load()

	payload := map[string]string{
		"nombre":  "Juan Pérez",
		"origen":  "Ciudad de México",
		"destino": "Cancún",
		"fecha":   "2026-06-15",
		"hora":    "08:30",
		"asiento": "12A",
		"email":   "juan.perez@example.com",
	}

	payloadBytes, err := json.Marshal(payload)
	if err != nil {
		log.Fatalf("error al serializar payload: %v", err)
	}

	client := asynq.NewClient(cfg.AsynqRedisClientOpt())
	defer func() {
		if closeErr := client.Close(); closeErr != nil {
			log.Printf("advertencia al cerrar cliente Redis: %v", closeErr)
		}
	}()

	task := asynq.NewTask(tasks.TypePaymentSuccess, payloadBytes)
	info, err := client.Enqueue(task)
	if err != nil {
		log.Fatalf("error al encolar tarea %s: %v", tasks.TypePaymentSuccess, err)
	}

	fmt.Printf("Tarea encolada correctamente\n")
	fmt.Printf("  Tipo:    %s\n", info.Type)
	fmt.Printf("  ID:      %s\n", info.ID)
	fmt.Printf("  Cola:    %s\n", info.Queue)
	fmt.Printf("  Redis:   %s\n", cfg.RedisAddr)
	fmt.Println("Verifica los logs del worker para confirmar la generación del PDF y el envío a n8n.")

	os.Exit(0)
}
