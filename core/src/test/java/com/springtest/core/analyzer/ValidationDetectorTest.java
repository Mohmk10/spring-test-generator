package com.springtest.core.analyzer;

import com.springtest.core.model.AnnotationInfo;
import com.springtest.core.model.ParameterInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationDetectorTest {

    private ValidationDetector detector;

    @BeforeEach
    void setUp() {
        detector = new ValidationDetector();
    }

    @Test
    void hasValidation_WithValidAnnotation_ShouldReturnTrue() {
        AnnotationInfo valid = AnnotationInfo.builder()
                .simpleName("Valid")
                .build();

        boolean result = detector.hasValidation(List.of(valid));

        assertThat(result).isTrue();
    }

    @Test
    void hasValidation_WithNotNullAnnotation_ShouldReturnTrue() {
        AnnotationInfo notNull = AnnotationInfo.builder()
                .simpleName("NotNull")
                .build();

        boolean result = detector.hasValidation(List.of(notNull));

        assertThat(result).isTrue();
    }

    @Test
    void hasValidation_WithNotEmptyAnnotation_ShouldReturnTrue() {
        AnnotationInfo notEmpty = AnnotationInfo.builder()
                .simpleName("NotEmpty")
                .build();

        boolean result = detector.hasValidation(List.of(notEmpty));

        assertThat(result).isTrue();
    }

    @Test
    void hasValidation_WithSizeAnnotation_ShouldReturnTrue() {
        AnnotationInfo size = AnnotationInfo.builder()
                .simpleName("Size")
                .build();

        boolean result = detector.hasValidation(List.of(size));

        assertThat(result).isTrue();
    }

    @Test
    void hasValidation_WithEmailAnnotation_ShouldReturnTrue() {
        AnnotationInfo email = AnnotationInfo.builder()
                .simpleName("Email")
                .build();

        boolean result = detector.hasValidation(List.of(email));

        assertThat(result).isTrue();
    }

    @Test
    void hasValidation_WithNoValidationAnnotation_ShouldReturnFalse() {
        AnnotationInfo pathVariable = AnnotationInfo.builder()
                .simpleName("PathVariable")
                .build();

        boolean result = detector.hasValidation(List.of(pathVariable));

        assertThat(result).isFalse();
    }

    @Test
    void detectValidationConstraints_ShouldReturnAllConstraints() {
        AnnotationInfo notNull = AnnotationInfo.builder()
                .simpleName("NotNull")
                .build();

        AnnotationInfo size = AnnotationInfo.builder()
                .simpleName("Size")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("name")
                .type("String")
                .annotations(List.of(notNull, size))
                .build();

        Set<String> constraints = detector.detectValidationConstraints(parameter);

        assertThat(constraints).containsExactlyInAnyOrder("NotNull", "Size");
    }

    @Test
    void needsNullCheck_ForNullableNonPrimitive_ShouldReturnTrue() {
        ParameterInfo parameter = ParameterInfo.builder()
                .name("id")
                .type("Long")
                .nullable(true)
                .primitive(false)
                .annotations(List.of())
                .build();

        boolean result = detector.needsNullCheck(parameter);

        assertThat(result).isTrue();
    }

    @Test
    void needsNullCheck_ForPrimitive_ShouldReturnFalse() {
        ParameterInfo parameter = ParameterInfo.builder()
                .name("age")
                .type("int")
                .nullable(false)
                .primitive(true)
                .annotations(List.of())
                .build();

        boolean result = detector.needsNullCheck(parameter);

        assertThat(result).isFalse();
    }

    @Test
    void needsEmptyCheck_WithNotEmptyAnnotation_ShouldReturnTrue() {
        AnnotationInfo notEmpty = AnnotationInfo.builder()
                .simpleName("NotEmpty")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("name")
                .type("String")
                .annotations(List.of(notEmpty))
                .build();

        boolean result = detector.needsEmptyCheck(parameter);

        assertThat(result).isTrue();
    }

    @Test
    void needsEmptyCheck_WithNotBlankAnnotation_ShouldReturnTrue() {
        AnnotationInfo notBlank = AnnotationInfo.builder()
                .simpleName("NotBlank")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("name")
                .type("String")
                .annotations(List.of(notBlank))
                .build();

        boolean result = detector.needsEmptyCheck(parameter);

        assertThat(result).isTrue();
    }

    @Test
    void needsSizeCheck_WithSizeAnnotation_ShouldReturnTrue() {
        AnnotationInfo size = AnnotationInfo.builder()
                .simpleName("Size")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("items")
                .type("List")
                .annotations(List.of(size))
                .build();

        boolean result = detector.needsSizeCheck(parameter);

        assertThat(result).isTrue();
    }

    @Test
    void needsRangeCheck_WithMinAnnotation_ShouldReturnTrue() {
        AnnotationInfo min = AnnotationInfo.builder()
                .simpleName("Min")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("age")
                .type("int")
                .annotations(List.of(min))
                .build();

        boolean result = detector.needsRangeCheck(parameter);

        assertThat(result).isTrue();
    }

    @Test
    void needsRangeCheck_WithPositiveAnnotation_ShouldReturnTrue() {
        AnnotationInfo positive = AnnotationInfo.builder()
                .simpleName("Positive")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("amount")
                .type("int")
                .annotations(List.of(positive))
                .build();

        boolean result = detector.needsRangeCheck(parameter);

        assertThat(result).isTrue();
    }

    @Test
    void needsFormatCheck_WithEmailAnnotation_ShouldReturnTrue() {
        AnnotationInfo email = AnnotationInfo.builder()
                .simpleName("Email")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("email")
                .type("String")
                .annotations(List.of(email))
                .build();

        boolean result = detector.needsFormatCheck(parameter);

        assertThat(result).isTrue();
    }

    @Test
    void needsFormatCheck_WithPatternAnnotation_ShouldReturnTrue() {
        AnnotationInfo pattern = AnnotationInfo.builder()
                .simpleName("Pattern")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("phone")
                .type("String")
                .annotations(List.of(pattern))
                .build();

        boolean result = detector.needsFormatCheck(parameter);

        assertThat(result).isTrue();
    }
}