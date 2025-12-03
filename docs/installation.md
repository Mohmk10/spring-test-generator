# Installation Guide

This guide covers all methods to install Spring Boot Test Generator.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation Methods](#installation-methods)
- [Verify Installation](#verify-installation)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required

- **Java 21 or higher**
```bash
  java -version
  # Should show: java version "21" or higher
```

- **Maven 3.9+** (for building from source)
```bash
  mvn -version
  # Should show: Apache Maven 3.9.x or higher
```

### Recommended

- **Git** (for cloning repository)
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)

---

## Installation Methods

### Method 1: Download Pre-built JAR (Recommended)

**Easiest and fastest method:**
```bash
# Download latest release
curl -L https://github.com/Mohmk10/spring-test-generator/releases/latest/download/spring-test-gen.jar -o spring-test-gen.jar

# Make executable (Unix/Mac)
chmod +x spring-test-gen.jar

# Run
java -jar spring-test-gen.jar --version
```

**Windows:**
```powershell
# Download using PowerShell
Invoke-WebRequest -Uri "https://github.com/Mohmk10/spring-test-generator/releases/latest/download/spring-test-gen.jar" -OutFile "spring-test-gen.jar"

# Run
java -jar spring-test-gen.jar --version
```

### Method 2: Build from Source

**For developers who want latest features:**
```bash
# Clone repository
git clone https://github.com/Mohmk10/spring-test-generator.git
cd spring-test-generator

# Build with Maven
mvn clean install -DskipTests

# JAR location
cd cli/target
ls spring-test-generator-cli-*.jar

# Run
java -jar spring-test-generator-cli-1.0.0-SNAPSHOT.jar --version
```

**Build with tests (recommended):**
```bash
mvn clean install
# This runs 160+ unit tests (~10 seconds)
```

### Method 3: Install Globally (Unix/Mac)

**Create a global command:**
```bash
# Download JAR
curl -L https://github.com/Mohmk10/spring-test-generator/releases/latest/download/spring-test-gen.jar -o spring-test-gen.jar

# Move to /usr/local/bin
sudo mv spring-test-gen.jar /usr/local/bin/spring-test-gen.jar

# Create wrapper script
echo '#!/bin/bash' | sudo tee /usr/local/bin/spring-test-gen
echo 'java -jar /usr/local/bin/spring-test-gen.jar "$@"' | sudo tee -a /usr/local/bin/spring-test-gen
sudo chmod +x /usr/local/bin/spring-test-gen

# Now you can use it anywhere
spring-test-gen --version
spring-test-gen analyze /path/to/project
```

### Method 4: Docker (Coming Soon)
```bash
# Pull image
docker pull mohmk10/spring-test-generator:latest

# Run
docker run -v $(pwd):/project mohmk10/spring-test-generator analyze /project
```

---

## Verify Installation

### Check Version
```bash
java -jar spring-test-gen.jar --version
# Output: Spring Boot Test Generator 1.0.0
```

### Run Help
```bash
java -jar spring-test-gen.jar --help
# Should display all available commands
```

### Test with Sample Project
```bash
# Create test directory
mkdir test-project
cd test-project

# Analyze (should report 0 classes)
java -jar spring-test-gen.jar analyze .

# Success if no errors
```

---

## Troubleshooting

### Issue: "java: command not found"

**Solution:** Install Java 21

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
- Download from [Adoptium](https://adoptium.net/)
- Install and add to PATH

### Issue: "Unsupported class file major version"

**Cause:** Using Java < 21

**Solution:** Upgrade to Java 21+
```bash
# Check current version
java -version

# Must show version 21 or higher
```

### Issue: "JAR file not executable"

**Solution:** Add execute permission
```bash
chmod +x spring-test-gen.jar
```

### Issue: Build fails with "Tests failed"

**Solution 1:** Skip tests during build
```bash
mvn clean install -DskipTests
```

**Solution 2:** Check Maven version
```bash
mvn -version
# Must be 3.9.0 or higher
```

### Issue: "OutOfMemoryError"

**Solution:** Increase heap size
```bash
java -Xmx2g -jar spring-test-gen.jar generate /large/project
```

### Issue: Slow analysis on large projects

**Solutions:**

**1. Analyze specific package:**
```bash
java -jar spring-test-gen.jar analyze . --package com.example.service
```

**2. Exclude patterns:**
```bash
# Create .spring-test-gen.properties
echo "exclude.patterns=**/generated/**,**/config/**" > .spring-test-gen.properties
```

**3. Use more memory:**
```bash
java -Xmx4g -jar spring-test-gen.jar analyze .
```

---

## Next Steps

- **[Quick Start Guide](quick-start.md)** - Generate your first tests
- **[CLI Reference](cli-reference.md)** - All commands and options
- **[Configuration](configuration.md)** - Customize behavior

---

## Need Help?

- **GitHub Issues:** [Report problems](https://github.com/Mohmk10/spring-test-generator/issues)
- **Discussions:** [Ask questions](https://github.com/Mohmk10/spring-test-generator/discussions)
- **Documentation:** [Full docs](../README.md)