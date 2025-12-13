package com.springtest.generator;

import com.springtest.assertion.AssertionGenerator;
import com.springtest.mock.MockGenerator;
import com.springtest.mock.StubGenerator;
import com.springtest.model.AnnotationInfo;
import com.springtest.model.ClassInfo;
import com.springtest.model.ClassType;
import com.springtest.model.FieldInfo;
import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates tests for Spring @Controller and @RestController classes using @WebMvcTest.
 * Creates tests with MockMvc for testing web endpoints.
 */
public class ControllerTestGenerator implements TestGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ControllerTestGenerator.class);

    private final MockGenerator mockGenerator;
    private final StubGenerator stubGenerator;
    private final AssertionGenerator assertionGenerator;

    /**
     * Creates a ControllerTestGenerator with default configuration.
     */
    public ControllerTestGenerator() {
        this.mockGenerator = new MockGenerator(true); // Use @MockBean for Spring context
        this.stubGenerator = new StubGenerator();
        this.assertionGenerator = new AssertionGenerator();
    }

    /**
     * Creates a ControllerTestGenerator with custom generators.
     *
     * @param mockGenerator      Mock generator instance
     * @param stubGenerator      Stub generator instance
     * @param assertionGenerator Assertion generator instance
     */
    public ControllerTestGenerator(MockGenerator mockGenerator, StubGenerator stubGenerator, AssertionGenerator assertionGenerator) {
        this.mockGenerator = mockGenerator;
        this.stubGenerator = stubGenerator;
        this.assertionGenerator = assertionGenerator;
    }

    @Override
    public String generateTest(ClassInfo classInfo) {
        if (classInfo == null) {
            throw new IllegalArgumentException("ClassInfo cannot be null");
        }
        if (!supports(classInfo)) {
            throw new IllegalArgumentException("ControllerTestGenerator does not support class type: " + classInfo.classType());
        }

        logger.info("Generating controller test for: {}", classInfo.simpleName());

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

        // MockMvc field
        testClass.append(generateMockMvcField());
        testClass.append("\n\n");

        // Mock fields
        testClass.append(generateMockFields(classInfo));
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
        return classInfo != null && classInfo.classType() == ClassType.CONTROLLER;
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

        // Spring Boot Test imports
        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;");
        imports.add("import org.springframework.boot.test.mock.mockito.MockBean;");
        imports.add("import org.springframework.test.web.servlet.MockMvc;");

        // MockMvc imports
        imports.add("import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;");
        imports.add("import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;");
        imports.add("import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;");

        // Mockito imports
        imports.add("import static org.mockito.Mockito.*;");
        imports.add("import static org.mockito.ArgumentMatchers.*;");

        // Media type for JSON
        imports.add("import org.springframework.http.MediaType;");

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
        return String.format("@WebMvcTest(%s.class)\nclass %sTest {",
                classInfo.simpleName(),
                classInfo.simpleName());
    }

    /**
     * Generates the MockMvc field.
     */
    private String generateMockMvcField() {
        return "    @Autowired\n    private MockMvc mockMvc;";
    }

    /**
     * Generates mock field declarations.
     */
    private String generateMockFields(ClassInfo classInfo) {
        StringBuilder fields = new StringBuilder();

        // Generate @MockBean fields for dependencies
        for (FieldInfo field : classInfo.getInjectedFields()) {
            fields.append("    @MockBean\n");
            fields.append("    private ").append(field.type()).append(" ").append(field.name()).append(";\n\n");
        }

        return fields.toString().trim();
    }

    /**
     * Generates all test methods for web mapping methods in the controller.
     */
    private String generateTestMethods(ClassInfo classInfo) {
        StringBuilder methods = new StringBuilder();

        List<MethodInfo> webMethods = classInfo.getWebMappingMethods();

        for (MethodInfo method : webMethods) {
            String httpMethod = getHttpMethod(method);
            String path = getRequestPath(classInfo, method);

            methods.append(generateWebTestMethod(classInfo, method, httpMethod, path));
            methods.append("\n");

            // Generate error test case
            methods.append(generateWebErrorTestMethod(classInfo, method, httpMethod, path));
            methods.append("\n");
        }

        // If no web mapping methods, generate a basic test
        if (webMethods.isEmpty()) {
            methods.append(generateBasicTest());
        }

        return methods.toString();
    }

    /**
     * Generates a test method for a web endpoint.
     */
    private String generateWebTestMethod(ClassInfo classInfo, MethodInfo methodInfo, String httpMethod, String path) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "test" + capitalize(methodInfo.name());
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() throws Exception {\n");

        // Arrange section
        test.append("        // Arrange\n");
        test.append(generateWebArrangeSection(classInfo, methodInfo));
        test.append("\n");

        // Act & Assert section
        test.append("        // Act & Assert\n");
        test.append(generateMockMvcPerform(httpMethod, path, methodInfo));

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates a test method for error scenarios.
     */
    private String generateWebErrorTestMethod(ClassInfo classInfo, MethodInfo methodInfo, String httpMethod, String path) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "test" + capitalize(methodInfo.name()) + "_ReturnsError";
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() throws Exception {\n");

        // Arrange section with error scenario
        test.append("        // Arrange - setup for error scenario\n");
        test.append("        // when(mockService.someMethod(any())).thenThrow(new RuntimeException(\"Error\"));\n");
        test.append("\n");

        // Act & Assert section expecting error
        test.append("        // Act & Assert\n");
        test.append(generateMockMvcPerformError(httpMethod, path, methodInfo));

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates the Arrange section for web tests.
     */
    private String generateWebArrangeSection(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder arrange = new StringBuilder();

        // Generate stubs for mock dependencies
        for (FieldInfo field : classInfo.getInjectedFields()) {
            String mockName = field.name();
            arrange.append("        // Setup mock for ").append(mockName).append("\n");

            if (!methodInfo.returnsVoid()) {
                String defaultValue = getDefaultValue(methodInfo.returnType());
                arrange.append("        when(").append(mockName).append(".someMethod(any())).thenReturn(").append(defaultValue).append(");\n");
            }
        }

        return arrange.toString();
    }

    /**
     * Generates MockMvc perform statement for successful scenario.
     */
    private String generateMockMvcPerform(String httpMethod, String path, MethodInfo methodInfo) {
        StringBuilder perform = new StringBuilder();

        perform.append("        mockMvc.perform(").append(httpMethod.toLowerCase()).append("(\"").append(path).append("\")\n");

        // Add content type for POST/PUT/PATCH
        if (httpMethod.equals("POST") || httpMethod.equals("PUT") || httpMethod.equals("PATCH")) {
            perform.append("                .contentType(MediaType.APPLICATION_JSON)\n");
            perform.append("                .content(\"{}\") // Add JSON payload\n");
        }

        perform.append("                .accept(MediaType.APPLICATION_JSON))\n");
        perform.append("            .andDo(print())\n");
        perform.append("            .andExpect(status().isOk())");

        // Add response body expectations
        if (!methodInfo.returnsVoid()) {
            perform.append("\n            .andExpect(content().contentType(MediaType.APPLICATION_JSON))");
            perform.append("\n            .andExpect(jsonPath(\"$\").exists())");
        }

        perform.append(";\n");

        return perform.toString();
    }

    /**
     * Generates MockMvc perform statement for error scenario.
     */
    private String generateMockMvcPerformError(String httpMethod, String path, MethodInfo methodInfo) {
        StringBuilder perform = new StringBuilder();

        perform.append("        mockMvc.perform(").append(httpMethod.toLowerCase()).append("(\"").append(path).append("\")\n");

        if (httpMethod.equals("POST") || httpMethod.equals("PUT") || httpMethod.equals("PATCH")) {
            perform.append("                .contentType(MediaType.APPLICATION_JSON)\n");
            perform.append("                .content(\"{}\") // Add invalid JSON payload\n");
        }

        perform.append("                .accept(MediaType.APPLICATION_JSON))\n");
        perform.append("            .andDo(print())\n");
        perform.append("            .andExpect(status().is4xxClientError());\n");

        return perform.toString();
    }

    /**
     * Generates a basic test when no web mapping methods exist.
     */
    private String generateBasicTest() {
        StringBuilder test = new StringBuilder();

        test.append("    @Test\n");
        test.append("    void testControllerInitialization() {\n");
        test.append("        // Verify that MockMvc is properly initialized\n");
        test.append("        // assertThat(mockMvc).isNotNull();\n");
        test.append("    }\n");

        return test.toString();
    }

    /**
     * Gets the HTTP method from method annotations.
     */
    private String getHttpMethod(MethodInfo methodInfo) {
        for (AnnotationInfo annotation : methodInfo.annotations()) {
            String name = annotation.name();
            if (name.equals("GetMapping")) return "GET";
            if (name.equals("PostMapping")) return "POST";
            if (name.equals("PutMapping")) return "PUT";
            if (name.equals("DeleteMapping")) return "DELETE";
            if (name.equals("PatchMapping")) return "PATCH";
            if (name.equals("RequestMapping")) {
                // Check method attribute in RequestMapping
                return "GET"; // Default
            }
        }
        return "GET"; // Default
    }

    /**
     * Gets the request path from annotations.
     */
    private String getRequestPath(ClassInfo classInfo, MethodInfo methodInfo) {
        String basePath = getBasePath(classInfo);
        String methodPath = getMethodPath(methodInfo);

        String fullPath = basePath + methodPath;
        if (fullPath.isEmpty()) {
            fullPath = "/";
        }

        return fullPath;
    }

    /**
     * Gets the base path from class-level @RequestMapping.
     */
    private String getBasePath(ClassInfo classInfo) {
        for (AnnotationInfo annotation : classInfo.annotations()) {
            if (annotation.name().equals("RequestMapping")) {
                // Extract value from annotation
                // For now, return empty string
                return "";
            }
        }
        return "";
    }

    /**
     * Gets the method path from method-level mapping annotation.
     */
    private String getMethodPath(MethodInfo methodInfo) {
        for (AnnotationInfo annotation : methodInfo.annotations()) {
            String name = annotation.name();
            if (name.endsWith("Mapping")) {
                // Extract value from annotation
                // For now, return method name as path
                return "/" + methodInfo.name();
            }
        }
        return "/" + methodInfo.name();
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
     * Gets a default value for a type.
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
            case "Optional" -> "Optional.of(\"test\")";
            default -> "null";
        };
    }
}
