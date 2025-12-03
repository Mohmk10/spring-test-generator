# Spring Boot Test Generator

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-brightgreen?style=flat-square)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)
![Build](https://img.shields.io/github/actions/workflow/status/Mohmk10/spring-test-generator/build.yml?style=flat-square)
![Coverage](https://img.shields.io/badge/Coverage-80%25-brightgreen?style=flat-square)

**Automatically generate high-quality unit and integration tests for Spring Boot applications**

[Quick Start](#quick-start) • [Documentation](#documentation) • [Examples](#examples) • [Contributing](#contributing)

</div>

---

## 🎯 Why Spring Test Generator?

Writing tests is **time-consuming and repetitive**. Spring Test Generator analyzes your Spring Boot code and **automatically generates**:

- ✅ **Unit tests** with Mockito for `@Service`, `@Controller`, `@Repository`
- ✅ **Integration tests** with `@SpringBootTest`
- ✅ **Edge case tests** (null checks, boundary values, validations)
- ✅ **Exception tests** for all thrown exceptions
- ✅ **Proper mocking** with `@Mock`, `@MockBean`
- ✅ **AssertJ assertions** for readable tests
- ✅ **100% production-ready** test code

**Save hours of work. Focus on business logic, not boilerplate tests.**

---

## 🚀 Quick Start

### Installation

**Option 1: Download CLI (Recommended)**
```bash
# Download latest release
curl -L https://github.com/Mohmk10/spring-test-generator/releases/latest/download/spring-test-gen.jar -o spring-test-gen.jar

# Make executable
chmod +x spring-test-gen.jar

# Run
java -jar spring-test-gen.jar --help
```

**Option 2: Build from Source**
```bash
git clone https://github.com/Mohmk10/spring-test-generator.git
cd spring-test-generator
mvn clean install
cd cli/target
java -jar spring-test-generator-cli-1.0.0-SNAPSHOT.jar
```

### Generate Your First Tests
```bash
# Analyze your Spring Boot project
java -jar spring-test-gen.jar analyze /path/to/your/project

# Generate tests for all classes
java -jar spring-test-gen.jar generate /path/to/your/project

# Generate tests for specific package
java -jar spring-test-gen.jar generate /path/to/your/project --package com.example.service

# Dry run (preview without writing)
java -jar spring-test-gen.jar generate /path/to/your/project --dry-run
```

**That's it!** Check your `src/test/java` directory for generated tests.

---

## 📖 Features

### 🎯 Smart Test Generation

**Analyzes your code structure:**
- Detects Spring stereotypes (`@Service`, `@Controller`, `@Repository`)
- Identifies dependencies to mock
- Analyzes method signatures and parameters
- Detects validation annotations
- Extracts exception handling

**Generates appropriate tests:**
- `@WebMvcTest` for `@Controller`/`@RestController`
- `@DataJpaTest` for `@Repository`
- `@ExtendWith(MockitoExtension.class)` for `@Service`
- `@SpringBootTest` for integration tests

### 🧪 Comprehensive Test Coverage

**For each public method, generates:**

1. **Happy path test** - Valid input, expected output
2. **Edge case tests** - Null, empty, boundary values
3. **Exception tests** - All declared/thrown exceptions
4. **Validation tests** - `@Valid`, `@NotNull`, constraints

**Example generated test:**
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
        User user = User.builder()
            .id(userId)
            .firstName("John")
            .lastName("Doe")
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        User result = userService.findById(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }
    
    @Test
    @DisplayName("findById - should throw exception when not found")
    void findById_WhenUserNotFound_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userService.findById(userId))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found: 999");
    }
}
```

### 🎨 Customization

**Test Naming Conventions:**
```bash
# METHOD_SCENARIO_EXPECTED (default)
findById_WhenUserExists_ShouldReturnUser()

# GIVEN_WHEN_THEN
givenUserExists_whenFindById_thenReturnUser()

# BDD
should_return_user_when_exists()

