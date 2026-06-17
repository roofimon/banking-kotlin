# We

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 17.3.17.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

E2E tests run with [Playwright](https://playwright.dev) against the real Spring Boot
backend. First-time setup:

```bash
npm install
npx playwright install chromium   # download the Chromium browser binary
```

With the backend running on port 8080 (`mvn spring-boot:run` from the repo root),
run `npm run e2e`. Playwright starts the Angular dev server itself. See the
[root README](../README.md#end-to-end-tests-playwright) for the full workflow and
the other `e2e:*` scripts.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
