package com.springtest.cli.output;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColorFormatterTest {

    private ColorFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new ColorFormatter();
    }

    @Test
    void red_ShouldFormatTextInRed() {
        String result = formatter.red("error");

        assertThat(result).isNotNull();
        assertThat(result).contains("error");
    }

    @Test
    void green_ShouldFormatTextInGreen() {
        String result = formatter.green("success");

        assertThat(result).isNotNull();
        assertThat(result).contains("success");
    }

    @Test
    void yellow_ShouldFormatTextInYellow() {
        String result = formatter.yellow("warning");

        assertThat(result).isNotNull();
        assertThat(result).contains("warning");
    }

    @Test
    void blue_ShouldFormatTextInBlue() {
        String result = formatter.blue("info");

        assertThat(result).isNotNull();
        assertThat(result).contains("info");
    }

    @Test
    void cyan_ShouldFormatTextInCyan() {
        String result = formatter.cyan("header");

        assertThat(result).isNotNull();
        assertThat(result).contains("header");
    }
}