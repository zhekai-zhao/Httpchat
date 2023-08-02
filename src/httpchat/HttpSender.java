package httpchat;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;

public class HttpSender {
    private Socket socket;
    private DataOutputStream outputStream;
    private static final int BUFFER_SIZE = 4096;

    public HttpSender(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
            
        String headers = "POST /sendfile HTTP/1.1\r\n" +
                         "FileName: " + URLEncoder.encode(file.getName(), "UTF-8") + "\r\n" +
                         "Content-Length: " + file.length() + "\r\n\r\n";

        System.out.println("Sending headers for file: " + headers);
        outputStream.writeBytes(headers);

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            System.out.println("Sending " + bytesRead + " bytes of file data...");
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        fis.close();
    }


    public void sendMessage(String message) throws IOException {
        byte[] encodedMessageBytes = message.getBytes("UTF-8");
        
        String headers = "POST /sendmessage HTTP/1.1\r\n" +
                         "Content-Length: " + encodedMessageBytes.length + "\r\n\r\n";

        outputStream.writeBytes(headers);
        outputStream.write(encodedMessageBytes);
        outputStream.flush();
    }


    private void sendHttpRequest(String httpRequest) throws IOException {
        outputStream.writeBytes(httpRequest);
        outputStream.flush();
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
            sender.sendFile("C:\\JavaProjects\\httpchat\\src\\httpchat\\test.zip");
            sender.closeConnection();
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
