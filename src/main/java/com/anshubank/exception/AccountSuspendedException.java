package com.anshubank.exception;

public class AccountSuspendedException extends Exception {

    public AccountSuspendedException(String message) {
        super(message);
    }

    public AccountSuspendedException(String message, Throwable cause) {
        super(message, cause);
    }
}