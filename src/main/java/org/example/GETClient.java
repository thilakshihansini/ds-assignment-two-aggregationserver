package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GETClient {
    private static int lamportClock = 0; // Initialize the client's Lamport clock

    public static void main(String[] args) {
        String urlString = "http://localhost:4567";

        try {

            URL url = new URL(urlString); // Create a URL object with the target URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // Open a connection to the URL
            connection.setRequestMethod("GET"); // Set the request method to GET
            connection.setRequestProperty("X-Lamport-Clock", String.valueOf(lamportClock)); // In the request header include the Lamport clock

            int responseCode = connection.getResponseCode(); // Get the response code
            if (responseCode == HttpURLConnection.HTTP_OK) { // read the response, if response code is HTTP_OK (200)
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    System.out.println("Response: " + response.toString()); // Print the raw response
                    String headerLamportClock = connection.getHeaderField("X-Lamport-Clock");// Update Lamport clock
                    if (headerLamportClock != null) {
                        lamportClock = Math.max(lamportClock, Integer.parseInt(headerLamportClock)) + 1;
                    }
                    System.out.println("Updated Lamport Clock: " + lamportClock);// Print the updated Lamport clock

                }

            } else {
                System.out.println("GET request failed with response code: " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("Error during GET request: " + e.getMessage());
        }
    }
}
