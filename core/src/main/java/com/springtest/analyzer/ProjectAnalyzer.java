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

/**
 * Public facade for analyzing Java projects and extracting class information.
 * This is the main entry point for code analysis functionality.
 */
public class ProjectAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(ProjectAnalyzer.class);
    private final ClassScanner classScanner;

    /**
     * Creates a new ProjectAnalyzer instance.
     */
    public ProjectAnalyzer() {
        this.classScanner = new ClassScanner();
    }

    /**
     * Analyzes a single Java source file.
     *
     * @param filePath Path to the Java source file
     * @return AnalysisResult containing the class information if successful
     * @throws FileNotFoundException if the file does not exist
     */
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

    /**
     * Analyzes Java source code provided as a string.
     *
     * @param sourceCode Java source code to analyze
     * @return AnalysisResult containing the class information if successful
     */
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

    /**
     * Analyzes all Java files in a project directory.
     * Recursively scans the directory for .java files.
     *
     * @param projectPath Path to the project directory
     * @return AnalysisResult containing all discovered classes
     * @throws IOException if there's an error reading the directory
     */
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

    /**
     * Finds all Java source files in a directory recursively.
     *
     * @param projectPath Path to search
     * @return List of Java files
     * @throws IOException if there's an error reading the directory
     */
    private List<File> findJavaFiles(Path projectPath) throws IOException {
        List<File> javaFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(projectPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toFile()));
        }

        return javaFiles;
    }

    /**
     * Result of a code analysis operation.
     * Contains the analyzed classes and any errors encountered.
     */
    public static class AnalysisResult {
        private final boolean success;
        private final List<ClassInfo> classes;
        private final List<String> errors;

        private AnalysisResult(boolean success, List<ClassInfo> classes, List<String> errors) {
            this.success = success;
            this.classes = classes != null ? List.copyOf(classes) : List.of();
            this.errors = errors != null ? List.copyOf(errors) : List.of();
        }

        /**
         * Creates a successful analysis result.
         *
         * @param classes List of analyzed classes
         * @return AnalysisResult instance
         */
        public static AnalysisResult success(List<ClassInfo> classes) {
            return new AnalysisResult(true, classes, List.of());
        }

        /**
         * Creates a successful analysis result with warnings.
         *
         * @param classes List of analyzed classes
         * @param errors  List of error messages (warnings)
         * @return AnalysisResult instance
         */
        public static AnalysisResult success(List<ClassInfo> classes, List<String> errors) {
            return new AnalysisResult(true, classes, errors);
        }

        /**
         * Creates a failed analysis result.
         *
         * @param error Error message
         * @return AnalysisResult instance
         */
        public static AnalysisResult failure(String error) {
            return new AnalysisResult(false, List.of(), List.of(error));
        }

        /**
         * Creates a failed analysis result with multiple errors.
         *
         * @param errors List of error messages
         * @return AnalysisResult instance
         */
        public static AnalysisResult failure(List<String> errors) {
            return new AnalysisResult(false, List.of(), errors);
        }

        /**
         * Checks if the analysis was successful.
         *
         * @return true if analysis succeeded
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Gets the list of analyzed classes.
         *
         * @return List of ClassInfo objects
         */
        public List<ClassInfo> getClasses() {
            return classes;
        }

        /**
         * Gets the list of errors encountered during analysis.
         *
         * @return List of error messages
         */
        public List<String> getErrors() {
            return errors;
        }

        /**
         * Checks if there are any errors.
         *
         * @return true if there are errors
         */
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        /**
         * Gets a summary of the analysis result.
         *
         * @return Summary string
         */
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
