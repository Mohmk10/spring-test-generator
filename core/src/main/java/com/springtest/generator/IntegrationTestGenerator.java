package com.springtest.generator;

import com.springtest.assertion.AssertionGenerator;
import com.springtest.model.ClassInfo;
import com.springtest.model.ClassType;
import com.springtest.model.FieldInfo;
import com.springtest.model.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates integration tests using @SpringBootTest.
 * Creates end-to-end tests that load the full Spring application context.
 */
public class IntegrationTestGenerator implements TestGenerator {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestGenerator.class);

    private final AssertionGenerator assertionGenerator;
    private final boolean useTestContainers;
    private final boolean useRestTemplate;

    /**
     * Creates an IntegrationTestGenerator with default configuration.
     */
    public IntegrationTestGenerator() {
        this(true, false);
    }

    /**
     * Creates an IntegrationTestGenerator with specified configuration.
     *
     * @param useTestContainers If true, generates tests with TestContainers
     * @param useRestTemplate   If true, uses RestTemplate for HTTP testing
     */
    public IntegrationTestGenerator(boolean useTestContainers, boolean useRestTemplate) {
        this.assertionGenerator = new AssertionGenerator();
        this.useTestContainers = useTestContainers;
        this.useRestTemplate = useRestTemplate;
    }

    /**
     * Creates an IntegrationTestGenerator with custom assertion generator.
     *
     * @param assertionGenerator Assertion generator instance
     * @param useTestContainers  If true, generates tests with TestContainers
     * @param useRestTemplate    If true, uses RestTemplate for HTTP testing
     */
    public IntegrationTestGenerator(AssertionGenerator assertionGenerator, boolean useTestContainers, boolean useRestTemplate) {
        this.assertionGenerator = assertionGenerator;
        this.useTestContainers = useTestContainers;
        this.useRestTemplate = useRestTemplate;
    }

    @Override
    public String generateTest(ClassInfo classInfo) {
        if (classInfo == null) {
            throw new IllegalArgumentException("ClassInfo cannot be null");
        }

        logger.info("Generating integration test for: {}", classInfo.simpleName());

        StringBuilder testClass = new StringBuilder();

        // Package declaration
        testClass.append(generatePackageDeclaration(classInfo));
        testClass.append("\n\n");

        // Imports
        testClass.append(generateImports(classInfo));
        testClass.append("\n\n");

        // Class declaration
        testClass.append(generateClassDeclaration(classInfo));
        testClass.append("\n\n");

        // Fields
        testClass.append(generateFields(classInfo));
        testClass.append("\n\n");

        // Test methods
        testClass.append(generateTestMethods(classInfo));
        testClass.append("\n");

        // Close class
        testClass.append("}\n");

        return testClass.toString();
    }

    @Override
    public boolean supports(ClassInfo classInfo) {
        // Integration tests can be generated for any Spring component
        return classInfo != null && classInfo.isSpringComponent();
    }

    /**
     * Generates the package declaration.
     */
    private String generatePackageDeclaration(ClassInfo classInfo) {
        return "package " + classInfo.packageName() + ";";
    }

    /**
     * Generates all required imports.
     */
    private String generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        // JUnit imports
        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.BeforeEach;");

        // Spring Boot Test imports
        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.springframework.boot.test.context.SpringBootTest;");

        // RestTemplate or TestRestTemplate imports
        if (useRestTemplate || classInfo.classType() == ClassType.CONTROLLER) {
            imports.add("import org.springframework.boot.test.web.client.TestRestTemplate;");
            imports.add("import org.springframework.boot.test.web.server.LocalServerPort;");
            imports.add("import org.springframework.http.ResponseEntity;");
            imports.add("import org.springframework.http.HttpStatus;");
        }

        // TestContainers imports (if enabled)
        if (useTestContainers) {
            imports.add("import org.springframework.test.context.DynamicPropertyRegistry;");
            imports.add("import org.springframework.test.context.DynamicPropertySource;");
            imports.add("import org.testcontainers.containers.PostgreSQLContainer;");
            imports.add("import org.testcontainers.junit.jupiter.Container;");
            imports.add("import org.testcontainers.junit.jupiter.Testcontainers;");
        }

        // AssertJ imports
        imports.add("import static org.assertj.core.api.Assertions.*;");

        // Java util imports
        imports.add("import java.util.List;");
        imports.add("import java.util.Optional;");

        // Import the class under test
        imports.add("import " + classInfo.qualifiedName() + ";");

        // Import dependency types
        for (FieldInfo field : classInfo.getInjectedFields()) {
            if (field.qualifiedType() != null && !field.qualifiedType().startsWith("java.")) {
                imports.add("import " + field.qualifiedType() + ";");
            }
        }

        return imports.stream()
                .distinct()
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    /**
     * Generates the test class declaration.
     */
    private String generateClassDeclaration(ClassInfo classInfo) {
        StringBuilder declaration = new StringBuilder();

        // Determine webEnvironment based on class type
        String webEnvironment = classInfo.classType() == ClassType.CONTROLLER
                ? "SpringBootTest.WebEnvironment.RANDOM_PORT"
                : "SpringBootTest.WebEnvironment.NONE";

        declaration.append("@SpringBootTest(webEnvironment = ").append(webEnvironment).append(")\n");

        if (useTestContainers) {
            declaration.append("@Testcontainers\n");
        }

        declaration.append("class ").append(classInfo.simpleName()).append("IntegrationTest {\n");

        // Add TestContainer configuration
        if (useTestContainers) {
            declaration.append("\n");
            declaration.append(generateTestContainerConfig());
        }

        return declaration.toString();
    }

    /**
     * Generates TestContainer configuration.
     */
    private String generateTestContainerConfig() {
        StringBuilder config = new StringBuilder();

        config.append("    @Container\n");
        config.append("    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(\"postgres:15-alpine\")\n");
        config.append("        .withDatabaseName(\"testdb\")\n");
        config.append("        .withUsername(\"test\")\n");
        config.append("        .withPassword(\"test\");\n\n");

        config.append("    @DynamicPropertySource\n");
        config.append("    static void configureProperties(DynamicPropertyRegistry registry) {\n");
        config.append("        registry.add(\"spring.datasource.url\", postgres::getJdbcUrl);\n");
        config.append("        registry.add(\"spring.datasource.username\", postgres::getUsername);\n");
        config.append("        registry.add(\"spring.datasource.password\", postgres::getPassword);\n");
        config.append("    }\n");

        return config.toString();
    }

    /**
     * Generates field declarations.
     */
    private String generateFields(ClassInfo classInfo) {
        StringBuilder fields = new StringBuilder();

        // Add @Autowired field for the class under test
        fields.append("    @Autowired\n");
        fields.append("    private ").append(classInfo.simpleName()).append(" ").append(getInstanceName(classInfo)).append(";\n");

        // Add RestTemplate for controllers
        if (useRestTemplate || classInfo.classType() == ClassType.CONTROLLER) {
            fields.append("\n");
            fields.append("    @Autowired\n");
            fields.append("    private TestRestTemplate restTemplate;\n");
            fields.append("\n");
            fields.append("    @LocalServerPort\n");
            fields.append("    private int port;\n");
        }

        return fields.toString();
    }

    /**
     * Generates all test methods.
     */
    private String generateTestMethods(ClassInfo classInfo) {
        StringBuilder methods = new StringBuilder();

        if (classInfo.classType() == ClassType.CONTROLLER) {
            methods.append(generateControllerIntegrationTests(classInfo));
        } else if (classInfo.classType() == ClassType.SERVICE) {
            methods.append(generateServiceIntegrationTests(classInfo));
        } else if (classInfo.classType() == ClassType.REPOSITORY) {
            methods.append(generateRepositoryIntegrationTests(classInfo));
        } else {
            methods.append(generateGenericIntegrationTest(classInfo));
        }

        return methods.toString();
    }

    /**
     * Generates integration tests for controllers.
     */
    private String generateControllerIntegrationTests(ClassInfo classInfo) {
        StringBuilder tests = new StringBuilder();

        List<MethodInfo> webMethods = classInfo.getWebMappingMethods();

        for (MethodInfo method : webMethods) {
            tests.append(generateRestTemplateTest(classInfo, method));
            tests.append("\n");
        }

        // If no web methods, generate basic test
        if (webMethods.isEmpty()) {
            tests.append(generateGenericIntegrationTest(classInfo));
        }

        return tests.toString();
    }

    /**
     * Generates a REST template test for a web endpoint.
     */
    private String generateRestTemplateTest(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "testIntegration_" + capitalize(methodInfo.name());
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() {\n");

        test.append("        // Arrange\n");
        test.append("        String url = \"http://localhost:\" + port + \"/").append(methodInfo.name()).append("\";\n");
        test.append("\n");

        test.append("        // Act\n");
        String responseType = methodInfo.returnsVoid() ? "Void" : methodInfo.returnType();
        test.append("        ResponseEntity<").append(responseType).append("> response = restTemplate.getForEntity(url, ").append(responseType).append(".class);\n");
        test.append("\n");

        test.append("        // Assert\n");
        test.append("        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);\n");
        if (!methodInfo.returnsVoid()) {
            test.append("        assertThat(response.getBody()).isNotNull();\n");
        }

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates integration tests for services.
     */
    private String generateServiceIntegrationTests(ClassInfo classInfo) {
        StringBuilder tests = new StringBuilder();

        List<MethodInfo> publicMethods = classInfo.methods().stream()
                .filter(m -> !m.isStatic())
                .filter(m -> !m.isGetter())
                .filter(m -> !m.isSetter())
                .limit(3) // Generate for first 3 methods
                .toList();

        for (MethodInfo method : publicMethods) {
            tests.append(generateServiceIntegrationTest(classInfo, method));
            tests.append("\n");
        }

        if (publicMethods.isEmpty()) {
            tests.append(generateGenericIntegrationTest(classInfo));
        }

        return tests.toString();
    }

    /**
     * Generates an integration test for a service method.
     */
    private String generateServiceIntegrationTest(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "testIntegration_" + capitalize(methodInfo.name());
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() {\n");

        test.append("        // Arrange\n");
        test.append("        // Setup test data\n");
        test.append("\n");

        test.append("        // Act\n");
        String instanceName = getInstanceName(classInfo);
        String methodCall = generateMethodCall(instanceName, methodInfo);

        if (!methodInfo.returnsVoid()) {
            test.append("        ").append(methodInfo.returnType()).append(" result = ").append(methodCall).append(";\n");
        } else {
            test.append("        ").append(methodCall).append(";\n");
        }
        test.append("\n");

        test.append("        // Assert\n");
        if (!methodInfo.returnsVoid()) {
            test.append("        ").append(assertionGenerator.generateMethodAssertions(methodInfo).replace("\n", "\n        ")).append("\n");
        } else {
            test.append("        // Verify expected behavior\n");
        }

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates integration tests for repositories.
     */
    private String generateRepositoryIntegrationTests(ClassInfo classInfo) {
        StringBuilder tests = new StringBuilder();

        tests.append("    @Test\n");
        tests.append("    void testIntegration_SaveAndFind() {\n");
        tests.append("        // This is a basic integration test\n");
        tests.append("        // Add specific repository operations\n");
        String instanceName = getInstanceName(classInfo);
        tests.append("        assertThat(").append(instanceName).append(").isNotNull();\n");
        tests.append("    }\n");

        return tests.toString();
    }

    /**
     * Generates a generic integration test.
     */
    private String generateGenericIntegrationTest(ClassInfo classInfo) {
        StringBuilder test = new StringBuilder();

        test.append("    @Test\n");
        test.append("    void testIntegrationContextLoads() {\n");
        test.append("        // Verify that the Spring context loads successfully\n");
        String instanceName = getInstanceName(classInfo);
        test.append("        assertThat(").append(instanceName).append(").isNotNull();\n");
        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates a method call string.
     */
    private String generateMethodCall(String instanceName, MethodInfo methodInfo) {
        if (methodInfo.parameters().isEmpty()) {
            return instanceName + "." + methodInfo.name() + "()";
        }

        String args = methodInfo.parameters().stream()
                .map(p -> getDefaultValue(p.type()))
                .collect(Collectors.joining(", "));

        return instanceName + "." + methodInfo.name() + "(" + args + ")";
    }

    /**
     * Gets the instance name for the class under test.
     */
    private String getInstanceName(ClassInfo classInfo) {
        String simpleName = classInfo.simpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * Capitalizes the first letter of a string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Gets a default value for a parameter type.
     */
    private String getDefaultValue(String type) {
        return switch (type) {
            case "String" -> "\"test\"";
            case "int", "Integer" -> "1";
            case "long", "Long" -> "1L";
            case "double", "Double" -> "1.0";
            case "float", "Float" -> "1.0f";
            case "boolean", "Boolean" -> "true";
            case "List" -> "List.of()";
            case "Set" -> "Set.of()";
            case "Map" -> "Map.of()";
            default -> "null";
        };
    }
}
