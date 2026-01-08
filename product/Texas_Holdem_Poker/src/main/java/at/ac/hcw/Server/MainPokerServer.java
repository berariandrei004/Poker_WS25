package at.ac.hcw.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainPokerServer {
    private static final int DEFAULT_PORT = 5000;
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static String lobbyId;
    public static void main(String[] args) {
        int maxClients;
        int port = DEFAULT_PORT;


        try {
            maxClients = Integer.parseInt(args[0]);

            if (args.length >= 2) {
                lobbyId = args[1];
            }

        } catch (NumberFormatException e) {
            System.out.println("Ungültige Argumente!");
            return;
        }

        System.out.println("Server startet...");
        System.out.println("Max Clients: " + maxClients);
        System.out.println("Port: " + port);

        ExecutorService threadPool = Executors.newFixedThreadPool(maxClients);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Server wird beendet...");
                threadPool.shutdownNow();
            }));

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                // Client zur Liste hinzufügen
                clients.add(handler);
                threadPool.execute(handler);
            }

        } catch (IOException e) {
            System.out.println("Server gestoppt.");
            System.out.println(e);
        }
    }
    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
    public static String getLobbyId() {
        return lobbyId;
    }
}