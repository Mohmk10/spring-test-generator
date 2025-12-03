package com.springtest.core.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;


@Value
@Builder
public class ClassInfo {

    String fullyQualifiedName;
    String simpleName;
    String packageName;

    List<AnnotationInfo> annotations;
    List<FieldInfo> fields;
    List<MethodInfo> methods;

    boolean interfaceType;
    boolean abstractClass;

    String superClass;
    List<String> interfaces;


    public boolean hasAnnotation(String annotationSimpleName) {
        return annotations != null && annotations.stream()
                .anyMatch(a -> a.matches(annotationSimpleName));
    }


    public String getSpringStereotype() {
        if (annotations == null) return null;

        return annotations.stream()
                .filter(AnnotationInfo::isSpringStereotype)
                .map(AnnotationInfo::getSimpleName)
                .findFirst()
                .orElse(null);
    }


    public boolean isSpringComponent() {
        return getSpringStereotype() != null;
    }


    public boolean isRestController() {
        return hasAnnotation("RestController");
    }


    public boolean isController() {
        return hasAnnotation("Controller");
    }


    public boolean isService() {
        return hasAnnotation("Service");
    }


    public boolean isRepository() {
        return hasAnnotation("Repository");
    }


    public List<FieldInfo> getMockableFields() {
        return fields != null
                ? fields.stream().filter(FieldInfo::shouldBeMocked).toList()
                : List.of();
    }


    public String getTestClassName() {
        return simpleName + "Test";
    }


    public String getTestType() {
        if (isRestController() || isController()) return "webmvc";
        if (isRepository()) return "datajpa";
        if (isService()) return "unit";
        return "unit";
    }
}