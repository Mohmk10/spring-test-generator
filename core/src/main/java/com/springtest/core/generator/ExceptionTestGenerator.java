package com.springtest.core.generator;

import com.springtest.core.model.ExceptionInfo;
import com.springtest.core.model.MethodInfo;
import com.springtest.core.model.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExceptionTestGenerator {

    public List<TestCase> generateExceptionTests(MethodInfo method) {
        List<TestCase> testCases = new ArrayList<>();

        for (ExceptionInfo exception : method.getAllExceptions()) {
            testCases.add(generateExceptionTest(method, exception));
        }

        return testCases;
    }

    private TestCase generateExceptionTest(MethodInfo method, ExceptionInfo exception) {
        String testMethodName = String.format("%s_ShouldThrow%s",
                method.getName(), exception.getSimpleExceptionType());

        String displayName = String.format("Test %s throws %s",
                method.getName(), exception.getSimpleExceptionType());

        List<String> givenStatements = new ArrayList<>();
        givenStatements.add("// Setup condition that triggers " + exception.getSimpleExceptionType());

        List<String> thenStatements = new ArrayList<>();

        if (exception.getMessagePattern() != null) {
            thenStatements.add(String.format(
                    "assertThatThrownBy(() -> service.%s(invalidParam))%n" +
                            "    .isInstanceOf(%s.class)%n" +
                            "    .hasMessageContaining(\"%s\");",
                    method.getName(),
                    exception.getSimpleExceptionType(),
                    exception.getMessagePattern()
            ));
        } else {
            thenStatements.add(String.format(
                    "assertThatThrownBy(() -> service.%s(invalidParam))%n" +
                            "    .isInstanceOf(%s.class);",
                    method.getName(),
                    exception.getSimpleExceptionType()
            ));
        }

        return TestCase.builder()
                .testMethodName(testMethodName)
                .displayName(displayName)
                .targetMethod(method)
                .scenario("exception")
                .description("Test exception: " + exception.getSimpleExceptionType())
                .givenStatements(givenStatements)
                .mockSetups(List.of())
                .whenStatement("// Exception should be thrown")
                .thenStatements(thenStatements)
                .expectedException(exception)
                .expectsException(true)
                .priority(8)
                .build();
    }
}