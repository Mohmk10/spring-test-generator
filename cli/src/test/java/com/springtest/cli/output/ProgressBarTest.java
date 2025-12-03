package com.springtest.cli.output;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressBarTest {

    private ProgressBar progressBar;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        progressBar = new ProgressBar();
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void start_ShouldInitializeProgressBar() {
        progressBar.start(10);

        String output = outContent.toString();
        assertThat(output).contains("0/10");
    }

    @Test
    void update_ShouldShowProgress() {
        progressBar.start(10);
        progressBar.update(5);

        String output = outContent.toString();
        assertThat(output).contains("5/10");
    }

    @Test
    void finish_ShouldShowCompletion() {
        progressBar.start(10);
        progressBar.finish();

        String output = outContent.toString();
        assertThat(output).contains("10/10");
        assertThat(output).contains("Completed in");
    }

    @Test
    void progressBar_ShouldShowPercentage() {
        progressBar.start(100);
        progressBar.update(50);

        String output = outContent.toString();
        assertThat(output).contains("50%");
    }
}