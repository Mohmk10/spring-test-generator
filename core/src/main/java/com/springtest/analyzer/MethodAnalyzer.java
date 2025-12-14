package com.springtest.analyzer;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.springtest.model.AccessModifier;
import com.springtest.model.AnnotationInfo;
import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class MethodAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(MethodAnalyzer.class);

    static MethodInfo analyzeMethod(MethodDeclaration method) {
        String name = method.getNameAsString();
        String returnType = method.getType().asString();
        String qualifiedReturnType = resolveQualifiedReturnType(method);

        List<ParameterInfo> parameters = analyzeParameters(method);
        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(method.getAnnotations());

        List<String> thrownExceptions = ExceptionDetector.extractDeclaredExceptions(method);
        List<String> possibleExceptions = ExceptionDetector.detectThrownExceptions(method);

        boolean hasValidation = ValidationDetector.hasValidation(method);
        AccessModifier accessModifier = determineAccessModifier(method);
        boolean isStatic = method.isStatic();
        boolean isAbstract = method.isAbstract();

        logger.debug("Analyzed method: {} with {} parameters", name, parameters.size());

        return new MethodInfo(
                name,
                returnType,
                qualifiedReturnType,
                parameters,
                annotations,
                thrownExceptions,
                possibleExceptions,
                hasValidation,
                accessModifier,
                isStatic,
                isAbstract
        );
    }

    static List<ParameterInfo> analyzeParameters(MethodDeclaration method) {
        List<ParameterInfo> parameters = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            parameters.add(analyzeParameter(parameter));
        }

        return parameters;
    }

    static ParameterInfo analyzeParameter(Parameter parameter) {
        String name = parameter.getNameAsString();
        String type = parameter.getType().asString();
        String qualifiedType = resolveQualifiedParameterType(parameter);

        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(parameter.getAnnotations());
        boolean required = ValidationDetector.isRequired(annotations);
        String genericType = extractGenericType(parameter);

        return new ParameterInfo(name, type, qualifiedType, annotations, required, genericType);
    }

    private static String resolveQualifiedReturnType(MethodDeclaration method) {
        try {
            return method.getType().resolve().describe();
        } catch (Exception e) {

            return method.getType().asString();
        }
    }

    private static String resolveQualifiedParameterType(Parameter parameter) {
        try {
            return parameter.getType().resolve().describe();
        } catch (Exception e) {

            return parameter.getType().asString();
        }
    }

    private static String extractGenericType(Parameter parameter) {
        String typeStr = parameter.getType().asString();

        if (typeStr.contains("<") && typeStr.contains(">")) {
            return typeStr;
        }

        return null;
    }

    private static AccessModifier determineAccessModifier(MethodDeclaration method) {
        if (method.hasModifier(Modifier.Keyword.PUBLIC)) {
            return AccessModifier.PUBLIC;
        } else if (method.hasModifier(Modifier.Keyword.PROTECTED)) {
            return AccessModifier.PROTECTED;
        } else if (method.hasModifier(Modifier.Keyword.PRIVATE)) {
            return AccessModifier.PRIVATE;
        } else {
            return AccessModifier.PACKAGE_PRIVATE;
        }
    }

    static List<MethodInfo> analyzeMethods(List<MethodDeclaration> methods) {
        List<MethodInfo> methodInfoList = new ArrayList<>();

        for (MethodDeclaration method : methods) {
            methodInfoList.add(analyzeMethod(method));
        }

        return methodInfoList;
    }
}
