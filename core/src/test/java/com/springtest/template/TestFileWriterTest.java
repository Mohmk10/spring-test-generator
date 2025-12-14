package com.springtest.template;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class TestFileWriterTest {

    @TempDir
    Path tempDir;

    private TestFileWriter testFileWriter;

    @BeforeEach
    void setUp() {
        testFileWriter = new TestFileWriter(tempDir.toString(), true);
    }

    @Test
    void testConstructorWithNullOutputDirectory() {
        assertThatThrownBy(() -> new TestFileWriter(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Output directory cannot be null or empty");
    }

    @Test
    void testConstructorWithEmptyOutputDirectory() {
        assertThatThrownBy(() -> new TestFileWriter(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Output directory cannot be null or empty");
    }

    @Test
    void testWriteTestFileWithNullPackageName() {
        assertThatThrownBy(() -> testFileWriter.writeTestFile(null, "TestClass", "content"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Package name cannot be null");
    }

    @Test
    void testWriteTestFileWithNullClassName() {
        assertThatThrownBy(() -> testFileWriter.writeTestFile("com.test", null, "content"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Class name cannot be null or empty");
    }

    @Test
    void testWriteTestFileWithEmptyClassName() {
        assertThatThrownBy(() -> testFileWriter.writeTestFile("com.test", "", "content"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Class name cannot be null or empty");
    }

    @Test
    void testWriteTestFileWithNullContent() {
        assertThatThrownBy(() -> testFileWriter.writeTestFile("com.test", "TestClass", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Content cannot be null");
    }

    @Test
    void testWriteTestFile() throws IOException {
        String packageName = "com.test";
        String className = "MyTest";
        String content = "public class MyTest {}";

        Path result = testFileWriter.writeTestFile(packageName, className, content);

        assertThat(result).isNotNull();
        assertThat(Files.exists(result)).isTrue();
        assertThat(Files.readString(result)).isEqualTo(content);
    }

    @Test
    void testWriteServiceTest() throws IOException {
        String packageName = "com.test.service";
        String className = "UserService";
        String content = "public class UserServiceTest {}";

        Path result = testFileWriter.writeServiceTest(packageName, className, content);

        assertThat(result).isNotNull();
        assertThat(Files.exists(result)).isTrue();
        assertThat(result.getFileName().toString()).isEqualTo("UserServiceTest.java");
    }

    @Test
    void testWriteControllerTest() throws IOException {
        String packageName = "com.test.controller";
        String className = "UserController";
        String content = "public class UserControllerTest {}";

        Path result = testFileWriter.writeControllerTest(packageName, className, content);

        assertThat(result).isNotNull();
        assertThat(Files.exists(result)).isTrue();
        assertThat(result.getFileName().toString()).isEqualTo("UserControllerTest.java");
    }

    @Test
    void testWriteRepositoryTest() throws IOException {
        String packageName = "com.test.repository";
        String className = "UserRepository";
        String content = "public class UserRepositoryTest {}";

        Path result = testFileWriter.writeRepositoryTest(packageName, className, content);

        assertThat(result).isNotNull();
        assertThat(Files.exists(result)).isTrue();
        assertThat(result.getFileName().toString()).isEqualTo("UserRepositoryTest.java");
    }

    @Test
    void testWriteIntegrationTest() throws IOException {
        String packageName = "com.test.integration";
        String className = "UserService";
        String content = "public class UserServiceIntegrationTest {}";

        Path result = testFileWriter.writeIntegrationTest(packageName, className, content);

        assertThat(result).isNotNull();
        assertThat(Files.exists(result)).isTrue();
        assertThat(result.getFileName().toString()).isEqualTo("UserServiceIntegrationTest.java");
    }

    @Test
    void testGetOutputDirectory() {
        Path outputDir = testFileWriter.getOutputDirectory();
        assertThat(outputDir).isEqualTo(tempDir);
    }

    @Test
    void testExists() {
        String packageName = "com.test";
        String className = "MyTest";
        String content = "public class MyTest {}";

        testFileWriter.writeTestFile(packageName, className, content);

        boolean exists = testFileWriter.exists(packageName, "MyTest");
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsWhenFileDoesNotExist() {
        boolean exists = testFileWriter.exists("com.test", "NonExistent");
        assertThat(exists).isFalse();
    }

    @Test
    void testDeleteTestFile() throws IOException {
        String packageName = "com.test";
        String className = "MyTest";
        String content = "public class MyTest {}";

        testFileWriter.writeTestFile(packageName, className, content);
        assertThat(testFileWriter.exists(packageName, "MyTest")).isTrue();

        testFileWriter.deleteTestFile(packageName, "MyTest");
        assertThat(testFileWriter.exists(packageName, "MyTest")).isFalse();
    }

    @Test
    void testOverwriteExistingFile() throws IOException {
        String packageName = "com.test";
        String className = "MyTest";
        String content1 = "public class MyTest {}";
        String content2 = "public class MyTest { void test() {} }";

        Path result1 = testFileWriter.writeTestFile(packageName, className, content1);
        assertThat(Files.readString(result1)).isEqualTo(content1);

        Path result2 = testFileWriter.writeTestFile(packageName, className, content2);
        assertThat(Files.readString(result2)).isEqualTo(content2);
        assertThat(result1).isEqualTo(result2);
    }
}
