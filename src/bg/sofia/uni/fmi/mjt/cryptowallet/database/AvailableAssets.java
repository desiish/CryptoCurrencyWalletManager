package bg.sofia.uni.fmi.mjt.cryptowallet.database;

import bg.sofia.uni.fmi.mjt.cryptowallet.asset.Asset;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AvailableAssets {
    private static final int MAX_ASSET_COUNT = 100;
    private static Set<Asset> availableAssets;

    public AvailableAssets() {
        availableAssets = new HashSet<>();
    }

    public void updateAvailableAssets(Set<Asset> toUpdate) {
        if (toUpdate == null) {
            throw new IllegalArgumentException("AssetSet cannot be null.");
        }

        availableAssets = toUpdate.stream().filter(Asset::isCrypto)
            .limit(MAX_ASSET_COUNT).collect(Collectors.toSet());
    }

    public String listAvailableAssets() {
        StringBuilder res = new StringBuilder();
        res.append("Available assets to purchase: ").append(System.lineSeparator());
        for (var asset: availableAssets) {
            res.append(asset.getAssetInfoString());
        }
        return res.toString();
    }

    public Asset find(String id) {
        Asset res = null;
        for (var asset : availableAssets) {
            if (asset.getAssetId().equals(id)) {
                res = asset;
                break;
            }
        }

        return res;
    }

    public String listOfferings() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available offerings: ").append(System.lineSeparator());
        for (Asset asset : availableAssets) {
            sb.append(asset.getAssetInfoString()).append(System.lineSeparator());
        }

        return sb.toString();
    }

    public Set<Asset> getAvailableAssets() {
        return availableAssets;
    }
}
