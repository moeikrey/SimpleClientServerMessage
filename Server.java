import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Server {
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            var threadPool = Executors.newCachedThreadPool();
            System.out.println("Server is listening on port 12345");

            while (true) {
                Socket socket = serverSocket.accept(); // Blocks until login 
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                threadPool.execute(clientHandler);
                System.out.println("Adding Client");
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to broadcast a message to all clients
    public static void broadcast(Message message) {
        for (ClientHandler client : clients) {
            if(client.username == message.getUsername())
                continue;
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private ObjectOutputStream out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                out = new ObjectOutputStream(socket.getOutputStream());

                // Receive login message with username
                Message loginMessage = (Message) in.readObject();
                if ("login".equals(loginMessage.getType())) {
                    username = loginMessage.getUsername();
                    loginMessage.setStatus("success");
                    out.writeObject(loginMessage);
                    out.flush();
                    System.out.println(username + " has logged in.");

                    // Notify other clients of new user login
                    Message userJoinedMessage = new Message("text");
                    userJoinedMessage.setUsername("Server");
                    userJoinedMessage.setText(username + " has joined the chat!");
                    broadcast(userJoinedMessage);

                    // Handle text and logout messages
                    while (true) {
                        Message message = (Message) in.readObject();
                        message.setUsername(username);
                        if ("text".equals(message.getType())) {
                            broadcast(message);
                        } else if ("logout".equals(message.getType())) {
                            message.setStatus("success");
                            out.writeObject(message);
                            out.flush();
                            break;
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                System.out.println(username + " has logged out.");

                // Notify other clients of user logout
                Message userLeftMessage = new Message("text");
                userLeftMessage.setUsername("Server");
                userLeftMessage.setText(username + " has left the chat.");
                broadcast(userLeftMessage);
            }
        }

        public void sendMessage(Message message) throws IOException {
            out.writeObject(message);
            out.flush();
        }
    }
}
