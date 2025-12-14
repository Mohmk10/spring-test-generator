package com.springtest.analyzer;

import com.springtest.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClassScannerTest {

    private ClassScanner classScanner;

    @BeforeEach
    void setUp() {
        classScanner = new ClassScanner();
    }

    @Test
    void shouldParseServiceClass_WhenSourceCodeIsValid() {

        String sourceCode = """
                package com.example.service;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService {
                    private String name;

                    public String getName() {
                        return name;
                    }
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();
        assertThat(classInfo.simpleName()).isEqualTo("UserService");
        assertThat(classInfo.qualifiedName()).isEqualTo("com.example.service.UserService");
        assertThat(classInfo.packageName()).isEqualTo("com.example.service");
        assertThat(classInfo.classType()).isEqualTo(ClassType.SERVICE);
        assertThat(classInfo.isSpringComponent()).isTrue();
    }

    @Test
    void shouldExtractAutowiredFields_WhenClassHasAutowiredFields() {

        String sourceCode = """
                package com.example.controller;

                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.stereotype.Controller;

                @Controller
                public class UserController {
                    @Autowired
                    private UserService userService;

                    @Autowired
                    private UserRepository userRepository;

                    private String regularField;
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.fields()).hasSize(3);
        assertThat(classInfo.getInjectedFields()).hasSize(2);

        assertThat(classInfo.getInjectedFields())
                .extracting(FieldInfo::name)
                .containsExactlyInAnyOrder("userService", "userRepository");

        assertThat(classInfo.fields())
                .filteredOn(field -> field.name().equals("regularField"))
                .hasSize(1)
                .first()
                .satisfies(field -> assertThat(field.injected()).isFalse());
    }

    @Test
    void shouldExtractMethods_WhenClassHasMethods() {

        String sourceCode = """
                package com.example.service;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService {
                    public String findById(Long id) {
                        return "user";
                    }

                    public void deleteUser(Long id) {

                    }

                    private String helperMethod() {
                        return "helper";
                    }
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.methods()).hasSize(3);
        assertThat(classInfo.methods())
                .extracting(MethodInfo::name)
                .containsExactlyInAnyOrder("findById", "deleteUser", "helperMethod");

        MethodInfo findByIdMethod = classInfo.methods().stream()
                .filter(m -> m.name().equals("findById"))
                .findFirst()
                .orElseThrow();

        assertThat(findByIdMethod.returnType()).isEqualTo("String");
        assertThat(findByIdMethod.parameters()).hasSize(1);
        assertThat(findByIdMethod.parameters().get(0).name()).isEqualTo("id");
        assertThat(findByIdMethod.parameters().get(0).type()).isEqualTo("Long");
        assertThat(findByIdMethod.accessModifier()).isEqualTo(AccessModifier.PUBLIC);
    }

    @Test
    void shouldExtractMethodParameters_WhenMethodHasParameters() {

        String sourceCode = """
                package com.example.controller;

                import org.springframework.web.bind.annotation.RestController;
                import org.springframework.web.bind.annotation.PostMapping;
                import jakarta.validation.Valid;
                import jakarta.validation.constraints.NotNull;

                @RestController
                public class UserController {
                    @PostMapping("/users")
                    public String createUser(@Valid @NotNull String name, int age) {
                        return "created";
                    }
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.methods()).hasSize(1);
        MethodInfo method = classInfo.methods().get(0);

        assertThat(method.parameters()).hasSize(2);

        ParameterInfo nameParam = method.parameters().get(0);
        assertThat(nameParam.name()).isEqualTo("name");
        assertThat(nameParam.type()).isEqualTo("String");
        assertThat(nameParam.annotations()).hasSize(2);
        assertThat(nameParam.required()).isTrue();

        ParameterInfo ageParam = method.parameters().get(1);
        assertThat(ageParam.name()).isEqualTo("age");
        assertThat(ageParam.type()).isEqualTo("int");
        assertThat(ageParam.isPrimitive()).isTrue();
    }

    @Test
    void shouldDetectWebMappingMethods_WhenClassIsRestController() {

        String sourceCode = """
                package com.example.controller;

                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api/users")
                public class UserController {
                    @GetMapping
                    public String getUsers() {
                        return "users";
                    }

                    @PostMapping
                    public String createUser(String name) {
                        return "created";
                    }

                    private void helperMethod() {

                    }
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.isRestController()).isTrue();
        assertThat(classInfo.getWebMappingMethods()).hasSize(2);
        assertThat(classInfo.getWebMappingMethods())
                .extracting(MethodInfo::name)
                .containsExactlyInAnyOrder("getUsers", "createUser");
    }

    @Test
    void shouldExtractDeclaredExceptions_WhenMethodDeclaresExceptions() {

        String sourceCode = """
                package com.example.service;

                import org.springframework.stereotype.Service;
                import java.io.IOException;

                @Service
                public class FileService {
                    public String readFile() throws IOException, IllegalArgumentException {
                        return "content";
                    }
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.methods()).hasSize(1);
        MethodInfo method = classInfo.methods().get(0);

        assertThat(method.thrownExceptions()).hasSize(2);
        assertThat(method.thrownExceptions())
                .containsExactlyInAnyOrder("IOException", "IllegalArgumentException");
    }

    @Test
    void shouldParseFileSuccessfully_WhenFileExists(@TempDir Path tempDir) throws IOException {

        String sourceCode = """
                package com.example;

                import org.springframework.stereotype.Service;

                @Service
                public class TestService {
                    public void doSomething() {
                    }
                }
                """;

        File javaFile = tempDir.resolve("TestService.java").toFile();
        Files.writeString(javaFile.toPath(), sourceCode);

        Optional<ClassInfo> result = classScanner.scanFile(javaFile);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();
        assertThat(classInfo.simpleName()).isEqualTo("TestService");
        assertThat(classInfo.sourcePath()).isEqualTo(javaFile.getAbsolutePath());
    }

    @Test
    void shouldThrowException_WhenFileDoesNotExist() {

        File nonExistentFile = new File("/path/to/nonexistent/file.java");

        assertThatThrownBy(() -> classScanner.scanFile(nonExistentFile))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    void shouldReturnEmpty_WhenSourceCodeIsInvalid() {

        String invalidSourceCode = """
                package com.example;

                public class InvalidClass {

                """;

        Optional<ClassInfo> result = classScanner.scanSource(invalidSourceCode);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldExtractDependencies_WhenClassHasInjectedFields() {

        String sourceCode = """
                package com.example.controller;

                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.stereotype.Controller;

                @Controller
                public class UserController {
                    @Autowired
                    private UserService userService;

                    @Autowired
                    private UserRepository userRepository;
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.dependencies()).hasSize(2);
        assertThat(classInfo.dependencies())
                .containsExactlyInAnyOrder("UserService", "UserRepository");
    }

    @Test
    void shouldExtractImplementedInterfaces_WhenClassImplementsInterfaces() {

        String sourceCode = """
                package com.example;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService implements UserOperations, Auditable {
                    public void doSomething() {
                    }
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.implementedInterfaces()).hasSize(2);
        assertThat(classInfo.implementedInterfaces())
                .containsExactlyInAnyOrder("UserOperations", "Auditable");
    }

    @Test
    void shouldExtractSuperClass_WhenClassExtendsAnotherClass() {

        String sourceCode = """
                package com.example;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService extends BaseService {
                    public void doSomething() {
                    }
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.superClass()).isEqualTo("BaseService");
    }

    @Test
    void shouldDetectInterface_WhenTypeIsInterface() {

        String sourceCode = """
                package com.example;

                public interface UserRepository {
                    void save(User user);
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.isInterface()).isTrue();
        assertThat(classInfo.isAbstract()).isFalse();
    }

    @Test
    void shouldDetectAbstractClass_WhenClassIsAbstract() {

        String sourceCode = """
                package com.example;

                public abstract class BaseService {
                    public abstract void doSomething();
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.isAbstract()).isTrue();
        assertThat(classInfo.isInterface()).isFalse();
    }

    @Test
    void shouldDetectFieldAccessModifiers_WhenFieldsHaveDifferentModifiers() {

        String sourceCode = """
                package com.example;

                public class TestClass {
                    public String publicField;
                    protected String protectedField;
                    private String privateField;
                    String packagePrivateField;
                }
                """;

        Optional<ClassInfo> result = classScanner.scanSource(sourceCode);

        assertThat(result).isPresent();
        ClassInfo classInfo = result.get();

        assertThat(classInfo.fields()).hasSize(4);

        assertThat(classInfo.fields())
                .filteredOn(f -> f.name().equals("publicField"))
                .hasSize(1)
                .first()
                .satisfies(f -> assertThat(f.accessModifier()).isEqualTo(AccessModifier.PUBLIC));

        assertThat(classInfo.fields())
                .filteredOn(f -> f.name().equals("protectedField"))
                .hasSize(1)
                .first()
                .satisfies(f -> assertThat(f.accessModifier()).isEqualTo(AccessModifier.PROTECTED));

        assertThat(classInfo.fields())
                .filteredOn(f -> f.name().equals("privateField"))
                .hasSize(1)
                .first()
                .satisfies(f -> assertThat(f.accessModifier()).isEqualTo(AccessModifier.PRIVATE));

        assertThat(classInfo.fields())
                .filteredOn(f -> f.name().equals("packagePrivateField"))
                .hasSize(1)
                .first()
                .satisfies(f -> assertThat(f.accessModifier()).isEqualTo(AccessModifier.PACKAGE_PRIVATE));
    }
}
