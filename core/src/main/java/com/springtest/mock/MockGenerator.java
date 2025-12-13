package com.springtest.mock;

import com.springtest.model.ClassInfo;
import com.springtest.model.FieldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates mock declarations for test classes.
 * Creates @Mock, @MockBean, and @InjectMocks annotations based on class dependencies.
 */
public class MockGenerator {
    private static final Logger logger = LoggerFactory.getLogger(MockGenerator.class);

    private final boolean useSpringBootTest;

    /**
     * Creates a MockGenerator with default configuration (JUnit + Mockito).
     */
    public MockGenerator() {
        this(false);
    }

    /**
     * Creates a MockGenerator with specified configuration.
     *
     * @param useSpringBootTest If true, uses @MockBean instead of @Mock
     */
    public MockGenerator(boolean useSpringBootTest) {
        this.useSpringBootTest = useSpringBootTest;
    }

    /**
     * Generates all mock declarations for a class under test.
     *
     * @param classInfo Information about the class being tested
     * @return Generated mock declarations as Java code
     */
    public String generateMocks(ClassInfo classInfo) {
        logger.debug("Generating mocks for class: {}", classInfo.simpleName());

        List<String> mockDeclarations = new ArrayList<>();

        // Generate mocks for injected dependencies
        for (FieldInfo field : classInfo.getInjectedFields()) {
            String mockDeclaration = generateMockForField(field);
            mockDeclarations.add(mockDeclaration);
        }

        // Generate @InjectMocks for the class under test
        String injectMocksDeclaration = generateInjectMocks(classInfo);
        mockDeclarations.add(injectMocksDeclaration);

        return String.join("\n\n", mockDeclarations);
    }

    /**
     * Generates a mock declaration for a single field.
     *
     * @param field Field information
     * @return Mock declaration as Java code
     */
    public String generateMockForField(FieldInfo field) {
        String annotation = useSpringBootTest ? "@MockBean" : "@Mock";
        return String.format("%s\nprivate %s %s;",
                annotation,
                field.type(),
                field.name());
    }

    /**
     * Generates @InjectMocks declaration for the class under test.
     *
     * @param classInfo Information about the class being tested
     * @return InjectMocks declaration as Java code
     */
    public String generateInjectMocks(ClassInfo classInfo) {
        String simpleName = classInfo.simpleName();
        String fieldName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);

        return String.format("@InjectMocks\nprivate %s %s;",
                simpleName,
                fieldName);
    }

    /**
     * Generates all required imports for mocks.
     *
     * @param classInfo Information about the class being tested
     * @return List of import statements
     */
    public List<String> generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        if (useSpringBootTest) {
            imports.add("import org.springframework.boot.test.mock.mockito.MockBean;");
        } else {
            imports.add("import org.mockito.Mock;");
        }

        imports.add("import org.mockito.InjectMocks;");

        // Add imports for injected field types
        for (FieldInfo field : classInfo.getInjectedFields()) {
            if (field.qualifiedType() != null && !field.qualifiedType().equals(field.type())) {
                imports.add(String.format("import %s;", field.qualifiedType()));
            }
        }

        return imports.stream().distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Generates field declarations without annotations (for manual mock setup).
     *
     * @param classInfo Information about the class being tested
     * @return Field declarations as Java code
     */
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

    /**
     * Generates setup code for manual mock initialization.
     *
     * @param classInfo Information about the class being tested
     * @return Setup method code
     */
    public String generateSetupMethod(ClassInfo classInfo) {
        StringBuilder setup = new StringBuilder();
        setup.append("@BeforeEach\n");
        setup.append("void setUp() {\n");

        // Initialize mocks
        for (FieldInfo field : classInfo.getInjectedFields()) {
            setup.append(String.format("    %s = mock(%s.class);\n",
                    field.name(),
                    field.type()));
        }

        // Initialize class under test
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

    /**
     * Checks if a class requires mocks based on its dependencies.
     *
     * @param classInfo Information about the class
     * @return true if the class has dependencies that should be mocked
     */
    public boolean requiresMocks(ClassInfo classInfo) {
        return !classInfo.getInjectedFields().isEmpty();
    }

    /**
     * Generates mock declarations for specific dependencies.
     *
     * @param dependencies List of dependency type names
     * @return Mock declarations as Java code
     */
    public String generateMocksForDependencies(List<String> dependencies) {
        return dependencies.stream()
                .map(this::generateMockForDependency)
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Generates a mock declaration for a single dependency type.
     *
     * @param dependencyType Type name of the dependency
     * @return Mock declaration as Java code
     */
    private String generateMockForDependency(String dependencyType) {
        String annotation = useSpringBootTest ? "@MockBean" : "@Mock";
        String fieldName = Character.toLowerCase(dependencyType.charAt(0)) + dependencyType.substring(1);

        return String.format("%s\nprivate %s %s;",
                annotation,
                dependencyType,
                fieldName);
    }
}
