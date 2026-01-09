package at.ac.hcw.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private String playerName;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("Ich server, sende:" + message);
        }
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            sendMessage("LobbySettings:" + MainPokerServer.getLobbyId() + ";" + MainPokerServer.getBigBlind() + ";" +
                    MainPokerServer.getSmallBlind() + ";" + MainPokerServer.getStartingCash());

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Client sagt: " + message);
                if (message.startsWith("PlayerName:")) {
                    int colonIndex = message.indexOf(":");
                    this.playerName = message.substring(colonIndex + 1);
                    MainPokerServer.broadcast("PlayerJoined:" + this.playerName);
                    sendPlayerListToThisClient();
                }
            }

        } catch (IOException e) {
            System.out.println("Client verbindung abgebrochen");
        } finally {
            System.out.println("Client hat sich getrennt.");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void sendPlayerListToThisClient() {
        synchronized (MainPokerServer.getClients()) {
            StringBuilder sb = new StringBuilder("PlayerList:");
            for (ClientHandler client : MainPokerServer.getClients()) {
                if (client.getPlayerName() != null) {
                    sb.append(client.getPlayerName()).append(";");
                }
            }
            // letztes Komma entfernen
            if (sb.length() > 11) sb.setLength(sb.length() - 1);
            sendMessage(sb.toString());
        }
    }
    private String getPlayerName() {
        return this.playerName;
    }
}
