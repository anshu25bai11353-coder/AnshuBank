package com.anshubank;

import com.anshubank.cli.BankApplication;

public class Main {

    public static void main(String[] args) {
        System.out.println(
                "Educational banking simulation - not for real financial transactions."
        );

        new BankApplication().start();
    }
}