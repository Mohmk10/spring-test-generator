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

public class ControllerTestGenerator extends TestGenerator {

    public ControllerTestGenerator(GeneratorConfig config, NamingStrategy namingStrategy,
                                   MockGenerator mockGenerator, StubGenerator stubGenerator,
                                   VerifyGenerator verifyGenerator, AssertionGenerator assertionGenerator) {
        super(config, namingStrategy, mockGenerator, stubGenerator, verifyGenerator, assertionGenerator);
    }

    @Override
    protected TestCase generateHappyPathTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "happy_path");
        String displayName = namingStrategy.generateDisplayName(method, "happy_path");

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("// Setup request and response objects");

        List<String> mockSetups = stubGenerator.generateStubs(method, "happy_path");

        String whenStatement = generateMockMvcCall(method, classInfo);

        List<String> thenStatements = new ArrayList<>();
        thenStatements.add("// Verify HTTP status and response body");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("happy_path")
                .description("Test successful HTTP request")
                .givenStatements(givenStatements)
                .mockSetups(mockSetups)
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

        testCases.add(generateNotFoundTest(method, classInfo));
        testCases.add(generateInvalidInputTest(method, classInfo));

        return testCases;
    }

    @Override
    protected List<TestCase> generateExceptionTests(MethodInfo method, ClassInfo classInfo) {
        return List.of();
    }

    private TestCase generateNotFoundTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "not_found");
        String displayName = namingStrategy.generateDisplayName(method, "not_found");

        List<String> givenStatements = List.of("Long nonExistentId = 999L;");
        List<String> mockSetups = stubGenerator.generateStubs(method, "not_found");
        String whenStatement = "// Perform request with non-existent ID";
        List<String> thenStatements = List.of("// Verify 404 status");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("not_found")
                .description("Test 404 response")
                .givenStatements(givenStatements)
                .mockSetups(mockSetups)
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .verifyStatements(List.of())
                .expectsException(false)
                .priority(8)
                .build();
    }

    private TestCase generateInvalidInputTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "invalid_input");
        String displayName = namingStrategy.generateDisplayName(method, "invalid_input");

        List<String> givenStatements = List.of("// Invalid request body");
        String whenStatement = "// Perform request with invalid input";
        List<String> thenStatements = List.of("// Verify 400 status");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("invalid_input")
                .description("Test validation failure")
                .givenStatements(givenStatements)
                .mockSetups(List.of())
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .verifyStatements(List.of())
                .expectsException(false)
                .priority(7)
                .build();
    }

    @Override
    protected List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();
        imports.add("import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;");
        imports.add("import org.springframework.boot.test.mock.mockito.MockBean;");
        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.springframework.test.web.servlet.MockMvc;");
        imports.add("import org.springframework.http.MediaType;");
        imports.add("import com.fasterxml.jackson.databind.ObjectMapper;");
        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.DisplayName;");
        imports.add("import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;");
        imports.add("import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;");
        imports.add("import static org.mockito.Mockito.*;");
        imports.add("import static org.assertj.core.api.Assertions.*;");
        imports.add(String.format("import %s;", classInfo.getFullyQualifiedName()));

        for (FieldInfo field : classInfo.getMockableFields()) {
            imports.add(String.format("import %s;", field.getType()));
        }

        return imports;
    }

    @Override
    protected List<String> generateClassAnnotations(ClassInfo classInfo) {
        return List.of(String.format("@WebMvcTest(%s.class)", classInfo.getSimpleName()));
    }

    @Override
    protected List<String> generateTestFields(ClassInfo classInfo) {
        List<String> fields = new ArrayList<>();
        fields.add("@Autowired\nprivate MockMvc mockMvc;");
        fields.add("@Autowired\nprivate ObjectMapper objectMapper;");

        for (FieldInfo field : classInfo.getMockableFields()) {
            fields.add(String.format("@MockBean\nprivate %s %s;", field.getSimpleType(), field.getName()));
        }

        return fields;
    }

    @Override
    protected List<String> generateSetupMethods(ClassInfo classInfo) {
        return List.of();
    }

    private String generateMockMvcCall(MethodInfo method, ClassInfo classInfo) {
        String instanceName = toLowerCamelCase(classInfo.getSimpleName());
        return String.format("// mockMvc.perform(...).andExpect(status().isOk());");
    }
}