package at.ac.hcw.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final MainPokerServer server;

    private PrintWriter out;
    private BufferedReader in;

    private String playerName;
    private Player player;

    public ClientHandler(Socket socket, MainPokerServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Lobby-Infos an Client senden
            sendMessage(
                    "LobbySettings:" +
                            server.getLobbyId()
            );

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Client sagt: " + message);

                if (message.startsWith("PlayerName:")) {
                    handlePlayerJoin(message);

                } else if (message.equals("START_GAME")) {
                    handleStartGame();

                } else {
                    handleGameCommand(message);
                }
            }

        } catch (IOException e) {
            System.out.println("Client Verbindung abgebrochen");
        } finally {
            cleanup();
        }
    }

    /* =========================
       Message Handling
       ========================= */

    private void handlePlayerJoin(String message) {
        this.playerName = message.substring(message.indexOf(":") + 1);

        this.player = new Player(
                playerName,
                server.getStartingCash()
        );

        server.getGame().addPlayer(player);
        server.broadcast("PlayerJoined:" + playerName);
    }

    private void handleStartGame() {
        server.getGame().startGame();
        server.broadcast("GameStarted");
    }

    private void handleGameCommand(String message) {
        Player current = server.getGame().getCurrentPlayer();

        if (current != player) {
            sendMessage("ERROR Not your turn");
            return;
        }

        String response = server.getGame().handleCommand(player, message);
        sendMessage(response);
    }

    /* =========================
       Helper
       ========================= */

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
            System.out.println("Server → Client: " + message);
        }
    }

    private void cleanup() {
        System.out.println("Client getrennt: " + playerName);
        server.removeClient(this);
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    public Player getPlayer() {
        return player;
    }

    public void sendPrivateCards(Card c1, Card c2) {
        sendMessage(
                "HAND_CARDS " +
                        c1.toString() + " " +
                        c2.toString()
        );
    }
}