# SIMPLE
testFindById()
```

**Custom Templates:**
```bash
# Use custom Freemarker templates
java -jar spring-test-gen.jar generate --template-dir ./my-templates
```

**Configuration file** (`.spring-test-gen.properties`):
```properties
source.directory=src/main/java
test.directory=src/test/java
skip.existing=true
generate.edge.cases=true
generate.exception.tests=true
test.naming.convention=METHOD_SCENARIO_EXPECTED
max.tests.per.method=10
```

---

## 📚 Documentation

- **[Installation Guide](docs/installation.md)** - Detailed setup instructions
- **[Quick Start](docs/quick-start.md)** - Get running in 5 minutes
- **[CLI Reference](docs/cli-reference.md)** - All commands and options
- **[Configuration](docs/configuration.md)** - Customize generation
- **[Templates](docs/templates.md)** - Create custom templates
- **[Examples](docs/examples.md)** - Real-world examples
- **[Troubleshooting](docs/troubleshooting.md)** - Common issues

---

## 🎯 Examples

### Service Test Generation

**Input (`UserService.java`):**
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .build();
        return userRepository.save(user);
    }
}
```

**Output (`UserServiceTest.java`):**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    // 8 tests generated:
    // - findById_WhenUserExists_ShouldReturnUser
    // - findById_WhenUserNotFound_ShouldThrowException
    // - findById_WhenIdIsNull_ShouldThrowException
    // - createUser_WithValidRequest_ShouldCreateUser
    // - createUser_WhenEmailExists_ShouldThrowException
    // - createUser_WhenRequestIsNull_ShouldThrowException
    // - createUser_WithEmptyFirstName_ShouldThrowValidationException
    // - createUser_WithInvalidEmail_ShouldThrowValidationException
}
```

### Controller Test Generation

**Input (`UserController.java`):**
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
```

**Output (`UserControllerTest.java`):**
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Test
    void getUser_WhenUserExists_ShouldReturn200() throws Exception {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).firstName("John").build();
        when(userService.findById(userId)).thenReturn(user);
        
        // When/Then
        mockMvc.perform(get("/api/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.firstName").value("John"));
    }
}
```

More examples in [docs/examples.md](docs/examples.md)

---

## 🛠️ Technology Stack

- **Java 21** - Modern Java features
- **JavaParser 3.25.9** - AST analysis
- **Freemarker 2.3.32** - Template engine
- **Picocli 4.7.5** - CLI framework
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Maven** - Build tool

---

## 🏗️ Architecture
```
spring-test-generator/
├── core/                    # Analysis & generation engine
│   ├── analyzer/           # Code analysis (JavaParser)
│   ├── generator/          # Test generators
│   ├── mock/               # Mock generation
│   ├── assertion/          # Assertion generation
│   ├── template/           # Template engine
│   └── naming/             # Naming strategies
│
├── cli/                    # Command-line interface
│   ├── commands/           # CLI commands
│   └── output/             # Console output
│
├── maven-plugin/           # Maven plugin (planned)
└── gradle-plugin/          # Gradle plugin (planned)
```

---

## 📈 Roadmap

### ✅ Version 1.0 (Current)
- [x] CLI tool
- [x] Service test generation
- [x] Controller test generation
- [x] Repository test generation
- [x] Edge case generation
- [x] Exception test generation
- [x] Custom templates
- [x] Multiple naming conventions

### 🚧 Version 1.1 (In Progress)
- [ ] Maven plugin
- [ ] Gradle plugin
- [ ] Watch mode (auto-regenerate)
- [ ] Coverage gap report

### 🔮 Version 2.0 (Future)
- [ ] Spring Boot 3.3+ support
- [ ] Kotlin support
- [ ] GraphQL test generation
- [ ] Testcontainers integration
- [ ] AI-powered assertion suggestions

---

## 🤝 Contributing

We welcome contributions! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

**Ways to contribute:**
- 🐛 Report bugs
- 💡 Suggest features
- 📝 Improve documentation
- 🔧 Submit pull requests
- ⭐ Star the project

---

## 📄 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file.

**100% Free and Open Source** - Use it anywhere, anytime.

---

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- JavaParser team for the excellent AST library
- Open source community for inspiration

---

## 📞 Contact

- **Author:** Mohamed Kouyate
- **GitHub:** [@Mohmk10](https://github.com/Mohmk10)
- **Issues:** [GitHub Issues](https://github.com/Mohmk10/spring-test-generator/issues)

---

## ⭐ Star History

If this project helps you, please give it a ⭐!

[![Star History Chart](https://api.star-history.com/svg?repos=Mohmk10/spring-test-generator&type=Date)](https://star-history.com/#Mohmk10/spring-test-generator&Date)

---

<div align="center">

**Made with ❤️ for the Spring Boot community**

[⬆ Back to top](#spring-boot-test-generator)

</div>