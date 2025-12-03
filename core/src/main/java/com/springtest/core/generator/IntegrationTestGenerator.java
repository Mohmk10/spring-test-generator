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
        String testMethodName = "test" + capitalize(method.getName()) + "Integration";
        String displayName = "Integration test for " + method.getName();

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("// Setup integration test data");

        String whenStatement = generateIntegrationCall(method, classInfo);

        List<String> thenStatements = new ArrayList<>();
        thenStatements.add("// Verify end-to-end behavior");

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
                .expectsException(false)
                .priority(10)
                .build();
    }

    @Override
    protected List<TestCase> generateEdgeCaseTests(MethodInfo method, ClassInfo classInfo) {
        return List.of();
    }

    @Override
    protected List<TestCase> generateExceptionTests(MethodInfo method, ClassInfo classInfo) {
        return List.of();
    }

    @Override
    protected List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.DisplayName;");
        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.springframework.boot.test.context.SpringBootTest;");
        imports.add("import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;");
        imports.add("import org.springframework.test.web.servlet.MockMvc;");
        imports.add("import static org.assertj.core.api.Assertions.*;");

        imports.add(String.format("import %s;", classInfo.getFullyQualifiedName()));

        return imports;
    }

    @Override
    protected List<String> generateClassAnnotations(ClassInfo classInfo) {
        List<String> annotations = new ArrayList<>();
        annotations.add("@SpringBootTest");
        annotations.add("@AutoConfigureMockMvc");
        return annotations;
    }

    @Override
    protected List<String> generateTestFields(ClassInfo classInfo) {
        List<String> fields = new ArrayList<>();

        fields.add("@Autowired\nprivate MockMvc mockMvc;");
        fields.add(String.format("@Autowired%nprivate %s %s;",
                classInfo.getSimpleName(), toLowerCamelCase(classInfo.getSimpleName())));

        return fields;
    }

    @Override
    protected List<String> generateSetupMethods(ClassInfo classInfo) {
        return List.of();
    }

    private String generateIntegrationCall(MethodInfo method, ClassInfo classInfo) {
        String instanceName = toLowerCamelCase(classInfo.getSimpleName());
        return String.format("%s.%s();", instanceName, method.getName());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}