package httpchat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpReceiver {

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public HttpReceiver(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void listenForMessages() {
        try {
            while (true) {
                socket = serverSocket.accept();
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // handle request
                String requestLine = reader.readLine();
                int contentLength = 0;
                if (requestLine != null && requestLine.startsWith("POST")) {
                    String line;
                    //read and handle header
                    while (!(line = reader.readLine()).isEmpty()) {
                        if (line.startsWith("Content-Length:")) {
                            contentLength = Integer.parseInt(line.split(":")[1].trim());
                        }
                    }

                    char[] messageChars = new char[contentLength];
                    reader.read(messageChars, 0, contentLength);
                    String message = new String(messageChars);
                    System.out.println("Received message: " + message);

                    // send response
                    writer.println("HTTP/1.1 200 OK");
                    writer.println("Content-Length: 0");
                    writer.println();
                    writer.flush();
                }
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    public static void main(String[] args) {
        try {
            HttpReceiver receiver = new HttpReceiver(8080);
            receiver.listenForMessages();
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
