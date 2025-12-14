package com.springtest.model;

import java.util.List;

public record MethodInfo(
        String name,
        String returnType,
        String qualifiedReturnType,
        List<ParameterInfo> parameters,
        List<AnnotationInfo> annotations,
        List<String> thrownExceptions,
        List<String> possibleExceptions,
        boolean hasValidation,
        AccessModifier accessModifier,
        boolean isStatic,
        boolean isAbstract
) {

    public MethodInfo {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Method name cannot be null or blank");
        }
        if (returnType == null || returnType.isBlank()) {
            throw new IllegalArgumentException("Method return type cannot be null or blank");
        }
        if (accessModifier == null) {
            accessModifier = AccessModifier.PUBLIC;
        }
        parameters = parameters == null ? List.of() : List.copyOf(parameters);
        annotations = annotations == null ? List.of() : List.copyOf(annotations);
        thrownExceptions = thrownExceptions == null ? List.of() : List.copyOf(thrownExceptions);
        possibleExceptions = possibleExceptions == null ? List.of() : List.copyOf(possibleExceptions);
    }

    public boolean isWebMapping() {
        return annotations.stream().anyMatch(AnnotationInfo::isWebMapping);
    }

    public boolean returnsVoid() {
        return "void".equals(returnType);
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public boolean throwsExceptions() {
        return !thrownExceptions.isEmpty() || !possibleExceptions.isEmpty();
    }

    public boolean isGetter() {
        return (name.startsWith("get") || name.startsWith("is")) &&
               parameters.isEmpty() &&
               !returnsVoid();
    }

    public boolean isSetter() {
        return name.startsWith("set") &&
               parameters.size() == 1 &&
               returnsVoid();
    }
}
