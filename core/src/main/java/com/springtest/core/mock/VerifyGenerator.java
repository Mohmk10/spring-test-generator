package com.springtest.core.mock;

import com.springtest.core.model.MethodInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VerifyGenerator {

    public List<String> generateVerifications(String fieldName, MethodInfo method) {
        List<String> verifications = new ArrayList<>();

        String methodName = method.getName();
        int paramCount = method.getParameters().size();

        if (paramCount == 0) {
            verifications.add(String.format("verify(%s).%s();", fieldName, methodName));
        } else {
            String matchers = generateMatchers(paramCount);
            verifications.add(String.format("verify(%s).%s(%s);", fieldName, methodName, matchers));
        }

        return verifications;
    }

    public String generateVerifyNoInteractions(String fieldName) {
        return String.format("verifyNoInteractions(%s);", fieldName);
    }

    public String generateVerifyNoMoreInteractions(String fieldName) {
        return String.format("verifyNoMoreInteractions(%s);", fieldName);
    }

    public String generateVerifyTimes(String fieldName, String methodName, int times) {
        return String.format("verify(%s, times(%d)).%s(any());", fieldName, times, methodName);
    }

    public String generateVerifyNever(String fieldName, String methodName) {
        return String.format("verify(%s, never()).%s(any());", fieldName, methodName);
    }

    private String generateMatchers(int count) {
        List<String> matchers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            matchers.add("any()");
        }
        return String.join(", ", matchers);
    }
}