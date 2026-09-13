package com.anshubank.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] out = md.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder sb = new StringBuilder();

            for (byte b : out) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean matches(String raw, String hash) {
        return MessageDigest.isEqual(
                hash(raw).getBytes(StandardCharsets.UTF_8),
                hash.getBytes(StandardCharsets.UTF_8)
        );
    }
}