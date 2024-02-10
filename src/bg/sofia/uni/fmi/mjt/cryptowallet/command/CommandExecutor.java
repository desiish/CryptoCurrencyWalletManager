package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import bg.sofia.uni.fmi.mjt.cryptowallet.database.AvailableAssets;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.Logs;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.UserSet;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.NoSuchAssetExistsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.NoSuchAssetPurchasedException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.User;

import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.Set;

public class CommandExecutor {
    private static final String LOGIN = "login";
    private static final String REGISTER = "register";
    private static final String DEPOSIT = "deposit";
    private static final String LIST_OFFERINGS = "list-offerings";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String GET_WALLET_SUMMARY = "get-wallet-summary";
    private static final String GET_WALLET_OVERALL_SUMMARY = "get-wallet-overall-summary";
    private static final String HELP = "help";
    private static final String SHUTDOWN = "shutdown";
    private static final String DISCONNECT = "disconnect";
    private static final String UNKNOWN_COMMAND = "Unknown command.";

    private static final String INVALID_ARGUMENTS = "Invalid arguments";
    private static final String SUCCESSFUL_OPERATION = "Transaction completed";
    private static final String MUST_LOGIN = "Log in first or create an account to perform this action.";
    private static final String ALREADY_LOGGED_IN = "You are already logged in.";
    private static final String NEGATIVE_AMOUNT = "Amount cannot be negative.";
    private static final String INVALID_AMOUNT_ARGUMENT = "Amount of money is invalid.";
    private static final String INSUFFICIENT_AMOUNT = "You do not have enough on balance.";
    private static final String ASSET_DOES_NOT_EXIST = "No such asset is available for purchasing.";
    private static final String ASSET_NOT_PURCHASED = "No such asset was purchased.";
    private static final String ACCOUNT_DOES_NOT_EXIST = "No such account exists.";
    private static final String INVALID_PASSWORD = "Incorrect password. Please try again.";
    private static final String REGISTERED_SUCCESSFULLY = "Registered successfully.";
    private static final String LOGGED_SUCCESSFULLY = "Logged in successfully.";
    private static final String ACCOUNT_EXISTS = "Such account already exists";
    private static final String DISCONNECTED_SUCCESSFULLY = "Disconnected successfully.";
    private static final String SHUTDOWN_MESSAGE = "Server was shutdown.";

    private static final int NUMBER_OF_ARGUMENTS_BUY_AND_LOGIN_AND_REGISTER = 2;
    private static final int NUMBER_OF_ARGUMENTS_DEPOSIT_AND_SELL = 1;

    private final UserSet users;
    private final Set<User> currentlyInUse;
    private final AvailableAssets availableAssets;

    public CommandExecutor(UserSet users, AvailableAssets availableAssets) {
        this.users = users;
        this.availableAssets = availableAssets;
        this.currentlyInUse = new HashSet<>();
    }

    public String execute(Command command, SelectionKey key) {
        return switch (command.command()) {
            case LOGIN -> login(command.arguments(), key);
            case REGISTER -> register(command.arguments(), key);
            case DEPOSIT -> deposit(command.arguments(), key);
            case LIST_OFFERINGS -> listOfferings(key);
            case BUY -> buy(command.arguments(), key);
            case SELL -> sell(command.arguments(), key);
            case GET_WALLET_SUMMARY -> getWalletSummary(key);
            case GET_WALLET_OVERALL_SUMMARY -> getWalletOverallSummary(key);
            case DISCONNECT -> disconnect(key);
            case HELP -> help();
            case SHUTDOWN -> SHUTDOWN_MESSAGE;
            default -> UNKNOWN_COMMAND;
        };
    }

    private String help() {
        return "Available commands: " + System.lineSeparator() +
            "login {name} {password}" + System.lineSeparator() +
            "register {name} {password}" + System.lineSeparator() +
            "deposit {amount}" + System.lineSeparator() +
            "list-offerings" + System.lineSeparator() +
            "buy {id} {amount}" + System.lineSeparator() +
            "sell {id}" + System.lineSeparator() +
            "wallet-summary" + System.lineSeparator() +
            "wallet-overall-summary" + System.lineSeparator() +
            "disconnect" + System.lineSeparator();
    }

