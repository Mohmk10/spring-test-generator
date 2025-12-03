package com.springtest.core.generator;

import com.springtest.core.assertion.AssertionGenerator;
import com.springtest.core.config.GeneratorConfig;
import com.springtest.core.mock.MockGenerator;
import com.springtest.core.mock.StubGenerator;
import com.springtest.core.mock.VerifyGenerator;
import com.springtest.core.model.AnnotationInfo;
import com.springtest.core.model.ClassInfo;
import com.springtest.core.model.MethodInfo;
import com.springtest.core.model.TestSuite;
import com.springtest.core.naming.impl.MethodScenarioExpectedNaming;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTestGeneratorTest {

    private ServiceTestGenerator generator;
    private GeneratorConfig config;

    @BeforeEach
    void setUp() {
        config = GeneratorConfig.builder().build();
        generator = new ServiceTestGenerator(
                config,
                new MethodScenarioExpectedNaming(),
                new MockGenerator(),
                new StubGenerator(),
                new VerifyGenerator(),
                new AssertionGenerator()
        );
    }

    @Test
    void generateTestSuite_ForServiceClass_ShouldGenerateTests() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        MethodInfo method = MethodInfo.builder()
                .name("findById")
                .returnType("User")
                .simpleReturnType("User")
                .parameters(List.of())
                .annotations(List.of())
                .declaredExceptions(List.of())
                .thrownExceptions(List.of())
                .voidReturn(false)
                .returnsOptional(false)
                .returnsCollection(false)
                .complexity(1)
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .packageName("com.example.service")
                .fullyQualifiedName("com.example.service.UserService")
                .annotations(List.of(service))
                .fields(List.of())
                .methods(List.of(method))
                .build();

        TestSuite testSuite = generator.generateTestSuite(classInfo);

        assertThat(testSuite).isNotNull();
        assertThat(testSuite.getTestClassName()).isEqualTo("UserServiceTest");
        assertThat(testSuite.getTestCases()).isNotEmpty();
    }

    @Test
    void generateTestSuite_ShouldIncludeMockitoExtension() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .packageName("com.example.service")
                .annotations(List.of(service))
                .fields(List.of())
                .methods(List.of())
                .build();

        TestSuite testSuite = generator.generateTestSuite(classInfo);

        assertThat(testSuite.getClassAnnotations())
                .contains("@ExtendWith(MockitoExtension.class)");
    }

    @Test
    void generateTestSuite_ShouldIncludeNecessaryImports() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .packageName("com.example.service")
                .fullyQualifiedName("com.example.service.UserService")
                .annotations(List.of(service))
                .fields(List.of())
                .methods(List.of())
                .build();

        TestSuite testSuite = generator.generateTestSuite(classInfo);

        assertThat(testSuite.getImports()).contains("import org.junit.jupiter.api.Test;");
        assertThat(testSuite.getImports()).contains("import org.mockito.InjectMocks;");
        assertThat(testSuite.getImports()).contains("import org.mockito.Mock;");
    }
}