package com.anshubank.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class InputValidator {

    private InputValidator() {
    }

    public static BigDecimal amount(String s) {
        try {
            BigDecimal b = new BigDecimal(s);
            return b.signum() > 0 ? b : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean email(String s) {
        return s != null
                && Pattern.matches(
                        "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
                        s
                );
    }

    public static boolean phone(String s) {
        return s != null
                && Pattern.matches(
                        "[0-9]{10}",
                        s
                );
    }

    public static boolean password(String s) {
        return s != null && s.length() >= 8;
    }
}