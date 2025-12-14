package com.springtest.mock;

import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StubGenerator {
    private static final Logger logger = LoggerFactory.getLogger(StubGenerator.class);

    public String generateWhenThenReturn(String mockName, MethodInfo methodInfo) {
        logger.debug("Generating when/thenReturn stub for method: {}", methodInfo.name());

        String methodCall = generateMethodCall(mockName, methodInfo);
        String returnValue = generateDefaultReturnValue(methodInfo.returnType());

        return String.format("when(%s).thenReturn(%s);",
                methodCall,
                returnValue);
    }

    public String generateWhenThenThrow(String mockName, MethodInfo methodInfo, String exceptionType) {
        logger.debug("Generating when/thenThrow stub for method: {} with exception: {}",
                methodInfo.name(), exceptionType);

        String methodCall = generateMethodCall(mockName, methodInfo);

        return String.format("when(%s).thenThrow(new %s(\"Test exception\"));",
                methodCall,
                exceptionType);
    }

    public String generateMultipleReturns(String mockName, MethodInfo methodInfo, List<String> returnValues) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        String values = String.join(", ", returnValues);

        return String.format("when(%s).thenReturn(%s);",
                methodCall,
                values);
    }

    public String generateDoThrow(String mockName, MethodInfo methodInfo, String exceptionType) {
        logger.debug("Generating doThrow stub for void method: {}", methodInfo.name());

        String methodCall = generateMethodCall(mockName, methodInfo);

        return String.format("doThrow(new %s(\"Test exception\")).when(%s);",
                exceptionType,
                methodCall);
    }

    public String generateDoNothing(String mockName, MethodInfo methodInfo) {
        String methodCall = generateMethodCall(mockName, methodInfo);

        return String.format("doNothing().when(%s);", methodCall);
    }

    public String generateDoAnswer(String mockName, MethodInfo methodInfo) {
        String methodCall = generateMethodCall(mockName, methodInfo);

        return String.format("doAnswer(invocation -> {\n" +
                        "    // Custom behavior\n" +
                        "    return %s;\n" +
                        "}).when(%s);",
                generateDefaultReturnValue(methodInfo.returnType()),
                methodCall);
    }

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

    private String generateMethodCall(String mockName, MethodInfo methodInfo) {
        if (methodInfo.parameters().isEmpty()) {
            return String.format("%s.%s()", mockName, methodInfo.name());
        }

        String matchers = methodInfo.parameters().stream()
                .map(this::generateArgumentMatcher)
                .collect(Collectors.joining(", "));

        return String.format("%s.%s(%s)", mockName, methodInfo.name(), matchers);
    }

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

    public String generateStubWithArguments(String mockName, MethodInfo methodInfo, List<String> argumentValues) {
        String args = String.join(", ", argumentValues);
        String methodCall = String.format("%s.%s(%s)", mockName, methodInfo.name(), args);
        String returnValue = generateDefaultReturnValue(methodInfo.returnType());

        return String.format("when(%s).thenReturn(%s);",
                methodCall,
                returnValue);
    }
}
