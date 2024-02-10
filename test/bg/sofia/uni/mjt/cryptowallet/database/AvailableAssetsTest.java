package bg.sofia.uni.mjt.cryptowallet.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

import bg.sofia.uni.fmi.mjt.cryptowallet.asset.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.AvailableAssets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

public class AvailableAssetsTest {

    private AvailableAssets availableAssets;

    @BeforeEach
    public void setUp() {
        availableAssets = new AvailableAssets();
    }

    @Test
    public void testUpdateAvailableAssets() {
        Set<Asset> assetsToUpdate = new HashSet<>();
        assetsToUpdate.add(new Asset("BTC", "Bitcoin", 1, 100));
        assetsToUpdate.add(new Asset("ETH", "Ethereum", 1, 100));

        assertDoesNotThrow(() -> availableAssets.updateAvailableAssets(assetsToUpdate));

        Set<Asset> updatedAssets = availableAssets.getAvailableAssets();
        assertEquals(2, updatedAssets.size());
        assertTrue(updatedAssets.contains(new Asset("BTC", "Bitcoin", 1, 100)));
        assertTrue(updatedAssets.contains(new Asset("ETH", "Ethereum", 1, 100)));
    }

    @Test
    public void testListAvailableAssets() {
        Set<Asset> assets = new HashSet<>();
        assets.add(new Asset("BTC", "Bitcoin", 1, 100));
        assets.add(new Asset("ETH", "Ethereum", 1, 100));

        availableAssets.updateAvailableAssets(assets);

        String expected = "Available assets to purchase: " + System.lineSeparator() +
            "Asset ID: BTC" + System.lineSeparator() +
            "Name: Bitcoin" + System.lineSeparator() +
            "Price: 100.0;" + System.lineSeparator() +
            "Asset ID: ETH" + System.lineSeparator() +
            "Name: Ethereum" + System.lineSeparator() +
            "Price: 100.0;" + System.lineSeparator();

        assertEquals(expected, availableAssets.listAvailableAssets());
    }

    @Test
    public void testFind() {
        Set<Asset> assets = new HashSet<>();
        assets.add(new Asset("BTC", "Bitcoin", 1, 100));
        assets.add(new Asset("ETH", "Ethereum", 1, 100));

        availableAssets.updateAvailableAssets(assets);

        assertEquals(new Asset("BTC", "Bitcoin", 1, 100),
            availableAssets.find("BTC"));
        assertNull(availableAssets.find("DOGE"));
    }

    @Test
    public void testListOfferings() {
        Set<Asset> assets = new HashSet<>();
        assets.add(new Asset("BTC", "Bitcoin", 1, 100));
        assets.add(new Asset("ETH", "Ethereum", 1, 100));

        availableAssets.updateAvailableAssets(assets);

        String expected = "Available offerings: " + System.lineSeparator() +
                        "Asset ID: BTC" + System.lineSeparator() +
                        "Name: Bitcoin" + System.lineSeparator() +
                        "Price: 100.0;" + System.lineSeparator() +
                        System.lineSeparator() +
                        "Asset ID: ETH" + System.lineSeparator() +
                        "Name: Ethereum" + System.lineSeparator() +
                        "Price: 100.0;" + System.lineSeparator() +
                        System.lineSeparator() ;

        assertEquals(expected, availableAssets.listOfferings());
    }
}

