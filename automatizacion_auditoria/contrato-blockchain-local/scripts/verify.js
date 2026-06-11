const hre = require("hardhat");

async function main() {
  console.log("Verificando conexión...");
  console.log("Red actual:", hre.network.name);
  
  try {
    const [account] = await hre.ethers.getSigners();
    console.log("✅ Cuenta cargada correctamente:", account.address);
    
    const balance = await hre.ethers.provider.getBalance(account.address);
    console.log("✅ Balance de la cuenta:", hre.ethers.formatEther(balance), "ETH");
    
    if (balance === 0n) {
      console.log("⚠️ Advertencia: Tu balance es 0 ETH. Necesitarás Sepolia ETH para desplegar el contrato.");
    } else {
      console.log("🚀 Todo listo. Tienes fondos y conexión para desplegar.");
    }
  } catch (error) {
    console.error("❌ Error de conexión:", error.message);
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
