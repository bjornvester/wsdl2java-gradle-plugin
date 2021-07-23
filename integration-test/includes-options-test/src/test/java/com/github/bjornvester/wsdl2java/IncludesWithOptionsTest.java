package com.github.bjornvester.wsdl2java;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IncludesWithOptionsTest {
    boolean foundA = false;
    boolean foundB = false;

    @Test
    void testIndividualOptions() throws IOException {
        Files.walk(Paths.get("."))
                .filter(path ->
                        path.toString().contains("includes_with_options") && path.getFileName().toString().endsWith(".java"))
                .forEach(this::checkWsdlLocation);

        assertTrue(foundA, "Did not find service A");
        assertTrue(foundB, "Did not find service B");
    }

    private void checkWsdlLocation(final Path path) {
        if (path.getFileName().toString().equals("HelloWorldAService.java")) {
            foundA = true;
            verifyWsdlLocationParam(path, "MyLocationA");
        } else if (path.getFileName().toString().equals("HelloWorldBService.java")) {
            foundB = true;
            verifyWsdlLocationParam(path, "MyLocationB");
        }
    }

    private void verifyWsdlLocationParam(final Path path, final String expectedValue) {
        try {
            String javaFile = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
            assertTrue(javaFile.contains("wsdlLocation = \"" + expectedValue + "\""),
                    String.format("Source file %s did not contain the expected wsdlLocation '%s'", path, expectedValue));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
