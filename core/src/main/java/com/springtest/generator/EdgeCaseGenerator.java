package com.springtest.generator;

import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EdgeCaseGenerator {
    private static final Logger logger = LoggerFactory.getLogger(EdgeCaseGenerator.class);

    public List<String> generateEdgeCaseTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating edge case tests for method: {}", methodInfo.name());

        List<String> testMethods = new ArrayList<>();

        testMethods.addAll(generateNullParameterTests(methodInfo, instanceName));

        testMethods.addAll(generateEmptyValueTests(methodInfo, instanceName));

        testMethods.addAll(generateBoundaryValueTests(methodInfo, instanceName));

        testMethods.addAll(generateInvalidFormatTests(methodInfo, instanceName));

        return testMethods;
    }

    public List<String> generateNullParameterTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating null parameter tests for: {}", methodInfo.name());

        List<String> tests = new ArrayList<>();

        for (int i = 0; i < methodInfo.parameters().size(); i++) {
            ParameterInfo param = methodInfo.parameters().get(i);

            if (param.isPrimitive()) {
                continue;
            }

            String testMethod = generateNullParameterTest(methodInfo, instanceName, i, param);
            tests.add(testMethod);
        }

        if (tests.isEmpty() && !methodInfo.parameters().isEmpty()) {

            return tests;
        }

        return tests;
    }

    private String generateNullParameterTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_WithNull%s",
                capitalize(methodInfo.name()),
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");

        List<String> args = buildArgumentsWithNullAt(methodInfo.parameters(), paramIndex);

        test.append("\n");

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

    private String generateEmptyStringTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_WithEmpty%s",
                capitalize(methodInfo.name()),
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");

        List<String> args = buildArgumentsWithValueAt(methodInfo.parameters(), paramIndex, "\"\"");

        test.append("\n");

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

    private String generateEmptyCollectionTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_WithEmpty%s",
                capitalize(methodInfo.name()),
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");

        String emptyValue = getEmptyCollectionValue(param.type());
        List<String> args = buildArgumentsWithValueAt(methodInfo.parameters(), paramIndex, emptyValue);

        test.append("\n");
        test.append("        assertThatCode(() -> ");
        test.append(instanceName).append(".").append(methodInfo.name());
        test.append("(").append(String.join(", ", args)).append("))\n");
        test.append("            .doesNotThrowAnyException();\n");

        test.append("    }\n");

        return test.toString();
    }

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

    private List<String> generateNumericBoundaryTests(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        List<String> tests = new ArrayList<>();

        tests.add(generateBoundaryTest(methodInfo, instanceName, paramIndex, param, "Zero", "0"));

        if (!param.type().equals("char")) {
            tests.add(generateBoundaryTest(methodInfo, instanceName, paramIndex, param, "Negative", "-1"));
        }

        String maxValue = getMaxValue(param.type());
        tests.add(generateBoundaryTest(methodInfo, instanceName, paramIndex, param, "MaxValue", maxValue));

        if (!param.type().equals("char")) {
            String minValue = getMinValue(param.type());
            tests.add(generateBoundaryTest(methodInfo, instanceName, paramIndex, param, "MinValue", minValue));
        }

        return tests;
    }

    private String generateBoundaryTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param, String boundaryName, String value) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_With%s%s",
                capitalize(methodInfo.name()),
                boundaryName,
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");

        List<String> args = buildArgumentsWithValueAt(methodInfo.parameters(), paramIndex, value);

        test.append("\n");
        test.append("        assertThatCode(() -> ");
        test.append(instanceName).append(".").append(methodInfo.name());
        test.append("(").append(String.join(", ", args)).append("))\n");
        test.append("            .doesNotThrowAnyException();\n");

        test.append("    }\n");

        return test.toString();
    }

    public List<String> generateInvalidFormatTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating invalid format tests for: {}", methodInfo.name());

        List<String> tests = new ArrayList<>();

        for (int i = 0; i < methodInfo.parameters().size(); i++) {
            ParameterInfo param = methodInfo.parameters().get(i);

            if (param.hasValidation()) {
                tests.add(generateInvalidFormatTest(methodInfo, instanceName, i, param));
            }
        }

        return tests;
    }

    private String generateInvalidFormatTest(MethodInfo methodInfo, String instanceName, int paramIndex, ParameterInfo param) {
        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_WithInvalid%s",
                capitalize(methodInfo.name()),
                capitalize(param.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");

        String invalidValue = getInvalidValue(param);
        List<String> args = buildArgumentsWithValueAt(methodInfo.parameters(), paramIndex, invalidValue);

        test.append("\n");
        test.append("        assertThatThrownBy(() -> ");
        test.append(instanceName).append(".").append(methodInfo.name());
        test.append("(").append(String.join(", ", args)).append("))\n");
        test.append("            .isInstanceOf(Exception.class);\n");

        test.append("    }\n");

        return test.toString();
    }

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

    private String getEmptyCollectionValue(String type) {
        return switch (type) {
            case "List" -> "List.of()";
            case "Set" -> "Set.of()";
            case "Map" -> "Map.of()";
            case "Collection" -> "Collections.emptyList()";
            default -> "List.of()";
        };
    }

    private String getInvalidValue(ParameterInfo param) {

        if (param.type().equals("String")) {
            return "\"invalid@@@format\"";
        }
        return "\"invalid\"";
    }

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

    private boolean isStringType(String type) {
        return "String".equals(type);
    }

    private boolean isCollectionType(String type) {
        return type.equals("List") || type.equals("Set") || type.equals("Collection") || type.equals("Map");
    }

    private boolean isNumericType(String type) {
        return type.equals("int") || type.equals("Integer") ||
               type.equals("long") || type.equals("Long") ||
               type.equals("double") || type.equals("Double") ||
               type.equals("float") || type.equals("Float") ||
               type.equals("byte") || type.equals("Byte") ||
               type.equals("short") || type.equals("Short");
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

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
