# Spring Test Generator for VS Code

Automatically generate unit and integration tests for Spring Boot applications directly in VS Code.

## Features

- **Generate Tests**: Automatically generate unit and integration tests for Spring Boot classes
- **Analyze Spring Classes**: Analyze and display all Spring Boot components in your project
- **Configurable**: Customize test generation with various options

## Requirements

- Java 17 or higher
- Maven project with Spring Test Generator CLI JAR built
- VS Code 1.80.0 or higher

## Installation

1. Build the Spring Test Generator CLI JAR:
   ```bash
   mvn clean package -pl core,cli
   ```

2. Install the VS Code extension (development mode):
   ```bash
   cd ide-integration/vscode
   npm install
   npm run compile
   ```

3. Press F5 in VS Code to launch Extension Development Host

## Usage

### Generate Tests

1. Open Command Palette (Ctrl+Shift+P / Cmd+Shift+P)
2. Run command: "Spring Test Generator: Generate Tests"
3. Tests will be generated in the configured output directory
4. Choose to open generated files

### Analyze Spring Classes

1. Open Command Palette (Ctrl+Shift+P / Cmd+Shift+P)
2. Run command: "Spring Test Generator: Analyze Spring Classes"
3. View results in the Output channel

## Configuration

Open VS Code Settings and search for "Spring Test Generator":

- `springTestGenerator.sourceDirectory`: Source code directory (default: `src/main/java`)
- `springTestGenerator.outputDirectory`: Test output directory (default: `src/test/java`)
- `springTestGenerator.testType`: Type of tests to generate - `unit`, `integration`, or `all` (default: `all`)
- `springTestGenerator.namingStrategy`: Test naming strategy - `method-scenario`, `bdd`, or `given-when-then` (default: `method-scenario`)
- `springTestGenerator.cliJarPath`: Path to CLI JAR (auto-detected if empty)

## Commands

- `springTestGen.generate`: Generate tests for Spring Boot classes
- `springTestGen.analyze`: Analyze and display Spring Boot classes

## Development

Build the extension:
```bash
npm run compile
```

Watch mode for development:
```bash
npm run watch
```

## License

This extension is part of the Spring Test Generator project.
