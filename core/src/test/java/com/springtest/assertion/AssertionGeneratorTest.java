package com.springtest.assertion;

import com.springtest.model.AccessModifier;
import com.springtest.model.MethodInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssertionGeneratorTest {

    private AssertionGenerator assertionGenerator;

    @BeforeEach
    void setUp() {
        assertionGenerator = new AssertionGenerator();
    }

    @Test
    void shouldGenerateStringAssertion_WhenReturnTypeIsString() {

        String result = assertionGenerator.generateBasicAssertion("result", "String");

        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isNotEmpty()");
    }

    @Test
    void shouldGenerateNumericAssertion_WhenReturnTypeIsInteger() {

        String result = assertionGenerator.generateBasicAssertion("result", "Integer");

        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isPositive()");
    }

    @Test
    void shouldGenerateCollectionAssertion_WhenReturnTypeIsList() {

        String result = assertionGenerator.generateBasicAssertion("result", "List");

        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isNotEmpty()");
    }

    @Test
    void shouldGenerateOptionalAssertion_WhenReturnTypeIsOptional() {

        String result = assertionGenerator.generateBasicAssertion("result", "Optional");

        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isPresent()");
    }

    @Test
    void shouldGenerateNoAssertion_WhenReturnTypeIsVoid() {

        String result = assertionGenerator.generateBasicAssertion("result", "void");

        assertThat(result).contains("No assertion needed for void method");
    }

    @Test
    void shouldGenerateIsEqualTo_WhenComparingValues() {

        String result = assertionGenerator.generateIsEqualTo("actual", "expected");

        assertThat(result).isEqualTo("assertThat(actual).isEqualTo(expected);");
    }

    @Test
    void shouldGenerateIsNotEqualTo_WhenValuesShoudDiffer() {

        String result = assertionGenerator.generateIsNotEqualTo("actual", "unexpected");

        assertThat(result).isEqualTo("assertThat(actual).isNotEqualTo(unexpected);");
    }

    @Test
    void shouldGenerateCollectionAssertions_WhenAssertingCollections() {

        String result = assertionGenerator.generateCollectionAssertions("users");

        assertThat(result).contains("assertThat(users)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isNotEmpty()");
        assertThat(result).contains("hasSize(1)");
    }

    @Test
    void shouldGenerateStringAssertions_WhenAssertingStrings() {

        String result = assertionGenerator.generateStringAssertions("name");

        assertThat(result).contains("assertThat(name)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isNotEmpty()");
        assertThat(result).contains("isNotBlank()");
    }

    @Test
    void shouldGenerateNumericAssertions_WhenAssertingNumbers() {

        String result = assertionGenerator.generateNumericAssertions("count");

        assertThat(result).contains("assertThat(count)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isPositive()");
        assertThat(result).contains("isGreaterThan(0)");
    }

    @Test
    void shouldGenerateOptionalAssertions_WhenAssertingOptional() {

        String result = assertionGenerator.generateOptionalAssertions("maybeUser");

        assertThat(result).contains("assertThat(maybeUser)");
        assertThat(result).contains("isPresent()");
        assertThat(result).contains("get()");
        assertThat(result).contains("isNotNull()");
    }

    @Test
    void shouldGenerateExceptionAssertion_WhenMethodShouldThrow() {

        String result = assertionGenerator.generateExceptionAssertion(
                "service.deleteUser(1L)",
                "IllegalArgumentException"
        );

        assertThat(result).contains("assertThatThrownBy");
        assertThat(result).contains("service.deleteUser(1L)");
        assertThat(result).contains("isInstanceOf(IllegalArgumentException.class)");
        assertThat(result).contains("hasMessageContaining");
    }

    @Test
    void shouldGenerateNoExceptionAssertion_WhenMethodShouldNotThrow() {

        String result = assertionGenerator.generateNoExceptionAssertion("service.saveUser(user)");

        assertThat(result).contains("assertThatCode");
        assertThat(result).contains("service.saveUser(user)");
        assertThat(result).contains("doesNotThrowAnyException()");
    }

    @Test
    void shouldGenerateContainsAssertion_WhenCheckingCollectionContents() {

        String result = assertionGenerator.generateContainsAssertion("list", List.of("\"item1\"", "\"item2\""));

        assertThat(result).contains("assertThat(list)");
        assertThat(result).contains("contains(\"item1\", \"item2\")");
    }

    @Test
    void shouldGenerateContainsExactly_WhenCheckingExactOrder() {

        String result = assertionGenerator.generateContainsExactly("list", List.of("\"a\"", "\"b\"", "\"c\""));

        assertThat(result).contains("assertThat(list)");
        assertThat(result).contains("containsExactly(\"a\", \"b\", \"c\")");
    }

    @Test
    void shouldGenerateHasSize_WhenCheckingCollectionSize() {

        String result = assertionGenerator.generateHasSize("users", 5);

        assertThat(result).isEqualTo("assertThat(users).hasSize(5);");
    }

    @Test
    void shouldGenerateExtractingAssertion_WhenCheckingObjectProperties() {

        String result = assertionGenerator.generateExtractingAssertion(
                "users",
                "name",
                List.of("\"John\"", "\"Jane\"")
        );

        assertThat(result).contains("assertThat(users)");
        assertThat(result).contains("extracting(\"name\")");
        assertThat(result).contains("containsExactly(\"John\", \"Jane\")");
    }

    @Test
    void shouldGenerateRecursiveComparison_WhenComparingObjects() {

        String result = assertionGenerator.generateUsingRecursiveComparison("actual", "expected");

        assertThat(result).contains("assertThat(actual)");
        assertThat(result).contains("usingRecursiveComparison()");
        assertThat(result).contains("isEqualTo(expected)");
    }

    @Test
    void shouldGenerateImports_WhenGeneratingAssertions() {

        List<String> imports = assertionGenerator.generateImports();

        assertThat(imports).contains("import static org.assertj.core.api.Assertions.*;");
    }

    @Test
    void shouldGenerateMethodAssertions_WhenProvidedMethodInfo() {

        MethodInfo method = new MethodInfo(
                "findUser",
                "String",
                "java.lang.String",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String result = assertionGenerator.generateAssertionForMethod(method);

        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isNotNull()");
    }

    @Test
    void shouldGenerateComprehensiveAssertions_WhenMultipleChecksNeeded() {

        String result = assertionGenerator.generateComprehensiveAssertions("result", "String");

        assertThat(result).contains("assertThat(result).isNotNull()");
        assertThat(result).contains("assertThat(result).isNotEmpty()");
        assertThat(result).contains("isNotBlank()");
    }
}
