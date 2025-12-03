package com.springtest.core.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;


@Value
@Builder
public class FieldInfo {

    String name;
    String type;
    String simpleType;
    List<AnnotationInfo> annotations;

    boolean finalField;
    boolean staticField;


    public boolean hasAnnotation(String annotationSimpleName) {
        return annotations != null && annotations.stream()
                .anyMatch(a -> a.matches(annotationSimpleName));
    }


    public boolean shouldBeMocked() {
        return hasAnnotation("Autowired")
                || (!finalField && !staticField && !isPrimitiveOrString());
    }


    public boolean isSpringBean() {
        return hasAnnotation("Autowired")
                || hasAnnotation("Qualifier")
                || hasAnnotation("Resource");
    }


    private boolean isPrimitiveOrString() {
        return simpleType.equals("String")
                || simpleType.equals("int")
                || simpleType.equals("Integer")
                || simpleType.equals("long")
                || simpleType.equals("Long")
                || simpleType.equals("boolean")
                || simpleType.equals("Boolean")
                || simpleType.equals("double")
                || simpleType.equals("Double");
    }
}