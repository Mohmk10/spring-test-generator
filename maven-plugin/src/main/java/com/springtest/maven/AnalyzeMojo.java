package com.springtest.maven;

import com.springtest.analyzer.ProjectAnalyzer;
import com.springtest.model.ClassInfo;
import com.springtest.model.ClassType;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mojo(name = "analyze")
public class AnalyzeMojo extends AbstractMojo {

    @Parameter(property = "sourceDirectory", defaultValue = "${project.build.sourceDirectory}")
    private File sourceDirectory;

    @Parameter(property = "detailed", defaultValue = "false")
    private boolean detailed;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Analyzing Spring Boot classes...");
        getLog().info("Source directory: " + sourceDirectory);

        if (!sourceDirectory.exists()) {
            throw new MojoExecutionException("Source directory does not exist: " + sourceDirectory);
        }

        try {
            ProjectAnalyzer analyzer = new ProjectAnalyzer();
            ProjectAnalyzer.AnalysisResult result = analyzer.analyzeProject(sourceDirectory.getAbsolutePath());
            List<ClassInfo> classes = result.getClasses();

            List<ClassInfo> springClasses = classes.stream()
                    .filter(c -> c.classType() != ClassType.OTHER)
                    .toList();

            if (springClasses.isEmpty()) {
                getLog().info("No Spring Boot classes found");
                return;
            }

            getLog().info("");
            getLog().info("=".repeat(60));
            getLog().info("Detected Spring Boot Classes");
            getLog().info("=".repeat(60));

            Map<ClassType, List<ClassInfo>> byType = springClasses.stream()
                    .collect(Collectors.groupingBy(ClassInfo::classType));

            printClassesByType("Services", byType.get(ClassType.SERVICE));
            printClassesByType("Controllers", byType.get(ClassType.CONTROLLER));
            printClassesByType("Repositories", byType.get(ClassType.REPOSITORY));
            printClassesByType("Components", byType.get(ClassType.COMPONENT));
            printClassesByType("Configurations", byType.get(ClassType.CONFIGURATION));

            getLog().info("");
            getLog().info("Summary:");
            getLog().info("  Total Spring classes: " + springClasses.size());
            getLog().info("  Services:       " + count(byType.get(ClassType.SERVICE)));
            getLog().info("  Controllers:    " + count(byType.get(ClassType.CONTROLLER)));
            getLog().info("  Repositories:   " + count(byType.get(ClassType.REPOSITORY)));
            getLog().info("  Components:     " + count(byType.get(ClassType.COMPONENT)));
            getLog().info("  Configurations: " + count(byType.get(ClassType.CONFIGURATION)));
            getLog().info("");

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to analyze project", e);
        }
    }

    private void printClassesByType(String typeName, List<ClassInfo> classes) {
        if (classes == null || classes.isEmpty()) {
            return;
        }

        getLog().info("");
        getLog().info(typeName + ":");
        for (ClassInfo classInfo : classes) {
            getLog().info("  - " + classInfo.qualifiedName());
            if (detailed) {
                getLog().info("    Package: " + classInfo.packageName());
                getLog().info("    Methods: " + classInfo.methods().size());
                getLog().info("    Fields:  " + classInfo.fields().size());
            }
        }
    }

    private int count(List<ClassInfo> classes) {
        return classes == null ? 0 : classes.size();
    }
}
