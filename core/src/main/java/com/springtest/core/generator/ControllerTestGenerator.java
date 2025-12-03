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
        givenStatements.add(generateRequestData(method));

        List<String> mockSetups = stubGenerator.generateStubs(method, "happy_path");

        String whenStatement = generateMockMvcCall(method);

        List<String> thenStatements = generateMockMvcAssertions(method, "200");

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
                .expectsException(false)
                .priority(10)
                .build();
    }

    @Override
    protected List<TestCase> generateEdgeCaseTests(MethodInfo method, ClassInfo classInfo) {
        List<TestCase> testCases = new ArrayList<>();

        testCases.add(generateNotFoundTest(method, classInfo));

        if (method.needsValidationTests()) {
            testCases.add(generateValidationTest(method, classInfo));
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
            givenStatements.add("// Exception scenario");

            List<String> mockSetups = stubGenerator.generateStubs(method, "exception");

            String whenStatement = generateMockMvcCall(method);

            List<String> thenStatements = generateMockMvcAssertions(method, "500");

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
                    .expectsException(false)
                    .priority(8)
                    .build());
        });

        return testCases;
    }

    private TestCase generateNotFoundTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "not_found");
        String displayName = namingStrategy.generateDisplayName(method, "not_found");

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("Long nonExistentId = 999L;");

        List<String> mockSetups = stubGenerator.generateStubs(method, "not_found");

        String whenStatement = generateMockMvcCall(method);

        List<String> thenStatements = generateMockMvcAssertions(method, "404");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("not_found")
                .description("Test 404 not found response")
                .givenStatements(givenStatements)
                .mockSetups(mockSetups)
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .expectsException(false)
                .priority(9)
                .build();
    }

    private TestCase generateValidationTest(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = namingStrategy.generateTestMethodName(method, "validation");
        String displayName = "Test validation failure";

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("// Invalid request data");

        String whenStatement = generateMockMvcCall(method);

        List<String> thenStatements = generateMockMvcAssertions(method, "400");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("validation")
                .description("Test validation error response")
                .givenStatements(givenStatements)
                .mockSetups(List.of())
                .whenStatement(whenStatement)
                .thenStatements(thenStatements)
                .expectsException(false)
                .priority(7)
                .build();
    }

    @Override
    protected List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.DisplayName;");
        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;");
        imports.add("import org.springframework.boot.test.mock.mockito.MockBean;");
        imports.add("import org.springframework.http.MediaType;");
        imports.add("import org.springframework.test.web.servlet.MockMvc;");
        imports.add("import com.fasterxml.jackson.databind.ObjectMapper;");
        imports.add("import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;");
        imports.add("import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;");
        imports.add("import static org.mockito.Mockito.*;");

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
            fields.add(String.format("@MockBean%nprivate %s %s;",
                    field.getSimpleType(), field.getName()));
        }

        return fields;
    }

    @Override
    protected List<String> generateSetupMethods(ClassInfo classInfo) {
        return List.of();
    }

    private String generateRequestData(MethodInfo method) {
        return "// Request data setup";
    }

    private String generateMockMvcCall(MethodInfo method) {
        String httpMethod = detectHttpMethod(method);
        String endpoint = detectEndpoint(method);

        return String.format("mockMvc.perform(%s(\"%s\")\n    .contentType(MediaType.APPLICATION_JSON))",
                httpMethod, endpoint);
    }

    private List<String> generateMockMvcAssertions(MethodInfo method, String expectedStatus) {
        List<String> assertions = new ArrayList<>();

        assertions.add(String.format(".andExpect(status().is(%s))", expectedStatus));

        if (!"404".equals(expectedStatus) && !"400".equals(expectedStatus) && !method.isVoidReturn()) {
            assertions.add(".andExpect(jsonPath(\"$.id\").exists())");
        }

        return assertions;
    }

    private String detectHttpMethod(MethodInfo method) {
        if (method.hasAnnotation("GetMapping")) return "get";
        if (method.hasAnnotation("PostMapping")) return "post";
        if (method.hasAnnotation("PutMapping")) return "put";
        if (method.hasAnnotation("DeleteMapping")) return "delete";
        if (method.hasAnnotation("PatchMapping")) return "patch";
        return "get";
    }

    private String detectEndpoint(MethodInfo method) {
        return "/api/resource";
    }
}