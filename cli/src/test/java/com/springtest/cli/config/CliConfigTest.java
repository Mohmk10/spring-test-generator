package com.springtest.cli.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class CliConfigTest {

    @TempDir
    Path tempDir;

    private CliConfig config;

    @BeforeEach
    void setUp() {
        config = new CliConfig();
    }

    @Test
    void testDefaultValues() {
        assertThat(config.getDefaultSourcePath()).isEqualTo("src/main/java");
        assertThat(config.getDefaultOutputPath()).isEqualTo("src/test/java");
        assertThat(config.getDefaultTestType()).isEqualTo("all");
        assertThat(config.getDefaultNamingStrategy()).isEqualTo("method-scenario");
    }

    @Test
    void testSetters() {
        config.setDefaultSourcePath("custom/src");
        config.setDefaultOutputPath("custom/test");
        config.setDefaultTestType("unit");
        config.setDefaultNamingStrategy("bdd");

        assertThat(config.getDefaultSourcePath()).isEqualTo("custom/src");
        assertThat(config.getDefaultOutputPath()).isEqualTo("custom/test");
        assertThat(config.getDefaultTestType()).isEqualTo("unit");
        assertThat(config.getDefaultNamingStrategy()).isEqualTo("bdd");
    }

    @Test
    void testToYaml() throws IOException {
        config.setDefaultSourcePath("src");
        config.setDefaultOutputPath("test");
        config.setDefaultTestType("integration");
        config.setDefaultNamingStrategy("given-when");

        config.saveToDirectory(tempDir.toString());

        Path configFile = tempDir.resolve(".springtest.yml");
        assertThat(configFile).exists();

        String content = Files.readString(configFile);
        assertThat(content).contains("defaultSourcePath: src");
        assertThat(content).contains("defaultOutputPath: test");
        assertThat(content).contains("defaultTestType: integration");
        assertThat(content).contains("defaultNamingStrategy: given-when");
    }

    @Test
    void testLoadFromDirectory() throws IOException {
        String yaml = """
            defaultSourcePath: loaded/src
            defaultOutputPath: loaded/test
            defaultTestType: unit
            defaultNamingStrategy: bdd
            """;

        Files.writeString(tempDir.resolve(".springtest.yml"), yaml);

        CliConfig loaded = CliConfig.loadFromDirectory(tempDir.toString());

        assertThat(loaded.getDefaultSourcePath()).isEqualTo("loaded/src");
        assertThat(loaded.getDefaultOutputPath()).isEqualTo("loaded/test");
        assertThat(loaded.getDefaultTestType()).isEqualTo("unit");
        assertThat(loaded.getDefaultNamingStrategy()).isEqualTo("bdd");
    }

    @Test
    void testLoadFromNonExistentDirectory() {
        CliConfig loaded = CliConfig.loadFromDirectory("/nonexistent");

        assertThat(loaded.getDefaultSourcePath()).isEqualTo("src/main/java");
        assertThat(loaded.getDefaultOutputPath()).isEqualTo("src/test/java");
    }

    @Test
    void testToMap() {
        config.setDefaultSourcePath("src");
        config.setDefaultOutputPath("test");
        config.setDefaultTestType("all");
        config.setDefaultNamingStrategy("method-scenario");

        Map<String, String> map = config.toMap();

        assertThat(map).containsEntry("defaultSourcePath", "src");
        assertThat(map).containsEntry("defaultOutputPath", "test");
        assertThat(map).containsEntry("defaultTestType", "all");
        assertThat(map).containsEntry("defaultNamingStrategy", "method-scenario");
    }

    @Test
    void testSaveAndLoad() throws IOException {
        config.setDefaultSourcePath("custom/src");
        config.setDefaultOutputPath("custom/test");
        config.setDefaultTestType("integration");
        config.setDefaultNamingStrategy("given-when");

        config.saveToDirectory(tempDir.toString());

        CliConfig loaded = CliConfig.loadFromDirectory(tempDir.toString());

        assertThat(loaded.getDefaultSourcePath()).isEqualTo("custom/src");
        assertThat(loaded.getDefaultOutputPath()).isEqualTo("custom/test");
        assertThat(loaded.getDefaultTestType()).isEqualTo("integration");
        assertThat(loaded.getDefaultNamingStrategy()).isEqualTo("given-when");
    }

    @Test
    void testLoadWithComments() throws IOException {
        String yaml = """
            # This is a comment
            defaultSourcePath: src
            # Another comment
            defaultOutputPath: test
            defaultTestType: all
            defaultNamingStrategy: method-scenario
            """;

        Files.writeString(tempDir.resolve(".springtest.yml"), yaml);

        CliConfig loaded = CliConfig.loadFromDirectory(tempDir.toString());

        assertThat(loaded.getDefaultSourcePath()).isEqualTo("src");
        assertThat(loaded.getDefaultOutputPath()).isEqualTo("test");
    }

    @Test
    void testLoadWithEmptyLines() throws IOException {
        String yaml = """
            defaultSourcePath: src

            defaultOutputPath: test

            defaultTestType: all
            defaultNamingStrategy: method-scenario
            """;

        Files.writeString(tempDir.resolve(".springtest.yml"), yaml);

        CliConfig loaded = CliConfig.loadFromDirectory(tempDir.toString());

        assertThat(loaded.getDefaultSourcePath()).isEqualTo("src");
        assertThat(loaded.getDefaultOutputPath()).isEqualTo("test");
    }
}
