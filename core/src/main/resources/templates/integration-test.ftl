package ${packageName};

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

<#if useRestTemplate>
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
</#if>

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
<#list injectedFields as field>
<#if field.qualifiedType?? && !field.qualifiedType?starts_with("java.")>
import ${field.qualifiedType};
</#if>
</#list>

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.${webEnvironment})
<#if useTestContainers>
@Testcontainers
</#if>
class ${className}IntegrationTest {

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
    private ${className} ${instanceName};

<#if useRestTemplate>
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

</#if>

<#if webMethods??>
<#list webMethods as method>
    @Test
    void testIntegration_${method.nameCapitalized}() {
<#if useRestTemplate>
        String url = "http://localhost:" + port + "${method.path}";
        ResponseEntity<${method.responseType}> response = restTemplate.getForEntity(url, ${method.responseType}.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
<#if !method.returnsVoid>
        assertThat(response.getBody()).isNotNull();
</#if>
<#else>
<#if method.returnsVoid>
        ${instanceName}.${method.name}(${method.args});
<#else>
        ${method.returnType} result = ${instanceName}.${method.name}(${method.args});
        assertThat(result).isNotNull();
</#if>
</#if>
    }

</#list>
<#else>
    @Test
    void testIntegrationContextLoads() {
        assertThat(${instanceName}).isNotNull();
    }
</#if>
}
