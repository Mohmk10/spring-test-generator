package com.springtest.cli.commands;

import com.springtest.cli.output.ConsoleReporter;
import com.springtest.core.analyzer.ProjectAnalyzer;
import com.springtest.core.config.ConfigLoader;
import com.springtest.core.config.GeneratorConfig;
import com.springtest.core.model.ClassInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "analyze",
        description = "Analyze a Spring Boot project to identify testable classes",
        mixinStandardHelpOptions = true
)
public class AnalyzeCommand implements Callable<Integer> {

    @Parameters(
            index = "0",
            description = "Project directory to analyze (default: current directory)",
            defaultValue = "."
    )
    private Path projectDir;

    @Option(
            names = {"-p", "--package"},
            description = "Specific package to analyze"
    )
    private String packageName;

    @Option(
            names = {"-v", "--verbose"},
            description = "Enable verbose output"
    )
    private boolean verbose;

    @Option(
            names = {"-c", "--config"},
            description = "Configuration file path"
    )
    private Path configFile;

    private final ConsoleReporter reporter = new ConsoleReporter();

    @Override
    public Integer call() {
        try {
            reporter.printHeader("Analyzing Project");

            GeneratorConfig config = loadConfig();
            ProjectAnalyzer analyzer = new ProjectAnalyzer(config);

            List<ClassInfo> classes = packageName != null
                    ? analyzer.analyzePackage(packageName)
                    : analyzer.analyzeProject();

            if (classes.isEmpty()) {
                reporter.printWarning("No testable classes found.");
                return 0;
            }

            reporter.printSuccess(String.format("Found %d testable classes", classes.size()));

            if (verbose) {
                printDetailedAnalysis(classes);
            }

            ProjectAnalyzer.AnalysisReport report = analyzer.generateReport(classes);
            reporter.printInfo(report.toString());

            return 0;

        } catch (Exception e) {
            reporter.printError("Analysis failed: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    private GeneratorConfig loadConfig() {
        if (configFile != null) {
            return ConfigLoader.loadFromFile(configFile);
        }
        return ConfigLoader.loadDefault();
    }

    private void printDetailedAnalysis(List<ClassInfo> classes) {
        System.out.println();
        System.out.println("Detailed Analysis:");
        System.out.println("==================");

        for (ClassInfo classInfo : classes) {
            System.out.println();
            System.out.printf("Class: %s%n", classInfo.getSimpleName());
            System.out.printf("  Type: %s%n", classInfo.getSpringStereotype());
            System.out.printf("  Methods: %d%n", classInfo.getMethods().size());
            System.out.printf("  Dependencies: %d%n", classInfo.getMockableFields().size());
            System.out.printf("  Test Type: %s%n", classInfo.getTestType());
        }
    }
}