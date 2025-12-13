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
        // When
        String result = assertionGenerator.generateBasicAssertion("result", "String");

        // Then
        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isNotEmpty()");
    }

    @Test
    void shouldGenerateNumericAssertion_WhenReturnTypeIsInteger() {
        // When
        String result = assertionGenerator.generateBasicAssertion("result", "Integer");

        // Then
        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isPositive()");
    }

    @Test
    void shouldGenerateCollectionAssertion_WhenReturnTypeIsList() {
        // When
        String result = assertionGenerator.generateBasicAssertion("result", "List");

        // Then
        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isNotEmpty()");
    }

    @Test
    void shouldGenerateOptionalAssertion_WhenReturnTypeIsOptional() {
        // When
        String result = assertionGenerator.generateBasicAssertion("result", "Optional");

        // Then
        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isPresent()");
    }

    @Test
    void shouldGenerateNoAssertion_WhenReturnTypeIsVoid() {
        // When
        String result = assertionGenerator.generateBasicAssertion("result", "void");

        // Then
        assertThat(result).contains("No assertion needed for void method");
    }

    @Test
    void shouldGenerateIsEqualTo_WhenComparingValues() {
        // When
        String result = assertionGenerator.generateIsEqualTo("actual", "expected");

        // Then
        assertThat(result).isEqualTo("assertThat(actual).isEqualTo(expected);");
    }

    @Test
    void shouldGenerateIsNotEqualTo_WhenValuesShoudDiffer() {
        // When
        String result = assertionGenerator.generateIsNotEqualTo("actual", "unexpected");

        // Then
        assertThat(result).isEqualTo("assertThat(actual).isNotEqualTo(unexpected);");
    }

    @Test
    void shouldGenerateCollectionAssertions_WhenAssertingCollections() {
        // When
        String result = assertionGenerator.generateCollectionAssertions("users");

        // Then
        assertThat(result).contains("assertThat(users)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isNotEmpty()");
        assertThat(result).contains("hasSize(1)");
    }

    @Test
    void shouldGenerateStringAssertions_WhenAssertingStrings() {
        // When
        String result = assertionGenerator.generateStringAssertions("name");

        // Then
        assertThat(result).contains("assertThat(name)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isNotEmpty()");
        assertThat(result).contains("isNotBlank()");
    }

    @Test
    void shouldGenerateNumericAssertions_WhenAssertingNumbers() {
        // When
        String result = assertionGenerator.generateNumericAssertions("count");

        // Then
        assertThat(result).contains("assertThat(count)");
        assertThat(result).contains("isNotNull()");
        assertThat(result).contains("isPositive()");
        assertThat(result).contains("isGreaterThan(0)");
    }

    @Test
    void shouldGenerateOptionalAssertions_WhenAssertingOptional() {
        // When
        String result = assertionGenerator.generateOptionalAssertions("maybeUser");

        // Then
        assertThat(result).contains("assertThat(maybeUser)");
        assertThat(result).contains("isPresent()");
        assertThat(result).contains("get()");
        assertThat(result).contains("isNotNull()");
    }

    @Test
    void shouldGenerateExceptionAssertion_WhenMethodShouldThrow() {
        // When
        String result = assertionGenerator.generateExceptionAssertion(
                "service.deleteUser(1L)",
                "IllegalArgumentException"
        );

        // Then
        assertThat(result).contains("assertThatThrownBy");
        assertThat(result).contains("service.deleteUser(1L)");
        assertThat(result).contains("isInstanceOf(IllegalArgumentException.class)");
        assertThat(result).contains("hasMessageContaining");
    }

    @Test
    void shouldGenerateNoExceptionAssertion_WhenMethodShouldNotThrow() {
        // When
        String result = assertionGenerator.generateNoExceptionAssertion("service.saveUser(user)");

        // Then
        assertThat(result).contains("assertThatCode");
        assertThat(result).contains("service.saveUser(user)");
        assertThat(result).contains("doesNotThrowAnyException()");
    }

    @Test
    void shouldGenerateContainsAssertion_WhenCheckingCollectionContents() {
        // When
        String result = assertionGenerator.generateContainsAssertion("list", List.of("\"item1\"", "\"item2\""));

        // Then
        assertThat(result).contains("assertThat(list)");
        assertThat(result).contains("contains(\"item1\", \"item2\")");
    }

    @Test
    void shouldGenerateContainsExactly_WhenCheckingExactOrder() {
        // When
        String result = assertionGenerator.generateContainsExactly("list", List.of("\"a\"", "\"b\"", "\"c\""));

        // Then
        assertThat(result).contains("assertThat(list)");
        assertThat(result).contains("containsExactly(\"a\", \"b\", \"c\")");
    }

    @Test
    void shouldGenerateHasSize_WhenCheckingCollectionSize() {
        // When
        String result = assertionGenerator.generateHasSize("users", 5);

        // Then
        assertThat(result).isEqualTo("assertThat(users).hasSize(5);");
    }

    @Test
    void shouldGenerateExtractingAssertion_WhenCheckingObjectProperties() {
        // When
        String result = assertionGenerator.generateExtractingAssertion(
                "users",
                "name",
                List.of("\"John\"", "\"Jane\"")
        );

        // Then
        assertThat(result).contains("assertThat(users)");
        assertThat(result).contains("extracting(\"name\")");
        assertThat(result).contains("containsExactly(\"John\", \"Jane\")");
    }

    @Test
    void shouldGenerateRecursiveComparison_WhenComparingObjects() {
        // When
        String result = assertionGenerator.generateUsingRecursiveComparison("actual", "expected");

        // Then
        assertThat(result).contains("assertThat(actual)");
        assertThat(result).contains("usingRecursiveComparison()");
        assertThat(result).contains("isEqualTo(expected)");
    }

    @Test
    void shouldGenerateImports_WhenGeneratingAssertions() {
        // When
        List<String> imports = assertionGenerator.generateImports();

        // Then
        assertThat(imports).contains("import static org.assertj.core.api.Assertions.*;");
    }

    @Test
    void shouldGenerateMethodAssertions_WhenProvidedMethodInfo() {
        // Given
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

        // When
        String result = assertionGenerator.generateAssertionForMethod(method);

        // Then
        assertThat(result).contains("assertThat(result)");
        assertThat(result).contains("isNotNull()");
    }

    @Test
    void shouldGenerateComprehensiveAssertions_WhenMultipleChecksNeeded() {
        // When
        String result = assertionGenerator.generateComprehensiveAssertions("result", "String");

        // Then
        assertThat(result).contains("assertThat(result).isNotNull()");
        assertThat(result).contains("assertThat(result).isNotEmpty()");
        assertThat(result).contains("isNotBlank()");
    }
}
