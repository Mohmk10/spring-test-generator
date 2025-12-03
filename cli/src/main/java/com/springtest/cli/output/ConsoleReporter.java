package com.springtest.cli.output;

public class ConsoleReporter {

    private final ColorFormatter formatter = new ColorFormatter();

    public void printHeader(String message) {
        System.out.println();
        System.out.println(formatter.cyan("═".repeat(60)));
        System.out.println(formatter.cyan("  " + message));
        System.out.println(formatter.cyan("═".repeat(60)));
        System.out.println();
    }

    public void printSuccess(String message) {
        System.out.println(formatter.green("✓ " + message));
    }

    public void printError(String message) {
        System.err.println(formatter.red("✗ " + message));
    }

    public void printWarning(String message) {
        System.out.println(formatter.yellow("⚠ " + message));
    }

    public void printInfo(String message) {
        System.out.println(formatter.blue("ℹ " + message));
    }

    public void print(String message) {
        System.out.println(message);
    }
}