package events

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"

	"github.com/hibiken/asynq"
	"github.com/redis/go-redis/v9"

	"automatizacion_auditoria/config"
	"automatizacion_auditoria/internal/queue/tasks"
)

// boletoEmitir representa un asiento individual dentro del evento.
type boletoEmitir struct {
	IDBoleto     int64  `json:"idBoleto"`
	Asiento      string `json:"asiento"`
	Nombre       string `json:"nombre"`
	TipoPasajero string `json:"tipoPasajero"`
}

// boletosEmitirEvent es el payload publicado por core-transaccional en el canal Pub/Sub.
type boletosEmitirEvent struct {
	EventType string         `json:"eventType"`
	IDPago    int64          `json:"idPago"`
	IDReserva int64          `json:"idReserva"`
	Email     string         `json:"email"`
	Origen    string         `json:"origen"`
	Destino   string         `json:"destino"`
	Fecha     string         `json:"fecha"`
	Hora      string         `json:"hora"`
	Boletos   []boletoEmitir `json:"boletos"`
}

// PaymentSubscriber escucha el canal Pub/Sub de emisión de boletos y encola
// una tarea Asynq por cada asiento de la reserva.
type PaymentSubscriber struct {
	cfg         *config.Config
	redisClient *redis.Client
	asynqClient *asynq.Client
}

// NewPaymentSubscriber crea el suscriptor con las dependencias necesarias.
func NewPaymentSubscriber(cfg *config.Config, redisClient *redis.Client, asynqClient *asynq.Client) *PaymentSubscriber {
	return &PaymentSubscriber{
		cfg:         cfg,
		redisClient: redisClient,
		asynqClient: asynqClient,
	}
}

// Run se suscribe al canal y procesa eventos hasta que el contexto se cancela.
func (s *PaymentSubscriber) Run(ctx context.Context) error {
	channel := s.cfg.BoletosEmitirChannel
	sub := s.redisClient.Subscribe(ctx, channel)
	defer sub.Close()

	if _, err := sub.Receive(ctx); err != nil {
		return fmt.Errorf("suscribirse al canal %s: %w", channel, err)
	}

	log.Printf("[boletos.emitir] suscrito al canal Pub/Sub: %s", channel)
	ch := sub.Channel()

	for {
		select {
		case <-ctx.Done():
			log.Printf("[boletos.emitir] suscripción detenida: %v", ctx.Err())
			return ctx.Err()
		case msg, ok := <-ch:
			if !ok {
				return fmt.Errorf("canal Pub/Sub cerrado inesperadamente")
			}
			s.handleMessage(ctx, msg.Payload)
		}
	}
}

func (s *PaymentSubscriber) handleMessage(ctx context.Context, payload string) {
	var event boletosEmitirEvent
	if err := json.Unmarshal([]byte(payload), &event); err != nil {
		log.Printf("[boletos.emitir] ERROR parseando evento: %v | payload=%s", err, payload)
		return
	}

	if len(event.Boletos) == 0 {
		log.Printf("[boletos.emitir] evento sin boletos idReserva=%d, ignorado", event.IDReserva)
		return
	}

	log.Printf("[boletos.emitir] evento recibido idReserva=%d idPago=%d boletos=%d",
		event.IDReserva, event.IDPago, len(event.Boletos))

	for _, boleto := range event.Boletos {
		if err := s.enqueueBoleto(ctx, event, boleto); err != nil {
			if errors.Is(err, asynq.ErrTaskIDConflict) || errors.Is(err, asynq.ErrDuplicateTask) {
				log.Printf("[boletos.emitir] boleto ya encolado previamente idBoleto=%d asiento=%s, omitido",
					boleto.IDBoleto, boleto.Asiento)
				continue
			}
			log.Printf("[boletos.emitir] ERROR encolando boleto idBoleto=%d asiento=%s: %v",
				boleto.IDBoleto, boleto.Asiento, err)
			continue
		}
	}
}

func (s *PaymentSubscriber) enqueueBoleto(ctx context.Context, event boletosEmitirEvent, boleto boletoEmitir) error {
	taskPayload := map[string]interface{}{
		"nombre":        boleto.Nombre,
		"origen":        event.Origen,
		"destino":       event.Destino,
		"fecha":         event.Fecha,
		"hora":          event.Hora,
		"asiento":       boleto.Asiento,
		"email":         event.Email,
		"tipo_pasajero": boleto.TipoPasajero,
		"id_reserva":    event.IDReserva,
		"id_boleto":     boleto.IDBoleto,
	}

	payloadBytes, err := json.Marshal(taskPayload)
	if err != nil {
		return fmt.Errorf("serializar payload de tarea: %w", err)
	}

	task := asynq.NewTask(tasks.TypePaymentSuccess, payloadBytes)

	// TaskID basado en el boleto: idempotencia si el evento se reprocesa.
	info, err := s.asynqClient.EnqueueContext(ctx, task,
		asynq.TaskID(fmt.Sprintf("boleto-%d", boleto.IDBoleto)))
	if err != nil {
		return err
	}

	log.Printf("[boletos.emitir] tarea encolada idBoleto=%d asiento=%s taskID=%s",
		boleto.IDBoleto, boleto.Asiento, info.ID)
	return nil
}
