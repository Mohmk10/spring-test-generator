package com.springtest.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.springtest.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Scans and parses Java source files to extract class information.
 * Package-private utility class for internal use.
 */
class ClassScanner {
    private static final Logger logger = LoggerFactory.getLogger(ClassScanner.class);
    private final JavaParser javaParser;

    ClassScanner() {
        this.javaParser = new JavaParser();
    }

    /**
     * Scans a Java source file and extracts class information.
     *
     * @param sourceFile Java source file to scan
     * @return Optional ClassInfo if parsing succeeds, empty otherwise
     * @throws FileNotFoundException if the source file does not exist
     */
    Optional<ClassInfo> scanFile(File sourceFile) throws FileNotFoundException {
        logger.info("Scanning file: {}", sourceFile.getAbsolutePath());

        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceFile);

        if (!parseResult.isSuccessful()) {
            logger.error("Failed to parse file: {}", sourceFile.getAbsolutePath());
            parseResult.getProblems().forEach(problem ->
                    logger.error("Parse error: {}", problem.getMessage())
            );
            return Optional.empty();
        }

        CompilationUnit cu = parseResult.getResult().orElseThrow();
        return scanCompilationUnit(cu, sourceFile.getAbsolutePath());
    }

    /**
     * Scans a Java source code string and extracts class information.
     *
     * @param sourceCode Java source code as string
     * @return Optional ClassInfo if parsing succeeds, empty otherwise
     */
    Optional<ClassInfo> scanSource(String sourceCode) {
        logger.debug("Scanning source code");

        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);

        if (!parseResult.isSuccessful()) {
            logger.error("Failed to parse source code");
            parseResult.getProblems().forEach(problem ->
                    logger.error("Parse error: {}", problem.getMessage())
            );
            return Optional.empty();
        }

        CompilationUnit cu = parseResult.getResult().orElseThrow();
        return scanCompilationUnit(cu, null);
    }

    /**
     * Scans a compilation unit and extracts class information.
     *
     * @param cu         Parsed compilation unit
     * @param sourcePath Path to source file (may be null)
     * @return Optional ClassInfo if a primary class is found, empty otherwise
     */
    private Optional<ClassInfo> scanCompilationUnit(CompilationUnit cu, String sourcePath) {
        // First try to get the primary type
        Optional<ClassOrInterfaceDeclaration> primaryClass = cu.getPrimaryType()
                .filter(type -> type instanceof ClassOrInterfaceDeclaration)
                .map(type -> (ClassOrInterfaceDeclaration) type);

        // If primary type not found, try to find any class/interface declaration
        if (primaryClass.isEmpty()) {
            List<ClassOrInterfaceDeclaration> types = cu.findAll(ClassOrInterfaceDeclaration.class);
            if (!types.isEmpty()) {
                primaryClass = Optional.of(types.get(0));
                logger.debug("Using first class/interface found: {}", types.get(0).getNameAsString());
            }
        }

        if (primaryClass.isEmpty()) {
            logger.warn("No class or interface found in compilation unit");
            return Optional.empty();
        }

        ClassOrInterfaceDeclaration classDecl = primaryClass.get();
        return Optional.of(analyzeClass(classDecl, cu, sourcePath));
    }

    /**
     * Analyzes a class declaration and extracts complete class information.
     *
     * @param classDecl  JavaParser class declaration
     * @param cu         Compilation unit containing the class
     * @param sourcePath Path to source file
     * @return ClassInfo object with complete class metadata
     */
    private ClassInfo analyzeClass(ClassOrInterfaceDeclaration classDecl, CompilationUnit cu, String sourcePath) {
        String simpleName = classDecl.getNameAsString();
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");
        String qualifiedName = packageName.isEmpty() ? simpleName : packageName + "." + simpleName;

        logger.debug("Analyzing class: {}", qualifiedName);

        // Extract annotations and determine class type
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(classDecl.getAnnotations());
        ClassType classType = determineClassType(annotations);

        // Extract fields
        List<FieldInfo> fields = analyzeFields(classDecl);

        // Extract methods
        List<MethodInfo> methods = MethodAnalyzer.analyzeMethods(classDecl.getMethods());

        // Extract dependencies
        List<String> dependencies = DependencyAnalyzer.extractDependencies(classDecl);

        // Extract interfaces and superclass
        List<String> implementedInterfaces = classDecl.getImplementedTypes().stream()
                .map(type -> type.asString())
                .toList();

        String superClass = classDecl.getExtendedTypes().stream()
                .findFirst()
                .map(type -> type.asString())
                .orElse(null);

        boolean isInterface = classDecl.isInterface();
        boolean isAbstract = classDecl.isAbstract();

        return ClassInfo.builder()
                .simpleName(simpleName)
                .qualifiedName(qualifiedName)
                .packageName(packageName)
                .classType(classType)
                .annotations(annotations)
                .fields(fields)
                .methods(methods)
                .dependencies(dependencies)
                .implementedInterfaces(implementedInterfaces)
                .superClass(superClass)
                .sourcePath(sourcePath)
                .isInterface(isInterface)
                .isAbstract(isAbstract)
                .build();
    }

    /**
     * Analyzes all fields in a class.
     *
     * @param classDecl JavaParser class declaration
     * @return List of FieldInfo objects
     */
    private List<FieldInfo> analyzeFields(ClassOrInterfaceDeclaration classDecl) {
        List<FieldInfo> fields = new ArrayList<>();

        for (FieldDeclaration field : classDecl.getFields()) {
            for (VariableDeclarator variable : field.getVariables()) {
                fields.add(analyzeField(field, variable));
            }
        }

        return fields;
    }

    /**
     * Analyzes a single field.
     *
     * @param field    JavaParser field declaration
     * @param variable Variable declarator for this field
     * @return FieldInfo object
     */
    private FieldInfo analyzeField(FieldDeclaration field, VariableDeclarator variable) {
        String name = variable.getNameAsString();
        String type = variable.getType().asString();
        String qualifiedType = resolveQualifiedType(variable);

        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(field.getAnnotations());
        boolean injected = DependencyAnalyzer.isInjectedField(field);
        AccessModifier accessModifier = determineFieldAccessModifier(field);
        boolean isFinal = field.isFinal();

        return new FieldInfo(name, type, qualifiedType, annotations, injected, accessModifier, isFinal);
    }

    /**
     * Resolves the qualified type name for a variable.
     *
     * @param variable Variable declarator
     * @return Fully qualified type name
     */
    private String resolveQualifiedType(VariableDeclarator variable) {
        try {
            return variable.getType().resolve().describe();
        } catch (Exception e) {
            return variable.getType().asString();
        }
    }

    /**
     * Determines the access modifier of a field.
     *
     * @param field JavaParser field declaration
     * @return AccessModifier enum value
     */
    private AccessModifier determineFieldAccessModifier(FieldDeclaration field) {
        if (field.hasModifier(Modifier.Keyword.PUBLIC)) {
            return AccessModifier.PUBLIC;
        } else if (field.hasModifier(Modifier.Keyword.PROTECTED)) {
            return AccessModifier.PROTECTED;
        } else if (field.hasModifier(Modifier.Keyword.PRIVATE)) {
            return AccessModifier.PRIVATE;
        } else {
            return AccessModifier.PACKAGE_PRIVATE;
        }
    }

    /**
     * Determines the Spring component type based on class annotations.
     *
     * @param annotations List of class annotations
     * @return ClassType enum value
     */
    private ClassType determineClassType(List<AnnotationInfo> annotations) {
        for (AnnotationInfo annotation : annotations) {
            ClassType type = switch (annotation.qualifiedName()) {
                case "org.springframework.stereotype.Service" -> ClassType.SERVICE;
                case "org.springframework.stereotype.Controller",
                     "org.springframework.web.bind.annotation.RestController" -> ClassType.CONTROLLER;
                case "org.springframework.stereotype.Repository" -> ClassType.REPOSITORY;
                case "org.springframework.stereotype.Component" -> ClassType.COMPONENT;
                case "org.springframework.context.annotation.Configuration" -> ClassType.CONFIGURATION;
                default -> null;
            };

            if (type != null) {
                return type;
            }
        }

        return ClassType.OTHER;
    }
}
