package info.kgeorgiy.ja.kuznetsov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server that responds to all requests with "Hello, " + request
 *
 * @author Ilya Kuznetsov (ilyakuznecov84@gmail.com)
 * Class implements {@link HelloServer}
 */
public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService service;

    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param port    server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(int port, int threads) {
        if (socket != null && !socket.isClosed()) {
            close();
        }
        try {
            socket = new DatagramSocket(port);
            service = Executors.newFixedThreadPool(threads);
            int bufferSize = socket.getReceiveBufferSize();
            for (int i = 0; i < threads; i++) {
                service.submit(() -> {
                    while (!socket.isClosed()) {
                        try {
                            DatagramPacket request = new DatagramPacket(new byte[bufferSize], bufferSize);
                            socket.receive(request);
                            String stringResponse = "Hello, " + new String(request.getData(), 0, request.getLength());
                            byte[] data = stringResponse.getBytes();
                            DatagramPacket response = new DatagramPacket(data, data.length, request.getSocketAddress());
                            socket.send(response);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        socket.close();
        service.shutdownNow();
    }

    /**
     * Main method that starts server. Arguments: port threads
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Invalid arguments");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            new HelloUDPServer().start(port, threads);
        } catch (NumberFormatException e) {
            System.err.println("Port, threads must be an integer argument");
        }
    }
}
