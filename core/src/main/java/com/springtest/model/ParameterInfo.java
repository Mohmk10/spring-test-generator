package com.springtest.model;

import java.util.List;

public record ParameterInfo(
        String name,
        String type,
        String qualifiedType,
        List<AnnotationInfo> annotations,
        boolean required,
        String genericType
) {

    public ParameterInfo {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Parameter name cannot be null or blank");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Parameter type cannot be null or blank");
        }
        annotations = annotations == null ? List.of() : List.copyOf(annotations);
    }

    public boolean hasValidation() {
        return annotations.stream().anyMatch(AnnotationInfo::isValidation);
    }

    public boolean isPrimitive() {
        return switch (type) {
            case "byte", "short", "int", "long", "float", "double", "boolean", "char" -> true;
            default -> false;
        };
    }
}
