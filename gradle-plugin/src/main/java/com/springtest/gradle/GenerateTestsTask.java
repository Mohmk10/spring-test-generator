package com.springtest.gradle;

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
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GenerateTestsTask extends DefaultTask {

    private String sourceDirectory;
    private String outputDirectory;
    private String testType;
    private String namingStrategy;
    private List<String> includes;
    private List<String> excludes;

    public GenerateTestsTask() {
        setGroup("spring test generator");
        setDescription("Generates unit and integration tests for Spring Boot classes");
    }

    @TaskAction
    public void generateTests() {
        getLogger().lifecycle("Generating tests...");
        getLogger().lifecycle("Source directory: " + sourceDirectory);
        getLogger().lifecycle("Output directory: " + outputDirectory);
        getLogger().lifecycle("Test type: " + testType);
        getLogger().lifecycle("Naming strategy: " + namingStrategy);

        File sourceDir = getProject().file(sourceDirectory);
        File outputDir = getProject().file(outputDirectory);

        if (!sourceDir.exists()) {
            throw new IllegalArgumentException("Source directory does not exist: " + sourceDirectory);
        }

        if (!outputDir.exists()) {
            getLogger().lifecycle("Creating output directory: " + outputDirectory);
            outputDir.mkdirs();
        }

        try {
            ProjectAnalyzer analyzer = new ProjectAnalyzer();
            ProjectAnalyzer.AnalysisResult result = analyzer.analyzeProject(sourceDir.getAbsolutePath());
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
                getLogger().warn("No Spring Boot classes found to generate tests for");
                return;
            }

            getLogger().lifecycle("Found " + classes.size() + " Spring Boot classes");

            NamingStrategy naming = createNamingStrategy();
            TestFileWriter writer = new TestFileWriter(outputDir.getAbsolutePath());

            int generatedCount = 0;
            for (ClassInfo classInfo : classes) {
                generatedCount += generateTestsForClass(classInfo, writer, naming);
            }

            getLogger().lifecycle("Successfully generated " + generatedCount + " test file(s)");

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate tests", e);
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
                    getLogger().lifecycle("Generated: " + className + ".java");
                    count++;
                } catch (Exception e) {
                    getLogger().error("Failed to write unit test for " + classInfo.simpleName(), e);
                }
            }
        }

        if (generateIntegration) {
            String testCode = generateIntegrationTest(classInfo);
            if (testCode != null) {
                try {
                    String className = classInfo.simpleName() + "IntegrationTest";
                    writer.writeTestFile(classInfo.packageName(), className, testCode);
                    getLogger().lifecycle("Generated: " + className + ".java");
                    count++;
                } catch (Exception e) {
                    getLogger().error("Failed to write integration test for " + classInfo.simpleName(), e);
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

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public void setNamingStrategy(String namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }
}
