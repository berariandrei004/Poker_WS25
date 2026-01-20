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
    private Player player;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            // System.out.println("Server sendet an " + playerName + ": " + message); // Debug
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Initialer Handshake
            sendMessage("LobbySettings:" + MainPokerServer.getLobbyId() + ";" + MainPokerServer.getBigBlind() + ";" +
                    MainPokerServer.getSmallBlind() + ";" + MainPokerServer.getStartingCash() + ";" + MainPokerServer.getMaxClients());
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Server empfängt von " + playerName + ": " + message);

                if (message.startsWith("PlayerName:")) {
                    this.playerName = message.split(":")[1];
                    // Player Objekt erstellen und dem Game hinzufügen
                    this.player = new Player(playerName, MainPokerServer.getStartingCash());

                    MainPokerServer.getGame().addPlayer(this.player);
                    MainPokerServer.getGame().addListener(this); // WICHTIG!

                    MainPokerServer.broadcast("PlayerJoined:" + this.playerName);
                    sendPlayerListToThisClient();

                } else if (message.equals("StartGame")) {
                    MainPokerServer.broadcast("GameStarted");
                    MainPokerServer.getGame().startNewRound();

                } else {
                    // Alle Spielbefehle (FOLD, CHECK, RAISE etc.)
                    // werden direkt an das Game delegiert
                    if (MainPokerServer.getGame() != null && this.player != null) {
                        String response = MainPokerServer.getGame().handleCommand(this.player, message);
                        // Optional: Bestätigung an Client zurück, aber UI reagiert meist auf Broadcasts
                        if(response.startsWith("ERROR")) {
                            sendMessage(response);
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Verbindung zu " + playerName + " verloren.");
        } finally {
            MainPokerServer.removeClient(this);
            //MainPokerServer.getGame().removeListener(this); // Listener entfernen
            try { socket.close(); } catch (IOException e) {}
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
}
