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

public class IntegrationTestGenerator implements TestGenerator {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestGenerator.class);

    private final AssertionGenerator assertionGenerator;
    private final boolean useTestContainers;
    private final boolean useRestTemplate;

    public IntegrationTestGenerator() {
        this(true, false);
    }

    public IntegrationTestGenerator(boolean useTestContainers, boolean useRestTemplate) {
        this.assertionGenerator = new AssertionGenerator();
        this.useTestContainers = useTestContainers;
        this.useRestTemplate = useRestTemplate;
    }

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

        testClass.append(generatePackageDeclaration(classInfo));
        testClass.append("\n\n");

        testClass.append(generateImports(classInfo));
        testClass.append("\n\n");

        testClass.append(generateClassDeclaration(classInfo));
        testClass.append("\n\n");

        testClass.append(generateFields(classInfo));
        testClass.append("\n\n");

        testClass.append(generateTestMethods(classInfo));
        testClass.append("\n");

        testClass.append("}\n");

        return testClass.toString();
    }

    @Override
    public boolean supports(ClassInfo classInfo) {

        return classInfo != null && classInfo.isSpringComponent();
    }

    private String generatePackageDeclaration(ClassInfo classInfo) {
        return "package " + classInfo.packageName() + ";";
    }

    private String generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.BeforeEach;");

        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.springframework.boot.test.context.SpringBootTest;");

        if (useRestTemplate || classInfo.classType() == ClassType.CONTROLLER) {
            imports.add("import org.springframework.boot.test.web.client.TestRestTemplate;");
            imports.add("import org.springframework.boot.test.web.server.LocalServerPort;");
            imports.add("import org.springframework.http.ResponseEntity;");
            imports.add("import org.springframework.http.HttpStatus;");
        }

        if (useTestContainers) {
            imports.add("import org.springframework.test.context.DynamicPropertyRegistry;");
            imports.add("import org.springframework.test.context.DynamicPropertySource;");
            imports.add("import org.testcontainers.containers.PostgreSQLContainer;");
            imports.add("import org.testcontainers.junit.jupiter.Container;");
            imports.add("import org.testcontainers.junit.jupiter.Testcontainers;");
        }

        imports.add("import static org.assertj.core.api.Assertions.*;");

        imports.add("import java.util.List;");
        imports.add("import java.util.Optional;");

        imports.add("import " + classInfo.qualifiedName() + ";");

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

    private String generateClassDeclaration(ClassInfo classInfo) {
        StringBuilder declaration = new StringBuilder();

        String webEnvironment = classInfo.classType() == ClassType.CONTROLLER
                ? "SpringBootTest.WebEnvironment.RANDOM_PORT"
                : "SpringBootTest.WebEnvironment.NONE";

        declaration.append("@SpringBootTest(webEnvironment = ").append(webEnvironment).append(")\n");

        if (useTestContainers) {
            declaration.append("@Testcontainers\n");
        }

        declaration.append("class ").append(classInfo.simpleName()).append("IntegrationTest {\n");

        if (useTestContainers) {
            declaration.append("\n");
            declaration.append(generateTestContainerConfig());
        }

        return declaration.toString();
    }

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

    private String generateFields(ClassInfo classInfo) {
        StringBuilder fields = new StringBuilder();

        fields.append("    @Autowired\n");
        fields.append("    private ").append(classInfo.simpleName()).append(" ").append(getInstanceName(classInfo)).append(";\n");

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

    private String generateControllerIntegrationTests(ClassInfo classInfo) {
        StringBuilder tests = new StringBuilder();

        List<MethodInfo> webMethods = classInfo.getWebMappingMethods();

        for (MethodInfo method : webMethods) {
            tests.append(generateRestTemplateTest(classInfo, method));
            tests.append("\n");
        }

        if (webMethods.isEmpty()) {
            tests.append(generateGenericIntegrationTest(classInfo));
        }

        return tests.toString();
    }

    private String generateRestTemplateTest(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "testIntegration_" + capitalize(methodInfo.name());
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() {\n");

        test.append("        String url = \"http://localhost:\" + port + \"/").append(methodInfo.name()).append("\";\n");
        test.append("\n");

        String responseType = methodInfo.returnsVoid() ? "Void" : methodInfo.returnType();
        test.append("        ResponseEntity<").append(responseType).append("> response = restTemplate.getForEntity(url, ").append(responseType).append(".class);\n");
        test.append("\n");

        test.append("        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);\n");
        if (!methodInfo.returnsVoid()) {
            test.append("        assertThat(response.getBody()).isNotNull();\n");
        }

        test.append("    }\n");

        return test.toString();
    }

    private String generateServiceIntegrationTests(ClassInfo classInfo) {
        StringBuilder tests = new StringBuilder();

        List<MethodInfo> publicMethods = classInfo.methods().stream()
                .filter(m -> !m.isStatic())
                .filter(m -> !m.isGetter())
                .filter(m -> !m.isSetter())
                .limit(3)
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

    private String generateServiceIntegrationTest(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "testIntegration_" + capitalize(methodInfo.name());
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() {\n");

        test.append("\n");

        String instanceName = getInstanceName(classInfo);
        String methodCall = generateMethodCall(instanceName, methodInfo);

        if (!methodInfo.returnsVoid()) {
            test.append("        ").append(methodInfo.returnType()).append(" result = ").append(methodCall).append(";\n");
        } else {
            test.append("        ").append(methodCall).append(";\n");
        }
        test.append("\n");

        if (!methodInfo.returnsVoid()) {
            test.append("        ").append(assertionGenerator.generateMethodAssertions(methodInfo).replace("\n", "\n        ")).append("\n");
        } else {
        }

        test.append("    }\n");

        return test.toString();
    }

    private String generateRepositoryIntegrationTests(ClassInfo classInfo) {
        StringBuilder tests = new StringBuilder();

        tests.append("    @Test\n");
        tests.append("    void testIntegration_SaveAndFind() {\n");
        String instanceName = getInstanceName(classInfo);
        tests.append("        assertThat(").append(instanceName).append(").isNotNull();\n");
        tests.append("    }\n");

        return tests.toString();
    }

    private String generateGenericIntegrationTest(ClassInfo classInfo) {
        StringBuilder test = new StringBuilder();

        test.append("    @Test\n");
        test.append("    void testIntegrationContextLoads() {\n");
        String instanceName = getInstanceName(classInfo);
        test.append("        assertThat(").append(instanceName).append(").isNotNull();\n");
        test.append("    }\n");

        return test.toString();
    }

    private String generateMethodCall(String instanceName, MethodInfo methodInfo) {
        if (methodInfo.parameters().isEmpty()) {
            return instanceName + "." + methodInfo.name() + "()";
        }

        String args = methodInfo.parameters().stream()
                .map(p -> getDefaultValue(p.type()))
                .collect(Collectors.joining(", "));

        return instanceName + "." + methodInfo.name() + "(" + args + ")";
    }

    private String getInstanceName(ClassInfo classInfo) {
        String simpleName = classInfo.simpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

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
