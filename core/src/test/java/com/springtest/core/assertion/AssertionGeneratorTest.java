package com.springtest.core.assertion;

import com.springtest.core.model.MethodInfo;
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
    void generateAssertions_ForHappyPath_ShouldIncludeNotNullCheck() {
        MethodInfo method = MethodInfo.builder()
                .name("findById")
                .returnType("User")
                .simpleReturnType("User")
                .voidReturn(false)
                .returnsOptional(false)
                .returnsCollection(false)
                .build();

        List<String> assertions = assertionGenerator.generateAssertions(method, "happy_path");

        assertThat(assertions).isNotEmpty();
        assertThat(assertions).anyMatch(a -> a.contains("isNotNull"));
    }

    @Test
    void generateAssertions_ForOptionalReturn_ShouldIncludePresentCheck() {
        MethodInfo method = MethodInfo.builder()
                .name("findById")
                .returnType("Optional<User>")
                .simpleReturnType("Optional")
                .voidReturn(false)
                .returnsOptional(true)
                .returnsCollection(false)
                .build();

        List<String> assertions = assertionGenerator.generateAssertions(method, "happy_path");

        assertThat(assertions).anyMatch(a -> a.contains("isPresent"));
    }

    @Test
    void generateAssertions_ForCollectionReturn_ShouldIncludeNotEmptyCheck() {
        MethodInfo method = MethodInfo.builder()
                .name("findAll")
                .returnType("List<User>")
                .simpleReturnType("List")
                .voidReturn(false)
                .returnsOptional(false)
                .returnsCollection(true)
                .build();

        List<String> assertions = assertionGenerator.generateAssertions(method, "happy_path");

        assertThat(assertions).anyMatch(a -> a.contains("isNotEmpty"));
    }

    @Test
    void generateAssertions_ForNotFoundScenario_ShouldIncludeEmptyCheck() {
        MethodInfo method = MethodInfo.builder()
                .name("findById")
                .returnType("Optional<User>")
                .simpleReturnType("Optional")
                .voidReturn(false)
                .returnsOptional(true)
                .returnsCollection(false)
                .build();

        List<String> assertions = assertionGenerator.generateAssertions(method, "not_found");

        assertThat(assertions).anyMatch(a -> a.contains("isEmpty"));
    }

    @Test
    void generateCustomAssertion_ForEquals_ShouldGenerateIsEqualTo() {
        String assertion = assertionGenerator.generateCustomAssertion("result", "expected", "equals");

        assertThat(assertion).contains("assertThat(result).isEqualTo(expected)");
    }

    @Test
    void generateCustomAssertion_ForNotNull_ShouldGenerateIsNotNull() {
        String assertion = assertionGenerator.generateCustomAssertion("result", null, "not_null");

        assertThat(assertion).contains("assertThat(result).isNotNull()");
    }

    @Test
    void generateCustomAssertion_ForIsTrue_ShouldGenerateIsTrue() {
        String assertion = assertionGenerator.generateCustomAssertion("result", null, "is_true");

        assertThat(assertion).contains("assertThat(result).isTrue()");
    }
}