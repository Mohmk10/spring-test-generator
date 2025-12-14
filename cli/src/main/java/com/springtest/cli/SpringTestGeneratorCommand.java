package com.springtest.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "spring-test-generator",
    mixinStandardHelpOptions = true,
    version = "1.0.0-SNAPSHOT",
    description = "Generates unit and integration tests for Spring Boot applications",
    subcommands = {
        GenerateCommand.class,
        AnalyzeCommand.class,
        ConfigCommand.class
    }
)
public class SpringTestGeneratorCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output")
    private boolean verbose;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SpringTestGeneratorCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("Spring Test Generator v1.0.0-SNAPSHOT");
        System.out.println();
        System.out.println("Usage: spring-test-generator <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  generate    Generate tests for Spring Boot classes");
        System.out.println("  analyze     Analyze source code and list detected classes");
        System.out.println("  config      Show or modify configuration");
        System.out.println();
        System.out.println("Use 'spring-test-generator <command> --help' for more information about a command.");
    }

    public boolean isVerbose() {
        return verbose;
    }
}
