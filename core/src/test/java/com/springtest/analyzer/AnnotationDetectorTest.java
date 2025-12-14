package com.springtest.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.springtest.model.AnnotationInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationDetectorTest {

    private JavaParser javaParser;

    @BeforeEach
    void setUp() {
        javaParser = new JavaParser();
    }

    @Test
    void shouldDetectServiceAnnotation_WhenClassIsAnnotatedWithService() {

        String sourceCode = """
                package com.example;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService {
                }
                """;

        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(classDecl.getAnnotations());

        assertThat(annotations).hasSize(1);
        AnnotationInfo annotation = annotations.get(0);
        assertThat(annotation.name()).isEqualTo("Service");
        assertThat(annotation.qualifiedName()).isEqualTo("org.springframework.stereotype.Service");
        assertThat(annotation.isSpringComponent()).isTrue();
    }

    @Test
    void shouldDetectRestControllerAnnotation_WhenClassIsAnnotatedWithRestController() {

        String sourceCode = """
                package com.example;

                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class UserController {
                }
                """;

        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserController").orElseThrow();
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(classDecl.getAnnotations());

        assertThat(annotations).hasSize(1);
        AnnotationInfo annotation = annotations.get(0);
        assertThat(annotation.name()).isEqualTo("RestController");
        assertThat(annotation.qualifiedName()).isEqualTo("org.springframework.web.bind.annotation.RestController");
        assertThat(annotation.isSpringComponent()).isTrue();
    }

    @Test
    void shouldDetectGetMappingAnnotation_WhenMethodIsAnnotatedWithGetMapping() {

        String sourceCode = """
                package com.example;

                import org.springframework.web.bind.annotation.GetMapping;

                public class UserController {
                    @GetMapping("/users")
                    public String getUsers() {
                        return "users";
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserController").orElseThrow();
        MethodDeclaration method = classDecl.getMethodsByName("getUsers").get(0);
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(method.getAnnotations());

        assertThat(annotations).hasSize(1);
        AnnotationInfo annotation = annotations.get(0);
        assertThat(annotation.name()).isEqualTo("GetMapping");
        assertThat(annotation.qualifiedName()).isEqualTo("org.springframework.web.bind.annotation.GetMapping");
        assertThat(annotation.isWebMapping()).isTrue();
    }

    @Test
    void shouldDetectAutowiredAnnotation_WhenFieldIsAnnotatedWithAutowired() {

        String sourceCode = """
                package com.example;

                import org.springframework.beans.factory.annotation.Autowired;

                public class UserController {
                    @Autowired
                    private UserService userService;
                }
                """;

        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserController").orElseThrow();
        FieldDeclaration field = classDecl.getFieldByName("userService").orElseThrow();
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(field.getAnnotations());

        assertThat(annotations).hasSize(1);
        AnnotationInfo annotation = annotations.get(0);
        assertThat(annotation.name()).isEqualTo("Autowired");
        assertThat(annotation.qualifiedName()).isEqualTo("org.springframework.beans.factory.annotation.Autowired");
        assertThat(annotation.isInjection()).isTrue();
    }

    @Test
    void shouldDetectValidAnnotation_WhenParameterIsAnnotatedWithValid() {

        String sourceCode = """
                package com.example;

                import jakarta.validation.Valid;

                public class UserController {
                    public void createUser(@Valid User user) {
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserController").orElseThrow();
        MethodDeclaration method = classDecl.getMethodsByName("createUser").get(0);
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(
                method.getParameter(0).getAnnotations()
        );

        assertThat(annotations).hasSize(1);
        AnnotationInfo annotation = annotations.get(0);
        assertThat(annotation.name()).isEqualTo("Valid");
        assertThat(annotation.qualifiedName()).isEqualTo("jakarta.validation.Valid");
        assertThat(annotation.isValidation()).isTrue();
    }

    @Test
    void shouldExtractAnnotationAttributes_WhenAnnotationHasValueAttribute() {

        String sourceCode = """
                package com.example;

                import org.springframework.web.bind.annotation.GetMapping;

                public class UserController {
                    @GetMapping("/users")
                    public String getUsers() {
                        return "users";
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserController").orElseThrow();
        MethodDeclaration method = classDecl.getMethodsByName("getUsers").get(0);
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(method.getAnnotations());

        assertThat(annotations).hasSize(1);
        AnnotationInfo annotation = annotations.get(0);
        assertThat(annotation.attributes()).containsKey("value");
        assertThat(annotation.attributes().get("value")).asString().contains("/users");
    }

    @Test
    void shouldDetectMultipleAnnotations_WhenElementHasMultipleAnnotations() {

        String sourceCode = """
                package com.example;

                import org.springframework.web.bind.annotation.RestController;
                import org.springframework.web.bind.annotation.RequestMapping;

                @RestController
                @RequestMapping("/api")
                public class UserController {
                }
                """;

        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserController").orElseThrow();
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(classDecl.getAnnotations());

        assertThat(annotations).hasSize(2);
        assertThat(annotations)
                .extracting(AnnotationInfo::name)
                .containsExactlyInAnyOrder("RestController", "RequestMapping");
        assertThat(annotations)
                .allMatch(ann -> ann.isSpringComponent() || ann.isWebMapping());
    }

    @Test
    void shouldInferQualifiedName_WhenImportIsMissing() {

        String sourceCode = """
                package com.example;

                @Service
                public class UserService {
                }
                """;

        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(classDecl.getAnnotations());

        assertThat(annotations).hasSize(1);
        AnnotationInfo annotation = annotations.get(0);
        assertThat(annotation.name()).isEqualTo("Service");

        assertThat(annotation.qualifiedName()).isEqualTo("org.springframework.stereotype.Service");
    }

    @Test
    void shouldReturnEmptyAttributes_WhenAnnotationHasNoAttributes() {

        String sourceCode = """
                package com.example;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService {
                }
                """;

        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(classDecl.getAnnotations());

        assertThat(annotations).hasSize(1);
        AnnotationInfo annotation = annotations.get(0);
        assertThat(annotation.attributes()).isEmpty();
    }
}
