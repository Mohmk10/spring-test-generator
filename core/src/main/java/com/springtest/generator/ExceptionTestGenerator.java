package com.springtest.generator;

import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates exception test methods for methods that throw exceptions.
 * Creates tests using assertThatThrownBy() for each declared and possible exception.
 */
public class ExceptionTestGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionTestGenerator.class);

    /**
     * Generates all exception test methods for a given method.
     *
     * @param methodInfo   Information about the method
     * @param instanceName Name of the instance to call the method on
     * @return List of test method strings
     */
    public List<String> generateExceptionTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating exception tests for method: {}", methodInfo.name());

        List<String> testMethods = new ArrayList<>();

        // Generate tests for declared exceptions (throws clause)
        for (String exception : methodInfo.thrownExceptions()) {
            testMethods.add(generateExceptionTest(methodInfo, instanceName, exception, true));
        }

        // Generate tests for possible exceptions (inferred from method body)
        for (String exception : methodInfo.possibleExceptions()) {
            // Don't duplicate if already in thrownExceptions
            if (!methodInfo.thrownExceptions().contains(exception)) {
                testMethods.add(generateExceptionTest(methodInfo, instanceName, exception, false));
            }
        }

        return testMethods;
    }

    /**
     * Generates a single exception test method.
     *
     * @param methodInfo   Method information
     * @param instanceName Instance name
     * @param exceptionType Exception type to test
     * @param isDeclared   Whether the exception is declared in throws clause
     * @return Test method as a string
     */
    public String generateExceptionTest(MethodInfo methodInfo, String instanceName, String exceptionType, boolean isDeclared) {
        logger.debug("Generating exception test for {} throwing {}", methodInfo.name(), exceptionType);

        StringBuilder test = new StringBuilder();

        String simpleName = getSimpleName(exceptionType);
        String testName = String.format("test%s_Throws%s",
                capitalize(methodInfo.name()),
                simpleName);

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("        // Arrange - setup to trigger ").append(simpleName).append("\n");
        test.append(generateArrangeForException(methodInfo, exceptionType));
        test.append("\n");

        test.append("        // Act & Assert\n");
        test.append("        assertThatThrownBy(() -> ");
        test.append(generateMethodCall(instanceName, methodInfo));
        test.append(")\n");
        test.append("            .isInstanceOf(").append(exceptionType).append(".class)");

        // Add message assertion
        test.append("\n");
        test.append("            .hasMessageContaining(\"").append(getExpectedMessage(exceptionType)).append("\")");

        // Add cause assertion if relevant
        if (hasCommonCause(exceptionType)) {
            test.append("\n");
            test.append("            .hasCauseInstanceOf(").append(getCommonCause(exceptionType)).append(".class)");
        }

        test.append(";\n");
        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates a test that verifies exception message.
     *
     * @param methodInfo    Method information
     * @param instanceName  Instance name
     * @param exceptionType Exception type
     * @return Test method as a string
     */
    public String generateExceptionMessageTest(MethodInfo methodInfo, String instanceName, String exceptionType) {
        StringBuilder test = new StringBuilder();

        String simpleName = getSimpleName(exceptionType);
        String testName = String.format("test%s_%sMessage",
                capitalize(methodInfo.name()),
                simpleName);

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("        // Arrange\n");
        test.append(generateArrangeForException(methodInfo, exceptionType));
        test.append("\n");

        test.append("        // Act & Assert\n");
        test.append("        assertThatThrownBy(() -> ");
        test.append(generateMethodCall(instanceName, methodInfo));
        test.append(")\n");
        test.append("            .isInstanceOf(").append(exceptionType).append(".class)\n");
        test.append("            .hasMessage(\"Expected error message\")\n");
        test.append("            .hasNoCause();\n");

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates a test that verifies exception cause.
     *
     * @param methodInfo    Method information
     * @param instanceName  Instance name
     * @param exceptionType Exception type
     * @param causeType     Expected cause type
     * @return Test method as a string
     */
    public String generateExceptionCauseTest(MethodInfo methodInfo, String instanceName, String exceptionType, String causeType) {
        StringBuilder test = new StringBuilder();

        String simpleName = getSimpleName(exceptionType);
        String causeSimpleName = getSimpleName(causeType);
        String testName = String.format("test%s_%sWithCause",
                capitalize(methodInfo.name()),
                simpleName);

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("        // Arrange - setup to trigger ").append(simpleName).append(" with cause\n");
        test.append(generateArrangeForException(methodInfo, exceptionType));
        test.append("\n");

        test.append("        // Act & Assert\n");
        test.append("        assertThatThrownBy(() -> ");
        test.append(generateMethodCall(instanceName, methodInfo));
        test.append(")\n");
        test.append("            .isInstanceOf(").append(exceptionType).append(".class)\n");
        test.append("            .hasCauseInstanceOf(").append(causeType).append(".class)\n");
        test.append("            .hasRootCauseInstanceOf(").append(causeType).append(".class)\n");
        test.append("            .hasMessageContaining(\"").append(getExpectedMessage(exceptionType)).append("\");\n");

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates a test for multiple exceptions.
     *
     * @param methodInfo   Method information
     * @param instanceName Instance name
     * @return Test method as a string
     */
    public String generateMultipleExceptionsTest(MethodInfo methodInfo, String instanceName) {
        if (methodInfo.thrownExceptions().isEmpty() && methodInfo.possibleExceptions().isEmpty()) {
            return "";
        }

        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_ThrowsAppropriateExceptions",
                capitalize(methodInfo.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("        // This method can throw multiple exceptions\n");
        test.append("        // Test with different scenarios to verify correct exception is thrown\n");
        test.append("\n");

        test.append("        // Scenario 1: Invalid input\n");
        test.append("        assertThatThrownBy(() -> ");
        test.append(generateMethodCall(instanceName, methodInfo));
        test.append(")\n");
        test.append("            .isInstanceOfAny(\n");

        List<String> allExceptions = new ArrayList<>();
        allExceptions.addAll(methodInfo.thrownExceptions());
        allExceptions.addAll(methodInfo.possibleExceptions());

        String exceptionClasses = allExceptions.stream()
                .distinct()
                .map(e -> e + ".class")
                .collect(Collectors.joining(",\n                "));

        test.append("                ").append(exceptionClasses).append("\n");
        test.append("            );\n");

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates arrange section for exception tests.
     */
    private String generateArrangeForException(MethodInfo methodInfo, String exceptionType) {
        StringBuilder arrange = new StringBuilder();

        // Add setup comments based on exception type
        String simpleName = getSimpleName(exceptionType);

        if (simpleName.contains("NotFound") || simpleName.contains("NoSuchElement")) {
            arrange.append("        // Setup: Use non-existent ID to trigger ").append(simpleName).append("\n");
        } else if (simpleName.contains("IllegalArgument") || simpleName.contains("Validation")) {
            arrange.append("        // Setup: Use invalid arguments to trigger ").append(simpleName).append("\n");
        } else if (simpleName.contains("IllegalState")) {
            arrange.append("        // Setup: Put object in invalid state to trigger ").append(simpleName).append("\n");
        } else if (simpleName.contains("NullPointer")) {
            arrange.append("        // Setup: Use null values to trigger ").append(simpleName).append("\n");
        } else {
            arrange.append("        // Setup: Configure scenario to trigger ").append(simpleName).append("\n");
        }

        return arrange.toString();
    }

    /**
     * Generates a method call with appropriate arguments.
     */
    private String generateMethodCall(String instanceName, MethodInfo methodInfo) {
        if (methodInfo.parameters().isEmpty()) {
            return instanceName + "." + methodInfo.name() + "()";
        }

        String args = methodInfo.parameters().stream()
                .map(this::getExceptionTriggeringValue)
                .collect(Collectors.joining(", "));

        return instanceName + "." + methodInfo.name() + "(" + args + ")";
    }

    /**
     * Gets a value that might trigger an exception.
     */
    private String getExceptionTriggeringValue(ParameterInfo param) {
        // Use values that are likely to cause exceptions
        if (param.required()) {
            return "null";
        }

        return switch (param.type()) {
            case "String" -> "\"invalid\"";
            case "int", "Integer" -> "-1";
            case "long", "Long" -> "-1L";
            case "double", "Double" -> "-1.0";
            case "float", "Float" -> "-1.0f";
            case "boolean", "Boolean" -> "false";
            case "List", "Set", "Collection" -> "List.of()";
            case "Map" -> "Map.of()";
            default -> "null";
        };
    }

    /**
     * Gets the simple name from a fully qualified exception name.
     */
    private String getSimpleName(String qualifiedName) {
        if (qualifiedName.contains(".")) {
            return qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
        }
        return qualifiedName;
    }

    /**
     * Gets an expected message for an exception type.
     */
    private String getExpectedMessage(String exceptionType) {
        String simpleName = getSimpleName(exceptionType);

        return switch (simpleName) {
            case "IllegalArgumentException" -> "Invalid argument";
            case "IllegalStateException" -> "Invalid state";
            case "NullPointerException" -> "null";
            case "EntityNotFoundException", "NoSuchElementException" -> "not found";
            case "ValidationException" -> "validation failed";
            case "AccessDeniedException" -> "Access denied";
            case "IOException" -> "I/O error";
            default -> "error";
        };
    }

    /**
     * Checks if an exception type commonly has a cause.
     */
    private boolean hasCommonCause(String exceptionType) {
        String simpleName = getSimpleName(exceptionType);
        return simpleName.contains("Wrapped") ||
               simpleName.contains("Runtime") ||
               simpleName.equals("DataAccessException") ||
               simpleName.equals("ServiceException");
    }

    /**
     * Gets a common cause type for an exception.
     */
    private String getCommonCause(String exceptionType) {
        String simpleName = getSimpleName(exceptionType);

        if (simpleName.contains("DataAccess")) {
            return "SQLException";
        } else if (simpleName.contains("Service")) {
            return "RuntimeException";
        }

        return "Exception";
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
     * Generates required imports for exception tests.
     *
     * @return List of import statements
     */
    public List<String> generateImports() {
        List<String> imports = new ArrayList<>();
        imports.add("import static org.assertj.core.api.Assertions.*;");
        imports.add("import java.util.List;");
        imports.add("import java.util.Map;");
        return imports;
    }

    /**
     * Checks if a method has any exceptions to test.
     *
     * @param methodInfo Method information
     * @return true if the method has exceptions
     */
    public boolean hasExceptionsToTest(MethodInfo methodInfo) {
        return !methodInfo.thrownExceptions().isEmpty() || !methodInfo.possibleExceptions().isEmpty();
    }
}
