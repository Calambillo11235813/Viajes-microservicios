package main

import (
	"context"
	"log"

	"github.com/hibiken/asynq"

	"automatizacion_auditoria/config"
	"automatizacion_auditoria/internal/events"
	"automatizacion_auditoria/internal/queue/handlers"
	"automatizacion_auditoria/internal/queue/tasks"
)

func main() {
	cfg := config.Load()

	srv := asynq.NewServer(
		cfg.AsynqRedisClientOpt(),
		asynq.Config{
			Concurrency: 10,
			Queues: map[string]int{
				"default": 10,
			},
			ErrorHandler: asynq.ErrorHandlerFunc(func(ctx context.Context, task *asynq.Task, err error) {
				log.Printf("[asynq] tarea fallida tipo=%s: %v", task.Type(), err)
			}),
		},
	)

	redisClient := cfg.RedisClient()
	defer redisClient.Close()

	mux := asynq.NewServeMux()
	paymentHandler := handlers.NewPaymentHandler(redisClient)
	mux.HandleFunc(tasks.TypePaymentSuccess, paymentHandler.HandlePaymentSuccessTask)

	// Bridge Pub/Sub → Asynq: escucha el canal de emisión de boletos publicado
	// por core-transaccional y encola una tarea por cada asiento.
	asynqClient := asynq.NewClient(cfg.AsynqRedisClientOpt())
	defer asynqClient.Close()

	subscriber := events.NewPaymentSubscriber(cfg, redisClient, asynqClient)
	subCtx, cancelSub := context.WithCancel(context.Background())
	defer cancelSub()

	go func() {
		if err := subscriber.Run(subCtx); err != nil && err != context.Canceled {
			log.Printf("[boletos.emitir] suscriptor finalizó con error: %v", err)
		}
	}()

	log.Printf("Worker Asynq iniciado (Redis: %s)", cfg.RedisAddr)
	if err := srv.Run(mux); err != nil {
		log.Fatalf("error al ejecutar worker Asynq: %v", err)
	}
}
