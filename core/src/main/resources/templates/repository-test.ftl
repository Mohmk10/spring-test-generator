package ${packageName};

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

<#if useTestContainers>
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
</#if>

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import ${classQualifiedName};

@DataJpaTest
<#if useTestContainers>
@Testcontainers
</#if>
class ${className}Test {

<#if useTestContainers>
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

</#if>
    @Autowired
    private ${className} ${repositoryFieldName};

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testSave() {
        // Arrange
        ${entityName} entity = new ${entityName}();
        // TODO: Set entity properties

        // Act
        ${entityName} saved = ${repositoryFieldName}.save(entity);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        // TODO: Add more assertions
    }

    @Test
    void testFindById() {
        // Arrange
        ${entityName} entity = new ${entityName}();
        // TODO: Set entity properties
        entityManager.persistAndFlush(entity);
        Long id = entity.getId();

        // Act
        Optional<${entityName}> found = ${repositoryFieldName}.findById(id);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
    }

    @Test
    void testFindAll() {
        // Arrange
        ${entityName} entity1 = new ${entityName}();
        ${entityName} entity2 = new ${entityName}();
        entityManager.persist(entity1);
        entityManager.persist(entity2);
        entityManager.flush();

        // Act
        List<${entityName}> all = ${repositoryFieldName}.findAll();

        // Assert
        assertThat(all).isNotEmpty();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testDelete() {
        // Arrange
        ${entityName} entity = new ${entityName}();
        entityManager.persistAndFlush(entity);
        Long id = entity.getId();

        // Act
        ${repositoryFieldName}.deleteById(id);
        entityManager.flush();

        // Assert
        Optional<${entityName}> deleted = ${repositoryFieldName}.findById(id);
        assertThat(deleted).isEmpty();
    }

<#list customMethods as method>
    @Test
    void test${method.nameCapitalized}() {
        // Arrange
        // TODO: Setup test data

        // Act
<#if method.returnsVoid>
        ${repositoryFieldName}.${method.name}(${method.args});
<#else>
        ${method.returnType} result = ${repositoryFieldName}.${method.name}(${method.args});
</#if>

        // Assert
<#if !method.returnsVoid>
        assertThat(result).isNotNull();
</#if>
        // TODO: Add assertions
    }

</#list>
}
