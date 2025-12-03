package com.springtest.core.mock;

import com.springtest.core.model.MethodInfo;
import com.springtest.core.model.ParameterInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StubGenerator {

    public List<String> generateStubs(MethodInfo method, String scenario) {
        List<String> stubs = new ArrayList<>();

        return switch (scenario) {
            case "happy_path" -> generateHappyPathStubs(method);
            case "not_found" -> generateNotFoundStubs(method);
            case "empty" -> generateEmptyStubs(method);
            case "exception" -> generateExceptionStubs(method);
            default -> stubs;
        };
    }

    private List<String> generateHappyPathStubs(MethodInfo method) {
        List<String> stubs = new ArrayList<>();

        if (method.isReturnsOptional()) {
            stubs.add("when(repository.findById(anyLong())).thenReturn(Optional.of(entity));");
        } else if (method.isReturnsCollection()) {
            stubs.add("when(repository.findAll()).thenReturn(List.of(entity));");
        } else if (!method.isVoidReturn()) {
            stubs.add("when(repository.save(any())).thenReturn(entity);");
        }

        return stubs;
    }

    private List<String> generateNotFoundStubs(MethodInfo method) {
        List<String> stubs = new ArrayList<>();

        if (method.isReturnsOptional()) {
            stubs.add("when(repository.findById(anyLong())).thenReturn(Optional.empty());");
        }

        return stubs;
    }

    private List<String> generateEmptyStubs(MethodInfo method) {
        List<String> stubs = new ArrayList<>();

        if (method.isReturnsCollection()) {
            stubs.add("when(repository.findAll()).thenReturn(List.of());");
        }

        return stubs;
    }

    private List<String> generateExceptionStubs(MethodInfo method) {
        List<String> stubs = new ArrayList<>();

        if (!method.getAllExceptions().isEmpty()) {
            String exceptionType = method.getAllExceptions().get(0).getSimpleExceptionType();
            stubs.add(String.format("when(repository.findById(anyLong())).thenThrow(new %s(\"Error\"));", exceptionType));
        }

        return stubs;
    }

    public String generateMockSetup(String fieldName, String methodName, String returnValue) {
        return String.format("when(%s.%s(any())).thenReturn(%s);",
                fieldName, methodName, returnValue);
    }

    public String generateMockSetupWithMatcher(String fieldName, String methodName,
                                               String matcherType, String returnValue) {
        return String.format("when(%s.%s(%s())).thenReturn(%s);",
                fieldName, methodName, matcherType, returnValue);
    }
}