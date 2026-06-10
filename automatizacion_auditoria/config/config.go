package config

import (
	"crypto/tls"
	"log"
	"os"
	"strconv"
	"strings"
	"sync"

	"github.com/hibiken/asynq"
	"github.com/joho/godotenv"
)

// Config centraliza las variables de entorno del microservicio.
type Config struct {
	RedisAddr             string
	RedisUsername         string
	RedisPassword         string
	RedisUseTLS           bool
	N8NTelegramWebhookURL string
	AWSRegion             string
	S3BucketName          string
	AWSAccessKeyID         string
	AWSSecretAccessKey     string
	BlockchainRPCURL       string
	BlockchainChainID      int64
	BlockchainPrivateKey   string
	SmartContractAddress   string
}

var (
	instance *Config
	once     sync.Once
)

// Load lee la configuración desde variables de entorno.
// Si existe un archivo .env en el directorio de trabajo, se carga automáticamente.
func Load() *Config {
	once.Do(func() {
		if err := godotenv.Load(); err != nil {
			log.Printf("config: .env no encontrado, usando variables de entorno del sistema")
		}

		chainID, _ := strconv.ParseInt(os.Getenv("BLOCKCHAIN_CHAIN_ID"), 10, 64)

		redisAddr := getEnv("REDIS_ADDR", "localhost:6379")

		instance = &Config{
			RedisAddr:              redisAddr,
			RedisUsername:          os.Getenv("REDIS_USERNAME"),
			RedisPassword:          os.Getenv("REDIS_PASSWORD"),
			RedisUseTLS:            redisUseTLS(redisAddr),
			N8NTelegramWebhookURL:  os.Getenv("N8N_TELEGRAM_WEBHOOK_URL"),
			AWSRegion: firstNonEmpty(
				strings.TrimSpace(os.Getenv("AWS_REGION")),
				strings.TrimSpace(os.Getenv("AWS_S3_REGION_NAME")),
				strings.TrimSpace(os.Getenv("S3_REGION")),
			),
			S3BucketName: firstNonEmpty(
				strings.TrimSpace(os.Getenv("S3_BUCKET_NAME")),
				strings.TrimSpace(os.Getenv("AWS_STORAGE_BUCKET_NAME")),
			),
			AWSAccessKeyID:         os.Getenv("AWS_ACCESS_KEY_ID"),
			AWSSecretAccessKey:     os.Getenv("AWS_SECRET_ACCESS_KEY"),
			BlockchainRPCURL:       os.Getenv("BLOCKCHAIN_RPC_URL"),
			BlockchainChainID:      chainID,
			BlockchainPrivateKey:   os.Getenv("BLOCKCHAIN_PRIVATE_KEY"),
			SmartContractAddress:   os.Getenv("SMART_CONTRACT_ADDRESS"),
		}
	})
	return instance
}

func getEnv(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}

func firstNonEmpty(values ...string) string {
	for _, value := range values {
		if value != "" {
			return value
		}
	}
	return ""
}

func redisUseTLS(redisAddr string) bool {
	if value := os.Getenv("REDIS_USE_TLS"); value != "" {
		useTLS, err := strconv.ParseBool(value)
		if err != nil {
			log.Printf("config: REDIS_USE_TLS inválido (%q), usando detección automática", value)
		} else {
			return useTLS
		}
	}

	return strings.Contains(redisAddr, "upstash.io")
}

// AsynqRedisClientOpt construye la conexión Redis para Asynq (TLS + auth para Upstash).
func (c *Config) AsynqRedisClientOpt() asynq.RedisClientOpt {
	opt := asynq.RedisClientOpt{
		Addr:     c.RedisAddr,
		Username: c.RedisUsername,
		Password: c.RedisPassword,
	}
	if c.RedisUseTLS {
		opt.TLSConfig = &tls.Config{MinVersion: tls.VersionTLS12}
	}
	return opt
}
