package com.springtest.naming;

public interface NamingStrategy {

    String generateTestMethodName(String methodName, String scenario, String expected);

    String generateTestMethodName(String methodName, String scenario);

    String generateTestMethodName(String methodName);

    String getStrategyName();
}
