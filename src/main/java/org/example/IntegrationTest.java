package org.example;
import org.junit.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import static org.junit.Assert.*;

public class IntegrationTest {

    private static Thread serverThread;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        serverThread = new Thread(() -> { // Start the server in a separate thread
            try {
                AggregationServer.main(new String[]{"4567"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
        Thread.sleep(2000); // Allow the server some time to start up
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        serverThread.interrupt();
    }

    @Test
    public void testPutRequest() throws Exception {
        URL url = new URL("http://localhost:4567"); //Simulate PUT Request
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Lamport-Clock", "1");
        connection.setDoOutput(true);


        String jsonInput = "{\"temperature\": \"25\", \"humidity\": \"50%\"}"; // Send JSON data
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }


        int responseCode = connection.getResponseCode(); // Verify the response code
        assertEquals(HttpURLConnection.HTTP_CREATED, responseCode);


        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) { // Verify the response message
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            assertEquals("Success", response.toString());
        }


        String lamportClock = connection.getHeaderField("X-Lamport-Clock"); // Verify that Lamport Clock has been updated in the response header
        assertNotNull(lamportClock);
        assertTrue(Integer.parseInt(lamportClock) >= 1);
    }

    @Test
    public void testGetRequest() throws Exception {

        URL url = new URL("http://localhost:4567"); // Simulate a GET request
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Lamport-Clock", "1");


        int responseCode = connection.getResponseCode(); // Verify the response code
        assertEquals(HttpURLConnection.HTTP_OK, responseCode);


        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) { // Read the response JSON
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            assertTrue(response.toString().contains("\"temperature\":")); // Verify the JSON structure
            assertTrue(response.toString().contains("\"humidity\":"));
        }

        String lamportClock = connection.getHeaderField("X-Lamport-Clock"); // Verify that Lamport Clock has been updated in the response header
        assertNotNull(lamportClock);
        assertTrue(Integer.parseInt(lamportClock) >= 1);
    }

}
