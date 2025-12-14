package com.springtest.generator;

import com.springtest.assertion.AssertionGenerator;
import com.springtest.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class IntegrationTestGeneratorTest {

    private IntegrationTestGenerator generator;
    private ClassInfo serviceClass;
    private ClassInfo controllerClass;

    @BeforeEach
    void setUp() {
        generator = new IntegrationTestGenerator(true, false);

        MethodInfo serviceMethod = new MethodInfo(
                "processUser",
                "User",
                "com.example.User",
                List.of(new ParameterInfo("id", "Long", "java.lang.Long", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        serviceClass = ClassInfo.builder()
                .simpleName("UserService")
                .qualifiedName("com.example.UserService")
                .packageName("com.example")
                .classType(ClassType.SERVICE)
                .addAnnotation(new AnnotationInfo("Service", "org.springframework.stereotype.Service", java.util.Map.of()))
                .addMethod(serviceMethod)
                .build();

        MethodInfo webMethod = new MethodInfo(
                "getUser",
                "User",
                "com.example.User",
                List.of(new ParameterInfo("id", "Long", "java.lang.Long", List.of(), false, null)),
                List.of(new AnnotationInfo("GetMapping", "org.springframework.web.bind.annotation.GetMapping", java.util.Map.of())),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        controllerClass = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .classType(ClassType.CONTROLLER)
                .addAnnotation(new AnnotationInfo("RestController", "org.springframework.web.bind.annotation.RestController", java.util.Map.of()))
                .addMethod(webMethod)
                .build();
    }

    @Test
    void testSupports_ServiceClass() {
        assertThat(generator.supports(serviceClass)).isTrue();
    }

    @Test
    void testSupports_ControllerClass() {
        assertThat(generator.supports(controllerClass)).isTrue();
    }

    @Test
    void testSupports_NonSpringComponent() {
        ClassInfo otherClass = ClassInfo.builder()
                .simpleName("SomeClass")
                .qualifiedName("com.example.SomeClass")
                .packageName("com.example")
                .classType(ClassType.OTHER)
                .build();

        assertThat(generator.supports(otherClass)).isFalse();
    }

    @Test
    void testGenerateTest_ContainsPackageDeclaration() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode).contains("package com.example;");
    }

    @Test
    void testGenerateTest_ContainsSpringBootTestAnnotation() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("@SpringBootTest")
                .contains("class UserServiceIntegrationTest {");
    }

    @Test
    void testGenerateTest_ServiceClass_WithoutWebEnvironment() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)");
    }

    @Test
    void testGenerateTest_ControllerClass_WithRandomPort() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)");
    }

    @Test
    void testGenerateTest_WithTestContainers() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("@Testcontainers")
                .contains("@Container")
                .contains("PostgreSQLContainer")
                .contains("@DynamicPropertySource");
    }

    @Test
    void testGenerateTest_WithoutTestContainers() {
        IntegrationTestGenerator generatorNoContainers = new IntegrationTestGenerator(false, false);
        String testCode = generatorNoContainers.generateTest(serviceClass);

        assertThat(testCode)
                .doesNotContain("@Testcontainers")
                .doesNotContain("PostgreSQLContainer");
    }

    @Test
    void testGenerateTest_ContainsAutowiredField() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("@Autowired")
                .contains("private UserService userService;");
    }

    @Test
    void testGenerateTest_ControllerClass_ContainsRestTemplate() {
        IntegrationTestGenerator generatorWithRest = new IntegrationTestGenerator(true, true);
        String testCode = generatorWithRest.generateTest(controllerClass);

        assertThat(testCode)
                .contains("private TestRestTemplate restTemplate;")
                .contains("@LocalServerPort")
                .contains("private int port;");
    }

    @Test
    void testGenerateTest_ServiceClass_ContainsServiceTests() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("testIntegration_ProcessUser");
    }

    @Test
    void testGenerateTest_ControllerClass_ContainsRestTemplateTests() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("testIntegration_GetUser")
                .contains("restTemplate.getForEntity");
    }

    @Test
    void testGenerateTest_ContainsRequiredImports() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("import org.springframework.boot.test.context.SpringBootTest;")
                .contains("import org.springframework.beans.factory.annotation.Autowired;")
                .contains("import static org.assertj.core.api.Assertions.*;");
    }

    @Test
    void testGenerateTest_ThrowsException_WhenClassInfoIsNull() {
        assertThatThrownBy(() -> generator.generateTest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ClassInfo cannot be null");
    }

    @Test
    void testGenerateTest_WithCustomAssertionGenerator() {
        AssertionGenerator assertionGenerator = new AssertionGenerator();
        IntegrationTestGenerator customGenerator = new IntegrationTestGenerator(assertionGenerator, true, false);

        String testCode = customGenerator.generateTest(serviceClass);

        assertThat(testCode).isNotEmpty();
        assertThat(testCode).contains("@SpringBootTest");
    }

    @Test
    void testGenerateTest_RepositoryClass() {
        ClassInfo repositoryClass = ClassInfo.builder()
                .simpleName("UserRepository")
                .qualifiedName("com.example.UserRepository")
                .packageName("com.example")
                .classType(ClassType.REPOSITORY)
                .build();

        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("testIntegration_SaveAndFind");
    }

    @Test
    void testGenerateTest_ComponentClass() {
        ClassInfo componentClass = ClassInfo.builder()
                .simpleName("UserComponent")
                .qualifiedName("com.example.UserComponent")
                .packageName("com.example")
                .classType(ClassType.COMPONENT)
                .build();

        String testCode = generator.generateTest(componentClass);

        assertThat(testCode)
                .contains("testIntegrationContextLoads");
    }

    @Test
    void testGenerateTest_ProducesCompilableCode() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .startsWith("package com.example;")
                .contains("import ")
                .contains("class UserServiceIntegrationTest {")
                .endsWith("}\n");
    }


    @Test
    void testGenerateTest_RestTemplateTest_ContainsHttpAssertions() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("ResponseEntity")
                .contains("assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK)")
                .contains("assertThat(response.getBody()).isNotNull()");
    }
}
