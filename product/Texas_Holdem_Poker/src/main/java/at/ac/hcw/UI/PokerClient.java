package at.ac.hcw.UI;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

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
            System.out.println("Verbindung erfolgreich zu " + serverIP + ":" + serverPort);
            return true;
        } catch (IOException e) {
            System.out.println("Verbindung fehlgeschlagen zu " + serverIP + ":" + serverPort);
            e.printStackTrace();
            return false;
        }
    }
    public void sendMessage(String msg) {
        out.println(msg);
    }
    public String receiveMessage() throws IOException {
        return in.readLine();
    }
    public void disconnect() throws IOException {
        System.out.println("Client disconnects");
        socket.close();
    }

    private void handleMessage(String message) {

        if (message.equals("NEW_ROUND")) {
            if (onNewRound != null) onNewRound.run();
            return;
        }

        if (message.startsWith("FLOP")) {
            if (onFlop != null) onFlop.accept(message.split(" "));
            return;
        }

        if (message.startsWith("TURN")) {
            if (onTurn != null) onTurn.accept(message.split(" ")[1]);
            return;
        }

        if (message.startsWith("RIVER")) {
            if (onRiver != null) onRiver.accept(message.split(" ")[1]);
            return;
        }

        if (listener != null) {
            listener.onServerMessage(message);
        }
    }
}
