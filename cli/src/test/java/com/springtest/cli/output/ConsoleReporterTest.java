package com.springtest.cli.output;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleReporterTest {

    private ConsoleReporter reporter;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        reporter = new ConsoleReporter();
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void printSuccess_ShouldOutputSuccessMessage() {
        reporter.printSuccess("Test passed");

        assertThat(outContent.toString()).contains("Test passed");
        assertThat(outContent.toString()).contains("✓");
    }

    @Test
    void printError_ShouldOutputErrorMessage() {
        reporter.printError("Test failed");

        assertThat(errContent.toString()).contains("Test failed");
        assertThat(errContent.toString()).contains("✗");
    }

    @Test
    void printWarning_ShouldOutputWarningMessage() {
        reporter.printWarning("Warning message");

        assertThat(outContent.toString()).contains("Warning message");
        assertThat(outContent.toString()).contains("⚠");
    }

    @Test
    void printInfo_ShouldOutputInfoMessage() {
        reporter.printInfo("Info message");

        assertThat(outContent.toString()).contains("Info message");
        assertThat(outContent.toString()).contains("ℹ");
    }

    @Test
    void printHeader_ShouldOutputFormattedHeader() {
        reporter.printHeader("Test Header");

        assertThat(outContent.toString()).contains("Test Header");
        assertThat(outContent.toString()).contains("═");
    }

    @Test
    void print_ShouldOutputPlainMessage() {
        reporter.print("Plain message");

        assertThat(outContent.toString()).contains("Plain message");
    }
}