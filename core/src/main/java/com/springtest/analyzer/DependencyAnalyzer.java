package com.springtest.analyzer;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.springtest.model.AnnotationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analyzes class dependencies including field injection and constructor injection.
 * Package-private utility class for internal use.
 */
class DependencyAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(DependencyAnalyzer.class);

    /**
     * Extracts all dependencies from a class.
     * This includes both field-injected and constructor-injected dependencies.
     *
     * @param classDeclaration JavaParser class declaration
     * @return List of dependency type names
     */
    static List<String> extractDependencies(ClassOrInterfaceDeclaration classDeclaration) {
        List<String> dependencies = new ArrayList<>();

        // Extract field-injected dependencies
        dependencies.addAll(extractFieldDependencies(classDeclaration));

        // Extract constructor-injected dependencies
        dependencies.addAll(extractConstructorDependencies(classDeclaration));

        return dependencies.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Extracts dependencies from fields annotated with injection annotations.
     *
     * @param classDeclaration JavaParser class declaration
     * @return List of field dependency type names
     */
    static List<String> extractFieldDependencies(ClassOrInterfaceDeclaration classDeclaration) {
        List<String> dependencies = new ArrayList<>();

        for (FieldDeclaration field : classDeclaration.getFields()) {
            if (isInjectedField(field)) {
                String fieldType = field.getCommonType().asString();
                dependencies.add(fieldType);
                logger.debug("Found injected field dependency: {}", fieldType);
            }
        }

        return dependencies;
    }

    /**
     * Extracts dependencies from constructor parameters.
     * Assumes constructor injection pattern commonly used in Spring.
     *
     * @param classDeclaration JavaParser class declaration
     * @return List of constructor parameter type names
     */
    static List<String> extractConstructorDependencies(ClassOrInterfaceDeclaration classDeclaration) {
        List<String> dependencies = new ArrayList<>();

        List<ConstructorDeclaration> constructors = classDeclaration.getConstructors();

        // Look for the primary constructor (usually the one with most parameters or @Autowired)
        ConstructorDeclaration primaryConstructor = findPrimaryConstructor(constructors);

        if (primaryConstructor != null) {
            for (Parameter parameter : primaryConstructor.getParameters()) {
                String paramType = parameter.getType().asString();
                dependencies.add(paramType);
                logger.debug("Found constructor dependency: {}", paramType);
            }
        }

        return dependencies;
    }

    /**
     * Checks if a field is injected via dependency injection annotations.
     *
     * @param field JavaParser field declaration
     * @return true if the field has injection annotations
     */
    static boolean isInjectedField(FieldDeclaration field) {
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(
                field.getAnnotations()
        );

        return annotations.stream().anyMatch(AnnotationInfo::isInjection);
    }

    /**
     * Finds the primary constructor for dependency injection.
     * Prefers constructors with @Autowired annotation, otherwise the one with most parameters.
     *
     * @param constructors List of constructors
     * @return Primary constructor or null if no suitable constructor found
     */
    private static ConstructorDeclaration findPrimaryConstructor(List<ConstructorDeclaration> constructors) {
        if (constructors.isEmpty()) {
            return null;
        }

        // First, look for @Autowired constructor
        for (ConstructorDeclaration constructor : constructors) {
            boolean hasAutowired = constructor.getAnnotations().stream()
                    .anyMatch(ann -> ann.getNameAsString().equals("Autowired"));
            if (hasAutowired) {
                return constructor;
            }
        }

        // If only one constructor, use it
        if (constructors.size() == 1) {
            return constructors.get(0);
        }

        // Otherwise, use the constructor with the most parameters (likely the injection constructor)
        return constructors.stream()
                .max((c1, c2) -> Integer.compare(c1.getParameters().size(), c2.getParameters().size()))
                .orElse(null);
    }

    /**
     * Extracts qualified type names for dependencies when possible.
     *
     * @param classDeclaration JavaParser class declaration
     * @return List of fully qualified dependency type names
     */
    static List<String> extractQualifiedDependencies(ClassOrInterfaceDeclaration classDeclaration) {
        List<String> qualifiedDependencies = new ArrayList<>();

        // Extract from fields
        for (FieldDeclaration field : classDeclaration.getFields()) {
            if (isInjectedField(field)) {
                try {
                    String qualifiedType = field.getCommonType().resolve().describe();
                    qualifiedDependencies.add(qualifiedType);
                } catch (Exception e) {
                    // If resolution fails, use simple type
                    qualifiedDependencies.add(field.getCommonType().asString());
                }
            }
        }

        // Extract from constructor
        ConstructorDeclaration primaryConstructor = findPrimaryConstructor(classDeclaration.getConstructors());
        if (primaryConstructor != null) {
            for (Parameter parameter : primaryConstructor.getParameters()) {
                try {
                    String qualifiedType = parameter.getType().resolve().describe();
                    qualifiedDependencies.add(qualifiedType);
                } catch (Exception e) {
                    // If resolution fails, use simple type
                    qualifiedDependencies.add(parameter.getType().asString());
                }
            }
        }

        return qualifiedDependencies.stream().distinct().collect(Collectors.toList());
    }
}
