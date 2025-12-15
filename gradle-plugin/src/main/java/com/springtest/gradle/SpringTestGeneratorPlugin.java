package com.springtest.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SpringTestGeneratorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        SpringTestGeneratorExtension extension = project.getExtensions().create(
                "springTestGenerator",
                SpringTestGeneratorExtension.class
        );

        extension.getSourceDirectory().convention("src/main/java");
        extension.getOutputDirectory().convention("src/test/java");
        extension.getTestType().convention("all");
        extension.getNamingStrategy().convention("method-scenario");
        extension.getIncludes().convention(project.provider(() -> null));
        extension.getExcludes().convention(project.provider(() -> null));

        project.getTasks().register("generateTests", GenerateTestsTask.class, task -> {
            task.setSourceDirectory(extension.getSourceDirectory().get());
            task.setOutputDirectory(extension.getOutputDirectory().get());
            task.setTestType(extension.getTestType().get());
            task.setNamingStrategy(extension.getNamingStrategy().get());

            if (extension.getIncludes().isPresent()) {
                task.setIncludes(extension.getIncludes().get());
            }

            if (extension.getExcludes().isPresent()) {
                task.setExcludes(extension.getExcludes().get());
            }
        });

        project.getTasks().register("analyzeSpringClasses", AnalyzeTask.class, task -> {
            task.setSourceDirectory(extension.getSourceDirectory().get());
            task.setDetailed(false);
        });
    }
}
