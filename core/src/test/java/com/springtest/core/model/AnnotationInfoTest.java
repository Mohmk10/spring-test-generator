package com.springtest.core.model;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationInfoTest {

    @Test
    void matches_WithMatchingSimpleName_ShouldReturnTrue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .name("org.springframework.stereotype.Service")
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        assertThat(annotation.matches("Service")).isTrue();
    }

    @Test
    void matches_WithNonMatchingSimpleName_ShouldReturnFalse() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .name("org.springframework.stereotype.Service")
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        assertThat(annotation.matches("Controller")).isFalse();
    }

    @Test
    void isSpringStereotype_WithServiceAnnotation_ShouldReturnTrue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringStereotype()).isTrue();
    }

    @Test
    void isSpringStereotype_WithControllerAnnotation_ShouldReturnTrue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("Controller")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringStereotype()).isTrue();
    }

    @Test
    void isSpringStereotype_WithRestControllerAnnotation_ShouldReturnTrue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("RestController")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringStereotype()).isTrue();
    }

    @Test
    void isSpringStereotype_WithRepositoryAnnotation_ShouldReturnTrue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("Repository")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringStereotype()).isTrue();
    }

    @Test
    void isSpringStereotype_WithComponentAnnotation_ShouldReturnTrue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("Component")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringStereotype()).isTrue();
    }

    @Test
    void isSpringStereotype_WithNonStereotypeAnnotation_ShouldReturnFalse() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("Deprecated")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringStereotype()).isFalse();
    }

    @Test
    void isSpringWebMapping_WithRequestMappingAnnotation_ShouldReturnTrue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("RequestMapping")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringWebMapping()).isTrue();
    }

    @Test
    void isSpringWebMapping_WithGetMappingAnnotation_ShouldReturnTrue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("GetMapping")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringWebMapping()).isTrue();
    }

    @Test
    void isSpringWebMapping_WithPostMappingAnnotation_ShouldReturnTrue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("PostMapping")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringWebMapping()).isTrue();
    }

    @Test
    void isSpringWebMapping_WithNonWebAnnotation_ShouldReturnFalse() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        assertThat(annotation.isSpringWebMapping()).isFalse();
    }

    @Test
    void getAttribute_WhenAttributeExists_ShouldReturnValue() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("RequestMapping")
                .attributes(Map.of("value", "/api/users", "method", "GET"))
                .build();

        assertThat(annotation.getAttribute("value")).isEqualTo("/api/users");
        assertThat(annotation.getAttribute("method")).isEqualTo("GET");
    }

    @Test
    void getAttribute_WhenAttributeDoesNotExist_ShouldReturnNull() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        assertThat(annotation.getAttribute("nonExistent")).isNull();
    }

    @Test
    void getAttribute_WhenAttributesIsNull_ShouldReturnNull() {
        AnnotationInfo annotation = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(null)
                .build();

        assertThat(annotation.getAttribute("value")).isNull();
    }
}