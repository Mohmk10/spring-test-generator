package com.springtest.generator;

import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExceptionTestGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionTestGenerator.class);

    public List<String> generateExceptionTests(MethodInfo methodInfo, String instanceName) {
        logger.debug("Generating exception tests for method: {}", methodInfo.name());

        List<String> testMethods = new ArrayList<>();

        for (String exception : methodInfo.thrownExceptions()) {
            testMethods.add(generateExceptionTest(methodInfo, instanceName, exception, true));
        }

        for (String exception : methodInfo.possibleExceptions()) {

            if (!methodInfo.thrownExceptions().contains(exception)) {
                testMethods.add(generateExceptionTest(methodInfo, instanceName, exception, false));
            }
        }

        return testMethods;
    }

    public String generateExceptionTest(MethodInfo methodInfo, String instanceName, String exceptionType, boolean isDeclared) {
        logger.debug("Generating exception test for {} throwing {}", methodInfo.name(), exceptionType);

        StringBuilder test = new StringBuilder();

        String simpleName = getSimpleName(exceptionType);
        String testName = String.format("test%s_Throws%s",
                capitalize(methodInfo.name()),
                simpleName);

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append(generateArrangeForException(methodInfo, exceptionType));
        test.append("\n");

        test.append("        assertThatThrownBy(() -> ");
        test.append(generateMethodCall(instanceName, methodInfo));
        test.append(")\n");
        test.append("            .isInstanceOf(").append(exceptionType).append(".class)");

        test.append("\n");
        test.append("            .hasMessageContaining(\"").append(getExpectedMessage(exceptionType)).append("\")");

        if (hasCommonCause(exceptionType)) {
            test.append("\n");
            test.append("            .hasCauseInstanceOf(").append(getCommonCause(exceptionType)).append(".class)");
        }

        test.append(";\n");
        test.append("    }\n");

        return test.toString();
    }

    public String generateExceptionMessageTest(MethodInfo methodInfo, String instanceName, String exceptionType) {
        StringBuilder test = new StringBuilder();

        String simpleName = getSimpleName(exceptionType);
        String testName = String.format("test%s_%sMessage",
                capitalize(methodInfo.name()),
                simpleName);

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append(generateArrangeForException(methodInfo, exceptionType));
        test.append("\n");

        test.append("        assertThatThrownBy(() -> ");
        test.append(generateMethodCall(instanceName, methodInfo));
        test.append(")\n");
        test.append("            .isInstanceOf(").append(exceptionType).append(".class)\n");
        test.append("            .hasMessage(\"Expected error message\")\n");
        test.append("            .hasNoCause();\n");

        test.append("    }\n");

        return test.toString();
    }

    public String generateExceptionCauseTest(MethodInfo methodInfo, String instanceName, String exceptionType, String causeType) {
        StringBuilder test = new StringBuilder();

        String simpleName = getSimpleName(exceptionType);
        String causeSimpleName = getSimpleName(causeType);
        String testName = String.format("test%s_%sWithCause",
                capitalize(methodInfo.name()),
                simpleName);

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append(generateArrangeForException(methodInfo, exceptionType));
        test.append("\n");

        test.append("        assertThatThrownBy(() -> ");
        test.append(generateMethodCall(instanceName, methodInfo));
        test.append(")\n");
        test.append("            .isInstanceOf(").append(exceptionType).append(".class)\n");
        test.append("            .hasCauseInstanceOf(").append(causeType).append(".class)\n");
        test.append("            .hasRootCauseInstanceOf(").append(causeType).append(".class)\n");
        test.append("            .hasMessageContaining(\"").append(getExpectedMessage(exceptionType)).append("\");\n");

        test.append("    }\n");

        return test.toString();
    }

    public String generateMultipleExceptionsTest(MethodInfo methodInfo, String instanceName) {
        if (methodInfo.thrownExceptions().isEmpty() && methodInfo.possibleExceptions().isEmpty()) {
            return "";
        }

        StringBuilder test = new StringBuilder();

        String testName = String.format("test%s_ThrowsAppropriateExceptions",
                capitalize(methodInfo.name()));

        test.append("    @Test\n");
        test.append("    void ").append(testName).append("() {\n");
        test.append("\n");

        test.append("        assertThatThrownBy(() -> ");
        test.append(generateMethodCall(instanceName, methodInfo));
        test.append(")\n");
        test.append("            .isInstanceOfAny(\n");

        List<String> allExceptions = new ArrayList<>();
        allExceptions.addAll(methodInfo.thrownExceptions());
        allExceptions.addAll(methodInfo.possibleExceptions());

        String exceptionClasses = allExceptions.stream()
                .distinct()
                .map(e -> e + ".class")
                .collect(Collectors.joining(",\n                "));

        test.append("                ").append(exceptionClasses).append("\n");
        test.append("            );\n");

        test.append("    }\n");

        return test.toString();
    }

    private String generateArrangeForException(MethodInfo methodInfo, String exceptionType) {
        StringBuilder arrange = new StringBuilder();

        String simpleName = getSimpleName(exceptionType);

        if (simpleName.contains("NotFound") || simpleName.contains("NoSuchElement")) {
        } else if (simpleName.contains("IllegalArgument") || simpleName.contains("Validation")) {
        } else if (simpleName.contains("IllegalState")) {
        } else if (simpleName.contains("NullPointer")) {
        } else {
        }

        return arrange.toString();
    }

    private String generateMethodCall(String instanceName, MethodInfo methodInfo) {
        if (methodInfo.parameters().isEmpty()) {
            return instanceName + "." + methodInfo.name() + "()";
        }

        String args = methodInfo.parameters().stream()
                .map(this::getExceptionTriggeringValue)
                .collect(Collectors.joining(", "));

        return instanceName + "." + methodInfo.name() + "(" + args + ")";
    }

    private String getExceptionTriggeringValue(ParameterInfo param) {

        if (param.required()) {
            return "null";
        }

        return switch (param.type()) {
            case "String" -> "\"invalid\"";
            case "int", "Integer" -> "-1";
            case "long", "Long" -> "-1L";
            case "double", "Double" -> "-1.0";
            case "float", "Float" -> "-1.0f";
            case "boolean", "Boolean" -> "false";
            case "List", "Set", "Collection" -> "List.of()";
            case "Map" -> "Map.of()";
            default -> "null";
        };
    }

    private String getSimpleName(String qualifiedName) {
        if (qualifiedName.contains(".")) {
            return qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
        }
        return qualifiedName;
    }

    private String getExpectedMessage(String exceptionType) {
        String simpleName = getSimpleName(exceptionType);

        return switch (simpleName) {
            case "IllegalArgumentException" -> "Invalid argument";
            case "IllegalStateException" -> "Invalid state";
            case "NullPointerException" -> "null";
            case "EntityNotFoundException", "NoSuchElementException" -> "not found";
            case "ValidationException" -> "validation failed";
            case "AccessDeniedException" -> "Access denied";
            case "IOException" -> "I/O error";
            default -> "error";
        };
    }

    private boolean hasCommonCause(String exceptionType) {
        String simpleName = getSimpleName(exceptionType);
        return simpleName.contains("Wrapped") ||
               simpleName.contains("Runtime") ||
               simpleName.equals("DataAccessException") ||
               simpleName.equals("ServiceException");
    }

    private String getCommonCause(String exceptionType) {
        String simpleName = getSimpleName(exceptionType);

        if (simpleName.contains("DataAccess")) {
            return "SQLException";
        } else if (simpleName.contains("Service")) {
            return "RuntimeException";
        }

        return "Exception";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public List<String> generateImports() {
        List<String> imports = new ArrayList<>();
        imports.add("import static org.assertj.core.api.Assertions.*;");
        imports.add("import java.util.List;");
        imports.add("import java.util.Map;");
        return imports;
    }

    public boolean hasExceptionsToTest(MethodInfo methodInfo) {
        return !methodInfo.thrownExceptions().isEmpty() || !methodInfo.possibleExceptions().isEmpty();
    }
}
