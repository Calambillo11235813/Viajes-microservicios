package main

import (
	"context"
	"flag"
	"fmt"
	"log"
	"os"

	"github.com/hibiken/asynq"

	"automatizacion_auditoria/config"
)

const telegramSentKeyPattern = "pago_exitoso:telegram:*"

func main() {
	queue := flag.String("queue", "default", "nombre de la cola Asynq a vaciar")
	clearTelegram := flag.Bool("clear-telegram", true, "eliminar marcas de Telegram enviado en Redis")
	yes := flag.Bool("yes", false, "confirmar sin preguntar (requerido para ejecutar)")
	flag.Parse()

	if !*yes {
		fmt.Fprintf(os.Stderr, "Uso: go run ./cmd/queue-purge --yes\n")
		fmt.Fprintf(os.Stderr, "Vacía tareas pendientes, en reintento, programadas y archivadas de la cola.\n")
		os.Exit(1)
	}

	cfg := config.Load()
	inspector := asynq.NewInspector(cfg.AsynqRedisClientOpt())
	defer inspector.Close()

	ctx := context.Background()

	if info, err := inspector.GetQueueInfo(*queue); err != nil {
		log.Printf("cola %q: no existe o está vacía (%v)", *queue, err)
	} else {
		log.Printf("estado actual cola %q: pending=%d active=%d retry=%d scheduled=%d archived=%d completed=%d",
			*queue, info.Pending, info.Active, info.Retry, info.Scheduled, info.Archived, info.Completed)
	}

	if active, err := inspector.ListActiveTasks(*queue); err == nil {
		for _, task := range active {
			if err := inspector.CancelProcessing(task.ID); err != nil {
				log.Printf("no se pudo cancelar tarea activa %s: %v", task.ID, err)
			} else {
				log.Printf("tarea activa cancelada: %s", task.ID)
			}
		}
	}

	deleted, err := purgeQueue(inspector, *queue)
	if err != nil {
		log.Fatalf("error vaciando cola: %v", err)
	}

	if *clearTelegram {
		n, err := clearTelegramFlags(ctx, cfg)
		if err != nil {
			log.Printf("advertencia limpiando marcas Telegram: %v", err)
		} else {
			log.Printf("marcas Telegram eliminadas: %d", n)
		}
	}

	log.Printf("cola %q vaciada: %d tareas eliminadas en total", *queue, deleted)
}

func purgeQueue(inspector *asynq.Inspector, queue string) (int, error) {
	total := 0

	steps := []struct {
		name string
		fn   func(string) (int, error)
	}{
		{"pending", inspector.DeleteAllPendingTasks},
		{"retry", inspector.DeleteAllRetryTasks},
		{"scheduled", inspector.DeleteAllScheduledTasks},
		{"archived", inspector.DeleteAllArchivedTasks},
	}

	for _, step := range steps {
		n, err := step.fn(queue)
		if err != nil {
			return total, fmt.Errorf("eliminar tareas %s: %w", step.name, err)
		}
		if n > 0 {
			log.Printf("eliminadas %d tareas %s", n, step.name)
		}
		total += n
	}

	return total, nil
}

func clearTelegramFlags(ctx context.Context, cfg *config.Config) (int, error) {
	rdb := cfg.RedisClient()
	defer rdb.Close()

	var cursor uint64
	deleted := 0

	for {
		keys, next, err := rdb.Scan(ctx, cursor, telegramSentKeyPattern, 100).Result()
		if err != nil {
			return deleted, err
		}

		if len(keys) > 0 {
			n, err := rdb.Del(ctx, keys...).Result()
			if err != nil {
				return deleted, err
			}
			deleted += int(n)
		}

		cursor = next
		if cursor == 0 {
			break
		}
	}

	return deleted, nil
}
