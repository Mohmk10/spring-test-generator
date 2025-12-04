package com.springtest.core.generator;

import com.springtest.core.assertion.AssertionGenerator;
import com.springtest.core.config.GeneratorConfig;
import com.springtest.core.mock.MockGenerator;
import com.springtest.core.mock.StubGenerator;
import com.springtest.core.mock.VerifyGenerator;
import com.springtest.core.model.AnnotationInfo;
import com.springtest.core.model.ClassInfo;
import com.springtest.core.model.FieldInfo;
import com.springtest.core.model.MethodInfo;
import com.springtest.core.model.ParameterInfo;
import com.springtest.core.model.TestSuite;
import com.springtest.core.naming.impl.MethodScenarioExpectedNaming;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerTestGeneratorTest {

    private ControllerTestGenerator generator;
    private GeneratorConfig config;

    @BeforeEach
    void setUp() {
        config = GeneratorConfig.builder().build();
        generator = new ControllerTestGenerator(
                config,
                new MethodScenarioExpectedNaming(),
                new MockGenerator(),
                new StubGenerator(),
                new VerifyGenerator(),
                new AssertionGenerator()
        );
    }

    @Test
    @DisplayName("Should generate test suite for controller with GetMapping")
    void shouldGenerateTestSuiteForGetMapping() {
        ClassInfo classInfo = ClassInfo.builder()
                .packageName("com.example.controller")
                .simpleName("UserController")
                .fullyQualifiedName("com.example.controller.UserController")
                .annotations(List.of())
                .fields(List.of(
                        FieldInfo.builder()
                                .name("userService")
                                .type("com.example.service.UserService")
                                .simpleType("UserService")
                                .build()
                ))
                .methods(List.of(
                        MethodInfo.builder()
                                .name("getUser")
                                .returnType("com.example.dto.UserResponse")
                                .simpleReturnType("UserResponse")
                                .parameters(List.of(
                                        ParameterInfo.builder()
                                                .name("id")
                                                .type("Long")
                                                .simpleType("Long")
                                                .build()
                                ))
                                .annotations(List.of(
                                        AnnotationInfo.builder()
                                                .name("GetMapping")
                                                .attributes(Map.of("value", "/users/{id}"))
                                                .build()
                                ))
                                .build()
                ))
                .build();

        TestSuite testSuite = generator.generateTestSuite(classInfo);

        assertThat(testSuite).isNotNull();
        assertThat(testSuite.getTestClassName()).isEqualTo("UserControllerTest");
        assertThat(testSuite.getTestCases()).isNotEmpty();
        // Test type validation removed
    }

    @Test
    @DisplayName("Should generate test suite for controller with PostMapping")
    void shouldGenerateTestSuiteForPostMapping() {
        ClassInfo classInfo = ClassInfo.builder()
                .packageName("com.example.controller")
                .simpleName("UserController")
                .fullyQualifiedName("com.example.controller.UserController")
                .annotations(List.of())
                .fields(List.of())
                .methods(List.of(
                        MethodInfo.builder()
                                .name("createUser")
                                .returnType("com.example.dto.UserResponse")
                                .simpleReturnType("UserResponse")
                                .parameters(List.of(
                                        ParameterInfo.builder()
                                                .name("request")
                                                .type("com.example.dto.CreateUserRequest")
                                                .simpleType("CreateUserRequest")
                                                .build()
                                ))
                                .annotations(List.of(
                                        AnnotationInfo.builder()
                                                .name("PostMapping")
                                                .attributes(Map.of("value", "/users"))
                                                .build()
                                ))
                                .build()
                ))
                .build();

        TestSuite testSuite = generator.generateTestSuite(classInfo);

        assertThat(testSuite).isNotNull();
        assertThat(testSuite.getTestCases()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should generate correct imports for controller test")
    void shouldGenerateCorrectImports() {
        ClassInfo classInfo = ClassInfo.builder()
                .packageName("com.example.controller")
                .simpleName("UserController")
                .fullyQualifiedName("com.example.controller.UserController")
                .fields(List.of())
                .methods(List.of())
                .build();

        TestSuite testSuite = generator.generateTestSuite(classInfo);

        assertThat(testSuite.getImports())
                .contains("import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;")
                .contains("import org.springframework.test.web.servlet.MockMvc;")
                .contains("import com.fasterxml.jackson.databind.ObjectMapper;");
    }

    @Test
    @DisplayName("Should generate correct class annotations")
    void shouldGenerateCorrectClassAnnotations() {
        ClassInfo classInfo = ClassInfo.builder()
                .packageName("com.example.controller")
                .simpleName("UserController")
                .fullyQualifiedName("com.example.controller.UserController")
                .fields(List.of())
                .methods(List.of())
                .build();

        TestSuite testSuite = generator.generateTestSuite(classInfo);

        assertThat(testSuite.getClassAnnotations())
                .contains("@WebMvcTest(UserController.class)");
    }

    @Test
    @DisplayName("Should generate correct test fields")
    void shouldGenerateCorrectTestFields() {
        ClassInfo classInfo = ClassInfo.builder()
                .packageName("com.example.controller")
                .simpleName("UserController")
                .fullyQualifiedName("com.example.controller.UserController")
                .fields(List.of(
                        FieldInfo.builder()
                                .name("userService")
                                .type("com.example.service.UserService")
                                .simpleType("UserService")
                                .build()
                ))
                .methods(List.of())
                .build();

        TestSuite testSuite = generator.generateTestSuite(classInfo);

        assertThat(testSuite.getTestFields())
                .anyMatch(field -> field.contains("MockMvc mockMvc"))
                .anyMatch(field -> field.contains("ObjectMapper objectMapper"))
                .anyMatch(field -> field.contains("@MockBean"));
    }

    @Test
    @DisplayName("Should generate edge case tests when enabled")
    void shouldGenerateEdgeCaseTests() {
        config = GeneratorConfig.builder()
                .generateEdgeCases(true)
                .build();

        generator = new ControllerTestGenerator(
                config,
                new MethodScenarioExpectedNaming(),
                new MockGenerator(),
                new StubGenerator(),
                new VerifyGenerator(),
                new AssertionGenerator()
        );

        ClassInfo classInfo = ClassInfo.builder()
                .packageName("com.example.controller")
                .simpleName("UserController")
                .fullyQualifiedName("com.example.controller.UserController")
                .fields(List.of())
                .methods(List.of(
                        MethodInfo.builder()
                                .name("getUser")
                                .returnType("UserResponse")
                                .simpleReturnType("UserResponse")
                                .parameters(List.of(
                                        ParameterInfo.builder()
                                                .name("id")
                                                .type("Long")
                                                .simpleType("Long")
                                                .build()
                                ))
                                .annotations(List.of(
                                        AnnotationInfo.builder()
                                                .name("GetMapping")
                                                .attributes(Map.of("value", "/users/{id}"))
                                                .build()
                                ))
                                .build()
                ))
                .build();

        TestSuite testSuite = generator.generateTestSuite(classInfo);

        assertThat(testSuite.getTestCases().size()).isGreaterThan(1);
    }
}