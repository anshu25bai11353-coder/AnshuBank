package com.anshubank.util;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;


public final class PasswordInput {

    private PasswordInput() {
    }

    /**
     * Secure password input for terminal.
     *
     * On macOS/Linux it displays * while typing.
     * On terminals that do not support character-by-character
     * input, it falls back to hidden console input.
     */
    public static String readPassword(String prompt) {

        Console console = System.console();

        if (console == null) {
            return readWithScannerFallback(prompt);
        }

        System.out.print(prompt);

        try {
            return readWithStars(console);
        } catch (Exception e) {
            System.out.println();
            char[] password = console.readPassword();
            return password == null ? "" : new String(password);
        }
    }

    /**
     * Reads password character by character and displays *.
     */
    private static String readWithStars(Console console)
            throws IOException, InterruptedException {

        StringBuilder password = new StringBuilder();

        setTerminalRawMode();

        try {
            InputStream input = System.in;

            while (true) {

                int value = input.read();

                if (value == -1) {
                    break;
                }

                char ch = (char) value;

                // ENTER
                if (ch == '\n' || ch == '\r') {
                    break;
                }

                // BACKSPACE
                if (ch == 127 || ch == 8) {

                    if (password.length() > 0) {
                        password.deleteCharAt(
                                password.length() - 1
                        );

                        System.out.print("\b \b");
                        System.out.flush();
                    }

                    continue;
                }

                // CTRL+C
                if (ch == 3) {
                    System.out.println();
                    throw new IOException(
                            "Password input cancelled"
                    );
                }

                // Ignore other control characters
                if (Character.isISOControl(ch)) {
                    continue;
                }

                password.append(ch);

                System.out.print("*");
                System.out.flush();
            }

        } finally {
            restoreTerminal();
        }

        System.out.println();

        return password.toString();
    }

    /**
     * Enables character-by-character terminal input.
     * Works on macOS/Linux.
     */
    private static void setTerminalRawMode()
            throws IOException, InterruptedException {

        new ProcessBuilder(
                "/bin/sh",
                "-c",
                "stty -echo -icanon min 1"
        ).inheritIO().start().waitFor();
    }

    /**
     * Restores normal terminal behaviour.
     */
    private static void restoreTerminal()
            throws IOException, InterruptedException {

        new ProcessBuilder(
                "/bin/sh",
                "-c",
                "stty echo icanon"
        ).inheritIO().start().waitFor();
    }

    /**
     * Fallback for environments where System.console()
     * is not available.
     */
    private static String readWithScannerFallback(
            String prompt
    ) {

        System.out.print(prompt);

        java.io.BufferedReader reader =
                new java.io.BufferedReader(
                        new java.io.InputStreamReader(System.in)
                );

        try {
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }
}