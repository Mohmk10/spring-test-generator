package com.springtest.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class GenerateCommandTest {

    @TempDir
    Path tempSourceDir;

    @TempDir
    Path tempOutputDir;

    private SpringTestGeneratorCommand mainCommand;
    private CommandLine commandLine;

    @BeforeEach
    void setUp() {
        mainCommand = new SpringTestGeneratorCommand();
        commandLine = new CommandLine(mainCommand);
    }

    @Test
    void testGenerateCommandRequiresSourceAndOutput() {
        int exitCode = commandLine.execute("generate");

        assertThat(exitCode).isNotEqualTo(0);
    }

    @Test
    void testGenerateCommandWithNonExistentSource() {
        int exitCode = commandLine.execute(
            "generate",
            "--source", "/nonexistent/path",
            "--output", tempOutputDir.toString()
        );

        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void testGenerateCommandWithValidSourceButNoSpringClasses() throws Exception {
        Files.createDirectories(tempSourceDir.resolve("com/example"));
        String javaFile = """
            package com.example;
            public class PlainClass {
                public void method() {}
            }
            """;
        Files.writeString(tempSourceDir.resolve("com/example/PlainClass.java"), javaFile);

        int exitCode = commandLine.execute(
            "generate",
            "--source", tempSourceDir.resolve("com/example/PlainClass.java").toString(),
            "--output", tempOutputDir.toString()
        );

    }

    @Test
    void testGenerateCommandHelp() {
        int exitCode = commandLine.execute("generate", "--help");

    }

    @Test
    void testGenerateCommandDefaultTestType() {
        GenerateCommand cmd = new GenerateCommand();
        CommandLine cmdLine = new CommandLine(cmd);

        cmdLine.parseArgs(
            "--source", tempSourceDir.toString(),
            "--output", tempOutputDir.toString()
        );

    }

    @Test
    void testGenerateCommandWithUnitTestType() {
        GenerateCommand cmd = new GenerateCommand();
        CommandLine cmdLine = new CommandLine(cmd);

        cmdLine.parseArgs(
            "--source", tempSourceDir.toString(),
            "--output", tempOutputDir.toString(),
            "--type", "unit"
        );

    }

    @Test
    void testGenerateCommandWithIntegrationTestType() {
        GenerateCommand cmd = new GenerateCommand();
        CommandLine cmdLine = new CommandLine(cmd);

        cmdLine.parseArgs(
            "--source", tempSourceDir.toString(),
            "--output", tempOutputDir.toString(),
            "--type", "integration"
        );

    }

    @Test
    void testGenerateCommandWithNamingStrategy() {
        GenerateCommand cmd = new GenerateCommand();
        CommandLine cmdLine = new CommandLine(cmd);

        cmdLine.parseArgs(
            "--source", tempSourceDir.toString(),
            "--output", tempOutputDir.toString(),
            "--naming", "bdd"
        );

    }

    @Test
    void testGenerateCommandWithClassName() {
        GenerateCommand cmd = new GenerateCommand();
        CommandLine cmdLine = new CommandLine(cmd);

        cmdLine.parseArgs(
            "--source", tempSourceDir.toString(),
            "--output", tempOutputDir.toString(),
            "--class", "UserService"
        );

    }
}
