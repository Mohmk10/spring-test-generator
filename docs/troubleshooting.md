# Troubleshooting Guide

Solutions to common issues when using Spring Boot Test Generator.

## Table of Contents

- [Installation Issues](#installation-issues)
- [Analysis Problems](#analysis-problems)
- [Generation Issues](#generation-issues)
- [Compilation Errors](#compilation-errors)
- [Test Failures](#test-failures)
- [Performance Issues](#performance-issues)
- [Configuration Problems](#configuration-problems)

---

## Installation Issues

### Issue: "java: command not found"

**Symptoms:**
```bash
bash: java: command not found
```

**Cause:** Java is not installed or not in PATH

**Solution:**

**Check Java installation:**
```bash
java -version
```

**Install Java 21:**

**macOS (Homebrew):**
```bash
brew install openjdk@21
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

**Windows:**
1. Download from [Adoptium](https://adoptium.net/)
2. Install and add to PATH
3. Restart terminal

**Verify:**
```bash
java -version
# Should show: openjdk version "21" or higher
```

---

### Issue: "Unsupported class file major version 65"

**Symptoms:**
```
java.lang.UnsupportedClassVersionError: Unsupported major.minor version 65.0
```

**Cause:** Using Java < 21 (version 65 = Java 21)

**Solution:**

**1. Check current version:**
```bash
java -version
```

**2. Install Java 21 (see above)**

**3. Set JAVA_HOME:**
```bash
# macOS/Linux
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Add to ~/.bashrc or ~/.zshrc
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 21)' >> ~/.zshrc
```

---

### Issue: "Unable to access jarfile"

**Symptoms:**
```
Error: Unable to access jarfile spring-test-gen.jar
```

**Cause:** JAR file not found or wrong path

**Solution:**

**1. Verify file exists:**
```bash
ls -la spring-test-gen.jar
```

**2. Use absolute path:**
```bash
java -jar /full/path/to/spring-test-gen.jar --version
```

**3. Download again if missing:**
```bash
curl -L https://github.com/Mohmk10/spring-test-generator/releases/latest/download/spring-test-gen.jar -o spring-test-gen.jar
```

---

## Analysis Problems

### Issue: "No testable classes found"

**Symptoms:**
```
⚠ No testable classes found.
```

**Causes & Solutions:**

**1. Wrong directory:**
```bash
# ❌ Wrong
cd /wrong/path
java -jar spring-test-gen.jar analyze .

# ✅ Correct - Navigate to project root
cd /path/to/spring-boot-project
java -jar spring-test-gen.jar analyze .
```

**2. No Spring annotations:**
```bash
# Check for Spring classes
find src/main/java -name "*.java" | xargs grep -l "@Service\|@Controller\|@Repository"
```

**3. Source directory doesn't exist:**
```bash
# Verify src/main/java exists
ls -la src/main/java
```

**4. All tests already exist:**
```bash
# Disable skip-existing
java -jar spring-test-gen.jar analyze . --skip-existing=false
```

---

### Issue: "Package directory does not exist"

**Symptoms:**
```
WARN Package directory does not exist: src/main/java/com/example/service
```

**Cause:** Specified package doesn't exist

**Solution:**

**1. List available packages:**
```bash
find src/main/java -type d
```

**2. Use correct package name:**
```bash
# Check actual package structure
tree src/main/java

# Use correct package
java -jar spring-test-gen.jar analyze . --package com.actual.package
```

---

### Issue: "Cannot parse Java file"

**Symptoms:**
```
ERROR Failed to parse: UserService.java
```

**Causes & Solutions:**

**1. Syntax errors in source:**
```bash
# Compile source first to check syntax
mvn compile
```

**2. Unsupported Java features:**
- Ensure source is Java 21 compatible
- Check for experimental features

**3. File encoding issues:**
```bash
# Check file encoding
file -I src/main/java/com/example/UserService.java

# Convert if needed (should be UTF-8)
iconv -f ISO-8859-1 -t UTF-8 file.java > file_utf8.java
```

---

## Generation Issues

### Issue: "No classes to generate tests for"

**Symptoms:**
```
⚠ No classes to generate tests for.
```

**Solutions:**

**1. Check if tests already exist:**
```bash
# Show what would be generated
java -jar spring-test-gen.jar generate . --dry-run
```

**2. Force regeneration:**
```bash
java -jar spring-test-gen.jar generate . --skip-existing=false
```

**3. Verify package filter:**
```bash
# Remove package restriction
java -jar spring-test-gen.jar generate .

# Instead of
java -jar spring-test-gen.jar generate . --package com.wrong.package
```

---

### Issue: "Failed to write test file"

**Symptoms:**
```
✗ Failed to generate tests for UserService: Permission denied
```

**Causes & Solutions:**

**1. Permission issues:**
```bash
# Check permissions
ls -la src/test/java

# Fix permissions
chmod -R u+w src/test/java
```

**2. Directory doesn't exist:**
```bash
# Create test directory
mkdir -p src/test/java
```

**3. Disk full:**
```bash
# Check disk space
df -h
```

---

### Issue: "Template not found"

**Symptoms:**
```
ERROR Template not found: service-test.ftl
```

**Cause:** Custom template directory misconfigured

**Solution:**

**1. Verify template directory exists:**
```bash
ls -la src/test/resources/templates/
```

**2. Check configuration:**
```properties
# .spring-test-gen.properties
use.custom.templates=true
template.directory=src/test/resources/templates
```

**3. Verify template files:**
```bash
ls src/test/resources/templates/*.ftl
```

**4. Disable custom templates:**
```properties
use.custom.templates=false
```

---

## Compilation Errors

### Issue: "Cannot find symbol: @Mock"

**Symptoms:**
```
error: cannot find symbol
  @Mock
   ^
  symbol: class Mock
```

**Cause:** Missing Mockito dependency

**Solution:**

**Maven - Add to `pom.xml`:**
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
```

**Gradle - Add to `build.gradle`:**
```groovy
testImplementation 'org.mockito:mockito-core:5.10.0'
testImplementation 'org.mockito:mockito-junit-jupiter:5.10.0'
```

---

### Issue: "Cannot find symbol: assertThat"

**Cause:** Missing AssertJ dependency

**Solution:**

**Maven:**
```xml
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.25.2</version>
    <scope>test</scope>
</dependency>
```

**Gradle:**
```groovy
testImplementation 'org.assertj:assertj-core:3.25.2'
```

---

### Issue: "Cannot find symbol: @WebMvcTest"

**Cause:** Missing Spring Boot Test dependency

**Solution:**

**Maven:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Gradle:**
```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-test'
```

---

### Issue: "Package org.junit.jupiter does not exist"

**Cause:** Missing JUnit 5 dependency

**Solution:**

**Maven:**
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
```

**Gradle:**
```groovy
testImplementation 'org.junit.jupiter:junit-jupiter:5.10.1'
```

---

## Test Failures

### Issue: Tests compile but fail at runtime

**Symptoms:**
```
Tests run: 1, Failures: 1, Errors: 0
  findById_WhenUserExists_ShouldReturnUser FAILED
```

**Common Causes:**

**1. Mock not initialized:**
```java
// ❌ Missing @ExtendWith
class UserServiceTest {
    @Mock
    private UserRepository userRepository; // Not initialized!
}

// ✅ Correct
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
}
```

**2. Wrong expected values:**
```java
// Generated test might have placeholder values
assertThat(result.getId()).isEqualTo(1L); // Adjust to actual value
```

**3. Missing test data setup:**
```java
// Add proper test data
User user = User.builder()
    .id(1L)
    .name("John")
    .email("john@example.com")
    .build();
```

---

### Issue: "NullPointerException in test"

**Cause:** Mock not set up or dependency not injected

**Solution:**

**1. Verify mock setup:**
```java
@BeforeEach
void setUp() {
    // Ensure mocks are initialized
    MockitoAnnotations.openMocks(this);
}
```

**2. Add missing when/thenReturn:**
```java
// Add mock behavior
when(userRepository.findById(1L)).thenReturn(Optional.of(user));
```

**3. Check @InjectMocks:**
```java
@InjectMocks
private UserService userService; // Ensure this is present
```

---

### Issue: "MockMvc is null"

**Cause:** Missing @WebMvcTest or @Autowired

**Solution:**
```java
@WebMvcTest(UserController.class)  // ✅ Required
class UserControllerTest {
    
    @Autowired  // ✅ Required
    private MockMvc mockMvc;
}
```

---

## Performance Issues

### Issue: Analysis is very slow

**Symptoms:**
- Takes several minutes to analyze
- High CPU usage
- No progress indication

**Solutions:**

**1. Analyze specific package:**
```bash
# Instead of entire project
java -jar spring-test-gen.jar analyze . --package com.example.service
```

**2. Exclude large directories:**
```properties
# .spring-test-gen.properties
exclude.patterns=**/generated/**,**/node_modules/**,**/target/**
```

**3. Increase JVM memory:**
```bash
java -Xmx4g -jar spring-test-gen.jar analyze .
```

---

### Issue: Generation is slow

**Solutions:**

**1. Generate incrementally:**
```bash
# Generate one package at a time
java -jar spring-test-gen.jar generate . --package com.example.service
java -jar spring-test-gen.jar generate . --package com.example.controller
```

**2. Disable edge cases temporarily:**
```properties
generate.edge.cases=false
generate.exception.tests=false
```

**3. Limit tests per method:**
```properties
max.tests.per.method=5
```

---

### Issue: "OutOfMemoryError: Java heap space"

**Symptoms:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Cause:** Large project exceeds default heap size

**Solution:**

**Increase heap size:**
```bash
# 2GB heap
java -Xmx2g -jar spring-test-gen.jar generate .

# 4GB heap for very large projects
java -Xmx4g -jar spring-test-gen.jar generate .

# 8GB heap
java -Xmx8g -jar spring-test-gen.jar generate .
```

---

## Configuration Problems

### Issue: "Configuration file not found"

**Symptoms:**
```
WARN Config file not found: .spring-test-gen.properties, using defaults
```

**This is NOT an error** - Generator uses default configuration

**To create config file:**
```bash
java -jar spring-test-gen.jar config init
```

---

### Issue: Configuration not being applied

**Solutions:**

**1. Verify file location:**
```bash
# Must be in project root
ls -la .spring-test-gen.properties
```

**2. Check file format:**
```properties
# ✅ Correct
source.directory=src/main/java

# ❌ Wrong (YAML format)
source:
  directory: src/main/java
```

**3. Verify property names:**
```bash
# Show active configuration
java -jar spring-test-gen.jar config show
```

**4. Use command-line override:**
```bash
java -jar spring-test-gen.jar generate . \
    -Dgenerate.edge.cases=true \
    -Dtest.naming.convention=BDD
```

---

## Getting Help

### Enable Verbose Logging
```bash
java -jar spring-test-gen.jar generate . --verbose
```

Shows:
- Which files are being analyzed
- Which tests are being generated
- Any warnings or errors
- Configuration being used

---

### Check Version
```bash
java -jar spring-test-gen.jar --version
```

Ensure you're using the latest version.

---

### Dry Run Mode
```bash
java -jar spring-test-gen.jar generate . --dry-run
```

Preview what will be generated without writing files.

---

### Clean State
```bash
# Remove all generated tests
find src/test/java -name "*Test.java" -delete

# Regenerate
java -jar spring-test-gen.jar generate .
```

---

### Minimal Reproducible Example

**Create minimal test case:**
```bash
# 1. Create simple project
mkdir test-project
cd test-project

# 2. Create minimal service
mkdir -p src/main/java/com/example
cat > src/main/java/com/example/SimpleService.java << 'EOF'
package com.example;

import org.springframework.stereotype.Service;

@Service
public class SimpleService {
    public String hello() {
        return "Hello";
    }
}
EOF

# 3. Try generation
java -jar spring-test-gen.jar generate . --verbose
```

---

## Still Having Issues?

### Search Existing Issues

[GitHub Issues](https://github.com/Mohmk10/spring-test-generator/issues)

### Open New Issue

**Include:**
1. **Version:** `java -jar spring-test-gen.jar --version`
2. **Java version:** `java -version`
3. **Command used:** Full command with options
4. **Error output:** Complete error message
5. **Configuration:** Content of `.spring-test-gen.properties`
6. **Sample code:** Minimal example that reproduces issue

**Template:**
```markdown
**Version:** 1.0.0
**Java:** OpenJDK 21.0.1
**OS:** macOS 14.2

**Command:**
java -jar spring-test-gen.jar generate . --verbose

**Error:**
[paste error output]

**Configuration:**
[paste .spring-test-gen.properties if used]

**Sample Code:**
[paste minimal code example]
```

---

## FAQ

**Q: Can I use with Java 17?**  
A: No, Java 21+ is required.

**Q: Does it work with Kotlin?**  
A: Not yet. Java only in v1.0.

**Q: Can I customize test structure?**  
A: Yes! See [Templates Guide](templates.md)

**Q: Will it overwrite my existing tests?**  
A: No, by default it skips existing tests. Use `--skip-existing=false` to force.

**Q: Can I use with Gradle?**  
A: Yes! CLI works with any build tool.

**Q: How do I report a bug?**  
A: [Open an issue](https://github.com/Mohmk10/spring-test-generator/issues/new)

---

## Additional Resources

- **[Installation Guide](installation.md)**
- **[Quick Start](quick-start.md)**
- **[CLI Reference](cli-reference.md)**
- **[Configuration](configuration.md)**
- **[GitHub Discussions](https://github.com/Mohmk10/spring-test-generator/discussions)**

---

**Last updated:** 2024-12-02