package com.springtest.assertion;

import com.springtest.model.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MutationHintGenerator {
    private static final Logger logger = LoggerFactory.getLogger(MutationHintGenerator.class);

    public List<String> generateHints(MethodInfo methodInfo) {
        logger.debug("Generating mutation hints for method: {}", methodInfo.name());

        List<String> hints = new ArrayList<>();

        hints.addAll(generateReturnTypeHints(methodInfo.returnType()));

        if (methodInfo.hasParameters()) {
            hints.addAll(generateParameterHints());
        }

        if (methodInfo.throwsExceptions()) {
            hints.addAll(generateExceptionHints());
        }

        if (methodInfo.returnType().equals("boolean") || methodInfo.returnType().equals("Boolean")) {
            hints.addAll(generateBooleanLogicHints());
        }

        return hints;
    }

    private List<String> generateReturnTypeHints(String returnType) {
        List<String> hints = new ArrayList<>();

        switch (returnType) {
            case "String":
                hints.add("// Mutation hint: Check string content, not just non-null/non-empty");
                hints.add("// Mutation hint: Verify exact string value or pattern");
                hints.add("// Mutation hint: Test edge cases: empty string, special characters");
                break;

            case "int", "Integer", "long", "Long":
                hints.add("// Mutation hint: Check exact numeric value, not just > 0");
                hints.add("// Mutation hint: Test boundary values: 0, -1, MAX_VALUE");
                hints.add("// Mutation hint: Verify calculation results, not just sign");
                break;

            case "boolean", "Boolean":
                hints.add("// Mutation hint: Test both true and false cases");
                hints.add("// Mutation hint: Verify the actual condition, not just the return");
                break;

            case "List", "Set":
                hints.add("// Mutation hint: Check collection size and contents");
                hints.add("// Mutation hint: Verify element order (for List)");
                hints.add("// Mutation hint: Test with empty collection");
                break;

            case "Optional":
                hints.add("// Mutation hint: Test both present and empty cases");
                hints.add("// Mutation hint: Verify the actual value inside Optional");
                break;

            default:
                if (returnType.startsWith("List<") || returnType.startsWith("Set<")) {
                    hints.add("// Mutation hint: Verify collection contents, not just size");
                    hints.add("// Mutation hint: Check element properties/fields");
                } else if (returnType.startsWith("Optional<")) {
                    hints.add("// Mutation hint: Verify Optional content when present");
                } else if (!returnType.equals("void")) {
                    hints.add("// Mutation hint: Verify object state/fields, not just non-null");
                    hints.add("// Mutation hint: Use field-by-field comparison");
                }
                break;
        }

        return hints;
    }

    private List<String> generateParameterHints() {
        List<String> hints = new ArrayList<>();
        hints.add("// Mutation hint: Test with null parameters");
        hints.add("// Mutation hint: Test with invalid/boundary parameter values");
        hints.add("// Mutation hint: Verify parameter validation logic");
        return hints;
    }

    private List<String> generateExceptionHints() {
        List<String> hints = new ArrayList<>();
        hints.add("// Mutation hint: Verify exception type, not just that it's thrown");
        hints.add("// Mutation hint: Check exception message content");
        hints.add("// Mutation hint: Verify exception cause if applicable");
        return hints;
    }

    private List<String> generateBooleanLogicHints() {
        List<String> hints = new ArrayList<>();
        hints.add("// Mutation hint: Test all branches of boolean conditions");
        hints.add("// Mutation hint: Verify negation doesn't change test outcome");
        hints.add("// Mutation hint: Test boundary conditions that flip the result");
        return hints;
    }

    public String generateAssertionStrengtheningHint(String assertionType) {
        return switch (assertionType) {
            case "isNotNull" ->
                    "// Strengthen: Add assertions about the object's state/properties";
            case "isNotEmpty" ->
                    "// Strengthen: Verify exact size or specific contents";
            case "isPositive" ->
                    "// Strengthen: Check exact value, not just sign";
            case "contains" ->
                    "// Strengthen: Use containsExactly or verify all elements";
            case "hasSize" ->
                    "// Strengthen: Verify contents, not just size";
            default ->
                    "// Strengthen: Consider more specific assertions";
        };
    }

    public List<String> generateCoverageHints(MethodInfo methodInfo) {
        List<String> hints = new ArrayList<>();

        hints.add("// Coverage hint: Test happy path");
        hints.add("// Coverage hint: Test error/exception paths");

        if (methodInfo.hasParameters()) {
            hints.add("// Coverage hint: Test with various parameter combinations");
            hints.add("// Coverage hint: Test edge cases and boundary values");
        }

        if (methodInfo.throwsExceptions()) {
            hints.add("// Coverage hint: Trigger each declared exception");
        }

        return hints;
    }

    public List<String> generateMutationOperatorHints() {
        List<String> hints = new ArrayList<>();

        hints.add("// Mutation operator: Arithmetic - verify exact calculations");
        hints.add("// Mutation operator: Conditional boundaries - test <= vs <");
        hints.add("// Mutation operator: Negate conditionals - test both branches");
        hints.add("// Mutation operator: Return values - verify exact return, not type");
        hints.add("// Mutation operator: Void method calls - verify side effects");

        return hints;
    }

    public String generateCompleteHintSection(MethodInfo methodInfo) {
        StringBuilder hints = new StringBuilder();

        hints.append("/*\n");
        hints.append(" * MUTATION TESTING HINTS:\n");
        hints.append(" * \n");

        for (String hint : generateHints(methodInfo)) {
            hints.append(" * ").append(hint.replace("//", "")).append("\n");
        }

        hints.append(" * \n");
        hints.append(" * COVERAGE IMPROVEMENTS:\n");
        hints.append(" * \n");

        for (String hint : generateCoverageHints(methodInfo)) {
            hints.append(" * ").append(hint.replace("//", "")).append("\n");
        }

        hints.append(" */");

        return hints.toString();
    }

    public String generateScenarioHint(String scenario) {
        return switch (scenario) {
            case "null-input" ->
                    "// Test with null input to ensure proper validation";
            case "empty-collection" ->
                    "// Test with empty collection to verify boundary handling";
            case "invalid-input" ->
                    "// Test with invalid input to verify validation logic";
            case "concurrent-access" ->
                    "// Consider testing thread-safety if applicable";
            case "timeout" ->
                    "// Test timeout scenarios for async operations";
            default ->
                    "// Test edge case: " + scenario;
        };
    }

    public List<String> generateSpecificityHints(String returnType) {
        List<String> hints = new ArrayList<>();

        if (returnType.equals("String")) {
            hints.add("// Use .isEqualTo() instead of .isNotEmpty() when possible");
            hints.add("// Use .matches() for pattern validation");
            hints.add("// Use .startsWith()/.endsWith() for prefix/suffix checks");
        } else if (returnType.matches("int|Integer|long|Long")) {
            hints.add("// Use .isEqualTo(expected) instead of .isPositive()");
            hints.add("// Use .isBetween(min, max) for range checks");
        } else if (returnType.startsWith("List<")) {
            hints.add("// Use .containsExactly() instead of .contains()");
            hints.add("// Use .extracting() to verify element properties");
        }

        return hints;
    }
}
