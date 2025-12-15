import * as vscode from 'vscode';

export interface SpringTestConfig {
    sourceDirectory: string;
    outputDirectory: string;
    testType: 'unit' | 'integration' | 'all';
    namingStrategy: 'method-scenario' | 'bdd' | 'given-when-then';
    cliJarPath: string;
}

export function getConfig(): SpringTestConfig {
    const config = vscode.workspace.getConfiguration('springTestGenerator');

    return {
        sourceDirectory: config.get<string>('sourceDirectory') || 'src/main/java',
        outputDirectory: config.get<string>('outputDirectory') || 'src/test/java',
        testType: config.get<'unit' | 'integration' | 'all'>('testType') || 'all',
        namingStrategy: config.get<'method-scenario' | 'bdd' | 'given-when-then'>('namingStrategy') || 'method-scenario',
        cliJarPath: config.get<string>('cliJarPath') || ''
    };
}

export function getWorkspaceRoot(): string | undefined {
    const workspaceFolders = vscode.workspace.workspaceFolders;
    if (!workspaceFolders || workspaceFolders.length === 0) {
        return undefined;
    }
    return workspaceFolders[0].uri.fsPath;
}
