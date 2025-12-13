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

/**
 * Detects exceptions in method declarations and method bodies.
 * Package-private utility class for internal use.
 */
class ExceptionDetector {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionDetector.class);

    /**
     * Extracts declared exceptions from a method's throws clause.
     *
     * @param method JavaParser method declaration
     * @return List of exception type names
     */
    static List<String> extractDeclaredExceptions(MethodDeclaration method) {
        return method.getThrownExceptions().stream()
                .map(ReferenceType::asString)
                .collect(Collectors.toList());
    }

    /**
     * Detects exceptions thrown in a method body by analyzing throw statements.
     *
     * @param method JavaParser method declaration
     * @return List of exception type names found in throw statements
     */
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

    /**
     * Extracts the exception type from a throw statement.
     *
     * @param throwStmt JavaParser throw statement
     * @return Exception type name or null if cannot be determined
     */
    private static String extractExceptionType(ThrowStmt throwStmt) {
        try {
            // Try to resolve the type of the expression being thrown
            var expression = throwStmt.getExpression();

            // Handle "throw new ExceptionType(...)" pattern
            if (expression.isObjectCreationExpr()) {
                return expression.asObjectCreationExpr().getType().asString();
            }

            // Handle "throw exceptionVariable" pattern
            if (expression.isNameExpr()) {
                try {
                    return expression.calculateResolvedType().describe();
                } catch (Exception e) {
                    // If resolution fails, return the variable name
                    return expression.asNameExpr().getNameAsString();
                }
            }

            // For other expressions, try to get the type as string
            return expression.toString();
        } catch (Exception e) {
            logger.debug("Could not extract exception type from throw statement: {}", throwStmt, e);
            return null;
        }
    }

    /**
     * Analyzes a method body to detect common exception patterns.
     * This includes both explicit throw statements and method calls that might throw exceptions.
     *
     * @param method JavaParser method declaration
     * @return List of potential exception types
     */
    static List<String> analyzeExceptionPatterns(MethodDeclaration method) {
        List<String> potentialExceptions = new ArrayList<>();

        method.getBody().ifPresent(body -> {
            // Add declared exceptions
            potentialExceptions.addAll(extractDeclaredExceptions(method));

            // Add thrown exceptions from body
            potentialExceptions.addAll(detectThrownExceptions(method));

            // Detect common patterns that might throw exceptions
            detectCommonExceptionPatterns(body, potentialExceptions);
        });

        return potentialExceptions.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Detects common code patterns that might throw exceptions.
     *
     * @param body Method body to analyze
     * @param exceptions List to add detected exception types to
     */
    private static void detectCommonExceptionPatterns(BlockStmt body, List<String> exceptions) {
        String bodyStr = body.toString();

        // Null pointer dereference patterns
        if (bodyStr.contains(".get(") || bodyStr.contains(".orElseThrow")) {
            if (!exceptions.contains("NullPointerException")) {
                exceptions.add("NullPointerException");
            }
        }

        // Array/List access patterns
        if (bodyStr.contains("[") && bodyStr.contains("]")) {
            if (!exceptions.contains("IndexOutOfBoundsException")) {
                exceptions.add("IndexOutOfBoundsException");
            }
        }

        // Number parsing patterns
        if (bodyStr.contains("Integer.parseInt") || bodyStr.contains("Long.parseLong") ||
            bodyStr.contains("Double.parseDouble")) {
            if (!exceptions.contains("NumberFormatException")) {
                exceptions.add("NumberFormatException");
            }
        }

        // IO operations
        if (bodyStr.contains("File") || bodyStr.contains("Stream") ||
            bodyStr.contains("Reader") || bodyStr.contains("Writer")) {
            if (!exceptions.contains("IOException")) {
                exceptions.add("IOException");
            }
        }
    }
}
