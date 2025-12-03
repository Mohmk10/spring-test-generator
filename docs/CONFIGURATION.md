# Configuration Guide

Complete guide to configuring Spring Boot Test Generator.

## Table of Contents

- [Configuration File](#configuration-file)
- [Configuration Properties](#configuration-properties)
- [Naming Conventions](#naming-conventions)
- [Custom Templates](#custom-templates)
- [Advanced Configuration](#advanced-configuration)

---

## Configuration File

### Location

Spring Test Generator looks for configuration in this order:

1. Command-line `--config` option
2. `SPRING_TEST_GEN_CONFIG` environment variable
3. `.spring-test-gen.properties` in current directory
4. Default values

### Creating Config File
```bash
# Initialize with defaults
java -jar spring-test-gen.jar config init

# Creates: .spring-test-gen.properties
```

### Format

Standard Java properties format:
```properties
# Comments start with #
property.name=value
another.property=value
```

---

## Configuration Properties

### Directory Settings

**Source Directory:**
```properties
# Location of Java source files
source.directory=src/main/java
```

**Test Directory:**
```properties
# Output location for generated tests
test.directory=src/test/java
```

**Custom Paths:**
```properties
# Non-standard Maven layout
source.directory=sources/java
test.directory=tests/java

# Gradle Kotlin layout
source.directory=src/main/kotlin
test.directory=src/test/kotlin
```

---

### Package Configuration

**Packages to Scan:**
```properties
# Comma-separated list of packages
packages.to.scan=com.example.service,com.example.controller

# Or use newlines
packages.to.scan=com.example.service,\
                 com.example.controller,\
                 com.example.repository
```

**Include Patterns:**
```properties
# Only process matching classes (glob patterns)
include.patterns=*Service*,*Controller*

# Multiple patterns
include.patterns=*Service*,\
                *Controller*,\
                *Repository*
```

**Exclude Patterns:**
```properties
# Skip matching classes
exclude.patterns=*Test*,*Config*,*Application*

# Common exclusions
exclude.patterns=**/config/**,\
                **/dto/**,\
                **/entity/**,\
                **/*Application.java
```

---

### Generation Options

**Skip Existing Tests:**
```properties
# Don't regenerate if test already exists
skip.existing=true

# Force regenerate all
skip.existing=false
```

**Edge Case Tests:**
```properties
# Generate null checks, boundary values, etc.
generate.edge.cases=true

# Disable edge cases
generate.edge.cases=false
```

**Exception Tests:**
```properties
# Generate tests for thrown exceptions
generate.exception.tests=true

# Disable exception tests
generate.exception.tests=false
```

**Integration Tests:**
```properties
# Generate @SpringBootTest integration tests
generate.integration.tests=false

# Enable integration tests
generate.integration.tests=true
```

---

### Test Naming Convention

**Available Conventions:**
```properties
# Default: METHOD_SCENARIO_EXPECTED
test.naming.convention=METHOD_SCENARIO_EXPECTED
# Example: findById_WhenUserExists_ShouldReturnUser

# GIVEN_WHEN_THEN
test.naming.convention=GIVEN_WHEN_THEN
# Example: givenUserExists_whenFindById_thenReturnUser

# BDD
test.naming.convention=BDD
# Example: should_return_user_when_exists

# SIMPLE
test.naming.convention=SIMPLE
# Example: testFindById
```

**Comparison:**

| Convention | Example | Best For |
|------------|---------|----------|
| `METHOD_SCENARIO_EXPECTED` | `findById_WhenUserExists_ShouldReturnUser` | Enterprise projects |
| `GIVEN_WHEN_THEN` | `givenUserExists_whenFindById_thenReturnUser` | BDD-style projects |
| `BDD` | `should_return_user_when_exists` | Behavior-focused testing |
| `SIMPLE` | `testFindById` | Legacy projects |

---

### Test Limits

**Max Tests Per Method:**
```properties
# Limit tests generated per method
max.tests.per.method=10

# Unlimited
max.tests.per.method=-1
```

---

### Logging

**Verbose Logging:**
```properties
# Enable detailed logs
verbose.logging=true

# Quiet mode
verbose.logging=false
```

**Dry Run:**
```properties
# Preview without writing files
dry.run=true

# Normal mode
dry.run=false
```

---

## Naming Conventions

### METHOD_SCENARIO_EXPECTED (Default)

**Format:** `methodName_scenario_expectedResult`

**Examples:**
```java
findById_WhenUserExists_ShouldReturnUser()
findById_WhenUserNotFound_ShouldThrowException()
findById_WhenIdIsNull_ShouldThrowIllegalArgumentException()
createUser_WithValidRequest_ShouldCreateUser()
createUser_WhenEmailExists_ShouldThrowException()
```

**Best for:** Enterprise projects, clear documentation

---

### GIVEN_WHEN_THEN

**Format:** `given{Scenario}_when{Action}_then{Result}`

**Examples:**
```java
givenUserExists_whenFindById_thenReturnUser()
givenUserNotFound_whenFindById_thenThrowException()
givenNullId_whenFindById_thenThrowIllegalArgumentException()
givenValidRequest_whenCreateUser_thenCreateUser()
givenEmailExists_whenCreateUser_thenThrowException()
```

**Best for:** BDD-style projects, Cucumber integration

---

### BDD

**Format:** `should_{action}_{condition}`

**Examples:**
```java
should_return_user_when_exists()
should_throw_exception_when_not_found()
should_throw_exception_when_null()
should_create_user_when_valid()
should_throw_exception_when_email_exists()
```

**Best for:** Behavior-focused testing, readability

---

### SIMPLE

**Format:** `test{MethodName}[{Scenario}]`

**Examples:**
```java
testFindById()
testFindByIdNotFound()
testFindByIdNull()
testCreateUser()
testCreateUserEmailExists()
```

**Best for:** Legacy projects, simple naming

---

## Custom Templates

### Template Directory

**Configure custom templates:**
```properties
# Enable custom templates
use.custom.templates=true

# Template directory
template.directory=src/test/resources/templates
```

### Template Structure
```
src/test/resources/templates/
├── service-test.ftl          # @Service tests
├── controller-test.ftl       # @Controller tests
├── repository-test.ftl       # @Repository tests
└── integration-test.ftl      # Integration tests
```

### Template Variables

**Available in all templates:**
```freemarker
${packageName}               - Test package
${className}                 - Test class name
${targetClass}               - Source class info
${testCases}                 - List of test cases
${imports}                   - Required imports
${classAnnotations}          - Class-level annotations
${testFields}                - Mock fields
${setupMethods}              - @BeforeEach methods
```

**Example Custom Template:**
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
        // Custom template logic
        ${testCase.whenStatement}
        <#list testCase.thenStatements as assertion>
        ${assertion}
        </#list>
    }
    
    </#list>
}
```

---

## Advanced Configuration

### Multiple Configurations

**Project-specific configs:**
```bash
# Development
java -jar spring-test-gen.jar generate . --config dev.properties

# Production
java -jar spring-test-gen.jar generate . --config prod.properties

# CI/CD
java -jar spring-test-gen.jar generate . --config ci.properties
```

**Example configs:**

**dev.properties:**
```properties
generate.edge.cases=true
generate.exception.tests=true
verbose.logging=true
dry.run=false
```

**ci.properties:**
```properties
generate.edge.cases=true
generate.exception.tests=true
skip.existing=false
verbose.logging=false
```

---

### Profile-Based Configuration
```properties
# Base configuration
source.directory=src/main/java
test.directory=src/test/java

# Development profile
dev.generate.edge.cases=true
dev.verbose.logging=true

# Production profile
prod.generate.edge.cases=true
prod.generate.exception.tests=true
prod.skip.existing=true
```

---

### Environment Variables

**Override properties via environment:**
```bash
# Set environment variables
export SPRING_TEST_GEN_SOURCE_DIR=src/main/java
export SPRING_TEST_GEN_TEST_DIR=src/test/java
export SPRING_TEST_GEN_VERBOSE=true

# Run generator
java -jar spring-test-gen.jar generate .
```

**Variable naming:**
- Replace `.` with `_`
- Convert to uppercase
- Prefix with `SPRING_TEST_GEN_`

Examples:
- `source.directory` → `SPRING_TEST_GEN_SOURCE_DIR`
- `generate.edge.cases` → `SPRING_TEST_GEN_GENERATE_EDGE_CASES`

---

### Command-Line Overrides

**Override any property:**
```bash
java -jar spring-test-gen.jar generate . \
    -Dsource.directory=custom/src \
    -Dtest.directory=custom/test \
    -Dgenerate.edge.cases=false
```

**Precedence (highest to lowest):**
1. Command-line `-D` options
2. Environment variables
3. Config file
4. Default values

---

## Example Configurations

### Minimal Configuration
```properties
# Minimal - use defaults
source.directory=src/main/java
test.directory=src/test/java
```

### Standard Configuration
```properties
# Standard Spring Boot project
source.directory=src/main/java
test.directory=src/test/java
packages.to.scan=com.example
skip.existing=true
generate.edge.cases=true
generate.exception.tests=true
test.naming.convention=METHOD_SCENARIO_EXPECTED
max.tests.per.method=10
```

### Enterprise Configuration
```properties
# Enterprise project with strict rules
source.directory=src/main/java
test.directory=src/test/java

# Specific packages only
packages.to.scan=com.company.service,\
                com.company.controller,\
                com.company.repository

# Exclude infrastructure
exclude.patterns=**/config/**,\
                **/infrastructure/**,\
                **/*Application.java

# Generate all test types
generate.edge.cases=true
generate.exception.tests=true
generate.integration.tests=true

# Naming
test.naming.convention=METHOD_SCENARIO_EXPECTED

# Quality
max.tests.per.method=15
skip.existing=true
verbose.logging=false
```

### Microservices Configuration
```properties
# Microservice with limited scope
source.directory=src/main/java
test.directory=src/test/java
packages.to.scan=com.company.userservice
include.patterns=*Service*,*Controller*,*Client*
generate.edge.cases=true
generate.exception.tests=true
test.naming.convention=BDD
```

---

## Troubleshooting

### Config Not Loading

**Check:**
1. File name: `.spring-test-gen.properties`
2. File location: project root
3. File format: valid Java properties

**Debug:**
```bash
# Show active configuration
java -jar spring-test-gen.jar config show

# Verbose mode shows config source
java -jar spring-test-gen.jar generate . --verbose
```

### Property Not Working

**Verify:**
```bash
# List all properties
cat .spring-test-gen.properties

# Check syntax
grep "property.name" .spring-test-gen.properties
```

### Template Not Found

**Verify:**
```bash
# Check template directory
ls -la src/test/resources/templates/

# Ensure .ftl extension
find . -name "*.ftl"
```

---

## Next Steps

- **[CLI Reference](cli-reference.md)** - All commands
- **[Examples](examples.md)** - Real-world scenarios
- **[Templates](templates.md)** - Custom template guide

---

**Questions?** [Open an issue](https://github.com/Mohmk10/spring-test-generator/issues)