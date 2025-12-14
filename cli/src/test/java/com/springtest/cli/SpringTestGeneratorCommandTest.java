package com.springtest.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.*;

class SpringTestGeneratorCommandTest {

    @Test
    void testMainCommandWithNoArgs() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        SpringTestGeneratorCommand cmd = new SpringTestGeneratorCommand();
        cmd.run();

        String output = outContent.toString();
        assertThat(output).contains("Spring Test Generator");
        assertThat(output).contains("generate");
        assertThat(output).contains("analyze");
        assertThat(output).contains("config");

        System.setOut(System.out);
    }

    @Test
    void testCommandLineWithHelp() {
        CommandLine commandLine = new CommandLine(new SpringTestGeneratorCommand());
        int exitCode = commandLine.execute("--help");

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void testCommandLineWithVersion() {
        CommandLine commandLine = new CommandLine(new SpringTestGeneratorCommand());
        int exitCode = commandLine.execute("--version");

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void testVerboseOption() {
        SpringTestGeneratorCommand cmd = new SpringTestGeneratorCommand();
        CommandLine commandLine = new CommandLine(cmd);

        commandLine.parseArgs("--verbose");

        assertThat(cmd.isVerbose()).isTrue();
    }

    @Test
    void testNoVerboseByDefault() {
        SpringTestGeneratorCommand cmd = new SpringTestGeneratorCommand();

        assertThat(cmd.isVerbose()).isFalse();
    }
}
