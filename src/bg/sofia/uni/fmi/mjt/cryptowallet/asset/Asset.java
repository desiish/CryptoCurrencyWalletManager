package bg.sofia.uni.fmi.mjt.cryptowallet.asset;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class Asset {
    private static final int IS_CRYPTO = 1;
    @SerializedName("asset_id")
    private final String assetId;
    private final String name;
    @SerializedName("price_usd")
    private final double price;
    @SerializedName("type_is_crypto")
    private final int isCrypto;

    public Asset(String assetId, String name, int isCrypto, double price) {
        this.assetId = assetId;
        this.name = name;
        this.isCrypto = isCrypto;
        this.price = price;
    }

    public boolean isCrypto() {
        return isCrypto == IS_CRYPTO;
    }

    public String getAssetId() {
        return assetId;
    }

    public double getPrice() {
        return price;
    }

    public String getAssetInfoString() {
        return "Asset ID: " + assetId + System.lineSeparator() +
            "Name: " + name + System.lineSeparator() +
            "Price: " + price + ";" + System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return isCrypto == asset.isCrypto && Objects.equals(assetId, asset.assetId) &&
            Objects.equals(name, asset.name) && Objects.equals(price, asset.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, name, price, isCrypto);
    }
}