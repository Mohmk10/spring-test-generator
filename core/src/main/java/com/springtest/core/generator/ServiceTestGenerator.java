package com.springtest.core.generator;

import com.springtest.core.assertion.AssertionGenerator;
import com.springtest.core.config.GeneratorConfig;
import com.springtest.core.mock.MockGenerator;
import com.springtest.core.mock.StubGenerator;
import com.springtest.core.mock.VerifyGenerator;
import com.springtest.core.model.ClassInfo;
import com.springtest.core.model.FieldInfo;
import com.springtest.core.model.MethodInfo;
import com.springtest.core.model.TestCase;
import com.springtest.core.naming.NamingStrategy;

import java.util.ArrayList;
import java.util.List;

public class ServiceTestGenerator extends TestGenerator {

    public ServiceTestGenerator(GeneratorConfig config, NamingStrategy namingStrategy,
                                MockGenerator mockGenerator, StubGenerator stubGenerator,
                                VerifyGenerator verifyGenerator, AssertionGenerator assertionGenerator) {
        super(config, namingStrategy, mockGenerator, stubGenerator, verifyGenerator, assertionGenerator);
    }

    @Override
    protected TestCase generateHappyPathTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "happy_path");
        String displayName = namingStrategy.generateDisplayName(method, "happy_path");

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add(generateTestData(method));

        List<String> mockSetups = stubGenerator.generateStubs(method, "happy_path");

        String whenStatement = generateMethodCall(method, classInfo);

        List<String> thenStatements = assertionGenerator.generateAssertions(method, "happy_path");

        List<String> verifyStatements = new ArrayList<>();
        for (FieldInfo field : classInfo.getMockableFields()) {
            verifyStatements.addAll(verifyGenerator.generateVerifications(field.getName(), method));
        }

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("happy_path")
                .description("Test successful execution with valid input")
                .givenStatements(givenStatements)
                .mockSetups(mockSetups)
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .verifyStatements(verifyStatements)
                .expectsException(false)
                .priority(10)
                .build();
    }

    @Override
    protected List<TestCase> generateEdgeCaseTests(MethodInfo method, ClassInfo classInfo) {
        List<TestCase> testCases = new ArrayList<>();

        if (method.needsNullChecks()) {
            testCases.add(generateNullCheckTest(method, classInfo));
        }

        if (method.isReturnsOptional()) {
            testCases.add(generateNotFoundTest(method, classInfo));
        }

        if (method.isReturnsCollection()) {
            testCases.add(generateEmptyCollectionTest(method, classInfo));
        }

        return testCases;
    }

    @Override
    protected List<TestCase> generateExceptionTests(MethodInfo method, ClassInfo classInfo) {
        List<TestCase> testCases = new ArrayList<>();

        method.getAllExceptions().forEach(exception -> {
            String testMethodName = namingStrategy.generateTestMethodName(method, "exception");
            String displayName = namingStrategy.generateDisplayName(method, "exception");

            List<String> givenStatements = new ArrayList<>();
            givenStatements.add("// Setup condition that triggers exception");

            List<String> mockSetups = stubGenerator.generateStubs(method, "exception");

            String whenStatement = generateMethodCall(method, classInfo);

            List<String> thenStatements = assertionGenerator.generateAssertions(method, "exception");

            testCases.add(TestCase.builder()
                    .testMethodName(testMethodName)
                    .displayName(displayName)
                    .targetMethod(method)
                    .scenario("exception")
                    .description("Test exception handling")
                    .givenStatements(givenStatements)
                    .mockSetups(mockSetups)
                    .whenStatement(whenStatement)
                    .thenStatements(thenStatements)
                    .expectedException(exception)
                    .expectsException(true)
                    .priority(8)
                    .build());
        });

        return testCases;
    }

    private TestCase generateNullCheckTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "null_check");
        String displayName = namingStrategy.generateDisplayName(method, "null_check");

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("// Null parameter");

        String whenStatement = generateMethodCallWithNull(method, classInfo);

        List<String> thenStatements = assertionGenerator.generateAssertions(method, "exception");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("null_check")
                .description("Test null parameter handling")
                .givenStatements(givenStatements)
                .mockSetups(List.of())
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .expectsException(true)
                .priority(7)
                .build();
    }

    private TestCase generateNotFoundTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "not_found");
        String displayName = namingStrategy.generateDisplayName(method, "not_found");

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("Long nonExistentId = 999L;");

        List<String> mockSetups = stubGenerator.generateStubs(method, "not_found");

        String whenStatement = generateMethodCall(method, classInfo);

        List<String> thenStatements = assertionGenerator.generateAssertions(method, "not_found");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("not_found")
                .description("Test entity not found scenario")
                .givenStatements(givenStatements)
                .mockSetups(mockSetups)
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .expectsException(false)
                .priority(9)
                .build();
    }

    private TestCase generateEmptyCollectionTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "empty");
        String displayName = namingStrategy.generateDisplayName(method, "empty");

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("// Empty collection scenario");

        List<String> mockSetups = stubGenerator.generateStubs(method, "empty");

        String whenStatement = generateMethodCall(method, classInfo);

        List<String> thenStatements = assertionGenerator.generateAssertions(method, "empty");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("empty")
                .description("Test empty collection handling")
                .givenStatements(givenStatements)
                .mockSetups(mockSetups)
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .expectsException(false)
                .priority(6)
                .build();
    }

    @Override
    protected List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.DisplayName;");
        imports.add("import org.junit.jupiter.api.extension.ExtendWith;");
        imports.add("import org.mockito.InjectMocks;");
        imports.add("import org.mockito.Mock;");
        imports.add("import org.mockito.junit.jupiter.MockitoExtension;");
        imports.add("import static org.assertj.core.api.Assertions.*;");
        imports.add("import static org.mockito.Mockito.*;");

        imports.add(String.format("import %s;", classInfo.getFullyQualifiedName()));

        for (FieldInfo field : classInfo.getMockableFields()) {
            imports.add(String.format("import %s;", field.getType()));
        }

        return imports;
    }

    @Override
    protected List<String> generateClassAnnotations(ClassInfo classInfo) {
        return List.of("@ExtendWith(MockitoExtension.class)");
    }

    @Override
    protected List<String> generateTestFields(ClassInfo classInfo) {
        List<String> fields = new ArrayList<>();

        fields.addAll(mockGenerator.generateMockFields(classInfo));
        fields.add(mockGenerator.generateTestedInstanceField(classInfo));

        return fields;
    }

    @Override
    protected List<String> generateSetupMethods(ClassInfo classInfo) {
        return List.of();
    }

    private String generateTestData(MethodInfo method) {
        return "// Test data setup";
    }

    private String generateMethodCall(MethodInfo method, ClassInfo classInfo) {
        String instanceName = toLowerCamelCase(classInfo.getSimpleName());
        String params = generateParameters(method);

        if (method.isVoidReturn()) {
            return String.format("%s.%s(%s);", instanceName, method.getName(), params);
        }

        return String.format("%s result = %s.%s(%s);",
                method.getSimpleReturnType(), instanceName, method.getName(), params);
    }

    private String generateMethodCallWithNull(MethodInfo method, ClassInfo classInfo) {
        String instanceName = toLowerCamelCase(classInfo.getSimpleName());
        return String.format("%s.%s(null);", instanceName, method.getName());
    }

    private String generateParameters(MethodInfo method) {
        if (method.getParameters().isEmpty()) {
            return "";
        }

        List<String> params = new ArrayList<>();
        for (int i = 0; i < method.getParameters().size(); i++) {
            params.add("param" + i);
        }
        return String.join(", ", params);
    }
}