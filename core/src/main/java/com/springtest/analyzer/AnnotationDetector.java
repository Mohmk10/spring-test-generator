package com.springtest.analyzer;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.springtest.model.AnnotationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Detects and extracts annotation information from JavaParser AST nodes.
 * Package-private utility class for internal use.
 */
class AnnotationDetector {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationDetector.class);

    /**
     * Extracts annotation information from a list of annotation expressions.
     *
     * @param annotations List of JavaParser annotation expressions
     * @return List of AnnotationInfo objects
     */
    static List<AnnotationInfo> extractAnnotations(List<AnnotationExpr> annotations) {
        return annotations.stream()
                .map(AnnotationDetector::extractAnnotation)
                .collect(Collectors.toList());
    }

    /**
     * Extracts annotation information from a single annotation expression.
     *
     * @param annotation JavaParser annotation expression
     * @return AnnotationInfo object
     */
    static AnnotationInfo extractAnnotation(AnnotationExpr annotation) {
        String name = annotation.getNameAsString();
        String qualifiedName = resolveQualifiedName(annotation);
        Map<String, Object> attributes = extractAttributes(annotation);

        logger.debug("Extracted annotation: {} (qualified: {})", name, qualifiedName);
        return new AnnotationInfo(name, qualifiedName, attributes);
    }

    /**
     * Resolves the fully qualified name of an annotation.
     * Attempts to resolve through the symbol solver if available.
     *
     * @param annotation JavaParser annotation expression
     * @return Fully qualified annotation name
     */
    private static String resolveQualifiedName(AnnotationExpr annotation) {
        try {
            return annotation.resolve().getQualifiedName();
        } catch (Exception e) {
            // If resolution fails, try to infer from common Spring/Jakarta annotations
            String name = annotation.getNameAsString();
            return inferQualifiedName(name);
        }
    }

    /**
     * Infers the qualified name for common annotations when symbol resolution fails.
     *
     * @param simpleName Simple annotation name
     * @return Inferred qualified name
     */
    private static String inferQualifiedName(String simpleName) {
        // Spring Framework annotations
        return switch (simpleName) {
            case "Service" -> "org.springframework.stereotype.Service";
            case "Controller" -> "org.springframework.stereotype.Controller";
            case "RestController" -> "org.springframework.web.bind.annotation.RestController";
            case "Repository" -> "org.springframework.stereotype.Repository";
            case "Component" -> "org.springframework.stereotype.Component";
            case "Configuration" -> "org.springframework.context.annotation.Configuration";
            case "Autowired" -> "org.springframework.beans.factory.annotation.Autowired";
            case "Value" -> "org.springframework.beans.factory.annotation.Value";
            case "Qualifier" -> "org.springframework.beans.factory.annotation.Qualifier";
            case "RequestMapping" -> "org.springframework.web.bind.annotation.RequestMapping";
            case "GetMapping" -> "org.springframework.web.bind.annotation.GetMapping";
            case "PostMapping" -> "org.springframework.web.bind.annotation.PostMapping";
            case "PutMapping" -> "org.springframework.web.bind.annotation.PutMapping";
            case "DeleteMapping" -> "org.springframework.web.bind.annotation.DeleteMapping";
            case "PatchMapping" -> "org.springframework.web.bind.annotation.PatchMapping";
            case "RequestBody" -> "org.springframework.web.bind.annotation.RequestBody";
            case "RequestParam" -> "org.springframework.web.bind.annotation.RequestParam";
            case "PathVariable" -> "org.springframework.web.bind.annotation.PathVariable";
            // Jakarta/Java EE validation annotations
            case "NotNull" -> "jakarta.validation.constraints.NotNull";
            case "NotBlank" -> "jakarta.validation.constraints.NotBlank";
            case "NotEmpty" -> "jakarta.validation.constraints.NotEmpty";
            case "Size" -> "jakarta.validation.constraints.Size";
            case "Min" -> "jakarta.validation.constraints.Min";
            case "Max" -> "jakarta.validation.constraints.Max";
            case "Pattern" -> "jakarta.validation.constraints.Pattern";
            case "Email" -> "jakarta.validation.constraints.Email";
            case "Valid" -> "jakarta.validation.Valid";
            case "Validated" -> "org.springframework.validation.annotation.Validated";
            case "Inject" -> "jakarta.inject.Inject";
            default -> simpleName;
        };
    }

    /**
     * Extracts attributes from an annotation.
     *
     * @param annotation JavaParser annotation expression
     * @return Map of attribute names to values
     */
    private static Map<String, Object> extractAttributes(AnnotationExpr annotation) {
        Map<String, Object> attributes = new HashMap<>();

        if (annotation instanceof NormalAnnotationExpr normalAnnotation) {
            for (MemberValuePair pair : normalAnnotation.getPairs()) {
                attributes.put(pair.getNameAsString(), pair.getValue().toString());
            }
        } else if (annotation instanceof SingleMemberAnnotationExpr singleMemberAnnotation) {
            attributes.put("value", singleMemberAnnotation.getMemberValue().toString());
        }

        return attributes;
    }
}
