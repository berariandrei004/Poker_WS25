package at.ac.hcw.UI;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
import javafx.application.Platform;

public class PokerClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ServerMessageListener listener;
    private String playerName;
    private Runnable onNewRound;
    private Consumer<String[]> onFlop;
    private Consumer<String> onTurn;
    private Consumer<String> onRiver;

    private String serverIP;
    private int serverPort;

    public PokerClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public void setMessageListener(ServerMessageListener listener) {
        this.listener = listener;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setOnNewRound(Runnable onNewRound) {
        this.onNewRound = onNewRound;
    }

    public void setOnFlop(Consumer<String[]> c) { onFlop = c; }
    public void setOnTurn(Consumer<String> c) { onTurn = c; }
    public void setOnRiver(Consumer<String> c) { onRiver = c; }

    public boolean connect() {
        try {
            socket = new Socket(serverIP, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            startListening();

            System.out.println("Verbindung erfolgreich zu " + serverIP + ":" + serverPort);
            return true;
        } catch (IOException e) {
            System.out.println("Verbindung fehlgeschlagen zu " + serverIP + ":" + serverPort);
            e.printStackTrace();
            return false;
        }
    }

    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    handleMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Verbindung zum Server verloren");
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void receiveAndHandleMessage() throws IOException {
        String message = in.readLine();
        if (message != null) {
            handleMessage(message);
        }
    }
    public void disconnect() throws IOException {
        System.out.println("Client disconnects");
        socket.close();
    }

    private void handleMessage(String message) {

        if (message.equals("NEW_ROUND")) {
            if (onNewRound != null) {
                Platform.runLater(onNewRound);
            }
            return;
        }

        if (message.startsWith("FLOP")) {
            if (onFlop != null) {
                String[] cards = message.split(" ");
                Platform.runLater(() -> onFlop.accept(cards));
            }
            return;
        }

        if (message.startsWith("TURN")) {
            if (onTurn != null) {
                String card = message.split(" ")[1];
                Platform.runLater(() -> onTurn.accept(card));
            }
            return;
        }

        if (message.startsWith("RIVER")) {
            if (onRiver != null) {
                String card = message.split(" ")[1];
                Platform.runLater(() -> onRiver.accept(card));
            }
            return;
        }

        if (listener != null) {
            Platform.runLater(() -> listener.onServerMessage(message));
        }
    }
}
