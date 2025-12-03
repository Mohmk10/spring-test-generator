package com.springtest.core.model;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FieldInfoTest {

    @Test
    void hasAnnotation_WhenAnnotationExists_ShouldReturnTrue() {
        AnnotationInfo autowired = AnnotationInfo.builder()
                .simpleName("Autowired")
                .build();

        FieldInfo field = FieldInfo.builder()
                .name("userRepository")
                .type("UserRepository")
                .annotations(List.of(autowired))
                .build();

        assertThat(field.hasAnnotation("Autowired")).isTrue();
    }

    @Test
    void shouldBeMocked_WithAutowiredAnnotation_ShouldReturnTrue() {
        AnnotationInfo autowired = AnnotationInfo.builder()
                .simpleName("Autowired")
                .build();

        FieldInfo field = FieldInfo.builder()
                .name("userRepository")
                .type("UserRepository")
                .simpleType("UserRepository")
                .annotations(List.of(autowired))
                .finalField(false)
                .staticField(false)
                .build();

        assertThat(field.shouldBeMocked()).isTrue();
    }

    @Test
    void shouldBeMocked_WithFinalField_ShouldReturnFalse() {
        FieldInfo field = FieldInfo.builder()
                .name("constant")
                .type("String")
                .simpleType("String")
                .annotations(List.of())
                .finalField(true)
                .staticField(false)
                .build();

        assertThat(field.shouldBeMocked()).isFalse();
    }

    @Test
    void shouldBeMocked_WithStaticField_ShouldReturnFalse() {
        FieldInfo field = FieldInfo.builder()
                .name("LOGGER")
                .type("Logger")
                .simpleType("Logger")
                .annotations(List.of())
                .finalField(false)
                .staticField(true)
                .build();

        assertThat(field.shouldBeMocked()).isFalse();
    }

    @Test
    void isSpringBean_WithAutowiredAnnotation_ShouldReturnTrue() {
        AnnotationInfo autowired = AnnotationInfo.builder()
                .simpleName("Autowired")
                .build();

        FieldInfo field = FieldInfo.builder()
                .name("service")
                .type("UserService")
                .annotations(List.of(autowired))
                .build();

        assertThat(field.isSpringBean()).isTrue();
    }

    @Test
    void isSpringBean_WithQualifierAnnotation_ShouldReturnTrue() {
        AnnotationInfo qualifier = AnnotationInfo.builder()
                .simpleName("Qualifier")
                .build();

        FieldInfo field = FieldInfo.builder()
                .name("service")
                .type("UserService")
                .annotations(List.of(qualifier))
                .build();

        assertThat(field.isSpringBean()).isTrue();
    }

    @Test
    void isSpringBean_WithNoSpringAnnotation_ShouldReturnFalse() {
        FieldInfo field = FieldInfo.builder()
                .name("service")
                .type("UserService")
                .annotations(List.of())
                .build();

        assertThat(field.isSpringBean()).isFalse();
    }
}