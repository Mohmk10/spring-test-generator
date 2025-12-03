package com.springtest.cli.commands;

import com.springtest.cli.output.ConsoleReporter;
import com.springtest.cli.output.ProgressBar;
import com.springtest.core.analyzer.ProjectAnalyzer;
import com.springtest.core.config.ConfigLoader;
import com.springtest.core.config.GeneratorConfig;
import com.springtest.core.generator.TestGeneratorFactory;
import com.springtest.core.model.ClassInfo;
import com.springtest.core.model.TestSuite;
import com.springtest.core.template.TemplateEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "generate",
        description = "Generate tests for Spring Boot classes",
        mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    @Parameters(
            index = "0",
            description = "Project directory (default: current directory)",
            defaultValue = "."
    )
    private Path projectDir;

    @Option(
            names = {"-p", "--package"},
            description = "Specific package to generate tests for"
    )
    private String packageName;

    @Option(
            names = {"-c", "--class"},
            description = "Specific class to generate tests for"
    )
    private String className;

    @Option(
            names = {"-o", "--output"},
            description = "Output directory for generated tests"
    )
    private Path outputDir;

    @Option(
            names = {"--dry-run"},
            description = "Preview what would be generated without writing files"
    )
    private boolean dryRun;

    @Option(
            names = {"-v", "--verbose"},
            description = "Enable verbose output"
    )
    private boolean verbose;

    @Option(
            names = {"--config"},
            description = "Configuration file path"
    )
    private Path configFile;

    @Option(
            names = {"--skip-existing"},
            description = "Skip classes that already have tests",
            defaultValue = "true"
    )
    private boolean skipExisting;

    private final ConsoleReporter reporter = new ConsoleReporter();
    private final ProgressBar progressBar = new ProgressBar();

    @Override
    public Integer call() {
        try {
            reporter.printHeader("Generating Tests");

            GeneratorConfig config = loadConfig();

            if (dryRun) {
                reporter.printWarning("DRY RUN MODE - No files will be written");
            }

            ProjectAnalyzer analyzer = new ProjectAnalyzer(config);
            List<ClassInfo> classes = analyzeClasses(analyzer);

            if (classes.isEmpty()) {
                reporter.printWarning("No classes to generate tests for.");
                return 0;
            }

            int generatedCount = generateTests(classes, config);

            reporter.printSuccess(String.format(
                    "Successfully generated tests for %d classes", generatedCount
            ));

            return 0;

        } catch (Exception e) {
            reporter.printError("Generation failed: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    private GeneratorConfig loadConfig() {
        GeneratorConfig config;

        if (configFile != null) {
            config = ConfigLoader.loadFromFile(configFile);
        } else {
            config = ConfigLoader.loadDefault();
        }

        if (outputDir != null) {
            config = config.withTestDirectory(outputDir);
        }

        config = config.withSkipExisting(skipExisting);
        config = config.withDryRun(dryRun);
        config = config.withVerboseLogging(verbose);

        return config;
    }

    private List<ClassInfo> analyzeClasses(ProjectAnalyzer analyzer) {
        if (packageName != null) {
            return analyzer.analyzePackage(packageName);
        }

        if (className != null) {
            Path classFile = projectDir.resolve(
                    className.replace(".", "/") + ".java"
            );
            return analyzer.analyzeFile(classFile)
                    .map(List::of)
                    .orElse(List.of());
        }

        return analyzer.analyzeProject();
    }

    private int generateTests(List<ClassInfo> classes, GeneratorConfig config) {
        TestGeneratorFactory factory = new TestGeneratorFactory(config);
        TemplateEngine templateEngine = new TemplateEngine();

        int generated = 0;
        progressBar.start(classes.size());

        for (ClassInfo classInfo : classes) {
            try {
                if (verbose) {
                    System.out.println("Generating tests for: " + classInfo.getSimpleName());
                }

                TestSuite testSuite = factory.createGenerator(classInfo)
                        .generateTestSuite(classInfo);

                if (!dryRun) {
                    Path outputPath = config.getTestDirectory().resolve(
                            classInfo.getPackageName().replace(".", "/")
                    );
                    templateEngine.writeTestClass(testSuite, outputPath);
                }

                generated++;
                progressBar.update(generated);

            } catch (Exception e) {
                reporter.printError("Failed to generate tests for " +
                        classInfo.getSimpleName() + ": " + e.getMessage());
                if (verbose) {
                    e.printStackTrace();
                }
            }
        }

        progressBar.finish();
        return generated;
    }
}