package com.anshubank.service;

import com.anshubank.database.JpaManager;
import com.anshubank.entity.*;

import java.util.List;

import jakarta.persistence.EntityManager;

public class JpaQueryService {

    public List<AccountEntity> activeAccounts() {
        EntityManager em = JpaManager.factory().createEntityManager();

        try {
            return em.createQuery(
                    "SELECT a FROM AccountEntity a WHERE a.status = 'ACTIVE'",
                    AccountEntity.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<TransactionEntity> highRisk() {
        EntityManager em = JpaManager.factory().createEntityManager();

        try {
            return em.createQuery(
                    "SELECT t FROM TransactionEntity t " +
                    "WHERE t.riskScore >= :score " +
                    "ORDER BY t.riskScore DESC",
                    TransactionEntity.class
            )
            .setParameter("score", 61)
            .getResultList();

        } finally {
            em.close();
        }
    }
}