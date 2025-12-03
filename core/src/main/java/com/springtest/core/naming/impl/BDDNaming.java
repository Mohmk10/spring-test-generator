package com.springtest.core.naming.impl;

import com.springtest.core.naming.NamingStrategy;
import com.springtest.core.model.MethodInfo;

public class BDDNaming implements NamingStrategy {

    @Override
    public String generateTestMethodName(MethodInfo method, String scenario) {
        String action = formatAction(method, scenario);
        String condition = formatCondition(scenario);

        return String.format("should_%s_%s", action, condition);
    }

    @Override
    public String generateDisplayName(MethodInfo method, String scenario) {
        String action = getActionDescription(method, scenario);
        String condition = getConditionDescription(scenario);

        return String.format("should %s %s", action, condition);
    }

    private String formatAction(MethodInfo method, String scenario) {
        if ("exception".equals(scenario) || "null_check".equals(scenario)) {
            return "throw_exception";
        }

        if (method.isVoidReturn()) {
            return "complete_successfully";
        }

        if ("not_found".equals(scenario) || "empty".equals(scenario)) {
            return "return_empty";
        }

        return "return_" + method.getSimpleReturnType().toLowerCase();
    }

    private String formatCondition(String scenario) {
        return switch (scenario) {
            case "happy_path" -> "when_valid";
            case "not_found" -> "when_not_found";
            case "empty" -> "when_empty";
            case "null_check" -> "when_null";
            case "exception" -> "when_error";
            default -> "when_" + scenario;
        };
    }

    private String getActionDescription(MethodInfo method, String scenario) {
        if ("exception".equals(scenario) || "null_check".equals(scenario)) {
            return "throw exception";
        }

        if (method.isVoidReturn()) {
            return "complete successfully";
        }

        if ("not_found".equals(scenario) || "empty".equals(scenario)) {
            return "return empty";
        }

        return "return " + method.getSimpleReturnType();
    }

    private String getConditionDescription(String scenario) {
        return switch (scenario) {
            case "happy_path" -> "when input is valid";
            case "not_found" -> "when not found";
            case "empty" -> "when empty";
            case "null_check" -> "when null";
            case "exception" -> "when error occurs";
            default -> "when " + scenario;
        };
    }
}