package com.anshubank.database;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaManager {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("anshuBankPU");

    private JpaManager() {
    }

    public static EntityManagerFactory factory() {
        return EMF;
    }

    public static void close() {

        if (EMF.isOpen()) {
            EMF.close();
        }
    }
}