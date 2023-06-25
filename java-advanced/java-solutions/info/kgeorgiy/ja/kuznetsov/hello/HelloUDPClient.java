package info.kgeorgiy.ja.kuznetsov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Client that sends requests via UDP.
 *
 * @author Ilya Kuznetsov (ilyakuznecov84@gmail.com)
 * Class implements {@link HelloClient}
 */
public class HelloUDPClient implements HelloClient {
    private final int TIMEOUT = 100;

    /**
     * Runs Hello client.
     * This method should return when all requests are completed.
     *
     * @param host     server host
     * @param port     server port
     * @param prefix   request prefix
     * @param threads  number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService service = Executors.newFixedThreadPool(threads);
        SocketAddress address = new InetSocketAddress(host, port);
        List<Future<?>> tasks = new ArrayList<>();
        for (int i = 1; i <= threads; i++) {
            final int finalI = i;
            Future<?> task = service.submit(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(TIMEOUT);
                    int bufferSize = socket.getReceiveBufferSize();
                    for (int j = 1; j <= requests; j++) {
                        String stringRequest = prefix + finalI + "_" + j;
                        byte[] data = stringRequest.getBytes();
                        DatagramPacket request = new DatagramPacket(data, data.length, address);
                        DatagramPacket response = new DatagramPacket(new byte[bufferSize], bufferSize);
                        while (!socket.isClosed()) {
                            try {
                                socket.send(request);
                                System.out.println("Request: " + stringRequest);
                                socket.receive(response);
                                String stringResponse = new String(response.getData(), 0, response.getLength());
                                if (stringResponse.contains(stringRequest)) {
                                    System.out.println("Response: " + stringResponse);
                                    break;
                                }
                            } catch (SocketTimeoutException ignored) {
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            tasks.add(task);
        }
        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        service.shutdownNow();
    }

    /**
     * Main method that sends requests. Arguments: host port prefix threads requests
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length != 5 || args[0] == null || args[1] == null
                || args[2] == null || args[3] == null || args[4] == null) {
            System.err.println("Invalid arguments");
            return;
        }
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);
            new HelloUDPClient().run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println("Port, threads, requests must be an integer argument");
        }
    }
}
