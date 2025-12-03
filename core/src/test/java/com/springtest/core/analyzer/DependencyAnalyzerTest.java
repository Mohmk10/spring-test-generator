package com.springtest.core.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.springtest.core.model.FieldInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyAnalyzerTest {

    private DependencyAnalyzer analyzer;
    private JavaParser parser;

    @BeforeEach
    void setUp() {
        AnnotationDetector annotationDetector = new AnnotationDetector();
        analyzer = new DependencyAnalyzer(annotationDetector);
        parser = new JavaParser();
    }

    @Test
    void analyzeFields_ShouldDetectFields() {
        String code = """
            public class UserService {
                @Autowired
                private UserRepository userRepository;
                
                private EmailService emailService;
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<FieldInfo> fields = analyzer.analyzeFields(classDecl.getFields());

        assertThat(fields).hasSize(2);
        assertThat(fields).extracting(FieldInfo::getName)
                .containsExactly("userRepository", "emailService");
    }

    @Test
    void analyzeFields_ShouldDetectFieldTypes() {
        String code = """
            public class UserService {
                private UserRepository userRepository;
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<FieldInfo> fields = analyzer.analyzeFields(classDecl.getFields());

        assertThat(fields.get(0).getType()).isEqualTo("UserRepository");
        assertThat(fields.get(0).getSimpleType()).isEqualTo("UserRepository");
    }

    @Test
    void analyzeFields_ShouldDetectAnnotations() {
        String code = """
            public class UserService {
                @Autowired
                private UserRepository userRepository;
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<FieldInfo> fields = analyzer.analyzeFields(classDecl.getFields());

        assertThat(fields.get(0).getAnnotations()).hasSize(1);
        assertThat(fields.get(0).getAnnotations().get(0).getSimpleName()).isEqualTo("Autowired");
    }

    @Test
    void analyzeFields_ShouldDetectFinalFields() {
        String code = """
            public class UserService {
                private final UserRepository userRepository;
                
                public UserService(UserRepository userRepository) {
                    this.userRepository = userRepository;
                }
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<FieldInfo> fields = analyzer.analyzeFields(classDecl.getFields());

        assertThat(fields.get(0).isFinalField()).isTrue();
    }

    @Test
    void analyzeFields_ShouldDetectStaticFields() {
        String code = """
            public class UserService {
                private static final String CONSTANT = "value";
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<FieldInfo> fields = analyzer.analyzeFields(classDecl.getFields());

        assertThat(fields.get(0).isStaticField()).isTrue();
    }

    @Test
    void getMockableFields_ShouldFilterNonMockableFields() {
        String code = """
            public class UserService {
                @Autowired
                private UserRepository userRepository;
                
                private static final String CONSTANT = "value";
                
                private final String config = "config";
            }
            """;

        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName("UserService").orElseThrow();

        List<FieldInfo> allFields = analyzer.analyzeFields(classDecl.getFields());
        List<FieldInfo> mockableFields = analyzer.getMockableFields(allFields);

        assertThat(mockableFields).hasSize(1);
        assertThat(mockableFields.get(0).getName()).isEqualTo("userRepository");
    }
}