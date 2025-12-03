package com.springtest.core.analyzer;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.springtest.core.model.AnnotationInfo;
import com.springtest.core.model.FieldInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DependencyAnalyzer {

    private final AnnotationDetector annotationDetector;

    public List<FieldInfo> analyzeFields(List<FieldDeclaration> fields) {
        return fields.stream()
                .flatMap(field -> field.getVariables().stream()
                        .map(var -> createFieldInfo(field, var)))
                .toList();
    }

    private FieldInfo createFieldInfo(FieldDeclaration field, VariableDeclarator variable) {
        String type = variable.getTypeAsString();
        String simpleName = extractSimpleType(type);

        List<AnnotationInfo> annotations = annotationDetector
                .extractAnnotations(field.getAnnotations());

        return FieldInfo.builder()
                .name(variable.getNameAsString())
                .type(type)
                .simpleType(simpleName)
                .annotations(annotations)
                .finalField(field.isFinal())
                .staticField(field.isStatic())
                .build();
    }

    private String extractSimpleType(String fullType) {
        String withoutGenerics = fullType.replaceAll("<.*>", "");

        if (withoutGenerics.contains(".")) {
            return withoutGenerics.substring(withoutGenerics.lastIndexOf(".") + 1);
        }

        return withoutGenerics;
    }

    public List<FieldInfo> getMockableFields(List<FieldInfo> allFields) {
        return allFields.stream()
                .filter(FieldInfo::shouldBeMocked)
                .toList();
    }
}