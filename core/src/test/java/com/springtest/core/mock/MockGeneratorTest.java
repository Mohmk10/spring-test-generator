package com.springtest.core.mock;

import com.springtest.core.model.AnnotationInfo;
import com.springtest.core.model.ClassInfo;
import com.springtest.core.model.FieldInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MockGeneratorTest {

    private MockGenerator mockGenerator;

    @BeforeEach
    void setUp() {
        mockGenerator = new MockGenerator();
    }

    @Test
    void generateMockFields_ForServiceClass_ShouldGenerateMockAnnotations() {
        AnnotationInfo autowired = AnnotationInfo.builder()
                .simpleName("Autowired")
                .attributes(Map.of())
                .build();

        FieldInfo field = FieldInfo.builder()
                .name("userRepository")
                .type("UserRepository")
                .simpleType("UserRepository")
                .annotations(List.of(autowired))
                .finalField(false)
                .staticField(false)
                .build();

        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .packageName("com.example")
                .annotations(List.of(service))
                .fields(List.of(field))
                .build();

        List<String> mockFields = mockGenerator.generateMockFields(classInfo);

        assertThat(mockFields).hasSize(1);
        assertThat(mockFields.get(0)).contains("@Mock");
        assertThat(mockFields.get(0)).contains("UserRepository");
        assertThat(mockFields.get(0)).contains("userRepository");
    }

    @Test
    void generateTestedInstanceField_ForService_ShouldGenerateInjectMocks() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .packageName("com.example")
                .annotations(List.of(service))
                .fields(List.of())
                .build();

        String testedField = mockGenerator.generateTestedInstanceField(classInfo);

        assertThat(testedField).contains("@InjectMocks");
        assertThat(testedField).contains("UserService");
        assertThat(testedField).contains("userService");
    }

    @Test
    void generateTestedInstanceField_ForController_ShouldGenerateAutowired() {
        AnnotationInfo restController = AnnotationInfo.builder()
                .simpleName("RestController")
                .attributes(Map.of())
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .packageName("com.example")
                .annotations(List.of(restController))
                .fields(List.of())
                .build();

        String testedField = mockGenerator.generateTestedInstanceField(classInfo);

        assertThat(testedField).contains("@Autowired");
        assertThat(testedField).contains("UserController");
    }

    @Test
    void generateImports_ForService_ShouldIncludeMockitoImports() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .attributes(Map.of())
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .annotations(List.of(service))
                .build();

        List<String> imports = mockGenerator.generateImports(classInfo);

        assertThat(imports).contains("import org.mockito.Mock;");
        assertThat(imports).contains("import org.mockito.InjectMocks;");
    }

    @Test
    void generateImports_ForController_ShouldIncludeSpringImports() {
        AnnotationInfo restController = AnnotationInfo.builder()
                .simpleName("RestController")
                .attributes(Map.of())
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .annotations(List.of(restController))
                .build();

        List<String> imports = mockGenerator.generateImports(classInfo);

        assertThat(imports).contains("import org.springframework.boot.test.mock.mockito.MockBean;");
        assertThat(imports).contains("import org.springframework.beans.factory.annotation.Autowired;");
    }
}