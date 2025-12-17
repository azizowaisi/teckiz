package com.teckiz.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class UtilHelper {

    private static final SecureRandom random = new SecureRandom();

    public static String generateEntityKey() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateUniqueKey() {
        long microtime = System.nanoTime();
        int randomInt = random.nextInt();
        String combined = microtime + String.valueOf(randomInt);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(combined.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Base64.getEncoder().encodeToString(combined.getBytes());
        }
    }

    public static String generateAPIKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(generateUniqueKey().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return generateEntityKey();
        }
    }

    public static String generatePassword() {
        String chars = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ!@#%&$?*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}

