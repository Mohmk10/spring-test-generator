package com.springtest.generator;

import com.springtest.assertion.AssertionGenerator;
import com.springtest.model.ClassInfo;
import com.springtest.model.ClassType;
import com.springtest.model.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RepositoryTestGenerator implements TestGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryTestGenerator.class);

    private final AssertionGenerator assertionGenerator;
    private final boolean useTestContainers;

    public RepositoryTestGenerator() {
        this(true);
    }

    public RepositoryTestGenerator(boolean useTestContainers) {
        this.assertionGenerator = new AssertionGenerator();
        this.useTestContainers = useTestContainers;
    }

    public RepositoryTestGenerator(AssertionGenerator assertionGenerator, boolean useTestContainers) {
        this.assertionGenerator = assertionGenerator;
        this.useTestContainers = useTestContainers;
    }

    @Override
    public String generateTest(ClassInfo classInfo) {
        if (classInfo == null) {
            throw new IllegalArgumentException("ClassInfo cannot be null");
        }
        if (!supports(classInfo)) {
            throw new IllegalArgumentException("RepositoryTestGenerator does not support class type: " + classInfo.classType());
        }

        logger.info("Generating repository test for: {}", classInfo.simpleName());

        StringBuilder testClass = new StringBuilder();

        testClass.append(generatePackageDeclaration(classInfo));
        testClass.append("\n\n");

        testClass.append(generateImports(classInfo));
        testClass.append("\n\n");

        testClass.append(generateClassDeclaration(classInfo));
        testClass.append("\n\n");

        testClass.append(generateRepositoryField(classInfo));
        testClass.append("\n\n");

        testClass.append(generateEntityManagerField());
        testClass.append("\n\n");

        testClass.append(generateTestMethods(classInfo));
        testClass.append("\n");

        testClass.append("}\n");

        return testClass.toString();
    }

    @Override
    public boolean supports(ClassInfo classInfo) {
        return classInfo != null && classInfo.classType() == ClassType.REPOSITORY;
    }

    private String generatePackageDeclaration(ClassInfo classInfo) {
        return "package " + classInfo.packageName() + ";";
    }

    private String generateImports(ClassInfo classInfo) {
        List<String> imports = new ArrayList<>();

        imports.add("import org.junit.jupiter.api.Test;");
        imports.add("import org.junit.jupiter.api.BeforeEach;");

        imports.add("import org.springframework.beans.factory.annotation.Autowired;");
        imports.add("import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;");
        imports.add("import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;");

        if (useTestContainers) {
            imports.add("import org.springframework.test.context.DynamicPropertyRegistry;");
            imports.add("import org.springframework.test.context.DynamicPropertySource;");
            imports.add("import org.testcontainers.containers.PostgreSQLContainer;");
            imports.add("import org.testcontainers.junit.jupiter.Container;");
            imports.add("import org.testcontainers.junit.jupiter.Testcontainers;");
        }

        imports.add("import static org.assertj.core.api.Assertions.*;");

        imports.add("import java.util.List;");
        imports.add("import java.util.Optional;");

        imports.add("import " + classInfo.qualifiedName() + ";");

        return imports.stream()
                .distinct()
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private String generateClassDeclaration(ClassInfo classInfo) {
        StringBuilder declaration = new StringBuilder();

        declaration.append("@DataJpaTest\n");

        if (useTestContainers) {
            declaration.append("@Testcontainers\n");
        }

        declaration.append("class ").append(classInfo.simpleName()).append("Test {\n");

        if (useTestContainers) {
            declaration.append("\n");
            declaration.append(generateTestContainerConfig());
        }

        return declaration.toString();
    }

    private String generateTestContainerConfig() {
        StringBuilder config = new StringBuilder();

        config.append("    @Container\n");
        config.append("    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(\"postgres:15-alpine\")\n");
        config.append("        .withDatabaseName(\"testdb\")\n");
        config.append("        .withUsername(\"test\")\n");
        config.append("        .withPassword(\"test\");\n\n");

        config.append("    @DynamicPropertySource\n");
        config.append("    static void configureProperties(DynamicPropertyRegistry registry) {\n");
        config.append("        registry.add(\"spring.datasource.url\", postgres::getJdbcUrl);\n");
        config.append("        registry.add(\"spring.datasource.username\", postgres::getUsername);\n");
        config.append("        registry.add(\"spring.datasource.password\", postgres::getPassword);\n");
        config.append("    }\n");

        return config.toString();
    }

    private String generateRepositoryField(ClassInfo classInfo) {
        return String.format("    @Autowired\n    private %s %s;",
                classInfo.simpleName(),
                getRepositoryFieldName(classInfo));
    }

    private String generateEntityManagerField() {
        return "    @Autowired\n    private TestEntityManager entityManager;";
    }

    private String generateTestMethods(ClassInfo classInfo) {
        StringBuilder methods = new StringBuilder();

        methods.append(generateSaveTest(classInfo));
        methods.append("\n");

        methods.append(generateFindByIdTest(classInfo));
        methods.append("\n");

        methods.append(generateFindAllTest(classInfo));
        methods.append("\n");

        methods.append(generateDeleteTest(classInfo));
        methods.append("\n");

        List<MethodInfo> customMethods = classInfo.methods().stream()
                .filter(m -> !m.isGetter() && !m.isSetter())
                .filter(m -> m.name().startsWith("find") || m.name().startsWith("count") || m.name().startsWith("exists"))
                .toList();

        for (MethodInfo method : customMethods) {
            methods.append(generateCustomQueryTest(classInfo, method));
            methods.append("\n");
        }

        return methods.toString();
    }

    private String generateSaveTest(ClassInfo classInfo) {
        StringBuilder test = new StringBuilder();

        String entityName = inferEntityName(classInfo);
        String repositoryField = getRepositoryFieldName(classInfo);

        test.append("    @Test\n");
        test.append("    void testSave() {\n");
        test.append("        ").append(entityName).append(" entity = new ").append(entityName).append("();\n");
        test.append("        ").append(entityName).append(" saved = ").append(repositoryField).append(".save(entity);\n");
        test.append("\n");

        test.append("        assertThat(saved).isNotNull();\n");
        test.append("        assertThat(saved.getId()).isNotNull();\n");
        test.append("    }\n");

        return test.toString();
    }

    private String generateFindByIdTest(ClassInfo classInfo) {
        StringBuilder test = new StringBuilder();

        String entityName = inferEntityName(classInfo);
        String repositoryField = getRepositoryFieldName(classInfo);

        test.append("    @Test\n");
        test.append("    void testFindById() {\n");
        test.append("        ").append(entityName).append(" entity = new ").append(entityName).append("();\n");
        test.append("        entityManager.persistAndFlush(entity);\n");
        test.append("        Long id = entity.getId();\n");
        test.append("\n");

        test.append("        Optional<").append(entityName).append("> found = ").append(repositoryField).append(".findById(id);\n");
        test.append("\n");

        test.append("        assertThat(found).isPresent();\n");
        test.append("        assertThat(found.get().getId()).isEqualTo(id);\n");
        test.append("    }\n");

        return test.toString();
    }

    private String generateFindAllTest(ClassInfo classInfo) {
        StringBuilder test = new StringBuilder();

        String entityName = inferEntityName(classInfo);
        String repositoryField = getRepositoryFieldName(classInfo);

        test.append("    @Test\n");
        test.append("    void testFindAll() {\n");
        test.append("        ").append(entityName).append(" entity1 = new ").append(entityName).append("();\n");
        test.append("        ").append(entityName).append(" entity2 = new ").append(entityName).append("();\n");
        test.append("        entityManager.persist(entity1);\n");
        test.append("        entityManager.persist(entity2);\n");
        test.append("        entityManager.flush();\n");
        test.append("\n");

        test.append("        List<").append(entityName).append("> all = ").append(repositoryField).append(".findAll();\n");
        test.append("\n");

        test.append("        assertThat(all).isNotEmpty();\n");
        test.append("        assertThat(all).hasSizeGreaterThanOrEqualTo(2);\n");
        test.append("    }\n");

        return test.toString();
    }

    private String generateDeleteTest(ClassInfo classInfo) {
        StringBuilder test = new StringBuilder();

        String entityName = inferEntityName(classInfo);
        String repositoryField = getRepositoryFieldName(classInfo);

        test.append("    @Test\n");
        test.append("    void testDelete() {\n");
        test.append("        ").append(entityName).append(" entity = new ").append(entityName).append("();\n");
        test.append("        entityManager.persistAndFlush(entity);\n");
        test.append("        Long id = entity.getId();\n");
        test.append("\n");

        test.append("        ").append(repositoryField).append(".deleteById(id);\n");
        test.append("        entityManager.flush();\n");
        test.append("\n");

        test.append("        Optional<").append(entityName).append("> deleted = ").append(repositoryField).append(".findById(id);\n");
        test.append("        assertThat(deleted).isEmpty();\n");
        test.append("    }\n");

        return test.toString();
    }

    private String generateCustomQueryTest(ClassInfo classInfo, MethodInfo methodInfo) {
        StringBuilder test = new StringBuilder();

        String testMethodName = "test" + capitalize(methodInfo.name());
        String repositoryField = getRepositoryFieldName(classInfo);

        test.append("    @Test\n");
        test.append("    void ").append(testMethodName).append("() {\n");
        test.append("\n");

        String methodCall = generateMethodCall(repositoryField, methodInfo);
        if (!methodInfo.returnsVoid()) {
            test.append("        ").append(methodInfo.returnType()).append(" result = ").append(methodCall).append(";\n");
        } else {
            test.append("        ").append(methodCall).append(";\n");
        }
        test.append("\n");

        if (!methodInfo.returnsVoid()) {
            test.append("        ").append(assertionGenerator.generateMethodAssertions(methodInfo).replace("\n", "\n        ")).append("\n");
        }
        test.append("    }\n");

        return test.toString();
    }

    private String inferEntityName(ClassInfo classInfo) {
        String repoName = classInfo.simpleName();
        if (repoName.endsWith("Repository")) {
            return repoName.substring(0, repoName.length() - "Repository".length());
        }
        return "Entity";
    }

    private String getRepositoryFieldName(ClassInfo classInfo) {
        String simpleName = classInfo.simpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    private String generateMethodCall(String instanceName, MethodInfo methodInfo) {
        if (methodInfo.parameters().isEmpty()) {
            return instanceName + "." + methodInfo.name() + "()";
        }

        String args = methodInfo.parameters().stream()
                .map(p -> getDefaultValue(p.type()))
                .collect(Collectors.joining(", "));

        return instanceName + "." + methodInfo.name() + "(" + args + ")";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String getDefaultValue(String type) {
        return switch (type) {
            case "String" -> "\"test\"";
            case "int", "Integer" -> "1";
            case "long", "Long" -> "1L";
            case "double", "Double" -> "1.0";
            case "float", "Float" -> "1.0f";
            case "boolean", "Boolean" -> "true";
            case "List" -> "List.of()";
            case "Set" -> "Set.of()";
            case "Map" -> "Map.of()";
            default -> "null";
        };
    }
}
