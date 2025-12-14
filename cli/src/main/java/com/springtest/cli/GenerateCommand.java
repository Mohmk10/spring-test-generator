package com.springtest.cli;

import com.springtest.analyzer.ClassScanner;
import com.springtest.cli.config.CliConfig;
import com.springtest.generator.*;
import com.springtest.model.ClassInfo;
import com.springtest.naming.BDDNaming;
import com.springtest.naming.GivenWhenThenNaming;
import com.springtest.naming.MethodScenarioExpectedNaming;
import com.springtest.naming.NamingStrategy;
import com.springtest.template.TestFileWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(
    name = "generate",
    description = "Generate tests for Spring Boot classes"
)
public class GenerateCommand implements Callable<Integer> {

    @ParentCommand
    private SpringTestGeneratorCommand parent;

    @Option(names = {"--source", "-s"}, description = "Source code directory path", required = true)
    private String sourcePath;

    @Option(names = {"--output", "-o"}, description = "Output directory path for generated tests", required = true)
    private String outputPath;

    @Option(names = {"--type", "-t"}, description = "Test type: unit, integration, or all (default: all)")
    private String testType = "all";

    @Option(names = {"--naming", "-n"}, description = "Naming strategy: method-scenario, given-when, or bdd (default: method-scenario)")
    private String namingStrategy = "method-scenario";

    @Option(names = {"--class", "-c"}, description = "Specific class name to generate tests for (optional)")
    private String className;

    @Override
    public Integer call() throws Exception {
        CliConfig config = CliConfig.load();

        if (sourcePath == null) {
            sourcePath = config.getDefaultSourcePath();
        }
        if (outputPath == null) {
            outputPath = config.getDefaultOutputPath();
        }
        if (testType == null) {
            testType = config.getDefaultTestType();
        }
        if (namingStrategy == null) {
            namingStrategy = config.getDefaultNamingStrategy();
        }

        Path source = Paths.get(sourcePath);
        if (!Files.exists(source)) {
            System.err.println("Error: Source path does not exist: " + sourcePath);
            return 1;
        }

        if (parent.isVerbose()) {
            System.out.println("Generating tests...");
            System.out.println("  Source: " + sourcePath);
            System.out.println("  Output: " + outputPath);
            System.out.println("  Type: " + testType);
            System.out.println("  Naming: " + namingStrategy);
        }

        NamingStrategy naming = createNamingStrategy(namingStrategy);
        TestFileWriter writer = new TestFileWriter(outputPath);

        if (className != null && !className.isEmpty()) {
            return generateForSpecificClass(source, writer, naming);
        } else {
            return generateForAllClasses(source, writer, naming);
        }
    }

    private Integer generateForSpecificClass(Path source, TestFileWriter writer, NamingStrategy naming) throws Exception {
        System.out.println("Analyzing class: " + className);

        ClassScanner scanner = new ClassScanner();
        ClassInfo classInfo = scanner.scanClass(source.toString());

        if (classInfo == null) {
            System.err.println("Error: Could not analyze class: " + className);
            return 1;
        }

        return generateTests(classInfo, writer, naming);
    }

    private Integer generateForAllClasses(Path source, TestFileWriter writer, NamingStrategy naming) throws Exception {
        System.out.println("Scanning source directory: " + sourcePath);

        ClassScanner scanner = new ClassScanner();
        ClassInfo classInfo = scanner.scanClass(source.toString());

        if (classInfo == null) {
            System.err.println("Error: No Spring Boot classes found in: " + sourcePath);
            return 1;
        }

        return generateTests(classInfo, writer, naming);
    }

    private Integer generateTests(ClassInfo classInfo, TestFileWriter writer, NamingStrategy naming) throws Exception {
        int generated = 0;

        if (shouldGenerateUnit()) {
            generated += generateUnitTests(classInfo, writer);
        }

        if (shouldGenerateIntegration()) {
            generated += generateIntegrationTests(classInfo, writer);
        }

        System.out.println("Successfully generated " + generated + " test file(s)");
        return 0;
    }

    private int generateUnitTests(ClassInfo classInfo, TestFileWriter writer) {
        int count = 0;

        ServiceTestGenerator serviceGenerator = new ServiceTestGenerator();
        if (serviceGenerator.supports(classInfo)) {
            String content = serviceGenerator.generateTest(classInfo);
            writer.writeServiceTest(classInfo.packageName(), classInfo.simpleName(), content);
            System.out.println("Generated: " + classInfo.simpleName() + "Test.java");
            count++;
        }

        ControllerTestGenerator controllerGenerator = new ControllerTestGenerator();
        if (controllerGenerator.supports(classInfo)) {
            String content = controllerGenerator.generateTest(classInfo);
            writer.writeControllerTest(classInfo.packageName(), classInfo.simpleName(), content);
            System.out.println("Generated: " + classInfo.simpleName() + "Test.java");
            count++;
        }

        RepositoryTestGenerator repositoryGenerator = new RepositoryTestGenerator();
        if (repositoryGenerator.supports(classInfo)) {
            String content = repositoryGenerator.generateTest(classInfo);
            writer.writeRepositoryTest(classInfo.packageName(), classInfo.simpleName(), content);
            System.out.println("Generated: " + classInfo.simpleName() + "Test.java");
            count++;
        }

        return count;
    }

    private int generateIntegrationTests(ClassInfo classInfo, TestFileWriter writer) {
        IntegrationTestGenerator integrationGenerator = new IntegrationTestGenerator();
        if (integrationGenerator.supports(classInfo)) {
            String content = integrationGenerator.generateTest(classInfo);
            writer.writeIntegrationTest(classInfo.packageName(), classInfo.simpleName(), content);
            System.out.println("Generated: " + classInfo.simpleName() + "IntegrationTest.java");
            return 1;
        }
        return 0;
    }

    private boolean shouldGenerateUnit() {
        return "unit".equalsIgnoreCase(testType) || "all".equalsIgnoreCase(testType);
    }

    private boolean shouldGenerateIntegration() {
        return "integration".equalsIgnoreCase(testType) || "all".equalsIgnoreCase(testType);
    }

    private NamingStrategy createNamingStrategy(String strategy) {
        return switch (strategy.toLowerCase()) {
            case "given-when", "given-when-then" -> new GivenWhenThenNaming();
            case "bdd" -> new BDDNaming();
            default -> new MethodScenarioExpectedNaming();
        };
    }
}
