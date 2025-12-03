package com.springtest.core.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ConfigLoader {

    private static final String DEFAULT_CONFIG_FILE = ".spring-test-gen.properties";

    public static GeneratorConfig loadFromFile(Path configFilePath) {
        if (!Files.exists(configFilePath)) {
            log.warn("Config file not found: {}, using defaults", configFilePath);
            return GeneratorConfig.builder().build();
        }

        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(configFilePath));

            return fromProperties(props);
        } catch (IOException e) {
            log.error("Failed to load config from: {}", configFilePath, e);
            return GeneratorConfig.builder().build();
        }
    }

    public static GeneratorConfig loadDefault() {
        Path defaultPath = Path.of(System.getProperty("user.dir"), DEFAULT_CONFIG_FILE);

        if (Files.exists(defaultPath)) {
            return loadFromFile(defaultPath);
        }

        return GeneratorConfig.builder().build();
    }

    private static GeneratorConfig fromProperties(Properties props) {
        var builder = GeneratorConfig.builder();

        String sourceDir = props.getProperty("sourceDirectory");
        if (sourceDir != null) {
            builder.sourceDirectory(Path.of(sourceDir));
        }

        String testDir = props.getProperty("testDirectory");
        if (testDir != null) {
            builder.testDirectory(Path.of(testDir));
        }

        String packages = props.getProperty("packagesToScan");
        if (packages != null) {
            builder.packagesToScan(parseList(packages));
        }

        String excludes = props.getProperty("excludePatterns");
        if (excludes != null) {
            builder.excludePatterns(new HashSet<>(parseList(excludes)));
        }

        String includes = props.getProperty("includePatterns");
        if (includes != null) {
            builder.includePatterns(new HashSet<>(parseList(includes)));
        }

        String skipExisting = props.getProperty("skipExisting");
        if (skipExisting != null) {
            builder.skipExisting(Boolean.parseBoolean(skipExisting));
        }

        String generateEdgeCases = props.getProperty("generateEdgeCases");
        if (generateEdgeCases != null) {
            builder.generateEdgeCases(Boolean.parseBoolean(generateEdgeCases));
        }

        String generateExceptionTests = props.getProperty("generateExceptionTests");
        if (generateExceptionTests != null) {
            builder.generateExceptionTests(Boolean.parseBoolean(generateExceptionTests));
        }

        String generateIntegrationTests = props.getProperty("generateIntegrationTests");
        if (generateIntegrationTests != null) {
            builder.generateIntegrationTests(Boolean.parseBoolean(generateIntegrationTests));
        }

        String namingConvention = props.getProperty("testNamingConvention");
        if (namingConvention != null) {
            builder.testNamingConvention(namingConvention);
        }

        String verbose = props.getProperty("verboseLogging");
        if (verbose != null) {
            builder.verboseLogging(Boolean.parseBoolean(verbose));
        }

        String dryRun = props.getProperty("dryRun");
        if (dryRun != null) {
            builder.dryRun(Boolean.parseBoolean(dryRun));
        }

        String maxTests = props.getProperty("maxTestsPerMethod");
        if (maxTests != null) {
            builder.maxTestsPerMethod(Integer.parseInt(maxTests));
        }

        String templateDir = props.getProperty("templateDirectory");
        if (templateDir != null) {
            builder.templateDirectory(Path.of(templateDir));
            builder.useCustomTemplates(true);
        }

        return builder.build();
    }

    private static List<String> parseList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String[] parts = value.split("[,;]");
        List<String> result = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        return result;
    }

    public static void saveToFile(GeneratorConfig config, Path outputPath) throws IOException {
        Properties props = new Properties();

        props.setProperty("sourceDirectory", config.getSourceDirectory().toString());
        props.setProperty("testDirectory", config.getTestDirectory().toString());

        if (!config.getPackagesToScan().isEmpty()) {
            props.setProperty("packagesToScan", String.join(",", config.getPackagesToScan()));
        }

        if (!config.getExcludePatterns().isEmpty()) {
            props.setProperty("excludePatterns", String.join(",", config.getExcludePatterns()));
        }

        if (!config.getIncludePatterns().isEmpty()) {
            props.setProperty("includePatterns", String.join(",", config.getIncludePatterns()));
        }

        props.setProperty("skipExisting", String.valueOf(config.isSkipExisting()));
        props.setProperty("generateEdgeCases", String.valueOf(config.isGenerateEdgeCases()));
        props.setProperty("generateExceptionTests", String.valueOf(config.isGenerateExceptionTests()));
        props.setProperty("generateIntegrationTests", String.valueOf(config.isGenerateIntegrationTests()));
        props.setProperty("testNamingConvention", config.getTestNamingConvention());
        props.setProperty("verboseLogging", String.valueOf(config.isVerboseLogging()));
        props.setProperty("dryRun", String.valueOf(config.isDryRun()));
        props.setProperty("maxTestsPerMethod", String.valueOf(config.getMaxTestsPerMethod()));

        if (config.getTemplateDirectory() != null) {
            props.setProperty("templateDirectory", config.getTemplateDirectory().toString());
        }

        Files.createDirectories(outputPath.getParent());
        props.store(Files.newOutputStream(outputPath), "Spring Test Generator Configuration");
    }
}