// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract TicketRegistry {
    mapping(bytes32 => bool) public registeredTickets;

    event TicketRegistered(bytes32 indexed ticketHash, address indexed registrant, uint256 timestamp);

    function registerTicket(bytes32 ticketHash) external returns (bool) {
        require(!registeredTickets[ticketHash], "Ticket already registered");
        registeredTickets[ticketHash] = true;
        emit TicketRegistered(ticketHash, msg.sender, block.timestamp);
        return true;
    }

    function isTicketRegistered(bytes32 ticketHash) external view returns (bool) {
        return registeredTickets[ticketHash];
    }
}
