package com.springtest.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class SpringTestGenCLITest {

    @Test
    void run_WithNoArguments_ShouldShowUsage() {
        SpringTestGenCLI cli = new SpringTestGenCLI();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            cli.run();

            String output = out.toString();
            assertThat(output).contains("Spring Test Generator");
            assertThat(output).contains("Available commands");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void run_WithHelpOption_ShouldShowHelp() {
        SpringTestGenCLI cli = new SpringTestGenCLI();
        CommandLine cmd = new CommandLine(cli);

        StringWriter out = new StringWriter();
        cmd.setOut(new PrintWriter(out));

        int exitCode = cmd.execute("--help");

        assertThat(exitCode).isEqualTo(0);
        assertThat(out.toString()).contains("spring-test-gen");
    }

    @Test
    void run_WithVersionOption_ShouldShowVersion() {
        SpringTestGenCLI cli = new SpringTestGenCLI();
        CommandLine cmd = new CommandLine(cli);

        StringWriter out = new StringWriter();
        cmd.setOut(new PrintWriter(out));

        int exitCode = cmd.execute("--version");

        assertThat(exitCode).isEqualTo(0);
        assertThat(out.toString()).contains("1.0.0");
    }

    @Test
    void run_WithUnknownCommand_ShouldFail() {
        SpringTestGenCLI cli = new SpringTestGenCLI();
        CommandLine cmd = new CommandLine(cli);

        int exitCode = cmd.execute("unknown-command");

        assertThat(exitCode).isNotEqualTo(0);
    }

    @Test
    void run_WithAnalyzeCommand_ShouldExecuteAnalyze() {
        SpringTestGenCLI cli = new SpringTestGenCLI();
        CommandLine cmd = new CommandLine(cli);

        int exitCode = cmd.execute("analyze", "--help");

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void run_WithGenerateCommand_ShouldExecuteGenerate() {
        SpringTestGenCLI cli = new SpringTestGenCLI();
        CommandLine cmd = new CommandLine(cli);

        int exitCode = cmd.execute("generate", "--help");

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void run_WithConfigCommand_ShouldExecuteConfig() {
        SpringTestGenCLI cli = new SpringTestGenCLI();
        CommandLine cmd = new CommandLine(cli);

        int exitCode = cmd.execute("config", "--help");

        assertThat(exitCode).isEqualTo(0);
    }
}