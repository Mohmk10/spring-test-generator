package com.springtest.cli.commands;

import com.springtest.cli.output.ConsoleReporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "watch",
        description = "Watch for changes and regenerate tests automatically",
        mixinStandardHelpOptions = true
)
public class WatchCommand implements Callable<Integer> {

    @Parameters(
            index = "0",
            description = "Project directory to watch (default: current directory)",
            defaultValue = "."
    )
    private Path projectDir;

    private final ConsoleReporter reporter = new ConsoleReporter();

    @Override
    public Integer call() {
        reporter.printInfo("Watch mode - Coming soon");
        reporter.printInfo("Will watch: " + projectDir);
        return 0;
    }
}