package com.springtest.core.mock;

import com.springtest.core.model.ClassInfo;
import com.springtest.core.model.FieldInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MockGenerator {

    public List<String> generateMockFields(ClassInfo classInfo) {
        List<String> mockFields = new ArrayList<>();

        for (FieldInfo field : classInfo.getMockableFields()) {
            String mockAnnotation = determineMockAnnotation(classInfo, field);
            mockFields.add(String.format("@%s%nprivate %s %s;",
                    mockAnnotation,
                    field.getSimpleType(),
                    field.getName()
            ));
        }

        return mockFields;
    }

    public String generateTestedInstanceField(ClassInfo classInfo) {
        String annotation = determineTestedInstanceAnnotation(classInfo);

        return String.format("@%s%nprivate %s %s;",
                annotation,
                classInfo.getSimpleName(),
                toLowerCamelCase(classInfo.getSimpleName())
        );
    }

    private String determineMockAnnotation(ClassInfo classInfo, FieldInfo field) {
        String testType = classInfo.getTestType();

        return switch (testType) {
            case "webmvc" -> "MockBean";
            case "datajpa" -> "Mock";
            default -> "Mock";
        };
    }

    private String determineTestedInstanceAnnotation(ClassInfo classInfo) {
        String testType = classInfo.getTestType();

        return switch (testType) {
            case "webmvc" -> "Autowired";
            case "datajpa" -> "Autowired";
            default -> "InjectMocks";
        };
    }

    private String toLowerCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();
        String testType = classInfo.getTestType();

        if ("webmvc".equals(testType)) {
            imports.add("import org.springframework.boot.test.mock.mockito.MockBean;");
            imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        } else {
            imports.add("import org.mockito.Mock;");
            imports.add("import org.mockito.InjectMocks;");
        }

        return imports;
    }
}