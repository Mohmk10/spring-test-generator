package com.springtest.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class GenerateTestsMojoTest {

    @TempDir
    Path tempSourceDir;

    @TempDir
    Path tempOutputDir;

    private GenerateTestsMojo mojo;

    @BeforeEach
    void setUp() {
        mojo = new GenerateTestsMojo();
    }

    @Test
    void testExecute_WithNonExistentSourceDirectory() throws Exception {
        File nonExistent = new File("/nonexistent/path");
        setField(mojo, "sourceDirectory", nonExistent);
        setField(mojo, "outputDirectory", tempOutputDir.toFile());

        assertThatThrownBy(() -> mojo.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("Source directory does not exist");
    }

    @Test
    void testExecute_WithEmptySourceDirectory() throws Exception {
        setField(mojo, "sourceDirectory", tempSourceDir.toFile());
        setField(mojo, "outputDirectory", tempOutputDir.toFile());
        setField(mojo, "testType", "all");
        setField(mojo, "namingStrategy", "method-scenario");

        mojo.execute();
    }

    @Test
    void testExecute_WithSpringServiceClass() throws Exception {
        Files.createDirectories(tempSourceDir.resolve("com/example"));
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
        Files.writeString(tempSourceDir.resolve("com/example/UserService.java"), serviceClass);

        setField(mojo, "sourceDirectory", tempSourceDir.toFile());
        setField(mojo, "outputDirectory", tempOutputDir.toFile());
        setField(mojo, "testType", "unit");
        setField(mojo, "namingStrategy", "method-scenario");

        mojo.execute();

        assertThat(tempOutputDir.resolve("com/example/UserServiceTest.java")).exists();
    }

    @Test
    void testExecute_WithTestTypeAll() throws Exception {
        Files.createDirectories(tempSourceDir.resolve("com/example"));
        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class UserService {
                public void save() {}
            }
            """;
        Files.writeString(tempSourceDir.resolve("com/example/UserService.java"), serviceClass);

        setField(mojo, "sourceDirectory", tempSourceDir.toFile());
        setField(mojo, "outputDirectory", tempOutputDir.toFile());
        setField(mojo, "testType", "all");
        setField(mojo, "namingStrategy", "bdd");

        mojo.execute();

        assertThat(tempOutputDir.resolve("com/example/UserServiceTest.java")).exists();
        assertThat(tempOutputDir.resolve("com/example/UserServiceIntegrationTest.java")).exists();
    }

    @Test
    void testExecute_WithTestTypeIntegration() throws Exception {
        Files.createDirectories(tempSourceDir.resolve("com/example"));
        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class DataService {
                public void process() {}
            }
            """;
        Files.writeString(tempSourceDir.resolve("com/example/DataService.java"), serviceClass);

        setField(mojo, "sourceDirectory", tempSourceDir.toFile());
        setField(mojo, "outputDirectory", tempOutputDir.toFile());
        setField(mojo, "testType", "integration");
        setField(mojo, "namingStrategy", "given-when-then");

        mojo.execute();

        assertThat(tempOutputDir.resolve("com/example/DataServiceTest.java")).doesNotExist();
        assertThat(tempOutputDir.resolve("com/example/DataServiceIntegrationTest.java")).exists();
    }

    @Test
    void testExecute_WithNamingStrategyBDD() throws Exception {
        Files.createDirectories(tempSourceDir.resolve("com/example"));
        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class OrderService {
                public void createOrder() {}
            }
            """;
        Files.writeString(tempSourceDir.resolve("com/example/OrderService.java"), serviceClass);

        setField(mojo, "sourceDirectory", tempSourceDir.toFile());
        setField(mojo, "outputDirectory", tempOutputDir.toFile());
        setField(mojo, "testType", "unit");
        setField(mojo, "namingStrategy", "bdd");

        mojo.execute();

        Path testFile = tempOutputDir.resolve("com/example/OrderServiceTest.java");
        assertThat(testFile).exists();
    }

    @Test
    void testExecute_CreatesOutputDirectoryIfNotExists() throws Exception {
        Files.createDirectories(tempSourceDir.resolve("com/example"));
        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class TestService {
                public void test() {}
            }
            """;
        Files.writeString(tempSourceDir.resolve("com/example/TestService.java"), serviceClass);

        Path newOutputDir = tempOutputDir.resolve("newdir");
        setField(mojo, "sourceDirectory", tempSourceDir.toFile());
        setField(mojo, "outputDirectory", newOutputDir.toFile());
        setField(mojo, "testType", "unit");
        setField(mojo, "namingStrategy", "method-scenario");

        mojo.execute();

        assertThat(newOutputDir).exists();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
