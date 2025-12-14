package com.springtest.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TemplateLoader {
    private static final Logger logger = LoggerFactory.getLogger(TemplateLoader.class);

    private static final String TEMPLATE_BASE_PATH = "/templates/";
    private final Map<String, String> templateCache;

    public TemplateLoader() {
        this.templateCache = new HashMap<>();
    }

    public String loadTemplate(String templateName) {
        if (templateName == null || templateName.isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }

        if (templateCache.containsKey(templateName)) {
            logger.debug("Loading template from cache: {}", templateName);
            return templateCache.get(templateName);
        }

        logger.info("Loading template from resources: {}", templateName);

        String templatePath = TEMPLATE_BASE_PATH + templateName;
        try (InputStream inputStream = getClass().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new IOException("Template not found: " + templatePath);
            }

            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            templateCache.put(templateName, content);
            return content;
        } catch (IOException e) {
            logger.error("Failed to load template: {}", templateName, e);
            throw new RuntimeException("Failed to load template: " + templateName, e);
        }
    }

    public String loadServiceTemplate() {
        return loadTemplate("service-test.ftl");
    }

    public String loadControllerTemplate() {
        return loadTemplate("controller-test.ftl");
    }

    public String loadRepositoryTemplate() {
        return loadTemplate("repository-test.ftl");
    }

    public String loadIntegrationTemplate() {
        return loadTemplate("integration-test.ftl");
    }

    public void clearCache() {
        logger.debug("Clearing template cache");
        templateCache.clear();
    }

    public boolean isCached(String templateName) {
        return templateCache.containsKey(templateName);
    }
}
