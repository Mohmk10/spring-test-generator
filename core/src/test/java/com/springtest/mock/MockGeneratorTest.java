package com.springtest.mock;

import com.springtest.model.AccessModifier;
import com.springtest.model.AnnotationInfo;
import com.springtest.model.ClassInfo;
import com.springtest.model.FieldInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MockGeneratorTest {

    private MockGenerator mockGenerator;
    private MockGenerator springBootMockGenerator;

    @BeforeEach
    void setUp() {
        mockGenerator = new MockGenerator();
        springBootMockGenerator = new MockGenerator(true);
    }

    @Test
    void shouldGenerateMockAnnotation_WhenUsingMockito() {
        // Given
        FieldInfo field = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(),
                true,
                AccessModifier.PRIVATE,
                false
        );

        // When
        String result = mockGenerator.generateMockForField(field);

        // Then
        assertThat(result).contains("@Mock");
        assertThat(result).contains("private UserService userService;");
        assertThat(result).doesNotContain("@MockBean");
    }

    @Test
    void shouldGenerateMockBeanAnnotation_WhenUsingSpringBootTest() {
        // Given
        FieldInfo field = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(),
                true,
                AccessModifier.PRIVATE,
                false
        );

        // When
        String result = springBootMockGenerator.generateMockForField(field);

        // Then
        assertThat(result).contains("@MockBean");
        assertThat(result).contains("private UserService userService;");
        assertThat(result).doesNotContain("@Mock\n");
    }

    @Test
    void shouldGenerateInjectMocks_WhenProvidedClassInfo() {
        // Given
        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .build();

        // When
        String result = mockGenerator.generateInjectMocks(classInfo);

        // Then
        assertThat(result).contains("@InjectMocks");
        assertThat(result).contains("private UserController userController;");
    }

    @Test
    void shouldGenerateAllMocks_WhenClassHasDependencies() {
        // Given
        FieldInfo userService = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(new AnnotationInfo("Autowired", "org.springframework.beans.factory.annotation.Autowired", Map.of())),
                true,
                AccessModifier.PRIVATE,
                false
        );

        FieldInfo userRepository = new FieldInfo(
                "userRepository",
                "UserRepository",
                "com.example.UserRepository",
                List.of(new AnnotationInfo("Autowired", "org.springframework.beans.factory.annotation.Autowired", Map.of())),
                true,
                AccessModifier.PRIVATE,
                false
        );

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .addField(userService)
                .addField(userRepository)
                .build();

        // When
        String result = mockGenerator.generateMocks(classInfo);

        // Then
        assertThat(result).contains("@Mock");
        assertThat(result).contains("private UserService userService;");
        assertThat(result).contains("private UserRepository userRepository;");
        assertThat(result).contains("@InjectMocks");
        assertThat(result).contains("private UserController userController;");
    }

    @Test
    void shouldGenerateImports_WhenGeneratingMocks() {
        // Given
        FieldInfo field = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(),
                true,
                AccessModifier.PRIVATE,
                false
        );

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .addField(field)
                .build();

        // When
        List<String> imports = mockGenerator.generateImports(classInfo);

        // Then
        assertThat(imports).contains("import org.mockito.Mock;");
        assertThat(imports).contains("import org.mockito.InjectMocks;");
    }

    @Test
    void shouldGenerateSpringBootImports_WhenUsingSpringBootTest() {
        // Given
        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .build();

        // When
        List<String> imports = springBootMockGenerator.generateImports(classInfo);

        // Then
        assertThat(imports).contains("import org.springframework.boot.test.mock.mockito.MockBean;");
        assertThat(imports).contains("import org.mockito.InjectMocks;");
    }

    @Test
    void shouldGenerateSetupMethod_WhenManualMockSetup() {
        // Given
        FieldInfo userService = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(),
                true,
                AccessModifier.PRIVATE,
                false
        );

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .addField(userService)
                .build();

        // When
        String setup = mockGenerator.generateSetupMethod(classInfo);

        // Then
        assertThat(setup).contains("@BeforeEach");
        assertThat(setup).contains("void setUp()");
        assertThat(setup).contains("userService = mock(UserService.class);");
        assertThat(setup).contains("userController = new UserController(userService);");
    }

    @Test
    void shouldReturnTrue_WhenClassRequiresMocks() {
        // Given
        FieldInfo field = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(),
                true,
                AccessModifier.PRIVATE,
                false
        );

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .addField(field)
                .build();

        // When
        boolean result = mockGenerator.requiresMocks(classInfo);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_WhenClassDoesNotRequireMocks() {
        // Given
        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .build();

        // When
        boolean result = mockGenerator.requiresMocks(classInfo);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldGenerateMocksForDependencies_WhenProvidedDependencyList() {
        // Given
        List<String> dependencies = List.of("UserService", "UserRepository");

        // When
        String result = mockGenerator.generateMocksForDependencies(dependencies);

        // Then
        assertThat(result).contains("@Mock");
        assertThat(result).contains("private UserService userService;");
        assertThat(result).contains("private UserRepository userRepository;");
    }

    @Test
    void shouldGenerateFieldDeclarations_WhenProvidedClassInfo() {
        // Given
        FieldInfo field = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(),
                true,
                AccessModifier.PRIVATE,
                false
        );

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .addField(field)
                .build();

        // When
        String result = mockGenerator.generateFieldDeclarations(classInfo);

        // Then
        assertThat(result).contains("private UserService userService;");
        assertThat(result).contains("private UserController userController;");
        assertThat(result).doesNotContain("@Mock");
        assertThat(result).doesNotContain("@InjectMocks");
    }
}
