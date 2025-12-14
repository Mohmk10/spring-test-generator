package com.springtest.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class AnalyzeMojoTest {

    @TempDir
    Path tempDir;

    private AnalyzeMojo mojo;

    @BeforeEach
    void setUp() {
        mojo = new AnalyzeMojo();
    }

    @Test
    void testExecute_WithNonExistentSourceDirectory() throws Exception {
        File nonExistent = new File("/nonexistent/path");
        setField(mojo, "sourceDirectory", nonExistent);

        assertThatThrownBy(() -> mojo.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("Source directory does not exist");
    }

    @Test
    void testExecute_WithEmptyDirectory() throws Exception {
        setField(mojo, "sourceDirectory", tempDir.toFile());
        setField(mojo, "detailed", false);

        mojo.execute();
    }

    @Test
    void testExecute_WithSpringClasses() throws Exception {
        Files.createDirectories(tempDir.resolve("com/example"));

        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class UserService {
                public void save() {}
            }
            """;
        Files.writeString(tempDir.resolve("com/example/UserService.java"), serviceClass);

        String controllerClass = """
            package com.example;
            import org.springframework.web.bind.annotation.RestController;
            @RestController
            public class UserController {
                public void getUsers() {}
            }
            """;
        Files.writeString(tempDir.resolve("com/example/UserController.java"), controllerClass);

        setField(mojo, "sourceDirectory", tempDir.toFile());
        setField(mojo, "detailed", false);

        mojo.execute();
    }

    @Test
    void testExecute_WithDetailedFlag() throws Exception {
        Files.createDirectories(tempDir.resolve("com/example"));

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
        Files.writeString(tempDir.resolve("com/example/OrderService.java"), serviceClass);

        setField(mojo, "sourceDirectory", tempDir.toFile());
        setField(mojo, "detailed", true);

        mojo.execute();
    }

    @Test
    void testExecute_WithRepository() throws Exception {
        Files.createDirectories(tempDir.resolve("com/example"));

        String repoClass = """
            package com.example;
            import org.springframework.stereotype.Repository;
            @Repository
            public interface UserRepository {
                void save();
            }
            """;
        Files.writeString(tempDir.resolve("com/example/UserRepository.java"), repoClass);

        setField(mojo, "sourceDirectory", tempDir.toFile());
        setField(mojo, "detailed", false);

        mojo.execute();
    }

    @Test
    void testExecute_WithComponent() throws Exception {
        Files.createDirectories(tempDir.resolve("com/example"));

        String componentClass = """
            package com.example;
            import org.springframework.stereotype.Component;
            @Component
            public class EmailSender {
                public void send() {}
            }
            """;
        Files.writeString(tempDir.resolve("com/example/EmailSender.java"), componentClass);

        setField(mojo, "sourceDirectory", tempDir.toFile());
        setField(mojo, "detailed", true);

        mojo.execute();
    }

    @Test
    void testExecute_WithConfiguration() throws Exception {
        Files.createDirectories(tempDir.resolve("com/example"));

        String configClass = """
            package com.example;
            import org.springframework.context.annotation.Configuration;
            @Configuration
            public class AppConfig {
            }
            """;
        Files.writeString(tempDir.resolve("com/example/AppConfig.java"), configClass);

        setField(mojo, "sourceDirectory", tempDir.toFile());
        setField(mojo, "detailed", false);

        mojo.execute();
    }

    @Test
    void testExecute_WithMixedClasses() throws Exception {
        Files.createDirectories(tempDir.resolve("com/example"));

        String serviceClass = """
            package com.example;
            import org.springframework.stereotype.Service;
            @Service
            public class DataService {}
            """;
        Files.writeString(tempDir.resolve("com/example/DataService.java"), serviceClass);

        String plainClass = """
            package com.example;
            public class PlainClass {}
            """;
        Files.writeString(tempDir.resolve("com/example/PlainClass.java"), plainClass);

        setField(mojo, "sourceDirectory", tempDir.toFile());
        setField(mojo, "detailed", false);

        mojo.execute();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
