package com.springtest.cli.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzeCommandTest {

    @TempDir
    Path tempDir;

    @Test
    void analyze_WithNoClasses_ShouldReturnZero() {
        AnalyzeCommand command = new AnalyzeCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute(tempDir.toString());

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void analyze_WithHelpOption_ShouldShowHelp() {
        AnalyzeCommand command = new AnalyzeCommand();
        CommandLine cmd = new CommandLine(command);

        StringWriter out = new StringWriter();
        cmd.setOut(new PrintWriter(out));

        int exitCode = cmd.execute("--help");

        assertThat(exitCode).isEqualTo(0);
        assertThat(out.toString()).contains("Analyze a Spring Boot project");
    }

    @Test
    void analyze_WithVersionOption_ShouldShowVersion() {
        AnalyzeCommand command = new AnalyzeCommand();
        CommandLine cmd = new CommandLine(command);

        StringWriter out = new StringWriter();
        cmd.setOut(new PrintWriter(out));

        int exitCode = cmd.execute("--version");

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void analyze_WithVerboseFlag_ShouldEnableVerboseOutput() {
        AnalyzeCommand command = new AnalyzeCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute(tempDir.toString(), "-v");

        assertThat(exitCode).isEqualTo(0);
    }
}