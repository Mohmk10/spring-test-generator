package com.springtest.core.analyzer;

import com.springtest.core.config.GeneratorConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class ClassScanner {

    private final GeneratorConfig config;

    public List<Path> scanJavaFiles() {
        List<Path> javaFiles = new ArrayList<>();
        Path sourceDir = config.getSourceDirectory();

        if (!Files.exists(sourceDir)) {
            log.error("Source directory does not exist: {}", sourceDir);
            return javaFiles;
        }

        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(this::shouldIncludeFile)
                    .forEach(javaFiles::add);
        } catch (IOException e) {
            log.error("Failed to scan source directory: {}", sourceDir, e);
        }

        log.info("Found {} Java files to analyze", javaFiles.size());
        return javaFiles;
    }

    private boolean shouldIncludeFile(Path path) {
        String relativePath = config.getSourceDirectory()
                .relativize(path)
                .toString();

        String className = relativePath
                .replace("/", ".")
                .replace("\\", ".")
                .replace(".java", "");

        return config.shouldProcessClass(className);
    }

    public List<Path> scanPackage(String packageName) {
        List<Path> javaFiles = new ArrayList<>();
        Path packagePath = config.getSourceDirectory().resolve(
                packageName.replace(".", "/")
        );

        if (!Files.exists(packagePath)) {
            log.warn("Package directory does not exist: {}", packagePath);
            return javaFiles;
        }

        try (Stream<Path> paths = Files.walk(packagePath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(javaFiles::add);
        } catch (IOException e) {
            log.error("Failed to scan package directory: {}", packagePath, e);
        }

        log.info("Found {} Java files in package {}", javaFiles.size(), packageName);
        return javaFiles;
    }

    public boolean hasTestFile(Path sourceFile) {
        if (!config.isSkipExisting()) {
            return false;
        }

        Path testFile = getExpectedTestPath(sourceFile);
        return Files.exists(testFile);
    }

    public Path getExpectedTestPath(Path sourceFile) {
        Path relativePath = config.getSourceDirectory().relativize(sourceFile);
        String testFileName = sourceFile.getFileName()
                .toString()
                .replace(".java", "Test.java");

        Path testPackageDir = config.getTestDirectory().resolve(relativePath.getParent());
        return testPackageDir.resolve(testFileName);
    }
}