package com.springtest.naming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MethodScenarioExpectedNamingTest {

    private MethodScenarioExpectedNaming naming;

    @BeforeEach
    void setUp() {
        naming = new MethodScenarioExpectedNaming();
    }

    @Test
    void testGenerateTestMethodNameWithAllParameters() {
        String result = naming.generateTestMethodName("save", "withValidUser", "returnsUser");

        assertThat(result).isEqualTo("testSave_WithValidUser_ReturnsUser");
    }

    @Test
    void testGenerateTestMethodNameWithMethodAndScenario() {
        String result = naming.generateTestMethodName("delete", "withNullId");

        assertThat(result).isEqualTo("testDelete_WithNullId");
    }

    @Test
    void testGenerateTestMethodNameWithMethodOnly() {
        String result = naming.generateTestMethodName("findAll");

        assertThat(result).isEqualTo("testFindAll");
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
        assertThat(strategyName).isEqualTo("MethodScenarioExpected");
    }

    @Test
    void testCapitalization() {
        String result = naming.generateTestMethodName("saveUser", "validInput", "success");
        assertThat(result).isEqualTo("testSaveUser_ValidInput_Success");
    }
}
