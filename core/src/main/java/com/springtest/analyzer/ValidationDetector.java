package com.springtest.analyzer;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.springtest.model.AnnotationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Detects validation annotations on fields, methods, and parameters.
 * Package-private utility class for internal use.
 */
class ValidationDetector {
    private static final Logger logger = LoggerFactory.getLogger(ValidationDetector.class);

    private static final Set<String> VALIDATION_ANNOTATION_NAMES = Set.of(
            "NotNull", "NotBlank", "NotEmpty", "Size", "Min", "Max",
            "Pattern", "Email", "Valid", "Validated", "Positive", "Negative",
            "PositiveOrZero", "NegativeOrZero", "Past", "Future",
            "PastOrPresent", "FutureOrPresent", "DecimalMin", "DecimalMax",
            "Digits", "AssertTrue", "AssertFalse"
    );

    /**
     * Checks if a field has validation annotations.
     *
     * @param field JavaParser field declaration
     * @return true if the field has validation annotations
     */
    static boolean hasValidation(FieldDeclaration field) {
        return field.getAnnotations().stream()
                .anyMatch(annotation -> VALIDATION_ANNOTATION_NAMES.contains(annotation.getNameAsString()));
    }

    /**
     * Checks if a method has validation annotations on its parameters.
     *
     * @param method JavaParser method declaration
     * @return true if any parameter has validation annotations
     */
    static boolean hasValidation(MethodDeclaration method) {
        return method.getParameters().stream()
                .anyMatch(ValidationDetector::hasValidation);
    }

    /**
     * Checks if a parameter has validation annotations.
     *
     * @param parameter JavaParser parameter
     * @return true if the parameter has validation annotations
     */
    static boolean hasValidation(Parameter parameter) {
        return parameter.getAnnotations().stream()
                .anyMatch(annotation -> VALIDATION_ANNOTATION_NAMES.contains(annotation.getNameAsString()));
    }

    /**
     * Checks if a list of annotations contains validation annotations.
     *
     * @param annotations List of AnnotationInfo objects
     * @return true if any annotation is a validation annotation
     */
    static boolean hasValidation(List<AnnotationInfo> annotations) {
        return annotations.stream().anyMatch(AnnotationInfo::isValidation);
    }

    /**
     * Determines if a parameter is required based on validation annotations.
     * A parameter is considered required if it has @NotNull, @NotBlank, or @NotEmpty.
     *
     * @param annotations List of AnnotationInfo objects for the parameter
     * @return true if the parameter is required
     */
    static boolean isRequired(List<AnnotationInfo> annotations) {
        return annotations.stream()
                .anyMatch(annotation ->
                        annotation.name().equals("NotNull") ||
                        annotation.name().equals("NotBlank") ||
                        annotation.name().equals("NotEmpty")
                );
    }
}
