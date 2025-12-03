package com.springtest.core.model;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassInfoTest {

    @Test
    void hasAnnotation_WhenAnnotationExists_ShouldReturnTrue() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .annotations(List.of(service))
                .build();

        assertThat(classInfo.hasAnnotation("Service")).isTrue();
    }

    @Test
    void getSpringStereotype_WithServiceAnnotation_ShouldReturnService() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .annotations(List.of(service))
                .build();

        assertThat(classInfo.getSpringStereotype()).isEqualTo("Service");
    }

    @Test
    void getSpringStereotype_WithControllerAnnotation_ShouldReturnController() {
        AnnotationInfo controller = AnnotationInfo.builder()
                .simpleName("Controller")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .annotations(List.of(controller))
                .build();

        assertThat(classInfo.getSpringStereotype()).isEqualTo("Controller");
    }

    @Test
    void getSpringStereotype_WithNoStereotype_ShouldReturnNull() {
        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserUtil")
                .annotations(List.of())
                .build();

        assertThat(classInfo.getSpringStereotype()).isNull();
    }

    @Test
    void isSpringComponent_WithStereotype_ShouldReturnTrue() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .annotations(List.of(service))
                .build();

        assertThat(classInfo.isSpringComponent()).isTrue();
    }

    @Test
    void isRestController_WithRestControllerAnnotation_ShouldReturnTrue() {
        AnnotationInfo restController = AnnotationInfo.builder()
                .simpleName("RestController")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .annotations(List.of(restController))
                .build();

        assertThat(classInfo.isRestController()).isTrue();
    }

    @Test
    void isController_WithControllerAnnotation_ShouldReturnTrue() {
        AnnotationInfo controller = AnnotationInfo.builder()
                .simpleName("Controller")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .annotations(List.of(controller))
                .build();

        assertThat(classInfo.isController()).isTrue();
    }

    @Test
    void isService_WithServiceAnnotation_ShouldReturnTrue() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .annotations(List.of(service))
                .build();

        assertThat(classInfo.isService()).isTrue();
    }

    @Test
    void isRepository_WithRepositoryAnnotation_ShouldReturnTrue() {
        AnnotationInfo repository = AnnotationInfo.builder()
                .simpleName("Repository")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserRepository")
                .annotations(List.of(repository))
                .build();

        assertThat(classInfo.isRepository()).isTrue();
    }

    @Test
    void getMockableFields_ShouldReturnOnlyMockableFields() {
        AnnotationInfo autowired = AnnotationInfo.builder()
                .simpleName("Autowired")
                .build();

        FieldInfo mockableField = FieldInfo.builder()
                .name("userRepository")
                .type("UserRepository")
                .simpleType("UserRepository")
                .annotations(List.of(autowired))
                .finalField(false)
                .staticField(false)
                .build();

        FieldInfo nonMockableField = FieldInfo.builder()
                .name("CONSTANT")
                .type("String")
                .simpleType("String")
                .annotations(List.of())
                .finalField(true)
                .staticField(false)
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .fields(List.of(mockableField, nonMockableField))
                .build();

        List<FieldInfo> mockableFields = classInfo.getMockableFields();

        assertThat(mockableFields).hasSize(1);
        assertThat(mockableFields.get(0).getName()).isEqualTo("userRepository");
    }

    @Test
    void getTestClassName_ShouldAppendTest() {
        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .build();

        assertThat(classInfo.getTestClassName()).isEqualTo("UserServiceTest");
    }

    @Test
    void getTestType_ForRestController_ShouldReturnWebmvc() {
        AnnotationInfo restController = AnnotationInfo.builder()
                .simpleName("RestController")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserController")
                .annotations(List.of(restController))
                .build();

        assertThat(classInfo.getTestType()).isEqualTo("webmvc");
    }

    @Test
    void getTestType_ForRepository_ShouldReturnDatajpa() {
        AnnotationInfo repository = AnnotationInfo.builder()
                .simpleName("Repository")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserRepository")
                .annotations(List.of(repository))
                .build();

        assertThat(classInfo.getTestType()).isEqualTo("datajpa");
    }

    @Test
    void getTestType_ForService_ShouldReturnUnit() {
        AnnotationInfo service = AnnotationInfo.builder()
                .simpleName("Service")
                .build();

        ClassInfo classInfo = ClassInfo.builder()
                .simpleName("UserService")
                .annotations(List.of(service))
                .build();

        assertThat(classInfo.getTestType()).isEqualTo("unit");
    }
}