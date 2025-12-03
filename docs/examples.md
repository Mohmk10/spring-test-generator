# Examples

Real-world examples of using Spring Boot Test Generator.

## Table of Contents

- [Basic Examples](#basic-examples)
- [Spring Boot Projects](#spring-boot-projects)
- [Advanced Scenarios](#advanced-scenarios)
- [CI/CD Integration](#cicd-integration)

---

## Basic Examples

### Example 1: Simple Service

**Input:** `UserService.java`
```java
package com.example.service;

import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    public User createUser(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }

        User user = new User(name, email);
        return userRepository.save(user);
    }
}
```

**Command:**
```bash
java -jar spring-test-gen.jar generate . --class com.example.service.UserService
```

**Output:** `UserServiceTest.java`
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // findById tests

    @Test
    @DisplayName("findById - should return user when exists")
    void findById_WhenUserExists_ShouldReturnUser() {
        Long userId = 1L;
        User user = User.builder().id(userId).name("John").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.findById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("findById - should throw exception when not found")
    void findById_WhenUserNotFound_ShouldThrowException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: 999");
    }

    // createUser tests

    @Test
    @DisplayName("createUser - should create user with valid input")
    void createUser_WithValidInput_ShouldCreateUser() {
        User user = new User("John", "john@example.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser("John", "john@example.com");

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - should throw exception when name is null")
    void createUser_WhenNameIsNull_ShouldThrowException() {
        assertThatThrownBy(() -> userService.createUser(null, "john@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be empty");
    }

    @Test
    @DisplayName("createUser - should throw exception when name is blank")
    void createUser_WhenNameIsBlank_ShouldThrowException() {
        assertThatThrownBy(() -> userService.createUser("   ", "john@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be empty");
    }

    @Test
    @DisplayName("createUser - should throw exception when email is invalid")
    void createUser_WhenEmailIsInvalid_ShouldThrowException() {
        assertThatThrownBy(() -> userService.createUser("John", "invalid-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email");
    }
}
```

---

### Example 2: REST Controller

**Input:** `UserController.java`
```java
package com.example.controller;

import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.getName(), request.getEmail());
        return ResponseEntity.status(201).body(UserResponse.from(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Command:**
```bash
java -jar spring-test-gen.jar generate . --class com.example.controller.UserController
```

**Output:** `UserControllerTest.java`
```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/users/{id} - should return 200 when user exists")
    void getUser_WhenUserExists_ShouldReturn200() throws Exception {
        Long userId = 1L;
        User user = User.builder().id(userId).name("John").build();
        when(userService.findById(userId)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John"));

        verify(userService).findById(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id} - should return 404 when user not found")
    void getUser_WhenUserNotFound_ShouldReturn404() throws Exception {
        Long userId = 999L;
        when(userService.findById(userId)).thenThrow(new UserNotFoundException("Not found"));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/users - should return 201 when user created")
    void createUser_WithValidRequest_ShouldReturn201() throws Exception {
        CreateUserRequest request = new CreateUserRequest("John", "john@example.com");
        User user = User.builder().id(1L).name("John").build();
        when(userService.createUser(any(), any())).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/users - should return 400 when request is invalid")
    void createUser_WithInvalidRequest_ShouldReturn400() throws Exception {
        CreateUserRequest request = new CreateUserRequest(null, "invalid-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - should return 204 when deleted")
    void deleteUser_WhenUserExists_ShouldReturn204() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }
}
```

---

### Example 3: Repository

**Input:** `UserRepository.java`
```java
package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByNameContaining(String name);

    boolean existsByEmail(String email);
}
```

**Command:**
```bash
java -jar spring-test-gen.jar generate . --class com.example.repository.UserRepository
```

**Output:** `UserRepositoryTest.java`
```java
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("findByEmail - should return user when exists")
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        User user = User.builder()
                .name("John")
                .email("john@example.com")
                .build();
        entityManager.persistAndFlush(user);

        Optional<User> result = userRepository.findByEmail("john@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("findByEmail - should return empty when not exists")
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> result = userRepository.findByEmail("notfound@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByNameContaining - should return matching users")
    void findByNameContaining_WhenUsersExist_ShouldReturnList() {
        User user1 = entityManager.persistAndFlush(
                User.builder().name("John Doe").email("john@example.com").build()
        );
        User user2 = entityManager.persistAndFlush(
                User.builder().name("Jane Doe").email("jane@example.com").build()
        );

        List<User> result = userRepository.findByNameContaining("Doe");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("existsByEmail - should return true when exists")
    void existsByEmail_WhenUserExists_ShouldReturnTrue() {
        entityManager.persistAndFlush(
                User.builder().email("john@example.com").build()
        );

        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - should return false when not exists")
    void existsByEmail_WhenUserNotExists_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        assertThat(exists).isFalse();
    }
}
```

---

## Spring Boot Projects

### Complete E-Commerce Example

**Project structure:**
```
ecommerce/
├── src/main/java/com/example/
│   ├── controller/
│   │   ├── ProductController.java
│   │   ├── OrderController.java
│   │   └── UserController.java
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── OrderService.java
│   │   └── UserService.java
│   └── repository/
│       ├── ProductRepository.java
│       ├── OrderRepository.java
│       └── UserRepository.java
```

**Generate all tests:**
```bash
cd ecommerce

# Analyze project
java -jar spring-test-gen.jar analyze .

# Generate all tests
java -jar spring-test-gen.jar generate .
```

**Result:**
```
✓ Generated 18 test classes
✓ Generated 156 test methods
✓ Estimated coverage: 82%
```

---

## Advanced Scenarios

### Scenario 1: Selective Package Generation

**Generate only service tests:**
```bash
java -jar spring-test-gen.jar generate . --package com.example.service
```

**Generate controllers and services:**
```bash
java -jar spring-test-gen.jar generate . \
    --package com.example.service,com.example.controller
```

---

### Scenario 2: Exclude Patterns

**Skip configuration and DTOs:**

Create `.spring-test-gen.properties`:
```properties
exclude.patterns=**/config/**,**/dto/**,**/*Application.java
```
```bash
java -jar spring-test-gen.jar generate .
```

---

### Scenario 3: Custom Naming Convention

**Use BDD naming:**
```properties
# .spring-test-gen.properties
test.naming.convention=BDD
```
```bash
java -jar spring-test-gen.jar generate .
```

**Generated test names:**
```java
should_return_user_when_exists()
should_throw_exception_when_not_found()
should_create_user_when_valid()
```

---

### Scenario 4: Integration Tests

**Enable integration tests:**
```properties
generate.integration.tests=true
```
```bash
java -jar spring-test-gen.jar generate .
```

**Generates:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userLifecycle_ShouldWorkEndToEnd() throws Exception {
        // CREATE
        mockMvc.perform(post("/api/users")...)
            .andExpect(status().isCreated());

        // READ
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());

        // DELETE
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
```

---

## CI/CD Integration

### GitHub Actions

**File:** `.github/workflows/test-generation.yml`
```yaml
name: Generate Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  generate-tests:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Download Test Generator
        run: |
          curl -L https://github.com/Mohmk10/spring-test-generator/releases/latest/download/spring-test-gen.jar \
            -o spring-test-gen.jar

      - name: Generate Tests
        run: |
          java -jar spring-test-gen.jar generate . \
            --skip-existing=false \
            --verbose

      - name: Run Tests
        run: mvn test

      - name: Upload Coverage
        uses: codecov/codecov-action@v3
```

---

### Jenkins Pipeline

**File:** `Jenkinsfile`
```groovy
pipeline {
    agent any

    stages {
        stage('Download Generator') {
            steps {
                sh '''
                    curl -L https://github.com/Mohmk10/spring-test-generator/releases/latest/download/spring-test-gen.jar \
                      -o spring-test-gen.jar
                '''
            }
        }

        stage('Generate Tests') {
            steps {
                sh '''
                    java -jar spring-test-gen.jar generate . \
                      --skip-existing=false \
                      --verbose
                '''
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Publish Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
                jacoco()
            }
        }
    }
}
```

---

### GitLab CI

**File:** `.gitlab-ci.yml`
```yaml
stages:
  - generate
  - test

generate-tests:
  stage: generate
  image: openjdk:21-jdk
  script:
    - curl -L https://github.com/Mohmk10/spring-test-generator/releases/latest/download/spring-test-gen.jar -o spring-test-gen.jar
    - java -jar spring-test-gen.jar generate . --skip-existing=false
  artifacts:
    paths:
      - src/test/java/

run-tests:
  stage: test
  image: maven:3.9-openjdk-21
  script:
    - mvn test
  dependencies:
    - generate-tests
```

---

## Batch Processing

### Generate for Multiple Projects

**Bash script:**
```bash
#!/bin/bash

projects=(
    "/path/to/project1"
    "/path/to/project2"
    "/path/to/project3"
)

for project in "${projects[@]}"; do
    echo "Generating tests for: $project"
    java -jar spring-test-gen.jar generate "$project"
done
```

---

## More Examples

See complete working examples in:
- [GitHub Repository](https://github.com/Mohmk10/spring-test-generator/tree/main/examples)
- [Sample Projects](https://github.com/Mohmk10/spring-test-generator-examples)

---

**Questions?** [Open an issue](https://github.com/Mohmk10/spring-test-generator/issues)