package com.springtest.core.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;


@Value
@Builder
public class TestCase {

    String testMethodName;

    String displayName;

    MethodInfo targetMethod;

    String scenario;
    String description;

    List<String> givenStatements;

    String whenStatement;

    List<String> thenStatements;

    ExceptionInfo expectedException;

    List<String> mockSetups;
    List<String> verifyStatements;

    boolean expectsException;
    int priority;
}