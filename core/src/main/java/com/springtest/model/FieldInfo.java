package com.springtest.model;

import java.util.List;

/**
 * Represents information about a class field.
 * Contains the field name, type, annotations, modifiers, and injection status.
 *
 * @param name           Name of the field
 * @param type           Simple type name of the field
 * @param qualifiedType  Fully qualified type name of the field
 * @param annotations    List of annotations applied to this field
 * @param injected       Whether this field is injected (via @Autowired, @Inject, etc.)
 * @param accessModifier Access modifier of the field
 * @param isFinal        Whether this field is declared as final
 */
public record FieldInfo(
        String name,
        String type,
        String qualifiedType,
        List<AnnotationInfo> annotations,
        boolean injected,
        AccessModifier accessModifier,
        boolean isFinal
) {
    /**
     * Compact constructor with validation.
     */
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

    /**
     * Checks if this field has any validation annotations.
     *
     * @return true if this field has validation annotations
     */
    public boolean hasValidation() {
        return annotations.stream().anyMatch(AnnotationInfo::isValidation);
    }

    /**
     * Checks if this field has an @Value annotation.
     *
     * @return true if this field is annotated with @Value
     */
    public boolean hasValueAnnotation() {
        return annotations.stream()
                .anyMatch(a -> "org.springframework.beans.factory.annotation.Value".equals(a.qualifiedName()));
    }

    /**
     * Checks if this field is a collection type.
     *
     * @return true if this field is a collection
     */
    public boolean isCollection() {
        return type.equals("List") || type.equals("Set") || type.equals("Collection") ||
               type.equals("Map") || qualifiedType.startsWith("java.util.");
    }
}
