package info.kgeorgiy.ja.kuznetsov.walk;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashSumCounter {
    public static String getHashSum(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] result = sha256.digest(bytes);
            return toHexString(result);
        } catch (SecurityException | NoSuchAlgorithmException | IOException e) {
            return getDefaultHashSum();
        }
    }

    private static String toHexString(byte[] bytes) {
        return String.format("%064x", new BigInteger(1, bytes));
    }

    public static String getDefaultHashSum() {
        return toHexString(new byte[0]);
    }
}