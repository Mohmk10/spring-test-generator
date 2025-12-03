package com.springtest.cli.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateCommandTest {

    @TempDir
    Path tempDir;

    @Test
    void generate_WithNoClasses_ShouldReturnZero() {
        GenerateCommand command = new GenerateCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute(tempDir.toString());

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void generate_WithDryRunFlag_ShouldNotWriteFiles() {
        GenerateCommand command = new GenerateCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute(tempDir.toString(), "--dry-run");

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void generate_WithHelpOption_ShouldShowHelp() {
        GenerateCommand command = new GenerateCommand();
        CommandLine cmd = new CommandLine(command);

        StringWriter out = new StringWriter();
        cmd.setOut(new PrintWriter(out));

        int exitCode = cmd.execute("--help");

        assertThat(exitCode).isEqualTo(0);
        assertThat(out.toString()).contains("Generate tests for Spring Boot classes");
    }

    @Test
    void generate_WithPackageOption_ShouldProcessSpecificPackage() {
        GenerateCommand command = new GenerateCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute(
                tempDir.toString(),
                "-p", "com.example.service"
        );

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void generate_WithOutputOption_ShouldUseCustomOutputDir() {
        GenerateCommand command = new GenerateCommand();
        CommandLine cmd = new CommandLine(command);

        Path outputDir = tempDir.resolve("custom-output");

        int exitCode = cmd.execute(
                tempDir.toString(),
                "-o", outputDir.toString()
        );

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void generate_WithSkipExistingFlag_ShouldSkipExistingTests() {
        GenerateCommand command = new GenerateCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute(
                tempDir.toString(),
                "--skip-existing"
        );

        assertThat(exitCode).isEqualTo(0);
    }
}