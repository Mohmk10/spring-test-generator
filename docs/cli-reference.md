# CLI Reference

Complete reference for all Spring Boot Test Generator commands and options.

## Table of Contents

- [Global Options](#global-options)
- [Commands](#commands)
    - [analyze](#analyze-command)
    - [generate](#generate-command)
    - [config](#config-command)
    - [report](#report-command)
    - [templates](#templates-command)
    - [watch](#watch-command)
- [Exit Codes](#exit-codes)
- [Examples](#examples)

---

## Global Options

Available for all commands:

| Option | Short | Description |
|--------|-------|-------------|
| `--help` | `-h` | Show help message |
| `--version` | `-V` | Show version information |

**Usage:**
```bash
java -jar spring-test-gen.jar --help
java -jar spring-test-gen.jar --version
```

---

## Commands

### `analyze` Command

Analyze a Spring Boot project to identify testable classes.

**Syntax:**
```bash
java -jar spring-test-gen.jar analyze [OPTIONS] [PROJECT_DIR]
```

**Arguments:**

| Argument | Required | Default | Description |
|----------|----------|---------|-------------|
| `PROJECT_DIR` | No | `.` | Project directory to analyze |

**Options:**

| Option | Short | Type | Default | Description |
|--------|-------|------|---------|-------------|
| `--package` | `-p` | String | - | Specific package to analyze |
| `--verbose` | `-v` | Flag | false | Enable verbose output |
| `--config` | `-c` | Path | `.spring-test-gen.properties` | Configuration file path |

**Examples:**
```bash
# Analyze current directory
java -jar spring-test-gen.jar analyze

# Analyze specific directory
java -jar spring-test-gen.jar analyze /path/to/project

# Analyze specific package
java -jar spring-test-gen.jar analyze . --package com.example.service

# Verbose output
java -jar spring-test-gen.jar analyze . -v

# Custom config file
java -jar spring-test-gen.jar analyze . --config custom-config.properties
```

**Output:**
```
════════════════════════════════════════════════════════════
  Analyzing Project
════════════════════════════════════════════════════════════

✓ Found 15 testable classes

Detailed Analysis:
==================

Class: UserService
  Type: Service
  Methods: 8
  Dependencies: 2
  Test Type: unit

Class: UserController
  Type: RestController
  Methods: 5
  Dependencies: 1
  Test Type: webmvc

...

Analysis Report:
==================
Total classes     : 15
Total methods     : 87
Total fields      : 45

Service count     : 8
Controller count  : 5
Repository count  : 2

Coverage estimation: ~78%
```

---

### `generate` Command

Generate tests for Spring Boot classes.

**Syntax:**
```bash
java -jar spring-test-gen.jar generate [OPTIONS] [PROJECT_DIR]
```

**Arguments:**

| Argument | Required | Default | Description |
|----------|----------|---------|-------------|
| `PROJECT_DIR` | No | `.` | Project directory |

**Options:**

| Option | Short | Type | Default | Description |
|--------|-------|------|---------|-------------|
| `--package` | `-p` | String | - | Specific package to generate tests for |
| `--class` | `-c` | String | - | Specific class to generate tests for |
| `--output` | `-o` | Path | `src/test/java` | Output directory for tests |
| `--dry-run` | - | Flag | false | Preview without writing files |
| `--skip-existing` | - | Flag | true | Skip classes with existing tests |
| `--verbose` | `-v` | Flag | false | Enable verbose output |
| `--config` | - | Path | `.spring-test-gen.properties` | Configuration file |

**Examples:**
```bash
# Generate all tests
java -jar spring-test-gen.jar generate

# Generate for specific package
java -jar spring-test-gen.jar generate . --package com.example.service

# Generate for specific class
java -jar spring-test-gen.jar generate . --class com.example.service.UserService

# Custom output directory
java -jar spring-test-gen.jar generate . --output custom/test/directory

# Dry run (preview)
java -jar spring-test-gen.jar generate . --dry-run

# Force regenerate (overwrite existing)
java -jar spring-test-gen.jar generate . --skip-existing=false

# Verbose output
java -jar spring-test-gen.jar generate . -v

# Combine options
java -jar spring-test-gen.jar generate . \
    --package com.example.service \
    --output src/test/java \
    --verbose
```

**Output:**
```
════════════════════════════════════════════════════════════
  Generating Tests
════════════════════════════════════════════════════════════

[========================================] 100% (15/15)

✓ Successfully generated tests for 15 classes

Generated files:
  src/test/java/com/example/service/UserServiceTest.java (8 tests)
  src/test/java/com/example/service/ProductServiceTest.java (12 tests)
  src/test/java/com/example/controller/UserControllerTest.java (5 tests)
  src/test/java/com/example/controller/ProductControllerTest.java (7 tests)
  src/test/java/com/example/repository/UserRepositoryTest.java (3 tests)
  ...

Total tests generated: 87

Completed in 2.34 seconds
```

**Dry Run Output:**
```
⚠ DRY RUN MODE - No files will be written

Preview of tests to be generated:

✓ UserServiceTest.java
  - findById_WhenUserExists_ShouldReturnUser
  - findById_WhenUserNotFound_ShouldThrowException
  - findById_WhenIdIsNull_ShouldThrowException
  - createUser_WithValidRequest_ShouldCreateUser
  - createUser_WhenEmailExists_ShouldThrowException
  ...

✓ UserControllerTest.java
  - getUser_WhenUserExists_ShouldReturn200
  - getUser_WhenUserNotFound_ShouldReturn404
  ...

Total: 15 classes, 87 tests
```

---

### `config` Command

Manage Spring Test Generator configuration.

**Syntax:**
```bash
java -jar spring-test-gen.jar config <SUBCOMMAND> [OPTIONS]
```

**Subcommands:**

#### `config init`

Initialize configuration file.
```bash
java -jar spring-test-gen.jar config init [OPTIONS]
```

**Options:**

| Option | Short | Description |
|--------|-------|-------------|
| `--force` | `-f` | Overwrite existing configuration |

**Examples:**
```bash
# Create config file
java -jar spring-test-gen.jar config init

# Force overwrite
java -jar spring-test-gen.jar config init --force
```

**Output:**
```
✓ Configuration file created: .spring-test-gen.properties
```

---

#### `config show`

Show current configuration.
```bash
java -jar spring-test-gen.jar config show
```

**Output:**
```
════════════════════════════════════════════════════════════
  Current Configuration
════════════════════════════════════════════════════════════

Source Directory    : src/main/java
Test Directory      : src/test/java
Skip Existing       : true
Generate Edge Cases : true
Generate Exceptions : true
Naming Convention   : METHOD_SCENARIO_EXPECTED
Max Tests Per Method: 10
```

---

#### `config set`

Set configuration values.
```bash
java -jar spring-test-gen.jar config set [OPTIONS]
```

**Options:**

| Option | Type | Description |
|--------|------|-------------|
| `--skip-existing` | Boolean | Skip existing tests |
| `--edge-cases` | Boolean | Generate edge case tests |
| `--exceptions` | Boolean | Generate exception tests |
| `--naming` | String | Test naming convention |

**Examples:**
```bash
# Disable edge case generation
java -jar spring-test-gen.jar config set --edge-cases=false

# Change naming convention
java -jar spring-test-gen.jar config set --naming=GIVEN_WHEN_THEN

# Multiple settings
java -jar spring-test-gen.jar config set \
    --skip-existing=false \
    --edge-cases=true \
    --naming=BDD
```

---

### `report` Command

Generate analysis report.

**Syntax:**
```bash
java -jar spring-test-gen.jar report [OPTIONS] [PROJECT_DIR]
```

**Options:**

| Option | Short | Type | Default | Description |
|--------|-------|------|---------|-------------|
| `--output` | `-o` | Path | - | Output file for report |
| `--format` | `-f` | String | `text` | Report format (text, json, html) |

**Examples:**
```bash
# Console report
java -jar spring-test-gen.jar report

# Save to file
java -jar spring-test-gen.jar report --output report.txt

# JSON format
java -jar spring-test-gen.jar report --format json --output report.json

# HTML format
java -jar spring-test-gen.jar report --format html --output report.html
```

**Output:**
```
╔═══════════════════════════════════════════════════════════╗
║           SPRING TEST GENERATOR - REPORT                  ║
╠═══════════════════════════════════════════════════════════╣
║ Classes analyzed     : 15                                 ║
║ Classes with tests   : 12                                 ║
║ Tests generated      : 87                                 ║
║                                                           ║
║ By type:                                                  ║
║   @Service tests     : 45                                 ║
║   @Controller tests  : 32                                 ║
║   @Repository tests  : 10                                 ║
║                                                           ║
║ Coverage estimation  : ~78%                               ║
║                                                           ║
║ Missing tests for:                                        ║
║   - ProductService.calculateDiscount()                    ║
║   - OrderController.cancelOrder()                         ║
║   - PaymentRepository.findByStatus()                      ║
╚═══════════════════════════════════════════════════════════╝
```

---

### `templates` Command

Manage test templates.

**Syntax:**
```bash
java -jar spring-test-gen.jar templates [OPTIONS]
```

**Status:** Coming in v1.1

---

### `watch` Command

Watch for changes and regenerate tests automatically.

**Syntax:**
```bash
java -jar spring-test-gen.jar watch [PROJECT_DIR]
```

**Status:** Coming in v1.1

---

## Exit Codes

| Code | Meaning |
|------|---------|
| `0` | Success |
| `1` | General error |
| `2` | Invalid arguments |
| `3` | Configuration error |
| `4` | Analysis error |
| `5` | Generation error |

**Usage in scripts:**
```bash
#!/bin/bash

java -jar spring-test-gen.jar generate .

if [ $? -eq 0 ]; then
    echo "✓ Tests generated successfully"
    mvn test
else
    echo "✗ Test generation failed"
    exit 1
fi
```

---

## Examples

### Complete Workflow
```bash
# Step 1: Initialize configuration
java -jar spring-test-gen.jar config init

# Step 2: Analyze project
java -jar spring-test-gen.jar analyze . --verbose

# Step 3: Preview generation
java -jar spring-test-gen.jar generate . --dry-run

# Step 4: Generate tests
java -jar spring-test-gen.jar generate .

# Step 5: Run tests
mvn test

# Step 6: Generate report
java -jar spring-test-gen.jar report --output report.txt
```

### Selective Generation
```bash
# Only services
java -jar spring-test-gen.jar generate . --package com.example.service

# Only controllers
java -jar spring-test-gen.jar generate . --package com.example.controller

# Specific class
java -jar spring-test-gen.jar generate . --class com.example.service.UserService
```

### CI/CD Integration
```bash
# Jenkins/GitHub Actions
java -jar spring-test-gen.jar generate . \
    --skip-existing=false \
    --verbose \
    || exit 1

mvn test || exit 1
```

### Custom Configuration
```bash
# Create custom config
cat > custom.properties << EOF
source.directory=src/main/java
test.directory=src/test/java
generate.edge.cases=true
generate.exception.tests=true
test.naming.convention=BDD
EOF

# Use custom config
java -jar spring-test-gen.jar generate . --config custom.properties
```

---

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_TEST_GEN_CONFIG` | Default config file path | `.spring-test-gen.properties` |
| `SPRING_TEST_GEN_VERBOSE` | Enable verbose by default | `false` |

**Usage:**
```bash
export SPRING_TEST_GEN_CONFIG=/path/to/config.properties
export SPRING_TEST_GEN_VERBOSE=true

java -jar spring-test-gen.jar generate .
```

---

## Tips & Tricks

### Alias for Convenience
```bash
# Add to ~/.bashrc or ~/.zshrc
alias stg='java -jar /path/to/spring-test-gen.jar'

# Usage
stg analyze
stg generate
stg --help
```

### Git Integration
```bash
# Generate tests for changed files only
git diff --name-only HEAD | \
    grep 'src/main/java.*\.java$' | \
    xargs -I {} java -jar spring-test-gen.jar generate . --class {}
```

### Parallel Generation
```bash
# Generate multiple packages in parallel
packages=("com.example.service" "com.example.controller" "com.example.repository")

for pkg in "${packages[@]}"; do
    java -jar spring-test-gen.jar generate . --package "$pkg" &
done

wait
echo "✓ All tests generated"
```

---

## Need Help?
```bash
# Command help
java -jar spring-test-gen.jar <command> --help

# General help
java -jar spring-test-gen.jar --help

# Version info
java -jar spring-test-gen.jar --version
```

**Online resources:**
- [Quick Start](quick-start.md)
- [Configuration Guide](configuration.md)
- [Examples](examples.md)
- [GitHub Issues](https://github.com/Mohmk10/spring-test-generator/issues)