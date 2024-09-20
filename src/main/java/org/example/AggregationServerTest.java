package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AggregationServerTest {

    private Socket mockSocket;
    private BufferedReader mockReader;
    private BufferedWriter mockWriter;
    private AggregationServer.ClientHandler clientHandler;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() throws Exception { //Making setup

        mockSocket = mock(Socket.class); // Mocking socket, reader, and writer
        mockReader = mock(BufferedReader.class);
        mockWriter = mock(BufferedWriter.class);


        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes())); // Mocking input and output streams
        outputStream = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        clientHandler = new AggregationServer.ClientHandler(mockSocket); // Initializing the ClientHandler with the mocked socket
    }

    @Test
    public void testHandleGETRequestSuccess() throws Exception {

        when(mockReader.readLine()) // Setting up a valid GET request for verifications
                .thenReturn("GET / HTTP/1.1")
                .thenReturn("X-Lamport-Clock: 5")
                .thenReturn("")
                .thenReturn(null);


        AggregationServer.ClientHandler.WriteToFile("weatherInfo.json", "{\"temp\": 25}"); // Preparing mock response data


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // Capturing the output using a ByteArrayOutputStream
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(outputStream));

        clientHandler.handleClient(mockSocket); // Execute the client handler logic


        String response = outputStream.toString();
        assertTrue(response.contains("HTTP/1.1 200 OK")); // Asserting that the response contains a 200 OK and the correct JSON data
        assertTrue(response.contains("{\"temp\": 25}"));
    }

    @Test
    public void testHandlePUTRequestWithValidJson() throws Exception {

        when(mockReader.readLine()) // Mocking the reader to simulate a valid PUT request
                .thenReturn("PUT / HTTP/1.1")
                .thenReturn("X-Lamport-Clock: 5")
                .thenReturn("")
                .thenReturn("{\"temp\": 25}")
                .thenReturn(null);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // Capturing the output using a ByteArrayOutputStream
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(outputStream));


        clientHandler.handleClient(mockSocket); // Execute the client handler logic


        String response = outputStream.toString(); // Asserting the response contains a 201 Created status
        assertTrue(response.contains("HTTP/1.1 201 Created"));
    }

    @Test
    public void testHandlePUTRequestWithInvalidJson() throws Exception {

        when(mockReader.readLine()) // Mocking the reader to simulate an invalid PUT request
                .thenReturn("PUT / HTTP/1.1")
                .thenReturn("X-Lamport-Clock: 5")
                .thenReturn("")
                .thenReturn("{invalid json}")
                .thenReturn(null);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // Capturing the output using a ByteArrayOutputStream
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(outputStream));


        clientHandler.handleClient(mockSocket); // Execute the client handler logic


        String response = outputStream.toString(); // Asserting that the response contains a 500 Internal Server Error status
        assertTrue(response.contains("HTTP/1.1 500 Internal Server Error"));
    }

    @Test
    public void testWriteToFile() throws IOException {

        String fileName = "test.json"; // Test writing to a file
        String content = "{\"name\":\"test\"}";
        AggregationServer.ClientHandler.WriteToFile(fileName, content);


        String readContent = AggregationServer.ClientHandler.ReadFromFile(fileName); // Reading the file to assert its content
        assertEquals(content, readContent);
    }

    @Test
    public void testReadFromFileNotFound() {
        String content = AggregationServer.ClientHandler.ReadFromFile("nonexistent.json"); // Test reading from a non-existing file
        assertEquals("Error reading the JSON file", content);
    }

    @Test
    public void testIsValidJson() {
        String validJson = "{\"key\":\"value\"}"; // Test for valid JSON string
        assertTrue(AggregationServer.ClientHandler.isValidJson(validJson));


        String invalidJson = "{invalid json}"; // Test for invalid JSON string
        assertTrue(!AggregationServer.ClientHandler.isValidJson(invalidJson));
    }
}
