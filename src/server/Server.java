package server;

import java.io.*;

import util.Log;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws UnknownHostException {
        Log logger = initLogger();

        var ip = getIp();
        var port = getPort();
        logger.ok("Server is running on " + ip + ":" + port);

        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                executor.execute(new ServerThread(socket, logger));
            }
        } catch (IOException e) {
            logger.err("Server exception: " + e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }


    private static Log initLogger() {
        return Log.makeMe(Server.class.getName(), SERVER_LOG, VERBOSE);
    }

    public static final String getIp() throws UnknownHostException {
        return InetAddress.getByName("localhost").getHostAddress();
    }

    public static final int getPort() {
        return 8080;
    }

    // --------------------------------------------------------------------
    private static final boolean VERBOSE = false;
    private static final String SERVER_LOG = "server.log";
}


class ServerThread implements Runnable {
    private final Log logger;

    public ServerThread(Socket socket, Log logger) {
        this.socket = socket;
        this.logger = logger;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String requestMethod = getRequestMethod(in);

            if (requestMethod.equals("GET")) {
                File htmlFile = createFile();
                writeResponse(out, htmlFile);
                if (out.checkError()) {
                    logger.warn("Error writing response to client");
                } else {
                    logger.ok("Server is ready to receive acknowledgement");
                }

            } else if (requestMethod.equals("POST")) {
                getClientAck(in);
            }
        } catch (IOException e) {
            logger.err("Error handling the client: " + e.getMessage(), e);
        } finally {
            try {
                socket.close();
                System.out.println("Client disconnected");
            } catch (IOException e) {
                logger.err("Error closing socket: " + e.getMessage(), e);
            }
        }
    }

    private File createFile() {
        File file = new File(ServerThread.htmlPath);

        logger.ok(file.getAbsolutePath());
        logger.ok(file.exists() ? "File " + file.getName() + " exists" : "html file doesn't exist");

        return file;
    }

    private String getRequestMethod(BufferedReader bf) {
        String method = null;
        try {
            String line;
            while ((line = bf.readLine()) != null) {
                if (line.startsWith("GET") || line.startsWith("POST")) {
                    method = line.split(" ")[0];
                    break;
                }
            }
        } catch (IOException e) {
            logger.err("Error reading request type: " + e.getMessage(), e);
        }
        logger.info("Request type: " + method);

        return method;
    }

    private void writeResponse(PrintWriter out, File htmlFile) {
        writeResponseHeaders(out, htmlFile);
        writeResponseContent(out, htmlFile);
        out.flush();
    }

    private void writeResponseContent(PrintWriter out, File htmlFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(htmlFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.println(line);
            }
            logger.ok("Response sent successfully.");
        } catch (IOException e) {
            logger.err("Error writing response content: " + e.getMessage(), e);
        }
    }

    private void getClientAck(BufferedReader in) {
        // Try to read acknowledgement, but handle disconnection gracefully
        if (socket.isClosed()) {
            logger.warn("Client disconnected before acknowledgement.");
            return;
        }
        try {
            String line = in.readLine();
            logger.info("Received client acknowledge: " + line);
        } catch (

        IOException e) {
            logger.err("Error reading client ack: " + e.getMessage(), e);
        }
    }


    private void writeResponseHeaders(PrintWriter out, File htmlFile) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html; charset=utf-8");
        out.println(("Content-Length: " + htmlFile.length()));
        out.println(); // to indicate end of headers
    }

    // --------------------------------------------------------------------
    private final Socket socket;
    private static final String htmlPath = "./../res/student-info.html";
}
