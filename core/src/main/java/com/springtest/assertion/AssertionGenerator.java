package com.springtest.assertion;

import com.springtest.model.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates AssertJ assertions for various types and scenarios.
 * Creates type-specific assertThat() statements with appropriate matchers.
 */
public class AssertionGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AssertionGenerator.class);

    /**
     * Generates a basic assertThat() statement for a value.
     *
     * @param actualValue Value to assert
     * @param returnType  Type of the value
     * @return Assertion code as Java string
     */
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

    /**
     * Generates an assertion for a method result.
     *
     * @param methodInfo Information about the method
     * @return Assertion code as Java string
     */
    public String generateAssertionForMethod(MethodInfo methodInfo) {
        String resultVar = "result";
        return generateBasicAssertion(resultVar, methodInfo.returnType());
    }

    /**
     * Generates an isEqualTo assertion.
     *
     * @param actualValue   Value to assert
     * @param expectedValue Expected value
     * @return Assertion code as Java string
     */
    public String generateIsEqualTo(String actualValue, String expectedValue) {
        return String.format("assertThat(%s).isEqualTo(%s);", actualValue, expectedValue);
    }

    /**
     * Generates an isNotEqualTo assertion.
     *
     * @param actualValue   Value to assert
     * @param expectedValue Value that should not match
     * @return Assertion code as Java string
     */
    public String generateIsNotEqualTo(String actualValue, String expectedValue) {
        return String.format("assertThat(%s).isNotEqualTo(%s);", actualValue, expectedValue);
    }

    /**
     * Generates collection-specific assertions.
     *
     * @param actualValue Value to assert (collection)
     * @return Assertion code as Java string
     */
    public String generateCollectionAssertions(String actualValue) {
        return String.format("assertThat(%s)\n" +
                "    .isNotNull()\n" +
                "    .isNotEmpty()\n" +
                "    .hasSize(1);", actualValue);
    }

    /**
     * Generates string-specific assertions.
     *
     * @param actualValue Value to assert (string)
     * @return Assertion code as Java string
     */
    public String generateStringAssertions(String actualValue) {
        return String.format("assertThat(%s)\n" +
                "    .isNotNull()\n" +
                "    .isNotEmpty()\n" +
                "    .isNotBlank();", actualValue);
    }

    /**
     * Generates numeric assertions.
     *
     * @param actualValue Value to assert (number)
     * @return Assertion code as Java string
     */
    public String generateNumericAssertions(String actualValue) {
        return String.format("assertThat(%s)\n" +
                "    .isNotNull()\n" +
                "    .isPositive()\n" +
                "    .isGreaterThan(0);", actualValue);
    }

    /**
     * Generates Optional assertions.
     *
     * @param actualValue Value to assert (Optional)
     * @return Assertion code as Java string
     */
    public String generateOptionalAssertions(String actualValue) {
        return String.format("assertThat(%s)\n" +
                "    .isPresent()\n" +
                "    .get()\n" +
                "    .isNotNull();", actualValue);
    }

    /**
     * Generates exception assertion (assertThatThrownBy).
     *
     * @param methodCall    Method call that should throw
     * @param exceptionType Expected exception type
     * @return Assertion code as Java string
     */
    public String generateExceptionAssertion(String methodCall, String exceptionType) {
        return String.format("assertThatThrownBy(() -> %s)\n" +
                "    .isInstanceOf(%s.class)\n" +
                "    .hasMessageContaining(\"expected message\");", methodCall, exceptionType);
    }

    /**
     * Generates assertion for no exception thrown.
     *
     * @param methodCall Method call that should not throw
     * @return Assertion code as Java string
     */
    public String generateNoExceptionAssertion(String methodCall) {
        return String.format("assertThatCode(() -> %s)\n" +
                "    .doesNotThrowAnyException();", methodCall);
    }

    /**
     * Generates contains assertion for collections.
     *
     * @param actualValue Value to assert (collection)
     * @param elements    Elements that should be contained
     * @return Assertion code as Java string
     */
    public String generateContainsAssertion(String actualValue, List<String> elements) {
        String elementsStr = String.join(", ", elements);
        return String.format("assertThat(%s).contains(%s);", actualValue, elementsStr);
    }

    /**
     * Generates containsExactly assertion for collections.
     *
     * @param actualValue Value to assert (collection)
     * @param elements    Exact elements expected
     * @return Assertion code as Java string
     */
    public String generateContainsExactly(String actualValue, List<String> elements) {
        String elementsStr = String.join(", ", elements);
        return String.format("assertThat(%s).containsExactly(%s);", actualValue, elementsStr);
    }

    /**
     * Generates hasSize assertion for collections.
     *
     * @param actualValue Value to assert (collection)
     * @param size        Expected size
     * @return Assertion code as Java string
     */
    public String generateHasSize(String actualValue, int size) {
        return String.format("assertThat(%s).hasSize(%d);", actualValue, size);
    }

    /**
     * Generates extracting assertion for collections of objects.
     *
     * @param actualValue Value to assert (collection)
     * @param property    Property to extract
     * @param values      Expected values
     * @return Assertion code as Java string
     */
    public String generateExtractingAssertion(String actualValue, String property, List<String> values) {
        String valuesStr = String.join(", ", values);
        return String.format("assertThat(%s)\n" +
                        "    .extracting(\"%s\")\n" +
                        "    .containsExactly(%s);",
                actualValue, property, valuesStr);
    }

    /**
     * Generates field-by-field comparison assertion.
     *
     * @param actualValue   Value to assert
     * @param expectedValue Expected value
     * @return Assertion code as Java string
     */
    public String generateUsingRecursiveComparison(String actualValue, String expectedValue) {
        return String.format("assertThat(%s)\n" +
                "    .usingRecursiveComparison()\n" +
                "    .isEqualTo(%s);", actualValue, expectedValue);
    }

    /**
     * Generates satisfies assertion for custom conditions.
     *
     * @param actualValue Value to assert
     * @param returnType  Type of the value
     * @return Assertion code as Java string
     */
    public String generateSatisfiesAssertion(String actualValue, String returnType) {
        return String.format("assertThat(%s).satisfies(value -> {\n" +
                        "    assertThat(value).isNotNull();\n" +
                        "    // Add more specific assertions\n" +
                        "});",
                actualValue);
    }

    /**
     * Generates required imports for assertions.
     *
     * @return List of import statements
     */
    public List<String> generateImports() {
        List<String> imports = new ArrayList<>();
        imports.add("import static org.assertj.core.api.Assertions.*;");
        return imports;
    }

    /**
     * Generates assertions based on method return type.
     *
     * @param methodInfo Information about the method
     * @return Complete assertion code
     */
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

    /**
     * Generates comprehensive assertions for an object.
     *
     * @param actualValue Value to assert
     * @param returnType  Type of the value
     * @return Complete assertion code with multiple checks
     */
    public String generateComprehensiveAssertions(String actualValue, String returnType) {
        StringBuilder assertions = new StringBuilder();

        // Basic null check
        assertions.append(String.format("assertThat(%s).isNotNull();\n", actualValue));

        // Type-specific assertions
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
