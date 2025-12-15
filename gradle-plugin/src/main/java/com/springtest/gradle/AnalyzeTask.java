package com.springtest.gradle;

import com.springtest.analyzer.ProjectAnalyzer;
import com.springtest.model.ClassInfo;
import com.springtest.model.ClassType;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyzeTask extends DefaultTask {

    private String sourceDirectory;
    private boolean detailed;

    public AnalyzeTask() {
        setGroup("spring test generator");
        setDescription("Analyzes and displays detected Spring Boot classes");
    }

    @TaskAction
    public void analyze() {
        getLogger().lifecycle("Analyzing Spring Boot classes...");
        getLogger().lifecycle("Source directory: " + sourceDirectory);

        File sourceDir = getProject().file(sourceDirectory);

        if (!sourceDir.exists()) {
            throw new IllegalArgumentException("Source directory does not exist: " + sourceDirectory);
        }

        try {
            ProjectAnalyzer analyzer = new ProjectAnalyzer();
            ProjectAnalyzer.AnalysisResult result = analyzer.analyzeProject(sourceDir.getAbsolutePath());
            List<ClassInfo> classes = result.getClasses();

            List<ClassInfo> springClasses = classes.stream()
                    .filter(c -> c.classType() != ClassType.OTHER)
                    .toList();

            if (springClasses.isEmpty()) {
                getLogger().lifecycle("No Spring Boot classes found");
                return;
            }

            getLogger().lifecycle("");
            getLogger().lifecycle("=".repeat(60));
            getLogger().lifecycle("Detected Spring Boot Classes");
            getLogger().lifecycle("=".repeat(60));

            Map<ClassType, List<ClassInfo>> byType = springClasses.stream()
                    .collect(Collectors.groupingBy(ClassInfo::classType));

            printClassesByType("Services", byType.get(ClassType.SERVICE));
            printClassesByType("Controllers", byType.get(ClassType.CONTROLLER));
            printClassesByType("Repositories", byType.get(ClassType.REPOSITORY));
            printClassesByType("Components", byType.get(ClassType.COMPONENT));
            printClassesByType("Configurations", byType.get(ClassType.CONFIGURATION));

            getLogger().lifecycle("");
            getLogger().lifecycle("Summary:");
            getLogger().lifecycle("  Total Spring classes: " + springClasses.size());
            getLogger().lifecycle("  Services:       " + count(byType.get(ClassType.SERVICE)));
            getLogger().lifecycle("  Controllers:    " + count(byType.get(ClassType.CONTROLLER)));
            getLogger().lifecycle("  Repositories:   " + count(byType.get(ClassType.REPOSITORY)));
            getLogger().lifecycle("  Components:     " + count(byType.get(ClassType.COMPONENT)));
            getLogger().lifecycle("  Configurations: " + count(byType.get(ClassType.CONFIGURATION)));
            getLogger().lifecycle("");

        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze project", e);
        }
    }

    private void printClassesByType(String typeName, List<ClassInfo> classes) {
        if (classes == null || classes.isEmpty()) {
            return;
        }

        getLogger().lifecycle("");
        getLogger().lifecycle(typeName + ":");
        for (ClassInfo classInfo : classes) {
            getLogger().lifecycle("  - " + classInfo.qualifiedName());
            if (detailed) {
                getLogger().lifecycle("    Package: " + classInfo.packageName());
                getLogger().lifecycle("    Methods: " + classInfo.methods().size());
                getLogger().lifecycle("    Fields:  " + classInfo.fields().size());
            }
        }
    }

    private int count(List<ClassInfo> classes) {
        return classes == null ? 0 : classes.size();
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setDetailed(boolean detailed) {
        this.detailed = detailed;
    }
}
