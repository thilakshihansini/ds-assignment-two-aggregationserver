package org.example;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import java.io.FileReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import java.io.OutputStreamWriter;

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
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            // Read the request line
            String requestLine = input.readLine();
            System.out.println("Received request: " + requestLine);

            String line;
            StringBuilder body = new StringBuilder();

            // Check if the request is a PUT request
            if (requestLine != null && requestLine.startsWith("PUT")) {
                // Read headers and body

                // Read headers until an empty line (end of headers)
                while ((line = input.readLine()) != null && !line.isEmpty()) {
                    System.out.println("Header: " + line);
                }

                // Read the body of the request
                while (input.ready()) {
                    body.append((char) input.read());
                }

                System.out.println("Received JSON body: " + body);
                WriteToFile("weatherInfo.json", body.toString());

                // Respond with a success message
                String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: 7\r\n" +
                        "\r\n" +
                        "Success";

                output.write(httpResponse);
                output.flush();

            }else if (requestLine != null && requestLine.startsWith("GET")) {
                String jsonResponse = ReadFromFile("weatherInfo.json");
                output.write("HTTP/1.1 200 OK\r\n");
                output.write("Content-Type: application/json\r\n");
                output.write("Content-Length: " + jsonResponse.length() + "\r\n");
                output.write("\r\n"); // End of headers
                output.write(jsonResponse); // Write the JSON data
                output.flush();

            }

            else {
                // If the request is not a PUT request, respond with 400 Bad Request
                String badRequestResponse = "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: 11\r\n" +
                        "\r\n" +
                        "Bad Request";

                output.write(badRequestResponse);
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

    public static void WriteToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);  // Write the content to the file
            System.out.println("File written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String ReadFromFile(String fileName) {
        String filePath = fileName;
        String jsonString = "";

        try (FileReader reader = new FileReader(filePath)) {
            // Create a Gson instance
            Gson gson = new Gson();

            // Parse JSON file into a JsonElement
            JsonElement jsonElement = JsonParser.parseReader(reader);

            // Convert JsonElement to JsonObject
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Print the JSON object or access specific elements
//            System.out.println("JSON Object: " + jsonObject);

            // Convert JsonObject to JSON string
            jsonString = gson.toJson(jsonObject);

        } catch (IOException e) {
//            System.err.println("Error reading the JSON file: " + e.getMessage());
            jsonString = "Error reading the JSON file";
        }

        return jsonString;
    }

}
