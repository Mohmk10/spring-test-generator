package com.springtest.cli.commands;

import com.springtest.cli.output.ConsoleReporter;
import com.springtest.core.config.ConfigLoader;
import com.springtest.core.config.GeneratorConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "config",
        description = "Manage Spring Test Generator configuration",
        mixinStandardHelpOptions = true,
        subcommands = {
                ConfigCommand.InitCommand.class,
                ConfigCommand.ShowCommand.class,
                ConfigCommand.SetCommand.class
        }
)
public class ConfigCommand implements Callable<Integer> {

    private final ConsoleReporter reporter = new ConsoleReporter();

    @Override
    public Integer call() {
        reporter.printInfo("Usage: spring-test-gen config <subcommand>");
        reporter.printInfo("Available subcommands: init, show, set");
        return 0;
    }

    @Command(name = "init", description = "Initialize configuration file")
    static class InitCommand implements Callable<Integer> {

        @Option(
                names = {"-f", "--force"},
                description = "Overwrite existing configuration"
        )
        private boolean force;

        private final ConsoleReporter reporter = new ConsoleReporter();

        @Override
        public Integer call() {
            try {
                Path configPath = Path.of(".spring-test-gen.properties");

                if (!force && configPath.toFile().exists()) {
                    reporter.printWarning("Configuration file already exists. Use --force to overwrite.");
                    return 1;
                }

                GeneratorConfig defaultConfig = GeneratorConfig.builder().build();
                ConfigLoader.saveToFile(defaultConfig, configPath);

                reporter.printSuccess("Configuration file created: " + configPath);
                return 0;

            } catch (Exception e) {
                reporter.printError("Failed to create configuration: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "show", description = "Show current configuration")
    static class ShowCommand implements Callable<Integer> {

        private final ConsoleReporter reporter = new ConsoleReporter();

        @Override
        public Integer call() {
            try {
                GeneratorConfig config = ConfigLoader.loadDefault();

                reporter.printHeader("Current Configuration");
                System.out.println("Source Directory    : " + config.getSourceDirectory());
                System.out.println("Test Directory      : " + config.getTestDirectory());
                System.out.println("Skip Existing       : " + config.isSkipExisting());
                System.out.println("Generate Edge Cases : " + config.isGenerateEdgeCases());
                System.out.println("Generate Exceptions : " + config.isGenerateExceptionTests());
                System.out.println("Naming Convention   : " + config.getTestNamingConvention());
                System.out.println("Max Tests Per Method: " + config.getMaxTestsPerMethod());

                return 0;

            } catch (Exception e) {
                reporter.printError("Failed to load configuration: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "set", description = "Set configuration value")
    static class SetCommand implements Callable<Integer> {

        @Option(names = "--skip-existing", description = "Skip existing tests")
        private Boolean skipExisting;

        @Option(names = "--edge-cases", description = "Generate edge case tests")
        private Boolean edgeCases;

        @Option(names = "--exceptions", description = "Generate exception tests")
        private Boolean exceptions;

        @Option(names = "--naming", description = "Test naming convention")
        private String naming;

        private final ConsoleReporter reporter = new ConsoleReporter();

        @Override
        public Integer call() {
            try {
                Path configPath = Path.of(".spring-test-gen.properties");
                GeneratorConfig config = ConfigLoader.loadDefault();

                if (skipExisting != null) {
                    config = config.withSkipExisting(skipExisting);
                }
                if (edgeCases != null) {
                    config = config.withGenerateEdgeCases(edgeCases);
                }
                if (exceptions != null) {
                    config = config.withGenerateExceptionTests(exceptions);
                }
                if (naming != null) {
                    config = config.withTestNamingConvention(naming);
                }

                ConfigLoader.saveToFile(config, configPath);
                reporter.printSuccess("Configuration updated");

                return 0;

            } catch (Exception e) {
                reporter.printError("Failed to update configuration: " + e.getMessage());
                return 1;
            }
        }
    }
}