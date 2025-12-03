package com.springtest.core.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;


@Value
@Builder
public class TestSuite {

    ClassInfo targetClass;

    String testClassName;
    String testPackage;

    List<TestCase> testCases;

    String testType;

    List<String> imports;
    List<String> classAnnotations;
    List<String> testFields;
    List<String> setupMethods;


    public int getTestCaseCount() {
        return testCases != null ? testCases.size() : 0;
    }


    public List<TestCase> getTestCasesByScenario(String scenario) {
        return testCases != null
                ? testCases.stream()
                .filter(tc -> scenario.equals(tc.getScenario()))
                .toList()
                : List.of();
    }


    public List<TestCase> getHappyPathTests() {
        return getTestCasesByScenario("happy_path");
    }


    public List<TestCase> getEdgeCaseTests() {
        return getTestCasesByScenario("edge_case");
    }


    public List<TestCase> getExceptionTests() {
        return getTestCasesByScenario("exception");
    }
}