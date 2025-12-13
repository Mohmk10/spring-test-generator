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

/**
 * Analyzes method declarations and extracts detailed method information.
 * Package-private utility class for internal use.
 */
class MethodAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(MethodAnalyzer.class);

    /**
     * Analyzes a method declaration and extracts all relevant information.
     *
     * @param method JavaParser method declaration
     * @return MethodInfo object with complete method metadata
     */
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

    /**
     * Analyzes all parameters of a method.
     *
     * @param method JavaParser method declaration
     * @return List of ParameterInfo objects
     */
    static List<ParameterInfo> analyzeParameters(MethodDeclaration method) {
        List<ParameterInfo> parameters = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            parameters.add(analyzeParameter(parameter));
        }

        return parameters;
    }

    /**
     * Analyzes a single parameter and extracts its information.
     *
     * @param parameter JavaParser parameter
     * @return ParameterInfo object
     */
    static ParameterInfo analyzeParameter(Parameter parameter) {
        String name = parameter.getNameAsString();
        String type = parameter.getType().asString();
        String qualifiedType = resolveQualifiedParameterType(parameter);

        List<AnnotationInfo> annotations = AnnotationDetector.extractAnnotations(parameter.getAnnotations());
        boolean required = ValidationDetector.isRequired(annotations);
        String genericType = extractGenericType(parameter);

        return new ParameterInfo(name, type, qualifiedType, annotations, required, genericType);
    }

    /**
     * Resolves the fully qualified return type of a method.
     *
     * @param method JavaParser method declaration
     * @return Fully qualified return type name
     */
    private static String resolveQualifiedReturnType(MethodDeclaration method) {
        try {
            return method.getType().resolve().describe();
        } catch (Exception e) {
            // If resolution fails, return simple type
            return method.getType().asString();
        }
    }

    /**
     * Resolves the fully qualified type of a parameter.
     *
     * @param parameter JavaParser parameter
     * @return Fully qualified parameter type name
     */
    private static String resolveQualifiedParameterType(Parameter parameter) {
        try {
            return parameter.getType().resolve().describe();
        } catch (Exception e) {
            // If resolution fails, return simple type
            return parameter.getType().asString();
        }
    }

    /**
     * Extracts generic type information from a parameter.
     *
     * @param parameter JavaParser parameter
     * @return Generic type string or null if not generic
     */
    private static String extractGenericType(Parameter parameter) {
        String typeStr = parameter.getType().asString();

        // Check if type contains generic information
        if (typeStr.contains("<") && typeStr.contains(">")) {
            return typeStr;
        }

        return null;
    }

    /**
     * Determines the access modifier of a method.
     *
     * @param method JavaParser method declaration
     * @return AccessModifier enum value
     */
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

    /**
     * Analyzes multiple methods from a class.
     *
     * @param methods List of JavaParser method declarations
     * @return List of MethodInfo objects
     */
    static List<MethodInfo> analyzeMethods(List<MethodDeclaration> methods) {
        List<MethodInfo> methodInfoList = new ArrayList<>();

        for (MethodDeclaration method : methods) {
            methodInfoList.add(analyzeMethod(method));
        }

        return methodInfoList;
    }
}
