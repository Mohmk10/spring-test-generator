package com.springtest.generator;

import com.springtest.assertion.AssertionGenerator;
import com.springtest.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RepositoryTestGeneratorTest {

    private RepositoryTestGenerator generator;
    private ClassInfo repositoryClass;

    @BeforeEach
    void setUp() {
        generator = new RepositoryTestGenerator(true);

        // Create a sample repository class
        MethodInfo findByNameMethod = new MethodInfo(
                "findByName",
                "List",
                "java.util.List",
                List.of(new ParameterInfo("name", "String", "java.lang.String", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        MethodInfo existsByEmailMethod = new MethodInfo(
                "existsByEmail",
                "boolean",
                "boolean",
                List.of(new ParameterInfo("email", "String", "java.lang.String", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        repositoryClass = ClassInfo.builder()
                .simpleName("UserRepository")
                .qualifiedName("com.example.UserRepository")
                .packageName("com.example")
                .classType(ClassType.REPOSITORY)
                .addMethod(findByNameMethod)
                .addMethod(existsByEmailMethod)
                .build();
    }

    @Test
    void testSupports_RepositoryClass() {
        assertThat(generator.supports(repositoryClass)).isTrue();
    }

    @Test
    void testSupports_NonRepositoryClass() {
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
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode).contains("package com.example;");
    }

    @Test
    void testGenerateTest_ContainsDataJpaTestAnnotation() {
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("@DataJpaTest")
                .contains("class UserRepositoryTest {");
    }

    @Test
    void testGenerateTest_WithTestContainers() {
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("@Testcontainers")
                .contains("@Container")
                .contains("PostgreSQLContainer")
                .contains("@DynamicPropertySource");
    }

    @Test
    void testGenerateTest_WithoutTestContainers() {
        RepositoryTestGenerator generatorNoContainers = new RepositoryTestGenerator(false);
        String testCode = generatorNoContainers.generateTest(repositoryClass);

        assertThat(testCode)
                .doesNotContain("@Testcontainers")
                .doesNotContain("PostgreSQLContainer");
    }

    @Test
    void testGenerateTest_ContainsRepositoryField() {
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("@Autowired")
                .contains("private UserRepository userRepository;");
    }

    @Test
    void testGenerateTest_ContainsEntityManager() {
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("@Autowired")
                .contains("private TestEntityManager entityManager;");
    }

    @Test
    void testGenerateTest_ContainsCrudTests() {
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("void testSave()")
                .contains("void testFindById()")
                .contains("void testFindAll()")
                .contains("void testDelete()");
    }

    @Test
    void testGenerateTest_ContainsCustomQueryTests() {
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("void testFindByName()")
                .contains("void testExistsByEmail()");
    }

    @Test
    void testGenerateTest_ContainsRequiredImports() {
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;")
                .contains("import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;")
                .contains("import static org.assertj.core.api.Assertions.*;");
    }

    @Test
    void testGenerateTest_ContainsEntityManagerOperations() {
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("entityManager.persist")
                .contains("entityManager.flush");
    }

    @Test
    void testGenerateTest_ThrowsException_WhenClassInfoIsNull() {
        assertThatThrownBy(() -> generator.generateTest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ClassInfo cannot be null");
    }

    @Test
    void testGenerateTest_ThrowsException_WhenNotRepositoryClass() {
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
    void testGenerateTest_InfersEntityName() {
        String testCode = generator.generateTest(repositoryClass);

        // Repository is UserRepository, so entity should be User
        assertThat(testCode)
                .contains("User entity = new User()");
    }

    @Test
    void testGenerateTest_WithCustomAssertionGenerator() {
        AssertionGenerator assertionGenerator = new AssertionGenerator();
        RepositoryTestGenerator customGenerator = new RepositoryTestGenerator(assertionGenerator, true);

        String testCode = customGenerator.generateTest(repositoryClass);

        assertThat(testCode).isNotEmpty();
        assertThat(testCode).contains("@DataJpaTest");
    }

    @Test
    void testGenerateTest_ProducesCompilableCode() {
        String testCode = generator.generateTest(repositoryClass);

        // Verify structure is correct
        assertThat(testCode)
                .startsWith("package com.example;")
                .contains("import ")
                .contains("class UserRepositoryTest {")
                .endsWith("}\n");
    }

    @Test
    void testGenerateTest_ContainsArrangeActAssert() {
        String testCode = generator.generateTest(repositoryClass);

        assertThat(testCode)
                .contains("// Arrange")
                .contains("// Act")
                .contains("// Assert");
    }
}
