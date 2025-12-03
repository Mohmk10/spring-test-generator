package com.springtest.core.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.springtest.core.model.ExceptionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionDetectorTest {

    private ExceptionDetector detector;
    private JavaParser parser;

    @BeforeEach
    void setUp() {
        detector = new ExceptionDetector();
        parser = new JavaParser();
    }

    @Test
    void detectDeclaredExceptions_ShouldDetectThrowsClause() {
        String code = """
            public class UserService {
                public User findById(Long id) throws UserNotFoundException {
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();
        MethodDeclaration method = classDecl.getMethodsByName("findById").get(0);

        List<ExceptionInfo> exceptions = detector.detectDeclaredExceptions(method);

        assertThat(exceptions).hasSize(1);
        assertThat(exceptions.get(0).getSimpleExceptionType()).isEqualTo("UserNotFoundException");
    }

    @Test
    void detectDeclaredExceptions_WithMultipleExceptions_ShouldDetectAll() {
        String code = """
            public class UserService {
                public User findById(Long id) throws UserNotFoundException, IllegalArgumentException {
                    return null;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();
        MethodDeclaration method = classDecl.getMethodsByName("findById").get(0);

        List<ExceptionInfo> exceptions = detector.detectDeclaredExceptions(method);

        assertThat(exceptions).hasSize(2);
        assertThat(exceptions).extracting(ExceptionInfo::getSimpleExceptionType)
                .containsExactly("UserNotFoundException", "IllegalArgumentException");
    }

    @Test
    void detectThrownExceptions_ShouldDetectThrowStatement() {
        String code = """
            public class UserService {
                public User findById(Long id) {
                    throw new UserNotFoundException("User not found");
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();
        MethodDeclaration method = classDecl.getMethodsByName("findById").get(0);

        List<ExceptionInfo> exceptions = detector.detectThrownExceptions(method);

        assertThat(exceptions).hasSize(1);
        assertThat(exceptions.get(0).getSimpleExceptionType()).isEqualTo("UserNotFoundException");
    }

    @Test
    void detectThrownExceptions_ShouldExtractExceptionMessage() {
        String code = """
            public class UserService {
                public User findById(Long id) {
                    throw new IllegalArgumentException("ID cannot be null");
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();
        MethodDeclaration method = classDecl.getMethodsByName("findById").get(0);

        List<ExceptionInfo> exceptions = detector.detectThrownExceptions(method);

        assertThat(exceptions.get(0).getMessagePattern()).isEqualTo("ID cannot be null");
    }

    @Test
    void detectThrownExceptions_WithConditionalThrow_ShouldDetect() {
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
        MethodDeclaration method = classDecl.getMethodsByName("findById").get(0);

        List<ExceptionInfo> exceptions = detector.detectThrownExceptions(method);

        assertThat(exceptions).isNotEmpty();
    }
}