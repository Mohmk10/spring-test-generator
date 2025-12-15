import * as vscode from 'vscode';
import { generateTests } from './commands/generateTests';
import { analyzeClasses } from './commands/analyzeClasses';

export function activate(context: vscode.ExtensionContext) {
    console.log('Spring Test Generator extension is now active');

    const generateCommand = vscode.commands.registerCommand(
        'springTestGen.generate',
        generateTests
    );

    const analyzeCommand = vscode.commands.registerCommand(
        'springTestGen.analyze',
        analyzeClasses
    );

    context.subscriptions.push(generateCommand);
    context.subscriptions.push(analyzeCommand);
}

export function deactivate() {
    console.log('Spring Test Generator extension is now deactivated');
}
