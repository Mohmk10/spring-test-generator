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

class DependencyAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(DependencyAnalyzer.class);

    static List<String> extractDependencies(ClassOrInterfaceDeclaration classDeclaration) {
        List<String> dependencies = new ArrayList<>();

        dependencies.addAll(extractFieldDependencies(classDeclaration));

        dependencies.addAll(extractConstructorDependencies(classDeclaration));

        return dependencies.stream().distinct().collect(Collectors.toList());
    }

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

    static List<String> extractConstructorDependencies(ClassOrInterfaceDeclaration classDeclaration) {
        List<String> dependencies = new ArrayList<>();

        List<ConstructorDeclaration> constructors = classDeclaration.getConstructors();

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

    static boolean isInjectedField(FieldDeclaration field) {
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(
                field.getAnnotations()
        );

        return annotations.stream().anyMatch(AnnotationInfo::isInjection);
    }

    private static ConstructorDeclaration findPrimaryConstructor(List<ConstructorDeclaration> constructors) {
        if (constructors.isEmpty()) {
            return null;
        }

        for (ConstructorDeclaration constructor : constructors) {
            boolean hasAutowired = constructor.getAnnotations().stream()
                    .anyMatch(ann -> ann.getNameAsString().equals("Autowired"));
            if (hasAutowired) {
                return constructor;
            }
        }

        if (constructors.size() == 1) {
            return constructors.get(0);
        }

        return constructors.stream()
                .max((c1, c2) -> Integer.compare(c1.getParameters().size(), c2.getParameters().size()))
                .orElse(null);
    }

    static List<String> extractQualifiedDependencies(ClassOrInterfaceDeclaration classDeclaration) {
        List<String> qualifiedDependencies = new ArrayList<>();

        for (FieldDeclaration field : classDeclaration.getFields()) {
            if (isInjectedField(field)) {
                try {
                    String qualifiedType = field.getCommonType().resolve().describe();
                    qualifiedDependencies.add(qualifiedType);
                } catch (Exception e) {

                    qualifiedDependencies.add(field.getCommonType().asString());
                }
            }
        }

        ConstructorDeclaration primaryConstructor = findPrimaryConstructor(classDeclaration.getConstructors());
        if (primaryConstructor != null) {
            for (Parameter parameter : primaryConstructor.getParameters()) {
                try {
                    String qualifiedType = parameter.getType().resolve().describe();
                    qualifiedDependencies.add(qualifiedType);
                } catch (Exception e) {

                    qualifiedDependencies.add(parameter.getType().asString());
                }
            }
        }

        return qualifiedDependencies.stream().distinct().collect(Collectors.toList());
    }
}
