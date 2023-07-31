package httpchat;

import java.io.*;
import java.net.Socket;

public class HttpSender {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private static final int BUFFER_SIZE = 4096;

    public HttpSender(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendCommand(int command, String content) throws IOException {
        String request = "POST /command HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + (content.length() + Integer.BYTES) + "\r\n" +  // Adding the size of the command
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                command +
                content;

        writer.print(request);
        writer.flush();

        // Read the response from the receiver
        String responseLine;
        while ((responseLine = reader.readLine()) != null && !responseLine.isEmpty()) {
            System.out.println(responseLine);
        }
    }

    public void sendFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[BUFFER_SIZE];
        StringBuilder fileContent = new StringBuilder();
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            fileContent.append(new String(buffer, 0, bytesRead));
        }

        fis.close();

        sendCommand(2, file.getName() + "\n" + file.length() + "\n" + fileContent);
    }

    public void sendMessage(String message) throws IOException {
        sendCommand(1, message);
    }

    public void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }

    public static void main(String[] args) {
        try {
            HttpSender sender = new HttpSender("localhost", 1234);
            sender.sendMessage("Hello!");
            sender.sendFile("C:\\JavaProjects\\chatclient\\src\\chatclient\\test.zip");
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
