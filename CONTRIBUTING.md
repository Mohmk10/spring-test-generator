# Contributing to Spring Boot Test Generator

First off, thank you for considering contributing to Spring Boot Test Generator! 🎉

It's people like you that make this tool better for everyone in the Spring Boot community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)

---

## Code of Conduct

This project adheres to a Code of Conduct. By participating, you are expected to uphold this code.

**Be respectful, inclusive, and constructive.**

---

## How Can I Contribute?

### 🐛 Reporting Bugs

**Before submitting a bug report:**
- Check existing [issues](https://github.com/Mohmk10/spring-test-generator/issues)
- Ensure you're using the latest version
- Test with a minimal reproducible example

**When submitting a bug:**
1. Use the bug report template
2. Provide clear title and description
3. Include code samples
4. Specify your environment (Java version, Spring Boot version, OS)
5. Include relevant logs/stack traces

### 💡 Suggesting Features

**Before suggesting a feature:**
- Check if it already exists
- Review the roadmap
- Consider if it fits the project scope

**When suggesting a feature:**
1. Use the feature request template
2. Explain the problem it solves
3. Provide use cases
4. Suggest implementation approach (optional)

### 📝 Improving Documentation

Documentation improvements are always welcome:
- Fix typos or grammar
- Add examples
- Clarify existing content
- Translate to other languages

### 🔧 Contributing Code

**Good first issues:**
- Look for issues labeled `good first issue`
- Start with bug fixes or small features
- Ask questions if unclear

---

## Development Setup

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **Git**
- **IDE** (IntelliJ IDEA recommended)

### Clone & Build
```bash
# Clone repository
git clone https://github.com/Mohmk10/spring-test-generator.git
cd spring-test-generator

# Build project
mvn clean install

# Run tests
mvn test

# Run CLI locally
cd cli/target
java -jar spring-test-generator-cli-1.0.0-SNAPSHOT.jar --help
```

### Project Structure
```
spring-test-generator/
├── core/                 # Core library (analysis + generation)
├── cli/                  # Command-line interface
├── maven-plugin/         # Maven plugin (planned)
├── gradle-plugin/        # Gradle plugin (planned)
├── docs/                 # Documentation
└── .github/              # GitHub workflows & templates
```

### Development Workflow

1. **Create a branch**
```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
```

2. **Make changes**
    - Write code
    - Add/update tests
    - Update documentation

3. **Test locally**
```bash
   mvn clean test
```

4. **Commit changes**
```bash
   git add .
   git commit -m "feat: add new feature"
```

5. **Push & open PR**
```bash
   git push origin feature/your-feature-name
```

---

## Pull Request Process

### Before Submitting

- ✅ All tests pass (`mvn test`)
- ✅ Code coverage ≥ 80%
- ✅ Code follows style guidelines
- ✅ Documentation updated
- ✅ No merge conflicts

### PR Checklist
```markdown
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] CHANGELOG.md updated (if needed)
- [ ] No breaking changes (or documented)
- [ ] Commits follow convention
```

### Review Process

1. **Automated checks** run (build, tests, coverage)
2. **Maintainer review** within 48 hours
3. **Feedback addressed** if needed
4. **Merge** once approved

---

## Coding Standards

### Java Style

**Follow standard Java conventions:**
```java
// ✅ Good
public class UserService {
    
    private final UserRepository userRepository;
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}

// ❌ Bad
public class userService {
    private UserRepository repo;
    public User findById(Long id){
        return repo.findById(id).orElseThrow(()->new UserNotFoundException(id));
    }
}
```

**Key principles:**
- Use meaningful names
- Keep methods short (<20 lines)
- Avoid deep nesting (max 3 levels)
- No magic numbers
- Extract constants

### Code Organization
```java
// Order: static fields → instance fields → constructors → methods

public class MyClass {
    
    // Static fields
    private static final String CONSTANT = "value";
    
    // Instance fields
    private final Dependency dependency;
    
    // Constructor
    public MyClass(Dependency dependency) {
        this.dependency = dependency;
    }
    
    // Public methods
    public void publicMethod() { }
    
    // Private methods
    private void privateMethod() { }
}
```

### Lombok Usage

**Preferred Lombok annotations:**
- `@RequiredArgsConstructor` for dependency injection
- `@Slf4j` for logging
- `@Value` for immutable classes
- `@Builder` for complex objects

**Avoid:**
- `@Data` (too implicit)
- `@AllArgsConstructor` (use Builder)

### Comments

**Write self-documenting code:**
```java
// ✅ Good - code explains itself
public User findActiveUserById(Long userId) {
    return userRepository.findById(userId)
        .filter(User::isActive)
        .orElseThrow(() -> new UserNotFoundException(userId));
}

// ❌ Bad - needs comments
public User find(Long id) {
    // Get user from database
    User u = repo.get(id);
    // Check if active
    if (u.getStatus() == 1) {
        return u;
    }
    throw new Exception();
}
```

**Comments should explain WHY, not WHAT:**
```java
// ✅ Good
// Retry logic needed due to intermittent database locks
for (int i = 0; i < 3; i++) {
    try {
        return executeQuery();
    } catch (LockException e) {
        Thread.sleep(100);
    }
}

// ❌ Bad
// Loop 3 times
for (int i = 0; i < 3; i++) { }
```

---

## Testing Guidelines

### Test Coverage Requirements

- **Minimum:** 80% line coverage
- **Target:** 90%+ for core modules
- **Critical paths:** 100% coverage

### Test Structure

**Use Given-When-Then pattern:**
```java
@Test
@DisplayName("findById - should return user when exists")
void findById_WhenUserExists_ShouldReturnUser() {
    // Given
    Long userId = 1L;
    User user = createTestUser(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    
    // When
    User result = userService.findById(userId);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userId);
    verify(userRepository).findById(userId);
}
```

### Test Naming

**Convention:** `methodName_condition_expectedBehavior`
```java
// ✅ Good
findById_WhenUserExists_ShouldReturnUser()
findById_WhenUserNotFound_ShouldThrowException()
createUser_WithInvalidEmail_ShouldThrowValidationException()

// ❌ Bad
testFindById()
test1()
userTest()
```

### Test Quality

**Good tests are:**
- **Independent** - can run in any order
- **Repeatable** - same result every time
- **Fast** - execute quickly
- **Readable** - clear intent
- **Isolated** - test one thing

**Example:**
```java
// ✅ Good - tests one specific scenario
@Test
void createUser_WhenEmailAlreadyExists_ShouldThrowException() {
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
    
    assertThatThrownBy(() -> userService.createUser(request))
        .isInstanceOf(EmailAlreadyExistsException.class);
}

// ❌ Bad - tests multiple things
@Test
void createUserTest() {
    userService.createUser(validRequest);
    assertThatThrownBy(() -> userService.createUser(invalidRequest));
    assertThatThrownBy(() -> userService.createUser(null));
}
```

---

## Commit Message Guidelines

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding/updating tests
- `chore`: Build, dependencies, tooling

### Examples
```bash
# Feature
feat(generator): add edge case test generation

# Bug fix
fix(analyzer): handle null annotations correctly

# Documentation
docs(readme): update installation instructions

# Refactoring
refactor(cli): simplify command parsing logic

# Tests
test(generator): add tests for ServiceTestGenerator
```

### Rules

- Use imperative mood ("add" not "added")
- Don't capitalize first letter
- No period at the end
- Keep subject line ≤ 50 characters
- Wrap body at 72 characters
- Reference issues: `Fixes #123`

---

## Code Review Guidelines

### As a Reviewer

**Be constructive and kind:**
- ✅ "Consider extracting this to a method for clarity"
- ❌ "This code is terrible"

**Focus on:**
- Correctness
- Readability
- Test coverage
- Performance (if critical)
- Security issues

**Approve if:**
- Code works correctly
- Tests are adequate
- Style is consistent
- No obvious issues

### As an Author

**Respond to feedback:**
- Address all comments
- Ask questions if unclear
- Mark resolved comments
- Thank reviewers

**Don't take it personally:**
- Reviews improve code quality
- Everyone's code gets reviewed
- Learn from feedback

---

## Release Process

**Maintainers only:**

1. Update version in `pom.xml`
2. Update `CHANGELOG.md`
3. Create release tag
4. GitHub Actions handles build & publish
5. Announce on GitHub Discussions

---

## Questions?

- **Discussions:** [GitHub Discussions](https://github.com/Mohmk10/spring-test-generator/discussions)
- **Issues:** [GitHub Issues](https://github.com/Mohmk10/spring-test-generator/issues)
- **Email:** Open an issue instead (public discussion benefits everyone)

---

## Thank You! 🙏

Your contributions make this project better for the entire Spring Boot community.

**Happy coding!** 🚀
```

---

### Contenu : `LICENSE`
```
MIT License

Copyright (c) 2024 Mohamed Kouyate

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.