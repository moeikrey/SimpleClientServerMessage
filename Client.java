/*
 * Author: Andreas Sotiras
 * 11/09/24
 * Clientside application.
 */
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) { // Automatically close Scanner with try-with-resources

            // Prompt user for server IP address
            System.out.print("Enter server IP address (e.g., 127.0.0.1 for localhost): ");
            String serverIp = scanner.nextLine();
            if(serverIp == ""){
                serverIp = "localhost";
            }

            // Prompt user for a username
            System.out.print("Enter a username: ");
            String username = scanner.nextLine();

            // Connect to the server using the provided IP address
            try (Socket socket = new Socket(serverIp, 12345);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // Send login message with username
                Message loginMessage = new Message("login");
                loginMessage.setUsername(username);
                out.writeObject(loginMessage);
                out.flush();

                // Receive login response
                Message response = (Message) in.readObject();
                if ("success".equals(response.getStatus())) {
                    System.out.println("Login successful as " + username);

                    // Start a thread to listen for incoming messages
                    new Thread(() -> {
                        try {
                            while (true) {
                                Message incomingMessage = (Message) in.readObject();

                                // Clear the prompt line, print the incoming message, and re-display the prompt
                                System.out.print("\033[2K\r" + incomingMessage.getUsername() + ": " + incomingMessage.getText() + "\n" + username + ": ");
                                System.out.flush();
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            System.out.println("Connection closed.");
                        }
                    }).start();

                    // Initial prompt for user input
                    System.out.print(username + ": ");

                    // Main loop for sending messages
                    while (true) {
                        String input = scanner.nextLine();

                        if ("logout".equalsIgnoreCase(input)) {
                            Message logoutMessage = new Message("logout");
                            out.writeObject(logoutMessage);
                            out.flush();
                            response = (Message) in.readObject();
                            if ("success".equals(response.getStatus())) {
                                System.out.println("Logout successful.");
                                break;
                            }
                        } else {
                            // Clear the previous line before sending
                            // System.out.print("\033[2K\r");

                            // Send text message
                            Message textMessage = new Message("text");
                            textMessage.setText(input);
                            textMessage.setUsername(username);
                            
                            out.writeObject(textMessage);
                            out.flush();
                        }

                        // Re-print prompt after sending message
                        System.out.print(username + ": ");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error: Unable to connect to server at " + serverIp);
            }
        }
    }
}
