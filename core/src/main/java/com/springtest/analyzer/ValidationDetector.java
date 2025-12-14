package com.springtest.analyzer;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.springtest.model.AnnotationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

class ValidationDetector {
    private static final Logger logger = LoggerFactory.getLogger(ValidationDetector.class);

    private static final Set<String> VALIDATION_ANNOTATION_NAMES = Set.of(
            "NotNull", "NotBlank", "NotEmpty", "Size", "Min", "Max",
            "Pattern", "Email", "Valid", "Validated", "Positive", "Negative",
            "PositiveOrZero", "NegativeOrZero", "Past", "Future",
            "PastOrPresent", "FutureOrPresent", "DecimalMin", "DecimalMax",
            "Digits", "AssertTrue", "AssertFalse"
    );

    static boolean hasValidation(FieldDeclaration field) {
        return field.getAnnotations().stream()
                .anyMatch(annotation -> VALIDATION_ANNOTATION_NAMES.contains(annotation.getNameAsString()));
    }

    static boolean hasValidation(MethodDeclaration method) {
        return method.getParameters().stream()
                .anyMatch(ValidationDetector::hasValidation);
    }

    static boolean hasValidation(Parameter parameter) {
        return parameter.getAnnotations().stream()
                .anyMatch(annotation -> VALIDATION_ANNOTATION_NAMES.contains(annotation.getNameAsString()));
    }

    static boolean hasValidation(List<AnnotationInfo> annotations) {
        return annotations.stream().anyMatch(AnnotationInfo::isValidation);
    }

    static boolean isRequired(List<AnnotationInfo> annotations) {
        return annotations.stream()
                .anyMatch(annotation ->
                        annotation.name().equals("NotNull") ||
                        annotation.name().equals("NotBlank") ||
                        annotation.name().equals("NotEmpty")
                );
    }
}
