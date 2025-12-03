package com.springtest.cli.output;

public class ColorFormatter {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    private final boolean colorsEnabled;

    public ColorFormatter() {
        this.colorsEnabled = System.console() != null && !isWindows();
    }

    public String red(String text) {
        return colorize(text, RED);
    }

    public String green(String text) {
        return colorize(text, GREEN);
    }

    public String yellow(String text) {
        return colorize(text, YELLOW);
    }

    public String blue(String text) {
        return colorize(text, BLUE);
    }

    public String cyan(String text) {
        return colorize(text, CYAN);
    }

    private String colorize(String text, String color) {
        if (!colorsEnabled) {
            return text;
        }
        return color + text + RESET;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}