package com.springtest.generator;

import com.springtest.assertion.AssertionGenerator;
import com.springtest.mock.MockGenerator;
import com.springtest.mock.StubGenerator;
import com.springtest.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ControllerTestGeneratorTest {

    private ControllerTestGenerator generator;
    private ClassInfo controllerClass;

    @BeforeEach
    void setUp() {
        generator = new ControllerTestGenerator();

        // Create a sample controller class
        FieldInfo serviceField = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(new AnnotationInfo("Autowired", "org.springframework.beans.factory.annotation.Autowired", java.util.Map.of())),
                true,
                AccessModifier.PRIVATE,
                false
        );

        MethodInfo getMethod = new MethodInfo(
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

        MethodInfo postMethod = new MethodInfo(
                "createUser",
                "User",
                "com.example.User",
                List.of(new ParameterInfo("user", "User", "com.example.User", List.of(), false, null)),
                List.of(new AnnotationInfo("PostMapping", "org.springframework.web.bind.annotation.PostMapping", java.util.Map.of())),
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
                .addField(serviceField)
                .addMethod(getMethod)
                .addMethod(postMethod)
                .build();
    }

    @Test
    void testSupports_ControllerClass() {
        assertThat(generator.supports(controllerClass)).isTrue();
    }

    @Test
    void testSupports_NonControllerClass() {
        ClassInfo serviceClass = ClassInfo.builder()
                .simpleName("UserService")
                .qualifiedName("com.example.UserService")
                .packageName("com.example")
                .classType(ClassType.SERVICE)
                .build();

        assertThat(generator.supports(serviceClass)).isFalse();
    }

    @Test
    void testGenerateTest_ContainsPackageDeclaration() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode).contains("package com.example;");
    }

    @Test
    void testGenerateTest_ContainsWebMvcTestAnnotation() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("@WebMvcTest(UserController.class)")
                .contains("class UserControllerTest {");
    }

    @Test
    void testGenerateTest_ContainsMockMvc() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("@Autowired")
                .contains("private MockMvc mockMvc;");
    }

    @Test
    void testGenerateTest_ContainsMockBean() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("@MockBean")
                .contains("private UserService userService;");
    }

    @Test
    void testGenerateTest_ContainsTestMethods() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("@Test")
                .contains("void testGetUser()")
                .contains("void testCreateUser()");
    }

    @Test
    void testGenerateTest_ContainsMockMvcPerform() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("mockMvc.perform(")
                .contains(".andExpect(status().isOk())");
    }

    @Test
    void testGenerateTest_ContainsErrorTestCases() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("testGetUser_ReturnsError")
                .contains("testCreateUser_ReturnsError");
    }

    @Test
    void testGenerateTest_ContainsRequiredImports() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains("import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;")
                .contains("import org.springframework.test.web.servlet.MockMvc;")
                .contains("import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;")
                .contains("import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;");
    }

    @Test
    void testGenerateTest_PostMethodContainsContentType() {
        String testCode = generator.generateTest(controllerClass);

        assertThat(testCode)
                .contains(".contentType(MediaType.APPLICATION_JSON)")
                .contains(".content(");
    }

    @Test
    void testGenerateTest_ThrowsException_WhenClassInfoIsNull() {
        assertThatThrownBy(() -> generator.generateTest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ClassInfo cannot be null");
    }

    @Test
    void testGenerateTest_ThrowsException_WhenNotControllerClass() {
        ClassInfo serviceClass = ClassInfo.builder()
                .simpleName("UserService")
                .qualifiedName("com.example.UserService")
                .packageName("com.example")
                .classType(ClassType.SERVICE)
                .build();

        assertThatThrownBy(() -> generator.generateTest(serviceClass))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not support class type");
    }

    @Test
    void testGenerateTest_WithNoWebMappingMethods() {
        ClassInfo emptyController = ClassInfo.builder()
                .simpleName("EmptyController")
                .qualifiedName("com.example.EmptyController")
                .packageName("com.example")
                .classType(ClassType.CONTROLLER)
                .build();

        String testCode = generator.generateTest(emptyController);

        assertThat(testCode)
                .contains("testControllerInitialization");
    }

    @Test
    void testGenerateTest_WithCustomGenerators() {
        MockGenerator mockGenerator = new MockGenerator(true);
        StubGenerator stubGenerator = new StubGenerator();
        AssertionGenerator assertionGenerator = new AssertionGenerator();

        ControllerTestGenerator customGenerator = new ControllerTestGenerator(mockGenerator, stubGenerator, assertionGenerator);

        String testCode = customGenerator.generateTest(controllerClass);

        assertThat(testCode).isNotEmpty();
        assertThat(testCode).contains("@WebMvcTest");
    }

    @Test
    void testGenerateTest_ProducesCompilableCode() {
        String testCode = generator.generateTest(controllerClass);

        // Verify structure is correct
        assertThat(testCode)
                .startsWith("package com.example;")
                .contains("import ")
                .contains("class UserControllerTest {")
                .endsWith("}\n");
    }
}
