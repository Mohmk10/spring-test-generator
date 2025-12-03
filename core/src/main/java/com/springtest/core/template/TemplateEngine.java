package com.springtest.core.template;

import com.springtest.core.model.TestSuite;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TemplateEngine {

    private final Configuration configuration;
    private final TemplateLoader templateLoader;

    public TemplateEngine() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_32);
        this.templateLoader = new TemplateLoader();
        configureFreemarker();
    }

    private void configureFreemarker() {
        configuration.setClassForTemplateLoading(getClass(), "/templates");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
    }

    public String generateTestClass(TestSuite testSuite) {
        try {
            String templateName = determineTemplate(testSuite.getTestType());
            Template template = configuration.getTemplate(templateName);

            Map<String, Object> dataModel = buildDataModel(testSuite);

            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);

            return writer.toString();
        } catch (IOException | TemplateException e) {
            log.error("Failed to generate test class for: {}", testSuite.getTestClassName(), e);
            return generateFallbackTestClass(testSuite);
        }
    }

    public void writeTestClass(TestSuite testSuite, Path outputPath) throws IOException {
        String testClassContent = generateTestClass(testSuite);

        Path testFilePath = outputPath.resolve(testSuite.getTestClassName() + ".java");
        Files.createDirectories(testFilePath.getParent());
        Files.writeString(testFilePath, testClassContent);

        log.info("Generated test file: {}", testFilePath);
    }

    private String determineTemplate(String testType) {
        return switch (testType) {
            case "webmvc" -> "controller-test.ftl";
            case "datajpa" -> "repository-test.ftl";
            case "integration" -> "integration-test.ftl";
            default -> "service-test.ftl";
        };
    }

    private Map<String, Object> buildDataModel(TestSuite testSuite) {
        Map<String, Object> model = new HashMap<>();

        model.put("packageName", testSuite.getTestPackage());
        model.put("imports", testSuite.getImports());
        model.put("className", testSuite.getTestClassName());
        model.put("classAnnotations", testSuite.getClassAnnotations());
        model.put("testFields", testSuite.getTestFields());
        model.put("setupMethods", testSuite.getSetupMethods());
        model.put("testCases", testSuite.getTestCases());
        model.put("targetClass", testSuite.getTargetClass());

        return model;
    }

    private String generateFallbackTestClass(TestSuite testSuite) {
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(testSuite.getTestPackage()).append(";\n\n");

        testSuite.getImports().forEach(imp -> sb.append(imp).append("\n"));
        sb.append("\n");

        testSuite.getClassAnnotations().forEach(ann -> sb.append(ann).append("\n"));
        sb.append("class ").append(testSuite.getTestClassName()).append(" {\n\n");

        testSuite.getTestFields().forEach(field -> sb.append("    ").append(field).append("\n\n"));

        sb.append("    // TODO: Implement test methods\n");

        sb.append("}\n");

        return sb.toString();
    }
}