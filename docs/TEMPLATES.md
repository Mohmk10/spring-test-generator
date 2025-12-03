# Template Guide

Learn how to create and customize test generation templates.

## Table of Contents

- [Overview](#overview)
- [Template Engine](#template-engine)
- [Built-in Templates](#built-in-templates)
- [Custom Templates](#custom-templates)
- [Template Variables](#template-variables)
- [Advanced Usage](#advanced-usage)

---

## Overview

Spring Boot Test Generator uses **Freemarker** templates to generate test code.

**Why templates?**
- Customize test structure
- Enforce company standards
- Add custom annotations
- Include specific test patterns

---

## Template Engine

### Freemarker Basics

**Variables:**
```freemarker
${variableName}
```

**Conditionals:**
```freemarker
<#if condition>
  ...
</#if>
```

**Loops:**
```freemarker
<#list items as item>
  ${item.name}
</#list>
```

**Learn more:** [Freemarker Documentation](https://freemarker.apache.org/docs/)

---

## Built-in Templates

### Service Test Template

**File:** `service-test.ftl`

**Generates:** Unit tests for `@Service` classes

**Features:**
- `@ExtendWith(MockitoExtension.class)`
- `@Mock` for dependencies
- `@InjectMocks` for service under test
- Given-When-Then structure

**Example output:**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("findById - should return user when exists")
    void findById_WhenUserExists_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        User result = userService.findById(userId);
        
        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
    }
}
```

---

### Controller Test Template

**File:** `controller-test.ftl`

**Generates:** Tests for `@Controller` / `@RestController` classes

**Features:**
- `@WebMvcTest`
- `MockMvc` setup
- `@MockBean` for services
- HTTP request/response testing

**Example output:**
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Test
    void getUser_WhenUserExists_ShouldReturn200() throws Exception {
        when(userService.findById(1L)).thenReturn(user);
        
        mockMvc.perform(get("/api/users/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
```

---

### Repository Test Template

**File:** `repository-test.ftl`

**Generates:** Tests for `@Repository` interfaces

**Features:**
- `@DataJpaTest`
- `TestEntityManager` setup
- Database integration tests

**Example output:**
```java
@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        User user = createTestUser();
        entityManager.persistAndFlush(user);
        
        Optional<User> result = userRepository.findByEmail("test@example.com");
        
        assertThat(result).isPresent();
    }
}
```

---

### Integration Test Template

**File:** `integration-test.ftl`

**Generates:** Full integration tests

**Features:**
- `@SpringBootTest`
- End-to-end testing
- All layers integration

---

## Custom Templates

### Setup

**1. Create template directory:**
```bash
mkdir -p src/test/resources/templates
```

**2. Enable custom templates:**
```properties
# .spring-test-gen.properties
use.custom.templates=true
template.directory=src/test/resources/templates
```

**3. Create template files:**
```bash
touch src/test/resources/templates/service-test.ftl
touch src/test/resources/templates/controller-test.ftl
touch src/test/resources/templates/repository-test.ftl
```

---

### Basic Template

**File:** `service-test.ftl`
```freemarker
package ${packageName};

<#list imports as import>
${import}
</#list>

<#list classAnnotations as annotation>
${annotation}
</#list>
class ${className} {
    
    <#list testFields as field>
    ${field}
    
    </#list>
    
    <#list testCases as testCase>
    @Test
    @DisplayName("${testCase.displayName}")
    void ${testCase.testMethodName}() {
        <#list testCase.givenStatements as given>
        ${given}
        </#list>
        
        <#list testCase.mockSetups as mock>
        ${mock}
        </#list>
        
        ${testCase.whenStatement}
        
        <#list testCase.thenStatements as then>
        ${then}
        </#list>
        
        <#list testCase.verifyStatements as verify>
        ${verify}
        </#list>
    }
    
    </#list>
}
```

---

## Template Variables

### Top-Level Variables

| Variable | Type | Description |
|----------|------|-------------|
| `packageName` | String | Test package name |
| `className` | String | Test class name |
| `targetClass` | ClassInfo | Source class being tested |
| `testCases` | List<TestCase> | All test cases |
| `imports` | List<String> | Required imports |
| `classAnnotations` | List<String> | Class-level annotations |
| `testFields` | List<String> | Mock fields |
| `setupMethods` | List<String> | @BeforeEach methods |
| `testType` | String | Test type (unit, webmvc, datajpa, integration) |

---

### ClassInfo Object

**Available properties:**
```freemarker
${targetClass.simpleName}              <!-- UserService -->
${targetClass.fullyQualifiedName}     <!-- com.example.UserService -->
${targetClass.packageName}            <!-- com.example -->
${targetClass.methods}                <!-- List of methods -->
${targetClass.fields}                 <!-- List of fields -->
${targetClass.annotations}            <!-- List of annotations -->
${targetClass.springStereotype}       <!-- Service, Controller, etc -->
```

**Example:**
```freemarker
<#if targetClass.springStereotype == "Service">
  <!-- Service-specific logic -->
</#if>
```

---

### TestCase Object

**Available properties:**
```freemarker
${testCase.testMethodName}            <!-- findById_WhenUserExists_ShouldReturnUser -->
${testCase.displayName}               <!-- findById - should return user when exists -->
${testCase.scenario}                  <!-- happy_path, not_found, exception, etc -->
${testCase.description}               <!-- Test description -->
${testCase.targetMethod}              <!-- Method being tested -->
${testCase.givenStatements}           <!-- Setup statements -->
${testCase.mockSetups}                <!-- Mock when/thenReturn -->
${testCase.whenStatement}             <!-- Method call -->
${testCase.thenStatements}            <!-- Assertions -->
${testCase.verifyStatements}          <!-- Mock verifications -->
${testCase.expectsException}          <!-- Boolean -->
${testCase.expectedException}         <!-- Exception info -->
${testCase.priority}                  <!-- Test priority (1-10) -->
```

---

### MethodInfo Object

**Available properties:**
```freemarker
${method.name}                        <!-- findById -->
${method.returnType}                  <!-- User -->
${method.simpleReturnType}            <!-- User (without package) -->
${method.parameters}                  <!-- List of parameters -->
${method.annotations}                 <!-- List of annotations -->
${method.voidReturn}                  <!-- Boolean -->
${method.returnsOptional}             <!-- Boolean -->
${method.returnsCollection}           <!-- Boolean -->
${method.complexity}                  <!-- Cyclomatic complexity -->
```

---

## Advanced Usage

### Conditional Logic

**Service vs Controller:**
```freemarker
<#if testType == "unit">
@ExtendWith(MockitoExtension.class)
<#elseif testType == "webmvc">
@WebMvcTest(${targetClass.simpleName}.class)
<#elseif testType == "datajpa">
@DataJpaTest
</#if>
```

---

### Custom Annotations

**Add company-specific annotations:**
```freemarker
@Test
@Tag("unit")
@Owner("${targetClass.packageName}")
@DisplayName("${testCase.displayName}")
void ${testCase.testMethodName}() {
    ...
}
```

---

### Custom Imports

**Add additional imports:**
```freemarker
<#list imports as import>
${import}
</#list>
import com.company.testing.BaseTest;
import com.company.testing.TestUtils;
```

---

### Test Data Builders

**Generate test data setup:**
```freemarker
<#if targetClass.simpleName?ends_with("Service")>
    private ${targetClass.simpleName?replace("Service", "")} createTest${targetClass.simpleName?replace("Service", "")}() {
        return ${targetClass.simpleName?replace("Service", "")}.builder()
            .id(1L)
            .name("Test")
            .build();
    }
</#if>
```

---

### Grouped Tests

**Organize tests with `@Nested`:**
```freemarker
class ${className} {
    
    <#list testCases?group_by(tc -> tc.targetMethod.name) as methodName, cases>
    @Nested
    @DisplayName("${methodName}()")
    class ${methodName?cap_first}Tests {
        
        <#list cases as testCase>
        @Test
        void ${testCase.testMethodName}() {
            ...
        }
        </#list>
    }
    
    </#list>
}
```

---

### Parameterized Tests

**Generate `@ParameterizedTest`:**
```freemarker
<#if testCase.scenario == "edge_case">
@ParameterizedTest
@MethodSource("edgeCaseProvider")
void ${testCase.testMethodName}(${testCase.targetMethod.parameters[0].type} input) {
    assertThatThrownBy(() -> service.${testCase.targetMethod.name}(input))
        .isInstanceOf(IllegalArgumentException.class);
}

static Stream<${testCase.targetMethod.parameters[0].type}> edgeCaseProvider() {
    return Stream.of(null, "", "   ");
}
</#if>
```

---

## Example: Custom Enterprise Template

**File:** `service-test.ftl`
```freemarker
package ${packageName};

<#-- Standard imports -->
<#list imports as import>
${import}
</#list>

<#-- Company imports -->
import com.company.testing.BaseUnitTest;
import com.company.testing.annotations.UnitTest;
import com.company.testing.annotations.Owner;

/**
 * Generated test for ${targetClass.simpleName}
 * 
 * @author Test Generator
 * @since ${.now?string("yyyy-MM-dd")}
 */
@UnitTest
@Owner("${targetClass.packageName}")
@ExtendWith(MockitoExtension.class)
class ${className} extends BaseUnitTest {
    
    <#-- Mock dependencies -->
    <#list testFields as field>
    ${field}
    
    </#list>
    
    <#-- Tested instance -->
    @InjectMocks
    private ${targetClass.simpleName} ${targetClass.simpleName?uncap_first};
    
    <#-- Test data setup -->
    @BeforeEach
    void setUp() {
        // Test data initialization
    }
    
    <#-- Group tests by method -->
    <#list testCases?group_by(tc -> tc.targetMethod.name) as methodName, cases>
    
    // ===== ${methodName}() =====
    
    <#list cases as testCase>
    @Test
    @DisplayName("${testCase.displayName}")
    <#if testCase.scenario == "edge_case">
    @Tag("edge-case")
    <#elseif testCase.scenario == "exception">
    @Tag("exception")
    </#if>
    void ${testCase.testMethodName}() {
        // Given
        <#list testCase.givenStatements as given>
        ${given}
        </#list>
        
        <#list testCase.mockSetups as mock>
        ${mock}
        </#list>
        
        // When
        <#if testCase.expectsException>
        // Then
        assertThatThrownBy(() -> ${testCase.whenStatement})
            .isInstanceOf(${testCase.expectedException.simpleExceptionType}.class);
        <#else>
        ${testCase.whenStatement}
        
        // Then
        <#list testCase.thenStatements as then>
        ${then}
        </#list>
        
        <#list testCase.verifyStatements as verify>
        ${verify}
        </#list>
        </#if>
    }
    
    </#list>
    </#list>
    
    // ===== Test Data Helpers =====
    
    private ${targetClass.simpleName?replace("Service", "")} createTest${targetClass.simpleName?replace("Service", "")}() {
        return ${targetClass.simpleName?replace("Service", "")}.builder()
            .id(1L)
            .build();
    }
}
```

---

## Testing Your Templates

### Test Locally
```bash
# Generate with custom templates
java -jar spring-test-gen.jar generate . \
    --template-dir src/test/resources/templates \
    --verbose

# Review generated tests
ls -la src/test/java/
```

### Validate Template Syntax

**Check for syntax errors:**
```bash
# Install Freemarker CLI (if available)
freemarker-cli validate service-test.ftl

# Or test with small project
java -jar spring-test-gen.jar generate sample-project --verbose
```

---

## Template Best Practices

### 1. Keep Templates DRY

**Use macros for repetition:**
```freemarker
<#macro testMethod testCase>
@Test
@DisplayName("${testCase.displayName}")
void ${testCase.testMethodName}() {
    <#nested>
}
</#macro>

<@testMethod testCase=testCase>
    // Test body
</@testMethod>
```

### 2. Handle Edge Cases

**Check for empty collections:**
```freemarker
<#if testFields?has_content>
  <#list testFields as field>
    ${field}
  </#list>
<#else>
  // No dependencies to mock
</#if>
```

### 3. Add Comments

**Document template logic:**
```freemarker
<#-- Generate mock fields for all autowired dependencies -->
<#list testFields as field>
    ${field}
</#list>
```

### 4. Format Output

**Use proper indentation:**
```freemarker
class ${className} {
    
    <#list testFields as field>
    ${field}
    
    </#list>
}
```

### 5. Test Coverage

**Generate comprehensive tests:**
```freemarker
<#-- Happy path -->
<#-- Edge cases -->
<#-- Exceptions -->
```

---

## Troubleshooting

### Template Not Found

**Check:**
1. Template directory exists
2. Template file has `.ftl` extension
3. Template directory configured correctly
```properties
template.directory=src/test/resources/templates
```

### Syntax Errors

**Common mistakes:**
```freemarker
<!-- ❌ Wrong -->
$targetClass.name

<!-- ✅ Correct -->
${targetClass.name}

<!-- ❌ Wrong -->
<#if testFields>

<!-- ✅ Correct -->
<#if testFields?has_content>
```

### Variables Not Rendering

**Debug with:**
```freemarker
<!-- Show all variables -->
<#list .data_model?keys as key>
${key}: ${.data_model[key]}
</#list>
```

---

## Resources

- **Freemarker Manual:** [https://freemarker.apache.org/docs/](https://freemarker.apache.org/docs/)
- **Built-in Templates:** Check `core/src/main/resources/templates/`
- **Examples:** See `docs/examples.md`

---

## Next Steps

- **[Examples](examples.md)** - Template examples
- **[Configuration](configuration.md)** - Configure templates
- **[CLI Reference](cli-reference.md)** - Template commands

---

**Need help?** [Open an issue](https://github.com/Mohmk10/spring-test-generator/issues)