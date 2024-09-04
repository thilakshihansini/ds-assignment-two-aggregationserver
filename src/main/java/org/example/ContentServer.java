package org.example;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ContentServer {
    public static void main(String[] args) {
        String filePath = "./weather_info.txt";

        JsonObject jsonObject = new JsonObject();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String input;
            while ((input = reader.readLine()) != null) {
                // Split the string into key and value
                String[] parts = input.split(":");
                if (parts.length == 2 || parts.length == 3) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if(parts.length == 3){
                        value = parts[1].trim() + "," + parts[2].trim();
                    }

                    jsonObject.addProperty(key, value);

                    // Print the JSON object

                } else {
                    System.out.println("Invalid data format");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(jsonObject.toString());

        SendData("localhost",4567, jsonObject.toString());
    }

    public static void SendData(String ip, int port, String value) {
        try {
            URL url = new URL("http://"+ip+":"+port);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send JSON data
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = value.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 200) {
                System.out.println("JSON data sent successfully.");
            } else {
                System.out.println("Failed to send JSON data.");
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
