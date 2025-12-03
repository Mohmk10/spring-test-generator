package com.springtest.core.template;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TemplateLoader {

    public String loadTemplate(String templateName) {
        try (InputStream is = getClass().getResourceAsStream("/templates/" + templateName)) {
            if (is == null) {
                log.warn("Template not found: {}", templateName);
                return getDefaultTemplate();
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load template: {}", templateName, e);
            return getDefaultTemplate();
        }
    }

    private String getDefaultTemplate() {
        return """
            package ${packageName};
            
            ${imports}
            
            ${classAnnotations}
            class ${className} {
                
                ${testFields}
                
                ${testCases}
            }
            """;
    }

    public boolean templateExists(String templateName) {
        try (InputStream is = getClass().getResourceAsStream("/templates/" + templateName)) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }
}