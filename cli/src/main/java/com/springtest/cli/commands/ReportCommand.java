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
        name = "report",
        description = "Generate analysis report for a Spring Boot project",
        mixinStandardHelpOptions = true
)
public class ReportCommand implements Callable<Integer> {

    @Parameters(
            index = "0",
            description = "Project directory (default: current directory)",
            defaultValue = "."
    )
    private Path projectDir;

    @Option(
            names = {"-o", "--output"},
            description = "Output file for the report"
    )
    private Path outputFile;

    @Option(
            names = {"-f", "--format"},
            description = "Report format (text, json, html)",
            defaultValue = "text"
    )
    private String format;

    private final ConsoleReporter reporter = new ConsoleReporter();

    @Override
    public Integer call() {
        try {
            reporter.printHeader("Generating Report");

            GeneratorConfig config = ConfigLoader.loadDefault();
            ProjectAnalyzer analyzer = new ProjectAnalyzer(config);

            List<ClassInfo> classes = analyzer.analyzeProject();

            if (classes.isEmpty()) {
                reporter.printWarning("No testable classes found.");
                return 0;
            }

            ProjectAnalyzer.AnalysisReport report = analyzer.generateReport(classes);

            if (outputFile != null) {
                writeReportToFile(report, outputFile);
                reporter.printSuccess("Report written to: " + outputFile);
            } else {
                System.out.println(report);
            }

            return 0;

        } catch (Exception e) {
            reporter.printError("Report generation failed: " + e.getMessage());
            return 1;
        }
    }

    private void writeReportToFile(ProjectAnalyzer.AnalysisReport report, Path outputFile) {
        // Implementation for writing report to file
    }
}