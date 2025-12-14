package com.springtest.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class AnalyzeCommandTest {

    @TempDir
    Path tempDir;

    private SpringTestGeneratorCommand mainCommand;
    private CommandLine commandLine;

    @BeforeEach
    void setUp() {
        mainCommand = new SpringTestGeneratorCommand();
        commandLine = new CommandLine(mainCommand);
    }

    @Test
    void testAnalyzeCommandRequiresSource() {
        int exitCode = commandLine.execute("analyze");

        assertThat(exitCode).isNotEqualTo(0);
    }

    @Test
    void testAnalyzeCommandWithNonExistentSource() {
        int exitCode = commandLine.execute(
            "analyze",
            "--source", "/nonexistent/path"
        );

        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void testAnalyzeCommandWithEmptyDirectory() throws Exception {
        Files.createDirectories(tempDir.resolve("src"));

        int exitCode = commandLine.execute(
            "analyze",
            "--source", tempDir.resolve("src").toString()
        );

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void testAnalyzeCommandHelp() {
        int exitCode = commandLine.execute("analyze", "--help");

    }

    @Test
    void testAnalyzeCommandWithDetailOption() {
        AnalyzeCommand cmd = new AnalyzeCommand();
        CommandLine cmdLine = new CommandLine(cmd);

        cmdLine.parseArgs(
            "--source", tempDir.toString(),
            "--detail"
        );

    }

    @Test
    void testAnalyzeCommandWithoutDetailOption() {
        AnalyzeCommand cmd = new AnalyzeCommand();
        CommandLine cmdLine = new CommandLine(cmd);

        cmdLine.parseArgs(
            "--source", tempDir.toString()
        );

    }

    @Test
    void testAnalyzeCommandWithPlainJavaClass() throws Exception {
        Files.createDirectories(tempDir.resolve("com/example"));
        String javaFile = """
            package com.example;
            public class PlainClass {
                public void method() {}
            }
            """;
        Files.writeString(tempDir.resolve("com/example/PlainClass.java"), javaFile);

        int exitCode = commandLine.execute(
            "analyze",
            "--source", tempDir.resolve("com/example/PlainClass.java").toString()
        );

        assertThat(exitCode).isEqualTo(0);
    }
}
