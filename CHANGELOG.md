# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-12-15

### Added

#### Core Modules
- **Module 1: Code Analyzer**
  - Project analyzer for Spring Boot applications
  - Class scanner with annotation detection
  - Method analyzer for extracting method signatures and dependencies
  - Dependency analyzer for identifying autowired components

- **Module 2: Mock & Assertion Generators**
  - Mock generator for creating Mockito mocks
  - Assertion generator for common assertion patterns
  - Support for service, repository, and component mocking
  - Smart assertion generation based on return types

- **Module 3: Test Generators**
  - Service test generator with comprehensive test methods
  - Repository test generator with JPA-specific tests
  - Controller test generator with MockMvc integration
  - Component test generator for generic Spring components

- **Module 4: Edge Case & Exception Generators**
  - Edge case test generator for boundary conditions
  - Exception test generator for error scenarios
  - Null safety test generation
  - Validation test generation

- **Module 5: Template Engine**
  - FreeMarker-based template engine
  - Multiple naming strategies (method-scenario, BDD, given-when-then)
  - Customizable test templates
  - Test file writer with proper package structure

#### Build Tools & IDE Integration
- **Module 6: CLI**
  - Command-line interface using Picocli
  - Generate command for test generation
  - Analyze command for project analysis
  - Support for unit, integration, and all test types

- **Module 7: Maven Plugin**
  - GenerateTestsMojo for Maven integration
  - AnalyzeMojo for Spring class analysis
  - Configurable source and output directories
  - Support for includes/excludes patterns

- **Module 8: Gradle Plugin**
  - SpringTestGeneratorPlugin for Gradle integration
  - GenerateTestsTask for test generation
  - AnalyzeTask for project analysis
  - Extension-based configuration

- **Module 9: VS Code Extension**
  - TypeScript-based VS Code extension
  - Generate tests command with progress notifications
  - Analyze Spring classes command with output channel
  - Configurable settings for all parameters
  - Auto-detection of CLI JAR path

### Technical Details
- Java 21 required
- Spring Boot 3.2.1 compatibility
- Zero-comment code policy enforced
- Comprehensive test coverage (313 tests)
- Apache 2.0 License

### Maven Central Ready
- Complete POM metadata (url, licenses, developers, scm)
- Maven source and javadoc plugins configured
- GPG signing support via release profile
- Ready for publication to Maven Central

[1.0.0]: https://github.com/mkankouyate/spring-test-generator/releases/tag/v1.0.0
