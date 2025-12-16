# Spring Test Generator

Automatically generate comprehensive unit and integration tests for Spring Boot applications.

[![Java](https://img.shields.io/badge/JAVA-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/SPRING%20BOOT-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](#)
[![Maven](https://img.shields.io/badge/MAVEN-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](#)
[![Gradle](https://img.shields.io/badge/GRADLE-02303A?style=for-the-badge&logo=gradle&logoColor=white)](#)
[![JUnit5](https://img.shields.io/badge/JUNIT5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](#)
[![Mockito](https://img.shields.io/badge/MOCKITO-6DB33F?style=for-the-badge)](#)
[![License](https://img.shields.io/badge/LICENSE-APACHE%202.0-blue?style=for-the-badge)](#)

## Features

- **Intelligent Test Generation**: Automatically generates unit and integration tests for Spring components
- **Multi-Component Support**: Handles REST Controllers, Services, Repositories, and more
- **Smart Mocking**: Automatically mocks dependencies using Mockito
- **Multiple Integration Methods**:
  - Maven Plugin
  - Gradle Plugin
  - Command-Line Interface (CLI)
  - VS Code Extension
- **Customizable Templates**: Uses FreeMarker templates for flexible test generation
- **Modern Java**: Built with Java 17+ and Spring Boot 3.2+
- **Best Practices**: Generates tests following industry best practices
- **JUnit 5 & Mockito**: Uses the latest testing frameworks

## Quick Start

### Maven Plugin

Add the plugin to your `pom.xml`:

```xml
<plugin>
    <groupId>io.github.mohmk10</groupId>
    <artifactId>spring-test-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Generate tests:

```bash
mvn spring-test-gen:generate
```

### Gradle Plugin

Add the plugin to your `build.gradle`:

```gradle
plugins {
    id 'io.github.mohmk10.spring-test-generator' version '1.0.0'
}

springTestGenerator {
    sourceDirectory = 'src/main/java'
    outputDirectory = 'src/test/java'
    testType = 'all'
}
```

Generate tests:

```bash
./gradlew generateTests
```

### Command-Line Interface

Download the CLI JAR and run:

```bash
java -jar spring-test-generator-cli.jar \
  --source-dir src/main/java \
  --output-dir src/test/java \
  --test-type all
```

### VS Code Extension

1. Install the extension from VS Code Marketplace
2. Open your Spring Boot project
3. Right-click on a Java class
4. Select "Spring Test Generator: Generate Tests"

## Usage Examples

### Generate Tests for a REST Controller

Given this controller:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
```

The generator creates:

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getUser_whenValidId_thenReturnsUser() throws Exception {
        User user = new User(1L, "John Doe");
        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
```

### Generate Tests for a Service

Given this service:

```java
@Service
public class UserService {
    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

The generator creates:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_whenUserExists_thenReturnsUser() {
        User user = new User(1L, "John Doe");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
    }

    @Test
    void findById_whenUserNotFound_thenThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1L))
            .isInstanceOf(UserNotFoundException.class);
    }
}
```

## Configuration

### Maven Plugin Configuration

```xml
<plugin>
    <groupId>io.github.mohmk10</groupId>
    <artifactId>spring-test-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <sourceDirectory>src/main/java</sourceDirectory>
        <outputDirectory>src/test/java</outputDirectory>
        <testType>all</testType>
        <namingStrategy>method-scenario</namingStrategy>
    </configuration>
</plugin>
```

### Gradle Plugin Configuration

```gradle
springTestGenerator {
    sourceDirectory = 'src/main/java'
    outputDirectory = 'src/test/java'
    testType = 'all'
    namingStrategy = 'method-scenario'
}
```

### Configuration Options

| Option | Values | Description |
|--------|--------|-------------|
| `sourceDirectory` | Path | Source code directory (default: `src/main/java`) |
| `outputDirectory` | Path | Test output directory (default: `src/test/java`) |
| `testType` | `unit`, `integration`, `all` | Types of tests to generate |
| `namingStrategy` | `method-scenario`, `bdd`, `given-when-then` | Test method naming convention |

## Building from Source

### Requirements

- JDK 17 or later
- Maven 3.9+
- Node.js 16+ (for VS Code extension)

### Build All Modules

```bash
mvn clean install
```

### Build Specific Modules

```bash
mvn clean install -pl core,cli,maven-plugin
```

### Build Gradle Plugin

```bash
cd gradle-plugin
./gradlew build
```

### Build VS Code Extension

```bash
cd ide-integration/vscode
npm install
npm run compile
```

## Project Structure

```
spring-test-generator/
├── core/                      # Core analysis and generation engine
├── cli/                       # Command-line interface
├── maven-plugin/              # Maven plugin
├── gradle-plugin/             # Gradle plugin
├── ide-integration/
│   └── vscode/               # VS Code extension
└── pom.xml                   # Parent POM
```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Links

- [GitHub Repository](https://github.com/Mohmk10/spring-test-generator)
- [Maven Central](https://central.sonatype.com/artifact/io.github.mohmk10/spring-test-generator)
- [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.mohmk10.spring-test-generator)
- [VS Code Marketplace](https://marketplace.visualstudio.com/items?itemName=mohmk10.spring-test-generator)

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/Mohmk10/spring-test-generator/issues) on GitHub.
