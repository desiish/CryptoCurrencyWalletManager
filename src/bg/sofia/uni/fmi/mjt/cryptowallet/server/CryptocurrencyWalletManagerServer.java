package bg.sofia.uni.fmi.mjt.cryptowallet.server;

import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.Command;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.Logs;
import bg.sofia.uni.fmi.mjt.cryptowallet.apicall.ApiCallRunnable;
import bg.sofia.uni.fmi.mjt.cryptowallet.apicall.ApiCall;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CryptocurrencyWalletManagerServer {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final String DISCONNECT = "disconnect";
    private static final String PROBLEM_OCCURRED_RESPONSE =
        "A problem occurred while reading input. Try again.";
    private static final String DISCONNECTED_SUCCESSFULLY_RESPONSE =
        "Disconnected successfully.";
    private static final String SHUTDOWN_RESPONSE =
        "Server was shutdown.";
    private static final int BUFFER_SIZE = 16384;
    private static final int TIME_BETWEEN_API_REQUESTS = 30;

    private boolean isServerWorking;
    private ByteBuffer buffer;
    private final CommandExecutor commandExecutor;

    public CryptocurrencyWalletManagerServer(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
        this.isServerWorking = true;
    }

    public void startServer() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            Selector selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);

            buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            isServerWorking = true;
            while (isServerWorking) {
                handleClientRequests(selector);
            }
        } catch (IOException e) {
            throw new RuntimeException("There was a problem with the server socket.", e);
        } catch (Exception e) {
            throw new RuntimeException("Exception was thrown by Autocloseable object");
        }
    }

    private void handleKey(SocketChannel sc, SelectionKey key) throws IOException {
        String clientInput = readClientInput(sc, key);

        if (clientInput == null || !key.isValid()) {
            return;
        }

        String response = null;

        try {
            response = commandExecutor
                .execute(Command.createCommand(clientInput), key);
        } catch (Exception e) {
            Logs.logErrorWithStackTrace(e.getStackTrace(), e.getMessage());
        } finally {
            sendResponseToClient(sc, response);
        }
    }

    private void handleClientRequests(Selector selector) throws IOException {
        int readyChannels = selector.select();
        if (readyChannels == 0) {
            return;
        }

        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                try {
                    handleKey(sc, key);
                } catch (IOException e) {
                    if (e.getMessage().contains("Connection reset")) {
                        handleDisconnect(sc, key);
                    }
                }
            } else if (key.isAcceptable()) {
                accept(selector, key);
            }
            keyIterator.remove();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String readClientInput(SocketChannel sc, SelectionKey key) throws IOException {
        buffer.clear();
        int r = sc.read(buffer);
        //if end of stream is reached
        if (r == -1) {
            handleDisconnect(sc, key);
            return null;
        }

        buffer.flip();
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    private void handleDisconnect(SocketChannel sc, SelectionKey key) throws IOException {
        commandExecutor.execute(Command.createCommand(DISCONNECT), key);
        sc.close();
        key.cancel();
    }

    private void sendResponseToClient(SocketChannel sc, String response) throws IOException {
        if (response == null) {
            response = PROBLEM_OCCURRED_RESPONSE;
        }

        buffer.clear();
        buffer.put(response.getBytes());
        buffer.flip();
        sc.write(buffer);
        if (response.equals(DISCONNECTED_SUCCESSFULLY_RESPONSE)) {
            sc.close();
        } else if (response.equals(SHUTDOWN_RESPONSE)) {
            sc.close();
            isServerWorking = false;
        }
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    public void start(ApiCall apiCall) {
        try (ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) {
            Runnable apiCallRunnable = new ApiCallRunnable(apiCall, commandExecutor.getAvailableAssets());
            Thread thread = new Thread(apiCallRunnable);
            scheduledExecutorService.scheduleAtFixedRate(thread,
                0, TIME_BETWEEN_API_REQUESTS, TimeUnit.MINUTES);
            startServer();
        } catch (Exception e) {
            Logs.logErrorWithStackTrace(e.getStackTrace(), e.getMessage());
        }
    }
}