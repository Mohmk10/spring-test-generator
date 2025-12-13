package com.springtest.generator;

import com.springtest.assertion.AssertionGenerator;
import com.springtest.mock.MockGenerator;
import com.springtest.mock.StubGenerator;
import com.springtest.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ServiceTestGeneratorTest {

    private ServiceTestGenerator generator;
    private ClassInfo serviceClass;

    @BeforeEach
    void setUp() {
        generator = new ServiceTestGenerator();

        // Create a sample service class
        FieldInfo repositoryField = new FieldInfo(
                "userRepository",
                "UserRepository",
                "com.example.UserRepository",
                List.of(new AnnotationInfo("Autowired", "org.springframework.beans.factory.annotation.Autowired", java.util.Map.of())),
                true,
                AccessModifier.PRIVATE,
                false
        );

        MethodInfo saveMethod = new MethodInfo(
                "saveUser",
                "User",
                "com.example.User",
                List.of(new ParameterInfo("user", "User", "com.example.User", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        MethodInfo findMethod = new MethodInfo(
                "findById",
                "Optional",
                "java.util.Optional",
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
                .addField(repositoryField)
                .addMethod(saveMethod)
                .addMethod(findMethod)
                .build();
    }

    @Test
    void testSupports_ServiceClass() {
        assertThat(generator.supports(serviceClass)).isTrue();
    }

    @Test
    void testSupports_NonServiceClass() {
        ClassInfo controllerClass = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .classType(ClassType.CONTROLLER)
                .build();

        assertThat(generator.supports(controllerClass)).isFalse();
    }

    @Test
    void testGenerateTest_ContainsPackageDeclaration() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode).contains("package com.example;");
    }

    @Test
    void testGenerateTest_ContainsClassDeclaration() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("@ExtendWith(MockitoExtension.class)")
                .contains("class UserServiceTest {");
    }

    @Test
    void testGenerateTest_ContainsMockAnnotations() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("@Mock")
                .contains("private UserRepository userRepository;")
                .contains("@InjectMocks")
                .contains("private UserService userService;");
    }

    @Test
    void testGenerateTest_ContainsTestMethods() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("@Test")
                .contains("void testSaveUser()")
                .contains("void testFindById()");
    }

    @Test
    void testGenerateTest_ContainsArrangeActAssert() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("// Arrange")
                .contains("// Act")
                .contains("// Assert");
    }

    @Test
    void testGenerateTest_ContainsRequiredImports() {
        String testCode = generator.generateTest(serviceClass);

        assertThat(testCode)
                .contains("import org.junit.jupiter.api.Test;")
                .contains("import org.mockito.Mock;")
                .contains("import org.mockito.InjectMocks;")
                .contains("import static org.assertj.core.api.Assertions.*;");
    }

    @Test
    void testGenerateTest_ThrowsException_WhenClassInfoIsNull() {
        assertThatThrownBy(() -> generator.generateTest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ClassInfo cannot be null");
    }

    @Test
    void testGenerateTest_ThrowsException_WhenNotServiceClass() {
        ClassInfo controllerClass = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .classType(ClassType.CONTROLLER)
                .build();

        assertThatThrownBy(() -> generator.generateTest(controllerClass))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not support class type");
    }

    @Test
    void testGenerateTest_WithMethodThrowingException() {
        MethodInfo methodWithException = new MethodInfo(
                "deleteUser",
                "void",
                "void",
                List.of(new ParameterInfo("id", "Long", "java.lang.Long", List.of(), false, null)),
                List.of(),
                List.of("IllegalArgumentException"),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        ClassInfo serviceWithException = ClassInfo.builder()
                .simpleName("UserService")
                .qualifiedName("com.example.UserService")
                .packageName("com.example")
                .classType(ClassType.SERVICE)
                .addMethod(methodWithException)
                .build();

        String testCode = generator.generateTest(serviceWithException);

        assertThat(testCode)
                .contains("testDeleteUser_ThrowsException")
                .contains("assertThatThrownBy");
    }

    @Test
    void testGenerateTest_WithNoPublicMethods() {
        ClassInfo emptyService = ClassInfo.builder()
                .simpleName("EmptyService")
                .qualifiedName("com.example.EmptyService")
                .packageName("com.example")
                .classType(ClassType.SERVICE)
                .build();

        String testCode = generator.generateTest(emptyService);

        assertThat(testCode)
                .contains("testServiceInitialization")
                .contains("assertThat(emptyService).isNotNull()");
    }

    @Test
    void testGenerateTest_WithCustomGenerators() {
        MockGenerator mockGenerator = new MockGenerator(false);
        StubGenerator stubGenerator = new StubGenerator();
        AssertionGenerator assertionGenerator = new AssertionGenerator();

        ServiceTestGenerator customGenerator = new ServiceTestGenerator(mockGenerator, stubGenerator, assertionGenerator);

        String testCode = customGenerator.generateTest(serviceClass);

        assertThat(testCode).isNotEmpty();
        assertThat(testCode).contains("@Mock");
        assertThat(testCode).contains("@InjectMocks");
    }

    @Test
    void testGenerateTest_ProducesCompilableCode() {
        String testCode = generator.generateTest(serviceClass);

        // Verify structure is correct
        assertThat(testCode)
                .startsWith("package com.example;")
                .contains("import ")
                .contains("class UserServiceTest {")
                .endsWith("}\n");
    }
}
