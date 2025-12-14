package com.springtest.mock;

import com.springtest.model.ClassInfo;
import com.springtest.model.FieldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockGenerator {
    private static final Logger logger = LoggerFactory.getLogger(MockGenerator.class);

    private final boolean useSpringBootTest;

    public MockGenerator() {
        this(false);
    }

    public MockGenerator(boolean useSpringBootTest) {
        this.useSpringBootTest = useSpringBootTest;
    }

    public String generateMocks(ClassInfo classInfo) {
        logger.debug("Generating mocks for class: {}", classInfo.simpleName());

        List<String> mockDeclarations = new ArrayList<>();

        for (FieldInfo field : classInfo.getInjectedFields()) {
            String mockDeclaration = generateMockForField(field);
            mockDeclarations.add(mockDeclaration);
        }

        String injectMocksDeclaration = generateInjectMocks(classInfo);
        mockDeclarations.add(injectMocksDeclaration);

        return String.join("\n\n", mockDeclarations);
    }

    public String generateMockForField(FieldInfo field) {
        String annotation = useSpringBootTest ? "@MockBean" : "@Mock";
        return String.format("%s\nprivate %s %s;",
                annotation,
                field.type(),
                field.name());
    }

    public String generateInjectMocks(ClassInfo classInfo) {
        String simpleName = classInfo.simpleName();
        String fieldName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);

        return String.format("@InjectMocks\nprivate %s %s;",
                simpleName,
                fieldName);
    }

    public List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        if (useSpringBootTest) {
            imports.add("import org.springframework.boot.test.mock.mockito.MockBean;");
        } else {
            imports.add("import org.mockito.Mock;");
        }

        imports.add("import org.mockito.InjectMocks;");

        for (FieldInfo field : classInfo.getInjectedFields()) {
            if (field.qualifiedType() != null && !field.qualifiedType().equals(field.type())) {
                imports.add(String.format("import %s;", field.qualifiedType()));
            }
        }

        return imports.stream().distinct().sorted().collect(Collectors.toList());
    }

    public String generateFieldDeclarations(ClassInfo classInfo) {
        List<String> declarations = new ArrayList<>();

        for (FieldInfo field : classInfo.getInjectedFields()) {
            declarations.add(String.format("private %s %s;", field.type(), field.name()));
        }

        String simpleName = classInfo.simpleName();
        String fieldName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        declarations.add(String.format("private %s %s;", simpleName, fieldName));

        return String.join("\n", declarations);
    }

    public String generateSetupMethod(ClassInfo classInfo) {
        StringBuilder setup = new StringBuilder();
        setup.append("@BeforeEach\n");
        setup.append("void setUp() {\n");

        for (FieldInfo field : classInfo.getInjectedFields()) {
            setup.append(String.format("    %s = mock(%s.class);\n",
                    field.name(),
                    field.type()));
        }

        String simpleName = classInfo.simpleName();
        String fieldName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);

        if (!classInfo.getInjectedFields().isEmpty()) {
            setup.append(String.format("    %s = new %s(",
                    fieldName,
                    simpleName));

            String params = classInfo.getInjectedFields().stream()
                    .map(FieldInfo::name)
                    .collect(Collectors.joining(", "));

            setup.append(params);
            setup.append(");\n");
        } else {
            setup.append(String.format("    %s = new %s();\n",
                    fieldName,
                    simpleName));
        }

        setup.append("}");

        return setup.toString();
    }

    public boolean requiresMocks(ClassInfo classInfo) {
        return !classInfo.getInjectedFields().isEmpty();
    }

    public String generateMocksForDependencies(List<String> dependencies) {
        return dependencies.stream()
                .map(this::generateMockForDependency)
                .collect(Collectors.joining("\n\n"));
    }

    private String generateMockForDependency(String dependencyType) {
        String annotation = useSpringBootTest ? "@MockBean" : "@Mock";
        String fieldName = Character.toLowerCase(dependencyType.charAt(0)) + dependencyType.substring(1);

        return String.format("%s\nprivate %s %s;",
                annotation,
                dependencyType,
                fieldName);
    }
}
