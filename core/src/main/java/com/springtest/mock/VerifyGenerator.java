package com.springtest.mock;

import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates Mockito verification statements.
 * Creates verify(), verifyNoInteractions(), verifyNoMoreInteractions(), and ArgumentCaptor code.
 */
public class VerifyGenerator {
    private static final Logger logger = LoggerFactory.getLogger(VerifyGenerator.class);

    /**
     * Generates a verify() statement for a method call.
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method to verify
     * @return Verify statement as Java string
     */
    public String generateVerify(String mockName, MethodInfo methodInfo) {
        logger.debug("Generating verify statement for method: {}", methodInfo.name());

        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s);", methodCall);
    }

    /**
     * Generates a verify() statement with specific times.
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method to verify
     * @param times      Number of times the method should be called
     * @return Verify statement as Java string
     */
    public String generateVerifyTimes(String mockName, MethodInfo methodInfo, int times) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, times(%d));", methodCall, times);
    }

    /**
     * Generates a verify() statement with never().
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method to verify
     * @return Verify statement as Java string
     */
    public String generateVerifyNever(String mockName, MethodInfo methodInfo) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, never());", methodCall);
    }

    /**
     * Generates a verify() statement with atLeastOnce().
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method to verify
     * @return Verify statement as Java string
     */
    public String generateVerifyAtLeastOnce(String mockName, MethodInfo methodInfo) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, atLeastOnce());", methodCall);
    }

    /**
     * Generates a verify() statement with atLeast(n).
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method to verify
     * @param times      Minimum number of invocations
     * @return Verify statement as Java string
     */
    public String generateVerifyAtLeast(String mockName, MethodInfo methodInfo, int times) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, atLeast(%d));", methodCall, times);
    }

    /**
     * Generates a verify() statement with atMost(n).
     *
     * @param mockName   Name of the mock object
     * @param methodInfo Information about the method to verify
     * @param times      Maximum number of invocations
     * @return Verify statement as Java string
     */
    public String generateVerifyAtMost(String mockName, MethodInfo methodInfo, int times) {
        String methodCall = generateMethodCall(mockName, methodInfo);
        return String.format("verify(%s, atMost(%d));", methodCall, times);
    }

    /**
     * Generates verifyNoInteractions() statement.
     *
     * @param mockNames List of mock names
     * @return VerifyNoInteractions statement as Java string
     */
    public String generateVerifyNoInteractions(List<String> mockNames) {
        String mocks = String.join(", ", mockNames);
        return String.format("verifyNoInteractions(%s);", mocks);
    }

    /**
     * Generates verifyNoMoreInteractions() statement.
     *
     * @param mockNames List of mock names
     * @return VerifyNoMoreInteractions statement as Java string
     */
    public String generateVerifyNoMoreInteractions(List<String> mockNames) {
        String mocks = String.join(", ", mockNames);
        return String.format("verifyNoMoreInteractions(%s);", mocks);
    }

    /**
     * Generates an ArgumentCaptor declaration and capture code.
     *
     * @param parameterType Type of the parameter to capture
     * @param parameterName Name for the captor variable
     * @return ArgumentCaptor code as Java string
     */
    public String generateArgumentCaptor(String parameterType, String parameterName) {
        logger.debug("Generating ArgumentCaptor for type: {}", parameterType);

        return String.format("ArgumentCaptor<%s> %sCaptor = ArgumentCaptor.forClass(%s.class);",
                parameterType,
                parameterName,
                parameterType);
    }

    /**
     * Generates code to capture an argument during verification.
     *
     * @param mockName      Name of the mock object
     * @param methodInfo    Information about the method
     * @param captorName    Name of the captor variable
     * @param parameterIndex Index of the parameter to capture
     * @return Verification code with argument capture
     */
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

    /**
     * Generates code to get captured value.
     *
     * @param captorName Name of the captor variable
     * @return Code to retrieve captured value
     */
    public String generateGetCapturedValue(String captorName) {
        return String.format("%s.getValue()", captorName);
    }

    /**
     * Generates code to get all captured values.
     *
     * @param captorName Name of the captor variable
     * @return Code to retrieve all captured values
     */
    public String generateGetAllCapturedValues(String captorName) {
        return String.format("%s.getAllValues()", captorName);
    }

    /**
     * Generates a complete ArgumentCaptor usage example.
     *
     * @param mockName       Name of the mock object
     * @param methodInfo     Information about the method
     * @param parameterIndex Index of the parameter to capture
     * @return Complete captor usage code
     */
    public String generateCompleteCaptor(String mockName, MethodInfo methodInfo, int parameterIndex) {
        ParameterInfo param = methodInfo.parameters().get(parameterIndex);
        String captorName = param.name() + "Captor";

        StringBuilder code = new StringBuilder();

        // Captor declaration
        code.append(generateArgumentCaptor(param.type(), param.name())).append("\n");

        // Verification with capture
        code.append(generateVerifyWithCaptor(mockName, methodInfo, captorName, parameterIndex)).append("\n");

        // Get captured value
        code.append(String.format("%s captured%s = %s;",
                param.type(),
                capitalize(param.name()),
                generateGetCapturedValue(captorName)));

        return code.toString();
    }

    /**
     * Generates required imports for verification.
     *
     * @return List of import statements
     */
    public List<String> generateImports() {
        List<String> imports = new ArrayList<>();
        imports.add("import static org.mockito.Mockito.*;");
        imports.add("import static org.mockito.ArgumentMatchers.*;");
        imports.add("import org.mockito.ArgumentCaptor;");
        return imports;
    }

    /**
     * Generates InOrder verification for sequential calls.
     *
     * @param mockName  Name of the mock object
     * @param methods   List of methods in expected order
     * @return InOrder verification code
     */
    public String generateInOrderVerification(String mockName, List<MethodInfo> methods) {
        StringBuilder code = new StringBuilder();
        code.append(String.format("InOrder inOrder = inOrder(%s);\n", mockName));

        for (MethodInfo method : methods) {
            String methodCall = generateMethodCall(mockName, method);
            code.append(String.format("inOrder.verify(%s);\n", methodCall));
        }

        return code.toString();
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
     * @return Argument matcher string
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
     * Capitalizes the first letter of a string.
     *
     * @param str Input string
     * @return Capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