    private String register(String[] args, SelectionKey key) {
        if (key.attachment() != null) {
            return ALREADY_LOGGED_IN;
        }

        if (args.length != NUMBER_OF_ARGUMENTS_BUY_AND_LOGIN_AND_REGISTER) {
            return INVALID_ARGUMENTS;
        }

        String username = args[0];
        String password = args[1];

        try {
            User user = new User(username, password);
            users.addUser(user);
        } catch (UserAlreadyExistsException e) {
            Logs.logErrorWithStackTrace(e.getStackTrace(), e.getMessage());
            return ACCOUNT_EXISTS;
        }

        return REGISTERED_SUCCESSFULLY;
    }

    private String login(String[] args, SelectionKey key) {
        if (key.attachment() != null) {
            return ALREADY_LOGGED_IN;
        }
        if (args.length != NUMBER_OF_ARGUMENTS_BUY_AND_LOGIN_AND_REGISTER) {
            return INVALID_ARGUMENTS;
        }

        String username = args[0];
        String password = args[1];
        User curr = null;
        for (User u : users.getUsers()) {
            if (u.getUsername().equals(username)) {
                curr = u;
                break;
            }
        }

        if (curr == null) {
            return ACCOUNT_DOES_NOT_EXIST;
        }
        if (!curr.passMatch(password)) {
            return INVALID_PASSWORD;
        }

        currentlyInUse.add(curr);
        key.attach(curr);
        return LOGGED_SUCCESSFULLY;
    }

    private String disconnect(SelectionKey key) {
        if (key.attachment() == null) {
            return MUST_LOGIN;
        }

        User user = (User) key.attachment();
        currentlyInUse.remove(user);
        key.attach(null);

        return DISCONNECTED_SUCCESSFULLY;
    }

    private String deposit(String[] args, SelectionKey key) {
        if (key.attachment() == null) {
            return MUST_LOGIN;
        }
        User user = (User) key.attachment();

        if (args.length != NUMBER_OF_ARGUMENTS_DEPOSIT_AND_SELL) {
            return INVALID_ARGUMENTS;
        }

        try {
            double amount = Double.parseDouble(args[0]);
            user.getWallet().depositMoney(amount);
        } catch (NumberFormatException | NullPointerException e) {
            return INVALID_AMOUNT_ARGUMENT;
        } catch (IllegalArgumentException e) {
            return NEGATIVE_AMOUNT;
        }

        return SUCCESSFUL_OPERATION;
    }

    private String listOfferings(SelectionKey key) {
        if (key.attachment() == null) {
            return MUST_LOGIN;
        }

        return availableAssets.listOfferings();
    }

    private String buy(String[] args, SelectionKey key) {
        if (key.attachment() == null) {
            return MUST_LOGIN;
        }
        User user = (User) key.attachment();

        if (args.length != NUMBER_OF_ARGUMENTS_BUY_AND_LOGIN_AND_REGISTER) {
            return INVALID_ARGUMENTS;
        }

        try {
            String id = args[0];
            double amount = Double.parseDouble(args[1]);
            user.getWallet().buyAsset(id, amount, availableAssets);
        } catch (NumberFormatException | NullPointerException e) {
            return INVALID_AMOUNT_ARGUMENT;
        } catch (IllegalArgumentException e) {
            return NEGATIVE_AMOUNT;
        } catch (InsufficientBalanceException e) {
            return INSUFFICIENT_AMOUNT;
        } catch (NoSuchAssetExistsException e) {
            return ASSET_DOES_NOT_EXIST;
        }

        return SUCCESSFUL_OPERATION;
    }

    private String sell(String[] args, SelectionKey key) {
        if (key.attachment() == null) {
            return MUST_LOGIN;
        }
        User user = (User) key.attachment();

        if (args.length != NUMBER_OF_ARGUMENTS_DEPOSIT_AND_SELL) {
            return INVALID_ARGUMENTS;
        }

        String id = args[0];

        try {
            user.getWallet().sellAsset(id, availableAssets);
        } catch (NoSuchAssetPurchasedException e) {
            return ASSET_NOT_PURCHASED;
        } catch (NoSuchAssetExistsException e) {
            return ASSET_DOES_NOT_EXIST;
        }

        return SUCCESSFUL_OPERATION;
    }

    private String getWalletSummary(SelectionKey key) {
        if (key.attachment() == null) {
            return MUST_LOGIN;
        }
        User user = (User) key.attachment();

        return user.getWallet().getWalletSummary();
    }

    private String getWalletOverallSummary(SelectionKey key) {
        if (key.attachment() == null) {
            return MUST_LOGIN;
        }

        User user = (User) key.attachment();
        return user.getWallet().getWalletOverallSummary(availableAssets);
    }

    public AvailableAssets getAvailableAssets() {
        return availableAssets;
    }
}
