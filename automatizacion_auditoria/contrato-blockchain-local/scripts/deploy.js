async function main() {
  const TicketRegistry = await ethers.getContractFactory("TicketRegistry");
  const contract = await TicketRegistry.deploy();
  await contract.waitForDeployment();

  const address = await contract.getAddress();
  console.log("TicketRegistry deployed to:", address);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
