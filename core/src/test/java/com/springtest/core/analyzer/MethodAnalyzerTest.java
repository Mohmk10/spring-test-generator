package com.springtest.core.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.springtest.core.model.MethodInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MethodAnalyzerTest {

    private MethodAnalyzer analyzer;
    private JavaParser parser;

    @BeforeEach
    void setUp() {
        AnnotationDetector annotationDetector = new AnnotationDetector();
        ExceptionDetector exceptionDetector = new ExceptionDetector();
        analyzer = new MethodAnalyzer(annotationDetector, exceptionDetector);
        parser = new JavaParser();
    }

    @Test
    void analyzeMethods_ShouldDetectPublicMethods() {
        String code = """
            public class UserService {
                public User findById(Long id) {
                    return null;
                }
                
                private void privateMethod() {
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods).hasSize(1);
        assertThat(methods.get(0).getName()).isEqualTo("findById");
    }

    @Test
    void analyzeMethods_ShouldDetectReturnType() {
        String code = """
            public class UserService {
                public User findById(Long id) {
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).getReturnType()).isEqualTo("User");
        assertThat(methods.get(0).getSimpleReturnType()).isEqualTo("User");
    }

    @Test
    void analyzeMethods_ShouldDetectVoidReturn() {
        String code = """
            public class UserService {
                public void deleteUser(Long id) {
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).isVoidReturn()).isTrue();
    }

    @Test
    void analyzeMethods_ShouldDetectOptionalReturn() {
        String code = """
            import java.util.Optional;
            
            public class UserService {
                public Optional<User> findById(Long id) {
                    return Optional.empty();
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).isReturnsOptional()).isTrue();
    }

    @Test
    void analyzeMethods_ShouldDetectCollectionReturn() {
        String code = """
            import java.util.List;
            
            public class UserService {
                public List<User> findAll() {
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).isReturnsCollection()).isTrue();
    }

    @Test
    void analyzeMethods_ShouldDetectParameters() {
        String code = """
            public class UserService {
                public User findById(Long id, String name) {
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).getParameters()).hasSize(2);
        assertThat(methods.get(0).getParameters().get(0).getName()).isEqualTo("id");
        assertThat(methods.get(0).getParameters().get(0).getType()).isEqualTo("Long");
        assertThat(methods.get(0).getParameters().get(1).getName()).isEqualTo("name");
        assertThat(methods.get(0).getParameters().get(1).getType()).isEqualTo("String");
    }

    @Test
    void analyzeMethods_ShouldDetectPrimitiveParameters() {
        String code = """
            public class UserService {
                public void updateAge(int age) {
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).getParameters().get(0).isPrimitive()).isTrue();
    }

    @Test
    void analyzeMethods_ShouldDetectParameterAnnotations() {
        String code = """
            public class UserService {
                public User create(@Valid UserRequest request) {
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).getParameters().get(0).getAnnotations()).hasSize(1);
        assertThat(methods.get(0).getParameters().get(0).getAnnotations().get(0).getSimpleName())
                .isEqualTo("Valid");
    }

    @Test
    void analyzeMethods_ShouldDetectMethodAnnotations() {
        String code = """
            public class UserController {
                @GetMapping("/users/{id}")
                public User getUser(@PathVariable Long id) {
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserController").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).getAnnotations()).hasSize(1);
        assertThat(methods.get(0).getAnnotations().get(0).getSimpleName()).isEqualTo("GetMapping");
    }

    @Test
    void analyzeMethods_ShouldDetectDeclaredExceptions() {
        String code = """
            public class UserService {
                public User findById(Long id) throws UserNotFoundException {
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).getDeclaredExceptions()).hasSize(1);
        assertThat(methods.get(0).getDeclaredExceptions().get(0).getSimpleExceptionType())
                .isEqualTo("UserNotFoundException");
    }

    @Test
    void analyzeMethods_ShouldDetectThrownExceptions() {
        String code = """
            public class UserService {
                public User findById(Long id) {
                    if (id == null) {
                        throw new IllegalArgumentException("ID cannot be null");
                    }
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).getThrownExceptions()).isNotEmpty();
    }

    @Test
    void analyzeMethods_ShouldCalculateComplexity() {
        String code = """
            public class UserService {
                public User findById(Long id) throws UserNotFoundException {
                    if (id == null) {
                        throw new IllegalArgumentException("ID cannot be null");
                    }
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<MethodInfo> methods = analyzer.analyzeMethods(classDecl.getMethods());

        assertThat(methods.get(0).getComplexity()).isGreaterThan(1);
    }
}