package org.example;

import java.io.*;
import java.net.Socket;

public class LamportClient {
    private static int lamportClock = 0;

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 4567;

        try (Socket socket = new Socket(serverAddress, port)) {
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            incrementLamportClock();
            String message = "Hello";
            output.println(message + ":" + lamportClock);


            String response = input.readLine();
            String[] responseParts = response.split(":");
            String responseMessage = responseParts[0];
            int receivedClock = Integer.parseInt(responseParts[1]);


            updateLamportClock(receivedClock);


            System.out.println("Received response: " + responseMessage);
            System.out.println("Updated client Lamport clock: " + lamportClock);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void incrementLamportClock() {
        lamportClock++;
    }

    private static synchronized void updateLamportClock(int receivedClock) {
        lamportClock = Math.max(lamportClock, receivedClock) + 1;
    }
}
