package com.springtest.naming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GivenWhenThenNamingTest {

    private GivenWhenThenNaming naming;

    @BeforeEach
    void setUp() {
        naming = new GivenWhenThenNaming();
    }

    @Test
    void testGenerateTestMethodNameWithAllParameters() {
        String result = naming.generateTestMethodName("save", "validUser", "userIsSaved");

        assertThat(result).isEqualTo("givenValidUser_whenSave_thenUserIsSaved");
    }

    @Test
    void testGenerateTestMethodNameWithMethodAndScenario() {
        String result = naming.generateTestMethodName("delete", "nullId");

        assertThat(result).isEqualTo("givenNullId_whenDelete");
    }

    @Test
    void testGenerateTestMethodNameWithMethodOnly() {
        String result = naming.generateTestMethodName("findAll");

        assertThat(result).isEqualTo("whenFindAll");
    }

    @Test
    void testGenerateTestMethodNameWithNullMethodName() {
        assertThatThrownBy(() -> naming.generateTestMethodName(null, "scenario", "expected"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Method name cannot be null or empty");
    }

    @Test
    void testGenerateTestMethodNameWithEmptyMethodName() {
        assertThatThrownBy(() -> naming.generateTestMethodName("", "scenario", "expected"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Method name cannot be null or empty");
    }

    @Test
    void testGenerateTestMethodNameWithNullScenario() {
        assertThatThrownBy(() -> naming.generateTestMethodName("method", null, "expected"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Scenario cannot be null or empty");
    }

    @Test
    void testGenerateTestMethodNameWithEmptyScenario() {
        assertThatThrownBy(() -> naming.generateTestMethodName("method", "", "expected"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Scenario cannot be null or empty");
    }

    @Test
    void testGenerateTestMethodNameWithNullExpected() {
        assertThatThrownBy(() -> naming.generateTestMethodName("method", "scenario", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Expected cannot be null or empty");
    }

    @Test
    void testGenerateTestMethodNameWithEmptyExpected() {
        assertThatThrownBy(() -> naming.generateTestMethodName("method", "scenario", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Expected cannot be null or empty");
    }

    @Test
    void testGenerateTestMethodNameTwoParamsWithNullMethodName() {
        assertThatThrownBy(() -> naming.generateTestMethodName(null, "scenario"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Method name cannot be null or empty");
    }

    @Test
    void testGenerateTestMethodNameTwoParamsWithNullScenario() {
        assertThatThrownBy(() -> naming.generateTestMethodName("method", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Scenario cannot be null or empty");
    }

    @Test
    void testGenerateTestMethodNameOneParamWithNull() {
        assertThatThrownBy(() -> naming.generateTestMethodName(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Method name cannot be null or empty");
    }

    @Test
    void testGenerateTestMethodNameOneParamWithEmpty() {
        assertThatThrownBy(() -> naming.generateTestMethodName(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Method name cannot be null or empty");
    }

    @Test
    void testGetStrategyName() {
        String strategyName = naming.getStrategyName();
        assertThat(strategyName).isEqualTo("GivenWhenThen");
    }

    @Test
    void testCapitalization() {
        String result = naming.generateTestMethodName("saveUser", "validInput", "success");
        assertThat(result).isEqualTo("givenValidInput_whenSaveUser_thenSuccess");
    }
}
