package com.springtest.generator;

import com.springtest.assertion.AssertionGenerator;
import com.springtest.mock.MockGenerator;
import com.springtest.mock.StubGenerator;
import com.springtest.model.ClassInfo;
import com.springtest.model.ClassType;
import com.springtest.model.FieldInfo;
import com.springtest.model.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceTestGenerator implements TestGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ServiceTestGenerator.class);

    private final MockGenerator mockGenerator;
    private final StubGenerator stubGenerator;
    private final AssertionGenerator assertionGenerator;

    public ServiceTestGenerator() {
        this.mockGenerator = new MockGenerator(false);
        this.stubGenerator = new StubGenerator();
        this.assertionGenerator = new AssertionGenerator();
    }

    public ServiceTestGenerator(MockGenerator mockGenerator, StubGenerator stubGenerator, AssertionGenerator assertionGenerator) {
        this.mockGenerator = mockGenerator;
        this.stubGenerator = stubGenerator;
        this.assertionGenerator = assertionGenerator;
    }

    @Override
    public String generateTest(ClassInfo classInfo) {
        if (classInfo == null) {
            throw new IllegalArgumentException("ClassInfo cannot be null");
        }
        if (!supports(classInfo)) {
            throw new IllegalArgumentException("ServiceTestGenerator does not support class type: " + classInfo.classType());
        }

        logger.info("Generating service test for: {}", classInfo.simpleName());

        StringBuilder testClass = new StringBuilder();

        testClass.append(generatePackageDeclaration(classInfo));
        testClass.append("\n\n");

        testClass.append(generateImports(classInfo));
        testClass.append("\n\n");

        testClass.append(generateClassDeclaration(classInfo));
        testClass.append("\n\n");

        testClass.append(generateMockFields(classInfo));
        testClass.append("\n\n");

        testClass.append(generateTestMethods(classInfo));
        testClass.append("\n");

        testClass.append("}\n");

        return testClass.toString();
    }

    @Override
    public boolean supports(ClassInfo classInfo) {
        return classInfo != null && classInfo.classType() == ClassType.SERVICE;
    }

    private String generatePackageDeclaration(ClassInfo classInfo) {
        return "package " + classInfo.packageName() + ";";
    }

    private String generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.BeforeEach;");
        imports.add("import org.junit.jupiter.api.extension.ExtendWith;");

        imports.add("import org.mockito.Mock;");
        imports.add("import org.mockito.InjectMocks;");
        imports.add("import org.mockito.junit.jupiter.MockitoExtension;");
        imports.add("import static org.mockito.Mockito.*;");
        imports.add("import static org.mockito.ArgumentMatchers.*;");

        imports.add("import static org.assertj.core.api.Assertions.*;");

        imports.add("import java.util.List;");
        imports.add("import java.util.Optional;");

        imports.add("import " + classInfo.qualifiedName() + ";");

        for (FieldInfo field : classInfo.getInjectedFields()) {
            if (field.qualifiedType() != null && !field.qualifiedType().startsWith("java.")) {
                imports.add("import " + field.qualifiedType() + ";");
            }
        }

        return imports.stream()
                .distinct()
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private String generateClassDeclaration(ClassInfo classInfo) {
        return String.format("@ExtendWith(MockitoExtension.class)\nclass %sTest {",
                classInfo.simpleName());
    }

    private String generateMockFields(ClassInfo classInfo) {
        StringBuilder fields = new StringBuilder();

        for (FieldInfo field : classInfo.getInjectedFields()) {
            fields.append("    ").append(mockGenerator.generateMockForField(field).indent(4).trim());
            fields.append("\n\n");
        }

        fields.append("    ").append(mockGenerator.generateInjectMocks(classInfo).indent(4).trim());

        return fields.toString();
    }

    private String generateTestMethods(ClassInfo classInfo) {
        StringBuilder methods = new StringBuilder();

        List<MethodInfo> publicMethods = classInfo.methods().stream()
                .filter(m -> !m.isStatic())
                .filter(m -> !m.isGetter())
                .filter(m -> !m.isSetter())
                .toList();

        for (MethodInfo method : publicMethods) {
            methods.append(generateTestMethod(classInfo, method));
            methods.append("\n");

            if (method.throwsExceptions()) {
                methods.append(generateExceptionTestMethod(classInfo, method));
                methods.append("\n");
            }
        }

        if (publicMethods.isEmpty()) {
            methods.append(generateBasicTest(classInfo));
        }

        return methods.toString();
    }

    private String generateTestMethod(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "test" + capitalize(methodInfo.name());
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() {\n");

        test.append(generateArrangeSection(classInfo, methodInfo));
        test.append("\n");

        test.append(generateActSection(classInfo, methodInfo));
        test.append("\n");

        test.append(generateAssertSection(methodInfo));

        test.append("    }\n");

        return test.toString();
    }

    private String generateArrangeSection(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder arrange = new StringBuilder();

        for (FieldInfo field : classInfo.getInjectedFields()) {
            String mockName = field.name();

        }

        return arrange.toString();
    }

    private String generateActSection(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder act = new StringBuilder();

        String instanceName = getInstanceName(classInfo);
        String methodCall = generateMethodCall(instanceName, methodInfo);

        if (!methodInfo.returnsVoid()) {
            String resultType = methodInfo.returnType();
            act.append("        ").append(resultType).append(" result = ").append(methodCall).append(";\n");
        } else {
            act.append("        ").append(methodCall).append(";\n");
        }

        return act.toString();
    }

    private String generateAssertSection(MethodInfo methodInfo) {
        StringBuilder assertSection = new StringBuilder();

        if (!methodInfo.returnsVoid()) {
            String assertion = assertionGenerator.generateMethodAssertions(methodInfo);
            assertSection.append("        ").append(assertion.replace("\n", "\n        ")).append("\n");
        }


        return assertSection.toString();
    }

    private String generateExceptionTestMethod(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "test" + capitalize(methodInfo.name()) + "_ThrowsException";
        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() {\n");

        String instanceName = getInstanceName(classInfo);
        String methodCall = generateMethodCall(instanceName, methodInfo);

        List<String> exceptions = methodInfo.thrownExceptions();
        String exceptionType = exceptions.isEmpty() ? "RuntimeException" : exceptions.get(0);

        test.append("        assertThatThrownBy(() -> ").append(methodCall).append(")\n");
        test.append("            .isInstanceOf(").append(exceptionType).append(".class);\n");

        test.append("    }\n");

        return test.toString();
    }

    private String generateBasicTest(ClassInfo classInfo) {
        StringBuilder test = new StringBuilder();

        test.append("    @Test\n");
        test.append("    void testServiceInitialization() {\n");
        String instanceName = getInstanceName(classInfo);
        test.append("        assertThat(").append(instanceName).append(").isNotNull();\n");
        test.append("    }\n");

        return test.toString();
    }

    private String generateMethodCall(String instanceName, MethodInfo methodInfo) {
        if (methodInfo.parameters().isEmpty()) {
            return instanceName + "." + methodInfo.name() + "()";
        }

        String args = methodInfo.parameters().stream()
                .map(p -> getDefaultValue(p.type()))
                .collect(Collectors.joining(", "));

        return instanceName + "." + methodInfo.name() + "(" + args + ")";
    }

    private String getInstanceName(ClassInfo classInfo) {
        String simpleName = classInfo.simpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String getDefaultValue(String type) {
        return switch (type) {
            case "String" -> "\"test\"";
            case "int", "Integer" -> "1";
            case "long", "Long" -> "1L";
            case "double", "Double" -> "1.0";
            case "float", "Float" -> "1.0f";
            case "boolean", "Boolean" -> "true";
            case "List" -> "List.of()";
            case "Set" -> "Set.of()";
            case "Map" -> "Map.of()";
            default -> "null";
        };
    }
}
