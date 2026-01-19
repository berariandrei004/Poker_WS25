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
            System.out.println("Ich server, sende:" + message);
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            sendMessage("LobbySettings:" + MainPokerServer.getLobbyId() + ";" + MainPokerServer.getBigBlind() + ";" +
                    MainPokerServer.getSmallBlind() + ";" + MainPokerServer.getStartingCash() + ";" + MainPokerServer.getMaxClients());

            String message;

            while ((message = in.readLine()) != null) {
                System.out.println("Client sagt: " + message);
                if (message.startsWith("PlayerName:")) {
                    int colonIndex = message.indexOf(":");
                    this.playerName = message.substring(colonIndex + 1);
                    this.player = new Player(
                            playerName,
                            MainPokerServer.getStartingCash()
                    );

                    // 🟢 Player ins Game einfügen
                    MainPokerServer.getGame().addPlayer(player);

                    // 🟢 Client als Listener fürs Game
                    MainPokerServer.getGame().addListener(this);
                    MainPokerServer.broadcast("PlayerJoined:" + this.playerName);
                    sendPlayerListToThisClient();
                } else if (message.equals("StartGame")) {
                    MainPokerServer.getGame().startGame();
                    MainPokerServer.broadcast("GameStarted");
                } else {
                    // 👉 alle anderen Commands
                    Player current = MainPokerServer.getGame().getCurrentPlayer();

                    if (current != this.player) {
                        sendMessage("ERROR Not your turn");
                        continue;
                    }

                    String response = MainPokerServer
                            .getGame()
                            .handleCommand(player, message);

                    sendMessage(response);
                }
            }

        } catch (IOException e) {
            System.out.println("Client verbindung abgebrochen");
        } finally {
            System.out.println("Client hat sich getrennt.");
            MainPokerServer.getGame().removeListener(this);
            MainPokerServer.removeClient(this);
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

    public void sendPrivateCards(Card c1, Card c2) {
        sendMessage("Handcards: " + c1 + " & " + c2);
    }

    private String getPlayerName() {
        return this.playerName;
    }
}
