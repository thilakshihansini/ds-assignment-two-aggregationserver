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
                String[] parts = input.split(":"); // Split the string into key and value
                if (parts.length == 2 || parts.length == 3) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if(parts.length == 3){
                        value = parts[1].trim() + "," + parts[2].trim();
                    }

                    jsonObject.addProperty(key, value);

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
            //URL url = new URL("http://"+ip+":"+port);
            //HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            UrlWrapper urlWrapper = new UrlWrapper("http://" + ip + ":" + port); // updated for testing purpose
            HttpURLConnection connection = urlWrapper.openConnection(); //add URLWrapper object for testing purpose
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) { // Send JSON data
                byte[] input = value.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode(); // Get response
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 200 || responseCode == 201 ) {
                System.out.println("JSON data sent successfully.");
            } else if (responseCode == 500) {
                System.out.println("Failed to send JSON data.");
                System.out.println("Invalid JSON format Internal server error");
            }
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
