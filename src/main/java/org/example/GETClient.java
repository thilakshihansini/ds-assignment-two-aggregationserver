package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GETClient {
    private int lamportClock = 0; // Make lamportClock non-static for better testing purpose


    public GETClient() { // Constructor
        this.lamportClock = 0;
    }


    public int getLamportClock() { // Getter for lamportClock - allow testing of lamport clock value
        return lamportClock;
    }


    public void performGetRequest(String urlString) { // Method to perform the GET request
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Lamport-Clock", String.valueOf(lamportClock));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    System.out.println("Response: " + response.toString());
                    String headerLamportClock = connection.getHeaderField("X-Lamport-Clock");
                    if (headerLamportClock != null) {
                        lamportClock = Math.max(lamportClock, Integer.parseInt(headerLamportClock)) + 1;
                    }

                    // System.out.println("Updated Lamport Clock: " + lamportClock);
                }
            } else {
                System.out.println("GET request failed with response code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error during GET request: " + e.getMessage());
        }
    }


    public static void main(String[] args) { // Main method to creates an instance and calls the request method
        GETClient client = new GETClient();
        client.performGetRequest("http://localhost:4567");
        int port;
        if (args.length == 0) { // check the command line arguments
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
    }
}
