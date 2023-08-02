package httpchat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpReceiver {
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream rawInput;
    private static final int BUFFER_SIZE = 4096;

    public HttpReceiver(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.socket = serverSocket.accept();
        this.rawInput = socket.getInputStream();
    }

    public void listenForMessages() throws IOException {
        System.out.println("Listening for messages...");

        while (true) {
            // Read headers
            String headers = readUntilEmptyLine();
            
            if (headers.isEmpty()) {
                System.out.println("Connection closed by client.");
                break;  // Connection closed by client
            }

            System.out.println("Headers received: " + headers);

            if (headers.contains("GET /hello")) {
                System.out.println("Connection message: Hello from Sender!");
            } else if (headers.contains("GET /goodbye")) {
                System.out.println("Termination message: Goodbye from Sender!");
                break;  // We assume the goodbye message is the last one and close the loop
            } else if (headers.contains("POST /sendfile")) {
                // Extracting headers
                Map<String, String> headerMap = extractHeaders(headers);
                int contentLength = Integer.parseInt(headerMap.get("Content-Length"));
                String fileName = headerMap.get("FileName");

                receiveFile(fileName, contentLength);
                System.out.println("File received.");
            } else if (headers.contains("POST /sendmessage")) {
                // Extracting headers
                Map<String, String> headerMap = extractHeaders(headers);
                int contentLength = Integer.parseInt(headerMap.get("Content-Length"));

                String message = readInputString(contentLength);
                System.out.println("Received message: " + message);
            } else {
                System.err.println("Unknown request: " + headers);
            }
        }
    }

    // Helper function to extract headers from the received string
    private Map<String, String> extractHeaders(String headers) {
        Map<String, String> headerMap = new HashMap<>();
        String[] lines = headers.split("\r\n");
        for (String line : lines) {
            String[] parts = line.split(": ", 2);
            if (parts.length == 2) {
                headerMap.put(parts[0], parts[1]);
            }
        }
        return headerMap;
    }


    private String readUntilEmptyLine() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int prev = -1, current;
        while ((current = rawInput.read()) != -1) {
            out.write(current);
            if (prev == '\r' && current == '\n') {
                int next = rawInput.read();
                if (next == '\r') {
                    int next2 = rawInput.read();
                    if (next2 == '\n') break;
                } else {
                    out.write(next);
                }
            }
            prev = current;
        }
        String result = out.toString("UTF-8");
        System.out.println("Headers received: " + result);
        return result;
    }

    private String readInputString(int length) throws IOException {
        byte[] bytes = new byte[length];
        int bytesRead = 0;
        while (bytesRead < length) {
            int result = rawInput.read(bytes, bytesRead, length - bytesRead);
            if (result == -1) break;
            bytesRead += result;
        }
        String result = new String(bytes, "UTF-8");
        System.out.println("Body received: " + result);
        return result;
    }
    
    public void receiveFile(String fileName, long fileSize) throws IOException {
    	System.out.println("Receiving file: " + fileName + " of size: " + fileSize);
        FileOutputStream fos = new FileOutputStream(fileName);
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytesRead = 0;
        while (totalBytesRead < fileSize) {
            int bytesRead = rawInput.read(buffer);
            fos.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
        fos.close();
    }

    public void close() throws IOException {
        rawInput.close();
        socket.close();
        serverSocket.close();
    }

    public static void main(String[] args) {
        try {
            HttpReceiver receiver = new HttpReceiver(1234);
            receiver.listenForMessages();
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
