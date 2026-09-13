package com.anshubank.concurrency;

import com.anshubank.service.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TransactionProcessor implements AutoCloseable {

    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    private final TransactionService service;

    private final List<Future<?>> tasks = new CopyOnWriteArrayList<>();

    public TransactionProcessor(TransactionService service) {
        this.service = service;
    }

    public void submitWithdrawal(String account, BigDecimal amount) {

        tasks.add(
            pool.submit(() -> {

                String worker = Thread.currentThread().getName();

                System.out.println(
                    "[" + worker + "] Processing withdrawal Rs." + amount
                );

                try {

                    var transaction = service.withdraw(account, amount);

                    System.out.println(
                        "[" + worker + "] Completed "
                            + transaction.transactionId()
                    );

                } catch (Exception e) {

                    System.out.println(
                        "[" + worker + "] Failed: "
                            + e.getMessage()
                    );
                }
            })
        );
    }

    public void demo(String account) {

        System.out.println("Queue Size: 2");

        submitWithdrawal(
            account,
            new BigDecimal("30000")
        );

        submitWithdrawal(
            account,
            new BigDecimal("25000")
        );
    }

    public void await() {

        pool.shutdown();

        try {

            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            pool.shutdownNow();
        }
    }

    @Override
    public void close() {
        await();
    }
}