package com.springtest.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class ConfigCommandTest {

    @TempDir
    Path tempDir;

    private SpringTestGeneratorCommand mainCommand;
    private CommandLine commandLine;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        mainCommand = new SpringTestGeneratorCommand();
        commandLine = new CommandLine(mainCommand);
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    void testConfigCommandShowsDefaultConfig() {
        int exitCode = commandLine.execute("config", "--show");

        assertThat(exitCode).isEqualTo(0);
        assertThat(outContent.toString()).contains("Current Configuration");
        assertThat(outContent.toString()).contains("defaultSourcePath");

        System.setOut(originalOut);
    }

    @Test
    void testConfigCommandWithNoArgsShowsConfig() {
        int exitCode = commandLine.execute("config");

        assertThat(exitCode).isEqualTo(0);
        assertThat(outContent.toString()).contains("Current Configuration");

        System.setOut(originalOut);
    }

    @Test
    void testConfigCommandHelp() {
        int exitCode = commandLine.execute("config", "--help");

        System.setOut(originalOut);
    }

    @Test
    void testConfigCommandSetSourcePath() throws Exception {
        System.setProperty("user.dir", tempDir.toString());

        int exitCode = commandLine.execute(
            "config",
            "--set-source", "custom/src"
        );

        assertThat(exitCode).isEqualTo(0);
        assertThat(outContent.toString()).contains("Default source path set to: custom/src");

        Path configFile = tempDir.resolve(".springtest.yml");
        if (Files.exists(configFile)) {
            String content = Files.readString(configFile);
            assertThat(content).contains("custom/src");
        }

        System.setOut(originalOut);
    }

    @Test
    void testConfigCommandSetOutputPath() throws Exception {
        System.setProperty("user.dir", tempDir.toString());

        int exitCode = commandLine.execute(
            "config",
            "--set-output", "custom/test"
        );

        assertThat(exitCode).isEqualTo(0);
        assertThat(outContent.toString()).contains("Default output path set to: custom/test");

        System.setOut(originalOut);
    }

    @Test
    void testConfigCommandSetValidTestType() throws Exception {
        System.setProperty("user.dir", tempDir.toString());

        int exitCode = commandLine.execute(
            "config",
            "--set-type", "unit"
        );

        assertThat(exitCode).isEqualTo(0);
        assertThat(outContent.toString()).contains("Default test type set to: unit");

        System.setOut(originalOut);
    }

    @Test
    void testConfigCommandSetInvalidTestType() {
        int exitCode = commandLine.execute(
            "config",
            "--set-type", "invalid"
        );

        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Invalid test type");

        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testConfigCommandSetValidNamingStrategy() throws Exception {
        System.setProperty("user.dir", tempDir.toString());

        int exitCode = commandLine.execute(
            "config",
            "--set-naming", "bdd"
        );

        assertThat(exitCode).isEqualTo(0);
        assertThat(outContent.toString()).contains("Default naming strategy set to: bdd");

        System.setOut(originalOut);
    }

    @Test
    void testConfigCommandSetInvalidNamingStrategy() {
        int exitCode = commandLine.execute(
            "config",
            "--set-naming", "invalid"
        );

        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Invalid naming strategy");

        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testConfigCommandGlobalFlag() {
        ConfigCommand cmd = new ConfigCommand();
        CommandLine cmdLine = new CommandLine(cmd);

        cmdLine.parseArgs("--global", "--show");

        System.setOut(originalOut);
    }
}
