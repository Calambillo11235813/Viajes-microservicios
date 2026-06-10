package blockchain

import (
	"context"
	"fmt"
	"math/big"
	"strings"

	"github.com/ethereum/go-ethereum/accounts/abi"
	"github.com/ethereum/go-ethereum/accounts/abi/bind"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/crypto"
	"github.com/ethereum/go-ethereum/ethclient"

	appconfig "automatizacion_auditoria/config"
)

const ticketRegistryABI = `[
	{"inputs":[{"internalType":"bytes32","name":"ticketHash","type":"bytes32"}],"name":"registerTicket","outputs":[{"internalType":"bool","name":"","type":"bool"}],"stateMutability":"nonpayable","type":"function"},
	{"inputs":[{"internalType":"bytes32","name":"ticketHash","type":"bytes32"}],"name":"isTicketRegistered","outputs":[{"internalType":"bool","name":"","type":"bool"}],"stateMutability":"view","type":"function"}
]`

// Client encapsula la conexión RPC y el contrato de registro de boletos.
type Client struct {
	ethClient *ethclient.Client
	contract  *bind.BoundContract
	chainID   *big.Int
}

// NewClient establece conexión con la red EVM y vincula el smart contract configurado.
func NewClient(ctx context.Context) (*Client, error) {
	cfg := appconfig.Load()

	if cfg.BlockchainRPCURL == "" {
		return nil, fmt.Errorf("BLOCKCHAIN_RPC_URL no configurado")
	}
	if cfg.SmartContractAddress == "" {
		return nil, fmt.Errorf("SMART_CONTRACT_ADDRESS no configurado")
	}
	if cfg.BlockchainChainID == 0 {
		return nil, fmt.Errorf("BLOCKCHAIN_CHAIN_ID no configurado")
	}

	ethClient, err := ethclient.DialContext(ctx, cfg.BlockchainRPCURL)
	if err != nil {
		return nil, fmt.Errorf("conectar a nodo blockchain: %w", err)
	}

	parsedABI, err := abi.JSON(strings.NewReader(ticketRegistryABI))
	if err != nil {
		ethClient.Close()
		return nil, fmt.Errorf("parsear ABI del contrato: %w", err)
	}

	contractAddr := common.HexToAddress(cfg.SmartContractAddress)
	boundContract := bind.NewBoundContract(contractAddr, parsedABI, ethClient, ethClient, ethClient)

	return &Client{
		ethClient: ethClient,
		contract:  boundContract,
		chainID:   big.NewInt(cfg.BlockchainChainID),
	}, nil
}

// Close libera la conexión RPC.
func (c *Client) Close() {
	if c.ethClient != nil {
		c.ethClient.Close()
	}
}

// NewTransactOpts crea las opciones de firma a partir de la clave privada configurada.
func (c *Client) NewTransactOpts(ctx context.Context) (*bind.TransactOpts, error) {
	cfg := appconfig.Load()

	if cfg.BlockchainPrivateKey == "" {
		return nil, fmt.Errorf("BLOCKCHAIN_PRIVATE_KEY no configurado")
	}

	privateKeyHex := strings.TrimPrefix(cfg.BlockchainPrivateKey, "0x")
	privateKey, err := crypto.HexToECDSA(privateKeyHex)
	if err != nil {
		return nil, fmt.Errorf("parsear clave privada: %w", err)
	}

	auth, err := bind.NewKeyedTransactorWithChainID(privateKey, c.chainID)
	if err != nil {
		return nil, fmt.Errorf("crear transactor: %w", err)
	}
	auth.Context = ctx

	return auth, nil
}

// EthClient expone el cliente subyacente para esperar confirmaciones.
func (c *Client) EthClient() *ethclient.Client {
	return c.ethClient
}

// Contract expone el contrato vinculado.
func (c *Client) Contract() *bind.BoundContract {
	return c.contract
}
