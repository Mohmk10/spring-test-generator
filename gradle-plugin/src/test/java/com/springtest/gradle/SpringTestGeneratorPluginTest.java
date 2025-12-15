package com.springtest.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SpringTestGeneratorPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void testPluginApplies() {
        project.getPluginManager().apply("com.springtest.gradle-plugin");

        assertThat(project.getExtensions().findByName("springTestGenerator"))
                .isNotNull()
                .isInstanceOf(SpringTestGeneratorExtension.class);
    }

    @Test
    void testGenerateTestsTaskRegistered() {
        project.getPluginManager().apply("com.springtest.gradle-plugin");

        assertThat(project.getTasks().findByName("generateTests"))
                .isNotNull()
                .isInstanceOf(GenerateTestsTask.class);
    }

    @Test
    void testAnalyzeSpringClassesTaskRegistered() {
        project.getPluginManager().apply("com.springtest.gradle-plugin");

        assertThat(project.getTasks().findByName("analyzeSpringClasses"))
                .isNotNull()
                .isInstanceOf(AnalyzeTask.class);
    }

    @Test
    void testExtensionHasDefaultValues() {
        project.getPluginManager().apply("com.springtest.gradle-plugin");

        SpringTestGeneratorExtension extension = project.getExtensions()
                .getByType(SpringTestGeneratorExtension.class);

        assertThat(extension.getSourceDirectory().get()).isEqualTo("src/main/java");
        assertThat(extension.getOutputDirectory().get()).isEqualTo("src/test/java");
        assertThat(extension.getTestType().get()).isEqualTo("all");
        assertThat(extension.getNamingStrategy().get()).isEqualTo("method-scenario");
    }

    @Test
    void testExtensionCanBeConfigured() {
        project.getPluginManager().apply("com.springtest.gradle-plugin");

        SpringTestGeneratorExtension extension = project.getExtensions()
                .getByType(SpringTestGeneratorExtension.class);

        extension.getSourceDirectory().set("custom/src");
        extension.getOutputDirectory().set("custom/test");
        extension.getTestType().set("unit");
        extension.getNamingStrategy().set("bdd");

        assertThat(extension.getSourceDirectory().get()).isEqualTo("custom/src");
        assertThat(extension.getOutputDirectory().get()).isEqualTo("custom/test");
        assertThat(extension.getTestType().get()).isEqualTo("unit");
        assertThat(extension.getNamingStrategy().get()).isEqualTo("bdd");
    }

    @Test
    void testTasksHaveCorrectGroup() {
        project.getPluginManager().apply("com.springtest.gradle-plugin");

        GenerateTestsTask generateTask = (GenerateTestsTask) project.getTasks()
                .findByName("generateTests");
        AnalyzeTask analyzeTask = (AnalyzeTask) project.getTasks()
                .findByName("analyzeSpringClasses");

        assertThat(generateTask.getGroup()).isEqualTo("spring test generator");
        assertThat(analyzeTask.getGroup()).isEqualTo("spring test generator");
    }

    @Test
    void testTasksHaveDescriptions() {
        project.getPluginManager().apply("com.springtest.gradle-plugin");

        GenerateTestsTask generateTask = (GenerateTestsTask) project.getTasks()
                .findByName("generateTests");
        AnalyzeTask analyzeTask = (AnalyzeTask) project.getTasks()
                .findByName("analyzeSpringClasses");

        assertThat(generateTask.getDescription()).isNotNull();
        assertThat(analyzeTask.getDescription()).isNotNull();
    }
}
