package com.springtest.generator;

import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates edge case tests for methods.
 * Creates tests for null parameters, empty collections, boundary values, and invalid formats.
 */
public class EdgeCaseGenerator {
    private static final Logger logger = LoggerFactory.getLogger(EdgeCaseGenerator.class);

    /**
     * Generates all edge case test methods for a given method.
     *
     * @param methodInfo   Information about the method
     * @param instanceName Name of the instance to call the method on
     * @return List of test method strings
     */
    public List<String> generateEdgeCaseTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating edge case tests for method: {}", methodInfo.name());

        List<String> testMethods = new ArrayList<>();

        // Generate null parameter tests
        testMethods.addAll(generateNullParameterTests(methodInfo, instanceName));

        // Generate empty collection/string tests
        testMethods.addAll(generateEmptyValueTests(methodInfo, instanceName));

        // Generate boundary value tests
        testMethods.addAll(generateBoundaryValueTests(methodInfo, instanceName));

        // Generate invalid format tests
        testMethods.addAll(generateInvalidFormatTests(methodInfo, instanceName));

        return testMethods;
    }

    /**
     * Generates test methods for null parameters.
     *
     * @param methodInfo   Method information
     * @param instanceName Instance name
     * @return List of null parameter test methods
     */
    public List<String> generateNullParameterTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating null parameter tests for: {}", methodInfo.name());

        List<String> tests = new ArrayList<>();

        // Generate a test for each nullable parameter
        for (int i = 0; i < methodInfo.parameters().size(); i++) {
            ParameterInfo param = methodInfo.parameters().get(i);

            // Skip primitive types (they can't be null)
            if (param.isPrimitive()) {
                continue;
            }

            String testMethod = generateNullParameterTest(methodInfo, instanceName, i, param);
            tests.add(testMethod);
        }

        // If method has no parameters or only primitives, generate a simple test
        if (tests.isEmpty() && !methodInfo.parameters().isEmpty()) {
            // All parameters are primitives, no null tests needed
            return tests;
        }

        return tests;
    }

    /**
     * Generates a single null parameter test.
     */
    private String generateNullParameterTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_WithNull%s",
                capitalize(methodInfo.name()),
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("        // Arrange - null ").append(param.name()).append("\n");

        // Build arguments list with null at the specific index
        List<String> args = buildArgumentsWithNullAt(methodInfo.parameters(), paramIndex);

        test.append("\n");
        test.append("        // Act & Assert\n");

        if (param.required() || methodInfo.hasValidation()) {
            // Expect an exception for required/validated parameters
            test.append("        assertThatThrownBy(() -> ");
            test.append(instanceName).append(".").append(methodInfo.name());
            test.append("(").append(String.join(", ", args)).append("))\n");
            test.append("            .isInstanceOf(IllegalArgumentException.class);\n");
        } else {
            // Method should handle null gracefully
            test.append("        assertThatCode(() -> ");
            test.append(instanceName).append(".").append(methodInfo.name());
            test.append("(").append(String.join(", ", args)).append("))\n");
            test.append("            .doesNotThrowAnyException();\n");
        }

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates test methods for empty strings and collections.
     */
    public List<String> generateEmptyValueTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating empty value tests for: {}", methodInfo.name());

        List<String> tests = new ArrayList<>();

        for (int i = 0; i < methodInfo.parameters().size(); i++) {
            ParameterInfo param = methodInfo.parameters().get(i);

            if (isStringType(param.type())) {
                tests.add(generateEmptyStringTest(methodInfo, instanceName, i, param));
            } else if (isCollectionType(param.type())) {
                tests.add(generateEmptyCollectionTest(methodInfo, instanceName, i, param));
            }
        }

        return tests;
    }

    /**
     * Generates an empty string test.
     */
    private String generateEmptyStringTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_WithEmpty%s",
                capitalize(methodInfo.name()),
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("        // Arrange - empty ").append(param.name()).append("\n");

        List<String> args = buildArgumentsWithValueAt(methodInfo.parameters(), paramIndex, "\"\"");

        test.append("\n");
        test.append("        // Act & Assert\n");

        if (param.required() || methodInfo.hasValidation()) {
            test.append("        assertThatThrownBy(() -> ");
            test.append(instanceName).append(".").append(methodInfo.name());
            test.append("(").append(String.join(", ", args)).append("))\n");
            test.append("            .isInstanceOf(IllegalArgumentException.class);\n");
        } else {
            test.append("        assertThatCode(() -> ");
            test.append(instanceName).append(".").append(methodInfo.name());
            test.append("(").append(String.join(", ", args)).append("))\n");
            test.append("            .doesNotThrowAnyException();\n");
        }

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates an empty collection test.
     */
    private String generateEmptyCollectionTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_WithEmpty%s",
                capitalize(methodInfo.name()),
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("        // Arrange - empty ").append(param.name()).append("\n");

        String emptyValue = getEmptyCollectionValue(param.type());
        List<String> args = buildArgumentsWithValueAt(methodInfo.parameters(), paramIndex, emptyValue);

        test.append("\n");
        test.append("        // Act & Assert\n");
        test.append("        assertThatCode(() -> ");
        test.append(instanceName).append(".").append(methodInfo.name());
        test.append("(").append(String.join(", ", args)).append("))\n");
        test.append("            .doesNotThrowAnyException();\n");

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates test methods for boundary values.
     */
    public List<String> generateBoundaryValueTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating boundary value tests for: {}", methodInfo.name());

        List<String> tests = new ArrayList<>();

        for (int i = 0; i < methodInfo.parameters().size(); i++) {
            ParameterInfo param = methodInfo.parameters().get(i);

            if (isNumericType(param.type())) {
                tests.addAll(generateNumericBoundaryTests(methodInfo, instanceName, i, param));
            }
        }

        return tests;
    }

    /**
     * Generates numeric boundary tests (0, -1, MAX_VALUE, MIN_VALUE).
     */
    private List<String> generateNumericBoundaryTests(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        List<String> tests = new ArrayList<>();

        // Test with zero
        tests.add(generateBoundaryTest(methodInfo, instanceName, paramIndex, param, "Zero", "0"));

        // Test with negative value (if signed type)
        if (!param.type().equals("char")) {
            tests.add(generateBoundaryTest(methodInfo, instanceName, paramIndex, param, "Negative", "-1"));
        }

        // Test with MAX_VALUE
        String maxValue = getMaxValue(param.type());
        tests.add(generateBoundaryTest(methodInfo, instanceName, paramIndex, param, "MaxValue", maxValue));

        // Test with MIN_VALUE
        if (!param.type().equals("char")) {
            String minValue = getMinValue(param.type());
            tests.add(generateBoundaryTest(methodInfo, instanceName, paramIndex, param, "MinValue", minValue));
        }

        return tests;
    }

    /**
     * Generates a single boundary value test.
     */
    private String generateBoundaryTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param, String boundaryName, String value) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_With%s%s",
                capitalize(methodInfo.name()),
                boundaryName,
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("        // Arrange - ").append(boundaryName).append(" value for ").append(param.name()).append("\n");

        List<String> args = buildArgumentsWithValueAt(methodInfo.parameters(), paramIndex, value);

        test.append("\n");
        test.append("        // Act & Assert\n");
        test.append("        assertThatCode(() -> ");
        test.append(instanceName).append(".").append(methodInfo.name());
        test.append("(").append(String.join(", ", args)).append("))\n");
        test.append("            .doesNotThrowAnyException();\n");

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Generates test methods for invalid formats.
     */
    public List<String> generateInvalidFormatTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating invalid format tests for: {}", methodInfo.name());

        List<String> tests = new ArrayList<>();

        for (int i = 0; i < methodInfo.parameters().size(); i++) {
            ParameterInfo param = methodInfo.parameters().get(i);

            // Check for email, URL, date formats based on validation annotations
            if (param.hasValidation()) {
                tests.add(generateInvalidFormatTest(methodInfo, instanceName, i, param));
            }
        }

        return tests;
    }

    /**
     * Generates an invalid format test.
     */
    private String generateInvalidFormatTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_WithInvalid%s",
                capitalize(methodInfo.name()),
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("        // Arrange - invalid format for ").append(param.name()).append("\n");

        String invalidValue = getInvalidValue(param);
        List<String> args = buildArgumentsWithValueAt(methodInfo.parameters(), paramIndex, invalidValue);

        test.append("\n");
        test.append("        // Act & Assert\n");
        test.append("        assertThatThrownBy(() -> ");
        test.append(instanceName).append(".").append(methodInfo.name());
        test.append("(").append(String.join(", ", args)).append("))\n");
        test.append("            .isInstanceOf(Exception.class);\n");

        test.append("    }\n");

        return test.toString();
    }

    /**
     * Builds an arguments list with null at a specific index.
     */
    private List<String> buildArgumentsWithNullAt(List<ParameterInfo> parameters, int nullIndex) {
        List<String> args = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            if (i == nullIndex) {
                args.add("null");
            } else {
                args.add(getDefaultValue(parameters.get(i)));
            }
        }
        return args;
    }

    /**
     * Builds an arguments list with a specific value at a specific index.
     */
    private List<String> buildArgumentsWithValueAt(List<ParameterInfo> parameters, int index, String value) {
        List<String> args = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            if (i == index) {
                args.add(value);
            } else {
                args.add(getDefaultValue(parameters.get(i)));
            }
        }
        return args;
    }

    /**
     * Gets a default value for a parameter.
     */
    private String getDefaultValue(ParameterInfo param) {
        return switch (param.type()) {
            case "String" -> "\"test\"";
            case "int", "Integer" -> "1";
            case "long", "Long" -> "1L";
            case "double", "Double" -> "1.0";
            case "float", "Float" -> "1.0f";
            case "boolean", "Boolean" -> "true";
            case "char", "Character" -> "'a'";
            case "List" -> "List.of(\"item\")";
            case "Set" -> "Set.of(\"item\")";
            case "Map" -> "Map.of(\"key\", \"value\")";
            default -> "null";
        };
    }

    /**
     * Gets an empty collection value.
     */
    private String getEmptyCollectionValue(String type) {
        return switch (type) {
            case "List" -> "List.of()";
            case "Set" -> "Set.of()";
            case "Map" -> "Map.of()";
            case "Collection" -> "Collections.emptyList()";
            default -> "List.of()";
        };
    }

    /**
     * Gets an invalid value for a parameter based on its validation.
     */
    private String getInvalidValue(ParameterInfo param) {
        // Based on common validation annotations
        if (param.type().equals("String")) {
            return "\"invalid@@@format\"";
        }
        return "\"invalid\"";
    }

    /**
     * Gets the maximum value for a numeric type.
     */
    private String getMaxValue(String type) {
        return switch (type) {
            case "int", "Integer" -> "Integer.MAX_VALUE";
            case "long", "Long" -> "Long.MAX_VALUE";
            case "double", "Double" -> "Double.MAX_VALUE";
            case "float", "Float" -> "Float.MAX_VALUE";
            case "byte", "Byte" -> "Byte.MAX_VALUE";
            case "short", "Short" -> "Short.MAX_VALUE";
            case "char", "Character" -> "Character.MAX_VALUE";
            default -> "Integer.MAX_VALUE";
        };
    }

    /**
     * Gets the minimum value for a numeric type.
     */
    private String getMinValue(String type) {
        return switch (type) {
            case "int", "Integer" -> "Integer.MIN_VALUE";
            case "long", "Long" -> "Long.MIN_VALUE";
            case "double", "Double" -> "Double.MIN_VALUE";
            case "float", "Float" -> "Float.MIN_VALUE";
            case "byte", "Byte" -> "Byte.MIN_VALUE";
            case "short", "Short" -> "Short.MIN_VALUE";
            default -> "Integer.MIN_VALUE";
        };
    }

    /**
     * Checks if a type is a string type.
     */
    private boolean isStringType(String type) {
        return "String".equals(type);
    }

    /**
     * Checks if a type is a collection type.
     */
    private boolean isCollectionType(String type) {
        return type.equals("List") || type.equals("Set") || type.equals("Collection") || type.equals("Map");
    }

    /**
     * Checks if a type is numeric.
     */
    private boolean isNumericType(String type) {
        return type.equals("int") || type.equals("Integer") ||
               type.equals("long") || type.equals("Long") ||
               type.equals("double") || type.equals("Double") ||
               type.equals("float") || type.equals("Float") ||
               type.equals("byte") || type.equals("Byte") ||
               type.equals("short") || type.equals("Short");
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
     * Generates required imports for edge case tests.
     *
     * @return List of import statements
     */
    public List<String> generateImports() {
        List<String> imports = new ArrayList<>();
        imports.add("import static org.assertj.core.api.Assertions.*;");
        imports.add("import java.util.List;");
        imports.add("import java.util.Set;");
        imports.add("import java.util.Map;");
        imports.add("import java.util.Collections;");
        return imports;
    }
}
