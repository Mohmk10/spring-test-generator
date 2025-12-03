package com.springtest.core.model;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterInfoTest {

    @Test
    void hasAnnotation_WhenAnnotationExists_ShouldReturnTrue() {
        AnnotationInfo notNull = AnnotationInfo.builder()
                .simpleName("NotNull")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("userId")
                .type("Long")
                .annotations(List.of(notNull))
                .build();

        assertThat(parameter.hasAnnotation("NotNull")).isTrue();
    }

    @Test
    void hasAnnotation_WhenAnnotationDoesNotExist_ShouldReturnFalse() {
        ParameterInfo parameter = ParameterInfo.builder()
                .name("userId")
                .type("Long")
                .annotations(List.of())
                .build();

        assertThat(parameter.hasAnnotation("NotNull")).isFalse();
    }

    @Test
    void hasAnnotation_WhenAnnotationsIsNull_ShouldReturnFalse() {
        ParameterInfo parameter = ParameterInfo.builder()
                .name("userId")
                .type("Long")
                .annotations(null)
                .build();

        assertThat(parameter.hasAnnotation("NotNull")).isFalse();
    }

    @Test
    void isValidated_WithValidAnnotation_ShouldReturnTrue() {
        AnnotationInfo valid = AnnotationInfo.builder()
                .simpleName("Valid")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("request")
                .type("UserRequest")
                .annotations(List.of(valid))
                .build();

        assertThat(parameter.isValidated()).isTrue();
    }

    @Test
    void isValidated_WithNotNullAnnotation_ShouldReturnTrue() {
        AnnotationInfo notNull = AnnotationInfo.builder()
                .simpleName("NotNull")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("email")
                .type("String")
                .annotations(List.of(notNull))
                .build();

        assertThat(parameter.isValidated()).isTrue();
    }

    @Test
    void isValidated_WithNotEmptyAnnotation_ShouldReturnTrue() {
        AnnotationInfo notEmpty = AnnotationInfo.builder()
                .simpleName("NotEmpty")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("name")
                .type("String")
                .annotations(List.of(notEmpty))
                .build();

        assertThat(parameter.isValidated()).isTrue();
    }

    @Test
    void isValidated_WithNoValidationAnnotation_ShouldReturnFalse() {
        ParameterInfo parameter = ParameterInfo.builder()
                .name("id")
                .type("Long")
                .annotations(List.of())
                .build();

        assertThat(parameter.isValidated()).isFalse();
    }

    @Test
    void isSpringWebParameter_WithPathVariableAnnotation_ShouldReturnTrue() {
        AnnotationInfo pathVariable = AnnotationInfo.builder()
                .simpleName("PathVariable")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("id")
                .type("Long")
                .annotations(List.of(pathVariable))
                .build();

        assertThat(parameter.isSpringWebParameter()).isTrue();
    }

    @Test
    void isSpringWebParameter_WithRequestBodyAnnotation_ShouldReturnTrue() {
        AnnotationInfo requestBody = AnnotationInfo.builder()
                .simpleName("RequestBody")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("request")
                .type("UserRequest")
                .annotations(List.of(requestBody))
                .build();

        assertThat(parameter.isSpringWebParameter()).isTrue();
    }

    @Test
    void isSpringWebParameter_WithRequestParamAnnotation_ShouldReturnTrue() {
        AnnotationInfo requestParam = AnnotationInfo.builder()
                .simpleName("RequestParam")
                .build();

        ParameterInfo parameter = ParameterInfo.builder()
                .name("page")
                .type("int")
                .annotations(List.of(requestParam))
                .build();

        assertThat(parameter.isSpringWebParameter()).isTrue();
    }

    @Test
    void isSpringWebParameter_WithNoWebAnnotation_ShouldReturnFalse() {
        ParameterInfo parameter = ParameterInfo.builder()
                .name("id")
                .type("Long")
                .annotations(List.of())
                .build();

        assertThat(parameter.isSpringWebParameter()).isFalse();
    }
}