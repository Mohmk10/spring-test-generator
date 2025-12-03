package com.springtest.core.naming.impl;

import com.springtest.core.naming.NamingStrategy;
import com.springtest.core.model.MethodInfo;

public class MethodScenarioExpectedNaming implements NamingStrategy {

    @Override
    public String generateTestMethodName(MethodInfo method, String scenario) {
        String methodName = method.getName();
        String scenarioPart = formatScenario(scenario);
        String expectedPart = formatExpected(method, scenario);

        return String.format("%s_%s_%s", methodName, scenarioPart, expectedPart);
    }

    @Override
    public String generateDisplayName(MethodInfo method, String scenario) {
        String scenarioDesc = getScenarioDescription(scenario);
        String expectedDesc = getExpectedDescription(method, scenario);

        return String.format("%s - %s - %s", method.getName(), scenarioDesc, expectedDesc);
    }

    private String formatScenario(String scenario) {
        return switch (scenario) {
            case "happy_path" -> "WhenValid";
            case "not_found" -> "WhenNotFound";
            case "empty" -> "WhenEmpty";
            case "null_check" -> "WhenNull";
            case "exception" -> "WhenError";
            default -> "When" + capitalize(scenario);
        };
    }

    private String formatExpected(MethodInfo method, String scenario) {
        if ("exception".equals(scenario) || "null_check".equals(scenario)) {
            return "ShouldThrowException";
        }

        if (method.isVoidReturn()) {
            return "ShouldSucceed";
        }

        if ("not_found".equals(scenario) || "empty".equals(scenario)) {
            return method.isReturnsOptional() ? "ShouldReturnEmpty" : "ShouldReturnNull";
        }

        return "ShouldReturnResult";
    }

    private String getScenarioDescription(String scenario) {
        return switch (scenario) {
            case "happy_path" -> "with valid input";
            case "not_found" -> "when entity not found";
            case "empty" -> "when collection is empty";
            case "null_check" -> "when parameter is null";
            case "exception" -> "when error occurs";
            default -> scenario;
        };
    }

    private String getExpectedDescription(MethodInfo method, String scenario) {
        if ("exception".equals(scenario) || "null_check".equals(scenario)) {
            return "should throw exception";
        }

        if (method.isVoidReturn()) {
            return "should complete successfully";
        }

        if ("not_found".equals(scenario) || "empty".equals(scenario)) {
            return "should return empty result";
        }

        return "should return valid result";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}