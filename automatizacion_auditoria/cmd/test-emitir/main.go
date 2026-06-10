package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"time"

	"automatizacion_auditoria/config"
)

// Publica un evento boletos.emitir de prueba con 3 asientos para validar el bridge
// Pub/Sub → Asynq sin depender de core-transaccional.
func main() {
	cfg := config.Load()

	event := map[string]interface{}{
		"eventType": "BOLETOS_EMITIR",
		"idPago":    9001,
		"idReserva": 5001,
		"email":     "juan.perez@example.com",
		"origen":    "Santa Cruz",
		"destino":   "La Paz",
		"fecha":     "2026-06-15",
		"hora":      "08:30",
		"boletos": []map[string]interface{}{
			{"idBoleto": 70001, "asiento": "12A", "nombre": "Juan Pérez", "tipoPasajero": "ADULTO"},
			{"idBoleto": 70002, "asiento": "12B", "nombre": "María Pérez", "tipoPasajero": "ADULTO"},
			{"idBoleto": 70003, "asiento": "12C", "nombre": "Lucía Pérez", "tipoPasajero": "MENOR"},
		},
	}

	payload, err := json.Marshal(event)
	if err != nil {
		log.Fatalf("error serializando evento: %v", err)
	}

	client := cfg.RedisClient()
	defer client.Close()

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	receivers, err := client.Publish(ctx, cfg.BoletosEmitirChannel, payload).Result()
	if err != nil {
		log.Fatalf("error publicando en %s: %v", cfg.BoletosEmitirChannel, err)
	}

	fmt.Printf("Evento publicado en canal %s\n", cfg.BoletosEmitirChannel)
	fmt.Printf("  Suscriptores que recibieron: %d\n", receivers)
	fmt.Printf("  Boletos en el evento:        3\n")
	if receivers == 0 {
		fmt.Println("ADVERTENCIA: ningún suscriptor activo. Asegúrate de que el worker esté corriendo.")
	}
	fmt.Println("Verifica los logs del worker: debe encolar y procesar 3 tareas tasks:pago_exitoso.")

	os.Exit(0)
}
