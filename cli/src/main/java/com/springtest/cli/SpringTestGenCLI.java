package com.springtest.cli;

import com.springtest.cli.commands.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "spring-test-gen",
        version = "1.0.0",
        description = "Generate unit and integration tests for Spring Boot applications",
        mixinStandardHelpOptions = true,
        subcommands = {
                AnalyzeCommand.class,
                GenerateCommand.class,
                ConfigCommand.class,
                ReportCommand.class,
                TemplatesCommand.class,
                WatchCommand.class
        }
)
public class SpringTestGenCLI implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SpringTestGenCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("Spring Test Generator v1.0.0");
        System.out.println();
        System.out.println("Usage: spring-test-gen <command> [options]");
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  analyze    - Analyze a Spring Boot project");
        System.out.println("  generate   - Generate tests for a project");
        System.out.println("  config     - Manage configuration");
        System.out.println("  report     - Generate analysis report");
        System.out.println("  templates  - Manage templates");
        System.out.println("  watch      - Watch and regenerate on changes");
        System.out.println();
        System.out.println("Run 'spring-test-gen <command> --help' for more information.");
    }
}