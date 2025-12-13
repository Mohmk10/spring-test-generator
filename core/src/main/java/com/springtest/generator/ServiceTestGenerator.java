package com.springtest.generator;

import com.springtest.assertion.AssertionGenerator;
import com.springtest.mock.MockGenerator;
import com.springtest.mock.StubGenerator;
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
 * Generates unit tests for Spring @Service classes using Mockito.
 * Creates tests with @ExtendWith(MockitoExtension.class) and appropriate mocks.
 */
public class ServiceTestGenerator implements TestGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ServiceTestGenerator.class);

    private final MockGenerator mockGenerator;
    private final StubGenerator stubGenerator;
    private final AssertionGenerator assertionGenerator;

    /**
     * Creates a ServiceTestGenerator with default configuration.
     */
    public ServiceTestGenerator() {
        this.mockGenerator = new MockGenerator(false);
        this.stubGenerator = new StubGenerator();
        this.assertionGenerator = new AssertionGenerator();
    }

    /**
     * Creates a ServiceTestGenerator with custom generators.
     *
     * @param mockGenerator      Mock generator instance
     * @param stubGenerator      Stub generator instance
     * @param assertionGenerator Assertion generator instance
     */
    public ServiceTestGenerator(MockGenerator mockGenerator, StubGenerator stubGenerator, AssertionGenerator assertionGenerator) {
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
            throw new IllegalArgumentException("ServiceTestGenerator does not support class type: " + classInfo.classType());
        }

        logger.info("Generating service test for: {}", classInfo.simpleName());

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
        return classInfo != null && classInfo.classType() == ClassType.SERVICE;
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
        imports.add("import org.junit.jupiter.api.extension.ExtendWith;");

        // Mockito imports
        imports.add("import org.mockito.Mock;");
        imports.add("import org.mockito.InjectMocks;");
        imports.add("import org.mockito.junit.jupiter.MockitoExtension;");
        imports.add("import static org.mockito.Mockito.*;");
        imports.add("import static org.mockito.ArgumentMatchers.*;");

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
        return String.format("@ExtendWith(MockitoExtension.class)\nclass %sTest {",
                classInfo.simpleName());
    }

    /**
     * Generates mock field declarations.
     */
    private String generateMockFields(ClassInfo classInfo) {
        StringBuilder fields = new StringBuilder();

        // Generate @Mock fields for dependencies
        for (FieldInfo field : classInfo.getInjectedFields()) {
            fields.append("    ").append(mockGenerator.generateMockForField(field).indent(4).trim());
            fields.append("\n\n");
        }

        // Generate @InjectMocks field for class under test
        fields.append("    ").append(mockGenerator.generateInjectMocks(classInfo).indent(4).trim());

        return fields.toString();
    }

    /**
     * Generates all test methods for public methods in the class.
     */
    private String generateTestMethods(ClassInfo classInfo) {
        StringBuilder methods = new StringBuilder();

        List<MethodInfo> publicMethods = classInfo.methods().stream()
                .filter(m -> !m.isStatic())
                .filter(m -> !m.isGetter())
                .filter(m -> !m.isSetter())
                .toList();

        for (MethodInfo method : publicMethods) {
            methods.append(generateTestMethod(classInfo, method));
            methods.append("\n");

            // Generate exception test if method throws exceptions
            if (method.throwsExceptions()) {
                methods.append(generateExceptionTestMethod(classInfo, method));
                methods.append("\n");
            }
        }

        // If no public methods, generate a basic test
        if (publicMethods.isEmpty()) {
            methods.append(generateBasicTest(classInfo));
        }

        return methods.toString();
    }

    /**
     * Generates a single test method for a service method.
     */
    private String generateTestMethod(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "test" + capitalize(methodInfo.name());
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() {\n");

        // Arrange section
        test.append("        // Arrange\n");
        test.append(generateArrangeSection(classInfo, methodInfo));
        test.append("\n");

        // Act section
        test.append("        // Act\n");
        test.append(generateActSection(classInfo, methodInfo));
        test.append("\n");

        // Assert section
        test.append("        // Assert\n");
        test.append(generateAssertSection(methodInfo));

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates the Arrange section of a test method.
     */
    private String generateArrangeSection(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder arrange = new StringBuilder();

        // Generate stubs for mock dependencies
        for (FieldInfo field : classInfo.getInjectedFields()) {
            String mockName = field.name();
            // Find methods in the field's type that might be called
            // For simplicity, we'll generate a basic stub
            arrange.append("        // Setup mock for ").append(mockName).append("\n");
            arrange.append("        // when(").append(mockName).append(".someMethod(any())).thenReturn(someValue);\n");
        }

        return arrange.toString();
    }

    /**
     * Generates the Act section of a test method.
     */
    private String generateActSection(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder act = new StringBuilder();

        String instanceName = getInstanceName(classInfo);
        String methodCall = generateMethodCall(instanceName, methodInfo);

        if (!methodInfo.returnsVoid()) {
            String resultType = methodInfo.returnType();
            act.append("        ").append(resultType).append(" result = ").append(methodCall).append(";\n");
        } else {
            act.append("        ").append(methodCall).append(";\n");
        }

        return act.toString();
    }

    /**
     * Generates the Assert section of a test method.
     */
    private String generateAssertSection(MethodInfo methodInfo) {
        StringBuilder assertSection = new StringBuilder();

        if (!methodInfo.returnsVoid()) {
            String assertion = assertionGenerator.generateMethodAssertions(methodInfo);
            assertSection.append("        ").append(assertion.replace("\n", "\n        ")).append("\n");
        }

        // Add verify statements for mock interactions
        assertSection.append("        // Verify mock interactions\n");
        assertSection.append("        // verify(mockDependency).methodCall(any());\n");

        return assertSection.toString();
    }

    /**
     * Generates an exception test method.
     */
    private String generateExceptionTestMethod(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "test" + capitalize(methodInfo.name()) + "_ThrowsException";
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() {\n");

        test.append("        // Arrange\n");
        test.append("        // Setup mock to throw exception\n");
        test.append("        // when(mockDependency.someMethod(any())).thenThrow(new RuntimeException(\"Test exception\"));\n");
        test.append("\n");

        test.append("        // Act & Assert\n");
        String instanceName = getInstanceName(classInfo);
        String methodCall = generateMethodCall(instanceName, methodInfo);

        List<String> exceptions = methodInfo.thrownExceptions();
        String exceptionType = exceptions.isEmpty() ? "RuntimeException" : exceptions.get(0);

        test.append("        assertThatThrownBy(() -> ").append(methodCall).append(")\n");
        test.append("            .isInstanceOf(").append(exceptionType).append(".class);\n");

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates a basic test when no public methods exist.
     */
    private String generateBasicTest(ClassInfo classInfo) {
        StringBuilder test = new StringBuilder();

        test.append("    @Test\n");
        test.append("    void testServiceInitialization() {\n");
        test.append("        // Verify that the service is properly initialized\n");
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
