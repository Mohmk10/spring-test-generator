package com.springtest.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TemplateEngineTest {

    @Mock
    private TemplateLoader templateLoader;

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        templateEngine = new TemplateEngine(templateLoader);
    }

    @Test
    void testProcessWithNullTemplateName() {
        Map<String, Object> dataModel = new HashMap<>();

        assertThatThrownBy(() -> templateEngine.process(null, dataModel))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template name cannot be null or empty");
    }

    @Test
    void testProcessWithEmptyTemplateName() {
        Map<String, Object> dataModel = new HashMap<>();

        assertThatThrownBy(() -> templateEngine.process("", dataModel))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template name cannot be null or empty");
    }

    @Test
    void testProcessWithNullDataModel() {
        assertThatThrownBy(() -> templateEngine.process("test.ftl", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Data model cannot be null");
    }

    @Test
    void testProcessWithNonExistentTemplate() {
        Map<String, Object> dataModel = new HashMap<>();

        assertThatThrownBy(() -> templateEngine.process("non-existent.ftl", dataModel))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to load template");
    }

    @Test
    void testProcessServiceTestWithNullDataModel() {
        assertThatThrownBy(() -> templateEngine.processServiceTest(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testProcessControllerTestWithNullDataModel() {
        assertThatThrownBy(() -> templateEngine.processControllerTest(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testProcessRepositoryTestWithNullDataModel() {
        assertThatThrownBy(() -> templateEngine.processRepositoryTest(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testProcessIntegrationTestWithNullDataModel() {
        assertThatThrownBy(() -> templateEngine.processIntegrationTest(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
