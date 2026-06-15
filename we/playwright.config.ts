import { defineConfig, devices } from '@playwright/test';

/**
 * E2E tests require the Spring Boot backend running on port 8080:
 *   Terminal 1 (backend): mvn spring-boot:run -f ../pom.xml
 *
 * The frontend on port 4200 is started automatically by Playwright:
 *   - default: `ng serve` (live reload — best for iterating locally,
 *     keep it warm between runs so you skip the first-compile cost).
 *   - E2E_STATIC=1: build once with esbuild and serve the static bundle.
 *     This avoids the dev-server's ~25s lazy first-compile, so cold runs
 *     (CI, fresh shell) are fast. Use `npm run e2e:ci`.
 * In UI mode, click the ▶ "Run all" button to start execution.
 */
const STATIC = !!process.env.E2E_STATIC;

export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  // Default expect timeout is 5s; under parallel load the first render of a
  // route can briefly exceed that, so give web-first assertions headroom.
  expect: { timeout: 10_000 },
  retries: 1,
  // 6 workers keeps the 12-vCPU box from saturating (browsers + backend +
  // dev-server). 8 oversubscribed and occasionally stalled a render past the
  // assertion timeout; 6 is nearly as fast and stable.
  workers: 6,
  fullyParallel: true,
  reporter: [['list'], ['html', { open: 'never' }]],

  // Playwright starts the frontend automatically (see header comment).
  // The Spring Boot backend must be started manually with: mvn spring-boot:run
  // Playwright waits for both servers to be ready before running any tests.
  webServer: [
    {
      command: STATIC ? 'npm run build && node e2e/static-server.mjs' : 'npm start',
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
      },
    },
  ],
});
