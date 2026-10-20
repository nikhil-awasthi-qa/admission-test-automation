# Student Admission Portal: UI + API + Database Test Automation

![Tests](https://github.com/nikhil-awasthi-qa/admission-test-automation/actions/workflows/tests.yml/badge.svg)

One small web app and one automated test suite that checks the same admission application at **three layers**:

| Layer | Tool | What it checks |
|-------|------|----------------|
| UI | Selenium WebDriver (Java), Page Object Model | The form a student fills in, validation messages, confirmation page, the admission office screen |
| API | Rest Assured | `POST / GET / PUT /api/applicants`, status codes, error messages |
| Database | SQL through JDBC (H2) | What is really stored: values, status changes, unique email, row counts |

The most important test is `EndToEndTest`: a student applies in the browser, the API returns the same record, the database row matches, the office approves it in the browser, and the API and database both show `APPROVED`.

This is my own practice project. The application is a simple demo that I wrote so that I could test all three layers, including the database, which a public demo site would not let me do.

## What is tested

- **UI (`UiApplyTest`)**: valid application, 8 invalid-input cases (data driven), duplicate email
- **API (`ApiApplicantTest`)**: 10 tests for create, validation errors, duplicate, get by id, 404, list, approve, invalid status
- **Database (`DbVerificationTest`)**: 6 tests for stored values, status updates, no row on failed validation, unique email at database level, row count
- **End to end (`EndToEndTest`)**: UI, API and database together (approve and reject)

## Test results

Last full run on my machine (Java 17, Maven 3.9.9, Chrome headless): **28 tests, 0 failures, 0 errors, 0 skipped**.

| Test class | Tests | Result |
|------------|-------|--------|
| `ApiApplicantTest` | 10 | all passed |
| `DbVerificationTest` | 6 | all passed |
| `UiApplyTest` | 10 | all passed |
| `EndToEndTest` | 2 | all passed |

The same suite also runs on GitHub Actions (Ubuntu, Java 17) on every push. The workflow runs completed successfully (runs #1 to #3, badge above). A report and screenshots of failed UI tests are attached to each run as an artifact.

## Run it yourself

You need Java 17+, Maven and Google Chrome.

```
mvn test
```

The tests start the application on a random port, open a headless Chrome, and use an in-memory H2 database. Nothing else needs to be installed or running.

To use the app by hand: `mvn spring-boot:run`, then open http://localhost:8080

## Project structure

```
src/main/java      the application (controllers, entity, repository)
src/main/resources templates (Thymeleaf pages) and settings
src/test/java      page objects, test data, and all tests
.github/workflows  the CI pipeline
```

## What I learned and practised

- Designing page objects so locators live in one place
- Data-driven tests with `@ParameterizedTest`
- Checking the same business rule at UI, API and database level
- Taking a screenshot automatically when a UI test fails
- Running the whole suite in CI
