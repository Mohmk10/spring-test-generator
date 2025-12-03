package com.springtest.core.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.springtest.core.model.AnnotationInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationDetectorTest {

    private AnnotationDetector detector;
    private JavaParser parser;

    @BeforeEach
    void setUp() {
        detector = new AnnotationDetector();
        parser = new JavaParser();
    }

    @Test
    void extractAnnotations_FromServiceClass_ShouldDetectServiceAnnotation() {
        String code = """
            @Service
            public class UserService {
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<AnnotationInfo> annotations = detector.extractAnnotations(classDecl.getAnnotations());

        assertThat(annotations).hasSize(1);
        assertThat(annotations.get(0).getSimpleName()).isEqualTo("Service");
    }

    @Test
    void extractAnnotations_FromRestController_ShouldDetectRestController() {
        String code = """
            @RestController
            @RequestMapping("/api")
            public class UserController {
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserController").orElseThrow();

        List<AnnotationInfo> annotations = detector.extractAnnotations(classDecl.getAnnotations());

        assertThat(annotations).hasSize(2);
        assertThat(annotations).extracting(AnnotationInfo::getSimpleName)
                .containsExactly("RestController", "RequestMapping");
    }

    @Test
    void extractAnnotations_WithAnnotationAttributes_ShouldExtractAttributes() {
        String code = """
            @RequestMapping(value = "/api/users", method = RequestMethod.GET)
            public class UserController {
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserController").orElseThrow();

        List<AnnotationInfo> annotations = detector.extractAnnotations(classDecl.getAnnotations());

        assertThat(annotations).hasSize(1);
        AnnotationInfo requestMapping = annotations.get(0);
        assertThat(requestMapping.getAttribute("value")).isEqualTo("\"/api/users\"");
        assertThat(requestMapping.getAttribute("method")).isEqualTo("RequestMethod.GET");
    }

    @Test
    void hasSpringStereotype_WithServiceAnnotation_ShouldReturnTrue() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .build();

        boolean result = detector.hasSpringStereotype(List.of(service));

        assertThat(result).isTrue();
    }

    @Test
    void hasSpringStereotype_WithNonStereotype_ShouldReturnFalse() {
        AnnotationInfo deprecated = AnnotationInfo.builder()
                .simpleName("Deprecated")
                .build();

        boolean result = detector.hasSpringStereotype(List.of(deprecated));

        assertThat(result).isFalse();
    }

    @Test
    void isTestableClass_WithSpringComponent_ShouldReturnTrue() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .build();

        boolean result = detector.isTestableClass(List.of(service));

        assertThat(result).isTrue();
    }
}