# Spring Test Generator - Gradle Plugin

Automatically generates unit and integration tests for Spring Boot applications using Gradle.

## Installation

Add the plugin to your `build.gradle`:

```groovy
plugins {
    id 'com.springtest.test-generator' version '1.0.0'
}
```

Or using the legacy plugin application:

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath 'com.springtest:spring-test-generator-gradle-plugin:1.0.0'
    }
}

apply plugin: 'com.springtest.test-generator'
```

## Configuration

Configure the plugin in your `build.gradle`:

```groovy
springTestGenerator {
    sourceDirectory = 'src/main/java'          // Default: src/main/java
    outputDirectory = 'src/test/java'          // Default: src/test/java
    testType = 'all'                           // Options: unit, integration, all (default)
    namingStrategy = 'method-scenario'         // Options: method-scenario (default), bdd, given-when-then
    includes = ['com.example.service.*']       // Optional: patterns to include
    excludes = ['com.example.internal.*']      // Optional: patterns to exclude
}
```

### Configuration Options

| Option | Description | Default | Values |
|--------|-------------|---------|--------|
| `sourceDirectory` | Source code directory | `src/main/java` | Any valid path |
| `outputDirectory` | Test output directory | `src/test/java` | Any valid path |
| `testType` | Type of tests to generate | `all` | `unit`, `integration`, `all` |
| `namingStrategy` | Test naming convention | `method-scenario` | `method-scenario`, `bdd`, `given-when-then` |
| `includes` | Patterns to include | `null` | List of glob patterns |
| `excludes` | Patterns to exclude | `null` | List of glob patterns |

## Tasks

### generateTests

Generates unit and/or integration tests for your Spring Boot classes.

```bash
./gradlew generateTests
```

This task will:
1. Analyze your Spring Boot application source code
2. Identify Spring components (Controllers, Services, Repositories)
3. Generate comprehensive test classes with:
   - Proper mocking setup
   - Test methods for all public methods
   - Edge cases and exception scenarios
   - Appropriate assertions

### analyzeSpringClasses

Analyzes and displays all Spring Boot classes in your project.

```bash
./gradlew analyzeSpringClasses
```

This task will output:
- Controllers with their endpoints
- Services with their methods
- Repositories with their queries
- Other Spring components

## Examples

### Generate All Tests

```groovy
springTestGenerator {
    testType = 'all'
}
```

```bash
./gradlew generateTests
```

### Generate Only Unit Tests with BDD Naming

```groovy
springTestGenerator {
    testType = 'unit'
    namingStrategy = 'bdd'
}
```

```bash
./gradlew generateTests
```

### Generate Tests for Specific Packages

```groovy
springTestGenerator {
    includes = ['com.example.service.*', 'com.example.controller.*']
    excludes = ['com.example.internal.*']
}
```

```bash
./gradlew generateTests
```

## Naming Strategies

### method-scenario (default)

```java
@Test
void testFindById_WithValidId_ReturnsUser()
```

### bdd

```java
@Test
void shouldReturnUser_WhenFindByIdCalledWithValidId()
```

### given-when-then

```java
@Test
void givenValidId_whenFindById_thenReturnsUser()
```

## Generated Test Structure

For a Spring Service class:

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
```

The plugin generates:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testFindById_WithValidId_ReturnsUser() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindById_WithNullId_ReturnsNull() {
        User result = userService.findById(null);

        assertThat(result).isNull();
    }
}
```

## Requirements

- Java 17 or higher
- Gradle 7.0 or higher
- Spring Boot project

## Support

For issues, questions, or contributions:
- GitHub: https://github.com/mkankouyate/spring-test-generator
- Issues: https://github.com/mkankouyate/spring-test-generator/issues

## License

Apache License 2.0 - See LICENSE file for details
