package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class LamportServer {
    private static int lamportClock = 0;

    public static void main(String[] args) {
        int port = 4567;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");


                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {

                String receivedMessage = input.readLine();
                String[] parts = receivedMessage.split(":");
                String message = parts[0];
                int receivedClock = Integer.parseInt(parts[1]);


                synchronized (LamportServer.class) {
                    lamportClock = Math.max(lamportClock, receivedClock) + 1;
                }


                System.out.println("Received message: " + message);
                System.out.println("Updated server Lamport clock: " + lamportClock);


                output.println("Response:" + lamportClock);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
