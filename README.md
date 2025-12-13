# Spring Boot Test Generator

Automatic test generation tool for Spring Boot applications.

## Project Structure

This is a multi-module Maven project with the following modules:

```
spring-test-generator/
├── core/                   # Core library for code analysis and test generation
├── cli/                    # Command-line interface
├── maven-plugin/           # Maven plugin integration
├── gradle-plugin/          # Gradle plugin integration (prepared structure)
└── pom.xml                # Parent POM
```

## Technologies

- **Java 21**
- **Spring Boot 3.2+**
- **JavaParser** - Code analysis and AST manipulation
- **Picocli** - CLI framework
- **Freemarker** - Template engine for test generation
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions

## Modules

### Core Module
Contains the core functionality for:
- Analyzing Spring Boot code structure
- Detecting patterns (Controllers, Services, Repositories)
- Generating test templates
- Managing test generation strategies

### CLI Module
Standalone command-line tool for generating tests:
```bash
java -jar cli/target/spring-test-generator.jar [options]
```

### Maven Plugin
Maven integration for test generation:
```xml
<plugin>
    <groupId>com.springtestgen</groupId>
    <artifactId>spring-test-generator-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</plugin>
```

### Gradle Plugin
Gradle integration (structure prepared for future implementation):
```gradle
plugins {
    id 'com.springtestgen.gradle-plugin' version '1.0.0-SNAPSHOT'
}
```

## Building the Project

```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl core

# Skip tests
mvn clean install -DskipTests
```

## Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl core
```

## Development Setup

### Requirements
- JDK 21
- Maven 3.9+
- Git

### IDE Setup
Import as a Maven project in your favorite IDE:
- IntelliJ IDEA: File > Open > Select pom.xml
- Eclipse: File > Import > Existing Maven Projects
- VS Code: Open folder (with Java extension pack)

## License

TBD
