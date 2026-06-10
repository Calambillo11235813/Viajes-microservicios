package main

import (
	"context"
	"log"

	"github.com/hibiken/asynq"

	"automatizacion_auditoria/config"
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

	mux := asynq.NewServeMux()
	mux.HandleFunc(tasks.TypePaymentSuccess, handlers.HandlePaymentSuccessTask)

	log.Printf("Worker Asynq iniciado (Redis: %s)", cfg.RedisAddr)
	if err := srv.Run(mux); err != nil {
		log.Fatalf("error al ejecutar worker Asynq: %v", err)
	}
}
