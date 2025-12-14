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

        FieldInfo field = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(),
                true,
                AccessModifier.PRIVATE,
                false
        );

        String result = mockGenerator.generateMockForField(field);

        assertThat(result).contains("@Mock");
        assertThat(result).contains("private UserService userService;");
        assertThat(result).doesNotContain("@MockBean");
    }

    @Test
    void shouldGenerateMockBeanAnnotation_WhenUsingSpringBootTest() {

        FieldInfo field = new FieldInfo(
                "userService",
                "UserService",
                "com.example.UserService",
                List.of(),
                true,
                AccessModifier.PRIVATE,
                false
        );

        String result = springBootMockGenerator.generateMockForField(field);

        assertThat(result).contains("@MockBean");
        assertThat(result).contains("private UserService userService;");
        assertThat(result).doesNotContain("@Mock\n");
    }

    @Test
    void shouldGenerateInjectMocks_WhenProvidedClassInfo() {

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .build();

        String result = mockGenerator.generateInjectMocks(classInfo);

        assertThat(result).contains("@InjectMocks");
        assertThat(result).contains("private UserController userController;");
    }

    @Test
    void shouldGenerateAllMocks_WhenClassHasDependencies() {

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

        String result = mockGenerator.generateMocks(classInfo);

        assertThat(result).contains("@Mock");
        assertThat(result).contains("private UserService userService;");
        assertThat(result).contains("private UserRepository userRepository;");
        assertThat(result).contains("@InjectMocks");
        assertThat(result).contains("private UserController userController;");
    }

    @Test
    void shouldGenerateImports_WhenGeneratingMocks() {

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

        List<String> imports = mockGenerator.generateImports(classInfo);

        assertThat(imports).contains("import org.mockito.Mock;");
        assertThat(imports).contains("import org.mockito.InjectMocks;");
    }

    @Test
    void shouldGenerateSpringBootImports_WhenUsingSpringBootTest() {

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .build();

        List<String> imports = springBootMockGenerator.generateImports(classInfo);

        assertThat(imports).contains("import org.springframework.boot.test.mock.mockito.MockBean;");
        assertThat(imports).contains("import org.mockito.InjectMocks;");
    }

    @Test
    void shouldGenerateSetupMethod_WhenManualMockSetup() {

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

        String setup = mockGenerator.generateSetupMethod(classInfo);

        assertThat(setup).contains("@BeforeEach");
        assertThat(setup).contains("void setUp()");
        assertThat(setup).contains("userService = mock(UserService.class);");
        assertThat(setup).contains("userController = new UserController(userService);");
    }

    @Test
    void shouldReturnTrue_WhenClassRequiresMocks() {

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

        boolean result = mockGenerator.requiresMocks(classInfo);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_WhenClassDoesNotRequireMocks() {

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .qualifiedName("com.example.UserController")
                .packageName("com.example")
                .build();

        boolean result = mockGenerator.requiresMocks(classInfo);

        assertThat(result).isFalse();
    }

    @Test
    void shouldGenerateMocksForDependencies_WhenProvidedDependencyList() {

        List<String> dependencies = List.of("UserService", "UserRepository");

        String result = mockGenerator.generateMocksForDependencies(dependencies);

        assertThat(result).contains("@Mock");
        assertThat(result).contains("private UserService userService;");
        assertThat(result).contains("private UserRepository userRepository;");
    }

    @Test
    void shouldGenerateFieldDeclarations_WhenProvidedClassInfo() {

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

        String result = mockGenerator.generateFieldDeclarations(classInfo);

        assertThat(result).contains("private UserService userService;");
        assertThat(result).contains("private UserController userController;");
        assertThat(result).doesNotContain("@Mock");
        assertThat(result).doesNotContain("@InjectMocks");
    }
}
