package com.springtest.model;

import java.util.List;

/**
 * Represents information about a method parameter.
 * Contains the parameter name, type, annotations, and other metadata.
 *
 * @param name          Name of the parameter
 * @param type          Simple type name of the parameter
 * @param qualifiedType Fully qualified type name of the parameter
 * @param annotations   List of annotations applied to this parameter
 * @param required      Whether this parameter is required (based on validation annotations)
 * @param genericType   Generic type information if this parameter is generic (e.g., "List&lt;String&gt;")
 */
public record ParameterInfo(
        String name,
        String type,
        String qualifiedType,
        List<AnnotationInfo> annotations,
        boolean required,
        String genericType
) {
    /**
     * Compact constructor with validation.
     */
    public ParameterInfo {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Parameter name cannot be null or blank");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Parameter type cannot be null or blank");
        }
        annotations = annotations == null ? List.of() : List.copyOf(annotations);
    }

    /**
     * Checks if this parameter has any validation annotations.
     *
     * @return true if this parameter has validation annotations
     */
    public boolean hasValidation() {
        return annotations.stream().anyMatch(AnnotationInfo::isValidation);
    }

    /**
     * Checks if this parameter is a primitive type.
     *
     * @return true if this parameter is a primitive type
     */
    public boolean isPrimitive() {
        return switch (type) {
            case "byte", "short", "int", "long", "float", "double", "boolean", "char" -> true;
            default -> false;
        };
    }
}
