package com.springtest.analyzer;

import com.springtest.model.ClassInfo;
import com.springtest.model.ClassType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectAnalyzerTest {

    private ProjectAnalyzer projectAnalyzer;

    @BeforeEach
    void setUp() {
        projectAnalyzer = new ProjectAnalyzer();
    }

    @Test
    void shouldAnalyzeSource_WhenValidSourceCodeIsProvided() {

        String sourceCode = """
                package com.example.service;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService {
                    public String findById(Long id) {
                        return "user";
                    }
                }
                """;

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeSource(sourceCode);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getClasses()).hasSize(1);
        assertThat(result.hasErrors()).isFalse();

        ClassInfo classInfo = result.getClasses().get(0);
        assertThat(classInfo.simpleName()).isEqualTo("UserService");
        assertThat(classInfo.classType()).isEqualTo(ClassType.SERVICE);
    }

    @Test
    void shouldReturnFailure_WhenSourceCodeIsNull() {

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeSource(null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getClasses()).isEmpty();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).contains("Source code is null or empty");
    }

    @Test
    void shouldReturnFailure_WhenSourceCodeIsEmpty() {

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeSource("");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void shouldReturnFailure_WhenSourceCodeIsInvalid() {

        String invalidSourceCode = """
                package com.example;

                public class InvalidClass {

                """;

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeSource(invalidSourceCode);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getClasses()).isEmpty();
        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void shouldAnalyzeFile_WhenValidFileIsProvided(@TempDir Path tempDir) throws IOException {

        String sourceCode = """
                package com.example.controller;

                import org.springframework.web.bind.annotation.RestController;
                import org.springframework.web.bind.annotation.GetMapping;

                @RestController
                public class UserController {
                    @GetMapping("/users")
                    public String getUsers() {
                        return "users";
                    }
                }
                """;

        File javaFile = tempDir.resolve("UserController.java").toFile();
        Files.writeString(javaFile.toPath(), sourceCode);

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeFile(javaFile.getAbsolutePath());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getClasses()).hasSize(1);
        assertThat(result.hasErrors()).isFalse();

        ClassInfo classInfo = result.getClasses().get(0);
        assertThat(classInfo.simpleName()).isEqualTo("UserController");
        assertThat(classInfo.classType()).isEqualTo(ClassType.CONTROLLER);
        assertThat(classInfo.sourcePath()).isEqualTo(javaFile.getAbsolutePath());
    }

    @Test
    void shouldThrowException_WhenFileDoesNotExist() {

        String nonExistentPath = "/path/to/nonexistent/file.java";

        assertThatThrownBy(() -> projectAnalyzer.analyzeFile(nonExistentPath))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    void shouldThrowException_WhenFileIsNotJavaFile(@TempDir Path tempDir) throws IOException {

        File textFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(textFile.toPath(), "This is not a Java file");

        assertThatThrownBy(() -> projectAnalyzer.analyzeFile(textFile.getAbsolutePath()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not a Java source file");
    }

    @Test
    void shouldAnalyzeProject_WhenProjectDirectoryContainsJavaFiles(@TempDir Path tempDir) throws IOException {

        Path srcDir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcDir);

        String serviceCode = """
                package com.example;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService {
                    public void doSomething() {
                    }
                }
                """;

        String controllerCode = """
                package com.example;

                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class UserController {
                    public void doSomething() {
                    }
                }
                """;

        Files.writeString(srcDir.resolve("UserService.java"), serviceCode);
        Files.writeString(srcDir.resolve("UserController.java"), controllerCode);

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeProject(tempDir.toString());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getClasses()).hasSize(2);
        assertThat(result.getClasses())
                .extracting(ClassInfo::simpleName)
                .containsExactlyInAnyOrder("UserService", "UserController");
    }

    @Test
    void shouldReturnFailure_WhenProjectDirectoryDoesNotExist() throws IOException {

        String nonExistentPath = "/path/to/nonexistent/project";

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeProject(nonExistentPath);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).anyMatch(error ->
                error.contains("does not exist") || error.contains("not a directory"));
    }

    @Test
    void shouldReturnEmptyResult_WhenProjectDirectoryHasNoJavaFiles(@TempDir Path tempDir) throws IOException {

        Files.createDirectories(tempDir.resolve("empty"));

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeProject(tempDir.toString());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getClasses()).isEmpty();
    }

    @Test
    void shouldAnalyzeMultipleFiles_WhenProjectHasNestedStructure(@TempDir Path tempDir) throws IOException {

        Path serviceDir = tempDir.resolve("src/main/java/com/example/service");
        Path controllerDir = tempDir.resolve("src/main/java/com/example/controller");
        Files.createDirectories(serviceDir);
        Files.createDirectories(controllerDir);

        String serviceCode = """
                package com.example.service;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService {
                }
                """;

        String repositoryCode = """
                package com.example.service;

                import org.springframework.stereotype.Repository;

                @Repository
                public class UserRepository {
                }
                """;

        String controllerCode = """
                package com.example.controller;

                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class UserController {
                }
                """;

        Files.writeString(serviceDir.resolve("UserService.java"), serviceCode);
        Files.writeString(serviceDir.resolve("UserRepository.java"), repositoryCode);
        Files.writeString(controllerDir.resolve("UserController.java"), controllerCode);

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeProject(tempDir.toString());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getClasses()).hasSize(3);
        assertThat(result.getClasses())
                .extracting(ClassInfo::classType)
                .containsExactlyInAnyOrder(ClassType.SERVICE, ClassType.REPOSITORY, ClassType.CONTROLLER);
    }

    @Test
    void shouldReturnSuccessWithWarnings_WhenSomeFilesFailToParse(@TempDir Path tempDir) throws IOException {

        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir);

        String validCode = """
                package com.example;

                import org.springframework.stereotype.Service;

                @Service
                public class ValidService {
                }
                """;

        String invalidCode = """
                package com.example;

                public class InvalidService {

                """;

        Files.writeString(srcDir.resolve("ValidService.java"), validCode);
        Files.writeString(srcDir.resolve("InvalidService.java"), invalidCode);

        ProjectAnalyzer.AnalysisResult result = projectAnalyzer.analyzeProject(tempDir.toString());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getClasses()).hasSize(1);
        assertThat(result.getClasses().get(0).simpleName()).isEqualTo("ValidService");
    }

    @Test
    void shouldCreateSuccessfulResult_WhenCreatingWithFactoryMethod() {

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("TestClass")
                .qualifiedName("com.example.TestClass")
                .packageName("com.example")
                .build();

        ProjectAnalyzer.AnalysisResult result = ProjectAnalyzer.AnalysisResult.success(
                java.util.List.of(classInfo)
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getClasses()).hasSize(1);
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSummary()).contains("Successfully analyzed 1 class(es)");
    }

    @Test
    void shouldCreateFailureResult_WhenCreatingWithFactoryMethod() {

        ProjectAnalyzer.AnalysisResult result = ProjectAnalyzer.AnalysisResult.failure("Test error");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getClasses()).isEmpty();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).contains("Test error");
        assertThat(result.getSummary()).contains("Analysis failed with 1 error(s)");
    }

    @Test
    void shouldCreateResultWithWarnings_WhenCreatingWithFactoryMethod() {

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("TestClass")
                .qualifiedName("com.example.TestClass")
                .packageName("com.example")
                .build();

        ProjectAnalyzer.AnalysisResult result = ProjectAnalyzer.AnalysisResult.success(
                java.util.List.of(classInfo),
                java.util.List.of("Warning: some issue")
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getClasses()).hasSize(1);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).contains("Warning: some issue");
        assertThat(result.getSummary()).contains("Successfully analyzed 1 class(es) with 1 warning(s)");
    }

    @Test
    void shouldReturnImmutableCollections_WhenAccessingResultData() {

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("TestClass")
                .qualifiedName("com.example.TestClass")
                .packageName("com.example")
                .build();

        ProjectAnalyzer.AnalysisResult result = ProjectAnalyzer.AnalysisResult.success(
                java.util.List.of(classInfo)
        );

        assertThatThrownBy(() -> result.getClasses().clear())
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> result.getErrors().add("new error"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldProvideUsefulToString_WhenCallingToString() {

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("TestClass")
                .qualifiedName("com.example.TestClass")
                .packageName("com.example")
                .build();

        ProjectAnalyzer.AnalysisResult result = ProjectAnalyzer.AnalysisResult.success(
                java.util.List.of(classInfo)
        );

        String toString = result.toString();

        assertThat(toString)
                .contains("success=true")
                .contains("classes=1")
                .contains("errors=0");
    }
}
