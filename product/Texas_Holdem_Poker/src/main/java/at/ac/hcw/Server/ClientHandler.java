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
        }
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Willkommen! Verbindung hergestellt.");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Client sagt: " + message);
                if (message.startsWith("PlayerName:")) {
                    int colonIndex = message.indexOf(":");
                    this.playerName = message.substring(colonIndex + 1);
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
}
