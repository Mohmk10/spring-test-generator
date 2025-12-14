package com.springtest.generator;

import com.springtest.model.AccessModifier;
import com.springtest.model.AnnotationInfo;
import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EdgeCaseGeneratorTest {

    private EdgeCaseGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new EdgeCaseGenerator();
    }

    @Test
    void testGenerateEdgeCaseTests_WithStringParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "processName",
                "void",
                "void",
                List.of(new ParameterInfo("name", "String", "java.lang.String", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateEdgeCaseTests(methodInfo, "service");

        assertThat(tests).isNotEmpty();
        assertThat(tests).anyMatch(test -> test.contains("WithNullName"));
        assertThat(tests).anyMatch(test -> test.contains("WithEmptyName"));
    }

    @Test
    void testGenerateEdgeCaseTests_WithIntParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "processCount",
                "void",
                "void",
                List.of(new ParameterInfo("count", "int", "int", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateEdgeCaseTests(methodInfo, "service");

        assertThat(tests).isNotEmpty();

        assertThat(tests).anyMatch(test -> test.contains("WithZeroCount"));
        assertThat(tests).anyMatch(test -> test.contains("WithNegativeCount"));
        assertThat(tests).anyMatch(test -> test.contains("WithMaxValueCount"));
    }

    @Test
    void testGenerateEdgeCaseTests_WithListParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "processItems",
                "void",
                "void",
                List.of(new ParameterInfo("items", "List", "java.util.List", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateEdgeCaseTests(methodInfo, "service");

        assertThat(tests).isNotEmpty();
        assertThat(tests).anyMatch(test -> test.contains("WithNullItems"));
        assertThat(tests).anyMatch(test -> test.contains("WithEmptyItems"));
    }

    @Test
    void testGenerateNullParameterTests_WithNullableParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "findUser",
                "User",
                "com.example.User",
                List.of(new ParameterInfo("id", "Long", "java.lang.Long", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateNullParameterTests(methodInfo, "service");

        assertThat(tests).hasSize(1);
        assertThat(tests.get(0)).contains("testFindUser_WithNullId");
        assertThat(tests.get(0)).contains("@Test");
        assertThat(tests.get(0)).contains("assertThatCode");
    }

    @Test
    void testGenerateNullParameterTests_WithRequiredParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "saveUser",
                "void",
                "void",
                List.of(new ParameterInfo("user", "User", "com.example.User", List.of(), true, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateNullParameterTests(methodInfo, "service");

        assertThat(tests).hasSize(1);
        assertThat(tests.get(0)).contains("testSaveUser_WithNullUser");
        assertThat(tests.get(0)).contains("assertThatThrownBy");
        assertThat(tests.get(0)).contains("IllegalArgumentException");
    }

    @Test
    void testGenerateNullParameterTests_WithPrimitiveParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "processCount",
                "void",
                "void",
                List.of(new ParameterInfo("count", "int", "int", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateNullParameterTests(methodInfo, "service");

        assertThat(tests).isEmpty();
    }

    @Test
    void testGenerateEmptyValueTests_WithStringParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "processName",
                "void",
                "void",
                List.of(new ParameterInfo("name", "String", "java.lang.String", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateEmptyValueTests(methodInfo, "service");

        assertThat(tests).hasSize(1);
        assertThat(tests.get(0)).contains("testProcessName_WithEmptyName");
        assertThat(tests.get(0)).contains("\"\"");
    }

    @Test
    void testGenerateEmptyValueTests_WithListParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "processItems",
                "void",
                "void",
                List.of(new ParameterInfo("items", "List", "java.util.List", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateEmptyValueTests(methodInfo, "service");

        assertThat(tests).hasSize(1);
        assertThat(tests.get(0)).contains("testProcessItems_WithEmptyItems");
        assertThat(tests.get(0)).contains("List.of()");
    }

    @Test
    void testGenerateBoundaryValueTests_WithIntParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "processCount",
                "void",
                "void",
                List.of(new ParameterInfo("count", "int", "int", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateBoundaryValueTests(methodInfo, "service");

        assertThat(tests).hasSizeGreaterThanOrEqualTo(4);
        assertThat(tests).anyMatch(test -> test.contains("WithZeroCount"));
        assertThat(tests).anyMatch(test -> test.contains("WithNegativeCount"));
        assertThat(tests).anyMatch(test -> test.contains("WithMaxValueCount"));
        assertThat(tests).anyMatch(test -> test.contains("WithMinValueCount"));
    }

    @Test
    void testGenerateBoundaryValueTests_WithLongParameter() {
        MethodInfo methodInfo = new MethodInfo(
                "processId",
                "void",
                "void",
                List.of(new ParameterInfo("id", "Long", "java.lang.Long", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateBoundaryValueTests(methodInfo, "service");

        assertThat(tests).hasSizeGreaterThanOrEqualTo(4);
        assertThat(tests).anyMatch(test -> test.contains("Long.MAX_VALUE"));
        assertThat(tests).anyMatch(test -> test.contains("Long.MIN_VALUE"));
    }

    @Test
    void testGenerateInvalidFormatTests_WithValidatedParameter() {
        AnnotationInfo validationAnnotation = new AnnotationInfo(
                "Email",
                "jakarta.validation.constraints.Email",
                java.util.Map.of()
        );

        MethodInfo methodInfo = new MethodInfo(
                "registerUser",
                "void",
                "void",
                List.of(new ParameterInfo("email", "String", "java.lang.String",
                        List.of(validationAnnotation), false, null)),
                List.of(),
                List.of(),
                List.of(),
                true,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateInvalidFormatTests(methodInfo, "service");

        assertThat(tests).isNotEmpty();
        assertThat(tests.get(0)).contains("testRegisterUser_WithInvalidEmail");
        assertThat(tests.get(0)).contains("assertThatThrownBy");
    }

    @Test
    void testGenerateInvalidFormatTests_WithNoValidation() {
        MethodInfo methodInfo = new MethodInfo(
                "processName",
                "void",
                "void",
                List.of(new ParameterInfo("name", "String", "java.lang.String", List.of(), false, null)),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateInvalidFormatTests(methodInfo, "service");

        assertThat(tests).isEmpty();
    }

    @Test
    void testGenerateEdgeCaseTests_WithMultipleParameters() {
        MethodInfo methodInfo = new MethodInfo(
                "createUser",
                "void",
                "void",
                List.of(
                        new ParameterInfo("name", "String", "java.lang.String", List.of(), false, null),
                        new ParameterInfo("age", "int", "int", List.of(), false, null),
                        new ParameterInfo("emails", "List", "java.util.List", List.of(), false, null)
                ),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateEdgeCaseTests(methodInfo, "service");

        assertThat(tests).isNotEmpty();

        assertThat(tests).anyMatch(test -> test.contains("Name"));
        assertThat(tests).anyMatch(test -> test.contains("Age"));
        assertThat(tests).anyMatch(test -> test.contains("Emails"));
    }

    @Test
    void testGenerateImports() {
        List<String> imports = generator.generateImports();

        assertThat(imports).isNotEmpty();
        assertThat(imports).contains("import static org.assertj.core.api.Assertions.*;");
        assertThat(imports).contains("import java.util.List;");
    }

    @Test
    void testGenerateEdgeCaseTests_WithNoParameters() {
        MethodInfo methodInfo = new MethodInfo(
                "doSomething",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateEdgeCaseTests(methodInfo, "service");

        assertThat(tests).isEmpty();
    }
}
