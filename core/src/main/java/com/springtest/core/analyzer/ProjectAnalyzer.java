package com.springtest.core.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.springtest.core.config.GeneratorConfig;
import com.springtest.core.model.AnnotationInfo;
import com.springtest.core.model.ClassInfo;
import com.springtest.core.model.FieldInfo;
import com.springtest.core.model.MethodInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ProjectAnalyzer {

    private final GeneratorConfig config;
    private final ClassScanner classScanner;
    private final AnnotationDetector annotationDetector;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final MethodAnalyzer methodAnalyzer;
    private final JavaParser javaParser;

    public ProjectAnalyzer(GeneratorConfig config) {
        this.config = config;
        this.classScanner = new ClassScanner(config);
        this.annotationDetector = new AnnotationDetector();
        this.dependencyAnalyzer = new DependencyAnalyzer(annotationDetector);
        this.methodAnalyzer = new MethodAnalyzer(annotationDetector, new ExceptionDetector());
        this.javaParser = new JavaParser();
    }

    public List<ClassInfo> analyzeProject() {
        log.info("Starting project analysis...");

        List<Path> javaFiles = classScanner.scanJavaFiles();
        List<ClassInfo> analyzedClasses = new ArrayList<>();

        for (Path javaFile : javaFiles) {
            if (classScanner.hasTestFile(javaFile)) {
                log.debug("Skipping {} - test already exists", javaFile.getFileName());
                continue;
            }

            try {
                Optional<ClassInfo> classInfo = analyzeFile(javaFile);
                classInfo.ifPresent(analyzedClasses::add);
            } catch (Exception e) {
                log.error("Failed to analyze file: {}", javaFile, e);
            }
        }

        log.info("Analysis complete. Found {} testable classes", analyzedClasses.size());
        return analyzedClasses;
    }

    public List<ClassInfo> analyzePackage(String packageName) {
        log.info("Analyzing package: {}", packageName);

        if (!config.shouldProcessPackage(packageName)) {
            log.warn("Package {} is excluded by configuration", packageName);
            return List.of();
        }

        List<Path> javaFiles = classScanner.scanPackage(packageName);
        List<ClassInfo> analyzedClasses = new ArrayList<>();

        for (Path javaFile : javaFiles) {
            try {
                Optional<ClassInfo> classInfo = analyzeFile(javaFile);
                classInfo.ifPresent(analyzedClasses::add);
            } catch (Exception e) {
                log.error("Failed to analyze file: {}", javaFile, e);
            }
        }

        log.info("Package analysis complete. Found {} testable classes", analyzedClasses.size());
        return analyzedClasses;
    }

    public Optional<ClassInfo> analyzeFile(Path javaFile) {
        try {
            String content = Files.readString(javaFile);
            CompilationUnit cu = javaParser.parse(content).getResult()
                    .orElseThrow(() -> new RuntimeException("Failed to parse: " + javaFile));

            return cu.getPrimaryType()
                    .filter(type -> type instanceof ClassOrInterfaceDeclaration)
                    .map(type -> (ClassOrInterfaceDeclaration) type)
                    .flatMap(this::analyzeClass);

        } catch (IOException e) {
            log.error("Failed to read file: {}", javaFile, e);
            return Optional.empty();
        }
    }

    private Optional<ClassInfo> analyzeClass(ClassOrInterfaceDeclaration classDecl) {
        List<AnnotationInfo> annotations = annotationDetector
                .extractAnnotations(classDecl.getAnnotations());

        if (!annotationDetector.isTestableClass(annotations)) {
            log.debug("Skipping {} - not a testable Spring component", classDecl.getNameAsString());
            return Optional.empty();
        }

        String packageName = classDecl.findCompilationUnit()
                .flatMap(cu -> cu.getPackageDeclaration())
                .map(pd -> pd.getNameAsString())
                .orElse("");

        String fullyQualifiedName = packageName.isEmpty()
                ? classDecl.getNameAsString()
                : packageName + "." + classDecl.getNameAsString();

        List<FieldInfo> fields = dependencyAnalyzer
                .analyzeFields(classDecl.getFields());

        List<MethodInfo> methods = methodAnalyzer
                .analyzeMethods(classDecl.getMethods());

        if (methods.isEmpty()) {
            log.debug("Skipping {} - no public methods to test", classDecl.getNameAsString());
            return Optional.empty();
        }

        List<String> interfaces = classDecl.getImplementedTypes().stream()
                .map(Object::toString)
                .toList();

        String superClass = classDecl.getExtendedTypes().isEmpty()
                ? null
                : classDecl.getExtendedTypes().get(0).getNameAsString();

        ClassInfo classInfo = ClassInfo.builder()
                .fullyQualifiedName(fullyQualifiedName)
                .simpleName(classDecl.getNameAsString())
                .packageName(packageName)
                .annotations(annotations)
                .fields(fields)
                .methods(methods)
                .interfaceType(classDecl.isInterface())
                .abstractClass(classDecl.isAbstract())
                .superClass(superClass)
                .interfaces(interfaces)
                .build();

        log.info("Analyzed class: {} ({} methods, {} fields)",
                classInfo.getSimpleName(),
                methods.size(),
                fields.size());

        return Optional.of(classInfo);
    }

    public AnalysisReport generateReport(List<ClassInfo> analyzedClasses) {
        int totalMethods = analyzedClasses.stream()
                .mapToInt(c -> c.getMethods().size())
                .sum();

        int totalFields = analyzedClasses.stream()
                .mapToInt(c -> c.getFields().size())
                .sum();

        long serviceCount = analyzedClasses.stream()
                .filter(ClassInfo::isService)
                .count();

        long controllerCount = analyzedClasses.stream()
                .filter(c -> c.isController() || c.isRestController())
                .count();

        long repositoryCount = analyzedClasses.stream()
                .filter(ClassInfo::isRepository)
                .count();

        return new AnalysisReport(
                analyzedClasses.size(),
                totalMethods,
                totalFields,
                (int) serviceCount,
                (int) controllerCount,
                (int) repositoryCount
        );
    }

    public record AnalysisReport(
            int totalClasses,
            int totalMethods,
            int totalFields,
            int serviceCount,
            int controllerCount,
            int repositoryCount
    ) {
        @Override
        public String toString() {
            return String.format("""
                
                ╔═══════════════════════════════════════════╗
                ║      PROJECT ANALYSIS REPORT              ║
                ╠═══════════════════════════════════════════╣
                ║ Total classes analyzed  : %-15d ║
                ║ Total methods found     : %-15d ║
                ║ Total fields found      : %-15d ║
                ║                                           ║
                ║ By type:                                  ║
                ║   @Service              : %-15d ║
                ║   @Controller/@Rest     : %-15d ║
                ║   @Repository           : %-15d ║
                ╚═══════════════════════════════════════════╝
                """,
                    totalClasses, totalMethods, totalFields,
                    serviceCount, controllerCount, repositoryCount
            );
        }
    }
}