import { defineConfig, devices } from '@playwright/test';

/**
 * E2E tests require the Spring Boot backend running on port 8080:
 *   Terminal 1 (backend): mvn spring-boot:run -f ../pom.xml
 *
 * The Angular dev server (port 4200) is started automatically by Playwright.
 * In UI mode, click the ▶ "Run all" button to start execution.
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  retries: 1,
  workers: 1,
  fullyParallel: false,
  reporter: [['list'], ['html', { open: 'never' }]],

  // Playwright starts the Angular dev server automatically.
  // The Spring Boot backend must be started manually with: mvn spring-boot:run
  // Playwright waits for both servers to be ready before running any tests.
  webServer: [
    {
      command: 'npm start',
      url: 'http://localhost:4200',
      reuseExistingServer: true,
      timeout: 120_000,
    },
    {
      command: 'echo "Waiting for Spring Boot backend on port 8080..."',
      url: 'http://localhost:8080/account/A123',
      reuseExistingServer: true,
      timeout: 60_000,
    },
  ],

  use: {
    baseURL: 'http://localhost:4200',
    headless: true,
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    channel: undefined,
  },

  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        launchOptions: {
          executablePath: '/usr/bin/chromium-browser',
        },
      },
    },
  ],
});
