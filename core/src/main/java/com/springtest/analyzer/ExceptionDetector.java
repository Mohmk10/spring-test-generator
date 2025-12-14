package com.springtest.analyzer;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ExceptionDetector {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionDetector.class);

    static List<String> extractDeclaredExceptions(MethodDeclaration method) {
        return method.getThrownExceptions().stream()
                .map(ReferenceType::asString)
                .collect(Collectors.toList());
    }

    static List<String> detectThrownExceptions(MethodDeclaration method) {
        List<String> exceptions = new ArrayList<>();

        method.getBody().ifPresent(body -> {
            List<ThrowStmt> throwStatements = body.findAll(ThrowStmt.class);
            for (ThrowStmt throwStmt : throwStatements) {
                String exceptionType = extractExceptionType(throwStmt);
                if (exceptionType != null && !exceptions.contains(exceptionType)) {
                    exceptions.add(exceptionType);
                    logger.debug("Detected thrown exception: {} in method: {}", exceptionType, method.getNameAsString());
                }
            }
        });

        return exceptions;
    }

    private static String extractExceptionType(ThrowStmt throwStmt) {
        try {

            var expression = throwStmt.getExpression();

            if (expression.isObjectCreationExpr()) {
                return expression.asObjectCreationExpr().getType().asString();
            }

            if (expression.isNameExpr()) {
                try {
                    return expression.calculateResolvedType().describe();
                } catch (Exception e) {

                    return expression.asNameExpr().getNameAsString();
                }
            }

            return expression.toString();
        } catch (Exception e) {
            logger.debug("Could not extract exception type from throw statement: {}", throwStmt, e);
            return null;
        }
    }

    static List<String> analyzeExceptionPatterns(MethodDeclaration method) {
        List<String> potentialExceptions = new ArrayList<>();

        method.getBody().ifPresent(body -> {

            potentialExceptions.addAll(extractDeclaredExceptions(method));

            potentialExceptions.addAll(detectThrownExceptions(method));

            detectCommonExceptionPatterns(body, potentialExceptions);
        });

        return potentialExceptions.stream().distinct().collect(Collectors.toList());
    }

    private static void detectCommonExceptionPatterns(BlockStmt body, List<String> exceptions) {
        String bodyStr = body.toString();

        if (bodyStr.contains(".get(") || bodyStr.contains(".orElseThrow")) {
            if (!exceptions.contains("NullPointerException")) {
                exceptions.add("NullPointerException");
            }
        }

        if (bodyStr.contains("[") && bodyStr.contains("]")) {
            if (!exceptions.contains("IndexOutOfBoundsException")) {
                exceptions.add("IndexOutOfBoundsException");
            }
        }

        if (bodyStr.contains("Integer.parseInt") || bodyStr.contains("Long.parseLong") ||
            bodyStr.contains("Double.parseDouble")) {
            if (!exceptions.contains("NumberFormatException")) {
                exceptions.add("NumberFormatException");
            }
        }

        if (bodyStr.contains("File") || bodyStr.contains("Stream") ||
            bodyStr.contains("Reader") || bodyStr.contains("Writer")) {
            if (!exceptions.contains("IOException")) {
                exceptions.add("IOException");
            }
        }
    }
}
