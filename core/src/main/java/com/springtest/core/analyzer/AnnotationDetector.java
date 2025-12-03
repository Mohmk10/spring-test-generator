package com.springtest.core.analyzer;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.springtest.core.model.AnnotationInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AnnotationDetector {

    public List<AnnotationInfo> extractAnnotations(List<AnnotationExpr> annotations) {
        return annotations.stream()
                .map(this::parseAnnotation)
                .toList();
    }

    private AnnotationInfo parseAnnotation(AnnotationExpr annotation) {
        String name = annotation.getNameAsString();
        String simpleName = name.contains(".")
                ? name.substring(name.lastIndexOf(".") + 1)
                : name;

        Map<String, String> attributes = extractAttributes(annotation);

        return AnnotationInfo.builder()
                .name(name)
                .simpleName(simpleName)
                .attributes(attributes)
                .build();
    }

    private Map<String, String> extractAttributes(AnnotationExpr annotation) {
        Map<String, String> attributes = new HashMap<>();

        if (annotation instanceof NormalAnnotationExpr normalAnnotation) {
            for (MemberValuePair pair : normalAnnotation.getPairs()) {
                attributes.put(
                        pair.getNameAsString(),
                        pair.getValue().toString()
                );
            }
        } else if (annotation instanceof SingleMemberAnnotationExpr singleMember) {
            attributes.put("value", singleMember.getMemberValue().toString());
        }

        return attributes;
    }

    public boolean hasSpringStereotype(List<AnnotationInfo> annotations) {
        return annotations.stream()
                .anyMatch(AnnotationInfo::isSpringStereotype);
    }

    public boolean isTestableClass(List<AnnotationInfo> annotations) {
        return hasSpringStereotype(annotations);
    }
}