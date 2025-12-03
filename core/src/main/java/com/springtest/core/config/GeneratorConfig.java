package com.springtest.core.config;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Value
@Builder
@With
public class GeneratorConfig {

    @Builder.Default
    Path sourceDirectory = Path.of("src/main/java");

    @Builder.Default
    Path testDirectory = Path.of("src/test/java");

    @Builder.Default
    List<String> packagesToScan = List.of();

    @Builder.Default
    Set<String> excludePatterns = Set.of();

    @Builder.Default
    Set<String> includePatterns = Set.of();

    @Builder.Default
    boolean skipExisting = true;

    @Builder.Default
    boolean generateEdgeCases = true;

    @Builder.Default
    boolean generateExceptionTests = true;

    @Builder.Default
    boolean generateIntegrationTests = false;

    @Builder.Default
    String testNamingConvention = "METHOD_SCENARIO_EXPECTED";

    @Builder.Default
    boolean verboseLogging = false;

    @Builder.Default
    boolean dryRun = false;

    @Builder.Default
    int maxTestsPerMethod = 10;

    @Builder.Default
    Path templateDirectory = null;

    @Builder.Default
    boolean useCustomTemplates = false;

    public boolean shouldProcessClass(String className) {
        for (String pattern : excludePatterns) {
            if (matchesPattern(className, pattern)) {
                return false;
            }
        }

        if (includePatterns.isEmpty()) {
            return true;
        }

        for (String pattern : includePatterns) {
            if (matchesPattern(className, pattern)) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldProcessPackage(String packageName) {
        if (packagesToScan.isEmpty()) {
            return true;
        }

        return packagesToScan.stream()
                .anyMatch(p -> packageName.equals(p) || packageName.startsWith(p + "."));
    }

    private boolean matchesPattern(String value, String pattern) {
        String regexPattern = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");

        return value.matches(regexPattern);
    }
}