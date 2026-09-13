package com.anshubank.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileManager {

    private FileManager() {
    }

    public static Path writeReport(
            String name,
            String content
    ) throws IOException {

        Path dir = Path.of("reports");
        Files.createDirectories(dir);

        Path p = dir.resolve(name);

        try (BufferedWriter w = Files.newBufferedWriter(p)) {
            w.write(content);
        }

        return p;
    }
}