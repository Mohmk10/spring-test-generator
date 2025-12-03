package com.springtest.core.generator;

import com.springtest.core.assertion.AssertionGenerator;
import com.springtest.core.config.GeneratorConfig;
import com.springtest.core.mock.MockGenerator;
import com.springtest.core.mock.StubGenerator;
import com.springtest.core.mock.VerifyGenerator;
import com.springtest.core.model.ClassInfo;
import com.springtest.core.model.MethodInfo;
import com.springtest.core.model.TestCase;
import com.springtest.core.naming.NamingStrategy;

import java.util.ArrayList;
import java.util.List;

public class RepositoryTestGenerator extends TestGenerator {

    public RepositoryTestGenerator(GeneratorConfig config, NamingStrategy namingStrategy,
                                   MockGenerator mockGenerator, StubGenerator stubGenerator,
                                   VerifyGenerator verifyGenerator, AssertionGenerator assertionGenerator) {
        super(config, namingStrategy, mockGenerator, stubGenerator, verifyGenerator, assertionGenerator);
    }

    @Override
    protected TestCase generateHappyPathTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "happy_path");
        String displayName = namingStrategy.generateDisplayName(method, "happy_path");

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("// Persist test entity");
        givenStatements.add("Entity entity = entityManager.persistAndFlush(createTestEntity());");

        String whenStatement = generateRepositoryCall(method, classInfo);

        List<String> thenStatements = assertionGenerator.generateAssertions(method, "happy_path");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("happy_path")
                .description("Test repository operation with persisted entity")
                .givenStatements(givenStatements)
                .mockSetups(List.of())
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .expectsException(false)
                .priority(10)
                .build();
    }

    @Override
    protected List<TestCase> generateEdgeCaseTests(MethodInfo method, ClassInfo classInfo) {
        List<TestCase> testCases = new ArrayList<>();

        if (method.isReturnsOptional() || method.getName().contains("find")) {
            testCases.add(generateNotFoundTest(method, classInfo));
        }

        return testCases;
    }

    @Override
    protected List<TestCase> generateExceptionTests(MethodInfo method, ClassInfo classInfo) {
        return List.of();
    }

    private TestCase generateNotFoundTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "not_found");
        String displayName = namingStrategy.generateDisplayName(method, "not_found");

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("Long nonExistentId = 999L;");

        String whenStatement = generateRepositoryCall(method, classInfo);

        List<String> thenStatements = assertionGenerator.generateAssertions(method, "not_found");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("not_found")
                .description("Test repository returns empty when entity not found")
                .givenStatements(givenStatements)
                .mockSetups(List.of())
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .expectsException(false)
                .priority(9)
                .build();
    }

    @Override
    protected List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.DisplayName;");
        imports.add("import org.junit.jupiter.api.BeforeEach;");
        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;");
        imports.add("import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;");
        imports.add("import static org.assertj.core.api.Assertions.*;");

        imports.add(String.format("import %s;", classInfo.getFullyQualifiedName()));

        return imports;
    }

    @Override
    protected List<String> generateClassAnnotations(ClassInfo classInfo) {
        return List.of("@DataJpaTest");
    }

    @Override
    protected List<String> generateTestFields(ClassInfo classInfo) {
        List<String> fields = new ArrayList<>();

        fields.add("@Autowired\nprivate TestEntityManager entityManager;");
        fields.add(String.format("@Autowired%nprivate %s %s;",
                classInfo.getSimpleName(), toLowerCamelCase(classInfo.getSimpleName())));

        return fields;
    }

    @Override
    protected List<String> generateSetupMethods(ClassInfo classInfo) {
        List<String> methods = new ArrayList<>();

        methods.add("""
            @BeforeEach
            void setUp() {
                // Clean database before each test
            }
            """);

        return methods;
    }

    private String generateRepositoryCall(MethodInfo method, ClassInfo classInfo) {
        String instanceName = toLowerCamelCase(classInfo.getSimpleName());
        String params = generateParameters(method);

        if (method.isVoidReturn()) {
            return String.format("%s.%s(%s);", instanceName, method.getName(), params);
        }

        return String.format("%s result = %s.%s(%s);",
                method.getSimpleReturnType(), instanceName, method.getName(), params);
    }

    private String generateParameters(MethodInfo method) {
        if (method.getParameters().isEmpty()) {
            return "";
        }

        List<String> params = new ArrayList<>();
        for (int i = 0; i < method.getParameters().size(); i++) {
            params.add("testParam" + i);
        }
        return String.join(", ", params);
    }
}