import * as vscode from 'vscode';
import * as path from 'path';
import { CliRunner } from '../utils/cliRunner';
import { getConfig, getWorkspaceRoot } from '../config';

export async function generateTests(): Promise<void> {
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
            progress.report({ message: 'Generating tests...' });

            try {
                const runner = new CliRunner(config);
                const result = await runner.generate(config);

                if (result.success) {
                    const generatedFiles = runner.parseGeneratedFiles(result.stdout);

                    if (generatedFiles.length > 0) {
                        vscode.window.showInformationMessage(
                            `Successfully generated ${generatedFiles.length} test file(s)`
                        );

                        const shouldOpen = await vscode.window.showQuickPick(
                            ['Yes', 'No'],
                            { placeHolder: 'Open generated files?' }
                        );

                        if (shouldOpen === 'Yes') {
                            await openGeneratedFiles(generatedFiles, workspaceRoot, config.outputDirectory);
                        }
                    } else {
                        vscode.window.showWarningMessage('No tests were generated. Check the output for details.');
                        showOutput(result.stdout, result.stderr);
                    }
                } else {
                    vscode.window.showErrorMessage('Failed to generate tests. Check the output for details.');
                    showOutput(result.stdout, result.stderr);
                }
            } catch (error) {
                const errorMessage = error instanceof Error ? error.message : String(error);
                vscode.window.showErrorMessage(`Error: ${errorMessage}`);
            }
        }
    );
}

async function openGeneratedFiles(files: string[], workspaceRoot: string, outputDir: string): Promise<void> {
    for (const file of files) {
        const filePath = path.join(workspaceRoot, outputDir, file.replace(/\./g, '/') + '.java');
        try {
            const document = await vscode.workspace.openTextDocument(filePath);
            await vscode.window.showTextDocument(document, { preview: false });
        } catch (error) {
            console.error(`Failed to open file: ${filePath}`, error);
        }
    }
}

function showOutput(stdout: string, stderr: string): void {
    const outputChannel = vscode.window.createOutputChannel('Spring Test Generator');
    outputChannel.clear();
    outputChannel.appendLine('=== STDOUT ===');
    outputChannel.appendLine(stdout);
    if (stderr) {
        outputChannel.appendLine('\n=== STDERR ===');
        outputChannel.appendLine(stderr);
    }
    outputChannel.show();
}
