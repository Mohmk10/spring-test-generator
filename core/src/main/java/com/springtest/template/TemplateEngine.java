package com.springtest.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class TemplateEngine {
    private static final Logger logger = LoggerFactory.getLogger(TemplateEngine.class);

    private final TemplateLoader templateLoader;
    private final Configuration configuration;

    public TemplateEngine() {
        this(new TemplateLoader());
    }

    public TemplateEngine(TemplateLoader templateLoader) {
        this.templateLoader = templateLoader;
        this.configuration = createConfiguration();
    }

    private Configuration createConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        return cfg;
    }

    public String process(String templateName, Map<String, Object> dataModel) {
        if (templateName == null || templateName.isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        if (dataModel == null) {
            throw new IllegalArgumentException("Data model cannot be null");
        }

        logger.info("Processing template: {}", templateName);

        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (IOException e) {
            logger.error("Failed to load template: {}", templateName, e);
            throw new RuntimeException("Failed to load template: " + templateName, e);
        } catch (TemplateException e) {
            logger.error("Failed to process template: {}", templateName, e);
            throw new RuntimeException("Failed to process template: " + templateName, e);
        }
    }

    public String processServiceTest(Map<String, Object> dataModel) {
        return process("service-test.ftl", dataModel);
    }

    public String processControllerTest(Map<String, Object> dataModel) {
        return process("controller-test.ftl", dataModel);
    }

    public String processRepositoryTest(Map<String, Object> dataModel) {
        return process("repository-test.ftl", dataModel);
    }

    public String processIntegrationTest(Map<String, Object> dataModel) {
        return process("integration-test.ftl", dataModel);
    }
}
