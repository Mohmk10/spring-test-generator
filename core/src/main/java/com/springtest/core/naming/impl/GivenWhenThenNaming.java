package com.springtest.core.naming.impl;

import com.springtest.core.naming.NamingStrategy;
import com.springtest.core.model.MethodInfo;

public class GivenWhenThenNaming implements NamingStrategy {

    @Override
    public String generateTestMethodName(MethodInfo method, String scenario) {
        String given = formatGiven(scenario);
        String when = formatWhen(method);
        String then = formatThen(method, scenario);

        return String.format("given%s_when%s_then%s", given, when, then);
    }

    @Override
    public String generateDisplayName(MethodInfo method, String scenario) {
        String given = getGivenDescription(scenario);
        String when = getWhenDescription(method);
        String then = getThenDescription(method, scenario);

        return String.format("Given %s, when %s, then %s", given, when, then);
    }

    private String formatGiven(String scenario) {
        return switch (scenario) {
            case "happy_path" -> "ValidInput";
            case "not_found" -> "EntityNotFound";
            case "empty" -> "EmptyCollection";
            case "null_check" -> "NullParameter";
            case "exception" -> "ErrorCondition";
            default -> capitalize(scenario);
        };
    }

    private String formatWhen(MethodInfo method) {
        return capitalize(method.getName());
    }

    private String formatThen(MethodInfo method, String scenario) {
        if ("exception".equals(scenario) || "null_check".equals(scenario)) {
            return "ThrowException";
        }

        if (method.isVoidReturn()) {
            return "Succeed";
        }

        if ("not_found".equals(scenario) || "empty".equals(scenario)) {
            return "ReturnEmpty";
        }

        return "ReturnResult";
    }

    private String getGivenDescription(String scenario) {
        return switch (scenario) {
            case "happy_path" -> "valid input";
            case "not_found" -> "entity not found";
            case "empty" -> "empty collection";
            case "null_check" -> "null parameter";
            case "exception" -> "error condition";
            default -> scenario;
        };
    }

    private String getWhenDescription(MethodInfo method) {
        return method.getName() + " is called";
    }

    private String getThenDescription(MethodInfo method, String scenario) {
        if ("exception".equals(scenario) || "null_check".equals(scenario)) {
            return "exception is thrown";
        }

        if (method.isVoidReturn()) {
            return "operation succeeds";
        }

        if ("not_found".equals(scenario) || "empty".equals(scenario)) {
            return "empty result is returned";
        }

        return "result is returned";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}