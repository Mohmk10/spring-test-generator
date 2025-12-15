import * as vscode from 'vscode';
import { CliRunner } from '../utils/cliRunner';
import { getConfig, getWorkspaceRoot } from '../config';

export async function analyzeClasses(): Promise<void> {
    const config = getConfig();
    const workspaceRoot = getWorkspaceRoot();

    if (!workspaceRoot) {
        vscode.window.showErrorMessage('No workspace folder found. Please open a workspace.');
        return;
    }

    await vscode.window.withProgress(
        {
            location: vscode.ProgressLocation.Notification,
            title: 'Spring Test Generator',
            cancellable: false
        },
        async (progress) => {
            progress.report({ message: 'Analyzing Spring classes...' });

            try {
                const runner = new CliRunner(config);
                const result = await runner.analyze(config);

                const outputChannel = vscode.window.createOutputChannel('Spring Test Generator - Analysis');
                outputChannel.clear();

                if (result.success) {
                    outputChannel.appendLine('Spring Boot Classes Analysis');
                    outputChannel.appendLine('='.repeat(60));
                    outputChannel.appendLine('');
                    outputChannel.appendLine(result.stdout);

                    if (result.stderr) {
                        outputChannel.appendLine('');
                        outputChannel.appendLine('=== Warnings ===');
                        outputChannel.appendLine(result.stderr);
                    }

                    outputChannel.show();
                    vscode.window.showInformationMessage('Analysis complete. Check the output channel.');
                } else {
                    outputChannel.appendLine('Analysis failed');
                    outputChannel.appendLine('='.repeat(60));
                    outputChannel.appendLine('');
                    outputChannel.appendLine('=== STDOUT ===');
                    outputChannel.appendLine(result.stdout);
                    outputChannel.appendLine('');
                    outputChannel.appendLine('=== STDERR ===');
                    outputChannel.appendLine(result.stderr);

                    outputChannel.show();
                    vscode.window.showErrorMessage('Failed to analyze classes. Check the output for details.');
                }
            } catch (error) {
                const errorMessage = error instanceof Error ? error.message : String(error);
                vscode.window.showErrorMessage(`Error: ${errorMessage}`);
            }
        }
    );
}
