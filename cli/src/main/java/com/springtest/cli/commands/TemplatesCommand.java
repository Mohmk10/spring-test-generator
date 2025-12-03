package com.springtest.cli.commands;

import com.springtest.cli.output.ConsoleReporter;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
        name = "templates",
        description = "Manage test templates",
        mixinStandardHelpOptions = true
)
public class TemplatesCommand implements Callable<Integer> {

    private final ConsoleReporter reporter = new ConsoleReporter();

    @Override
    public Integer call() {
        reporter.printInfo("Templates management - Coming soon");
        return 0;
    }
}