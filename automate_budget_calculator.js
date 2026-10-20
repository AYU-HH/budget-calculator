#!/usr/bin/env node
const { exec } = require('child_process');
const fs = require('fs');
const path = require('path');

// Configuration
const PROJECT_DIR = path.resolve(__dirname);
const PACKAGE_JSON = path.join(PROJECT_DIR, 'package.json');
const LOG_FILE = path.join(PROJECT_DIR, 'setup.log');
const ERROR_LOG_FILE = path.join(PROJECT_DIR, 'setup_errors.log');

// Logging functions
function log(message) {
    const timestamp = new Date().toISOString();
    const logEntry = `[${timestamp}] ${message}
`;
    fs.appendFileSync(LOG_FILE, logEntry);
    console.log(logEntry.trim());
}

function errorLog(message) {
    const timestamp = new Date().toISOString();
    const errorLogEntry = `[${timestamp}] ERROR: ${message}
`;
    fs.appendFileSync(ERROR_LOG_FILE, errorLogEntry);
    console.error(errorLogEntry.trim());
}

// Execute command with error handling
function runCommand(command, description) {
    return new Promise((resolve, reject) => {
        log(`Starting: ${description}`);
        exec(command, { cwd: PROJECT_DIR }, (error, stdout, stderr) => {
            if (error) {
                errorLog(`Error in ${description}: ${error.message}`);
                if (stderr) errorLog(stderr);
                reject(new Error(`Failed to execute ${description}: ${error.message}`));
            } else {
                log(`Completed: ${description}`);
                log(stdout);
                resolve(stdout);
            }
        });
    });
}

// Install dependencies
async function installDependencies() {
    try {
        await runCommand('npm install', 'Installing dependencies');
        log('Dependencies installed successfully.');
    } catch (error) {
        errorLog(`Failed to install dependencies: ${error.message}`);
        throw error;
    }
}

// Run tests
async function runTests() {
    try {
        await runCommand('npm test', 'Running tests');
        log('Tests completed.');
    } catch (error) {
        errorLog(`Tests failed: ${error.message}`);
        throw error;
    }
}

// Build the application
async function buildApplication() {
    try {
        await runCommand('npm run build', 'Building application');
        log('Build completed successfully.');
    } catch (error) {
        errorLog(`Build failed: ${error.message}`);
        throw error;
    }
}

// Start the development server
async function startServer() {
    try {
        const startCommand = 'npm start';
        log(`Starting server with command: ${startCommand}`);
        const serverProcess = exec(startCommand, { cwd: PROJECT_DIR });
        serverProcess.stdout.on('data', (data) => {
            log(`Server: ${data}`);
        });
        serverProcess.stderr.on('data', (data) => {
            errorLog(`Server error: ${data}`);
        });
        serverProcess.on('close', (code) => {
            if (code !== 0) {
                errorLog(`Server stopped with code ${code}`);
                throw new Error(`Server exited with code ${code}`);
            }
        });
        return serverProcess;
    } catch (error) {
        errorLog(`Failed to start server: ${error.message}`);
        throw error;
    }
}

// Deploy the application
async function deployApplication() {
    try {
        // Assuming a deployment script exists
        await runCommand('npm run deploy', 'Deploying application');
        log('Deployment completed successfully.');
    } catch (error) {
        errorLog(`Deployment failed: ${error.message}`);
        throw error;
    }
}

// Main function to orchestrate the process
async function main() {
    try {
        log('Starting budget calculator automation process...');

        // Step 1: Install dependencies
        await installDependencies();

        // Step 2: Run tests
        await runTests();

        // Step 3: Build the application
        await buildApplication();

        // Step 4: Start the development server
        const serverProcess = await startServer();

        // Step 5: Deploy the application (optional)
        // Uncomment if deployment is required
        // await deployApplication();

        log('Automation process completed successfully.');

    } catch (error) {
        errorLog(`Automation process failed: ${error.message}`);
        process.exit(1);
    }
}

// Run the main function
main();