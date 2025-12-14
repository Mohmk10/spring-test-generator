package com.springtest.model;

import java.util.List;

public record FieldInfo(
        String name,
        String type,
        String qualifiedType,
        List<AnnotationInfo> annotations,
        boolean injected,
        AccessModifier accessModifier,
        boolean isFinal
) {

    public FieldInfo {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Field name cannot be null or blank");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Field type cannot be null or blank");
        }
        if (accessModifier == null) {
            accessModifier = AccessModifier.PRIVATE;
        }
        annotations = annotations == null ? List.of() : List.copyOf(annotations);
    }

    public boolean hasValidation() {
        return annotations.stream().anyMatch(AnnotationInfo::isValidation);
    }

    public boolean hasValueAnnotation() {
        return annotations.stream()
                .anyMatch(a -> "org.springframework.beans.factory.annotation.Value".equals(a.qualifiedName()));
    }

    public boolean isCollection() {
        return type.equals("List") || type.equals("Set") || type.equals("Collection") ||
               type.equals("Map") || qualifiedType.startsWith("java.util.");
    }
}
