package com.springtest.core.model;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class ExceptionInfo {

    String exceptionType;
    String simpleExceptionType;
    String condition;
    String messagePattern;


    public boolean isStandardJavaException() {
        return exceptionType.startsWith("java.lang.")
                || exceptionType.startsWith("java.io.")
                || exceptionType.startsWith("java.util.");
    }


    public boolean isIllegalArgumentException() {
        return simpleExceptionType.equals("IllegalArgumentException");
    }


    public boolean isRuntimeException() {
        return simpleExceptionType.endsWith("RuntimeException")
                || isIllegalArgumentException()
                || simpleExceptionType.equals("NullPointerException");
    }
}