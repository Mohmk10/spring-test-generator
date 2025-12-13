package com.springtest.mock;

import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates Mockito stub configurations (when/thenReturn, when/thenThrow).
 * Creates stubbing code for mocked dependencies based on method signatures.
 */
public class StubGenerator {
    private static final Logger logger = LoggerFactory.getLogger(StubGenerator.class);

    /**
     * Generates a when().thenReturn() stub for a method.
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method to stub
     * @return Stub code as Java string
     */
    public String generateWhenThenReturn(String mockName, MethodInfo methodInfo) {
        logger.debug("Generating when/thenReturn stub for method: {}", methodInfo.name());

        String methodCall = generateMethodCall(mockName, methodInfo);
        String returnValue = generateDefaultReturnValue(methodInfo.returnType());

        return String.format("when(%s).thenReturn(%s);",
                methodCall,
                returnValue);
    }

    /**
     * Generates a when().thenThrow() stub for a method.
     *
     * @param mockName      Name of the mock object
     * @param methodInfo    Information about the method to stub
     * @param exceptionType Type of exception to throw
     * @return Stub code as Java string
     */
    public String generateWhenThenThrow(String mockName, MethodInfo methodInfo, String exceptionType) {
        logger.debug("Generating when/thenThrow stub for method: {} with exception: {}",
                methodInfo.name(), exceptionType);

        String methodCall = generateMethodCall(mockName, methodInfo);

        return String.format("when(%s).thenThrow(new %s(\"Test exception\"));",
                methodCall,
                exceptionType);
    }

    /**
     * Generates multiple return values for a method (thenReturn chaining).
     *
     * @param mockName     Name of the mock object
     * @param methodInfo   Information about the method to stub
     * @param returnValues List of return values
     * @return Stub code as Java string
     */
    public String generateMultipleReturns(String mockName, MethodInfo methodInfo, List<String> returnValues) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        String values = String.join(", ", returnValues);

        return String.format("when(%s).thenReturn(%s);",
                methodCall,
                values);
    }

    /**
     * Generates a doThrow() stub for void methods.
     *
     * @param mockName      Name of the mock object
     * @param methodInfo    Information about the method to stub
     * @param exceptionType Type of exception to throw
     * @return Stub code as Java string
     */
    public String generateDoThrow(String mockName, MethodInfo methodInfo, String exceptionType) {
        logger.debug("Generating doThrow stub for void method: {}", methodInfo.name());

        String methodCall = generateMethodCall(mockName, methodInfo);

        return String.format("doThrow(new %s(\"Test exception\")).when(%s);",
                exceptionType,
                methodCall);
    }

    /**
     * Generates a doNothing() stub for void methods.
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method to stub
     * @return Stub code as Java string
     */
    public String generateDoNothing(String mockName, MethodInfo methodInfo) {
        String methodCall = generateMethodCall(mockName, methodInfo);

        return String.format("doNothing().when(%s);", methodCall);
    }

    /**
     * Generates a doAnswer() stub for complex behavior.
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method to stub
     * @return Stub code as Java string
     */
    public String generateDoAnswer(String mockName, MethodInfo methodInfo) {
        String methodCall = generateMethodCall(mockName, methodInfo);

        return String.format("doAnswer(invocation -> {\n" +
                        "    // Custom behavior\n" +
                        "    return %s;\n" +
                        "}).when(%s);",
                generateDefaultReturnValue(methodInfo.returnType()),
                methodCall);
    }

    /**
     * Generates all stubs for a list of methods.
     *
     * @param mockName Name of the mock object
     * @param methods  List of methods to stub
     * @return List of stub statements
     */
    public List<String> generateAllStubs(String mockName, List<MethodInfo> methods) {
        return methods.stream()
                .map(method -> {
                    if (method.returnsVoid()) {
                        return generateDoNothing(mockName, method);
                    } else {
                        return generateWhenThenReturn(mockName, method);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Generates a method call with appropriate argument matchers.
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method
     * @return Method call string with matchers
     */
    private String generateMethodCall(String mockName, MethodInfo methodInfo) {
        if (methodInfo.parameters().isEmpty()) {
            return String.format("%s.%s()", mockName, methodInfo.name());
        }

        String matchers = methodInfo.parameters().stream()
                .map(this::generateArgumentMatcher)
                .collect(Collectors.joining(", "));

        return String.format("%s.%s(%s)", mockName, methodInfo.name(), matchers);
    }

    /**
     * Generates an argument matcher for a parameter.
     *
     * @param parameter Parameter information
     * @return Argument matcher string (e.g., "any()", "anyString()")
     */
    private String generateArgumentMatcher(ParameterInfo parameter) {
        return switch (parameter.type()) {
            case "String" -> "anyString()";
            case "int", "Integer" -> "anyInt()";
            case "long", "Long" -> "anyLong()";
            case "double", "Double" -> "anyDouble()";
            case "boolean", "Boolean" -> "anyBoolean()";
            case "List" -> "anyList()";
            case "Set" -> "anySet()";
            case "Map" -> "anyMap()";
            default -> String.format("any(%s.class)", parameter.type());
        };
    }

    /**
     * Generates a default return value for a given type.
     *
     * @param returnType Return type name
     * @return Default value as string
     */
    private String generateDefaultReturnValue(String returnType) {
        return switch (returnType) {
            case "void" -> null;
            case "String" -> "\"test\"";
            case "int", "Integer" -> "0";
            case "long", "Long" -> "0L";
            case "double", "Double" -> "0.0";
            case "float", "Float" -> "0.0f";
            case "boolean", "Boolean" -> "false";
            case "List" -> "List.of()";
            case "Set" -> "Set.of()";
            case "Map" -> "Map.of()";
            case "Optional" -> "Optional.empty()";
            default -> {
                if (returnType.startsWith("List<")) {
                    yield "List.of()";
                } else if (returnType.startsWith("Set<")) {
                    yield "Set.of()";
                } else if (returnType.startsWith("Map<")) {
                    yield "Map.of()";
                } else if (returnType.startsWith("Optional<")) {
                    yield "Optional.empty()";
                }
                yield "null";
            }
        };
    }

    /**
     * Generates required imports for stubbing.
     *
     * @return List of import statements
     */
    public List<String> generateImports() {
        List<String> imports = new ArrayList<>();
        imports.add("import static org.mockito.Mockito.*;");
        imports.add("import static org.mockito.ArgumentMatchers.*;");
        imports.add("import java.util.List;");
        imports.add("import java.util.Set;");
        imports.add("import java.util.Map;");
        imports.add("import java.util.Optional;");
        return imports;
    }

    /**
     * Generates a stub with specific argument values instead of matchers.
     *
     * @param mockName      Name of the mock object
     * @param methodInfo    Information about the method to stub
     * @param argumentValues Specific argument values
     * @return Stub code as Java string
     */
    public String generateStubWithArguments(String mockName, MethodInfo methodInfo, List<String> argumentValues) {
        String args = String.join(", ", argumentValues);
        String methodCall = String.format("%s.%s(%s)", mockName, methodInfo.name(), args);
        String returnValue = generateDefaultReturnValue(methodInfo.returnType());

        return String.format("when(%s).thenReturn(%s);",
                methodCall,
                returnValue);
    }
}
