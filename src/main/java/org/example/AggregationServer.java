package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AggregationServer {

    private static int lamportClock = 0; // Initialize Lamport clock

    public static void main(String[] args) {
        int port;


        if (args.length == 0) { // Validate port number
            port = 4567;
        } else if (args.length == 1) {
            port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) {
                System.out.println("Invalid port number");
                return;
            }
        } else {
            System.err.println("Invalid arguments");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept incoming client socket connections
                System.out.println("New client connected");
                new Thread(new ClientHandler(clientSocket)).start();// Start a new thread to handle the client connection
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static class ClientHandler implements Runnable {// Create inner class to handle client requests
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            handleClient(clientSocket);
        }

        private void handleClient(Socket clientSocket) {
            try (
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
            ) {
                // Read the request line
                String requestLine = input.readLine();
                System.out.println("Received request: " + requestLine);

                String line;
                StringBuilder body = new StringBuilder();
                int clientLamportClock = 0; // Initialize the client's Lamport clock


                while ((line = input.readLine()) != null && !line.isEmpty()) { // Read headers to extract the client's Lamport clock
                    if (line.startsWith("X-Lamport-Clock: ")) {
                        clientLamportClock = Integer.parseInt(line.substring("X-Lamport-Clock: ".length()));
                    }
                    System.out.println("Header: " + line);
                }


                while (input.ready()) {// Read the body of the request
                    body.append((char) input.read());
                }

                System.out.println("Received JSON body: " + body);

                String httpResponse;

                if (requestLine != null && requestLine.startsWith("PUT")) {

                    lamportClock = Math.max(lamportClock, clientLamportClock) + 1;// Update Lamport clock

                    if (isValidJson(body.toString())) {
                        WriteToFile("weatherInfo.json", body.toString());
                        httpResponse = "HTTP/1.1 201 Created\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 7\r\n" +
                                "X-Lamport-Clock: " + lamportClock + "\r\n" +
                                "\r\n" +
                                "Success";
                    } else {
                        httpResponse = "HTTP/1.1 500 Internal Server Error\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 21\r\n" +
                                "X-Lamport-Clock: " + lamportClock + "\r\n" +
                                "\r\n" +
                                "Internal server error";
                    }

                    output.write(httpResponse);
                    output.flush();

                } else if (requestLine != null && requestLine.startsWith("GET")) {
                    String jsonResponse = ReadFromFile("weatherInfo.json");
                    output.write("HTTP/1.1 200 OK\r\n");
                    output.write("Content-Type: application/json\r\n");
                    output.write("Content-Length: " + jsonResponse.length() + "\r\n");
                    output.write("X-Lamport-Clock: " + lamportClock + "\r\n");
                    output.write("\r\n"); // End of headers
                    output.write(jsonResponse); // Write the JSON data
                    output.flush();

                } else {

                    String badRequestResponse = "HTTP/1.1 400 Bad Request\r\n" + // Respond with 400 Bad Request, if the request is not a PUT or GET
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: 11\r\n" +
                            "X-Lamport-Clock: " + lamportClock + "\r\n" +
                            "\r\n" +
                            "Bad Request";

                    output.write(badRequestResponse);
                    output.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public static void WriteToFile(String fileName, String content) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(content);  // Write the content to the file
                System.out.println("File written successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static String ReadFromFile(String fileName) {
            String jsonString = "";

            try (FileReader reader = new FileReader(fileName)) {
                Gson gson = new Gson();
                JsonElement jsonElement = JsonParser.parseReader(reader);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                jsonString = gson.toJson(jsonObject);
            } catch (IOException e) {
                jsonString = "Error reading the JSON file";
            }

            return jsonString;
        }

        public static boolean isValidJson(String jsonString) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(jsonString);
                return true; // Return true for valid JSON
            } catch (Exception e) {
                return false; // Return false for invalid JSON
            }
        }

        public static boolean isJsonEmpty(String jsonString) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(jsonString);
                return jsonNode.isObject() && jsonNode.size() == 0;
            } catch (Exception e) {
                System.out.println("Invalid JSON: " + e.getMessage());
                return false; // Treat invalid JSON as not empty
            }
        }
    }
}
