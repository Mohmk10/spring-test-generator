package com.springtest.core.config;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratorConfigTest {

    @Test
    void builder_ShouldCreateConfigWithDefaults() {
        GeneratorConfig config = GeneratorConfig.builder().build();

        assertThat(config.getSourceDirectory()).isEqualTo(Path.of("src/main/java"));
        assertThat(config.getTestDirectory()).isEqualTo(Path.of("src/test/java"));
        assertThat(config.isSkipExisting()).isTrue();
        assertThat(config.isGenerateEdgeCases()).isTrue();
        assertThat(config.isGenerateExceptionTests()).isTrue();
        assertThat(config.getTestNamingConvention()).isEqualTo("METHOD_SCENARIO_EXPECTED");
    }

    @Test
    void with_ShouldCreateModifiedCopy() {
        GeneratorConfig config = GeneratorConfig.builder()
                .skipExisting(true)
                .build();

        GeneratorConfig modified = config.withSkipExisting(false);

        assertThat(config.isSkipExisting()).isTrue();
        assertThat(modified.isSkipExisting()).isFalse();
    }

    @Test
    void shouldProcessClass_WithNoPatterns_ShouldReturnTrue() {
        GeneratorConfig config = GeneratorConfig.builder().build();

        boolean result = config.shouldProcessClass("com.example.UserService");

        assertThat(result).isTrue();
    }

    @Test
    void shouldProcessClass_WithExcludePattern_ShouldReturnFalse() {
        GeneratorConfig config = GeneratorConfig.builder()
                .excludePatterns(Set.of("*Test*"))
                .build();

        boolean result = config.shouldProcessClass("com.example.UserServiceTest");

        assertThat(result).isFalse();
    }

    @Test
    void shouldProcessClass_WithIncludePattern_ShouldReturnTrue() {
        GeneratorConfig config = GeneratorConfig.builder()
                .includePatterns(Set.of("*Service*"))
                .build();

        boolean result = config.shouldProcessClass("com.example.UserService");

        assertThat(result).isTrue();
    }

    @Test
    void shouldProcessClass_NotMatchingIncludePattern_ShouldReturnFalse() {
        GeneratorConfig config = GeneratorConfig.builder()
                .includePatterns(Set.of("*Service*"))
                .build();

        boolean result = config.shouldProcessClass("com.example.UserController");

        assertThat(result).isFalse();
    }

    @Test
    void shouldProcessPackage_WithNoPackages_ShouldReturnTrue() {
        GeneratorConfig config = GeneratorConfig.builder().build();

        boolean result = config.shouldProcessPackage("com.example.service");

        assertThat(result).isTrue();
    }

    @Test
    void shouldProcessPackage_WithMatchingPackage_ShouldReturnTrue() {
        GeneratorConfig config = GeneratorConfig.builder()
                .packagesToScan(List.of("com.example.service"))
                .build();

        boolean result = config.shouldProcessPackage("com.example.service");

        assertThat(result).isTrue();
    }

    @Test
    void shouldProcessPackage_WithSubPackage_ShouldReturnTrue() {
        GeneratorConfig config = GeneratorConfig.builder()
                .packagesToScan(List.of("com.example"))
                .build();

        boolean result = config.shouldProcessPackage("com.example.service");

        assertThat(result).isTrue();
    }

    @Test
    void shouldProcessPackage_NotMatchingPackage_ShouldReturnFalse() {
        GeneratorConfig config = GeneratorConfig.builder()
                .packagesToScan(List.of("com.example.service"))
                .build();

        boolean result = config.shouldProcessPackage("com.other.service");

        assertThat(result).isFalse();
    }
}