package httpchat;

import java.io.*;
import java.net.Socket;

public class HttpSender {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public HttpSender(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) throws IOException {
        // HTTP POST request message
        String request = "POST /chat HTTP/1.1\r\n" +
                         "Host: localhost\r\n" +
                         "Content-Length: " + message.length() + "\r\n" +
                         "Content-Type: text/plain\r\n" +
                         "\r\n" + 
                         message;
        writer.print(request);
        writer.flush();

        // response
        String responseLine;
        while ((responseLine = reader.readLine()) != null) {
            System.out.println(responseLine);
        }
    }

    public void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }

    public static void main(String[] args) {
        try {
            HttpSender sender = new HttpSender("localhost", 8080);
            sender.sendMessage("Hello, Receiver!");
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
