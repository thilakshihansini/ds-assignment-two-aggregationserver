package org.example;

import com.google.gson.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ContentServerTest {

    private final String filePath = "./weather_info_test.txt";

    @Before
    public void setUp() throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) { // Create a temporary file for testing
            writer.write("Temperature:25\n");
            writer.write("Humidity:50\n");
            writer.write("Wind:10,Speed\n");
        }
    }

    @After
    public void tearDown() {

        File file = new File(filePath); // Delete the temporary file after the test
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testMainMethodCreatesCorrectJsonObject() { // class for test MainMethodCreates CorrectJsonObject
        JsonObject jsonObject = new JsonObject();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String input;
            while ((input = reader.readLine()) != null) {
                String[] parts = input.split(":");
                if (parts.length == 2 || parts.length == 3) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if (parts.length == 3) {
                        value = parts[1].trim() + "," + parts[2].trim();
                    }

                    jsonObject.addProperty(key, value);
                }
            }
        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }

        assertEquals("25", jsonObject.get("Temperature").getAsString());
        assertEquals("50", jsonObject.get("Humidity").getAsString());
        assertEquals("10,Speed", jsonObject.get("Wind").getAsString());
    }

    @Test
    public void testSendData() throws Exception {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(201);


        UrlWrapper mockUrlWrapper = mock(UrlWrapper.class); // Mock UrlWrapper
        when(mockUrlWrapper.openConnection()).thenReturn(mockConnection);


        ArgumentCaptor<ByteArrayOutputStream> captor = ArgumentCaptor.forClass(ByteArrayOutputStream.class); // Use ArgumentCaptor to capture the output stream content


        ByteArrayOutputStream outputStreamMock = mock(ByteArrayOutputStream.class); // Mock OutputStream
        when(mockConnection.getOutputStream()).thenReturn(outputStreamMock);


        ContentServer.SendData("localhost", 4567, "{\"Temperature\":\"25\"}");  // Call the method to be tested


        verify(mockConnection).setRequestMethod("PUT"); // Verify the connection setup
        verify(mockConnection).setRequestProperty("Content-Type", "application/json");
        verify(mockConnection).setDoOutput(true);
        verify(mockConnection).getResponseCode();
        verify(mockConnection).disconnect();


        verify(outputStreamMock).write(captor.capture().size()); // Validate the send data
        String sentData = new String(captor.getValue().toByteArray(), "utf-8");
        assertEquals("{\"Temperature\":\"25\"}", sentData);
    }
}
