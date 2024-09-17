package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class LamportReplicaServer {
    private static int lamportClock = 0;
    private static List<Socket> replicaSockets = new ArrayList<>(); // List to store replica connections
    private static int replicaPort = 4568; // Port for replica communication

    public static void main(String[] args) {
        int port = 4567; // Default port


        if (args.length == 1) {
            replicaPort = Integer.parseInt(args[0]);
        }


        new Thread(() -> startReplicaServer(replicaPort)).start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("New client connected");
                    handleClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String receivedMessage = input.readLine();
            if (receivedMessage != null) {
                String[] parts = receivedMessage.split(":");
                String message = parts[0];
                int receivedClock = Integer.parseInt(parts[1]);


                synchronized (LamportReplicaServer.class) {
                    lamportClock = Math.max(lamportClock, receivedClock) + 1;
                }


                broadcastClockUpdate(lamportClock);


                System.out.println("Received message: " + message);
                System.out.println("Updated server Lamport clock: " + lamportClock);


                output.println("Response:" + lamportClock);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startReplicaServer(int port) {
        try (ServerSocket replicaServerSocket = new ServerSocket(port)) {
            System.out.println("Replica server is listening on port " + port);

            while (true) {
                try (Socket replicaSocket = replicaServerSocket.accept()) {
                    System.out.println("New replica connected");
                    replicaSockets.add(replicaSocket);
                    new Thread(() -> handleReplica(replicaSocket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleReplica(Socket replicaSocket) {
        try (
                BufferedReader input = new BufferedReader(new InputStreamReader(replicaSocket.getInputStream()));
        ) {
            String receivedMessage;
            while ((receivedMessage = input.readLine()) != null) {
                String[] parts = receivedMessage.split(":");
                int receivedClock = Integer.parseInt(parts[1]);


                synchronized (LamportReplicaServer.class) {
                    lamportClock = Math.max(lamportClock, receivedClock) + 1;
                }
                System.out.println("Updated server Lamport clock from replica: " + lamportClock);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastClockUpdate(int clock) {
        for (Socket socket : replicaSockets) {
            try {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                output.println("ClockUpdate:" + clock);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
