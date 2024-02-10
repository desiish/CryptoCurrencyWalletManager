package bg.sofia.uni.fmi.mjt.cryptowallet.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CryptocurrencyWalletManagerClient {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 16384;

    private static final String CONNECTION_INTERRUPTED_MESSAGE =
        "Connection to server was interrupted. Please try reconnecting...";
    private static final String DISCONNECTED_SUCCESSFULLY = "Disconnected successfully.";
    private static final String SHUTDOWN = "Server was shutdown.";
    private static final ByteBuffer BUFFER = ByteBuffer.allocate(BUFFER_SIZE);

    private static void handleInput(SocketChannel sc, Scanner scanner) throws IOException {
        String message = scanner.nextLine();
        BUFFER.clear();
        BUFFER.put(message.getBytes());
        BUFFER.flip();
        sc.write(BUFFER);
    }

    public static void main(String[] args) {
        try (SocketChannel sc = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            sc.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to server.");

            while (true) {
                handleInput(sc, scanner);
                try {
                    BUFFER.clear();
                    sc.read(BUFFER);
                    BUFFER.flip();

                    byte[] byteArray = new byte[BUFFER.remaining()];
                    BUFFER.get(byteArray);
                    String response = new String(byteArray, StandardCharsets.UTF_8);
                    System.out.println(response);

                    if (response.equals(DISCONNECTED_SUCCESSFULLY) || response.equals(SHUTDOWN)) {
                        break;
                    }
                } catch (IOException e) {
                    System.out.println(SHUTDOWN);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(CONNECTION_INTERRUPTED_MESSAGE);
        }
    }
}