package at.ac.hcw.UI;
import java.io.*;
import java.net.Socket;

public class PokerClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ServerMessageListener listener;
    private String playerName;

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

    public void startListening(ServerMessageListener listener) {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    listener.onServerMessage(msg);
                }
            } catch (IOException e) {
                System.out.println("Verbindung zum Server verloren");
            }
        }).start();
    }
}
