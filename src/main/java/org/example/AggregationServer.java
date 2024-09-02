package org.example;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.JsonObject;

public class AggregationServer {
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
                    System.out.println("Invalid input format");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(jsonObject.toString());
    }
}
