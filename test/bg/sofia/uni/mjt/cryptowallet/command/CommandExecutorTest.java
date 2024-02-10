package bg.sofia.uni.mjt.cryptowallet.command;

import bg.sofia.uni.fmi.mjt.cryptowallet.asset.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.Command;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.AvailableAssets;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.UserSet;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.NoSuchAssetExistsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.NoSuchAssetPurchasedException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.User;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.Set;

public class CommandExecutorTest {

    private CommandExecutor commandExecutor;

    @Mock
    private UserSet userSet;
    @Mock
    private AvailableAssets availableAssets;
    @Mock
    private SelectionKey selectionKey;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        commandExecutor = new CommandExecutor(userSet, availableAssets);
    }

    @Test
    public void testRegister() {
        try {
            Command registerCommand = new Command("register", new String[] {"username", "password"});
            userSet.addUser(any(User.class));
            String result = commandExecutor.execute(registerCommand, selectionKey);
            assertEquals("Registered successfully.", result);
        } catch (UserAlreadyExistsException e) {
            fail();
        }
    }

    @Test
    public void testRegisterNotNullKeyAttachment() {
        User loggedInUser = new User("loggedUser", "password");
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        Command registerCommand = new Command("register", new String[]{"newUser", "newPassword"});
        String result = commandExecutor.execute(registerCommand, selectionKey);
        assertEquals("You are already logged in.", result);
    }

    @Test
    public void testRegisterWithInvalidArguments() {
        Command invalidRegisterCommand = new Command("register", new String[]{"username"});
        String result = commandExecutor.execute(invalidRegisterCommand, selectionKey);
        assertEquals("Invalid arguments", result);
    }

    @Test
    public void testRegisterWhenAccountExists() {
        try {
            String existingUsername = "existingUser";
            doThrow(new UserAlreadyExistsException("Account already exists"))
                .when(userSet).addUser(any(User.class));
            Command registerCommand = new Command("register", new String[]{existingUsername, "password"});
            String result = commandExecutor.execute(registerCommand, selectionKey);
            assertEquals("Such account already exists", result);
        } catch (UserAlreadyExistsException e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    public void testLogin() {
        User user = new User("username", "password");
        when(userSet.getUsers()).thenReturn(Set.of(user));
        Command loginCommand = new Command("login", new String[]{"username", "password"});
        String result = commandExecutor.execute(loginCommand, selectionKey);
        assertEquals("Logged in successfully.", result);
    }

    @Test
    public void testLoginNotNullKeyAttachment() {
        User loggedInUser = new User("loggedUser", "password");
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        Command loginCommand = new Command("login", new String[]{"newUser", "newPassword"});
        String result = commandExecutor.execute(loginCommand, selectionKey);
        assertEquals("You are already logged in.", result);
    }

    @Test
    public void testLoginWithInvalidArguments() {
        Command invalidLoginCommand = new Command("login", new String[]{"username"});
        String result = commandExecutor.execute(invalidLoginCommand, selectionKey);
        assertEquals("Invalid arguments", result);
    }

    @Test
    public void testLoginWhenAccountDoesNotExist() {
        when(userSet.getUsers()).thenReturn(new HashSet<>());
        Command loginCommand = new Command("login", new String[]{"nonExistentUser", "password"});
        String result = commandExecutor.execute(loginCommand, selectionKey);
        assertEquals("No such account exists.", result);
    }

    @Test
    public void testLoginWhenPasswordDoesNotMatch() {
        User existingUser = new User("existingUser", "correctPassword");
        when(userSet.getUsers()).thenReturn(Set.of(existingUser));
        Command loginCommand = new Command("login", new String[]{"existingUser", "incorrectPassword"});
        String result = commandExecutor.execute(loginCommand, selectionKey);
        assertEquals("Incorrect password. Please try again.", result);
    }

    @Test
    public void testDisconnect() {
        User user = new User("username", "password");
        when(selectionKey.attachment()).thenReturn(user);
        Command disconnectCommand = new Command("disconnect", new String[]{});
        String result = commandExecutor.execute(disconnectCommand, selectionKey);
        assertEquals("Disconnected successfully.", result);
    }

    @Test
    public void testDisconnectNullKetAttachment() {
        when(selectionKey.attachment()).thenReturn(null);
        Command disconnectCommand = new Command("disconnect", null);
        String result = commandExecutor.execute(disconnectCommand, selectionKey);
        assertEquals("Log in first or create an account to perform this action.", result);
    }

    @Test
    public void testDeposit() {
        User user = new User("username", "password");
        when(selectionKey.attachment()).thenReturn(user);
        Command depositCommand = new Command("deposit", new String[]{"100"});
        String result = commandExecutor.execute(depositCommand, selectionKey);
        assertEquals("Transaction completed", result);
    }

    @Test
    public void testDepositNullKetAttachment() {
        when(selectionKey.attachment()).thenReturn(null);
        Command depositCommand = new Command("deposit", new String[]{"100"});
        String result = commandExecutor.execute(depositCommand, selectionKey);
        assertEquals("Log in first or create an account to perform this action.", result);
    }

    @Test
    public void testDepositInvalidArguments() {
        User loggedInUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        Command depositCommand = new Command("deposit", new String[]{"100", "100"});
        String result = commandExecutor.execute(depositCommand, selectionKey);
        assertEquals("Invalid arguments", result);
    }

    @Test
    public void testDepositInvalidAmountArgument() {
        User loggedInUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        Command depositCommand = new Command("deposit", new String[]{"invalidAmount"});
        String result = commandExecutor.execute(depositCommand, selectionKey);
        assertEquals("Amount of money is invalid.", result);
    }

    @Test
    public void testDepositNegativeAmount() {
        User loggedInUser = mock(User.class);
        Wallet mockedWallet = mock(Wallet.class);
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        when(loggedInUser.getWallet()).thenReturn(mockedWallet);
        doThrow(IllegalArgumentException.class).when(mockedWallet).depositMoney(any(double.class));
        Command depositCommand = new Command("deposit", new String[]{"-100"});
        String result = commandExecutor.execute(depositCommand, selectionKey);
        assertEquals("Amount cannot be negative.", result);
    }

    @Test
    public void testListOfferings() {
        when(selectionKey.attachment()).thenReturn(new User("username", "password"));
        when(availableAssets.listOfferings()).thenReturn("Offerings list");
        Command listOfferingsCommand = new Command("list-offerings", new String[]{});
        String result = commandExecutor.execute(listOfferingsCommand, selectionKey);
        assertEquals("Offerings list", result);
    }

    @Test
    public void testListOfferingsNullKetAttachment() {
        when(selectionKey.attachment()).thenReturn(null);
        Command listOfferingsCommand = new Command("list-offerings", null);
        String result = commandExecutor.execute(listOfferingsCommand, selectionKey);
        assertEquals("Log in first or create an account to perform this action.", result);
    }

    @Test
    public void testBuy() {
        User user = new User("username", "password");
        when(selectionKey.attachment()).thenReturn(user);
        when(availableAssets.find("id")).thenReturn(new Asset("id", "Bitcoin", 1, 1000));
        Command buyCommand = new Command("buy", new String[]{"id", "0"});
        String result = commandExecutor.execute(buyCommand, selectionKey);
        assertEquals("Transaction completed", result);
    }

    @Test
    public void testBuyNullKetAttachment() {
        when(selectionKey.attachment()).thenReturn(null);
        Command buyCommand = new Command("buy", new String[]{"ETH", "0"});
        String result = commandExecutor.execute(buyCommand, selectionKey);
        assertEquals("Log in first or create an account to perform this action.", result);
    }

    @Test
    public void testBuyInvalidArguments() {
        User loggedInUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        Command buyCommand = new Command("buy", new String[]{"100"});
        String result = commandExecutor.execute(buyCommand, selectionKey);
        assertEquals("Invalid arguments", result);
    }

    @Test
    public void testBuyInvalidAmountArgument() {
        User loggedInUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        Command buyCommand = new Command("buy", new String[]{"ETH", "invalidAmount"});
        String result = commandExecutor.execute(buyCommand, selectionKey);
        assertEquals("Amount of money is invalid.", result);
    }

    @Test
    public void testBuyNegativeAmount() {
        try {
            User loggedInUser = mock(User.class);
            Wallet mockedWallet = mock(Wallet.class);
            when(selectionKey.attachment()).thenReturn(loggedInUser);
            when(loggedInUser.getWallet()).thenReturn(mockedWallet);
            doThrow(IllegalArgumentException.class).when(mockedWallet).buyAsset(any(String.class),
                any(double.class), any(AvailableAssets.class));
            Command buyCommand = new Command("buy", new String[] {"ETH", "-100"});
            String result = commandExecutor.execute(buyCommand, selectionKey);
            assertEquals("Amount cannot be negative.", result);
        } catch (InsufficientBalanceException | NoSuchAssetExistsException e) {
            fail();
        }
    }

    @Test
    public void testBuyInsufficientBalance() {
        try {
            User loggedInUser = mock(User.class);
            Wallet mockedWallet = mock(Wallet.class);
            when(selectionKey.attachment()).thenReturn(loggedInUser);
            when(loggedInUser.getWallet()).thenReturn(mockedWallet);
            doThrow(InsufficientBalanceException.class).when(mockedWallet).buyAsset(any(String.class),
                any(double.class), any(AvailableAssets.class));
            Command buyCommand = new Command("buy", new String[] {"ETH", "100"});
            String result = commandExecutor.execute(buyCommand, selectionKey);
            assertEquals("You do not have enough on balance.", result);
        } catch (InsufficientBalanceException | NoSuchAssetExistsException e) {
            fail();
        }
    }

    @Test
    public void testBuyAssetDoesNotExist() {
        try {
            User loggedInUser = mock(User.class);
            Wallet mockedWallet = mock(Wallet.class);
            when(selectionKey.attachment()).thenReturn(loggedInUser);
            when(loggedInUser.getWallet()).thenReturn(mockedWallet);
            doThrow(NoSuchAssetExistsException.class).when(mockedWallet).buyAsset(any(String.class),
                any(double.class), any(AvailableAssets.class));
            Command buyCommand = new Command("buy", new String[] {"ETH", "100"});
            String result = commandExecutor.execute(buyCommand, selectionKey);
            assertEquals("No such asset is available for purchasing.", result);
        } catch (InsufficientBalanceException | NoSuchAssetExistsException e) {
            fail();
        }
    }

    @Test
    public void testSell() {
        try {
            User user = new User("username", "password");
            when(selectionKey.attachment()).thenReturn(user);
            when(availableAssets.find("id")).thenReturn(new Asset("id", "Bitcoin", 1, 1000));
            user.getWallet().buyAsset("id", 0, availableAssets);
            Command sellCommand = new Command("sell", new String[] {"id"});
            String result = commandExecutor.execute(sellCommand, selectionKey);
            assertEquals("Transaction completed", result);
        } catch (NoSuchAssetExistsException | InsufficientBalanceException e) {
            fail();
        }
    }

    @Test
    public void testSellNullKetAttachment() {
        when(selectionKey.attachment()).thenReturn(null);
        Command sellCommand = new Command("sell", new String[]{"ETH"});
        String result = commandExecutor.execute(sellCommand, selectionKey);
        assertEquals("Log in first or create an account to perform this action.", result);
    }

    @Test
    public void testSellInvalidArguments() {
        User loggedInUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        Command sellCommand = new Command("sell", new String[]{"100", "100"});
        String result = commandExecutor.execute(sellCommand, selectionKey);
        assertEquals("Invalid arguments", result);
    }

    @Test
    public void testSellAssetDoesNotExist() {
        try {
            User loggedInUser = mock(User.class);
            Wallet mockedWallet = mock(Wallet.class);
            when(selectionKey.attachment()).thenReturn(loggedInUser);
            when(loggedInUser.getWallet()).thenReturn(mockedWallet);
            doThrow(NoSuchAssetExistsException.class).when(mockedWallet).sellAsset(any(String.class),
                any(AvailableAssets.class));
            Command sellCommand = new Command("sell", new String[] {"ETH"});
            String result = commandExecutor.execute(sellCommand, selectionKey);
            assertEquals("No such asset is available for purchasing.", result);
        } catch (NoSuchAssetExistsException | NoSuchAssetPurchasedException e) {
            fail();
        }
    }

    @Test
    public void testSellAssetNotPurchased() {
        try {
            User loggedInUser = mock(User.class);
            Wallet mockedWallet = mock(Wallet.class);
            when(selectionKey.attachment()).thenReturn(loggedInUser);
            when(loggedInUser.getWallet()).thenReturn(mockedWallet);
            doThrow(NoSuchAssetPurchasedException.class).when(mockedWallet).sellAsset(any(String.class),
                any(AvailableAssets.class));
            Command sellCommand = new Command("sell", new String[] {"ETH"});
            String result = commandExecutor.execute(sellCommand, selectionKey);
            assertEquals("No such asset was purchased.", result);
        } catch (NoSuchAssetExistsException | NoSuchAssetPurchasedException e) {
            fail();
        }
    }

    @Test
    public void testGetWalletSummary() {
        User loggedInUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        Wallet wallet = mock(Wallet.class);
        String expectedSummary = "Bitcoin: 2.0 BTC\nEthereum: 5.0 ETH\n";
        when(wallet.getWalletSummary()).thenReturn(expectedSummary);
        when(loggedInUser.getWallet()).thenReturn(wallet);
        String result = commandExecutor.execute(new Command("get-wallet-summary", null), selectionKey);

        assertEquals(expectedSummary, result);
    }

    @Test
    public void testGetWalletSummaryNullKetAttachment() {
        when(selectionKey.attachment()).thenReturn(null);
        Command command = new Command("get-wallet-summary", null);
        String result = commandExecutor.execute(command, selectionKey);
        assertEquals("Log in first or create an account to perform this action.", result);
    }

    @Test
    public void testGetWalletOverallSummary() {
        User loggedInUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(loggedInUser);
        Wallet wallet = mock(Wallet.class);
        String expectedOverallSummary = "Bitcoin: 2.0 BTC\nEthereum: 5.0 ETH\nTotal balance: $10000\n";
        when(wallet.getWalletOverallSummary(availableAssets)).thenReturn(expectedOverallSummary);
        when(loggedInUser.getWallet()).thenReturn(wallet);
        String result = commandExecutor.execute(new Command("get-wallet-overall-summary", null), selectionKey);

        assertEquals(expectedOverallSummary, result);
    }

    @Test
    public void testGetWalletOverallSummaryNullKetAttachment() {
        when(selectionKey.attachment()).thenReturn(null);
        Command command = new Command("get-wallet-overall-summary", null);
        String result = commandExecutor.execute(command, selectionKey);
        assertEquals("Log in first or create an account to perform this action.", result);
    }

    @Test
    public void testHelp() {
        Command helpCommand = new Command("help", new String[]{});
        String result = commandExecutor.execute(helpCommand, selectionKey);
        assertTrue(result.contains("Available commands:"));
    }

    @Test
    public void testShutdown() {
        String result = commandExecutor.execute(new Command("shutdown", null), selectionKey);
        assertEquals("Server was shutdown.", result);
    }

    @Test
    public void testGetAvailableAssets() {
        AvailableAssets result = commandExecutor.getAvailableAssets();
        assertEquals(availableAssets, result);
    }

    @Test
    public void testUnknownCommand() {
        String unknownCommand = "unknown-command";
        String result = commandExecutor.execute(new Command(unknownCommand, null), selectionKey);
        assertEquals("Unknown command.", result);
    }
}
