//
// Source file for the server side. 
//
// Created by Sanny Syberfeldt
// Maintained by Marcus Brohede
//

import java.net.*;
import java.util.*;
//import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Server {

    private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
    private DatagramSocket m_socket;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Server portnumber");
            System.exit(-1);
        }

        try {
            Server instance = new Server(Integer.parseInt(args[0]));
            instance.listenForClientMessages();
        } catch (NumberFormatException e) {
            System.err.println("Error: port number must be an integer.");
            System.exit(-1);
        }
    }

    private Server(int portNumber) {
        try {
            m_socket = new DatagramSocket(portNumber);
        } catch (Exception e) {
            System.out.println("Cannot create server socket.");
        }
    }

    private void listenForClientMessages() {
        System.out.println("Waiting for client messages... ");
        
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        do {
            // TODO: Listen for client messages.
            // On reception of message, do the following:
            // * Unmarshal message
            // * Depending on message type, either
            // - Try to create a new ClientConnection using addClient(), send
            // response message to client detailing whether it was successful
            // - Broadcast the message to all connected users using broadcast()
            // - Send a private message to a user using sendPrivateMessage()
            try {
                m_socket.receive(packet);   
            } catch (Exception e) {
                System.out.println("Wasen't able to receive message from client.");
            }
            
            String message = new String(packet.getData(), 0, packet.getLength());
            String[] messageParts = message.split(" ");
            List<String> messageList = new ArrayList<String>(Arrays.asList(message.split(" ")));
            String[] messageName = message.split(" FROM: ");
            String messageTo = messageList.get(1);

            switch(messageList.get(0)) {
                case "New":
                    // Check if user allready exsits else add person
                    if(addClient(messageList.get(1), packet.getAddress(), packet.getPort())) {
                        System.out.println("New user connected: " + messageList.get(1));
                        sendPrivateMessage("Success", messageTo);
                    } else {
                        System.out.println("Failed to connect user.");  
                        sendUsernameTakenMessage(packet.getAddress(), packet.getPort());
                    }
                    break;
                case "/tell":
                    messageList.remove(messageList.get(0));
                    messageList.remove(messageList.get(0));
                    messageList.remove("FROM:");
                    messageList.remove(messageName[1]);

                    message = String.join(" ", messageList);

                    // Add from name to the message
                    message = messageName[1] + ": " + message;

                    sendPrivateMessage(message, messageTo);
                    sendPrivateMessage(message, messageName[1]);
                    // Get confirmation that the user got the message (Message sent back) else resendTopDownLeftRight()
                    break;
                default:
                    messageList.remove("FROM:");
                    messageList.remove(messageName[1]);
                    message = String.join(" ", messageList);
                    message = messageName[1] + ": " + message;
                    broadcast(message);
                    break;
            }   
        } while (true);
    }

    public boolean addClient(String name, InetAddress address, int port) {
        ClientConnection c;

        for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
            c = itr.next();

            if (c.hasName(name)) {
                return false; // Already exists a client with this name
            }
        }

        m_connectedClients.add(new ClientConnection(name, address, port));
        return true;
    }

    public void sendPrivateMessage(String message, String name) {
        ClientConnection c;

        for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
            c = itr.next();

            if (c.hasName(name)) {
                c.sendMessage(message, m_socket);
            }
        }
    }

    public void broadcast(String message) {
        for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
            itr.next().sendMessage(message, m_socket);
        }
    }

    public void sendUsernameTakenMessage(InetAddress addr, int port) {
        String responseMsg = "UsernameTaken";
        byte[] buffer = responseMsg.getBytes();

        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, addr, port);

        try {
            m_socket.send(responsePacket);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
