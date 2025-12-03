package com.springtest.core.analyzer;

import com.springtest.core.model.AnnotationInfo;
import com.springtest.core.model.ParameterInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ValidationDetector {

    private static final Set<String> VALIDATION_ANNOTATIONS = Set.of(
            "Valid", "NotNull", "NotEmpty", "NotBlank",
            "Size", "Min", "Max", "Pattern",
            "Email", "Positive", "Negative"
    );

    public boolean hasValidation(List<AnnotationInfo> annotations) {
        return annotations.stream()
                .anyMatch(a -> VALIDATION_ANNOTATIONS.contains(a.getSimpleName()));
    }

    public Set<String> detectValidationConstraints(ParameterInfo parameter) {
        Set<String> constraints = new HashSet<>();

        for (AnnotationInfo annotation : parameter.getAnnotations()) {
            String simpleName = annotation.getSimpleName();

            if (VALIDATION_ANNOTATIONS.contains(simpleName)) {
                constraints.add(simpleName);
            }
        }

        return constraints;
    }

    public boolean needsNullCheck(ParameterInfo parameter) {
        return parameter.isNullable() && !parameter.isPrimitive();
    }

    public boolean needsEmptyCheck(ParameterInfo parameter) {
        return parameter.hasAnnotation("NotEmpty") || parameter.hasAnnotation("NotBlank");
    }

    public boolean needsSizeCheck(ParameterInfo parameter) {
        return parameter.hasAnnotation("Size");
    }

    public boolean needsRangeCheck(ParameterInfo parameter) {
        return parameter.hasAnnotation("Min")
                || parameter.hasAnnotation("Max")
                || parameter.hasAnnotation("Positive")
                || parameter.hasAnnotation("Negative");
    }

    public boolean needsFormatCheck(ParameterInfo parameter) {
        return parameter.hasAnnotation("Email")
                || parameter.hasAnnotation("Pattern");
    }
}