package httpchat;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpReceiver {
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private static final int BUFFER_SIZE = 4096;

    public HttpReceiver(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.socket = serverSocket.accept();
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void listenForCommands() throws IOException {
        String requestLine = reader.readLine();
        int contentLength = 0;

        if (requestLine != null && requestLine.startsWith("POST")) {
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            char[] contentChars = new char[contentLength];
            reader.read(contentChars, 0, contentLength);
            String content = new String(contentChars);

            int command = Integer.parseInt(content.substring(0, 1));
            content = content.substring(1); // Remove command

            switch (command) {
                case 1:
                    System.out.println("Received message: " + content);
                    break;

                case 2:
                    receiveFile(content);
                    break;

                default:
                    System.err.println("Unknown command received: " + command);
                    break;
            }

            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Length: 0");
            writer.println();
            writer.flush();
        }
    }

    public void receiveFile(String content) throws IOException {
        String[] fileParts = content.split("\n", 2);  // Split only into two parts
        String fileName = fileParts[0];
        byte[] fileData = fileParts[1].getBytes();

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            
            while ((bytesRead = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("File received: " + fileName);
    }

    public void close() throws IOException {
        writer.close();
        reader.close();
        socket.close();
        serverSocket.close();
    }

    public static void main(String[] args) {
        try {
            HttpReceiver receiver = new HttpReceiver(1234);
            receiver.listenForCommands();
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
