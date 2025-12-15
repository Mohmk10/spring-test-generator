package com.springtest.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class GenerateTestsTaskTest {

    @TempDir
    Path tempDir;

    private Project project;
    private GenerateTestsTask task;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(tempDir.toFile()).build();
        task = project.getTasks().create("testGenerateTests", GenerateTestsTask.class);
    }

    @Test
    void testTaskHasCorrectGroup() {
        assertThat(task.getGroup()).isEqualTo("spring test generator");
    }

    @Test
    void testTaskHasDescription() {
        assertThat(task.getDescription()).isNotNull()
                .contains("Generates");
    }

    @Test
    void testSetters() {
        task.setSourceDirectory("custom/src");
        task.setOutputDirectory("custom/test");
        task.setTestType("unit");
        task.setNamingStrategy("bdd");
    }

    @Test
    void testGenerateTests_WithNonExistentSourceDirectory() {
        task.setSourceDirectory("nonexistent");
        task.setOutputDirectory(tempDir.resolve("output").toString());
        task.setTestType("unit");
        task.setNamingStrategy("method-scenario");

        assertThatThrownBy(() -> task.generateTests())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source directory does not exist");
    }

    @Test
    void testGenerateTests_WithEmptySourceDirectory() throws Exception {
        Path sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);

        task.setSourceDirectory(sourceDir.toString());
        task.setOutputDirectory(tempDir.resolve("output").toString());
        task.setTestType("unit");
        task.setNamingStrategy("method-scenario");

        task.generateTests();
    }

    @Test
    void testGenerateTests_WithSpringServiceClass() throws Exception {
        Path sourceDir = tempDir.resolve("src/main/java");
        Files.createDirectories(sourceDir.resolve("com/example"));

        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class UserService {
                public String findById(Long id) {
                    return "User-" + id;
                }
            }
            """;
        Files.writeString(sourceDir.resolve("com/example/UserService.java"), serviceClass);

        Path outputDir = tempDir.resolve("src/test/java");

        task.setSourceDirectory(sourceDir.toString());
        task.setOutputDirectory(outputDir.toString());
        task.setTestType("unit");
        task.setNamingStrategy("method-scenario");

        task.generateTests();

        assertThat(outputDir.resolve("com/example/UserServiceTest.java")).exists();
    }

    @Test
    void testGenerateTests_CreatesOutputDirectory() throws Exception {
        Path sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir.resolve("com/example"));

        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class TestService {
                public void test() {}
            }
            """;
        Files.writeString(sourceDir.resolve("com/example/TestService.java"), serviceClass);

        Path outputDir = tempDir.resolve("newdir");

        task.setSourceDirectory(sourceDir.toString());
        task.setOutputDirectory(outputDir.toString());
        task.setTestType("unit");
        task.setNamingStrategy("method-scenario");

        task.generateTests();

        assertThat(outputDir).exists();
    }
}
