package blockchain

import (
	"crypto/sha256"
	"encoding/hex"
)

// CalculateSHA256 calcula el hash SHA-256 del PDF y lo retorna en formato hexadecimal.
func CalculateSHA256(data []byte) (string, error) {
	hash := sha256.Sum256(data)
	return hex.EncodeToString(hash[:]), nil
}
