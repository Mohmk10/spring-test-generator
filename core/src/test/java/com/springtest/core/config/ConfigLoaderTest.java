package com.springtest.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadFromFile_WithNonExistentFile_ShouldReturnDefaults() {
        Path nonExistent = tempDir.resolve("config.properties");

        GeneratorConfig config = ConfigLoader.loadFromFile(nonExistent);

        assertThat(config.isSkipExisting()).isTrue();
        assertThat(config.isGenerateEdgeCases()).isTrue();
    }

    @Test
    void loadFromFile_WithValidFile_ShouldLoadProperties() throws IOException {
        Path configFile = tempDir.resolve("config.properties");
        String content = """
            sourceDirectory=src/main/kotlin
            testDirectory=src/test/kotlin
            skipExisting=false
            generateEdgeCases=false
            testNamingConvention=GIVEN_WHEN_THEN
            verboseLogging=true
            """;
        Files.writeString(configFile, content);

        GeneratorConfig config = ConfigLoader.loadFromFile(configFile);

        assertThat(config.getSourceDirectory()).isEqualTo(Path.of("src/main/kotlin"));
        assertThat(config.getTestDirectory()).isEqualTo(Path.of("src/test/kotlin"));
        assertThat(config.isSkipExisting()).isFalse();
        assertThat(config.isGenerateEdgeCases()).isFalse();
        assertThat(config.getTestNamingConvention()).isEqualTo("GIVEN_WHEN_THEN");
        assertThat(config.isVerboseLogging()).isTrue();
    }

    @Test
    void loadFromFile_WithPackages_ShouldParseList() throws IOException {
        Path configFile = tempDir.resolve("config.properties");
        String content = """
            packagesToScan=com.example.service,com.example.controller
            """;
        Files.writeString(configFile, content);

        GeneratorConfig config = ConfigLoader.loadFromFile(configFile);

        assertThat(config.getPackagesToScan())
                .containsExactly("com.example.service", "com.example.controller");
    }

    @Test
    void loadFromFile_WithExcludePatterns_ShouldParseSet() throws IOException {
        Path configFile = tempDir.resolve("config.properties");
        String content = """
            excludePatterns=.*Test.*,.*IT.*
            """;
        Files.writeString(configFile, content);

        GeneratorConfig config = ConfigLoader.loadFromFile(configFile);

        assertThat(config.getExcludePatterns())
                .containsExactlyInAnyOrder(".*Test.*", ".*IT.*");
    }

    @Test
    void saveToFile_ShouldWriteProperties() throws IOException {
        GeneratorConfig config = GeneratorConfig.builder()
                .sourceDirectory(Path.of("src/main/kotlin"))
                .skipExisting(false)
                .generateEdgeCases(true)
                .testNamingConvention("BDD")
                .build();

        Path outputFile = tempDir.resolve("output.properties");
        ConfigLoader.saveToFile(config, outputFile);

        assertThat(Files.exists(outputFile)).isTrue();
        String content = Files.readString(outputFile);
        assertThat(content).contains("sourceDirectory=src/main/kotlin");
        assertThat(content).contains("skipExisting=false");
        assertThat(content).contains("generateEdgeCases=true");
        assertThat(content).contains("testNamingConvention=BDD");
    }

    @Test
    void loadDefault_WithNoDefaultFile_ShouldReturnDefaults() {
        GeneratorConfig config = ConfigLoader.loadDefault();

        assertThat(config).isNotNull();
        assertThat(config.isSkipExisting()).isTrue();
    }
}