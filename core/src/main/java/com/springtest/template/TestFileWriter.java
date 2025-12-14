package com.springtest.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestFileWriter {
    private static final Logger logger = LoggerFactory.getLogger(TestFileWriter.class);

    private final Path outputDirectory;
    private final boolean createDirectories;

    public TestFileWriter(String outputDirectory) {
        this(outputDirectory, true);
    }

    public TestFileWriter(String outputDirectory, boolean createDirectories) {
        if (outputDirectory == null || outputDirectory.isEmpty()) {
            throw new IllegalArgumentException("Output directory cannot be null or empty");
        }
        this.outputDirectory = Paths.get(outputDirectory);
        this.createDirectories = createDirectories;
    }

    public Path writeTestFile(String packageName, String className, String content) {
        if (packageName == null) {
            throw new IllegalArgumentException("Package name cannot be null");
        }
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("Class name cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        logger.info("Writing test file for class: {}.{}", packageName, className);

        Path packagePath = buildPackagePath(packageName);
        Path filePath = packagePath.resolve(className + ".java");

        try {
            if (createDirectories) {
                Files.createDirectories(packagePath);
            }

            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Successfully wrote test file: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("Failed to write test file: {}", filePath, e);
            throw new RuntimeException("Failed to write test file: " + filePath, e);
        }
    }

    public Path writeServiceTest(String packageName, String className, String content) {
        return writeTestFile(packageName, className + "Test", content);
    }

    public Path writeControllerTest(String packageName, String className, String content) {
        return writeTestFile(packageName, className + "Test", content);
    }

    public Path writeRepositoryTest(String packageName, String className, String content) {
        return writeTestFile(packageName, className + "Test", content);
    }

    public Path writeIntegrationTest(String packageName, String className, String content) {
        return writeTestFile(packageName, className + "IntegrationTest", content);
    }

    private Path buildPackagePath(String packageName) {
        String[] parts = packageName.split("\\.");
        Path result = outputDirectory;
        for (String part : parts) {
            result = result.resolve(part);
        }
        return result;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public boolean exists(String packageName, String className) {
        Path packagePath = buildPackagePath(packageName);
        Path filePath = packagePath.resolve(className + ".java");
        return Files.exists(filePath);
    }

    public void deleteTestFile(String packageName, String className) throws IOException {
        Path packagePath = buildPackagePath(packageName);
        Path filePath = packagePath.resolve(className + ".java");
        Files.deleteIfExists(filePath);
        logger.info("Deleted test file: {}", filePath);
    }
}
