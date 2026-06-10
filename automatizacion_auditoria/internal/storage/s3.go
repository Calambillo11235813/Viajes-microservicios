package storage

import (
	"bytes"
	"context"
	"fmt"

	"github.com/aws/aws-sdk-go-v2/aws"
	awsconfig "github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/credentials"
	"github.com/aws/aws-sdk-go-v2/service/s3"

	appconfig "automatizacion_auditoria/config"
)

// UploadPDFToS3 sube el PDF al bucket configurado y retorna la key del objeto almacenado.
func UploadPDFToS3(ctx context.Context, pdfData []byte, key string) (string, error) {
	cfg := appconfig.Load()

	if cfg.S3BucketName == "" {
		return "", fmt.Errorf("S3_BUCKET_NAME no configurado")
	}
	if cfg.AWSRegion == "" {
		return "", fmt.Errorf("AWS_REGION no configurado")
	}

	loadOpts := []func(*awsconfig.LoadOptions) error{
		awsconfig.WithRegion(cfg.AWSRegion),
	}

	if cfg.AWSAccessKeyID != "" && cfg.AWSSecretAccessKey != "" {
		loadOpts = append(loadOpts, awsconfig.WithCredentialsProvider(
			credentials.NewStaticCredentialsProvider(cfg.AWSAccessKeyID, cfg.AWSSecretAccessKey, ""),
		))
	}

	awsCfg, err := awsconfig.LoadDefaultConfig(ctx, loadOpts...)
	if err != nil {
		return "", fmt.Errorf("cargar configuracion AWS: %w", err)
	}

	client := s3.NewFromConfig(awsCfg)

	_, err = client.PutObject(ctx, &s3.PutObjectInput{
		Bucket:      aws.String(cfg.S3BucketName),
		Key:         aws.String(key),
		Body:        bytes.NewReader(pdfData),
		ContentType: aws.String("application/pdf"),
	})
	if err != nil {
		return "", fmt.Errorf("subir PDF a S3 (bucket=%s, key=%s): %w", cfg.S3BucketName, key, err)
	}

	return key, nil
}
