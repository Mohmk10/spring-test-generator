package com.springtest.core.analyzer;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ReferenceType;
import com.springtest.core.model.ExceptionInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExceptionDetector {

    public List<ExceptionInfo> detectDeclaredExceptions(MethodDeclaration method) {
        return method.getThrownExceptions().stream()
                .map(this::createExceptionInfo)
                .toList();
    }

    public List<ExceptionInfo> detectThrownExceptions(MethodDeclaration method) {
        List<ExceptionInfo> exceptions = new ArrayList<>();

        method.findAll(ThrowStmt.class).forEach(throwStmt -> {
            String exceptionType = throwStmt.getExpression().toString();

            if (exceptionType.contains("new ")) {
                exceptionType = exceptionType
                        .replace("new ", "")
                        .replaceAll("\\(.*\\)", "")
                        .trim();
            }

            String simpleType = extractSimpleType(exceptionType);
            String message = extractExceptionMessage(throwStmt.getExpression().toString());

            exceptions.add(ExceptionInfo.builder()
                    .exceptionType(exceptionType)
                    .simpleExceptionType(simpleType)
                    .condition("unknown")
                    .messagePattern(message)
                    .build());
        });

        return exceptions;
    }

    private ExceptionInfo createExceptionInfo(ReferenceType exceptionType) {
        String fullType = exceptionType.asString();
        String simpleType = extractSimpleType(fullType);

        return ExceptionInfo.builder()
                .exceptionType(fullType)
                .simpleExceptionType(simpleType)
                .condition("unknown")
                .messagePattern(null)
                .build();
    }

    private String extractSimpleType(String fullType) {
        String withoutGenerics = fullType.replaceAll("<.*>", "");

        if (withoutGenerics.contains(".")) {
            return withoutGenerics.substring(withoutGenerics.lastIndexOf(".") + 1);
        }

        return withoutGenerics;
    }

    private String extractExceptionMessage(String throwExpression) {
        int messageStart = throwExpression.indexOf("\"");
        if (messageStart == -1) {
            return null;
        }

        int messageEnd = throwExpression.indexOf("\"", messageStart + 1);
        if (messageEnd == -1) {
            return null;
        }

        return throwExpression.substring(messageStart + 1, messageEnd);
    }
}