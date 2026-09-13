package com.anshubank;

import com.anshubank.security.SecurityUtil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilTest {

    @Test
    void hashMatches() {
        String h = SecurityUtil.hash("Customer@123");

        assertNotEquals("Customer@123", h);
        assertTrue(
                SecurityUtil.matches("Customer@123", h)
        );
        assertFalse(
                SecurityUtil.matches("wrong", h)
        );
    }
}