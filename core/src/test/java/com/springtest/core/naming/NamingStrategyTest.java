package com.springtest.core.naming;

import com.springtest.core.model.MethodInfo;
import com.springtest.core.naming.impl.BDDNaming;
import com.springtest.core.naming.impl.GivenWhenThenNaming;
import com.springtest.core.naming.impl.MethodScenarioExpectedNaming;
import com.springtest.core.naming.impl.SimpleNaming;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NamingStrategyTest {

    @Test
    void methodScenarioExpectedNaming_ShouldGenerateCorrectFormat() {
        NamingStrategy strategy = new MethodScenarioExpectedNaming();

        MethodInfo method = MethodInfo.builder()
                .name("findById")
                .voidReturn(false)
                .returnsOptional(false)
                .parameters(List.of())
                .build();

        String testName = strategy.generateTestMethodName(method, "happy_path");

        assertThat(testName).isEqualTo("findById_WhenValid_ShouldReturnResult");
    }

    @Test
    void givenWhenThenNaming_ShouldGenerateCorrectFormat() {
        NamingStrategy strategy = new GivenWhenThenNaming();

        MethodInfo method = MethodInfo.builder()
                .name("findById")
                .voidReturn(false)
                .returnsOptional(false)
                .parameters(List.of())
                .build();

        String testName = strategy.generateTestMethodName(method, "happy_path");

        assertThat(testName).isEqualTo("givenValidInput_whenFindById_thenReturnResult");
    }

    @Test
    void bddNaming_ShouldGenerateCorrectFormat() {
        NamingStrategy strategy = new BDDNaming();

        MethodInfo method = MethodInfo.builder()
                .name("findById")
                .returnType("User")
                .simpleReturnType("User")
                .voidReturn(false)
                .returnsOptional(false)
                .parameters(List.of())
                .build();

        String testName = strategy.generateTestMethodName(method, "happy_path");

        assertThat(testName).contains("should_");
        assertThat(testName).contains("when_valid");
    }

    @Test
    void simpleNaming_ShouldGenerateCorrectFormat() {
        NamingStrategy strategy = new SimpleNaming();

        MethodInfo method = MethodInfo.builder()
                .name("findById")
                .voidReturn(false)
                .parameters(List.of())
                .build();

        String testName = strategy.generateTestMethodName(method, "happy_path");

        assertThat(testName).isEqualTo("testFindById");
    }

    @Test
    void methodScenarioExpectedNaming_ForExceptionScenario_ShouldIncludeThrowException() {
        NamingStrategy strategy = new MethodScenarioExpectedNaming();

        MethodInfo method = MethodInfo.builder()
                .name("findById")
                .voidReturn(false)
                .parameters(List.of())
                .build();

        String testName = strategy.generateTestMethodName(method, "exception");

        assertThat(testName).contains("ShouldThrowException");
    }
}