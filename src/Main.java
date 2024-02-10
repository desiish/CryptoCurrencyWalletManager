import bg.sofia.uni.fmi.mjt.cryptowallet.apicall.ApiCall;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.AvailableAssets;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.UserSet;
import bg.sofia.uni.fmi.mjt.cryptowallet.server.CryptocurrencyWalletManagerServer;

import java.io.File;
import java.net.http.HttpClient;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String apiKey;
        apiKey = scanner.nextLine();
        try (UserSet set = new UserSet("database" + File.separator + "users.txt")) {
            ApiCall apiCall = new ApiCall(HttpClient.newBuilder().build(), apiKey);
            CommandExecutor commandExecutor = new CommandExecutor(set, new AvailableAssets());
            CryptocurrencyWalletManagerServer cryptocurrencyWalletManagerServer
                = new CryptocurrencyWalletManagerServer(commandExecutor);
            cryptocurrencyWalletManagerServer.start(apiCall);
        }
    }
}