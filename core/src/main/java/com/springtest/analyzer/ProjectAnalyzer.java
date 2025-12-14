package com.springtest.analyzer;

import com.springtest.model.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ProjectAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(ProjectAnalyzer.class);
    private final ClassScanner classScanner;

    public ProjectAnalyzer() {
        this.classScanner = new ClassScanner();
    }

    public AnalysisResult analyzeFile(String filePath) throws FileNotFoundException {
        logger.info("Analyzing file: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        if (!file.isFile() || !file.getName().endsWith(".java")) {
            throw new IllegalArgumentException("Not a Java source file: " + filePath);
        }

        Optional<ClassInfo> classInfo = classScanner.scanFile(file);

        if (classInfo.isPresent()) {
            logger.info("Successfully analyzed file: {}", filePath);
            return AnalysisResult.success(List.of(classInfo.get()));
        } else {
            String error = "Failed to parse file: " + filePath;
            logger.error(error);
            return AnalysisResult.failure(error);
        }
    }

    public AnalysisResult analyzeSource(String sourceCode) {
        logger.debug("Analyzing source code");

        if (sourceCode == null || sourceCode.isBlank()) {
            return AnalysisResult.failure("Source code is null or empty");
        }

        Optional<ClassInfo> classInfo = classScanner.scanSource(sourceCode);

        if (classInfo.isPresent()) {
            logger.debug("Successfully analyzed source code");
            return AnalysisResult.success(List.of(classInfo.get()));
        } else {
            String error = "Failed to parse source code";
            logger.error(error);
            return AnalysisResult.failure(error);
        }
    }

    public AnalysisResult analyzeProject(String projectPath) throws IOException {
        logger.info("Analyzing project: {}", projectPath);

        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            return AnalysisResult.failure("Project directory does not exist or is not a directory: " + projectPath);
        }

        List<ClassInfo> classes = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        List<File> javaFiles = findJavaFiles(projectDir.toPath());
        logger.info("Found {} Java files in project", javaFiles.size());

        for (File javaFile : javaFiles) {
            try {
                Optional<ClassInfo> classInfo = classScanner.scanFile(javaFile);
                classInfo.ifPresent(classes::add);
            } catch (FileNotFoundException e) {
                String error = "File not found: " + javaFile.getAbsolutePath();
                logger.error(error, e);
                errors.add(error);
            } catch (Exception e) {
                String error = "Error analyzing file " + javaFile.getAbsolutePath() + ": " + e.getMessage();
                logger.error(error, e);
                errors.add(error);
            }
        }

        logger.info("Successfully analyzed {} classes from project", classes.size());

        if (classes.isEmpty() && !errors.isEmpty()) {
            return AnalysisResult.failure("No classes analyzed. Errors: " + String.join("; ", errors));
        }

        return AnalysisResult.success(classes, errors);
    }

    private List<File> findJavaFiles(Path projectPath) throws IOException {
        List<File> javaFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(projectPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toFile()));
        }

        return javaFiles;
    }

    public static class AnalysisResult {
        private final boolean success;
        private final List<ClassInfo> classes;
        private final List<String> errors;

        private AnalysisResult(boolean success, List<ClassInfo> classes, List<String> errors) {
            this.success = success;
            this.classes = classes != null ? List.copyOf(classes) : List.of();
            this.errors = errors != null ? List.copyOf(errors) : List.of();
        }

        public static AnalysisResult success(List<ClassInfo> classes) {
            return new AnalysisResult(true, classes, List.of());
        }

        public static AnalysisResult success(List<ClassInfo> classes, List<String> errors) {
            return new AnalysisResult(true, classes, errors);
        }

        public static AnalysisResult failure(String error) {
            return new AnalysisResult(false, List.of(), List.of(error));
        }

        public static AnalysisResult failure(List<String> errors) {
            return new AnalysisResult(false, List.of(), errors);
        }

        public boolean isSuccess() {
            return success;
        }

        public List<ClassInfo> getClasses() {
            return classes;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public String getSummary() {
            if (success) {
                String summary = String.format("Successfully analyzed %d class(es)", classes.size());
                if (!errors.isEmpty()) {
                    summary += String.format(" with %d warning(s)", errors.size());
                }
                return summary;
            } else {
                return String.format("Analysis failed with %d error(s)", errors.size());
            }
        }

        @Override
        public String toString() {
            return "AnalysisResult{" +
                   "success=" + success +
                   ", classes=" + classes.size() +
                   ", errors=" + errors.size() +
                   '}';
        }
    }
}
