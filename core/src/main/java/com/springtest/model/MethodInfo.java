package com.springtest.model;

import java.util.List;

/**
 * Represents information about a method.
 * Contains the method name, return type, parameters, annotations, exceptions, and modifiers.
 *
 * @param name                 Name of the method
 * @param returnType           Simple return type name
 * @param qualifiedReturnType  Fully qualified return type name
 * @param parameters           List of method parameters
 * @param annotations          List of annotations applied to this method
 * @param thrownExceptions     List of exceptions declared in the throws clause
 * @param possibleExceptions   List of exceptions that might be thrown (inferred from method body)
 * @param hasValidation        Whether this method has validation annotations on parameters
 * @param accessModifier       Access modifier of the method
 * @param isStatic             Whether this method is static
 * @param isAbstract           Whether this method is abstract
 */
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
    /**
     * Compact constructor with validation.
     */
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

    /**
     * Checks if this method is a web mapping endpoint.
     *
     * @return true if this method has web mapping annotations
     */
    public boolean isWebMapping() {
        return annotations.stream().anyMatch(AnnotationInfo::isWebMapping);
    }

    /**
     * Checks if this method returns void.
     *
     * @return true if the return type is void
     */
    public boolean returnsVoid() {
        return "void".equals(returnType);
    }

    /**
     * Checks if this method has parameters.
     *
     * @return true if this method has at least one parameter
     */
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    /**
     * Checks if this method throws any exceptions.
     *
     * @return true if this method declares thrown exceptions
     */
    public boolean throwsExceptions() {
        return !thrownExceptions.isEmpty() || !possibleExceptions.isEmpty();
    }

    /**
     * Checks if this method is a getter (starts with "get" or "is" and has no parameters).
     *
     * @return true if this method is a getter
     */
    public boolean isGetter() {
        return (name.startsWith("get") || name.startsWith("is")) &&
               parameters.isEmpty() &&
               !returnsVoid();
    }

    /**
     * Checks if this method is a setter (starts with "set", has one parameter, returns void).
     *
     * @return true if this method is a setter
     */
    public boolean isSetter() {
        return name.startsWith("set") &&
               parameters.size() == 1 &&
               returnsVoid();
    }
}
