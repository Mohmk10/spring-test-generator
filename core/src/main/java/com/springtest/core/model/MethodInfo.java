package com.springtest.core.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;


@Value
@Builder
public class MethodInfo {

    String name;
    String returnType;
    String simpleReturnType;

    List<ParameterInfo> parameters;
    List<AnnotationInfo> annotations;
    List<ExceptionInfo> declaredExceptions;
    List<ExceptionInfo> thrownExceptions;


    boolean publicMethod;
    boolean staticMethod;
    boolean voidReturn;
    boolean returnsOptional;
    boolean returnsCollection;
    int complexity;


    public boolean hasAnnotation(String annotationSimpleName) {
        return annotations != null && annotations.stream()
                .anyMatch(a -> a.matches(annotationSimpleName));
    }


    public boolean isSpringWebEndpoint() {
        return annotations != null && annotations.stream()
                .anyMatch(AnnotationInfo::isSpringWebMapping);
    }


    public List<ExceptionInfo> getAllExceptions() {
        var all = new java.util.ArrayList<ExceptionInfo>();
        if (declaredExceptions != null) all.addAll(declaredExceptions);
        if (thrownExceptions != null) all.addAll(thrownExceptions);
        return all;
    }


    public boolean needsNullChecks() {
        return parameters != null && parameters.stream()
                .anyMatch(ParameterInfo::isNullable);
    }


    public boolean needsValidationTests() {
        return parameters != null && parameters.stream()
                .anyMatch(ParameterInfo::isValidated);
    }
}