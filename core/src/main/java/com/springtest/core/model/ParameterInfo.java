package com.springtest.core.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;


@Value
@Builder
public class ParameterInfo {

    String name;
    String type;
    String simpleType;
    List<AnnotationInfo> annotations;

    boolean primitive;
    boolean collection;
    boolean nullable;


    public boolean hasAnnotation(String annotationSimpleName) {
        return annotations != null && annotations.stream()
                .anyMatch(a -> a.matches(annotationSimpleName));
    }


    public boolean isValidated() {
        return hasAnnotation("Valid")
                || hasAnnotation("NotNull")
                || hasAnnotation("NotEmpty")
                || hasAnnotation("NotBlank");
    }


    public boolean isSpringWebParameter() {
        return hasAnnotation("PathVariable")
                || hasAnnotation("RequestParam")
                || hasAnnotation("RequestBody")
                || hasAnnotation("RequestHeader");
    }
}