package at.ac.hcw.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainPokerServer {
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar server.jar <maxClients> [port]");
            return;
        }

        int maxClients;
        int port = DEFAULT_PORT;

        try {
            maxClients = Integer.parseInt(args[0]);

            if (args.length >= 2) {
                port = Integer.parseInt(args[1]);
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

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client verbunden: "
                        + clientSocket.getInetAddress());

                threadPool.execute(new ClientHandler(clientSocket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}