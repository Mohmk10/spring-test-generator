package com.springtest.naming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BDDNaming implements NamingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(BDDNaming.class);

    @Override
    public String generateTestMethodName(String methodName, String scenario, String expected) {
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }
        if (scenario == null || scenario.isEmpty()) {
            throw new IllegalArgumentException("Scenario cannot be null or empty");
        }
        if (expected == null || expected.isEmpty()) {
            throw new IllegalArgumentException("Expected cannot be null or empty");
        }

        String action = formatAction(methodName);
        String condition = formatCondition(scenario);
        String result = formatResult(expected);

        String testName = "should" + result + "When" + condition + "And" + action;
        logger.debug("Generated test name: {}", testName);
        return testName;
    }

    @Override
    public String generateTestMethodName(String methodName, String scenario) {
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }
        if (scenario == null || scenario.isEmpty()) {
            throw new IllegalArgumentException("Scenario cannot be null or empty");
        }

        String action = formatAction(methodName);
        String condition = formatCondition(scenario);

        String testName = "should" + action + "When" + condition;
        logger.debug("Generated test name: {}", testName);
        return testName;
    }

    @Override
    public String generateTestMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }

        String action = formatAction(methodName);
        String testName = "should" + action;
        logger.debug("Generated test name: {}", testName);
        return testName;
    }

    @Override
    public String getStrategyName() {
        return "BDD";
    }

    private String formatAction(String methodName) {
        return capitalize(methodName);
    }

    private String formatCondition(String scenario) {
        return capitalize(scenario);
    }

    private String formatResult(String expected) {
        return capitalize(expected);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
