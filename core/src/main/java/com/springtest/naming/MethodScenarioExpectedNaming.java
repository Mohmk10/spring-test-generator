package com.springtest.naming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodScenarioExpectedNaming implements NamingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(MethodScenarioExpectedNaming.class);

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

        String formattedMethod = formatMethodName(methodName);
        String formattedScenario = formatScenario(scenario);
        String formattedExpected = formatExpected(expected);

        String testName = "test" + formattedMethod + "_" + formattedScenario + "_" + formattedExpected;
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

        String formattedMethod = formatMethodName(methodName);
        String formattedScenario = formatScenario(scenario);

        String testName = "test" + formattedMethod + "_" + formattedScenario;
        logger.debug("Generated test name: {}", testName);
        return testName;
    }

    @Override
    public String generateTestMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }

        String formattedMethod = formatMethodName(methodName);
        String testName = "test" + formattedMethod;
        logger.debug("Generated test name: {}", testName);
        return testName;
    }

    @Override
    public String getStrategyName() {
        return "MethodScenarioExpected";
    }

    private String formatMethodName(String methodName) {
        return capitalize(methodName);
    }

    private String formatScenario(String scenario) {
        return capitalize(scenario);
    }

    private String formatExpected(String expected) {
        return capitalize(expected);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
