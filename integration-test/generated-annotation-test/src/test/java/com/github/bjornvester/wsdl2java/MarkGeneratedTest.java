package com.github.bjornvester.wsdl2java;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MarkGeneratedTest {
    AtomicInteger filesWithGeneratedAnnotation = new AtomicInteger(0);

    @Test
    void testMarkGenerated() throws IOException {
        Files.walk(Paths.get("."))
                .filter(path -> path.toString().contains("markgenerated") && path.getFileName().toString().endsWith(".java"))
                .forEach(this::checkGeneratedAnnotations);

        assertEquals(4, filesWithGeneratedAnnotation.get(), "Unexpected number of source files found with the @Generated annotation");
    }

    private void checkGeneratedAnnotations(final Path path) {
        try {
            String javaFile = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
            if (javaFile.contains("@Generated")) {
                filesWithGeneratedAnnotation.incrementAndGet();
                assertFalse(javaFile.contains("date = "), String.format("Source file %s contains a date", path));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
