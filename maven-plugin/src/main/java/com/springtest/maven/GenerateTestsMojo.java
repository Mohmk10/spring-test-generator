package com.springtest.maven;

import com.springtest.analyzer.ClassScanner;
import com.springtest.analyzer.ProjectAnalyzer;
import com.springtest.generator.IntegrationTestGenerator;
import com.springtest.generator.ServiceTestGenerator;
import com.springtest.model.ClassInfo;
import com.springtest.model.ClassType;
import com.springtest.naming.BDDNaming;
import com.springtest.naming.GivenWhenThenNaming;
import com.springtest.naming.MethodScenarioExpectedNaming;
import com.springtest.naming.NamingStrategy;
import com.springtest.template.TestFileWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GenerateTestsMojo extends AbstractMojo {

    @Parameter(property = "sourceDirectory", defaultValue = "${project.build.sourceDirectory}")
    private File sourceDirectory;

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.testSourceDirectory}")
    private File outputDirectory;

    @Parameter(property = "testType", defaultValue = "all")
    private String testType;

    @Parameter(property = "namingStrategy", defaultValue = "method-scenario")
    private String namingStrategy;

    @Parameter(property = "includes")
    private List<String> includes;

    @Parameter(property = "excludes")
    private List<String> excludes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Generating tests...");
        getLog().info("Source directory: " + sourceDirectory);
        getLog().info("Output directory: " + outputDirectory);
        getLog().info("Test type: " + testType);
        getLog().info("Naming strategy: " + namingStrategy);

        if (!sourceDirectory.exists()) {
            throw new MojoExecutionException("Source directory does not exist: " + sourceDirectory);
        }

        if (!outputDirectory.exists()) {
            getLog().info("Creating output directory: " + outputDirectory);
            outputDirectory.mkdirs();
        }

        try {
            ProjectAnalyzer analyzer = new ProjectAnalyzer();
            ProjectAnalyzer.AnalysisResult result = analyzer.analyzeProject(sourceDirectory.getAbsolutePath());
            List<ClassInfo> classes = result.getClasses();

            if (includes != null && !includes.isEmpty()) {
                classes = filterIncludes(classes);
            }

            if (excludes != null && !excludes.isEmpty()) {
                classes = filterExcludes(classes);
            }

            classes = classes.stream()
                    .filter(c -> c.classType() != ClassType.OTHER)
                    .toList();

            if (classes.isEmpty()) {
                getLog().warn("No Spring Boot classes found to generate tests for");
                return;
            }

            getLog().info("Found " + classes.size() + " Spring Boot classes");

            NamingStrategy naming = createNamingStrategy();
            TestFileWriter writer = new TestFileWriter(outputDirectory.toPath().toString());

            int generatedCount = 0;
            for (ClassInfo classInfo : classes) {
                generatedCount += generateTestsForClass(classInfo, writer, naming);
            }

            getLog().info("Successfully generated " + generatedCount + " test file(s)");

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate tests", e);
        }
    }

    private List<ClassInfo> filterIncludes(List<ClassInfo> classes) {
        List<ClassInfo> filtered = new ArrayList<>();
        for (ClassInfo classInfo : classes) {
            for (String include : includes) {
                if (matches(classInfo.qualifiedName(), include)) {
                    filtered.add(classInfo);
                    break;
                }
            }
        }
        return filtered;
    }

    private List<ClassInfo> filterExcludes(List<ClassInfo> classes) {
        List<ClassInfo> filtered = new ArrayList<>();
        for (ClassInfo classInfo : classes) {
            boolean excluded = false;
            for (String exclude : excludes) {
                if (matches(classInfo.qualifiedName(), exclude)) {
                    excluded = true;
                    break;
                }
            }
            if (!excluded) {
                filtered.add(classInfo);
            }
        }
        return filtered;
    }

    private boolean matches(String className, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        return className.matches(regex);
    }

    private int generateTestsForClass(ClassInfo classInfo, TestFileWriter writer, NamingStrategy naming) {
        int count = 0;

        boolean generateUnit = testType.equals("all") || testType.equals("unit");
        boolean generateIntegration = testType.equals("all") || testType.equals("integration");

        if (generateUnit) {
            String testCode = generateUnitTest(classInfo, naming);
            if (testCode != null) {
                try {
                    String className = classInfo.simpleName() + "Test";
                    writer.writeTestFile(classInfo.packageName(), className, testCode);
                    getLog().info("Generated: " + className + ".java");
                    count++;
                } catch (Exception e) {
                    getLog().error("Failed to write unit test for " + classInfo.simpleName(), e);
                }
            }
        }

        if (generateIntegration) {
            String testCode = generateIntegrationTest(classInfo);
            if (testCode != null) {
                try {
                    String className = classInfo.simpleName() + "IntegrationTest";
                    writer.writeTestFile(classInfo.packageName(), className, testCode);
                    getLog().info("Generated: " + className + ".java");
                    count++;
                } catch (Exception e) {
                    getLog().error("Failed to write integration test for " + classInfo.simpleName(), e);
                }
            }
        }

        return count;
    }

    private String generateUnitTest(ClassInfo classInfo, NamingStrategy naming) {
        switch (classInfo.classType()) {
            case SERVICE:
                ServiceTestGenerator generator = new ServiceTestGenerator();
                return generator.generateTest(classInfo);
            default:
                return null;
        }
    }

    private String generateIntegrationTest(ClassInfo classInfo) {
        IntegrationTestGenerator generator = new IntegrationTestGenerator();
        return generator.generateTest(classInfo);
    }

    private NamingStrategy createNamingStrategy() {
        return switch (namingStrategy.toLowerCase()) {
            case "bdd" -> new BDDNaming();
            case "given-when-then" -> new GivenWhenThenNaming();
            default -> new MethodScenarioExpectedNaming();
        };
    }
}
