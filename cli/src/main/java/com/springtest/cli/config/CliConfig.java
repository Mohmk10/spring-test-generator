package com.springtest.cli.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CliConfig {

    private static final String CONFIG_FILE = ".springtest.yml";

    private String defaultSourcePath;
    private String defaultOutputPath;
    private String defaultTestType;
    private String defaultNamingStrategy;

    public CliConfig() {
        this.defaultSourcePath = "src/main/java";
        this.defaultOutputPath = "src/test/java";
        this.defaultTestType = "all";
        this.defaultNamingStrategy = "method-scenario";
    }

    public static CliConfig load() {
        Path configPath = Paths.get(System.getProperty("user.home"), CONFIG_FILE);
        CliConfig config = new CliConfig();

        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath);
                config.parseYaml(content);
            } catch (IOException e) {
                System.err.println("Warning: Could not load config file: " + e.getMessage());
            }
        }

        return config;
    }

    public static CliConfig loadFromDirectory(String directory) {
        Path configPath = Paths.get(directory, CONFIG_FILE);
        CliConfig config = new CliConfig();

        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath);
                config.parseYaml(content);
            } catch (IOException e) {
                System.err.println("Warning: Could not load config file: " + e.getMessage());
            }
        }

        return config;
    }

    public void save() throws IOException {
        Path configPath = Paths.get(System.getProperty("user.home"), CONFIG_FILE);
        String yaml = toYaml();
        Files.writeString(configPath, yaml);
    }

    public void saveToDirectory(String directory) throws IOException {
        Path configPath = Paths.get(directory, CONFIG_FILE);
        String yaml = toYaml();
        Files.writeString(configPath, yaml);
    }

    private void parseYaml(String yaml) {
        String[] lines = yaml.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "defaultSourcePath":
                        this.defaultSourcePath = value;
                        break;
                    case "defaultOutputPath":
                        this.defaultOutputPath = value;
                        break;
                    case "defaultTestType":
                        this.defaultTestType = value;
                        break;
                    case "defaultNamingStrategy":
                        this.defaultNamingStrategy = value;
                        break;
                }
            }
        }
    }

    private String toYaml() {
        StringBuilder yaml = new StringBuilder();
        yaml.append("defaultSourcePath: ").append(defaultSourcePath).append("\n");
        yaml.append("defaultOutputPath: ").append(defaultOutputPath).append("\n");
        yaml.append("defaultTestType: ").append(defaultTestType).append("\n");
        yaml.append("defaultNamingStrategy: ").append(defaultNamingStrategy).append("\n");
        return yaml.toString();
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("defaultSourcePath", defaultSourcePath);
        map.put("defaultOutputPath", defaultOutputPath);
        map.put("defaultTestType", defaultTestType);
        map.put("defaultNamingStrategy", defaultNamingStrategy);
        return map;
    }

    public String getDefaultSourcePath() {
        return defaultSourcePath;
    }

    public void setDefaultSourcePath(String defaultSourcePath) {
        this.defaultSourcePath = defaultSourcePath;
    }

    public String getDefaultOutputPath() {
        return defaultOutputPath;
    }

    public void setDefaultOutputPath(String defaultOutputPath) {
        this.defaultOutputPath = defaultOutputPath;
    }

    public String getDefaultTestType() {
        return defaultTestType;
    }

    public void setDefaultTestType(String defaultTestType) {
        this.defaultTestType = defaultTestType;
    }

    public String getDefaultNamingStrategy() {
        return defaultNamingStrategy;
    }

    public void setDefaultNamingStrategy(String defaultNamingStrategy) {
        this.defaultNamingStrategy = defaultNamingStrategy;
    }
}
