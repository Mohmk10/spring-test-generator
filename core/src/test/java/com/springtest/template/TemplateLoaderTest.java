package com.springtest.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TemplateLoaderTest {

    private TemplateLoader templateLoader;

    @BeforeEach
    void setUp() {
        templateLoader = new TemplateLoader();
    }

    @Test
    void testLoadTemplateWithNullName() {
        assertThatThrownBy(() -> templateLoader.loadTemplate(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template name cannot be null or empty");
    }

    @Test
    void testLoadTemplateWithEmptyName() {
        assertThatThrownBy(() -> templateLoader.loadTemplate(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template name cannot be null or empty");
    }

    @Test
    void testLoadTemplateWithNonExistentTemplate() {
        assertThatThrownBy(() -> templateLoader.loadTemplate("non-existent.ftl"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to load template");
    }

    @Test
    void testLoadServiceTemplate() {
        String content = templateLoader.loadServiceTemplate();
        assertThat(content).isNotNull();
        assertThat(content).isNotEmpty();
        assertThat(content).contains("package");
    }

    @Test
    void testLoadControllerTemplate() {
        String content = templateLoader.loadControllerTemplate();
        assertThat(content).isNotNull();
        assertThat(content).isNotEmpty();
        assertThat(content).contains("package");
    }

    @Test
    void testLoadRepositoryTemplate() {
        String content = templateLoader.loadRepositoryTemplate();
        assertThat(content).isNotNull();
        assertThat(content).isNotEmpty();
        assertThat(content).contains("package");
    }

    @Test
    void testLoadIntegrationTemplate() {
        String content = templateLoader.loadIntegrationTemplate();
        assertThat(content).isNotNull();
        assertThat(content).isNotEmpty();
        assertThat(content).contains("package");
    }

    @Test
    void testClearCache() {
        assertThatCode(() -> templateLoader.clearCache())
            .doesNotThrowAnyException();
    }

    @Test
    void testIsCached() {
        boolean cached = templateLoader.isCached("test.ftl");
        assertThat(cached).isFalse();
    }

    @Test
    void testIsCachedAfterLoad() {
        templateLoader.loadServiceTemplate();

        boolean cached = templateLoader.isCached("service-test.ftl");
        assertThat(cached).isTrue();
    }
}
