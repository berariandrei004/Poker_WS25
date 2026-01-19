package at.ac.hcw.Server;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;


public class MainPokerServer {

    private final int port;
    private final int maxClients;
    private final int startingCash;
    private final String lobbyId;

    private final List<ClientHandler> clients =
            Collections.synchronizedList(new ArrayList<>());

    private final Game game;
    private final ExecutorService threadPool;

    public MainPokerServer(
            int port,
            int maxClients,
            String lobbyId,
            int smallBlind,
            int bigBlind,
            int startingCash
    ) {
        this.port = port;
        this.maxClients = maxClients;
        this.lobbyId = lobbyId;
        this.startingCash = startingCash;

        Player[] players = new Player[maxClients];
        this.game = new Game(smallBlind, bigBlind, players);

        this.threadPool = Executors.newFixedThreadPool(maxClients);

        System.out.println("=== Poker Server gestartet ===");
        System.out.println("Lobby-ID: " + lobbyId);
        System.out.println("Port: " + port);
        System.out.println("Max Clients: " + maxClients);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Server wird beendet...");
                threadPool.shutdownNow();
            }));

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();

                if (clients.size() >= maxClients) {
                    clientSocket.close();
                    continue;
                }

                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                threadPool.execute(handler);
            }
        }
    }

    /* =======================
       Server API für Clients
       ======================= */

    public Game getGame() {
        return game;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public int getStartingCash() {
        return startingCash;
    }

    public void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client entfernt. Aktive Clients: " + clients.size());
    }

    /* =======================
       main()
       ======================= */

    public static void main(String[] args) {

        if (args.length < 5) {
            System.out.println(
                    "Usage: java MainPokerServer <maxClients> <lobbyId> <bigBlind> <smallBlind> <startingCash>"
            );
            return;
        }

        try {
            int maxClients = Integer.parseInt(args[0]);
            String lobbyId = args[1];
            int bigBlind = Integer.parseInt(args[2]);
            int smallBlind = Integer.parseInt(args[3]);
            int startingCash = Integer.parseInt(args[4]);

            int port = 5000; // bewusst fix: ein Server = ein Spiel

            MainPokerServer server = new MainPokerServer(
                    port,
                    maxClients,
                    lobbyId,
                    smallBlind,
                    bigBlind,
                    startingCash
            );

            server.start();

        } catch (Exception e) {
            System.out.println("Server konnte nicht gestartet werden:");
            e.printStackTrace();
        }
    }
}