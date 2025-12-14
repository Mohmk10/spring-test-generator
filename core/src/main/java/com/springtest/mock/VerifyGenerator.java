package com.springtest.mock;

import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VerifyGenerator {
    private static final Logger logger = LoggerFactory.getLogger(VerifyGenerator.class);

    public String generateVerify(String mockName, MethodInfo methodInfo) {
        logger.debug("Generating verify statement for method: {}", methodInfo.name());

        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s);", methodCall);
    }

    public String generateVerifyTimes(String mockName, MethodInfo methodInfo, int times) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, times(%d));", methodCall, times);
    }

    public String generateVerifyNever(String mockName, MethodInfo methodInfo) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, never());", methodCall);
    }

    public String generateVerifyAtLeastOnce(String mockName, MethodInfo methodInfo) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, atLeastOnce());", methodCall);
    }

    public String generateVerifyAtLeast(String mockName, MethodInfo methodInfo, int times) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, atLeast(%d));", methodCall, times);
    }

    public String generateVerifyAtMost(String mockName, MethodInfo methodInfo, int times) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, atMost(%d));", methodCall, times);
    }

    public String generateVerifyNoInteractions(List<String> mockNames) {
        String mocks = String.join(", ", mockNames);
        return String.format("verifyNoInteractions(%s);", mocks);
    }

    public String generateVerifyNoMoreInteractions(List<String> mockNames) {
        String mocks = String.join(", ", mockNames);
        return String.format("verifyNoMoreInteractions(%s);", mocks);
    }

    public String generateArgumentCaptor(String parameterType, String parameterName) {
        logger.debug("Generating ArgumentCaptor for type: {}", parameterType);

        return String.format("ArgumentCaptor<%s> %sCaptor = ArgumentCaptor.forClass(%s.class);",
                parameterType,
                parameterName,
                parameterType);
    }

    public String generateVerifyWithCaptor(String mockName, MethodInfo methodInfo,
                                           String captorName, int parameterIndex) {
        List<String> matchers = new ArrayList<>();

        for (int i = 0; i < methodInfo.parameters().size(); i++) {
            if (i == parameterIndex) {
                matchers.add(captorName + ".capture()");
            } else {
                ParameterInfo param = methodInfo.parameters().get(i);
                matchers.add(generateArgumentMatcher(param));
            }
        }

        String args = String.join(", ", matchers);
        return String.format("verify(%s).%s(%s);",
                mockName,
                methodInfo.name(),
                args);
    }

    public String generateGetCapturedValue(String captorName) {
        return String.format("%s.getValue()", captorName);
    }

    public String generateGetAllCapturedValues(String captorName) {
        return String.format("%s.getAllValues()", captorName);
    }

    public String generateCompleteCaptor(String mockName, MethodInfo methodInfo, int parameterIndex) {
        ParameterInfo param = methodInfo.parameters().get(parameterIndex);
        String captorName = param.name() + "Captor";

        StringBuilder code = new StringBuilder();

        code.append(generateArgumentCaptor(param.type(), param.name())).append("\n");

        code.append(generateVerifyWithCaptor(mockName, methodInfo, captorName, parameterIndex)).append("\n");

        code.append(String.format("%s captured%s = %s;",
                param.type(),
                capitalize(param.name()),
                generateGetCapturedValue(captorName)));

        return code.toString();
    }

    public List<String> generateImports() {
        List<String> imports = new ArrayList<>();
        imports.add("import static org.mockito.Mockito.*;");
        imports.add("import static org.mockito.ArgumentMatchers.*;");
        imports.add("import org.mockito.ArgumentCaptor;");
        return imports;
    }

    public String generateInOrderVerification(String mockName, List<MethodInfo> methods) {
        StringBuilder code = new StringBuilder();
        code.append(String.format("InOrder inOrder = inOrder(%s);\n", mockName));

        for (MethodInfo method : methods) {
            String methodCall = generateMethodCall(mockName, method);
            code.append(String.format("inOrder.verify(%s);\n", methodCall));
        }

        return code.toString();
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

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
