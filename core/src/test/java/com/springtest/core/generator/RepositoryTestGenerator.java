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

        String entityName = extractEntityName(classInfo);

        List<String> givenStatements = List.of(
                "// Persist test entity",
                entityName + " entity = entityManager.persistAndFlush(createTestEntity());"
        );

        String whenStatement = generateRepositoryCall(method, entityName);

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
                .verifyStatements(List.of())
                .expectsException(false)
                .priority(10)
                .build();
    }

    @Override
    protected List<TestCase> generateEdgeCaseTests(MethodInfo method, ClassInfo classInfo) {
        List<TestCase> testCases = new ArrayList<>();

        if (method.getName().startsWith("find") || method.getName().startsWith("exists")) {
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

        String entityName = extractEntityName(classInfo);
        String whenStatement = generateRepositoryCall(method, entityName);

        List<String> thenStatements = assertionGenerator.generateAssertions(method, "not_found");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("not_found")
                .description("Test entity not found")
                .givenStatements(List.of())
                .mockSetups(List.of())
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .verifyStatements(List.of())
                .expectsException(false)
                .priority(8)
                .build();
    }

    @Override
    protected List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();
        imports.add("import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;");
        imports.add("import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;");
        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.DisplayName;");
        imports.add("import org.junit.jupiter.api.BeforeEach;");
        imports.add("import static org.assertj.core.api.Assertions.*;");
        imports.add("import java.util.Optional;");
        imports.add("import java.util.List;");
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
        fields.add(String.format("@Autowired\nprivate %s repository;", classInfo.getSimpleName()));
        fields.add("@Autowired\nprivate TestEntityManager entityManager;");
        return fields;
    }

    @Override
    protected List<String> generateSetupMethods(ClassInfo classInfo) {
        return List.of("@BeforeEach\nvoid setUp() {\n    repository.deleteAll();\n}");
    }

    private String extractEntityName(ClassInfo classInfo) {
        String name = classInfo.getSimpleName();
        return name.replace("Repository", "");
    }

    private String generateRepositoryCall(MethodInfo method, String entityName) {
        String repoName = toLowerCamelCase(entityName + "Repository");
        String params = method.getParameters().isEmpty() ? "" : "testParam0";

        String returnType = method.getSimpleReturnType();
        if (returnType.equals("void")) {
            return String.format("%s.%s(%s);", repoName, method.getName(), params);
        }

        return String.format("%s result = %s.%s(%s);", returnType, repoName, method.getName(), params);
    }
}