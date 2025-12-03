package com.springtest.core.generator;

import com.springtest.core.model.MethodInfo;
import com.springtest.core.model.ParameterInfo;
import com.springtest.core.model.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EdgeCaseGenerator {

    public List<TestCase> generateEdgeCases(MethodInfo method) {
        List<TestCase> edgeCases = new ArrayList<>();

        for (ParameterInfo param : method.getParameters()) {
            edgeCases.addAll(generateEdgeCasesForParameter(method, param));
        }

        return edgeCases;
    }

    private List<TestCase> generateEdgeCasesForParameter(MethodInfo method, ParameterInfo param) {
        List<TestCase> cases = new ArrayList<>();

        if (param.isNullable()) {
            cases.add(generateNullCase(method, param));
        }

        if (isStringType(param)) {
            cases.add(generateEmptyStringCase(method, param));
            cases.add(generateWhitespaceCase(method, param));
        }

        if (isNumericType(param)) {
            cases.add(generateZeroCase(method, param));
            cases.add(generateNegativeCase(method, param));
            cases.add(generateMaxValueCase(method, param));
        }

        if (param.isCollection()) {
            cases.add(generateEmptyCollectionCase(method, param));
        }

        return cases;
    }

    private TestCase generateNullCase(MethodInfo method, ParameterInfo param) {
        return TestCase.builder()
                .testMethodName(method.getName() + "_WithNull" + capitalize(param.getName()))
                .displayName("Test with null " + param.getName())
                .targetMethod(method)
                .scenario("edge_case_null")
                .description("Test null parameter: " + param.getName())
                .givenStatements(List.of("// Null " + param.getName()))
                .mockSetups(List.of())
                .whenStatement("// Call with null")
                .thenStatements(List.of("// Verify behavior"))
                .expectsException(true)
                .priority(5)
                .build();
    }

    private TestCase generateEmptyStringCase(MethodInfo method, ParameterInfo param) {
        return TestCase.builder()
                .testMethodName(method.getName() + "_WithEmpty" + capitalize(param.getName()))
                .displayName("Test with empty " + param.getName())
                .targetMethod(method)
                .scenario("edge_case_empty")
                .description("Test empty string: " + param.getName())
                .givenStatements(List.of("String " + param.getName() + " = \"\";"))
                .mockSetups(List.of())
                .whenStatement("// Call with empty string")
                .thenStatements(List.of("// Verify behavior"))
                .expectsException(false)
                .priority(4)
                .build();
    }

    private TestCase generateWhitespaceCase(MethodInfo method, ParameterInfo param) {
        return TestCase.builder()
                .testMethodName(method.getName() + "_WithWhitespace" + capitalize(param.getName()))
                .displayName("Test with whitespace " + param.getName())
                .targetMethod(method)
                .scenario("edge_case_whitespace")
                .description("Test whitespace string: " + param.getName())
                .givenStatements(List.of("String " + param.getName() + " = \"   \";"))
                .mockSetups(List.of())
                .whenStatement("// Call with whitespace")
                .thenStatements(List.of("// Verify behavior"))
                .expectsException(false)
                .priority(3)
                .build();
    }

    private TestCase generateZeroCase(MethodInfo method, ParameterInfo param) {
        return TestCase.builder()
                .testMethodName(method.getName() + "_WithZero" + capitalize(param.getName()))
                .displayName("Test with zero " + param.getName())
                .targetMethod(method)
                .scenario("edge_case_zero")
                .description("Test zero value: " + param.getName())
                .givenStatements(List.of(param.getType() + " " + param.getName() + " = 0;"))
                .mockSetups(List.of())
                .whenStatement("// Call with zero")
                .thenStatements(List.of("// Verify behavior"))
                .expectsException(false)
                .priority(4)
                .build();
    }

    private TestCase generateNegativeCase(MethodInfo method, ParameterInfo param) {
        return TestCase.builder()
                .testMethodName(method.getName() + "_WithNegative" + capitalize(param.getName()))
                .displayName("Test with negative " + param.getName())
                .targetMethod(method)
                .scenario("edge_case_negative")
                .description("Test negative value: " + param.getName())
                .givenStatements(List.of(param.getType() + " " + param.getName() + " = -1;"))
                .mockSetups(List.of())
                .whenStatement("// Call with negative")
                .thenStatements(List.of("// Verify behavior"))
                .expectsException(false)
                .priority(4)
                .build();
    }

    private TestCase generateMaxValueCase(MethodInfo method, ParameterInfo param) {
        return TestCase.builder()
                .testMethodName(method.getName() + "_WithMax" + capitalize(param.getName()))
                .displayName("Test with max " + param.getName())
                .targetMethod(method)
                .scenario("edge_case_max")
                .description("Test maximum value: " + param.getName())
                .givenStatements(List.of(param.getType() + " " + param.getName() + " = " + getMaxValue(param) + ";"))
                .mockSetups(List.of())
                .whenStatement("// Call with max value")
                .thenStatements(List.of("// Verify behavior"))
                .expectsException(false)
                .priority(3)
                .build();
    }

    private TestCase generateEmptyCollectionCase(MethodInfo method, ParameterInfo param) {
        return TestCase.builder()
                .testMethodName(method.getName() + "_WithEmptyCollection")
                .displayName("Test with empty collection")
                .targetMethod(method)
                .scenario("edge_case_empty_collection")
                .description("Test empty collection: " + param.getName())
                .givenStatements(List.of(param.getType() + " " + param.getName() + " = List.of();"))
                .mockSetups(List.of())
                .whenStatement("// Call with empty collection")
                .thenStatements(List.of("// Verify behavior"))
                .expectsException(false)
                .priority(4)
                .build();
    }

    private boolean isStringType(ParameterInfo param) {
        return "String".equals(param.getSimpleType());
    }

    private boolean isNumericType(ParameterInfo param) {
        String type = param.getSimpleType();
        return "int".equals(type) || "Integer".equals(type)
                || "long".equals(type) || "Long".equals(type)
                || "double".equals(type) || "Double".equals(type);
    }

    private String getMaxValue(ParameterInfo param) {
        String type = param.getSimpleType();
        return switch (type) {
            case "int", "Integer" -> "Integer.MAX_VALUE";
            case "long", "Long" -> "Long.MAX_VALUE";
            case "double", "Double" -> "Double.MAX_VALUE";
            default -> "999999";
        };
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}