package bg.sofia.uni.mjt.cryptowallet.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import bg.sofia.uni.fmi.mjt.cryptowallet.asset.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.AvailableAssets;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.NoSuchAssetExistsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.NoSuchAssetPurchasedException;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class WalletTest {
    private Wallet wallet;
    private AvailableAssets availableAssets;

    @BeforeEach
    public void setUp() {
        wallet = new Wallet();
        availableAssets = new AvailableAssets();
    }

    @Test
    public void testDepositMoney() {
        wallet.depositMoney(100);
        assertEquals(100, wallet.getBalance());
    }

    @Test
    public void testDepositMoneyNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> wallet.depositMoney(-100));
    }

    @Test
    public void testBuyAsset() throws NoSuchAssetExistsException, InsufficientBalanceException {
        Asset asset = new Asset("BTC", "Bitcoin", 1, 100);
        availableAssets.updateAvailableAssets(Set.of(asset));

        wallet.depositMoney(1000);
        wallet.buyAsset("BTC", 500, availableAssets);

        assertTrue(wallet.getPurchasedAssets().containsKey("BTC"));
        assertEquals(1, wallet.getPurchasedAssetsAmount().get("BTC").size());
        assertEquals(5, wallet.getPurchasedAssetsAmount().get("BTC").iterator().next());
    }

    @Test
    public void testBuyAssetNoSuchAssetExists() {
        assertThrows(NoSuchAssetExistsException.class, () -> wallet.buyAsset("DOGE", 0, availableAssets));
    }

    @Test
    public void testBuyAssetInsufficientBalance() throws NoSuchAssetExistsException {
        Asset asset = new Asset("BTC", "Bitcoin", 1, 100);
        availableAssets.updateAvailableAssets(Set.of(asset));

        wallet.depositMoney(100);
        assertThrows(InsufficientBalanceException.class, () -> wallet.buyAsset("BTC", 500, availableAssets));
    }

    @Test
    public void testBuyAssetNegativeAmount() {
        Asset asset = new Asset("BTC", "Bitcoin", 1, 100);
        availableAssets.updateAvailableAssets(Set.of(asset));

        wallet.depositMoney(1000);
        assertThrows(IllegalArgumentException.class, () -> wallet.buyAsset("BTC", -500, availableAssets));
    }

    @Test
    public void testBuyNullArgument() {
        assertThrows(IllegalArgumentException.class, () -> wallet.buyAsset("BTC", 100, null));
    }

    @Test
    public void testSellAsset() {
        try {
            Asset asset = new Asset("BTC", "Bitcoin", 1, 100);
            availableAssets.updateAvailableAssets(Set.of(asset));

            wallet.depositMoney(1000);
            wallet.buyAsset("BTC", 500, availableAssets);

            wallet.sellAsset("BTC", availableAssets);

            assertEquals(1000, wallet.getBalance());
            assertFalse(wallet.getPurchasedAssetsAmount().containsKey("BTC"));
        } catch (NoSuchAssetExistsException | NoSuchAssetPurchasedException | InsufficientBalanceException e) {
            fail();
        }
    }

    @Test
    public void testSellAssetNoSuchAssetPurchased() {
        assertThrows(NoSuchAssetPurchasedException.class, () -> wallet.sellAsset("BTC", availableAssets));
    }

    @Test
    public void testSellAssetNoSuchAssetExists() {
        try {
            Asset asset = new Asset("BTC", "Bitcoin", 1, 100);
            availableAssets.updateAvailableAssets(Set.of(asset));

            wallet.depositMoney(1000);
            wallet.buyAsset("BTC", 500, availableAssets);

            Set<Asset> set = new HashSet<>();
            availableAssets.updateAvailableAssets(set);
            assertThrows(NoSuchAssetExistsException.class, () -> wallet.sellAsset("BTC", availableAssets));
        } catch (NoSuchAssetExistsException | InsufficientBalanceException e) {
            fail();
        }
    }

    @Test
    public void testSellAssetWithMultipleAmounts() {
        try {
            Asset asset = new Asset("BTC", "Bitcoin", 1, 100);
            availableAssets.updateAvailableAssets(Set.of(asset));

            wallet.depositMoney(1000);
            wallet.buyAsset("BTC", 500, availableAssets);
            wallet.buyAsset("BTC", 300, availableAssets);

            wallet.sellAsset("BTC", availableAssets);

            assertEquals(1000, wallet.getBalance());
            assertFalse(wallet.getPurchasedAssetsAmount().containsKey("BTC"));
        } catch (NoSuchAssetExistsException | NoSuchAssetPurchasedException | InsufficientBalanceException e) {
            fail();
        }
    }

    @Test
    public void testSellAssetWithMultipleAssets() {
        try {
            Asset asset1 = new Asset("BTC", "Bitcoin", 1, 100);
            Asset asset2 = new Asset("ETH", "Ethereum", 1, 100);
            availableAssets.updateAvailableAssets(Set.of(asset1, asset2));

            wallet.depositMoney(2000);
            wallet.buyAsset("BTC", 500, availableAssets);
            wallet.buyAsset("ETH", 800, availableAssets);

            wallet.sellAsset("BTC", availableAssets);

            assertEquals(1200, wallet.getBalance());
            assertFalse(wallet.getPurchasedAssetsAmount().containsKey("BTC"));
        } catch (NoSuchAssetExistsException | NoSuchAssetPurchasedException | InsufficientBalanceException e) {
            fail();
        }
    }

    @Test
    public void testSellNullArgument() {
        assertThrows(IllegalArgumentException.class, () -> wallet.sellAsset(null, null));
    }

    @Test
    public void testGetWalletSummary() throws NoSuchAssetExistsException, InsufficientBalanceException {
        Asset asset1 = new Asset("BTC", "Bitcoin", 1, 100);
        Asset asset2 = new Asset("ETH", "Ethereum", 1, 100);
        availableAssets.updateAvailableAssets(Set.of(asset1, asset2));

        wallet.depositMoney(2000);
        wallet.buyAsset("BTC", 500, availableAssets);
        wallet.buyAsset("ETH", 800, availableAssets);

        String expectedSummary = "Wallet summary : " + System.lineSeparator() +
            "Current balance: $700.0" + System.lineSeparator() +
            "BTC: 5.0" + System.lineSeparator() +
            "ETH: 8.0" + System.lineSeparator();

        assertEquals(expectedSummary, wallet.getWalletSummary());
    }

    @Test
    public void testGetWalletSummaryNoAssets() {
        String expectedSummary = "Wallet summary : " + System.lineSeparator() +
            "Current balance: $0.0" + System.lineSeparator();

        assertEquals(expectedSummary, wallet.getWalletSummary());
    }

    @Test
    public void testGetWalletOverallSummary() throws NoSuchAssetExistsException, InsufficientBalanceException {
        Asset asset1 = new Asset("BTC", "Bitcoin", 1, 100);
        Asset asset2 = new Asset("ETH", "Ethereum", 1, 100);
        availableAssets.updateAvailableAssets(Set.of(asset1, asset2));

        wallet.depositMoney(2000);
        wallet.buyAsset("BTC", 500, availableAssets);
        wallet.buyAsset("ETH", 800, availableAssets);
        Asset asset3 = new Asset("BTC", "Bitcoin", 1, 200);
        availableAssets.updateAvailableAssets(Set.of(asset3, asset2));
        String expectedOverallSummary = "Overall winnings: $500.0" + System.lineSeparator();

        assertEquals(expectedOverallSummary, wallet.getWalletOverallSummary(availableAssets));
    }

    @Test
    public void testGetWalletOverallSummaryNoAssets() {
        String expectedOverallSummary = "Overall winnings: $0.0" + System.lineSeparator();
        assertEquals(expectedOverallSummary, wallet.getWalletOverallSummary(availableAssets));
    }
}
