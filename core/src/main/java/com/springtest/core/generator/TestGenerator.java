package com.springtest.core.generator;

import com.springtest.core.assertion.AssertionGenerator;
import com.springtest.core.config.GeneratorConfig;
import com.springtest.core.mock.MockGenerator;
import com.springtest.core.mock.StubGenerator;
import com.springtest.core.mock.VerifyGenerator;
import com.springtest.core.model.ClassInfo;
import com.springtest.core.model.MethodInfo;
import com.springtest.core.model.TestCase;
import com.springtest.core.model.TestSuite;
import com.springtest.core.naming.NamingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class TestGenerator {

    protected final GeneratorConfig config;
    protected final NamingStrategy namingStrategy;
    protected final MockGenerator mockGenerator;
    protected final StubGenerator stubGenerator;
    protected final VerifyGenerator verifyGenerator;
    protected final AssertionGenerator assertionGenerator;

    public TestSuite generateTestSuite(ClassInfo classInfo) {
        log.info("Generating test suite for: {}", classInfo.getSimpleName());

        List<TestCase> testCases = new ArrayList<>();

        for (MethodInfo method : classInfo.getMethods()) {
            testCases.addAll(generateTestCasesForMethod(method, classInfo));
        }

        List<String> imports = generateImports(classInfo);
        List<String> classAnnotations = generateClassAnnotations(classInfo);
        List<String> testFields = generateTestFields(classInfo);
        List<String> setupMethods = generateSetupMethods(classInfo);

        return TestSuite.builder()
                .targetClass(classInfo)
                .testClassName(classInfo.getTestClassName())
                .testPackage(classInfo.getPackageName())
                .testCases(testCases)
                .testType(classInfo.getTestType())
                .imports(imports)
                .classAnnotations(classAnnotations)
                .testFields(testFields)
                .setupMethods(setupMethods)
                .build();
    }

    protected List<TestCase> generateTestCasesForMethod(MethodInfo method, ClassInfo classInfo) {
        List<TestCase> testCases = new ArrayList<>();

        testCases.add(generateHappyPathTest(method, classInfo));

        if (config.isGenerateEdgeCases()) {
            testCases.addAll(generateEdgeCaseTests(method, classInfo));
        }

        if (config.isGenerateExceptionTests() && !method.getAllExceptions().isEmpty()) {
            testCases.addAll(generateExceptionTests(method, classInfo));
        }

        return testCases;
    }

    protected abstract TestCase generateHappyPathTest(MethodInfo method, ClassInfo classInfo);

    protected abstract List<TestCase> generateEdgeCaseTests(MethodInfo method, ClassInfo classInfo);

    protected abstract List<TestCase> generateExceptionTests(MethodInfo method, ClassInfo classInfo);

    protected abstract List<String> generateImports(ClassInfo classInfo);

    protected abstract List<String> generateClassAnnotations(ClassInfo classInfo);

    protected abstract List<String> generateTestFields(ClassInfo classInfo);

    protected abstract List<String> generateSetupMethods(ClassInfo classInfo);

    protected String toLowerCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}