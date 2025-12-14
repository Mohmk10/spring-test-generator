package com.springtest.assertion;

import com.springtest.model.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AssertionGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AssertionGenerator.class);

    public String generateBasicAssertion(String actualValue, String returnType) {
        logger.debug("Generating basic assertion for type: {}", returnType);

        return switch (returnType) {
            case "void" -> "// No assertion needed for void method";
            case "String" -> String.format("assertThat(%s).isNotNull().isNotEmpty();", actualValue);
            case "int", "Integer", "long", "Long" ->
                    String.format("assertThat(%s).isNotNull().isPositive();", actualValue);
            case "boolean", "Boolean" ->
                    String.format("assertThat(%s).isNotNull();", actualValue);
            case "List" -> String.format("assertThat(%s).isNotNull().isNotEmpty();", actualValue);
            case "Set" -> String.format("assertThat(%s).isNotNull().isNotEmpty();", actualValue);
            case "Map" -> String.format("assertThat(%s).isNotNull().isNotEmpty();", actualValue);
            case "Optional" -> String.format("assertThat(%s).isPresent();", actualValue);
            default -> {
                if (returnType.startsWith("List<") || returnType.startsWith("Set<")) {
                    yield String.format("assertThat(%s).isNotNull().isNotEmpty();", actualValue);
                } else if (returnType.startsWith("Optional<")) {
                    yield String.format("assertThat(%s).isPresent();", actualValue);
                } else if (returnType.startsWith("Map<")) {
                    yield String.format("assertThat(%s).isNotNull().isNotEmpty();", actualValue);
                }
                yield String.format("assertThat(%s).isNotNull();", actualValue);
            }
        };
    }

    public String generateAssertionForMethod(MethodInfo methodInfo) {
        String resultVar = "result";
        return generateBasicAssertion(resultVar, methodInfo.returnType());
    }

    public String generateIsEqualTo(String actualValue, String expectedValue) {
        return String.format("assertThat(%s).isEqualTo(%s);", actualValue, expectedValue);
    }

    public String generateIsNotEqualTo(String actualValue, String expectedValue) {
        return String.format("assertThat(%s).isNotEqualTo(%s);", actualValue, expectedValue);
    }

    public String generateCollectionAssertions(String actualValue) {
        return String.format("assertThat(%s)\n" +
                "    .isNotNull()\n" +
                "    .isNotEmpty()\n" +
                "    .hasSize(1);", actualValue);
    }

    public String generateStringAssertions(String actualValue) {
        return String.format("assertThat(%s)\n" +
                "    .isNotNull()\n" +
                "    .isNotEmpty()\n" +
                "    .isNotBlank();", actualValue);
    }

    public String generateNumericAssertions(String actualValue) {
        return String.format("assertThat(%s)\n" +
                "    .isNotNull()\n" +
                "    .isPositive()\n" +
                "    .isGreaterThan(0);", actualValue);
    }

    public String generateOptionalAssertions(String actualValue) {
        return String.format("assertThat(%s)\n" +
                "    .isPresent()\n" +
                "    .get()\n" +
                "    .isNotNull();", actualValue);
    }

    public String generateExceptionAssertion(String methodCall, String exceptionType) {
        return String.format("assertThatThrownBy(() -> %s)\n" +
                "    .isInstanceOf(%s.class)\n" +
                "    .hasMessageContaining(\"expected message\");", methodCall, exceptionType);
    }

    public String generateNoExceptionAssertion(String methodCall) {
        return String.format("assertThatCode(() -> %s)\n" +
                "    .doesNotThrowAnyException();", methodCall);
    }

    public String generateContainsAssertion(String actualValue, List<String> elements) {
        String elementsStr = String.join(", ", elements);
        return String.format("assertThat(%s).contains(%s);", actualValue, elementsStr);
    }

    public String generateContainsExactly(String actualValue, List<String> elements) {
        String elementsStr = String.join(", ", elements);
        return String.format("assertThat(%s).containsExactly(%s);", actualValue, elementsStr);
    }

    public String generateHasSize(String actualValue, int size) {
        return String.format("assertThat(%s).hasSize(%d);", actualValue, size);
    }

    public String generateExtractingAssertion(String actualValue, String property, List<String> values) {
        String valuesStr = String.join(", ", values);
        return String.format("assertThat(%s)\n" +
                        "    .extracting(\"%s\")\n" +
                        "    .containsExactly(%s);",
                actualValue, property, valuesStr);
    }

    public String generateUsingRecursiveComparison(String actualValue, String expectedValue) {
        return String.format("assertThat(%s)\n" +
                "    .usingRecursiveComparison()\n" +
                "    .isEqualTo(%s);", actualValue, expectedValue);
    }

    public String generateSatisfiesAssertion(String actualValue, String returnType) {
        return String.format("assertThat(%s).satisfies(value -> {\n" +
                        "    assertThat(value).isNotNull();\n" +
                        "    // Add more specific assertions\n" +
                        "});",
                actualValue);
    }

    public List<String> generateImports() {
        List<String> imports = new ArrayList<>();
        imports.add("import static org.assertj.core.api.Assertions.*;");
        return imports;
    }

    public String generateMethodAssertions(MethodInfo methodInfo) {
        String resultVar = "result";
        String returnType = methodInfo.returnType();

        if (returnType.equals("void")) {
            return "// No assertion needed for void method";
        }

        if (returnType.equals("String")) {
            return generateStringAssertions(resultVar);
        }

        if (returnType.matches("int|Integer|long|Long|double|Double|float|Float")) {
            return generateNumericAssertions(resultVar);
        }

        if (returnType.startsWith("List<") || returnType.startsWith("Set<")) {
            return generateCollectionAssertions(resultVar);
        }

        if (returnType.startsWith("Optional<")) {
            return generateOptionalAssertions(resultVar);
        }

        return generateBasicAssertion(resultVar, returnType);
    }

    public String generateComprehensiveAssertions(String actualValue, String returnType) {
        StringBuilder assertions = new StringBuilder();

        assertions.append(String.format("assertThat(%s).isNotNull();\n", actualValue));

        if (returnType.equals("String")) {
            assertions.append(String.format("assertThat(%s).isNotEmpty().isNotBlank();\n", actualValue));
        } else if (returnType.matches("int|Integer|long|Long")) {
            assertions.append(String.format("assertThat(%s).isPositive();\n", actualValue));
        } else if (returnType.startsWith("List<") || returnType.startsWith("Set<")) {
            assertions.append(String.format("assertThat(%s).isNotEmpty();\n", actualValue));
        } else if (returnType.startsWith("Optional<")) {
            assertions.append(String.format("assertThat(%s).isPresent();\n", actualValue));
        }

        return assertions.toString().trim();
    }
}
