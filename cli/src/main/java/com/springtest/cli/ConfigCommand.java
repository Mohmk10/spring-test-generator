package com.springtest.cli;

import com.springtest.cli.config.CliConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "config",
    description = "Show or modify configuration"
)
public class ConfigCommand implements Callable<Integer> {

    @ParentCommand
    private SpringTestGeneratorCommand parent;

    @Option(names = {"--show"}, description = "Show current configuration")
    private boolean show;

    @Option(names = {"--set-source"}, description = "Set default source path")
    private String setSourcePath;

    @Option(names = {"--set-output"}, description = "Set default output path")
    private String setOutputPath;

    @Option(names = {"--set-type"}, description = "Set default test type (unit/integration/all)")
    private String setTestType;

    @Option(names = {"--set-naming"}, description = "Set default naming strategy (method-scenario/given-when/bdd)")
    private String setNamingStrategy;

    @Option(names = {"--global"}, description = "Use global config (~/.springtest.yml) instead of local")
    private boolean global;

    @Override
    public Integer call() throws Exception {
        CliConfig config = global ? CliConfig.load() : CliConfig.loadFromDirectory(".");

        if (setSourcePath != null) {
            config.setDefaultSourcePath(setSourcePath);
            saveConfig(config);
            System.out.println("Default source path set to: " + setSourcePath);
        }

        if (setOutputPath != null) {
            config.setDefaultOutputPath(setOutputPath);
            saveConfig(config);
            System.out.println("Default output path set to: " + setOutputPath);
        }

        if (setTestType != null) {
            if (!isValidTestType(setTestType)) {
                System.err.println("Error: Invalid test type. Must be: unit, integration, or all");
                return 1;
            }
            config.setDefaultTestType(setTestType);
            saveConfig(config);
            System.out.println("Default test type set to: " + setTestType);
        }

        if (setNamingStrategy != null) {
            if (!isValidNamingStrategy(setNamingStrategy)) {
                System.err.println("Error: Invalid naming strategy. Must be: method-scenario, given-when, or bdd");
                return 1;
            }
            config.setDefaultNamingStrategy(setNamingStrategy);
            saveConfig(config);
            System.out.println("Default naming strategy set to: " + setNamingStrategy);
        }

        if (show || (setSourcePath == null && setOutputPath == null && setTestType == null && setNamingStrategy == null)) {
            showConfig(config);
        }

        return 0;
    }

    private void showConfig(CliConfig config) {
        System.out.println("Current Configuration:");
        System.out.println("=====================");

        Map<String, String> configMap = config.toMap();
        configMap.forEach((key, value) -> System.out.println(key + ": " + value));

        String configLocation = global ? "~/.springtest.yml" : "./.springtest.yml";
        System.out.println("\nConfig file: " + configLocation);
    }

    private void saveConfig(CliConfig config) throws IOException {
        if (global) {
            config.save();
        } else {
            config.saveToDirectory(".");
        }
    }

    private boolean isValidTestType(String type) {
        return "unit".equalsIgnoreCase(type) ||
               "integration".equalsIgnoreCase(type) ||
               "all".equalsIgnoreCase(type);
    }

    private boolean isValidNamingStrategy(String strategy) {
        return "method-scenario".equalsIgnoreCase(strategy) ||
               "given-when".equalsIgnoreCase(strategy) ||
               "given-when-then".equalsIgnoreCase(strategy) ||
               "bdd".equalsIgnoreCase(strategy);
    }
}
