package org.example;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class GETClientTest {

    private GETClient client;
    private HttpURLConnection mockConnection;

    @Before
    public void setUp() throws Exception { //creating setup before testing
        client = new GETClient();
        mockConnection = mock(HttpURLConnection.class);
    }

    @Test
    public void testLamportClockInitialization() {
        assertEquals(0, client.getLamportClock());
    }

    @Test
    public void testPerformGetRequestSuccess() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK); // Simulate a successful response code and provide input stream data
        InputStream mockInputStream = new ByteArrayInputStream("Mock server response".getBytes());
        when(mockConnection.getInputStream()).thenReturn(mockInputStream);
        when(mockConnection.getHeaderField("X-Lamport-Clock")).thenReturn("5");


        simulateHttpConnection(client, mockConnection); // Inject mocked HttpURLConnection


        client.performGetRequest("http://localhost:4567"); // Perform the GET request


        assertEquals(6, client.getLamportClock()); // Check if the Lamport clock was updated correctly - Max(0, 5) + 1 = 6
    }

    @Test
    public void testPerformGetRequestFailure() throws Exception {

        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST); // Simulate a failed response code

        simulateHttpConnection(client, mockConnection);// Inject mocked HttpURLConnection

        client.performGetRequest("http://localhost:4567");  // Perform the GET request

        assertEquals(0, client.getLamportClock()); // Ensure that the lamportClock remains unchanged
    }


    private void simulateHttpConnection(GETClient client, HttpURLConnection connection) throws Exception { // Simulates the HTTP connection process
        HttpURLConnection.setFollowRedirects(false); // Replace the actual HttpURLConnection with the mock object
        when(connection.getRequestMethod()).thenReturn("GET");
        when(connection.getRequestProperty("X-Lamport-Clock")).thenReturn(String.valueOf(client.getLamportClock()));
    }
}

