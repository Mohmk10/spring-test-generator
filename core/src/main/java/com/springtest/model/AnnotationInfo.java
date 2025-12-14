package com.springtest.model;

import java.util.Map;
import java.util.Set;

public record AnnotationInfo(
        String name,
        String qualifiedName,
        Map<String, Object> attributes
) {
    private static final Set<String> SPRING_COMPONENT_ANNOTATIONS = Set.of(
            "org.springframework.stereotype.Component",
            "org.springframework.stereotype.Service",
            "org.springframework.stereotype.Repository",
            "org.springframework.stereotype.Controller",
            "org.springframework.web.bind.annotation.RestController",
            "org.springframework.context.annotation.Configuration"
    );

    private static final Set<String> WEB_MAPPING_ANNOTATIONS = Set.of(
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping"
    );

    private static final Set<String> VALIDATION_ANNOTATIONS = Set.of(
            "jakarta.validation.constraints.NotNull",
            "jakarta.validation.constraints.NotBlank",
            "jakarta.validation.constraints.NotEmpty",
            "jakarta.validation.constraints.Size",
            "jakarta.validation.constraints.Min",
            "jakarta.validation.constraints.Max",
            "jakarta.validation.constraints.Pattern",
            "jakarta.validation.constraints.Email",
            "jakarta.validation.Valid",
            "org.springframework.validation.annotation.Validated"
    );

    private static final Set<String> INJECTION_ANNOTATIONS = Set.of(
            "org.springframework.beans.factory.annotation.Autowired",
            "jakarta.inject.Inject",
            "org.springframework.beans.factory.annotation.Value",
            "org.springframework.beans.factory.annotation.Qualifier"
    );

    public boolean isSpringComponent() {
        return SPRING_COMPONENT_ANNOTATIONS.contains(qualifiedName);
    }

    public boolean isWebMapping() {
        return WEB_MAPPING_ANNOTATIONS.contains(qualifiedName);
    }

    public boolean isValidation() {
        return VALIDATION_ANNOTATIONS.contains(qualifiedName);
    }

    public boolean isInjection() {
        return INJECTION_ANNOTATIONS.contains(qualifiedName);
    }
}
