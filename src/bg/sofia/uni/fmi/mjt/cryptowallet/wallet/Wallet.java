package bg.sofia.uni.fmi.mjt.cryptowallet.wallet;

import bg.sofia.uni.fmi.mjt.cryptowallet.asset.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.AvailableAssets;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.NoSuchAssetExistsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.NoSuchAssetPurchasedException;

import java.io.Serializable;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Objects;

public class Wallet implements Serializable {
    private static final double START_BALANCE = 0.0;
    private double balance;
    private final Map<String, Set<Double>> purchasedAssetsAmount;
    private final Map<String, Double> purchasedAssets;

    public Wallet() {
        balance = START_BALANCE;
        purchasedAssetsAmount = new HashMap<>();
        purchasedAssets = new HashMap<>();
    }

    public void depositMoney(double amount) {
        if (amount < START_BALANCE) {
            throw new IllegalArgumentException("Deposited amount cannot be negative");
        }

        balance += amount;
    }

    public void buyAsset(String id, double toPay, AvailableAssets availableAssets)
        throws NoSuchAssetExistsException, InsufficientBalanceException {
        if (id == null || availableAssets == null) {
            throw new IllegalArgumentException("Id or AvailableAssets cannot be null.");
        }

        if (toPay < START_BALANCE) {
            throw new IllegalArgumentException("Cannot pay negative amount of money.");
        }

        if (toPay > balance) {
            throw new InsufficientBalanceException("Not enough money available.");
        }

        Asset asset = availableAssets.find(id);
        if (asset == null) {
            throw new NoSuchAssetExistsException("No such asset exists");
        }

        if (!purchasedAssetsAmount.containsKey(asset.getAssetId())) {
            purchasedAssetsAmount.put(asset.getAssetId(), new HashSet<>());
            purchasedAssets.put(asset.getAssetId(), asset.getPrice());
        }

        purchasedAssetsAmount.get(asset.getAssetId()).add(toPay / asset.getPrice());
        balance -= toPay;
    }

    public void sellAsset(String id, AvailableAssets availableAssets)
        throws NoSuchAssetExistsException, NoSuchAssetPurchasedException {
        if (id == null || availableAssets == null) {
            throw new IllegalArgumentException("ID or AvailableAssets cannot be null.");
        }

        if (!purchasedAssets.containsKey(id)) {
            throw new NoSuchAssetPurchasedException("No such asset was purchased");
        }

        Asset asset = availableAssets.find(id);

        if (asset == null) {
            throw new NoSuchAssetExistsException("No such asset exists");
        }

        double toAdd = 0.0;
        for (double amount : purchasedAssetsAmount.get(id)) {
            toAdd += amount * asset.getPrice();
        }

        balance += toAdd;
        purchasedAssetsAmount.remove(id);
        purchasedAssets.remove(id);
    }

    @Override
    public String toString() {
        return "Wallet{" +
            "balance=" + balance +
            ", purchasedAssets=" + purchasedAssetsAmount +
            '}';
    }

    public String getWalletSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Wallet summary : ").append(System.lineSeparator())
            .append("Current balance: $").append(balance).append(System.lineSeparator());
        for (String s : purchasedAssetsAmount.keySet()) {
            sb.append(s).append(": ");
            double sum = START_BALANCE;
            for (double amount : purchasedAssetsAmount.get(s)) {
                sum += amount;
            }
            sb.append(sum).append(System.lineSeparator());
        }
        return sb.toString();
    }

    public String getWalletOverallSummary(AvailableAssets availableAssets) {
        if (availableAssets == null) {
            throw new IllegalArgumentException("Available assets cannot be null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Overall winnings: $");
        double winnings = START_BALANCE;

        for (String id : purchasedAssets.keySet()) {
            Asset asset = availableAssets.find(id);
            double sum = 0.0;
            for (double amount : purchasedAssetsAmount.get(id)) {
                sum += amount;
            }
            winnings += sum * (asset.getPrice() - purchasedAssets.get(id));
        }

        sb.append(winnings).append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return Double.compare(balance, wallet.balance) == 0 &&
            Objects.equals(purchasedAssetsAmount, wallet.purchasedAssetsAmount) &&
            Objects.equals(purchasedAssets, wallet.purchasedAssets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(balance, purchasedAssetsAmount, purchasedAssets);
    }

    public Map<String, Double> getPurchasedAssets() {
        return purchasedAssets;
    }

    public Map<String, Set<Double>> getPurchasedAssetsAmount() {
        return purchasedAssetsAmount;
    }

    public double getBalance() {
        return balance;
    }
}