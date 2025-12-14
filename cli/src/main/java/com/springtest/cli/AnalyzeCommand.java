package com.springtest.cli;

import com.springtest.analyzer.ClassScanner;
import com.springtest.model.ClassInfo;
import com.springtest.model.ClassType;
import com.springtest.model.MethodInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(
    name = "analyze",
    description = "Analyze source code and list detected Spring Boot classes"
)
public class AnalyzeCommand implements Callable<Integer> {

    @ParentCommand
    private SpringTestGeneratorCommand parent;

    @Option(names = {"--source", "-s"}, description = "Source code directory path", required = true)
    private String sourcePath;

    @Option(names = {"--detail", "-d"}, description = "Show detailed information including methods")
    private boolean showDetails;

    @Override
    public Integer call() throws Exception {
        Path source = Paths.get(sourcePath);
        if (!Files.exists(source)) {
            System.err.println("Error: Source path does not exist: " + sourcePath);
            return 1;
        }

        if (parent.isVerbose()) {
            System.out.println("Analyzing source directory: " + sourcePath);
        }

        ClassScanner scanner = new ClassScanner();
        ClassInfo classInfo = scanner.scanClass(source.toString());

        if (classInfo == null) {
            System.out.println("No Spring Boot classes found in: " + sourcePath);
            return 0;
        }

        printClassInfo(classInfo);

        return 0;
    }

    private void printClassInfo(ClassInfo classInfo) {
        System.out.println("\nDetected Spring Boot Class:");
        System.out.println("============================");
        System.out.println("Name:    " + classInfo.qualifiedName());
        System.out.println("Type:    " + formatClassType(classInfo.classType()));
        System.out.println("Package: " + classInfo.packageName());

        if (!classInfo.annotations().isEmpty()) {
            System.out.println("\nAnnotations:");
            classInfo.annotations().forEach(annotation ->
                System.out.println("  - @" + annotation.name())
            );
        }

        if (!classInfo.getInjectedFields().isEmpty()) {
            System.out.println("\nInjected Dependencies:");
            classInfo.getInjectedFields().forEach(field ->
                System.out.println("  - " + field.type() + " " + field.name())
            );
        }

        if (showDetails && !classInfo.methods().isEmpty()) {
            System.out.println("\nMethods:");
            for (MethodInfo method : classInfo.methods()) {
                if (!method.isGetter() && !method.isSetter()) {
                    String signature = method.name() + "(" + method.parameters().size() + " params)";
                    String returnType = method.returnsVoid() ? "void" : method.returnType();
                    System.out.println("  - " + signature + " : " + returnType);
                }
            }
        }

        System.out.println("\nTest Generation Support:");
        System.out.println("  Unit Tests:        " + (classInfo.isSpringComponent() ? "Yes" : "No"));
        System.out.println("  Integration Tests: " + (classInfo.isSpringComponent() ? "Yes" : "No"));
    }

    private String formatClassType(ClassType type) {
        return switch (type) {
            case CONTROLLER -> "REST Controller";
            case SERVICE -> "Service";
            case REPOSITORY -> "Repository";
            case COMPONENT -> "Component";
            default -> "Unknown";
        };
    }
}
