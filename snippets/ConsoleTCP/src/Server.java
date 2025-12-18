import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {
    InetSocketAddress endpoint = new InetSocketAddress("127.0.0.1", 12345);
    private ServerSocket server;
    ArrayList<Socket> sockets = new ArrayList<>();

    public boolean start() {
        try {
            server = new ServerSocket();
            server.bind(endpoint, 20);

            ScheduledExecutorService scheduler =
                    Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate(() -> sendKeepAlive(), 1, 1, TimeUnit.SECONDS);

            Thread thread = new Thread(this);
            thread.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void sendKeepAlive() {
        //System.out.println("keep-alive");
        //sendToAll("\u0007", false);
        sendToAll("\u0000", false);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket client = server.accept();
                onClientConnected(client);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void onClientConnected(Socket client) {
        sockets.add(client);
        Thread thread = new Thread(() -> handleClient(client));
        thread.start();
    }

    private void handleClient(Socket client) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8)
            );

            String line;
            while ((line = reader.readLine()) != null) {

                String message = getRemoteEndpoint(client) + "> " + line;
                System.out.println("Received from " + message);

                sendToAll(message, true);
            }
        } catch (IOException e) {
            System.out.println("IO Exception !!!!!!!!!!!!!!!");
        }

    }

    private void sendToAll(String line, boolean newline) {
        for (Socket s : sockets) {
            sendTo(s, line, newline);
        }
    }

    private String getRemoteEndpoint(Socket socket){
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    private void sendTo(Socket s, String line, boolean newline) {
        try {
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8),
                    true // auto-flush
            );
            if (newline)
                writer.println(line);
            else {
                writer.print(line);
                writer.flush();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
