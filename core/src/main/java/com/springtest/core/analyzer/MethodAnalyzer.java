package com.springtest.core.analyzer;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.springtest.core.model.AnnotationInfo;
import com.springtest.core.model.ExceptionInfo;
import com.springtest.core.model.MethodInfo;
import com.springtest.core.model.ParameterInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MethodAnalyzer {

    private final AnnotationDetector annotationDetector;
    private final ExceptionDetector exceptionDetector;

    public List<MethodInfo> analyzeMethods(List<MethodDeclaration> methods) {
        return methods.stream()
                .filter(this::isTestableMethod)
                .map(this::createMethodInfo)
                .toList();
    }

    private boolean isTestableMethod(MethodDeclaration method) {
        return method.isPublic() && !method.isAbstract();
    }

    private MethodInfo createMethodInfo(MethodDeclaration method) {
        String returnType = method.getTypeAsString();
        String simpleReturnType = extractSimpleType(returnType);

        List<AnnotationInfo> annotations = annotationDetector
                .extractAnnotations(method.getAnnotations());

        List<ParameterInfo> parameters = method.getParameters().stream()
                .map(this::createParameterInfo)
                .toList();

        List<ExceptionInfo> declaredExceptions = exceptionDetector
                .detectDeclaredExceptions(method);

        List<ExceptionInfo> thrownExceptions = exceptionDetector
                .detectThrownExceptions(method);

        boolean voidReturn = returnType.equals("void");
        boolean returnsOptional = returnType.contains("Optional");
        boolean returnsCollection = isCollectionType(returnType);

        int complexity = calculateComplexity(method, parameters.size(),
                declaredExceptions.size() + thrownExceptions.size());

        return MethodInfo.builder()
                .name(method.getNameAsString())
                .returnType(returnType)
                .simpleReturnType(simpleReturnType)
                .parameters(parameters)
                .annotations(annotations)
                .declaredExceptions(declaredExceptions)
                .thrownExceptions(thrownExceptions)
                .publicMethod(method.isPublic())
                .staticMethod(method.isStatic())
                .voidReturn(voidReturn)
                .returnsOptional(returnsOptional)
                .returnsCollection(returnsCollection)
                .complexity(complexity)
                .build();
    }

    private ParameterInfo createParameterInfo(Parameter parameter) {
        String type = parameter.getTypeAsString();
        String simpleType = extractSimpleType(type);

        List<AnnotationInfo> annotations = annotationDetector
                .extractAnnotations(parameter.getAnnotations());

        boolean primitive = parameter.getType().isPrimitiveType();
        boolean collection = isCollectionType(type);
        boolean nullable = !hasNonNullAnnotation(annotations) && !primitive;

        return ParameterInfo.builder()
                .name(parameter.getNameAsString())
                .type(type)
                .simpleType(simpleType)
                .annotations(annotations)
                .primitive(primitive)
                .collection(collection)
                .nullable(nullable)
                .build();
    }

    private String extractSimpleType(String fullType) {
        String withoutGenerics = fullType.replaceAll("<.*>", "");

        if (withoutGenerics.contains(".")) {
            return withoutGenerics.substring(withoutGenerics.lastIndexOf(".") + 1);
        }

        return withoutGenerics;
    }

    private boolean isCollectionType(String type) {
        return type.contains("List")
                || type.contains("Set")
                || type.contains("Collection")
                || type.contains("Map")
                || type.endsWith("[]");
    }

    private boolean hasNonNullAnnotation(List<AnnotationInfo> annotations) {
        return annotations.stream()
                .anyMatch(a -> a.matches("NonNull") || a.matches("NotNull"));
    }

    private int calculateComplexity(MethodDeclaration method, int paramCount, int exceptionCount) {
        int complexity = 1;

        complexity += paramCount;
        complexity += exceptionCount * 2;

        long conditionalCount = method.findAll(com.github.javaparser.ast.stmt.IfStmt.class).size();
        complexity += (int) conditionalCount;

        long loopCount = method.findAll(com.github.javaparser.ast.stmt.ForStmt.class).size()
                + method.findAll(com.github.javaparser.ast.stmt.WhileStmt.class).size()
                + method.findAll(com.github.javaparser.ast.stmt.ForEachStmt.class).size();
        complexity += (int) loopCount;

        return complexity;
    }
}