package com.springtest.core.generator;

import com.springtest.core.assertion.AssertionGenerator;
import com.springtest.core.config.GeneratorConfig;
import com.springtest.core.mock.MockGenerator;
import com.springtest.core.mock.StubGenerator;
import com.springtest.core.mock.VerifyGenerator;
import com.springtest.core.model.ClassInfo;
import com.springtest.core.naming.NamingStrategy;
import com.springtest.core.naming.impl.BDDNaming;
import com.springtest.core.naming.impl.GivenWhenThenNaming;
import com.springtest.core.naming.impl.MethodScenarioExpectedNaming;
import com.springtest.core.naming.impl.SimpleNaming;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestGeneratorFactory {

    private final GeneratorConfig config;
    private final MockGenerator mockGenerator;
    private final StubGenerator stubGenerator;
    private final VerifyGenerator verifyGenerator;
    private final AssertionGenerator assertionGenerator;

    public TestGeneratorFactory(GeneratorConfig config) {
        this.config = config;
        this.mockGenerator = new MockGenerator();
        this.stubGenerator = new StubGenerator();
        this.verifyGenerator = new VerifyGenerator();
        this.assertionGenerator = new AssertionGenerator();
    }

    public TestGenerator createGenerator(ClassInfo classInfo) {
        NamingStrategy namingStrategy = createNamingStrategy();
        String testType = classInfo.getTestType();

        return switch (testType) {
            case "webmvc" -> new ControllerTestGenerator(
                    config, namingStrategy, mockGenerator, stubGenerator,
                    verifyGenerator, assertionGenerator
            );
            case "datajpa" -> new RepositoryTestGenerator(
                    config, namingStrategy, mockGenerator, stubGenerator,
                    verifyGenerator, assertionGenerator
            );
            case "integration" -> new IntegrationTestGenerator(
                    config, namingStrategy, mockGenerator, stubGenerator,
                    verifyGenerator, assertionGenerator
            );
            default -> new ServiceTestGenerator(
                    config, namingStrategy, mockGenerator, stubGenerator,
                    verifyGenerator, assertionGenerator
            );
        };
    }

    private NamingStrategy createNamingStrategy() {
        String convention = config.getTestNamingConvention();

        return switch (convention) {
            case "GIVEN_WHEN_THEN" -> new GivenWhenThenNaming();
            case "BDD" -> new BDDNaming();
            case "SIMPLE" -> new SimpleNaming();
            default -> new MethodScenarioExpectedNaming();
        };
    }
}