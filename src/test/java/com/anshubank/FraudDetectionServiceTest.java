package com.anshubank;

import com.anshubank.service.*;
import com.anshubank.repository.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FraudDetectionServiceTest {

    @Test
    void largeAmountGetsRisk() {
        try {
            var r = new FraudDetectionService(
                    new TransactionRepository()
            ).analyze(
                    "SB10001",
                    new BigDecimal("90000")
            );

            assertTrue(r.score() >= 35);
            assertTrue(
                    r.level().name().matches(
                            "MEDIUM|HIGH|CRITICAL"
                    )
            );

        } catch (Exception e) {
            fail(e);
        }
    }
}