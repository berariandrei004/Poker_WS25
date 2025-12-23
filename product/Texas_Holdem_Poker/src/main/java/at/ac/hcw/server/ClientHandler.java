package at.ac.hcw.server;
import java.io.*;
import java.net.*;
import java.util.*;
public class ClientHandler implements Runnable{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Client sagt: " + msg);
                handleClientMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientMessage(String msg) {
        // Beispiel: Raise / Call / Fold

    }
}

