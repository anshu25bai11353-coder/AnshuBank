package com.anshubank.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public final class LoggerUtil {

    private LoggerUtil() {
    }

    public static synchronized void log(String level, String msg) {
        try {
            File dir = new File("logs");
            dir.mkdirs();

            try (BufferedWriter w = new BufferedWriter(
                    new FileWriter(new File(dir, "anshubank.log"), true)
            )) {
                w.write(
                        LocalDateTime.now()
                                + " [" + level + "] "
                                + msg
                );
                w.newLine();
            }

        } catch (IOException ignored) {
        }
    }

    public static void info(String m) {
        log("INFO", m);
    }

    public static void warn(String m) {
        log("WARN", m);
    }

    public static void error(String m) {
        log("ERROR", m);
    }
}