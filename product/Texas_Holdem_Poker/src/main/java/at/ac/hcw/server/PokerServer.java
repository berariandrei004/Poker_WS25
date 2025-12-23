package at.ac.hcw.server;
import java.io.*;
import java.net.*;
import java.util.*;

public class PokerServer {
    private int port;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private int maxPlayers;
    private int bigBlind;
    private int smallBlind;
    private int startingCash;

    public PokerServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server läuft auf Port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    public void notifyPlayerTurn(ClientHandler client) {
        client.sendMessage("YOUR_TURN");
    }

}
