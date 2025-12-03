package com.springtest.core.naming.impl;

import com.springtest.core.naming.NamingStrategy;
import com.springtest.core.model.MethodInfo;

public class SimpleNaming implements NamingStrategy {

    @Override
    public String generateTestMethodName(MethodInfo method, String scenario) {
        return "test" + capitalize(method.getName()) + getScenarioSuffix(scenario);
    }

    @Override
    public String generateDisplayName(MethodInfo method, String scenario) {
        return method.getName() + " - " + getScenarioDescription(scenario);
    }

    private String getScenarioSuffix(String scenario) {
        return switch (scenario) {
            case "happy_path" -> "";
            case "not_found" -> "NotFound";
            case "empty" -> "Empty";
            case "null_check" -> "Null";
            case "exception" -> "Exception";
            default -> capitalize(scenario);
        };
    }

    private String getScenarioDescription(String scenario) {
        return switch (scenario) {
            case "happy_path" -> "success case";
            case "not_found" -> "not found";
            case "empty" -> "empty";
            case "null_check" -> "null input";
            case "exception" -> "exception";
            default -> scenario;
        };
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}