package blockchain

import (
	"context"
	"encoding/hex"
	"fmt"
	"log"
	"strings"
	"time"

	"github.com/ethereum/go-ethereum/accounts/abi/bind"
)

const txConfirmationTimeout = 2 * time.Minute

// RegisterTicketHash registra el hash del boleto en el smart contract y retorna el tx hash.
func RegisterTicketHash(ctx context.Context, hashHex string) (string, error) {
	hash32, err := hashHexToBytes32(hashHex)
	if err != nil {
		return "", err
	}

	client, err := NewClient(ctx)
	if err != nil {
		return "", err
	}
	defer client.Close()

	registered, err := isTicketRegistered(ctx, client, hash32)
	if err != nil {
		return "", fmt.Errorf("consultar registro previo: %w", err)
	}
	if registered {
		log.Printf("hash de boleto ya registrado en blockchain, hash=%s", hashHex)
		return "", nil
	}

	auth, err := client.NewTransactOpts(ctx)
	if err != nil {
		return "", err
	}

	tx, err := client.Contract().Transact(auth, "registerTicket", hash32)
	if err != nil {
		return "", fmt.Errorf("enviar transaccion registerTicket: %w", err)
	}

	waitCtx, cancel := context.WithTimeout(ctx, txConfirmationTimeout)
	defer cancel()

	receipt, err := bind.WaitMined(waitCtx, client.EthClient(), tx)
	if err != nil {
		return "", fmt.Errorf("esperar confirmacion de transaccion %s: %w", tx.Hash().Hex(), err)
	}

	if receipt.Status == 0 {
		alreadyRegistered, checkErr := isTicketRegistered(ctx, client, hash32)
		if checkErr == nil && alreadyRegistered {
			log.Printf("hash de boleto ya registrado tras reintento, hash=%s txHash=%s", hashHex, tx.Hash().Hex())
			return tx.Hash().Hex(), nil
		}
		return "", fmt.Errorf("transaccion revertida: %s", tx.Hash().Hex())
	}

	txHash := receipt.TxHash.Hex()
	log.Printf("boleto certificado en blockchain, txHash=%s hash=%s", txHash, hashHex)
	return txHash, nil
}

func isTicketRegistered(ctx context.Context, client *Client, hash32 [32]byte) (bool, error) {
	var results []interface{}
	err := client.Contract().Call(&bind.CallOpts{Context: ctx}, &results, "isTicketRegistered", hash32)
	if err != nil {
		return false, err
	}
	if len(results) == 0 {
		return false, nil
	}
	registered, ok := results[0].(bool)
	if !ok {
		return false, fmt.Errorf("respuesta inesperada de isTicketRegistered")
	}
	return registered, nil
}

func hashHexToBytes32(hashHex string) ([32]byte, error) {
	normalized := strings.TrimPrefix(strings.ToLower(hashHex), "0x")
	if len(normalized) != 64 {
		return [32]byte{}, fmt.Errorf("hash SHA-256 invalido: se esperaban 64 caracteres hex, recibidos %d", len(normalized))
	}

	decoded, err := hex.DecodeString(normalized)
	if err != nil {
		return [32]byte{}, fmt.Errorf("decodificar hash hex: %w", err)
	}

	var out [32]byte
	copy(out[:], decoded)
	return out, nil
}
