# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Maven Plugin integration
- Gradle Plugin integration
- Watch mode for auto-regeneration
- Coverage gap analysis report

---

## [1.0.0] - 2024-12-02

### 🎉 Initial Release

#### Added
- **CLI Tool** - Command-line interface for test generation
- **Core Analysis Engine**
    - JavaParser-based code analysis
    - Spring annotation detection (@Service, @Controller, @Repository)
    - Method signature analysis
    - Dependency detection
    - Exception detection
    - Validation annotation detection

- **Test Generators**
    - Service test generator (@Service)
    - Controller test generator (@RestController, @Controller)
    - Repository test generator (@Repository)
    - Integration test generator
    - Edge case test generator
    - Exception test generator

- **Smart Mocking**
    - Automatic @Mock/@MockBean generation
    - when/thenReturn stub generation
    - verify() call generation

- **Assertion Generation**
    - AssertJ assertions
    - Null checks
    - Optional assertions
    - Collection assertions
    - Exception assertions

- **Test Naming Strategies**
    - METHOD_SCENARIO_EXPECTED (default)
    - GIVEN_WHEN_THEN
    - BDD
    - SIMPLE

- **Template Engine**
    - Freemarker-based templates
    - Customizable templates
    - Service test template
    - Controller test template
    - Repository test template
    - Integration test template

- **CLI Commands**
    - `analyze` - Analyze Spring Boot project
    - `generate` - Generate tests
    - `config` - Manage configuration
    - `report` - Generate coverage report

- **Configuration**
    - File-based configuration (.spring-test-gen.properties)
    - Command-line options
    - Dry-run mode
    - Skip existing tests
    - Package filtering
    - Custom templates

- **Documentation**
    - Comprehensive README
    - Installation guide
    - Quick start guide
    - CLI reference
    - Configuration reference
    - Examples
    - Contributing guidelines

#### Technical Details
- Java 21
- Spring Boot 3.2+ support
- JUnit 5
- Mockito
- AssertJ
- JavaParser 3.25.9
- Freemarker 2.3.32
- Picocli 4.7.5

---

## Legend

- `Added` - New features
- `Changed` - Changes in existing functionality
- `Deprecated` - Soon-to-be removed features
- `Removed` - Removed features
- `Fixed` - Bug fixes
- `Security` - Security fixes

---

[Unreleased]: https://github.com/Mohmk10/spring-test-generator/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/Mohmk10/spring-test-generator/releases/tag/v1.0.0