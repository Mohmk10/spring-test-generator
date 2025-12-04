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

public class IntegrationTestGenerator extends TestGenerator {

    public IntegrationTestGenerator(GeneratorConfig config, NamingStrategy namingStrategy,
                                    MockGenerator mockGenerator, StubGenerator stubGenerator,
                                    VerifyGenerator verifyGenerator, AssertionGenerator assertionGenerator) {
        super(config, namingStrategy, mockGenerator, stubGenerator, verifyGenerator, assertionGenerator);
    }

    @Override
    protected TestCase generateHappyPathTest(MethodInfo method, ClassInfo classInfo) {
        String entityName = extractEntityName(classInfo);
        String testMethodName = "test" + method.getName().substring(0, 1).toUpperCase() + method.getName().substring(1) + "Integration";
        String displayName = "Integration test for " + method.getName();

        List<String> givenStatements = List.of("// Setup integration test data");
        String whenStatement = generateIntegrationCall(method, classInfo);
        List<String> thenStatements = List.of("// Verify end-to-end behavior");

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("integration")
                .description("Full integration test")
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
        testCases.add(generateDummyEdgeCase(method, classInfo));
        return testCases;
    }

    @Override
    protected List<TestCase> generateExceptionTests(MethodInfo method, ClassInfo classInfo) {
        return List.of();
    }

    private TestCase generateDummyEdgeCase(MethodInfo method, ClassInfo classInfo) {
        String testMethodName = "testEdgeCase" + method.getName();
        String displayName = "Edge case test for " + method.getName();

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("edge_case")
                .description("Edge case integration test")
                .givenStatements(List.of())
                .mockSetups(List.of())
                .whenStatement("// Edge case scenario")
                .thenStatements(List.of())
                .verifyStatements(List.of())
                .expectsException(false)
                .priority(8)
                .build();
    }

    @Override
    protected List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();
        imports.add("import org.springframework.boot.test.context.SpringBootTest;");
        imports.add("import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;");
        imports.add("import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;");
        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.springframework.test.web.servlet.MockMvc;");
        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.DisplayName;");
        imports.add("import org.junit.jupiter.api.BeforeEach;");
        imports.add("import static org.assertj.core.api.Assertions.*;");
        imports.add(String.format("import %s;", classInfo.getFullyQualifiedName()));
        return imports;
    }

    @Override
    protected List<String> generateClassAnnotations(ClassInfo classInfo) {
        return List.of(
                "@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)",
                "@AutoConfigureMockMvc"
        );
    }

    @Override
    protected List<String> generateTestFields(ClassInfo classInfo) {
        List<String> fields = new ArrayList<>();
        fields.add("@Autowired\nprivate MockMvc mockMvc;");
        fields.add(String.format("@Autowired\nprivate %s %s;",
                classInfo.getSimpleName(),
                toLowerCamelCase(classInfo.getSimpleName())));
        return fields;
    }

    @Override
    protected List<String> generateSetupMethods(ClassInfo classInfo) {
        return List.of("@BeforeEach\nvoid setUp() {\n    // Setup integration test environment\n}");
    }

    private String extractEntityName(ClassInfo classInfo) {
        return classInfo.getSimpleName().replace("Controller", "");
    }

    private String generateIntegrationCall(MethodInfo method, ClassInfo classInfo) {
        String instanceName = toLowerCamelCase(classInfo.getSimpleName());
        return String.format("%s.%s();", instanceName, method.getName());
    }
}