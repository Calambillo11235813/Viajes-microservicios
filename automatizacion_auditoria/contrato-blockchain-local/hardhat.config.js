require("@nomicfoundation/hardhat-ethers");
require("dotenv").config({ path: '../.env' }); // Cargar variables desde el .env del microservicio

/** @type import('hardhat/config').HardhatUserConfig */
module.exports = {
  solidity: "0.8.19",
  networks: {
    hardhat: {
      chainId: 1337,
      accounts: {
        mnemonic: "test test test test test test test test test test test junk",
      },
    },
    localhost: {
      url: "http://127.0.0.1:8545",
      chainId: 1337,
    },
    sepolia: {
      url: process.env.BLOCKCHAIN_RPC_URL || "",
      accounts: process.env.BLOCKCHAIN_PRIVATE_KEY ? [`0x${process.env.BLOCKCHAIN_PRIVATE_KEY.replace('0x', '')}`] : [],
      chainId: 11155111,
    }
  },
};
