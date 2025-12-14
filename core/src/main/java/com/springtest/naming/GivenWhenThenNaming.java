package com.springtest.naming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GivenWhenThenNaming implements NamingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(GivenWhenThenNaming.class);

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

        String given = formatGiven(scenario);
        String when = formatWhen(methodName);
        String then = formatThen(expected);

        String testName = "given" + given + "_when" + when + "_then" + then;
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

        String given = formatGiven(scenario);
        String when = formatWhen(methodName);

        String testName = "given" + given + "_when" + when;
        logger.debug("Generated test name: {}", testName);
        return testName;
    }

    @Override
    public String generateTestMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }

        String when = formatWhen(methodName);
        String testName = "when" + when;
        logger.debug("Generated test name: {}", testName);
        return testName;
    }

    @Override
    public String getStrategyName() {
        return "GivenWhenThen";
    }

    private String formatGiven(String scenario) {
        return capitalize(scenario);
    }

    private String formatWhen(String methodName) {
        return capitalize(methodName);
    }

    private String formatThen(String expected) {
        return capitalize(expected);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
