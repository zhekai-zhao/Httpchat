package httpchat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpReceiver {
    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream inputStream;
    private static final int BUFFER_SIZE = 4096;

    public HttpReceiver(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.socket = serverSocket.accept();
        this.inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void listenForMessages() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while (true) {
                line = reader.readLine();
                if (line == null) break;  // End of stream

                if (line.startsWith("GET")) {
                    String path = line.split(" ")[1];

                    if ("/hello".equals(path)) {
                        System.out.println("Connection message: Hello from Sender!");
                    } else if ("/goodbye".equals(path)) {
                        System.out.println("Termination message: Goodbye from Sender!");
                        return;  // Exit the loop on receiving termination command
                    }

                    // Skip rest of the headers
                    while (!"".equals(line = reader.readLine()));

                } else if (line.startsWith("POST")) {
                    // Skip all header lines until the blank line
                    while (!"".equals(line = reader.readLine()));

                    int command = inputStream.readInt();

                    switch (command) {
                        case 1:  // This is a message
                            String message = inputStream.readUTF();
                            System.out.println("Received message: " + message);
                            break;
                        case 2:  // This is a file
                            receiveFile();
                            System.out.println("File received.");
                            break;
                        default:
                            System.err.println("Unknown command received: " + command);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred while reading from the socket: " + e.getMessage());
        }
    }



    public void receiveFile() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String fileDetails = reader.readLine();

        String[] details = fileDetails.split(":");
        String fileName = details[1];
        long fileSize = Long.parseLong(details[2]);

        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] buffer = new byte[BUFFER_SIZE];
        long remaining = fileSize;

        while (remaining > 0) {
            int bytesRead = inputStream.read(buffer, 0, (int)Math.min(BUFFER_SIZE, remaining));
            fos.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }

        fos.close();
    }



    public void close() throws IOException {
        inputStream.close();
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
