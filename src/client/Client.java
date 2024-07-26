package client;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import server.Server;
import util.Log;

public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Client started");
        logger = initLogger();

        SERVER_IP = Server.getIp();
        SERVER_PORT = Server.getPort();
        Socket socket;
        logger.ok("Attempting to connect to " + SERVER_IP + ":" + SERVER_PORT);
        try {
            socket = connectToServer();
        } catch (ConnectException e) {
            logger.err("Server is not running. Please run the server first.");
            return;
        }

        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            openInBrowser(SERVER_IP, SERVER_PORT);
            sendAcknowledge(out);
        } catch (IOException e) {
            logger.err("Error handling the client: " + e.getMessage(), e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.err("Error closing the socket: " + e.getMessage(), e);
            }
        }
    }

    private static Log initLogger() {
        return Log.makeMe(Client.class.getName(), CLIENT_LOG, VERBOSE);
    }

    private static String receiveMessage() {
        return "I RECEIVED THE INFORMATION OF STUDENT's PROJECT GROUP";
    }

    private static void openInBrowser(String ip, int port) {
        try {
            logger.ok("Viewing in browser...");
            Desktop.getDesktop().browse(new URI("http://" + ip + ":" + port));
        } catch (IOException | URISyntaxException e) {
            logger.err("Error viewing in browser: " + e.getMessage(), e);
        }
        logger.ok("Viewed in browser successfully!");
    }

    private static Socket connectToServer() throws UnknownHostException, IOException {
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        logger.ok("Connected to the server on " + SERVER_IP + ":" + SERVER_PORT + " successfully!");

        return socket;
    }

    private static void sendAcknowledge(PrintWriter out) {
        out.println("POST /sendAcknowledge HTTP/1.1");
        logger.ok("Sending acknowledge to server...");
        out.println(receiveMessage());
        logger.ok("Acknowledge sent successfully!");
    }

    // --------------------------------------------------------------------
    private static String SERVER_IP;
    private static int SERVER_PORT;
    private final static String CLIENT_LOG = "client.log";
    private final static boolean VERBOSE = true;
    private static Log logger;
}
