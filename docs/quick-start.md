# Quick Start Guide

Get Spring Boot Test Generator running in **5 minutes**.

## Step 1: Install
```bash
# Download
curl -L https://github.com/Mohmk10/spring-test-generator/releases/latest/download/spring-test-gen.jar -o spring-test-gen.jar

# Verify
java -jar spring-test-gen.jar --version
```

---

## Step 2: Navigate to Your Project
```bash
cd /path/to/your/spring-boot-project
```

**Your project should have:**
- `src/main/java` - Source code
- `pom.xml` or `build.gradle` - Build file
- Spring Boot classes (`@Service`, `@Controller`, etc.)

---

## Step 3: Analyze Your Project
```bash
java -jar spring-test-gen.jar analyze .
```

**Output example:**
```
════════════════════════════════════════════════════════════
  Analyzing Project
════════════════════════════════════════════════════════════

✓ Found 15 testable classes

Analysis Report:
==================
Total classes     : 15
Services          : 8
Controllers       : 5
Repositories      : 2

Methods analyzed  : 87
Fields analyzed   : 45
```

---

## Step 4: Generate Tests
```bash
java -jar spring-test-gen.jar generate .
```

**Output example:**
```
════════════════════════════════════════════════════════════
  Generating Tests
════════════════════════════════════════════════════════════

[========================================] 100% (15/15)

✓ Successfully generated tests for 15 classes

Generated files:
  src/test/java/com/example/service/UserServiceTest.java
  src/test/java/com/example/service/ProductServiceTest.java
  src/test/java/com/example/controller/UserControllerTest.java
  ...

Completed in 2.34 seconds
```

---

## Step 5: Review Generated Tests

**Open:** `src/test/java/com/example/service/UserServiceTest.java`
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
    
    // ... more tests
}
```

---

## Step 6: Run Tests
```bash
# Maven
mvn test

# Gradle
./gradlew test
```

**All generated tests should pass!** ✅

---

## Common Use Cases

### Generate for Specific Package
```bash
java -jar spring-test-gen.jar generate . --package com.example.service
```

### Generate for Specific Class
```bash
java -jar spring-test-gen.jar generate . --class com.example.service.UserService
```

### Preview Without Writing (Dry Run)
```bash
java -jar spring-test-gen.jar generate . --dry-run
```

### Skip Existing Tests
```bash
java -jar spring-test-gen.jar generate . --skip-existing
```

### Custom Output Directory
```bash
java -jar spring-test-gen.jar generate . --output src/test/java
```

---

## What Gets Generated?

### For Each Public Method

✅ **Happy path test** - Valid input, expected output  
✅ **Edge case tests** - Null, empty, boundary values  
✅ **Exception tests** - All thrown exceptions  
✅ **Validation tests** - `@Valid`, `@NotNull`, etc.

### Example: Service Method

**Input code:**
```java
@Service
public class UserService {
    
    public User findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

**Generated tests (3):**
1. `findById_WhenUserExists_ShouldReturnUser`
2. `findById_WhenUserNotFound_ShouldThrowUserNotFoundException`
3. `findById_WhenIdIsNull_ShouldThrowIllegalArgumentException`

---

## Configuration (Optional)

Create `.spring-test-gen.properties` in project root:
```properties
# Source and test directories
source.directory=src/main/java
test.directory=src/test/java

# Generation options
skip.existing=true
generate.edge.cases=true
generate.exception.tests=true

# Test naming convention
# Options: METHOD_SCENARIO_EXPECTED, GIVEN_WHEN_THEN, BDD, SIMPLE
test.naming.convention=METHOD_SCENARIO_EXPECTED

# Maximum tests per method
max.tests.per.method=10
```

---

## Troubleshooting

### No Classes Found

**Check:**
1. Are you in the project root?
2. Does `src/main/java` exist?
3. Are there Spring annotations (`@Service`, `@Controller`)?
```bash
# Verify directory
ls src/main/java

# Check for Spring classes
find src/main/java -name "*.java" | xargs grep -l "@Service\|@Controller\|@Repository"
```

### Tests Not Compiling

**Common issues:**

1. **Missing imports** - Add in your `pom.xml`:
```xml
   <dependency>
       <groupId>org.junit.jupiter</groupId>
       <artifactId>junit-jupiter</artifactId>
       <scope>test</scope>
   </dependency>
   <dependency>
       <groupId>org.mockito</groupId>
       <artifactId>mockito-junit-jupiter</artifactId>
       <scope>test</scope>
   </dependency>
```

2. **Java version mismatch** - Ensure Java 21+:
```bash
   java -version
```

### Tests Failing

**Review generated tests:**
- Adjust mock setup if needed
- Fix expected values
- Add missing test data

**Generated tests are a starting point** - customize as needed!

---

## Next Steps

- **[CLI Reference](cli-reference.md)** - All commands and options
- **[Configuration Guide](configuration.md)** - Customize generation
- **[Examples](examples.md)** - Real-world scenarios
- **[Templates](templates.md)** - Create custom templates

---

## Quick Command Reference
```bash
# Analyze project
java -jar spring-test-gen.jar analyze .

# Generate all tests
java -jar spring-test-gen.jar generate .

# Generate for package
java -jar spring-test-gen.jar generate . -p com.example.service

# Dry run
java -jar spring-test-gen.jar generate . --dry-run

# Help
java -jar spring-test-gen.jar --help

# Version
java -jar spring-test-gen.jar --version
```

---

**You're all set!** 🚀

Generate tests, save time, ship faster.