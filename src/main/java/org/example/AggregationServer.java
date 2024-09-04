package org.example;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer {
    public static void main(String[] args) {

        int port;

        // The port number validation
        if (args.length == 0) {
            port = 4567;
        } else if (args.length == 1) {
            port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) {
                System.out.println("Invalid port number");
                return;
            }
        }else {
            System.err.println("Invalid arguments");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {

                Socket clientSocket = serverSocket.accept(); // Accept incoming client connections
                System.out.println("New client connected");

                handleClient(clientSocket); // Handle the client connection
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print Exceptions
        }

    }

    // Method to handle the client connection
    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream output = clientSocket.getOutputStream();
        ) {
            // Read the request line
            String requestLine = input.readLine();
            System.out.println("Received request: " + requestLine);

            // Check if the request is a PUT request
            if (requestLine != null && requestLine.startsWith("PUT")) {
                // Read headers and body
                String line;
                StringBuilder body = new StringBuilder();

                // Read headers until an empty line (end of headers)
                while ((line = input.readLine()) != null && !line.isEmpty()) {
                    System.out.println("Header: " + line);
                }

                // Read the body of the request
                while (input.ready()) {
                    body.append((char) input.read());
                }

                System.out.println("Received JSON body: " + body);

                // Respond with a success message
                String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: 7\r\n" +
                        "\r\n" +
                        "Success";

                output.write(httpResponse.getBytes());
                output.flush();
            } else {
                // If the request is not a PUT request, respond with 400 Bad Request
                String badRequestResponse = "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: 11\r\n" +
                        "\r\n" +
                        "Bad Request";

                output.write(badRequestResponse.getBytes());
                output.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the client socket
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
