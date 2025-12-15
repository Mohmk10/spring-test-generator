package com.springtest.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class AnalyzeTaskTest {

    @TempDir
    Path tempDir;

    private Project project;
    private AnalyzeTask task;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(tempDir.toFile()).build();
        task = project.getTasks().create("testAnalyze", AnalyzeTask.class);
    }

    @Test
    void testTaskHasCorrectGroup() {
        assertThat(task.getGroup()).isEqualTo("spring test generator");
    }

    @Test
    void testTaskHasDescription() {
        assertThat(task.getDescription()).isNotNull()
                .contains("Analyzes");
    }

    @Test
    void testSetters() {
        task.setSourceDirectory("custom/src");
        task.setDetailed(true);
    }

    @Test
    void testAnalyze_WithNonExistentSourceDirectory() {
        task.setSourceDirectory("nonexistent");
        task.setDetailed(false);

        assertThatThrownBy(() -> task.analyze())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source directory does not exist");
    }

    @Test
    void testAnalyze_WithEmptyDirectory() throws Exception {
        Path sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);

        task.setSourceDirectory(sourceDir.toString());
        task.setDetailed(false);

        task.analyze();
    }

    @Test
    void testAnalyze_WithSpringServiceClass() throws Exception {
        Path sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir.resolve("com/example"));

        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class UserService {
                public void save() {}
            }
            """;
        Files.writeString(sourceDir.resolve("com/example/UserService.java"), serviceClass);

        task.setSourceDirectory(sourceDir.toString());
        task.setDetailed(false);

        task.analyze();
    }

    @Test
    void testAnalyze_WithDetailedFlag() throws Exception {
        Path sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir.resolve("com/example"));

        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class OrderService {
                private String name;
                public void createOrder() {}
                public void deleteOrder() {}
            }
            """;
        Files.writeString(sourceDir.resolve("com/example/OrderService.java"), serviceClass);

        task.setSourceDirectory(sourceDir.toString());
        task.setDetailed(true);

        task.analyze();
    }

    @Test
    void testAnalyze_WithMultipleSpringClasses() throws Exception {
        Path sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir.resolve("com/example"));

        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class DataService {}
            """;
        Files.writeString(sourceDir.resolve("com/example/DataService.java"), serviceClass);

        String controllerClass = """
            package com.example;
            import org.springframework.web.bind.annotation.RestController;
            @RestController
            public class DataController {}
            """;
        Files.writeString(sourceDir.resolve("com/example/DataController.java"), controllerClass);

        task.setSourceDirectory(sourceDir.toString());
        task.setDetailed(false);

        task.analyze();
    }

    @Test
    void testAnalyze_WithMixedClasses() throws Exception {
        Path sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir.resolve("com/example"));

        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class MyService {}
            """;
        Files.writeString(sourceDir.resolve("com/example/MyService.java"), serviceClass);

        String plainClass = """
            package com.example;
            public class PlainClass {}
            """;
        Files.writeString(sourceDir.resolve("com/example/PlainClass.java"), plainClass);

        task.setSourceDirectory(sourceDir.toString());
        task.setDetailed(false);

        task.analyze();
    }
}
