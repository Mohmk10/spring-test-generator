import { exec } from 'child_process';
import * as path from 'path';
import * as fs from 'fs';
import { SpringTestConfig, getWorkspaceRoot } from '../config';

export interface CliResult {
    success: boolean;
    stdout: string;
    stderr: string;
    exitCode: number;
}

export class CliRunner {
    private jarPath: string;

    constructor(config: SpringTestConfig) {
        this.jarPath = this.resolveJarPath(config.cliJarPath);
    }

    private resolveJarPath(configPath: string): string {
        if (configPath && fs.existsSync(configPath)) {
            return configPath;
        }

        const workspaceRoot = getWorkspaceRoot();
        if (!workspaceRoot) {
            throw new Error('No workspace folder found');
        }

        const possiblePaths = [
            path.join(workspaceRoot, 'cli/target/spring-test-generator.jar'),
            path.join(workspaceRoot, '../cli/target/spring-test-generator.jar'),
            path.join(workspaceRoot, 'target/spring-test-generator.jar')
        ];

        for (const p of possiblePaths) {
            if (fs.existsSync(p)) {
                return p;
            }
        }

        throw new Error('CLI JAR not found. Please build the project or configure cliJarPath in settings.');
    }

    async generate(config: SpringTestConfig): Promise<CliResult> {
        const workspaceRoot = getWorkspaceRoot();
        if (!workspaceRoot) {
            throw new Error('No workspace folder found');
        }

        const sourceDir = path.join(workspaceRoot, config.sourceDirectory);
        const outputDir = path.join(workspaceRoot, config.outputDirectory);

        const command = [
            'java',
            '-jar',
            `"${this.jarPath}"`,
            'generate',
            '-s', `"${sourceDir}"`,
            '-o', `"${outputDir}"`,
            '--type', config.testType,
            '--naming', config.namingStrategy
        ].join(' ');

        return this.executeCommand(command);
    }

    async analyze(config: SpringTestConfig): Promise<CliResult> {
        const workspaceRoot = getWorkspaceRoot();
        if (!workspaceRoot) {
            throw new Error('No workspace folder found');
        }

        const sourceDir = path.join(workspaceRoot, config.sourceDirectory);

        const command = [
            'java',
            '-jar',
            `"${this.jarPath}"`,
            'analyze',
            '-s', `"${sourceDir}"`
        ].join(' ');

        return this.executeCommand(command);
    }

    private executeCommand(command: string): Promise<CliResult> {
        return new Promise((resolve) => {
            exec(command, { maxBuffer: 10 * 1024 * 1024 }, (error, stdout, stderr) => {
                resolve({
                    success: error === null,
                    stdout: stdout.toString(),
                    stderr: stderr.toString(),
                    exitCode: error?.code || 0
                });
            });
        });
    }

    parseGeneratedFiles(stdout: string): string[] {
        const files: string[] = [];
        const lines = stdout.split('\n');

        for (const line of lines) {
            if (line.includes('Generated:')) {
                const match = line.match(/Generated:\s+(.+\.java)/);
                if (match) {
                    files.push(match[1]);
                }
            }
        }

        return files;
    }
}
