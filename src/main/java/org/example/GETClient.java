package org.example;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;



public class GETClient {
    public static void main(String[] args) {
        String urlString = "http://localhost:4567/getjson";
//        String urlString = args[0];

        try {
            // Create a URL object with the target URL
            URL url = new URL(urlString);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();

            // If the response code is HTTP_OK (200), read the response
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response from the input stream
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                // Close the input stream
                in.close();

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());

                // Print each key-value pair line by line
                jsonResponse.keys().forEachRemaining(key -> {
                    Object value = jsonResponse.get(key);
                    System.out.println(key + ": " + value);
                });
            } else {
                System.out.println("GET request failed");
            }

        } catch (Exception e) {
            System.err.println("Error during GET request: " + e.getMessage());
        }
    }
}
