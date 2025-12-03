package com.springtest.core.model;

import lombok.Builder;
import lombok.Value;
import java.util.Map;


@Value
@Builder
public class AnnotationInfo {

    String name;
    String simpleName;
    Map<String, String> attributes;

    public boolean matches(String simpleNameToMatch) {
        return this.simpleName.equals(simpleNameToMatch);
    }


    public boolean isSpringStereotype() {
        return matches("Service")
                || matches("Controller")
                || matches("RestController")
                || matches("Repository")
                || matches("Component");
    }


    public boolean isSpringWebMapping() {
        return matches("RequestMapping")
                || matches("GetMapping")
                || matches("PostMapping")
                || matches("PutMapping")
                || matches("DeleteMapping")
                || matches("PatchMapping");
    }


    public String getAttribute(String attributeName) {
        return attributes != null ? attributes.get(attributeName) : null;
    }
}