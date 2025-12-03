package com.springtest.core.assertion;

import com.springtest.core.model.MethodInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AssertionGenerator {

    public List<String> generateAssertions(MethodInfo method, String scenario) {
        List<String> assertions = new ArrayList<>();

        return switch (scenario) {
            case "happy_path" -> generateHappyPathAssertions(method);
            case "not_found" -> generateNotFoundAssertions(method);
            case "empty" -> generateEmptyAssertions(method);
            case "null_check" -> generateNullCheckAssertions(method);
            case "exception" -> generateExceptionAssertions(method);
            default -> assertions;
        };
    }

    private List<String> generateHappyPathAssertions(MethodInfo method) {
        List<String> assertions = new ArrayList<>();

        if (method.isVoidReturn()) {
            return assertions;
        }

        assertions.add("assertThat(result).isNotNull();");

        if (method.isReturnsOptional()) {
            assertions.add("assertThat(result).isPresent();");
        } else if (method.isReturnsCollection()) {
            assertions.add("assertThat(result).isNotEmpty();");
        }

        return assertions;
    }

    private List<String> generateNotFoundAssertions(MethodInfo method) {
        List<String> assertions = new ArrayList<>();

        if (method.isReturnsOptional()) {
            assertions.add("assertThat(result).isEmpty();");
        } else if (method.isReturnsCollection()) {
            assertions.add("assertThat(result).isEmpty();");
        }

        return assertions;
    }

    private List<String> generateEmptyAssertions(MethodInfo method) {
        List<String> assertions = new ArrayList<>();

        if (method.isReturnsCollection()) {
            assertions.add("assertThat(result).isEmpty();");
        }

        return assertions;
    }

    private List<String> generateNullCheckAssertions(MethodInfo method) {
        List<String> assertions = new ArrayList<>();

        assertions.add("assertThat(result).isNull();");

        return assertions;
    }

    private List<String> generateExceptionAssertions(MethodInfo method) {
        List<String> assertions = new ArrayList<>();

        if (!method.getAllExceptions().isEmpty()) {
            String exceptionType = method.getAllExceptions().get(0).getSimpleExceptionType();
            String message = method.getAllExceptions().get(0).getMessagePattern();

            if (message != null) {
                assertions.add(String.format(
                        "assertThatThrownBy(() -> service.%s(null))%n    .isInstanceOf(%s.class)%n    .hasMessageContaining(\"%s\");",
                        method.getName(), exceptionType, message
                ));
            } else {
                assertions.add(String.format(
                        "assertThatThrownBy(() -> service.%s(null))%n    .isInstanceOf(%s.class);",
                        method.getName(), exceptionType
                ));
            }
        }

        return assertions;
    }

    public String generateCustomAssertion(String actual, String expected, String assertionType) {
        return switch (assertionType) {
            case "equals" -> String.format("assertThat(%s).isEqualTo(%s);", actual, expected);
            case "not_null" -> String.format("assertThat(%s).isNotNull();", actual);
            case "is_null" -> String.format("assertThat(%s).isNull();", actual);
            case "is_true" -> String.format("assertThat(%s).isTrue();", actual);
            case "is_false" -> String.format("assertThat(%s).isFalse();", actual);
            case "contains" -> String.format("assertThat(%s).contains(%s);", actual, expected);
            case "has_size" -> String.format("assertThat(%s).hasSize(%s);", actual, expected);
            default -> String.format("assertThat(%s).isEqualTo(%s);", actual, expected);
        };
    }
}