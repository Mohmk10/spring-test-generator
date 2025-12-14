package com.springtest.generator;

import com.springtest.model.AccessModifier;
import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ExceptionTestGeneratorTest {

    private ExceptionTestGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ExceptionTestGenerator();
    }

    @Test
    void testGenerateExceptionTests_WithDeclaredExceptions() {
        MethodInfo methodInfo = new MethodInfo(
                "deleteUser",
                "void",
                "void",
                List.of(new ParameterInfo("id", "Long", "java.lang.Long", List.of(), false, null)),
                List.of(),
                List.of("IllegalArgumentException", "EntityNotFoundException"),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateExceptionTests(methodInfo, "service");

        assertThat(tests).hasSize(2);
        assertThat(tests).anyMatch(test -> test.contains("testDeleteUser_ThrowsIllegalArgumentException"));
        assertThat(tests).anyMatch(test -> test.contains("testDeleteUser_ThrowsEntityNotFoundException"));
    }

    @Test
    void testGenerateExceptionTests_WithPossibleExceptions() {
        MethodInfo methodInfo = new MethodInfo(
                "processData",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of(),
                List.of("NullPointerException", "IllegalStateException"),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateExceptionTests(methodInfo, "service");

        assertThat(tests).hasSize(2);
        assertThat(tests).anyMatch(test -> test.contains("testProcessData_ThrowsNullPointerException"));
        assertThat(tests).anyMatch(test -> test.contains("testProcessData_ThrowsIllegalStateException"));
    }

    @Test
    void testGenerateExceptionTests_WithBothDeclaredAndPossible() {
        MethodInfo methodInfo = new MethodInfo(
                "saveData",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of("IOException"),
                List.of("NullPointerException"),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateExceptionTests(methodInfo, "service");

        assertThat(tests).hasSize(2);
        assertThat(tests).anyMatch(test -> test.contains("ThrowsIOException"));
        assertThat(tests).anyMatch(test -> test.contains("ThrowsNullPointerException"));
    }

    @Test
    void testGenerateExceptionTests_WithNoDuplicates() {
        MethodInfo methodInfo = new MethodInfo(
                "processUser",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of("IllegalArgumentException"),
                List.of("IllegalArgumentException", "NullPointerException"),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateExceptionTests(methodInfo, "service");

        assertThat(tests).hasSize(2);
        long illegalArgCount = tests.stream()
                .filter(test -> test.contains("ThrowsIllegalArgumentException"))
                .count();
        assertThat(illegalArgCount).isEqualTo(1);
    }

    @Test
    void testGenerateExceptionTest_ContainsAssertion() {
        MethodInfo methodInfo = new MethodInfo(
                "validateInput",
                "void",
                "void",
                List.of(new ParameterInfo("input", "String", "java.lang.String", List.of(), false, null)),
                List.of(),
                List.of("IllegalArgumentException"),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String test = generator.generateExceptionTest(methodInfo, "service", "IllegalArgumentException", true);

        assertThat(test).contains("@Test");
        assertThat(test).contains("void testValidateInput_ThrowsIllegalArgumentException()");
        assertThat(test).contains("assertThatThrownBy");
        assertThat(test).contains(".isInstanceOf(IllegalArgumentException.class)");
        assertThat(test).contains(".hasMessageContaining(");
    }

    @Test
    void testGenerateExceptionMessageTest() {
        MethodInfo methodInfo = new MethodInfo(
                "processData",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of("RuntimeException"),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String test = generator.generateExceptionMessageTest(methodInfo, "service", "RuntimeException");

        assertThat(test).contains("testProcessData_RuntimeExceptionMessage");
        assertThat(test).contains(".hasMessage(\"Expected error message\")");
        assertThat(test).contains(".hasNoCause()");
    }

    @Test
    void testGenerateExceptionCauseTest() {
        MethodInfo methodInfo = new MethodInfo(
                "executeQuery",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of("DataAccessException"),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String test = generator.generateExceptionCauseTest(methodInfo, "service", "DataAccessException", "SQLException");

        assertThat(test).contains("testExecuteQuery_DataAccessExceptionWithCause");
        assertThat(test).contains(".hasCauseInstanceOf(SQLException.class)");
        assertThat(test).contains(".hasRootCauseInstanceOf(SQLException.class)");
    }

    @Test
    void testGenerateMultipleExceptionsTest() {
        MethodInfo methodInfo = new MethodInfo(
                "processRequest",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of("IllegalArgumentException", "IllegalStateException"),
                List.of("NullPointerException"),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String test = generator.generateMultipleExceptionsTest(methodInfo, "service");

        assertThat(test).contains("testProcessRequest_ThrowsAppropriateExceptions");
        assertThat(test).contains(".isInstanceOfAny(");
        assertThat(test).contains("IllegalArgumentException.class");
        assertThat(test).contains("IllegalStateException.class");
        assertThat(test).contains("NullPointerException.class");
    }

    @Test
    void testGenerateMultipleExceptionsTest_WithNoExceptions() {
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

        String test = generator.generateMultipleExceptionsTest(methodInfo, "service");

        assertThat(test).isEmpty();
    }

    @Test
    void testHasExceptionsToTest_WithDeclaredExceptions() {
        MethodInfo methodInfo = new MethodInfo(
                "saveData",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of("IOException"),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        assertThat(generator.hasExceptionsToTest(methodInfo)).isTrue();
    }

    @Test
    void testHasExceptionsToTest_WithPossibleExceptions() {
        MethodInfo methodInfo = new MethodInfo(
                "processData",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of(),
                List.of("NullPointerException"),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        assertThat(generator.hasExceptionsToTest(methodInfo)).isTrue();
    }

    @Test
    void testHasExceptionsToTest_WithNoExceptions() {
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

        assertThat(generator.hasExceptionsToTest(methodInfo)).isFalse();
    }

    @Test
    void testGenerateImports() {
        List<String> imports = generator.generateImports();

        assertThat(imports).isNotEmpty();
        assertThat(imports).contains("import static org.assertj.core.api.Assertions.*;");
        assertThat(imports).contains("import java.util.List;");
    }

    @Test
    void testGenerateExceptionTest_WithQualifiedExceptionName() {
        MethodInfo methodInfo = new MethodInfo(
                "findUser",
                "User",
                "com.example.User",
                List.of(),
                List.of(),
                List.of("jakarta.persistence.EntityNotFoundException"),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String test = generator.generateExceptionTest(methodInfo, "service", "jakarta.persistence.EntityNotFoundException", true);

        assertThat(test).contains("testFindUser_ThrowsEntityNotFoundException");
        assertThat(test).contains(".isInstanceOf(jakarta.persistence.EntityNotFoundException.class)");
    }

    @Test
    void testGenerateExceptionTests_WithMethodParameters() {
        MethodInfo methodInfo = new MethodInfo(
                "updateUser",
                "void",
                "void",
                List.of(
                        new ParameterInfo("id", "Long", "java.lang.Long", List.of(), false, null),
                        new ParameterInfo("name", "String", "java.lang.String", List.of(), false, null)
                ),
                List.of(),
                List.of("IllegalArgumentException"),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        List<String> tests = generator.generateExceptionTests(methodInfo, "service");

        assertThat(tests).hasSize(1);
        String test = tests.get(0);
        assertThat(test).contains("service.updateUser(");
    }

}
