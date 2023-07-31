package httpchat;


import java.io.*;
import java.net.Socket;

public class HttpSender {
    private Socket socket;
    private DataOutputStream outputStream;
    private static final int BUFFER_SIZE = 4096;

    public HttpSender(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }
    
    public void sendHttpRequest(String httpRequest) throws IOException {
        outputStream.writeBytes(httpRequest);
        outputStream.flush();
    }


    public void openConnection() throws IOException {
        String httpRequest = "GET /hello HTTP/1.1\r\n\r\n";
        sendHttpRequest(httpRequest);
    }

    public void closeConnection() throws IOException {
        String httpRequest = "GET /goodbye HTTP/1.1\r\n\r\n";
        sendHttpRequest(httpRequest);
    }

    public void sendFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);

        String httpRequest = "POST /file HTTP/1.1\r\n";
        httpRequest += "Content-Length: " + (4 + file.getName().length() + 8 + file.length()) + "\r\n\r\n";
        
        sendHttpRequest(httpRequest);

        outputStream.writeInt(2); // Command 2 indicates this is a file
        outputStream.writeUTF(file.getName());
        outputStream.writeLong(file.length());

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        fis.close();
        outputStream.flush();
    }





    public void sendMessage(String message) throws IOException {
        String httpRequest = "POST /message HTTP/1.1\r\n";
        httpRequest += "Content-Length: " + (4 + message.length() + 2) + "\r\n\r\n";
        httpRequest += "1"; // Command 1 indicates this is a message
        httpRequest += message;
        
        sendHttpRequest(httpRequest);
    }

    public void close() throws IOException {
        outputStream.close();
        socket.close();
    }

    public static void main(String[] args) {
        try {
            HttpSender sender = new HttpSender("localhost", 1234);
            sender.openConnection();
            sender.sendMessage("Hello!");
            sender.sendFile("C:\\JavaProjects\\chatclient\\src\\chatclient\\test.zip"); 
            sender.closeConnection();
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

