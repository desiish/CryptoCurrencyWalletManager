package bg.sofia.uni.fmi.mjt.cryptowallet.user;

import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.Wallet;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class User implements Serializable {
    private static final String HASHING_ALGORITHM = "MD5";
    private static final int HASHING_CONSTANT_1 = 0xff;
    private static final int HASHING_CONSTANT_2 = 0x100;
    private static final int HASHING_CONSTANT_3 = 16;
    private final String username;
    private final String password;
    private final Wallet wallet;

    public User(String username, String password) {
        this.password = hashPassword(password);
        this.username = username;
        this.wallet = new Wallet();
    }

    private String hashPassword(String password) {
        String generatedPassword;
        try {
            MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & HASHING_CONSTANT_1) + HASHING_CONSTANT_2,
                    HASHING_CONSTANT_3).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error occurred while hashing password.");
        }
        return generatedPassword;
    }

    public boolean passMatch(String password) {
        return hashPassword(password).equals(this.password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(password, user.password) &&
            Objects.equals(wallet, user.wallet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, wallet);
    }

    public String getUsername() {
        return username;
    }

    public Wallet getWallet() {
        return wallet;
    }

}